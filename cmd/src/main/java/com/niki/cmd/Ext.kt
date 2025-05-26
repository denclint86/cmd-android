package com.niki.cmd

import android.util.Log
import com.niki.cmd.model.bean.ShellResult
import kotlinx.coroutines.CancellableContinuation
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume

internal val Any.TAG: String
    get() = this::class.java.simpleName

internal fun Throwable.feedback(): String {
    return "[logcat]: message: ${message}\ncause: $cause"
}

internal fun <T> CancellableContinuation<T>.safeResume(value: T) = runCatching {
    if (isActive) resume(value)
}

internal fun execCommandWithTimeout(
    command: String?,
    tag: String,
    timeoutMillis: Long? = 0L
): ShellResult {
    if (command.isNullOrBlank()) {
        Log.e(tag, "$tag: 命令为空")
        return ShellResult("", "Error: Empty command", -1)
    }

    val timeout = timeoutMillis ?: 0L

    var process: Process? = null
    try {
        process = Runtime.getRuntime().exec(command)

        val result = if (timeout > 0) {
            executeWithTimeout(process, command, tag, timeout)
        } else {
            executeWithoutTimeout(process, command, tag)
        }

        return result
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
    } catch (e: TimeoutException) {
        Log.e(tag, "$tag: 执行超时: $command", e)
        return ShellResult("", "Execution timeout: ${e.message}", -5)
    } finally {
        process?.destroy()
    }
}

private fun executeWithTimeout(
    process: Process,
    command: String,
    tag: String,
    timeoutMillis: Long
): ShellResult {
    val finished = process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)

    if (!finished) {
        process.destroyForcibly()
        throw TimeoutException("Command execution timed out after $timeoutMillis ms")
    }

    return readProcessOutput(process, command, tag)
}

private fun executeWithoutTimeout(process: Process, command: String, tag: String): ShellResult {
    process.waitFor()
    return readProcessOutput(process, command, tag)
}

private fun readProcessOutput(process: Process, command: String, tag: String): ShellResult {
    val stdout = process.inputStream.bufferedReader().use { it.readText() }.trim()
    val stderr = process.errorStream.bufferedReader().use { it.readText() }.trim()
    val exitCode = process.exitValue()

    Log.d(tag, "$tag: 执行命令: $command -> stdout: $stdout")
    return ShellResult(stdout, stderr, exitCode)
}