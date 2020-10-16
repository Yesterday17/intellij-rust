/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.cargo.toolchain

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import org.rust.cargo.runconfig.RsProcessHandler
import org.rust.remote.sdk.RsRemoteSdkUtils.isCustomSdkHomePath
import org.rust.stdext.toPath
import java.nio.file.Path

object RsLocalToolchainProvider : RsToolchainProvider {
    override fun isApplicable(homePath: String): Boolean = !isCustomSdkHomePath(homePath)

    override fun getToolchain(homePath: String, toolchainName: String?): RsToolchain =
        RsLocalToolchain(homePath.toPath(), toolchainName)
}

class RsLocalToolchain(location: Path, name: String?) : RsToolchain(location, name) {

    override fun startProcess(commandLine: GeneralCommandLine): RsProcessHandler {
        val handler = RsProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        return handler
    }

    override fun <T : GeneralCommandLine> patchCommandLine(commandLine: T): T = commandLine

    override fun expandUserHome(path: String): String = FileUtil.expandUserHome(path)

    override fun getExecutableName(toolName: String): String =
        if (SystemInfo.isWindows) "$toolName.exe" else toolName
}
