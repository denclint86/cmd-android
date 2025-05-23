package com.niki.cmd.shizuku

import com.niki.cmd.model.bean.Result

internal interface IUserServiceBinder<T> {
    suspend fun bind(): Result<T>

    fun unbind()
}