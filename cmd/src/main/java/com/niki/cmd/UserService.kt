package com.niki.cmd

import kotlin.system.exitProcess

internal class UserService : IUserService.Stub() {

    override fun destroy() = exitProcess(0)

    override fun exit() = destroy()

    override fun exec(command: String, timeoutMillis: Long): ExecResult {
        val shellResult = execCommandWithTimeout(command, "Shizuku", timeoutMillis)
        val result = ExecResult().apply {
            this.stdout = shellResult.stdout
            this.stderr = shellResult.stderr
            this.exitCode = shellResult.exitCode
        }
        return result
    }
}