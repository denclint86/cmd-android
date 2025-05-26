package com.niki.cmd

import android.content.Context
import com.niki.cmd.model.bean.ShellResult

class ShizukuShell(
    context: Context
) : Shell {
    override val TEST_TIMEOUT: Long = 20_000L
    override val PERMISSION_LEVEL: String = "Shizuku"

    private val shizukuUtil by lazy { ShizukuUtil(context) }

    override suspend fun isAvailable(): Boolean {
        val result = shizukuUtil.exec("echo test", TEST_TIMEOUT)
        return judge(result)
    }

    override suspend fun exec(command: String): ShellResult =
        shizukuUtil.exec(command)

    override suspend fun exec(command: String, timeoutMillis: Long): ShellResult =
        shizukuUtil.exec(command, timeoutMillis)
}