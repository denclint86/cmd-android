package com.niki.cmd

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

internal val Any.TAG: String
    get() = this::class.java.simpleName

internal fun Throwable.feedback(): String {
    return "[logcat]: message: ${message}\ncause: $cause"
}

internal fun <T> CancellableContinuation<T>.safeResume(value: T) = runCatching {
    if (isActive) resume(value)
}