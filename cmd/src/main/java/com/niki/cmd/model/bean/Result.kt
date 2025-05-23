package com.niki.cmd.model.bean

internal data class Result<T>(
    val isSuccess: Boolean,
    val message: String? = null,
    val data: T? = null
)