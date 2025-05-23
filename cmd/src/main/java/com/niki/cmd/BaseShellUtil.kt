package com.niki.cmd

import android.util.Log
import com.niki.cmd.model.bean.ShellResult
import java.io.IOException
import java.util.concurrent.TimeUnit

internal class BaseShellUtil {
    fun execCommand(
        command: String,
        timeoutMillis: Long?,
        logPrefix: String
    ): ShellResult = try {
        val result = executeProcess(command, timeoutMillis)
        Log.d(TAG, "$logPrefix 执行: $command -> ${result.stdout}")
        result
    } catch (e: Exception) {
        Log.e(TAG, "$logPrefix 执行失败: $command", e)
        ShellResult.error(e.feedback())
    }

    private fun executeProcess(command: String, timeoutMillis: Long?): ShellResult {
        val process = Runtime.getRuntime().exec(command)
        try {
            val stdout = process.inputStream.bufferedReader().use { it.readText() }
            val stderr = process.errorStream.bufferedReader().use { it.readText() }

            val hasTimedOut = if (timeoutMillis != null && timeoutMillis > 0)
                !process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)
            else {
                process.waitFor()
                false
            }

            val exitCode = if (hasTimedOut) {
                process.destroy()
                -1 // 超时视为失败
            } else {
                process.exitValue()
            }
            return ShellResult(stdout.trim(), stderr.trim(), exitCode)
        } catch (e: SecurityException) {
            return ShellResult(
                "",
                "Permission denied: ${e.message}", -2
            )
        } catch (e: IOException) {
            return ShellResult(
                "",
                "IO error: ${e.message}", -3
            )
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            return ShellResult(
                "", "Execution interrupted: ${e.message}", -4
            )
        } finally {
            process.destroy() // 确保进程被销毁
        }
    }
}