package org.iflytek.system.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 竞赛基础信息实体，对应表 competition
 *
 * 仅用于学生端竞赛报名相关功能。
 */
public class Competition
{

    // 在类中添加以下字段
    private String sortType;   // 用于接收 'asc' 或 'desc'

    public Double getMinScore() {
        return minScore;
    }

    public void setMinScore(Double minScore) {
        this.minScore = minScore;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Double maxScore) {
        this.maxScore = maxScore;
    }

    private Double minScore;   // 最小分
    private Double maxScore;

    // 在 Competition 类中添加属性
    private Integer teamCount;

    /** 赛事状态 (用于查询接收参数：not_started, ongoing, finished) */
    private String status;


    /** 竞赛ID */
    private Long competitionId;

    /** 竞赛名称 */
    private String competitionName;

    /** 竞赛说明 */
    private String description;

    /** 竞赛类型 */
    private String competitionType;

    /** 竞赛链接 */
    private String competitionLink;

    /** 宣传图片路径 */
    private String bannerImage;

    /** 通知公告 */
    private String announcement;

    /** 学习链接 */
    private String learningLink;

    /** 学习人数 */
    private Integer learningCount;

    /** 学习图片路径 */
    private String learningImage;
    /** 学习简介 */
    private String learningDescription;

    /** 官方文件列表 */
    private List<CompetitionFile> officialFiles;

    /** 报名开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registerStartTime;

    /** 报名结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registerEndTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }


    public Integer getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(Integer teamCount) {
        this.teamCount = teamCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(String competitionType) {
        this.competitionType = competitionType;
    }

    public String getCompetitionLink() {
        return competitionLink;
    }

    public void setCompetitionLink(String competitionLink) {
        this.competitionLink = competitionLink;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public Date getRegisterStartTime() {
        return registerStartTime;
    }

    public void setRegisterStartTime(Date registerStartTime) {
        this.registerStartTime = registerStartTime;
    }

    public Date getRegisterEndTime() {
        return registerEndTime;
    }

    public void setRegisterEndTime(Date registerEndTime) {
        this.registerEndTime = registerEndTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        //测试2
        this.updateTime = updateTime;
    }

    public String getLearningLink() {
        return learningLink;
    }

    public void setLearningLink(String learningLink) {
        this.learningLink = learningLink;
    }

    public String getLearningImage() {
        return learningImage;
    }

    public void setLearningImage(String learningImage) {
        this.learningImage = learningImage;
    }

    public String getLearningDescription() {
        return learningDescription;
    }

    public void setLearningDescription(String learningDescription) {
        this.learningDescription = learningDescription;
    }

    public Integer getLearningCount() {
        return learningCount;
    }

    public void setLearningCount(Integer learningCount) {
        this.learningCount = learningCount;
    }

    public List<CompetitionFile> getOfficialFiles() {
        return officialFiles;
    }

    public void setOfficialFiles(List<CompetitionFile> officialFiles) {
        this.officialFiles = officialFiles;
    }
}

