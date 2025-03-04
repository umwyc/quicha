package com.wyc.quicha.codesandbox;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessUtil {

    public static ExecuteMessage executeProcess(Process process) throws IOException, InterruptedException {

        // 进程执行结果信息
        ExecuteMessage executeMessage = new ExecuteMessage();

        // 等待获取进程退出码
        int exitValue = process.waitFor();

        // 1.获取进程退出码
        executeMessage.setExitValue(exitValue);
        BufferedReader standardReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        // 2.获取进程执行的错误信息
        if (exitValue != 0) {
            String executeErrorMessage = "";
            String executeErrorLine = "";
            StringBuilder executeErrorMessageBuilder = new StringBuilder();
            while ((executeErrorLine = errorReader.readLine()) != null) {
                executeErrorMessageBuilder.append(executeErrorLine);
            }
            executeErrorMessage = executeErrorMessageBuilder.toString();
            executeMessage.setErrorMessage(executeErrorMessage);
        }

        // 3.获取进程执行的信息
        String executeInfoMessage = "";
        String executeInfoLine = "";
        StringBuilder executeInfoMessageBuilder = new StringBuilder();
        while ((executeInfoLine = standardReader.readLine()) != null) {
            executeInfoMessageBuilder.append(executeInfoLine);
        }
        executeInfoMessage = executeInfoMessageBuilder.toString();
        executeMessage.setMessage(executeInfoMessage);

        // 4.释放资源
        standardReader.close();
        errorReader.close();
        process.destroy();

        return executeMessage;
    }
}
