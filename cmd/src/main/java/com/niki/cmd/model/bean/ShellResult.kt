package com.niki.cmd.model.bean

data class ShellResult(
    val stdout: String, // 标准输出
    val stderr: String, // 标准错误
    val exitCode: Int, // 退出码, 0 表示成功
) {
    companion object {
        fun error(msg: String?): ShellResult = ShellResult("", msg ?: "unknown error", -1)
    }

    // 判断命令是否成功
    val isSuccess: Boolean
        get() = exitCode == 0

    // 提供简化的输出字符串, 方便日志或 UI 显示
    val output: String
        get() = if (isSuccess) stdout else stderr
}