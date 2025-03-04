package com.wyc.quicha.codesandbox;

import lombok.Data;

/**
 * 用户执行代码结果（包含编译与运行）
 */
@Data
public class ExecuteMessage {

    /**
     * 进程退出码
     */
    private int exitValue;

    /**
     * 进程执行结果信息
     */
    private String message;

    /**
     * 进程执行错误结果信息
     */
    private String errorMessage;

}
