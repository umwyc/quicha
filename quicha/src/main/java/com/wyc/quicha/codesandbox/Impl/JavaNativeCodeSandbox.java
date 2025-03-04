package com.wyc.quicha.codesandbox.Impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import com.wyc.quicha.codesandbox.*;
import com.wyc.quicha.constant.ExecuteConstant;
import com.wyc.quicha.judge.JudgeInfo;
import com.wyc.quicha.model.enums.JudgeInfoMessageEnum;
import org.springframework.util.StopWatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Java 原生代码沙箱
 */
public class JavaNativeCodeSandbox implements CodeSandbox {

    /**
     * 全部代码目录，用户提交的代码全部都放在这个目录之下
     */
    public static final String GLOBAL_CODE_DIR = System.getProperty("user.dir") + File.separator + "test-code";

    /**
     * 运行代码的超时时间
     */
    public static final long TIME_OUT = 5000L;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();

        // 创建代码执行响应
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();

        // 1.创建Main.java文件存放代码
        if (!FileUtil.exist(GLOBAL_CODE_DIR)) {
            FileUtil.mkdir(GLOBAL_CODE_DIR);
        }
        String userCodeParentDir = GLOBAL_CODE_DIR + File.separator + UUID.randomUUID();
        String userCodeFilePath = userCodeParentDir + File.separator + ExecuteConstant.JAVA_FILENAME;
        FileUtil.writeString(code, userCodeFilePath, StandardCharsets.UTF_8);

        // 2.编译用户代码
        try {
            File userCodeFile = FileUtil.file(userCodeFilePath);
            String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
            // 执行编译
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            // 获取编译结果
            ExecuteMessage executeMessage = ProcessUtil.executeProcess(compileProcess);
        } catch (IOException e) {
            e.printStackTrace();
            executeCodeResponse.setMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getText());
            return executeCodeResponse;
        } catch (InterruptedException e) {
            e.printStackTrace();
            executeCodeResponse.setMessage(JudgeInfoMessageEnum.SYSTEM_ERROR.getText());
            return executeCodeResponse;
        }
        // 3.运行用户代码
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        long timeCost = -1;
        for (int i = 0; i < inputList.size(); i++) {
            try {
                // 限制程序消耗的内存大小
                String runCmd = String.format("java -Xmx128m -Dfile.encoding=utf-8 -cp %s Main %s" + System.lineSeparator(), userCodeParentDir, inputList.get(i));

                // 执行运行
                stopWatch.start();
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                stopWatch.stop();

                // 更新程序的执行时间
                if(stopWatch.getLastTaskTimeMillis() > timeCost){
                    timeCost = stopWatch.getLastTaskTimeMillis();
                }

                // 限制程序的执行时间
                final boolean[] isTimeExceed = {false};
                final boolean[] isSystemError = {false};
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_OUT);
                        runProcess.destroy();
                        isTimeExceed[0] = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        isSystemError[0] = true;
                    }
                }).start();

                // 程序运行超时
                if(isTimeExceed[0]) {
                    executeCodeResponse.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getText());
                    return executeCodeResponse;
                }

                // 系统错误
                if(isSystemError[0]) {
                    executeCodeResponse.setMessage(JudgeInfoMessageEnum.SYSTEM_ERROR.getText());
                    return executeCodeResponse;
                }

                // 正常执行，获取运行结果并加入结果数组中
                ExecuteMessage executeMessage = ProcessUtil.executeProcess(runProcess);
                executeMessageList.add(executeMessage);
            } catch (IOException e) {
                e.printStackTrace();
                executeCodeResponse.setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getText());
                return executeCodeResponse;
            } catch (InterruptedException e) {
                e.printStackTrace();
                executeCodeResponse.setMessage(JudgeInfoMessageEnum.SYSTEM_ERROR.getText());
                return executeCodeResponse;
            }
        }

        // 4.获取用户执行结果
        executeCodeResponse.setOutputList(executeMessageList.stream()
                .map(ExecuteMessage::getMessage)
                .collect(Collectors.toList()));
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTimeCost(timeCost);
        judgeInfo.setMemoryCost(0L);
        judgeInfo.setMessage("");
        executeCodeResponse.setJudgeInfo(judgeInfo);

        // 5.释放资源(直接将父文件夹删除)
        FileUtil.del(userCodeParentDir);

        return executeCodeResponse;
    }
}


