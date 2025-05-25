package com.niki.cmd

import com.niki.cmd.model.bean.ShellResult

class RootShell : Shell {
    override val TEST_TIMEOUT: Long = 20_000L
    override val PERMISSION_LEVEL: String = "Root"

    override suspend fun isAvailable(): Boolean {
        val result = execCommand("su -c echo test", PERMISSION_LEVEL)
        return judge(result)
    }

    override suspend fun exec(command: String): ShellResult =
        execCommand("su -c $command", PERMISSION_LEVEL)
}