package com.niki.cmd

import kotlin.system.exitProcess

internal class UserService : IUserService.Stub() {

    override fun destroy() = exitProcess(0)

    override fun exit() = destroy()

    override fun exec(command: String?): ExecResult {
        val shellResult = execCommand(command, "Shizuku")
        val result = ExecResult().apply {
            this.stdout = shellResult.stdout
            this.stderr = shellResult.stderr
            this.exitCode = shellResult.exitCode
        }
        return result
    }
}