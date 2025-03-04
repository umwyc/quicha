package com.wyc.quicha.judge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class JudgeInfo {

    private long timeCost;

    private long memoryCost;

    private String message;

}
