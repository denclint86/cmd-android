package com.niki.cmd

import android.content.Context
import android.util.Log
import com.niki.cmd.model.PermissionHandler
import com.niki.cmd.model.ShizukuShellExecutor
import com.niki.cmd.model.UserServiceBinder
import com.niki.cmd.model.bean.ShellResult
import com.niki.cmd.shizuku.IPermissionHandler
import com.niki.cmd.shizuku.IShizukuShellExecutor
import com.niki.cmd.shizuku.IUserServiceBinder
import kotlinx.coroutines.withTimeoutOrNull
import java.lang.ref.WeakReference

internal class ShizukuUtil(context: Context) {
    private val permissionHandler: IPermissionHandler = PermissionHandler()
    private val userServiceBinder: IUserServiceBinder<IUserService> by lazy {
        UserServiceBinder(context)
    }
    private val shizukuShellExecutor: IShizukuShellExecutor<IUserService> by lazy {
        ShizukuShellExecutor(
            WeakReference(null)
        )
    }

    init {
        shizukuShellExecutor.setBindNeededCallback {
            if (ensurePermission()) {
                userServiceBinder.bind().data
            } else {
                null
            }
        }
    }

    suspend fun exec(command: String): ShellResult =
        execInternal(command)

    suspend fun exec(command: String, timeout: Long): ShellResult = withTimeoutOrNull(timeout) {
        execInternal(command, timeout) // 这里比较复杂, 用多个超时机制保证正确处理超时事件
    } ?: ShellResult.error("timeout running shizuku command")

    private suspend fun ensurePermission() =
        permissionHandler.ensurePermission().isSuccess

    private suspend fun execInternal(command: String, timeout: Long = 0L): ShellResult {
        val start = System.currentTimeMillis()
        val permissionResult = permissionHandler.ensurePermission()
        if (!permissionResult.isSuccess)
            return ShellResult.error(permissionResult.message)

        val serviceBindResult = userServiceBinder.bind()
        if (!serviceBindResult.isSuccess)
            return ShellResult.error(serviceBindResult.message)

        val t = timeout - (System.currentTimeMillis() - start)
        if (t < 0) {
            return ShellResult.error("timeout running shizuku command")
        }

        val execResult = shizukuShellExecutor.exec(command, t)

        execResult.data?.let {
            Log.d(TAG, "Shizuku util 正常读取: $it")
        }

        return execResult.data
            ?: ShellResult.error("Shizuku service unsupported")
    }
}