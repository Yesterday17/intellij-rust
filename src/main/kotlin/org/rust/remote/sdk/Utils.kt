/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.remote.sdk

import com.intellij.execution.process.ProcessOutput
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.Ref
import com.intellij.remote.RemoteSdkException
import com.intellij.remote.ext.LanguageCaseCollector
import com.intellij.util.ui.UIUtil
import org.rust.cargo.toolchain.impl.RustcVersion
import org.rust.openapiext.RsExecutionException
import org.rust.remote.RsCredentialsContribution

object RsRemoteSdkUtils {
    private val CUSTOM_RUST_SDK_HOME_PATH_PATTERN: Regex = "[-a-zA-Z_0-9]{2,}:.*".toRegex()

    fun isRemoteSdk(sdk: Sdk): Boolean = sdk.sdkAdditionalData is RsRemoteSdkAdditionalData

    /**
     * Returns whether provided Rust toolchain path corresponds to custom Rust SDK.
     *
     * @param homePath SDK home path
     * @return whether provided Rust toolchain path corresponds to Rust SDK
     */
    fun isCustomSdkHomePath(homePath: String): Boolean =
        CUSTOM_RUST_SDK_HOME_PATH_PATTERN.matches(homePath)

    fun isIncompleteRemote(sdk: Sdk): Boolean {
        if (!isRemoteSdk(sdk)) return false
        val additionalData = sdk.sdkAdditionalData as? RsRemoteSdkAdditionalData ?: return true
        return additionalData.isValid
    }

    fun hasInvalidRemoteCredentials(sdk: Sdk): Boolean {
        if (!isRemoteSdk(sdk)) return false
        val additionalData = sdk.sdkAdditionalData as? RsRemoteSdkAdditionalData ?: return false
        val result = Ref.create(false)
        additionalData.switchOnConnectionType(
            *object : LanguageCaseCollector<RsCredentialsContribution>() {
                override fun processLanguageContribution(
                    languageContribution: RsCredentialsContribution,
                    credentials: Any?
                ) {
                    result.set(credentials == null)
                }
            }.collectCases(RsCredentialsContribution::class.java)
        )
        return result.get()
    }

    fun createAndInitRemoteSdk(
        data: RsRemoteSdkAdditionalData,
        existingSdks: Collection<Sdk>,
        suggestedName: String? = null
    ): Sdk {
        // We do not pass `sdkName` so that `createRemoteSdk` generates it by itself
        val remoteSdk = RsRemoteSdkFactory.createRemoteSdk(null, data, suggestedName, existingSdks)
        RsRemoteSdkFactory.initSdk(remoteSdk, null, null)
        return remoteSdk
    }

    fun getRemoteToolchainVersion(
        project: Project?,
        data: RsRemoteSdkAdditionalData,
        nullForUnparsableVersion: Boolean
    ): RustcVersion? {
        val result = Ref.create<RustcVersion>(null)
        val exception = Ref.create<RemoteSdkException>(null)
        val task: Task.Modal = object : Task.Modal(project, "Getting Remote Toolchain Version", true) {
            override fun run(indicator: ProgressIndicator) {
                try {
                    val command = arrayOf("rustc", "--version", "--verbose")
                    val processOutput = RsRemoteProcessRunner.getManager(data)
                        .executeRemoteProcess(myProject, command, null, data)
                    if (processOutput.exitCode == 0) {
                        val rustcVersion = RustcVersion.parseRustcVersion(processOutput.stdoutLines)
                        if (rustcVersion != null || nullForUnparsableVersion) {
                            result.set(rustcVersion)
                            return
                        }
                    }
                    exception.set(createException(processOutput, command))
                } catch (e: Exception) {
                    exception.set(RemoteSdkException.cantObtainRemoteCredentials(e))
                }
            }
        }

        if (!ProgressManager.getInstance().hasProgressIndicator()) {
            UIUtil.invokeAndWaitIfNeeded(Runnable { ProgressManager.getInstance().run(task) })
        } else {
            task.run(ProgressManager.getInstance().progressIndicator)
        }

        if (!exception.isNull) {
            throw exception.get()
        }

        return result.get()
    }

    private fun createException(processOutput: ProcessOutput, command: Array<String>): RemoteSdkException {
        val exception = RsExecutionException("Can't obtain Rust version", command.first(), arrayListOf(*command), processOutput)
        return RemoteSdkException.cantObtainRemoteCredentials(exception)
    }
}
