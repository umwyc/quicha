package com.wyc.quicha.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyc.quicha.common.ErrorCode;
import com.wyc.quicha.constant.CommonConstant;
import com.wyc.quicha.exception.BusinessException;
import com.wyc.quicha.judge.JudgeInfo;
import com.wyc.quicha.judge.JudgeService;
import com.wyc.quicha.mapper.QuestionSubmitMapper;
import com.wyc.quicha.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wyc.quicha.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wyc.quicha.model.entity.Question;
import com.wyc.quicha.model.entity.QuestionSubmit;
import com.wyc.quicha.model.entity.User;
import com.wyc.quicha.model.enums.JudgeInfoMessageEnum;
import com.wyc.quicha.model.enums.QuestionSubmitLanguageEnum;
import com.wyc.quicha.model.enums.QuestionSubmitStatusEnum;
import com.wyc.quicha.model.vo.QuestionSubmitVO;
import com.wyc.quicha.service.QuestionService;
import com.wyc.quicha.service.QuestionSubmitService;
import com.wyc.quicha.service.UserService;
import com.wyc.quicha.utils.SqlUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author 1451712641qq.com
 * @description 针对表【question_submit(题目提交列表)】的数据库操作Service实现
 * @createDate 2024-11-18 18:32:15
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private JudgeService judgeService;

    @Resource
    private QuestionService questionService;

    /**
     * 添加题目提交记录
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 简单校验
        String language = questionSubmitAddRequest.getLanguage();
        String code = questionSubmitAddRequest.getCode();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言不存在");
        }
        if (StrUtil.isEmpty(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有提交代码");
        }
        Long userId = questionSubmitAddRequest.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        Long questionId = questionSubmitAddRequest.getQuestionId();
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目信息不存在");
        }

        // 查询题目信息
        Question question = questionService.getById(questionId);

        // 设置提交初始状态
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(new JudgeInfo(0, 0, JudgeInfoMessageEnum.WAITING.getValue())));
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        BeanUtils.copyProperties(questionSubmitAddRequest, questionSubmit);
        questionSubmit.setQuestionTags(question.getTags());
        questionSubmit.setQuestionTitle(question.getTitle());

        // 将题目提交信息先保存到数据库中
        boolean save = save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目提交失败");
        }

        // 将题目的提交数加 1
        question.setSubmitNum(question.getSubmitNum() + 1);
        boolean update = questionService.updateById(question);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        // 先返回题目提交id，异步执行判题服务
        Long questionSubmitId = questionSubmit.getId();
        CompletableFuture.runAsync(() -> {
            judgeService.doJudge(questionSubmitId);
        });
        return questionSubmitId;
    }

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        String questionTitle = questionSubmitQueryRequest.getQuestionTitle();
        String language = questionSubmitQueryRequest.getLanguage();
        String judgeInfo = questionSubmitQueryRequest.getJudgeInfo();
        Integer status = questionSubmitQueryRequest.getStatus();
        List<String> questionTags = questionSubmitQueryRequest.getQuestionTags();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }

        queryWrapper.like(StrUtil.isNotBlank(questionTitle), "questionTitle", questionTitle);
        queryWrapper.like(questionTags != null && questionTags.size() > 0,
                "questionTags", JSONUtil.toJsonStr(questionTags));
        queryWrapper.like(StrUtil.isNotBlank(judgeInfo), "judgeInfo", judgeInfo);
        queryWrapper.eq(StrUtil.isNotBlank(language), "language", language);
        queryWrapper.eq(status != null && status >= 0 && StrUtil.isNotBlank(status.toString()),
                "status", status);

        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC)
                , sortField);
        return queryWrapper;
    }

    /**
     * 数据脱敏
     *
     * @param questionSubmit
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVO(questionSubmit);
        // 查询关联用户
        Long userId = questionSubmitVO.getUserId();
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        questionSubmitVO.setUserVO(userService.getUserVO(user));
        // 查询关联题目
        Long questionId = questionSubmitVO.getQuestionId();
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        questionSubmitVO.setQuestionVO(questionService.getQuestionVO(question));
        return questionSubmitVO;
    }

    /**
     * 数据列表脱敏
     *
     * @param questionSubmitList
     * @return
     */
    @Override
    public List<QuestionSubmitVO> getQuestionSubmitVO(List<QuestionSubmit> questionSubmitList) {
        return questionSubmitList.stream().map(questionSubmit -> getQuestionSubmitVO(questionSubmit)).collect(Collectors.toList());
    }
}




