package com.wyc.quicha.model.dto.questionsubmit;

import com.baomidou.mybatisplus.annotation.TableField;
import com.wyc.quicha.judge.JudgeInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class QuestionSubmitAddRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;

    /**
     * 0-未判题  1-判题中  2-成功  3-失败
     */
    private Integer status;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 题目名称
     */
    private String questionTitle;

    /**
     * 题目标签
     */
    private List<String> questionTags;

    /**
     * 提交用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}