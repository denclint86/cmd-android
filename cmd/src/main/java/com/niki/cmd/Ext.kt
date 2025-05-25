package com.niki.cmd

import android.util.Log
import com.niki.cmd.model.bean.ShellResult
import kotlinx.coroutines.CancellableContinuation
import java.io.IOException
import kotlin.coroutines.resume

internal val Any.TAG: String
    get() = this::class.java.simpleName

internal fun Throwable.feedback(): String {
    return "[logcat]: message: ${message}\ncause: $cause"
}

internal fun <T> CancellableContinuation<T>.safeResume(value: T) = runCatching {
    if (isActive) resume(value)
}

internal fun execCommand(command: String?, tag: String): ShellResult {
    if (command.isNullOrBlank()) {
        Log.e(tag, "$tag: 命令为空")
        return ShellResult("", "Error: Empty command", -1)
    }

    var process: Process? = null
    try {
        process = Runtime.getRuntime().exec(command)
        val stdout = process.inputStream.bufferedReader().use { it.readText() }.trim()
        val stderr = process.errorStream.bufferedReader().use { it.readText() }.trim()
        val exitCode = process.waitFor()

        Log.d(tag, "$tag: 执行命令: $command -> stdout: $stdout")
        return ShellResult(stdout, stderr, exitCode)
    } catch (e: SecurityException) {
        Log.e(tag, "$tag: 权限不足: $command", e)
        return ShellResult("", "Permission denied: ${e.message}", -2)
    } catch (e: IOException) {
        Log.e(tag, "$tag: IO错误: $command", e)
        return ShellResult("", "IO error: ${e.message}", -3)
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        Log.e(tag, "$tag: 执行中断: $command", e)
        return ShellResult("", "Execution interrupted: ${e.message}", -4)
    } finally {
        process?.destroy()
    }
}