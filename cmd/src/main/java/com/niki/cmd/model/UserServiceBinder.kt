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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs


internal class UserServiceBinder(private val context: Context) :
    IUserServiceBinder<IUserService> {
    private var userService: IUserService? = null
    private var serviceConnection: ServiceConnection? = null
    private var userServiceArgs: UserServiceArgs? = null
    private val bindMutex = Mutex()
    private var bindingDeferred: CompletableDeferred<Result<IUserService>>? = null

    override suspend fun bind(): Result<IUserService> = bindMutex.withLock {
        // 如果已经连接, 直接返回
        if (userService != null) {
            Log.d(TAG, "Shizuku service 已经连接")
            return Result(true, null, userService)
        }

        // 如果正在绑定中, 等待绑定结果
        bindingDeferred?.let { deferred ->
            Log.d(TAG, "Shizuku service 绑定中, 等待结果...")
            return deferred.await()
        }

        // 开始新的绑定操作
        val deferred = CompletableDeferred<Result<IUserService>>()
        bindingDeferred = deferred

        try {
            val result = requestPermission()
            deferred.complete(result)
            result
        } catch (t: Throwable) {
            val errorResult = Result<IUserService>(false, "绑定异常: ${t.feedback()}")
            deferred.complete(errorResult)
            errorResult
        } finally {
            bindingDeferred = null
        }
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
                        Log.d(TAG, "Shizuku service 成功连接: ${name.className}")
                        continuation.safeResume(Result(true, null, userService))
                    }

                    override fun onServiceDisconnected(name: ComponentName) {
                        bindMutex.tryLock().let { locked ->
                            try {
                                userService = null
                                bindingDeferred = null // 重置绑定状态
                                Log.d(TAG, "Shizuku service 断开: ${name.className}")
                            } finally {
                                if (locked) bindMutex.unlock()
                            }
                        }
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
            bindingDeferred = null // 重置绑定状态
        }
    }
}

//internal class UserServiceBinderV1(private val context: Context) : IUserServiceBinder<IUserService> {
//    private var userService: IUserService? = null
//    private var serviceConnection: ServiceConnection? = null
//    private var userServiceArgs: UserServiceArgs? = null
//
//    /**
//     * 潜在并发安全问题
//     */
//    override suspend fun bind(): Result<IUserService> {
//        if (userService != null) {
//            Log.d(TAG, "Shizuku service 已经连接")
//            return Result(
//                true, null, userService
//            )
//        }
//
//        return requestPermission()
//    }
//
//    private suspend fun requestPermission(): Result<IUserService> =
//        suspendCancellableCoroutine<Result<IUserService>> { continuation ->
//            try {
//                userServiceArgs = UserServiceArgs(
//                    ComponentName(context, UserService::class.java.name)
//                )
//                    .daemon(true)
//                    .processNameSuffix(context.packageName + ".shizuku.service")
//                    .debuggable(true)
//
//                serviceConnection = object : ServiceConnection {
//                    override fun onServiceConnected(name: ComponentName, service: IBinder) {
//                        userService = IUserService.Stub.asInterface(service)
//
//                        Log.d(TAG, "Shizuku service 成功连接: ${name.className}")
//                        continuation.safeResume(Result(true, null, userService))
//                    }
//
//                    override fun onServiceDisconnected(name: ComponentName) {
//                        userService = null
//
//                        Log.d(TAG, "Shizuku service 断开: ${name.className}")
//                    }
//                }
//
//                Shizuku.bindUserService(userServiceArgs!!, serviceConnection!!)
//                continuation.invokeOnCancellation {
//                    unbind()
//                }
//            } catch (t: Throwable) {
//                Log.e(TAG, "绑定失败", t)
//                continuation.safeResume(Result(false, "绑定时发生异常: ${t.feedback()}"))
//            }
//        }
//
//    override fun unbind() {
//        try {
//            serviceConnection?.let { conn ->
//                userServiceArgs?.let { args ->
//                    Shizuku.unbindUserService(args, conn, true)
//                }
//            }
//        } catch (t: Throwable) {
//            Log.e(TAG, "解绑出错", t)
//        } finally {
//            Log.d(TAG, "Shizuku service 解绑")
//            userService = null
//            serviceConnection = null
//            userServiceArgs = null
//        }
//    }
//}