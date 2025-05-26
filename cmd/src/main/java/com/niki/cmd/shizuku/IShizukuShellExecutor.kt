package com.niki.cmd.shizuku

import com.niki.cmd.model.bean.Result
import com.niki.cmd.model.bean.ShellResult

internal interface IShizukuShellExecutor<T> {
    suspend fun exec(command: String): Result<ShellResult>

    suspend fun exec(command: String, timeoutMillis: Long): Result<ShellResult>

    fun setBindNeededCallback(callback: (suspend () -> T?)?)
}