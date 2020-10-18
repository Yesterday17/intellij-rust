/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.ide.sdk

import com.intellij.openapi.projectRoots.Sdk
import org.rust.ide.sdk.remote.RsRemoteSdkUtils.isRemoteSdk

class RsLocalSdkValidator : RsSdkValidator {
    override fun isInvalid(sdk: Sdk): Boolean {
        if (isRemoteSdk(sdk)) return false
        val toolchain = sdk.homeDirectory
        return toolchain == null || !toolchain.exists()
    }
}
