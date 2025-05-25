package com.niki.app

import android.content.Context
import com.niki.cmd.RootShell
import com.niki.cmd.Shell
import com.niki.cmd.ShizukuShell
import com.niki.cmd.UserShell
import com.niki.cmd.model.bean.ShellResult

class ShellManager(context: Context) {

    private val executors = listOf<Shell>(
        RootShell(),
        ShizukuShell(context),
        UserShell()
    )

    private var selectedExecutor: Shell? = null
    private val selectedName: String
        get() = selectedExecutor?.javaClass?.simpleName ?: "no_executor"

    private suspend fun selectExecutor() {
        selectedExecutor = executors.firstOrNull {
            it.isAvailable()
        }
    }

    suspend fun exec(command: String): ShellResult {
        if (selectedExecutor == null)
            selectExecutor()
        val executor = selectedExecutor ?: throw Throwable("call select but no executor was chosen")
        if (!executor.isAvailable())
            throw Throwable("$selectedName is unavailable!")
        return executor.exec(command)
    }
}