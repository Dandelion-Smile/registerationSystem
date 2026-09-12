package org.iflytek.system.domain;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 竞赛材料与成绩，对应表 competition_participation
 */
public class CompetitionParticipation
{
    /** 参赛ID（等于报名ID） */
    private Long participationId;

    /** PPT 路径 */
    private String pptPath;

    /** PDF 路径 */
    private String pdfPath;

    /** 提交时间 */
    private Date submitTime;

    /** 最终得分 */
    private BigDecimal finalScore;

    /** 状态：未提交 / 已提交 / 已评分 */
    private String participationStatus;

    public Long getParticipationId()
    {
        return participationId;
    }

    public void setParticipationId(Long participationId)
    {
        this.participationId = participationId;
    }

    public String getPptPath()
    {
        return pptPath;
    }

    public void setPptPath(String pptPath)
    {
        this.pptPath = pptPath;
    }

    public String getPdfPath()
    {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath)
    {
        this.pdfPath = pdfPath;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public BigDecimal getFinalScore()
    {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore)
    {
        this.finalScore = finalScore;
    }

    public String getParticipationStatus()
    {
        return participationStatus;
    }

    public void setParticipationStatus(String participationStatus)
    {
        this.participationStatus = participationStatus;
    }
}

