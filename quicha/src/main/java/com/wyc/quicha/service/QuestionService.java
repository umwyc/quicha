package com.wyc.quicha.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wyc.quicha.model.dto.question.QuestionQueryRequest;
import com.wyc.quicha.model.entity.Question;
import com.wyc.quicha.model.vo.QuestionVO;

import java.util.List;

/**
* @description 针对表【question(题目表)】的数据库操作Service
*/
public interface QuestionService extends IService<Question> {

    /**
     * 数据校验
     *
     * @param question
     * @param b
     */
    void validQuestion(Question question, boolean b);


    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    Wrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    /**
     * 获取脱敏类
     *
     * @param question
     * @return
     */
    QuestionVO getQuestionVO(Question question);

    /**
     * 获取脱敏数据列表
     *
     * @param questionList
     * @return
     */
    List<QuestionVO> getQuestionVO(List<Question> questionList);
}
