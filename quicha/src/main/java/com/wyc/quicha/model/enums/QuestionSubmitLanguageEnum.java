package com.wyc.quicha.model.enums;

import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum QuestionSubmitLanguageEnum {

    JAVA("java", "java");

    private String text;

    private String value;

    QuestionSubmitLanguageEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static List<String> getValues(){
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static QuestionSubmitLanguageEnum getEnumByValue(String value){
        if(ObjectUtils.isEmpty(value)){
            return null;
        }
        for(QuestionSubmitLanguageEnum item : QuestionSubmitLanguageEnum.values()){
            if(item.value.equals(value)){
                return item;
            }
        }
        return null;
    }

    public String getValue(){
        return this.value;
    }

    public String getText(){
        return this.text;
    }

}
