package com.niki.cmd

import com.niki.cmd.model.bean.ShellResult
import kotlinx.coroutines.withTimeoutOrNull

interface Shell {
    val TEST_TIMEOUT: Long
    val PERMISSION_LEVEL: String

    fun judge(result: ShellResult): Boolean {
        return (result.isSuccess && result.stdout == "test")
    }

    suspend fun isAvailable(): Boolean

    suspend fun isAvailable(timeoutMillis: Long): Boolean =
        withTimeoutOrNull(timeoutMillis) {
            isAvailable()
        } ?: false

    suspend fun exec(command: String): ShellResult

    suspend fun exec(command: String, timeoutMillis: Long): ShellResult
}
