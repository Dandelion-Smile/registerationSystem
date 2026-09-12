package org.iflytek.system.domain;

import java.util.Date;
import org.iflytek.common.annotation.Excel;

/**
 * Admin competition team export row.
 */
public class CompetitionTeamExport
{
    @Excel(name = "序号", sort = 1)
    private Integer sortNo;

    @Excel(name = "队伍名称", sort = 2, width = 24)
    private String teamName;

    @Excel(name = "作品名称", sort = 3, width = 28)
    private String workName;

    @Excel(name = "负责人", sort = 4, width = 16)
    private String leaderName;

    @Excel(name = "指导老师", sort = 5, width = 24)
    private String advisorNames;

    @Excel(name = "评审老师", sort = 6, width = 28)
    private String reviewerNames;

    @Excel(name = "平均分", sort = 7)
    private String avgScore;

    @Excel(name = "报名时间", sort = 8, dateFormat = "yyyy-MM-dd HH:mm:ss", width = 22)
    private Date registerTime;

    public Integer getSortNo()
    {
        return sortNo;
    }

    public void setSortNo(Integer sortNo)
    {
        this.sortNo = sortNo;
    }

    public String getTeamName()
    {
        return teamName;
    }

    public void setTeamName(String teamName)
    {
        this.teamName = teamName;
    }

    public String getWorkName()
    {
        return workName;
    }

    public void setWorkName(String workName)
    {
        this.workName = workName;
    }

    public String getLeaderName()
    {
        return leaderName;
    }

    public void setLeaderName(String leaderName)
    {
        this.leaderName = leaderName;
    }

    public String getAdvisorNames()
    {
        return advisorNames;
    }

    public void setAdvisorNames(String advisorNames)
    {
        this.advisorNames = advisorNames;
    }

    public String getReviewerNames()
    {
        return reviewerNames;
    }

    public void setReviewerNames(String reviewerNames)
    {
        this.reviewerNames = reviewerNames;
    }

    public String getAvgScore()
    {
        return avgScore;
    }

    public void setAvgScore(String avgScore)
    {
        this.avgScore = avgScore;
    }

    public Date getRegisterTime()
    {
        return registerTime;
    }

    public void setRegisterTime(Date registerTime)
    {
        this.registerTime = registerTime;
    }
}
