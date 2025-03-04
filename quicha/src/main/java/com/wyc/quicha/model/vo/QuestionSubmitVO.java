package com.wyc.quicha.model.vo;

import cn.hutool.json.JSONUtil;
import com.wyc.quicha.judge.JudgeInfo;
import com.wyc.quicha.model.entity.QuestionSubmit;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class QuestionSubmitVO {

    /**
     * id
     */
    private Long id;

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
     * 提交题目信息
     */
    private QuestionVO questionVO;

    /**
     * 提交用户id
     */
    private Long userId;

    /**
     * 提交用户信息
     */
    private UserVO userVO;

    private static final long serialVersionUID = 1L;

    /**
     * 实体类转脱敏类
     *
     * @param questionSubmit
     * @return
     */
    public static QuestionSubmitVO objToVO(QuestionSubmit questionSubmit){
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        BeanUtils.copyProperties(questionSubmit, questionSubmitVO);
        questionSubmitVO.setJudgeInfo(JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class));
        return questionSubmitVO;
    }

    /**
     * 脱敏类转实体类
     *
     * @param questionSubmitVO
     * @return
     */
    public static QuestionSubmit voToObj(QuestionSubmitVO questionSubmitVO){
        QuestionSubmit questionSubmit = new QuestionSubmit();
        BeanUtils.copyProperties(questionSubmitVO, questionSubmit);
        questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(questionSubmitVO.getJudgeInfo()));
        return questionSubmit;
    }

}
