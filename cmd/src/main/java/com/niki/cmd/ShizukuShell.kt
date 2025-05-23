package com.niki.cmd

import android.content.Context
import android.util.Log
import com.niki.cmd.model.bean.ShellResult

class ShizukuShell(context: Context) : Shell {
    companion object {
        private const val EXECUTOR_NAME = "Shizuku"
    }

    private val shizukuUtil by lazy { ShizukuUtil(context) }

    override suspend fun isAvailable(): Boolean = try {
        val result = shizukuUtil.exec("echo test", Shell.TEST_TIMEOUT)
        Log.d(TAG, "$EXECUTOR_NAME 可用性检查: echo test -> ${result.stdout}")
        result.isSuccess && result.stdout == "test"
    } catch (e: Exception) {
        Log.e(TAG, "$EXECUTOR_NAME 可用性检查失败: echo test", e)
        false
    }

    override suspend fun exec(command: String): ShellResult = try {
        val result = shizukuUtil.exec(command)
        Log.d(TAG, "$EXECUTOR_NAME 执行: $command -> ${result.stdout}")
        result
    } catch (e: Exception) {
        Log.e(TAG, "$EXECUTOR_NAME 执行失败: $command", e)
        ShellResult.error(e.feedback())
    }
}