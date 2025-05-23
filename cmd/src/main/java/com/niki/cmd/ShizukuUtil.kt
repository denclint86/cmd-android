package com.niki.cmd

import android.content.Context
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

    suspend fun exec(command: String): ShellResult {
        val permissionResult = permissionHandler.ensurePermission()
        if (!permissionResult.isSuccess)
            return ShellResult.error(permissionResult.message)

        val serviceBindResult = userServiceBinder.bind()
        if (!serviceBindResult.isSuccess)
            return ShellResult.error(serviceBindResult.message)

        val execResult = shizukuShellExecutor.execute(command)

        return execResult.data
            ?: ShellResult.error("Shizuku service unsupported")
    }

    suspend fun exec(command: String, timeout: Long): ShellResult = withTimeoutOrNull(timeout) {
        exec(command)
    } ?: ShellResult.error("timeout running shizuku command")

    private suspend fun ensurePermission() =
        permissionHandler.ensurePermission().isSuccess
}