/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl

import com.intellij.remote.CredentialsType
import com.intellij.remote.ext.CredentialsLanguageContribution
import org.rust.remote.RsCredentialsContribution

class RsWslCredentialsContribution : CredentialsLanguageContribution<RsCredentialsContribution>(),
                                     RsCredentialsContribution {
    override fun getType(): CredentialsType<*> = RsWslCredentialsType.getInstance()
    override fun getLanguageContributionClass() = RsCredentialsContribution::class.java
    override fun getLanguageContribution() = this
}
