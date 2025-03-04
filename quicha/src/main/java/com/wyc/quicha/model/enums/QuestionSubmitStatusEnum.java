package com.wyc.quicha.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum QuestionSubmitStatusEnum {

    //0-未判题  1-判题中  2-成功  3-失败

    WAITING("等待中", 0),

    RUNNING("判题中", 1),

    SUCCEED("成功", 2),

    FAILED("失败", 3);

    private String text;
    private Integer value;

    QuestionSubmitStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    public static List<Integer> getValues(){
        return Arrays.stream(QuestionSubmitStatusEnum.values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static QuestionSubmitStatusEnum getEnumByValue(String value){
        if(ObjectUtils.isEmpty(value)){
            return null;
        }
        for(QuestionSubmitStatusEnum item : QuestionSubmitStatusEnum.values()){
            if(item.value.equals(value)){
                return item;
            }
        }
        return null;
    }

}
