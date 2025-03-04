package com.wyc.quicha.codesandbox;

/**
 * 代码沙箱
 */
public interface CodeSandbox {

    /**
     * 根据执行代码请求执行得到响应结果
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
