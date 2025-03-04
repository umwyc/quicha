package com.wyc.quicha.model.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class QuestionUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;


    /**
     * 样例
     */
    private List<JudgeCase> judgeCase;

    /**
     * 题目配置
     */
    private JudgeConfig judgeConfig;

    private static final long serialVersionUID = 1L;
}