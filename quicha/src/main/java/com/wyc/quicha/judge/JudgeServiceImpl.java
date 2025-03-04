package com.wyc.quicha.judge;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.wyc.quicha.codesandbox.CodeSandbox;
import com.wyc.quicha.codesandbox.ExecuteCodeRequest;
import com.wyc.quicha.codesandbox.ExecuteCodeResponse;
import com.wyc.quicha.codesandbox.Impl.JavaNativeCodeSandbox;
import com.wyc.quicha.common.ErrorCode;
import com.wyc.quicha.exception.BusinessException;
import com.wyc.quicha.model.dto.question.JudgeCase;
import com.wyc.quicha.model.dto.question.JudgeConfig;
import com.wyc.quicha.model.entity.Question;
import com.wyc.quicha.model.entity.QuestionSubmit;
import com.wyc.quicha.model.entity.User;
import com.wyc.quicha.model.enums.JudgeInfoMessageEnum;
import com.wyc.quicha.model.enums.QuestionSubmitStatusEnum;
import com.wyc.quicha.service.QuestionService;
import com.wyc.quicha.service.QuestionSubmitService;
import com.wyc.quicha.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    /**
     * 判题方法实现
     *
     * @param questionSubmitId
     * @return
     */
    @Override
    public QuestionSubmit doJudge(Long questionSubmitId) {
        // 1.校验
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交记录不存在");
        }
        Question question = questionService.getById(questionSubmit.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        User user = userService.getById(questionSubmit.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请勿重复提交");
        }

        // 2.设置当前的状态为正在运行，不要让用户重复提交了
        questionSubmit.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmit);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库错误");
        }

        // 3.判题
        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();
        // 获取题目配置信息
        JudgeConfig judgeConfig = JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class);
        long timeLimit = judgeConfig.getTimeLimit();
        long memoryLimit = judgeConfig.getMemoryLimit();
        // 获取判题的用例
        List<JudgeCase> judgeCases = JSONUtil.toList(question.getJudgeCase(), JudgeCase.class);
        List<String> inputList = judgeCases.stream()
                .map(JudgeCase::getInput)
                .collect(Collectors.toList());
        List<String> outputList = judgeCases.stream()
                .map(JudgeCase::getOutput)
                .collect(Collectors.toList());
        // 创建代码执行请求
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        // 调用代码沙箱
        CodeSandbox codeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);

        // 4.校验结果是否正确
        // 编译，运行错误或者系统出错
        if (StrUtil.isNotBlank(executeCodeResponse.getMessage())) {
            return setFailJudgeResult(questionSubmit, QuestionSubmitStatusEnum.FAILED.getValue(), executeCodeResponse.getMessage());
        }
        // 用户结果的长度出错
        if (executeCodeResponse.getOutputList().size() != inputList.size()) {
            return setFailJudgeResult(questionSubmit, QuestionSubmitStatusEnum.FAILED.getValue(), JudgeInfoMessageEnum.WRONG_ANSWER.getText());
        }
        // 用户结果与正确答案不一致
        for (int i = 0; i < executeCodeResponse.getOutputList().size(); i++) {
            if (!executeCodeResponse.getOutputList().get(i).equals(outputList.get(i))) {
                return setFailJudgeResult(questionSubmit, QuestionSubmitStatusEnum.FAILED.getValue(), JudgeInfoMessageEnum.WRONG_ANSWER.getText());
            }
        }
        // 判断是否超时或者超出内存限制
        long timeCost = executeCodeResponse.getJudgeInfo().getTimeCost();
        long memoryCost = executeCodeResponse.getJudgeInfo().getMemoryCost();
        if (memoryCost > memoryLimit) {
            return setFailJudgeResult(questionSubmit, QuestionSubmitStatusEnum.FAILED.getValue(), JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getText());
        }
        if (timeCost > timeLimit) {
            return setFailJudgeResult(questionSubmit, QuestionSubmitStatusEnum.FAILED.getValue(), JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getText());
        }
        // 返回成功的结果
        questionSubmit.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMemoryCost(memoryCost);
        judgeInfo.setTimeCost(timeCost);
        judgeInfo.setMessage(JudgeInfoMessageEnum.Accepted.getValue());
        questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        // 先更新提交题目数据库
        update = questionSubmitService.updateById(questionSubmit);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库异常");
        }
        // 再更新题目数据库，通过数加 1
        question.setAcceptedNum(question.getAcceptedNum() + 1);
        update = questionService.updateById(question);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库异常");
        }
        return questionSubmit;
    }

    /**
     * 返回判题失败的结果
     *
     * @param questionSubmit
     * @param status
     * @param judgeInfoMessage
     * @return
     */
    private QuestionSubmit setFailJudgeResult(QuestionSubmit questionSubmit, Integer status, String judgeInfoMessage) {
        questionSubmit.setStatus(status);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTimeCost(0L);
        judgeInfo.setMemoryCost(0L);
        judgeInfo.setMessage(judgeInfoMessage);
        questionSubmit.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        boolean update = questionSubmitService.updateById(questionSubmit);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据库异常");
        }
        return questionSubmit;
    }
}
