package com.wyc.quicha.codesandbox;

import com.wyc.quicha.judge.JudgeInfo;
import lombok.Data;

import java.util.List;

@Data
public class ExecuteCodeResponse {

    /**
     * 输出结果
     */
    private List<String> outputList;

    /**
     * 执行错误信息
     */
    private String message;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;
}
