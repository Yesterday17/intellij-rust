/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.cargo.runconfig.test

import com.intellij.execution.Executor
import com.intellij.execution.testframework.Printer
import com.intellij.execution.testframework.TestConsoleProperties
import com.intellij.execution.testframework.sm.SMCustomMessagesParsing
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.execution.testframework.sm.runner.SMTestLocator
import com.intellij.execution.ui.ConsoleViewContentType
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.runconfig.RsCommandConfiguration
import org.rust.cargo.toolchain.RsToolchain
import org.rust.ide.sdk.toolchain

class CargoTestConsoleProperties(
    config: RsCommandConfiguration,
    executor: Executor
) : SMTRunnerConsoleProperties(config, TEST_FRAMEWORK_NAME, executor), SMCustomMessagesParsing {
    private val toolchain: RsToolchain? = config.sdk?.toolchain ?: project.toolchain

    init {
        isIdBasedTestTree = true
    }

    override fun getTestLocator(): SMTestLocator = CargoTestLocator

    override fun createTestEventsConverter(
        testFrameworkName: String,
        consoleProperties: TestConsoleProperties
    ): OutputToGeneralTestEventsConverter = CargoTestEventsConverter(testFrameworkName, consoleProperties, toolchain)

    override fun printExpectedActualHeader(printer: Printer, expected: String, actual: String) {
        printer.print("\n", ConsoleViewContentType.ERROR_OUTPUT)
        printer.print("Left:  ", ConsoleViewContentType.SYSTEM_OUTPUT)
        printer.print("$actual\n", ConsoleViewContentType.ERROR_OUTPUT)
        printer.print("Right: ", ConsoleViewContentType.SYSTEM_OUTPUT)
        printer.print(expected, ConsoleViewContentType.ERROR_OUTPUT)
    }

    companion object {
        const val TEST_FRAMEWORK_NAME: String = "Cargo Test"
    }
}
