/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl

import com.intellij.execution.wsl.WSLUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.remote.CredentialsType
import com.intellij.remote.ext.CredentialsEditor
import com.intellij.remote.ext.CredentialsLanguageContribution
import com.intellij.remote.ext.RemoteCredentialsHandler
import com.intellij.remote.ui.BundleAccessor
import com.intellij.remote.ui.CredentialsEditorProvider
import com.intellij.remote.ui.RemoteSdkEditorForm

class RsWslCredentialsType : CredentialsType<RsWslCredentialsHolder>(WSL_CREDENTIALS_NAME, WSL_CREDENTIALS_PREFIX),
                             CredentialsEditorProvider {

    override fun getCredentialsKey(): Key<RsWslCredentialsHolder> = WSL_CREDENTIALS_KEY

    override fun getHandler(credentials: RsWslCredentialsHolder): RemoteCredentialsHandler =
        RsWslCredentialsHandler(credentials)

    override fun createCredentials(): RsWslCredentialsHolder = RsWslCredentialsHolder()

    override fun isAvailable(languageContribution: CredentialsLanguageContribution<*>?): Boolean =
        languageContribution is RsWslCredentialsContribution && WSLUtil.hasAvailableDistributions()

    override fun createEditor(
        project: Project?,
        languageContribution: CredentialsLanguageContribution<*>?,
        parentForm: RemoteSdkEditorForm
    ): CredentialsEditor<*> = RsWslCredentialsEditor()

    // TODO: expand user home
    override fun getDefaultInterpreterPath(bundleAccessor: BundleAccessor): String = "~/.cargo/bin"

    override fun getWeight(): Int = 50

    companion object {
        private const val WSL_CREDENTIALS_NAME = "WSL"
        private val WSL_CREDENTIALS_KEY: Key<RsWslCredentialsHolder> = Key.create("WSL_CREDENTIALS_HOLDER")

        fun getInstance(): RsWslCredentialsType = EP_NAME.findExtension(RsWslCredentialsType::class.java)!! // TODO
    }
}
