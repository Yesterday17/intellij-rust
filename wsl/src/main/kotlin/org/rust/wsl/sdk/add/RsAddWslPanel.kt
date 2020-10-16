/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl.sdk.add

import com.intellij.execution.wsl.WSLDistributionWithRoot
import com.intellij.execution.wsl.WSLUtil
import com.intellij.icons.AllIcons
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.ui.ComponentWithBrowseButton
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Disposer
import com.intellij.remote.CredentialsType
import com.intellij.ui.PanelWithAnchor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.layout.panel
import com.intellij.util.ui.FormBuilder
import org.rust.ide.sdk.RsDetectedSdk
import org.rust.ide.sdk.RsSdkAdditionalData
import org.rust.ide.sdk.RsSdkAdditionalDataPanel
import org.rust.ide.sdk.RsSdkAdditionalDataPanel.Companion.validateSdkAdditionalDataPanel
import org.rust.ide.sdk.RsSdkPathChoosingComboBox
import org.rust.ide.sdk.RsSdkPathChoosingComboBox.Companion.addToolchainsAsync
import org.rust.ide.sdk.RsSdkPathChoosingComboBox.Companion.validateSdkComboBox
import org.rust.ide.sdk.RsSdkUtils.detectRustSdks
import org.rust.ide.sdk.add.RsAddSdkPanel
import org.rust.openapiext.RsExecutionException
import org.rust.remote.sdk.RsRemoteSdkAdditionalData
import org.rust.remote.sdk.RsRemoteSdkUtils.createAndInitRemoteSdk
import org.rust.remote.sdk.RsRemoteSdkUtils.getRemoteToolchainVersion
import org.rust.stdext.Result
import org.rust.wsl.*
import java.awt.BorderLayout
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import javax.swing.Icon
import javax.swing.JTextPane

private const val URL: String = "ms-windows-store://search/?query=Linux"
private const val MESSAGE: String = "<html>You don't have WSL distribution installed. <a href=\"$URL\">Install WSL distributions.</a></html>"

class RsAddWslSdkPanel(private val existingSdks: List<Sdk>) : RsAddSdkPanel() {
    override val panelName: String = "WSL"
    override val icon: Icon = AllIcons.RunConfigurations.Wsl

    private val sdkPathComboBox: RsSdkPathChoosingComboBox = RsSdkPathChoosingComboBox()
    private val homePath: String? get() = sdkPathComboBox.selectedSdk?.homePath

    private val sdkAdditionalDataPanel: RsSdkAdditionalDataPanel = RsSdkAdditionalDataPanel()
    private val data: RsSdkAdditionalData? get() = sdkAdditionalDataPanel.data

    private val credentialsType: CredentialsType<RsWslCredentialsHolder> = RsWslCredentialsType.getInstance()
    private val credentialsEditor: RsWslCredentialsEditor by lazy { RsWslCredentialsEditor() }

    private var preparedSdk: Sdk? = null
    override val sdk: Sdk? get() = preparedSdk

    private val browser: RsWslPathBrowser = RsWslPathBrowser(interpreterPathField)

    init {
        if (!WSLUtil.hasAvailableDistributions()) {
            layout = BorderLayout()
            add(Messages.configureMessagePaneUi(JTextPane(), MESSAGE), BorderLayout.NORTH)
        } else {
            initUI()
        }
        interpreterPathField.text = "/usr/bin/python"
    }

    override fun onSelected() = credentialsEditor.onSelected()

    override fun complete() {
        val sdkAdditionalData = RsRemoteSdkAdditionalData(interpreterPathField.text)
        val credentials = credentialsType.createCredentials()
        credentialsEditor.saveCredentials(credentials)
        sdkAdditionalData.setCredentials(credentialsType.credentialsKey, credentials)
        val createAndInitRemoteSdk = createSdk(sdkAdditionalData)
        preparedSdk = createAndInitRemoteSdk
    }

    private fun createSdk(additionalData: RsRemoteSdkAdditionalData): Sdk {
        return createAndInitRemoteSdk(additionalData, existingSdks)
    }

    private fun validateDataAndSuggestName(sdkAdditionalData: RsRemoteSdkAdditionalData): String? {
        val versionString = getRemoteToolchainVersion(null, sdkAdditionalData, false)
            ?: throw RsExecutionException("Bad command", sdkAdditionalData.interpreterPath, emptyList())

        return when (val it = sdkAdditionalData.distribution) {
            is Result.Success -> "$versionString @ ${it.result.presentableName}"
            is Result.Failure -> "Error: ${it.error}"
        }
    }

    internal fun configure(data: PyRemoteSdkAdditionalData) {
        interpreterPathField.text = data.interpreterPath
        data.wslCredentials.let {
            credentialsEditor.init(it)
        }
    }

    init {
        layout = BorderLayout()
        val formPanel = panel {
            row("Toolchain path:") { sdkPathComboBox() }
            sdkAdditionalDataPanel.attachTo(this)
        }
        add(formPanel, BorderLayout.NORTH)

        sdkPathComboBox.childComponent.addItemListener { event ->
            if (event.stateChange == ItemEvent.SELECTED) {
                sdkAdditionalDataPanel.notifySdkHomeChanged(homePath)
            }
        }
        addToolchainsAsync(sdkPathComboBox) { detectRustSdks(existingSdks) }
    }

    private fun initUI() {
        layout = BorderLayout()

        val interpreterPathLabel = JBLabel("Toolchain path:")

        val form = FormBuilder()
            .addComponent(credentialsEditor.mainPanel)

        val listener = getBrowseButtonActionListener()
        if (listener != null) {
            form.addLabeledComponent(interpreterPathLabel, ComponentWithBrowseButton(interpreterPathField, listener))
        } else {
            form.addLabeledComponent(interpreterPathLabel, interpreterPathField)
        }

        (credentialsEditor as? PanelWithAnchor)?.anchor = interpreterPathLabel

        add(form.panel, BorderLayout.NORTH)
    }

    /**
     * If return value is not null then interpreter path has "browse" button with this listener
     */
    private fun getBrowseButtonActionListener(): ActionListener = ActionListener {
        credentialsEditor.wslDistribution?.let { distro ->
            browser.browsePath(
                WSLDistributionWithRoot(distro),
                credentialsEditor.mainPanel
            )
        }
    }

    override fun getOrCreateSdk(): Sdk? =
        when (val sdk = sdkPathComboBox.selectedSdk) {
            is RsDetectedSdk -> data?.let { sdk.setup(existingSdks, it) }
            else -> sdk
        }

    override fun validateAll(): List<ValidationInfo> = listOfNotNull(
        validateWslDistributions(),
        credentialsEditor.validate(),
        validateSdkComboBox(sdkPathComboBox),
        validateSdkAdditionalDataPanel(sdkAdditionalDataPanel)
    )

    override fun dispose() {
        Disposer.dispose(sdkAdditionalDataPanel)
    }

    companion object {
        private fun validateWslDistributions(): ValidationInfo? =
            if (!WSLUtil.hasAvailableDistributions()) {
                ValidationInfo("Can't find installed WSL distribution. Make sure you have one.")
            } else {
                null
            }
    }
}
