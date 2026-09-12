package org.iflytek.system.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 模拟考试题目实体，对应表 mock_exam_question
 *
 * 包含判断题、单选题、多选题三种类型。
 */
public class MockExamQuestion
{
    /** 主键ID */
    private Long questionId;

    /** 题目类型：judge/single/multiple */
    private String type;

    /** 题目内容 */
    private String content;

    /** 选项A */
    private String optionA;

    /** 选项B */
    private String optionB;

    /** 选项C */
    private String optionC;

    /** 选项D */
    private String optionD;

    /**
     * 正确答案：
     * - 判断题：T/F
     * - 单选题：A/B/C/D
     * - 多选题：例如 A,B 或 A,B,C
     */
    private String correctAnswer;

    /** 分值 */
    private Integer score;

    /** 题目所属分类（如编程、数据库等） */
    private String category;

    /** 难度：easy/medium/hard */
    private String difficulty;

    /** 是否启用：1-启用，0-禁用 */
    private Integer enabled;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getOptionA()
    {
        return optionA;
    }

    public void setOptionA(String optionA)
    {
        this.optionA = optionA;
    }

    public String getOptionB()
    {
        return optionB;
    }

    public void setOptionB(String optionB)
    {
        this.optionB = optionB;
    }

    public String getOptionC()
    {
        return optionC;
    }

    public void setOptionC(String optionC)
    {
        this.optionC = optionC;
    }

    public String getOptionD()
    {
        return optionD;
    }

    public void setOptionD(String optionD)
    {
        this.optionD = optionD;
    }

    public String getCorrectAnswer()
    {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer)
    {
        this.correctAnswer = correctAnswer;
    }

    public Integer getScore()
    {
        return score;
    }

    public void setScore(Integer score)
    {
        this.score = score;
    }

    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getDifficulty()
    {
        return difficulty;
    }

    public void setDifficulty(String difficulty)
    {
        this.difficulty = difficulty;
    }

    public Integer getEnabled()
    {
        return enabled;
    }

    public void setEnabled(Integer enabled)
    {
        this.enabled = enabled;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }
}

