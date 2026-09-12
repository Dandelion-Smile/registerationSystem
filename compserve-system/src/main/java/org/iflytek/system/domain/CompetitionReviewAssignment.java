package org.iflytek.system.domain;

import java.util.Date;

/**
 * 赛事评审分配实体，对应表 competition_review_assignment
 */
public class CompetitionReviewAssignment
{
    private Long id;

    private Long competitionId;

    private Long participationId;

    private Long teamId;

    private Long reviewerId;

    private String reviewerName;

    private String assignmentStatus;

    private Long assignedBy;

    private Date assignedTime;

    private Date updateTime;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getCompetitionId()
    {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId)
    {
        this.competitionId = competitionId;
    }

    public Long getParticipationId()
    {
        return participationId;
    }

    public void setParticipationId(Long participationId)
    {
        this.participationId = participationId;
    }

    public Long getTeamId()
    {
        return teamId;
    }

    public void setTeamId(Long teamId)
    {
        this.teamId = teamId;
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

    public String getAssignmentStatus()
    {
        return assignmentStatus;
    }

    public void setAssignmentStatus(String assignmentStatus)
    {
        this.assignmentStatus = assignmentStatus;
    }

    public Long getAssignedBy()
    {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy)
    {
        this.assignedBy = assignedBy;
    }

    public Date getAssignedTime()
    {
        return assignedTime;
    }

    public void setAssignedTime(Date assignedTime)
    {
        this.assignedTime = assignedTime;
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
