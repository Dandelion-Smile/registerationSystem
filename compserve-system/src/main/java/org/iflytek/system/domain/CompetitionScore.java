package org.iflytek.system.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 竞赛评分实体，对应表 competition_score
 */
public class CompetitionScore
{
    /** 评分ID */
    private Long scoreId;

    /** 参赛ID（等于报名ID） */
    private Long participationId;

    /** 评审人ID */
    private Long reviewerId;

    /** 评审人姓名 */
    private String reviewerName;

    /** 得分 */
    private BigDecimal score;

    /** 评语 */
    private String comment;

    /** 评分时间 */
    private Date scoreTime;

    public Long getScoreId()
    {
        return scoreId;
    }

    public void setScoreId(Long scoreId)
    {
        this.scoreId = scoreId;
    }

    public Long getParticipationId()
    {
        return participationId;
    }

    public void setParticipationId(Long participationId)
    {
        this.participationId = participationId;
    }

    public Long getReviewerId()
    {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId)
    {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName()
    {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName)
    {
        this.reviewerName = reviewerName;
    }

    public BigDecimal getScore()
    {
        return score;
    }

    public void setScore(BigDecimal score)
    {
        this.score = score;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public Date getScoreTime()
    {
        return scoreTime;
    }

    public void setScoreTime(Date scoreTime)
    {
        this.scoreTime = scoreTime;
    }
}
