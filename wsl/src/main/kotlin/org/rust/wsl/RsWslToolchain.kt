/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.wsl.WSLDistribution
import com.intellij.openapi.projectRoots.Sdk
import org.rust.cargo.runconfig.RsProcessHandler
import org.rust.cargo.toolchain.RsToolchain
import org.rust.cargo.toolchain.RsToolchainProvider
import org.rust.ide.sdk.RsSdkAdditionalData
import org.rust.stdext.toPath
import java.io.File
import java.nio.file.Path

object RsWslToolchainProvider : RsToolchainProvider {
    override fun isApplicable(sdk: Sdk): Boolean = sdk.isWsl

    override fun getToolchain(sdk: Sdk): RsToolchain? {
        val homePath = sdk.homePath?.toPath() ?: return null
        val additionalData = sdk.sdkAdditionalData as? RsSdkAdditionalData ?: return null
        val toolchainName = additionalData.toolchainName
        val distribution = sdk.distribution?.successOrNull ?: return null
        return RsWslToolchain(homePath, toolchainName, distribution)
    }
}

class RsWslToolchain(
    location: Path,
    name: String?,
    private val distribution: WSLDistribution
) : RsToolchain(location, name) {

    override fun startProcess(commandLine: GeneralCommandLine): RsProcessHandler {
        val patchedCommandLine = patchCommandLine(commandLine)
        val processHandler = RsWslProcessHandler(patchedCommandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }

    override fun <T : GeneralCommandLine> patchCommandLine(commandLine: T): T {
        for (group in commandLine.parametersList.paramsGroups) {
            val params = ArrayList(group.parameters)
            group.parametersList.clearAll()
            group.parametersList.addAll(params.map { distribution.toRemotePath(it) })
        }

        commandLine.environment.forEach { (k, v) ->
            commandLine.environment[k] = distribution.toRemotePath(v)
        }

        commandLine.workDirectory?.let {
            if (it.path.startsWith("/")) {
                commandLine.workDirectory = File(distribution.getWindowsPath(it.path) ?: it.path)
            }
        }

        val remoteWorkDir = commandLine.workDirectory?.toString()
            ?.let { distribution.toRemotePath(it) }
        return distribution.patchCommandLine(commandLine, null, remoteWorkDir, false)
    }

    override fun expandUserHome(path: String): String = distribution.expandUserHome(path)

    override fun getExecutableName(toolName: String): String = toolName
}
