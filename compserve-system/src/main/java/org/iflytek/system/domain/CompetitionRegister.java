package org.iflytek.system.domain;

import java.util.Date;

/**
 * 竞赛报名记录，对应表 competition_register
 */
public class CompetitionRegister
{
    private String workName;

    /** 作品描述 */
    private String workDescription;

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public Double getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(Double avgScore) {
        this.avgScore = avgScore;
    }

    /** 指导老师 */
    private String teacherName;

    /** 平均分 (为了匹配 SQL 逻辑建议也加上) */
    private Double avgScore;

    /** 报名ID */
    private Long registerId;

    /** 竞赛ID */
    private Long competitionId;

    /** 队伍ID */
    private Long teamId;

    /** 队伍名称（冗余存储） */
    private String teamName;

    /** 报名人用户ID（sys_user） */
    private Long userId;

    /** 报名时间 */
    private Date registerTime;

    /** 队伍成员 JSON 字符串 */
    private String teamMembers;

    public Long getRegisterId()
    {
        return registerId;
    }

    public void setRegisterId(Long registerId)
    {
        this.registerId = registerId;
    }

    public Long getCompetitionId()
    {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId)
    {
        this.competitionId = competitionId;
    }

    public Long getTeamId()
    {
        return teamId;
    }

    public void setTeamId(Long teamId)
    {
        this.teamId = teamId;
    }

    public String getTeamName()
    {
        return teamName;
    }

    public void setTeamName(String teamName)
    {
        this.teamName = teamName;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Date getRegisterTime()
    {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime)
    {
        this.registerTime = registerTime;
    }

    public String getTeamMembers()
    {
        return teamMembers;
    }

    public void setTeamMembers(String teamMembers)
    {
        this.teamMembers = teamMembers;
    }
}

