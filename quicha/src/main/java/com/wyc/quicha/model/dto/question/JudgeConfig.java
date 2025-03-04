package com.wyc.quicha.model.dto.question;

import lombok.Data;

/**
 * 题目配置
 */
@Data
public class JudgeConfig {

    /**
     * 时间限制
     */
    long timeLimit;

    /**
     * 内存限制
     */
    long memoryLimit;

    /**
     * 堆栈限制
     */
    long stackLimit;
}
