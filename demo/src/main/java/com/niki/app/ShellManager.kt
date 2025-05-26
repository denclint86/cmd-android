package com.niki.app

import android.content.Context
import com.niki.cmd.RootShell
import com.niki.cmd.Shell
import com.niki.cmd.ShizukuShell
import com.niki.cmd.UserShell
import com.niki.cmd.model.bean.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShellManager(context: Context) {

    private val executors = listOf<Shell>(
        RootShell(),
        ShizukuShell(context),
        UserShell()
    )

    private var selectedShell: Shell? = null
    private val selectedName: String
        get() = selectedShell?.javaClass?.simpleName ?: "no_executor"

    private suspend fun selectExecutor() {
        selectedShell = executors.firstOrNull {
            it.isAvailable()
        }
    }

    suspend fun exec(command: String): ShellResult {
        selectExecutor().also {
            withContext(Dispatchers.Main) {
                "working as ${selectedShell?.PERMISSION_LEVEL}"
            }
        }
        val shell = selectedShell ?: throw Throwable("no shell was chosen")
//        if (!shell.isAvailable())
//            throw Throwable("$selectedName is unavailable!")
        return shell.exec(command, 5_000L)
    }
}