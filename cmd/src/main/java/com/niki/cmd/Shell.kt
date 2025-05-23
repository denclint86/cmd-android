package com.niki.cmd

import com.niki.cmd.model.bean.ShellResult
import kotlinx.coroutines.withTimeoutOrNull

interface Shell {
    companion object {
        const val TEST_TIMEOUT = 10_000L
    }

    suspend fun isAvailable(): Boolean

    /**
     * 无超时限制的命令执行, 这是极不安全和不稳定的的
     */
    suspend fun exec(command: String): ShellResult

    suspend fun exec(command: String, timeoutMillis: Long): ShellResult =
        withTimeoutOrNull(timeoutMillis) {
            exec(command)
        } ?: ShellResult.error("timeout running command")
}
