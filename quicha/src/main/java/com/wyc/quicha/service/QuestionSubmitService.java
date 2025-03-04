package com.wyc.quicha.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wyc.quicha.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wyc.quicha.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wyc.quicha.model.entity.QuestionSubmit;
import com.wyc.quicha.model.entity.User;
import com.wyc.quicha.model.vo.QuestionSubmitVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @description 针对表【question_submit(题目提交列表)】的数据库操作Service
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 添加题目提交记录
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    long addQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取到查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 数据脱敏
     *
     * @param questionSubmit
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit);

    /**
     * 数据列表脱敏
     *
     * @param questionSubmitList
     * @return
     */
    List<QuestionSubmitVO> getQuestionSubmitVO(List<QuestionSubmit> questionSubmitList);
}
