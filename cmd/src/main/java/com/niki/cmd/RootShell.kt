package com.niki.cmd

import android.util.Log
import com.niki.cmd.model.bean.ShellResult

class RootShell : Shell {
    companion object {
        private const val EXECUTOR_NAME = "Root"
    }

    private val util: BaseShellUtil by lazy { BaseShellUtil() }

    override suspend fun isAvailable(): Boolean = try {
        val result = util.execCommand("su -c echo test", Shell.TEST_TIMEOUT, EXECUTOR_NAME)
        result.isSuccess && result.stdout.trim() == "test"
    } catch (e: Exception) {
        Log.e(TAG, "$EXECUTOR_NAME 不可用", e)
        false
    }

    override suspend fun exec(command: String): ShellResult {
        return util.execCommand("su -c $command", null, EXECUTOR_NAME)
    }
}