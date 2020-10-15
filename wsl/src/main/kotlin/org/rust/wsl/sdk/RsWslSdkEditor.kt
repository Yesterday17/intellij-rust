/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl.sdk

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import org.rust.remote.sdk.RsEditRemoteSdkDialog
import org.rust.remote.sdk.RsRemoteSdkAdditionalData
import org.rust.remote.sdk.RsRemoteSdkEditor
import org.rust.wsl.isWsl

class RsWslSdkEditor : RsRemoteSdkEditor {

    override fun isApplicable(data: RsRemoteSdkAdditionalData): Boolean = data.isWsl

    override fun createSdkEditorDialog(project: Project, existingSdks: List<Sdk>): RsEditRemoteSdkDialog =
        RsEditWslSdkDialog(project, existingSdks)
}
