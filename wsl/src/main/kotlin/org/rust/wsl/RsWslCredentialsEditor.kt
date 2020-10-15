/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl

import com.intellij.execution.wsl.WSLDistribution
import com.intellij.execution.wsl.WSLUtil
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.remote.RemoteSdkAdditionalData
import com.intellij.remote.ext.CredentialsEditor
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.layout.panel
import java.util.function.Consumer
import java.util.function.Supplier
import javax.swing.JPanel

class RsWslCredentialsEditor : CredentialsEditor<RsWslCredentialsHolder> {
    private val comboBoxModel: CollectionComboBoxModel<WSLDistribution> = run {
        val distributions = WSLUtil.getAvailableDistributions()
        distributions.sortedBy { it.presentableName }
        CollectionComboBoxModel(distributions)
    }

    private val comboBox: ComboBox<WSLDistribution> = ComboBox<WSLDistribution>().apply {
        model = comboBoxModel
        renderer = SimpleListCellRenderer.create("") { it.presentableName }
    }

    private val panel: JPanel = panel {
        row("Linux Distribution:") { comboBox() }
    }

    val wslDistribution: WSLDistribution?
        get() = comboBoxModel.selected

    override fun getName(): String = RsWslCredentialsType.getInstance().name

    override fun getMainPanel(): JPanel = panel

    override fun onSelected() {}

    override fun validate(): ValidationInfo? =
        if (comboBoxModel.selected == null) {
            ValidationInfo("Please, select a distribution", comboBox)
        } else {
            null
        }

    override fun validateFinal(
        supplier: Supplier<out RemoteSdkAdditionalData<*>?>,
        helpersPathUpdateCallback: Consumer<String>
    ): String? = null

    override fun saveCredentials(credentials: RsWslCredentialsHolder) {
        val selectedItem = wslDistribution ?: throw RuntimeException("Saving invalid credentials")
        credentials.distributionId = selectedItem.id
    }

    override fun init(credentials: RsWslCredentialsHolder) {
        comboBoxModel.selectedItem = WSLUtil.getDistributionById(credentials.distributionId)
    }
}
