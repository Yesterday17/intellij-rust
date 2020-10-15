/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.wsl

import com.intellij.execution.wsl.WSLDistribution
import com.intellij.execution.wsl.WSLUtil
import org.jdom.Element

class RsWslCredentialsHolder {
    private var _distributionId: String? = null
    var distributionId: String
        get() = checkNotNull(_distributionId) { "Invoked get before initializing" }
        set(distributionId) {
            _distributionId = distributionId
        }

    val distribution: WSLDistribution?
        get() = WSLUtil.getDistributionById(distributionId)

    fun save(rootElement: Element) {
        if (_distributionId == null) {
            throw RuntimeException("Invoked save before initializing")
        }
        rootElement.setAttribute(DISTRIBUTION_ID, _distributionId)
    }

    fun load(rootElement: Element) {
        _distributionId = rootElement.getAttributeValue(DISTRIBUTION_ID)
    }

    companion object {
        private const val DISTRIBUTION_ID: String = "DISTRIBUTION_ID"
    }
}
