package com.niki.cmd.model

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.niki.cmd.IUserService
import com.niki.cmd.TAG
import com.niki.cmd.UserService
import com.niki.cmd.feedback
import com.niki.cmd.model.bean.Result
import com.niki.cmd.safeResume
import com.niki.cmd.shizuku.IUserServiceBinder
import kotlinx.coroutines.suspendCancellableCoroutine
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs

internal class UserServiceBinder(private val context: Context) : IUserServiceBinder<IUserService> {
    private var userService: IUserService? = null
    private var serviceConnection: ServiceConnection? = null
    private var userServiceArgs: UserServiceArgs? = null
    private var isRunning = false

    /**
     * 潜在并发安全问题
     */
    override suspend fun bind(): Result<IUserService> {
        if (isRunning && userService != null) {
            Log.d(TAG, "Shizuku service 已经连接")
            return Result(
                true, null, userService
            )
        }

        return requestPermission()
    }

    private suspend fun requestPermission(): Result<IUserService> =
        suspendCancellableCoroutine<Result<IUserService>> { continuation ->
            try {
                userServiceArgs = UserServiceArgs(
                    ComponentName(context, UserService::class.java.name)
                )
                    .daemon(true)
                    .processNameSuffix(context.packageName + ".shizuku.service")
                    .debuggable(true)

                serviceConnection = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName, service: IBinder) {
                        userService = IUserService.Stub.asInterface(service)
                        isRunning = true

                        Log.d(TAG, "Shizuku service 成功连接: ${name.className}")
                        continuation.safeResume(Result(true, null, userService))
                    }

                    override fun onServiceDisconnected(name: ComponentName) {
                        userService = null
                        isRunning = false

                        Log.d(TAG, "Shizuku service 断开: ${name.className}")
                    }
                }

                Shizuku.bindUserService(userServiceArgs!!, serviceConnection!!)
                continuation.invokeOnCancellation {
                    unbind()
                }
            } catch (t: Throwable) {
                Log.e(TAG, "绑定失败", t)
                continuation.safeResume(Result(false, "绑定时发生异常: ${t.feedback()}"))
            }
        }

    override fun unbind() {
        try {
            serviceConnection?.let { conn ->
                userServiceArgs?.let { args ->
                    Shizuku.unbindUserService(args, conn, true)
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "解绑出错", t)
        } finally {
            Log.d(TAG, "Shizuku service 解绑")
            userService = null
            serviceConnection = null
            userServiceArgs = null
            isRunning = false
        }
    }
}