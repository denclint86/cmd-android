package com.niki.app

import android.content.Context
import com.niki.cmd.Shell
import com.niki.cmd.ShizukuShell

suspend fun test(context: Context) {
    val shell: Shell = ShizukuShell(context)
    if (shell.isAvailable()) {
        val result = shell.exec("echo test", 20_000L)
        println(result.stdout)
        println(result.stderr)
        println(result.exitCode)
    }
}