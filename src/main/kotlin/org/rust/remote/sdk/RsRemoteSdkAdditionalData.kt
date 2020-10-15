/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.remote.sdk

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.Key
import com.intellij.remote.*
import com.intellij.remote.ext.CredentialsCase
import com.intellij.remote.ext.CredentialsManager
import com.intellij.util.Consumer
import org.jdom.Element
import org.rust.ide.sdk.RsSdkAdditionalData

class RsRemoteSdkAdditionalData private constructor(
    remoteToolchainPath: String,
    private val remoteSdkProperties: RemoteSdkPropertiesHolder
) : RsSdkAdditionalData(),
    RemoteSdkProperties by remoteSdkProperties,
    RemoteSdkAdditionalData<RsRemoteSdkCredentials> {
    private val remoteConnectionCredentialsWrapper = RemoteConnectionCredentialsWrapper()

    val presentableDetails: String
        get() = remoteConnectionCredentialsWrapper.getPresentableDetails(remoteSdkProperties.interpreterPath)

    init {
        interpreterPath = remoteToolchainPath
    }

    constructor(remoteToolchainPath: String) : this(remoteToolchainPath, RemoteSdkPropertiesHolder(RUST_HELPERS))

    override fun connectionCredentials(): RemoteConnectionCredentialsWrapper {
        return remoteConnectionCredentialsWrapper
    }

    override fun <C> setCredentials(key: Key<C>, credentials: C) {
        remoteConnectionCredentialsWrapper.setCredentials(key, credentials)
    }

    override fun getRemoteConnectionType(): CredentialsType<*> {
        return remoteConnectionCredentialsWrapper.remoteConnectionType
    }

    override fun switchOnConnectionType(vararg cases: CredentialsCase<*>) {
        remoteConnectionCredentialsWrapper.switchType(*cases)
    }

    override fun setSdkId(sdkId: String?) {
        throw IllegalStateException("sdkId in this class is constructed based on fields, so it can't be set")
    }

    override fun getSdkId(): String = constructSdkId(remoteConnectionCredentialsWrapper, remoteSdkProperties)

    override fun getRemoteSdkCredentials(): RsRemoteSdkCredentials? = throw NotImplementedError()

    override fun getRemoteSdkCredentials(
        allowSynchronousInteraction: Boolean
    ): RsRemoteSdkCredentials? = throw NotImplementedError()

    override fun getRemoteSdkCredentials(
        project: Project?,
        allowSynchronousInteraction: Boolean
    ): RsRemoteSdkCredentials? = throw NotImplementedError()

    override fun produceRemoteSdkCredentials(
        remoteSdkCredentialsConsumer: Consumer<RsRemoteSdkCredentials>
    ) = throw NotImplementedError()

    override fun produceRemoteSdkCredentials(
        allowSynchronousInteraction: Boolean,
        remoteSdkCredentialsConsumer: Consumer<RsRemoteSdkCredentials>
    ) = throw NotImplementedError()

    override fun produceRemoteSdkCredentials(
        project: Project?,
        allowSynchronousInteraction: Boolean,
        remoteSdkCredentialsConsumer: Consumer<RsRemoteSdkCredentials>
    ) = throw NotImplementedError()

    fun copy(): RsRemoteSdkAdditionalData {
        val copy = RsRemoteSdkAdditionalData(remoteSdkProperties.interpreterPath)
        copyTo(copy)
        return copy
    }

    fun copyTo(copy: RsRemoteSdkAdditionalData) {
        remoteSdkProperties.copyTo(copy.remoteSdkProperties)
        remoteConnectionCredentialsWrapper.copyTo(copy.remoteConnectionCredentialsWrapper)
    }

    override fun save(rootElement: Element) {
        super.save(rootElement)
        remoteSdkProperties.save(rootElement)
        remoteConnectionCredentialsWrapper.save(rootElement)
    }

    companion object {
        private const val RUST_HELPERS: String = ".rust_helpers"

        fun load(sdk: Sdk, element: Element?): RsRemoteSdkAdditionalData {
            val path = sdk.homePath
            val data = RsRemoteSdkAdditionalData(RemoteSdkCredentialsHolder.getInterpreterPathFromFullPath(path))
            data.load(element)

            if (element != null) {
                CredentialsManager.getInstance().loadCredentials(path, element, data)
                data.remoteSdkProperties.load(element)
            }

            return data
        }

        private fun constructSdkId(
            remoteConnectionCredentialsWrapper: RemoteConnectionCredentialsWrapper,
            properties: RemoteSdkPropertiesHolder
        ): String = remoteConnectionCredentialsWrapper.id + properties.interpreterPath
    }

    override fun getRemoteSdkDataKey(): Any = remoteConnectionCredentialsWrapper.connectionKey
}
