/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.remote.sdk

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk

/**
 * EP to create custom [RsEditRemoteSdkDialog] to edit SDK.
 */
interface RsRemoteSdkEditor {
    fun isApplicable(data: RsRemoteSdkAdditionalData): Boolean
    fun createSdkEditorDialog(project: Project, existingSdks: List<Sdk>): RsEditRemoteSdkDialog

    companion object {
        private val EP: ExtensionPointName<RsRemoteSdkEditor> =
            ExtensionPointName.create("org.rust.remoteSdkEditor")

        private fun forData(data: RsRemoteSdkAdditionalData): RsRemoteSdkEditor? =
            EP.extensionList.find { it.isApplicable(data) }

        fun sdkEditor(
            data: RsRemoteSdkAdditionalData,
            project: Project,
            existingSdks: List<Sdk>
        ): RsEditRemoteSdkDialog? = forData(data)?.createSdkEditorDialog(project, existingSdks)
    }
}
