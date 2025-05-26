package com.niki.cmd.model

import android.util.Log
import com.niki.cmd.IUserService
import com.niki.cmd.TAG
import com.niki.cmd.model.bean.Result
import com.niki.cmd.model.bean.ShellResult
import com.niki.cmd.shizuku.IShizukuShellExecutor
import java.lang.ref.WeakReference

internal class ShizukuShellExecutor(
    private var userService: WeakReference<IUserService>
) : IShizukuShellExecutor<IUserService> {
    private var callback: (suspend () -> IUserService?)? = null

    override suspend fun exec(command: String): Result<ShellResult> = execInternal(command, 0L)

    override suspend fun exec(command: String, timeoutMillis: Long): Result<ShellResult> =
        execInternal(command, timeoutMillis)

    override fun setBindNeededCallback(callback: (suspend () -> IUserService?)?) {
        this.callback = callback
    }

    private suspend fun getService(): IUserService? {
        return userService.get() ?: run {
            Log.d(TAG, "要求重新绑定 user service")
            val service = callback?.invoke()
            userService = WeakReference(service)
            return service
        }
    }

    private suspend fun execInternal(command: String, timeout: Long? = 0L): Result<ShellResult> {
        val service = getService() ?: return Result(false, "user service 未绑定")

        val t = timeout ?: 0L

        val r = service.exec(command, t).run {
            ShellResult(stdout, stderr, exitCode)
        }

        return Result(true, null, r)
    }
}