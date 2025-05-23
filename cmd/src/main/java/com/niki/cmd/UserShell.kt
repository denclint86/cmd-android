package com.niki.cmd

import com.niki.cmd.model.bean.ShellResult

class UserShell : Shell {
    companion object {
        private const val EXECUTOR_NAME = "User"
    }

    private val util: BaseShellUtil by lazy { BaseShellUtil() }

    override suspend fun isAvailable(): Boolean = true

    override suspend fun exec(command: String): ShellResult {
        return util.execCommand(command, null, EXECUTOR_NAME)
    }
}