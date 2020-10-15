/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl

import com.intellij.execution.wsl.WSLDistribution
import com.intellij.execution.wsl.WSLUtil
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.io.FileUtil
import org.rust.remote.sdk.RsRemoteSdkAdditionalData
import org.rust.stdext.Result
import java.io.File

internal const val WSL_CREDENTIALS_PREFIX: String = "wsl://"

internal val RsRemoteSdkAdditionalData.wslCredentials: RsWslCredentialsHolder?
    get() = connectionCredentials().credentials as? RsWslCredentialsHolder

internal val Sdk.distribution: Result<WSLDistribution, String>?
    get() = (sdkAdditionalData as? RsRemoteSdkAdditionalData)?.distribution

internal val RsRemoteSdkAdditionalData.distribution: Result<WSLDistribution, String>
    get() = wslCredentials?.distribution?.let { Result.Success(it) }
        ?: Result.Failure("Unknown distribution ${wslCredentials?.distributionId}")

internal fun WSLDistribution.toRemotePath(localPath: String): String =
    localPath.split(File.pathSeparatorChar).joinToString(":") { getWslPath(it) ?: it }

internal val Sdk.isWsl: Boolean
    get() = (sdkAdditionalData as? RsRemoteSdkAdditionalData)?.isWsl == true

internal val RsRemoteSdkAdditionalData.isWsl: Boolean
    get() = remoteConnectionType == RsWslCredentialsType.getInstance()

internal fun WSLDistribution.expandUserHome(path: String): String {
    if (!path.startsWith("~/")) return path
    val userHome = environment["HOME"] ?: return path
    return "$userHome${path.drop(1)}"
}

internal fun WSLDistribution.toUncPath(wslPath: String): String =
    WSLDistribution.UNC_PREFIX + msId + FileUtil.toSystemDependentName(wslPath)

internal fun parseUncPath(uncPath: String): Pair<WSLDistribution, String>? {
    if (!uncPath.startsWith(WSLDistribution.UNC_PREFIX)) return null
    val path = uncPath.removePrefix(WSLDistribution.UNC_PREFIX)
    val index = path.indexOf('\\')
    if (index == -1) return null
    val distName = path.substring(0, index)
    val distribution = WSLUtil.getDistributionByMsId(distName) ?: return null
    val wslPath = FileUtil.toSystemIndependentName(path.substring(index))
    return distribution to wslPath
}
