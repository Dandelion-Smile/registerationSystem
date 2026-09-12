package org.iflytek.system.domain;

/**
 * 模拟考试作答明细，对应表 mock_exam_record_question
 */
public class MockExamRecordQuestion
{
    /** 主键ID */
    private Long id;

    /** 记录ID */
    private Long recordId;

    /** 题目ID */
    private Long questionId;

    /** 用户答案（多选用逗号分隔，如 A,B） */
    private String userAnswer;

    /** 正确答案快照 */
    private String correctAnswer;

    /** 本题得分 */
    private Integer score;

    /** 是否答对：1-正确，0-错误 */
    private Integer correct;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getRecordId()
    {
        return recordId;
    }

    public void setRecordId(Long recordId)
    {
        this.recordId = recordId;
    }

    public Long getQuestionId()
    {
        return questionId;
    }

    public void setQuestionId(Long questionId)
    {
        this.questionId = questionId;
    }

    public String getUserAnswer()
    {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer)
    {
        this.userAnswer = userAnswer;
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

    public Integer getCorrect()
    {
        return correct;
    }

    public void setCorrect(Integer correct)
    {
        this.correct = correct;
    }
}

