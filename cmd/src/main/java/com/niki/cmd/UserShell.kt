package com.niki.cmd

import com.niki.cmd.model.bean.ShellResult

class UserShell : Shell {
    override val TEST_TIMEOUT: Long = 20_000L
    override val PERMISSION_LEVEL: String = "User"

    override suspend fun isAvailable(): Boolean = true

    override suspend fun exec(command: String): ShellResult =
        execCommandWithTimeout(command, PERMISSION_LEVEL)

    override suspend fun exec(command: String, timeoutMillis: Long): ShellResult =
        execCommandWithTimeout(command, PERMISSION_LEVEL, timeoutMillis)
}