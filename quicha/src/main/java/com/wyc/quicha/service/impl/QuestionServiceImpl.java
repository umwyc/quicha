package com.wyc.quicha.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyc.quicha.common.ErrorCode;
import com.wyc.quicha.constant.CommonConstant;
import com.wyc.quicha.exception.BusinessException;
import com.wyc.quicha.mapper.QuestionMapper;
import com.wyc.quicha.model.dto.question.QuestionQueryRequest;
import com.wyc.quicha.model.entity.Question;
import com.wyc.quicha.model.entity.User;
import com.wyc.quicha.model.vo.QuestionVO;
import com.wyc.quicha.service.QuestionService;
import com.wyc.quicha.service.UserService;
import com.wyc.quicha.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description 针对表【question(题目表)】的数据库操作Service实现
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {

    @Resource
    private UserService userService;

    /**
     * 校验题目是否合法
     *
     * @param question
     * @param add
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        if(StrUtil.isBlank(title)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题不能为空");
        }
        if(StrUtil.isBlank(content)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容不能为空");
        }
        if(StrUtil.isBlank(tags)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签不能为空");
        }
        // 创建时参数不能为空
        if (add) {
            if (StringUtils.isAnyBlank(title,tags)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建题目时参数不能为空");
            }
        }
        // 有参数则校验
        if (title.length() > 40) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }

    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public Wrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        List<String> tagList = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();


        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        if (StringUtils.isNotBlank(title)) {
            queryWrapper.like("title", title);
        }
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(userId != null && userId > 0, "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC)
                , sortField);

        return queryWrapper;
    }

    /**
     * 获取脱敏类
     *
     * @param question
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question) {
        QuestionVO questionVO = QuestionVO.objToVO(question);
        Long userId = questionVO.getUserId();
        if(userId == null || userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询关联用户
        User user = userService.getById(userId);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        questionVO.setUserVO(userService.getUserVO(user));
        return questionVO;
    }

    /**
     * 获取数据脱敏列表
     *
     * @param questionList
     * @return
     */
    @Override
    public List<QuestionVO> getQuestionVO(List<Question> questionList) {
        return questionList.stream().map(question -> getQuestionVO(question)).collect(Collectors.toList());
    }

}
