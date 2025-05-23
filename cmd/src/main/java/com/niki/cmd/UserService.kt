package com.niki.cmd

import android.os.Build
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.system.exitProcess

internal class UserService : IUserService.Stub() {
    override fun destroy() = exitProcess(0)

    override fun exit() = destroy()

    override fun exec(command: String?): ExecResult {
        var process: Process? = null
        val stdoutSB = StringBuilder()
        val stderrSB = StringBuilder()

        val result = ExecResult().apply {
            stdout = ""
            stderr = ""
            exitCode = 0
        }

        if (command == null || command.trim { it <= ' ' }.isEmpty()) {
            result.apply {
                stdout = ""
                stderr = "Error: Empty command"
                exitCode = -1
            }
            return result
        }

        try {
            process = Runtime.getRuntime()
                .exec(command.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while ((stdoutReader.readLine().also { line = it }) != null) {
                stdoutSB.append(line).append('\n')
            }
            stdoutReader.close()

            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))
            while ((stderrReader.readLine().also { line = it }) != null) {
                stderrSB.append(line).append('\n')
            }
            stderrReader.close()

            result.apply {
                stdout = stdoutSB.toString()
                stderr = stderrSB.toString()
                exitCode = process.waitFor()
            }
        } catch (e: SecurityException) {
            result.apply {
                stdout = ""
                stderr = "Permission denied: ${e.message}"
                exitCode = -2
            }
        } catch (e: IOException) {
            result.apply {
                stdout = ""
                stderr = "IO error: ${e.message}"
                exitCode = -3
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            result.apply {
                stdout = ""
                stderr = "Execution interrupted: ${e.message}"
                exitCode = -4
            }
        } finally {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                process?.destroyForcibly()
        }

        return result
    }
}