package com.niki.cmd.model

import android.util.Log
import com.niki.cmd.TAG
import com.niki.cmd.feedback
import com.niki.cmd.model.bean.Result
import com.niki.cmd.safeResume
import com.niki.cmd.shizuku.IPermissionHandler
import kotlinx.coroutines.suspendCancellableCoroutine
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener

internal class PermissionHandler : IPermissionHandler {
    companion object {
        private const val REQUEST_CODE_PERMISSION = 666
    }

    private var permissionResultListener: OnRequestPermissionResultListener? = null


    override suspend fun hasPermission(): Boolean {
        val hasPermission =
            (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED)

        Log.d(TAG, "Shizuku 鉴权结果: $hasPermission")
        return hasPermission
    }

    override suspend fun ensurePermission(): Result<Nothing> = when {
        hasPermission() ->
            Result(true)

        Shizuku.shouldShowRequestPermissionRationale() -> {
            Log.e(TAG, "用户拒绝授权 Shizuku")
            Result(false, "用户拒绝授权")
        }

        else ->
            requestPermission()
    }

    private suspend fun requestPermission(): Result<Nothing> = try {
        suspendCancellableCoroutine<Result<Nothing>> { continuation ->
            permissionResultListener =
                OnRequestPermissionResultListener { _, grantResult ->
                    // 协程取消前移除监听器
                    permissionResultListener?.let {
                        Shizuku.removeRequestPermissionResultListener(it)
                    }

                    val isGranted =
                        (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED)

                    Log.d(TAG, "Shizuku 授权结果: $isGranted")

                    val msg: String? = if (isGranted) null else "授权失败"

                    continuation.safeResume(
                        Result(isGranted, msg)
                    )
                }

            permissionResultListener?.let {
                Shizuku.addRequestPermissionResultListener(it)
            }

            Shizuku.requestPermission(REQUEST_CODE_PERMISSION)
        }
    } catch (t: Throwable) {
        Log.e(TAG, "授权失败", t)
        Result(false, "授权时发生异常: ${t.feedback()}")
    }
}