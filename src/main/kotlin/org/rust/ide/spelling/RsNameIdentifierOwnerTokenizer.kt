/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.spelling

import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.spellchecker.inspections.IdentifierSplitter
import com.intellij.spellchecker.tokenizer.TokenConsumer
import com.intellij.spellchecker.tokenizer.Tokenizer
import org.rust.lang.core.psi.RS_RAW_PREFIX
import org.rust.lang.core.psi.RsElementTypes
import org.rust.lang.core.psi.ext.RsNameIdentifierOwner
import org.rust.lang.core.psi.ext.elementType
import org.rust.lang.core.psi.ext.startOffset
import org.rust.lang.core.psi.unescapeIdentifier

// Inspired by `PsiIdentifierOwnerTokenizer`
object RsNameIdentifierOwnerTokenizer : Tokenizer<RsNameIdentifierOwner>() {

    override fun tokenize(element: RsNameIdentifierOwner, consumer: TokenConsumer) {
        val identifier = element.nameIdentifier ?: return
        val range = identifier.textRange
        if (range.isEmpty) return

        var offset = range.startOffset - element.startOffset
        val parent = if (offset < 0) {
            val commonParent = PsiTreeUtil.findCommonParent(identifier, element) ?: return
            offset = range.startOffset - commonParent.startOffset
            commonParent
        } else {
            element
        }
        val text = identifier.text
        val (unescapedText, offsetShift) = when {
            identifier.elementType == RsElementTypes.QUOTE_IDENTIFIER -> text.removePrefix("'") to 1
            text.startsWith(RS_RAW_PREFIX) -> text.unescapeIdentifier() to 2
            else -> text to 0
        }
        offset += offsetShift
        consumer.consumeToken(parent, unescapedText, true, offset, TextRange.allOf(unescapedText), IdentifierSplitter.getInstance())
    }
}
