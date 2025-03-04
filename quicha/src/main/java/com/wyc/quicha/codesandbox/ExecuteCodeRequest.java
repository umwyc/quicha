package com.wyc.quicha.codesandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {

    /**
     * 输入参数
     */
    private List<String> inputList;

    /**
     * 用户提交代码
     */
    private String code;

    /**
     * 用户使用语言
     */
    private String language;
}
