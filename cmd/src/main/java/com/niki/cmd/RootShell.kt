package com.niki.cmd

import com.niki.cmd.model.bean.ShellResult

class RootShell : Shell {
    override val TEST_TIMEOUT: Long = 20_000L
    override val PERMISSION_LEVEL: String = "Root"

    override suspend fun isAvailable(): Boolean {
        val result = execCommandWithTimeout("su -c echo test", PERMISSION_LEVEL, TEST_TIMEOUT)
        return judge(result)
    }

    override suspend fun exec(command: String): ShellResult =
        execCommandWithTimeout("su -c $command", PERMISSION_LEVEL)

    override suspend fun exec(command: String, timeoutMillis: Long): ShellResult =
        execCommandWithTimeout("su -c $command", PERMISSION_LEVEL, timeoutMillis)
}