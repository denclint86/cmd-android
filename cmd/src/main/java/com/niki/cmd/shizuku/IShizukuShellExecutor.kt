package com.niki.cmd.shizuku

import com.niki.cmd.model.bean.Result
import com.niki.cmd.model.bean.ShellResult

internal interface IShizukuShellExecutor<T> {
    suspend fun execute(command: String): Result<ShellResult>

    fun setBindNeededCallback(callback: (suspend () -> T?)?)
}