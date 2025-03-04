package com.wyc.quicha.codesandbox;

import cn.hutool.core.io.FileUtil;
import com.wyc.quicha.codesandbox.Impl.JavaNativeCodeSandbox;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class testJavaNativeCodeSandbox {

    public static void main(String[] args) {
        //代码沙箱执行A + B代码
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        String code = FileUtil.readString("D:\\tools\\back-end\\quicha-backend\\src\\main\\resources\\Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4", "5 6", "7 8", "9 10"));
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");

        JavaNativeCodeSandbox sandbox = new JavaNativeCodeSandbox();
        ExecuteCodeResponse executeCodeResponse = sandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);

//        //  不安全代码1，超时执行
//        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
//        executeCodeRequest.setLanguage("java");
//        executeCodeRequest.setCode(FileUtil.readUtf8String("D:\\tools\\back-end\\quicha-backend-codesandbox\\src\\main\\resources\\unsafe\\TimeExceed.java"));
//        executeCodeRequest.setInputList(new ArrayList<>());
//
//        JavaNativeCodeSandbox sandbox = new JavaNativeCodeSandbox();
//        sandbox.executeCode(executeCodeRequest);

//        //  不安全代码2，内存超出
//        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
//        executeCodeRequest.setLanguage("java");
//        executeCodeRequest.setCode(FileUtil.readUtf8String("D:\\tools\\back-end\\quicha-backend-codesandbox\\src\\main\\resources\\unsafe\\MemoryExceed.java"));
//        executeCodeRequest.setInputList(new ArrayList<>());
//
//        JavaNativeCodeSandbox sandbox = new JavaNativeCodeSandbox();
//        sandbox.executeCode(executeCodeRequest);

//        //  不安全代码3，读取服务器
//        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
//        executeCodeRequest.setLanguage("java");
//        executeCodeRequest.setCode(FileUtil.readUtf8String("D:\\tools\\back-end\\quicha-backend-codesandbox\\src\\main\\resources\\unsafe\\ReaderServer.java"));
//        executeCodeRequest.setInputList(new LinkedList<>());
//
//        JavaNativeCodeSandbox sandbox = new JavaNativeCodeSandbox();
//        sandbox.executeCode(executeCodeRequest);

        //  不安全代码4，写服务器（导致服务器故障，植入木马程序）
    }
}
