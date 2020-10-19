/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.cargo.runconfig.wasmpack

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.util.execution.ParametersListUtil
import org.jdom.Element
import org.rust.cargo.WasmPackCommandLine
import org.rust.cargo.project.model.cargoProjects
import org.rust.cargo.runconfig.*
import org.rust.cargo.runconfig.command.workingDirectory
import org.rust.cargo.runconfig.ui.WasmPackCommandConfigurationEditor
import org.rust.cargo.toolchain.tools.wasmPack
import java.nio.file.Path

class WasmPackCommandConfiguration(
    project: Project,
    name: String,
    factory: ConfigurationFactory
) : RsCommandConfiguration(project, name, factory) {

    var command: String = "build"
    var workingDirectory: Path? = project.cargoProjects.allProjects.firstOrNull()?.workingDirectory

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        WasmPackCommandConfigurationEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState? {
        val wasmPack = toolchain?.wasmPack() ?: return null
        val workingDirectory = workingDirectory?.toFile() ?: return null

        return WasmPackCommandRunState(environment, this, wasmPack, workingDirectory)
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.writeString("command", command)
        element.writePath("workingDirectory", workingDirectory)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        element.readString("command")?.let { command = it }
        element.readPath("workingDirectory")?.let { workingDirectory = it }
    }

    override fun suggestedName(): String? {
        return command.substringBefore(' ').capitalize()
    }

    fun setFromCmd(cmd: WasmPackCommandLine) {
        command = ParametersListUtil.join(cmd.command, *cmd.additionalArguments.toTypedArray())
        workingDirectory = cmd.workingDirectory
    }
}
