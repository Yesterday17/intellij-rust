/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package org.rust.stdext

/**
 * Operation result to be used with pattern matching.
 * Must be replaced with stdlib solution after [https://github.com/Kotlin/KEEP/blob/master/proposals/stdlib/result.md] completion.
 *
 * Can't be moved to core module because core modules do not support Kotlin and there is no sealed classes in java.
 */
sealed class Result<SUCC, ERR> {
    data class Failure<SUCC, ERR>(val error: ERR) : Result<SUCC, ERR>()
    data class Success<SUCC, ERR>(val result: SUCC) : Result<SUCC, ERR>()

    fun <RES> map(map: (SUCC) -> RES): Result<RES, ERR> =
        when (this) {
            is Success -> Success(map(result))
            is Failure -> Failure(error)
        }

    val successOrNull: SUCC? get() = if (this is Success) result else null
}
