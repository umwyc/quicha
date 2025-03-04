package com.wyc.quicha.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wyc.quicha.annotation.AuthCheck;
import com.wyc.quicha.common.BaseResponse;
import com.wyc.quicha.common.DeleteRequest;
import com.wyc.quicha.common.ErrorCode;
import com.wyc.quicha.common.ResultUtils;
import com.wyc.quicha.constant.UserConstant;
import com.wyc.quicha.exception.BusinessException;
import com.wyc.quicha.exception.ThrowUtils;
import com.wyc.quicha.model.dto.question.*;
import com.wyc.quicha.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wyc.quicha.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wyc.quicha.model.entity.Question;
import com.wyc.quicha.model.entity.QuestionSubmit;
import com.wyc.quicha.model.entity.User;
import com.wyc.quicha.model.vo.QuestionSubmitVO;
import com.wyc.quicha.model.vo.QuestionVO;
import com.wyc.quicha.service.QuestionService;
import com.wyc.quicha.service.QuestionSubmitService;
import com.wyc.quicha.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {


    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    // region 题目的增删查改

    /**
     * 创建（仅管理员）
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest
            , HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        // 处理tags
        List<String> tags = questionAddRequest.getTags();
        if (tags != null && !tags.isEmpty()) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // 处理judgeCase
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        if(CollUtil.isNotEmpty(judgeCase)) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        // 处理judgeConfig
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if(ObjUtil.isNotEmpty(judgeConfig)) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        // 保存至数据库
        boolean save = questionService.save(question);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        long questionId = question.getId();
        return ResultUtils.success(questionId);
    }

    /**
     * 删除（仅管理员）
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean remove = questionService.removeById(id);
        return ResultUtils.success(remove);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        // 处理tags
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null && !tags.isEmpty()) {
            question.setTags(JSONUtil.toJsonStr(tags));
        }
        // 处理judgeCase
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        if(CollUtil.isNotEmpty(judgeCase)) {
            question.setJudgeCase(JSONUtil.toJsonStr(judgeCase));
        }
        // 处理judgeConfig
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if(ObjUtil.isNotEmpty(judgeConfig)) {
            question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 获取（仅管理员）
     *
     * @param id
     * @return QuestionAdminVO
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Question> getQuestionById(@RequestParam Long id) {
        if(id == null || id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询题目是否存在
        Question question = questionService.getById(id);
        if(question == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(question);
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    // endregion 题目的增删查改

    /**
     * 根据 id 获取题目
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询题目是否存在
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        Page<QuestionVO> questionVOPage = new Page<>(current, size, questionPage.getTotal());
        questionVOPage.setRecords(questionService.getQuestionVO(questionPage.getRecords()));
        return ResultUtils.success(questionVOPage);
    }




    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return
     */
    @PostMapping("/question_submit/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                                HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目不存在");
        }
        // 登录才能操作
        final User loginUser = userService.getLoginUser(request);
        questionSubmitAddRequest.setUserId(loginUser.getId());
        long id = questionSubmitService.addQuestionSubmit(questionSubmitAddRequest, loginUser);
        // 返回题目的 id
        return ResultUtils.success(id);
    }

    /**
     * 根据 id 获取提交题目信息
     *
     * @param questionSubmitId
     * @return
     */
    @GetMapping("question_submit/get/vo")
    public BaseResponse<QuestionSubmitVO> getQuestionSubmitVOById(@RequestParam Long questionSubmitId){
        if(questionSubmitId == null || questionSubmitId <= 0){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交题目信息不存在");
        }
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if(questionSubmit == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交题目信息不存在");
        }
        QuestionSubmitVO questionSubmitVO = questionSubmitService.getQuestionSubmitVO(questionSubmit);
        return ResultUtils.success(questionSubmitVO);
    }

    /**
     * 获取题目的分页列表
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/question_submit/list/page/vo")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitVOByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest
            , HttpServletRequest request) {
        if(questionSubmitQueryRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int pageSize = questionSubmitQueryRequest.getPageSize();
        int current = questionSubmitQueryRequest.getCurrent();
        QueryWrapper<QuestionSubmit> queryWrapper = questionSubmitService.getQueryWrapper(questionSubmitQueryRequest);
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, pageSize), queryWrapper);
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(current, pageSize, questionSubmitPage.getTotal());
        questionSubmitVOPage.setRecords(questionSubmitService.getQuestionSubmitVO(questionSubmitPage.getRecords()));
        return ResultUtils.success(questionSubmitVOPage);
    }

}

