package com.niki.cmd.shizuku

import com.niki.cmd.model.bean.Result

internal interface IPermissionHandler {
    suspend fun hasPermission(): Boolean

    suspend fun ensurePermission(): Result<Nothing>
}