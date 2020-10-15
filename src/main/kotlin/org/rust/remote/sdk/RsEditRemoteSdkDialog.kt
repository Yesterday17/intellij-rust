/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.remote.sdk

import com.intellij.openapi.projectRoots.Sdk

/**
 * Injected by [RsRemoteSdkEditor] to edit remote toolchain.
 */
interface RsEditRemoteSdkDialog {
    fun setEditing(data: RsRemoteSdkAdditionalData)
    fun showAndGet(): Boolean
    fun getSdk(): Sdk?
}
