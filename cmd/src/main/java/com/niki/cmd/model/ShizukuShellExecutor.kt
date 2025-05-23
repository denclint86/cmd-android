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

    override suspend fun execute(command: String): Result<ShellResult> {
        if (userService.get() == null) {
            Log.d(TAG, "要求重新绑定 user service")
            userService = WeakReference(callback?.invoke())
        }

        val u = userService.get() ?: return Result(false, "user service 未绑定")

        val r = u.exec(command).run {
            ShellResult(stdout, stderr, exitCode)
        }

        return Result(true, null, r)
    }

    override fun setBindNeededCallback(callback: (suspend () -> IUserService?)?) {
        this.callback = callback
    }
}