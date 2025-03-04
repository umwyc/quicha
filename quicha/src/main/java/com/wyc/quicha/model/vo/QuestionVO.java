package com.wyc.quicha.model.vo;

import cn.hutool.json.JSONUtil;
import com.wyc.quicha.model.dto.question.JudgeCase;
import com.wyc.quicha.model.dto.question.JudgeConfig;
import com.wyc.quicha.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class QuestionVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 提交次数
     */
    private Integer submitNum;

    /**
     * 通过次数
     */
    private Integer acceptedNum;

    /**
     * 样例
     */
    private List<JudgeCase> judgeCase;

    /**
     * 题目配置
     */
    private JudgeConfig judgeConfig;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建用户
     */
    private UserVO userVO;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

    /**
     * 实体类转封装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVO(Question question){
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        questionVO.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class));
        questionVO.setJudgeCase(JSONUtil.toList(question.getJudgeCase(), JudgeCase.class));
        questionVO.setTags(JSONUtil.toList(question.getTags(), String.class));
        return questionVO;
    }

    /**
     * 封装类转实体类
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO){
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        question.setJudgeConfig(JSONUtil.toJsonStr(questionVO.getJudgeConfig()));
        question.setJudgeCase(JSONUtil.toJsonStr(questionVO.getJudgeCase()));
        question.setTags(JSONUtil.toJsonStr(questionVO.getTags()));
        return question;
    }
}
