package com.niki.cmd;

import com.niki.cmd.ExecResult;

interface IUserService {
    // 销毁服务
    void destroy() = 16777114;  // 固定的方法编号, 由 Shizuku 规定

    // 自定义方法
    void exit() = 1;
    
    // 执行 shell 命令
    ExecResult exec(String command, long timeoutMillis) = 2;
}