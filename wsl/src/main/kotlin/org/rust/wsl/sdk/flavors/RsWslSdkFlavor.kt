/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl.sdk.flavors

import com.intellij.execution.wsl.WSLUtil
import com.intellij.openapi.application.Experiments
import com.intellij.util.io.isDirectory
import org.rust.cargo.toolchain.Rustup
import org.rust.ide.sdk.flavors.RsSdkFlavor
import org.rust.ide.sdk.flavors.RsSdkFlavor.Companion.hasExecutable
import org.rust.stdext.toPath
import org.rust.wsl.expandUserHome
import java.nio.file.Path

object RsWslSdkFlavor : RsSdkFlavor {
    override fun isApplicable(): Boolean = WSLUtil.isSystemCompatible()
        && Experiments.getInstance().isFeatureEnabled("wsl.p9.support")

    @Suppress("UnstableApiUsage")
    override fun getHomePathCandidates(): List<Path> {
        val result = mutableListOf<Path>()
        for (distribution in WSLUtil.getAvailableDistributions()) {
            val root = distribution.uncRoot.absolutePath.toPath()
            val path = root.resolve(distribution.expandUserHome("~/.cargo/bin"))
            if (path.isDirectory()) result.add(path)
        }
        return result
    }

    override fun isValidSdkPath(sdkPath: Path): Boolean =
        super.isValidSdkPath(sdkPath) && sdkPath.hasExecutable(Rustup.NAME)
}
