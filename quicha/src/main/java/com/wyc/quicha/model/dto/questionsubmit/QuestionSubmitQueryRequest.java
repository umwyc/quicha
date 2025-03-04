package com.wyc.quicha.model.dto.questionsubmit;

import com.wyc.quicha.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class QuestionSubmitQueryRequest extends PageRequest implements Serializable {
    /**
     * 题目名称
     */
    private String questionTitle;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 判题信息
     */
    private String judgeInfo;

    /**
     * 0-未判题  1-判题中  2-成功  3-失败
     */
    private Integer status;

    /**
     * 标签列表（json 数组）
     */
    private List<String> questionTags;

    private static final long serialVersionUID = 1L;
}