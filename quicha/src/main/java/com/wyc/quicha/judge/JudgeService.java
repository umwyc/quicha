package com.wyc.quicha.judge;

import com.wyc.quicha.model.entity.QuestionSubmit;

/**
 * 判题接口
 */
public interface JudgeService {

    /**
     * 根据提交题目的 id 编号执行判题逻辑，返回一个提交题目的实体类
     *
     * @param questionSubmitId
     * @return
     */
    QuestionSubmit doJudge(Long questionSubmitId);

}
