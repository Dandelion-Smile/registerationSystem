package org.iflytek.system.domain;

import java.util.Date;

/**
 * 竞赛权限实体，对应表 competition_permissions
 */
public class CompetitionPermission
{
    /** 用户ID */
    private Long userId;

    /** 竞赛ID */
    private Long competitionId;

    /** 权限标识，0表示无评分权限，1表示有评分权限 */
    private Integer permissionId;

    /** 赋权时间（可选，如果表中有该字段） */
    private Date grantTime;

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getCompetitionId()
    {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId)
    {
        this.competitionId = competitionId;
    }

    public Integer getPermissionId()
    {
        return permissionId;
    }

    public void setPermissionId(Integer permissionId)
    {
        this.permissionId = permissionId;
    }

    public Date getGrantTime()
    {
        return grantTime;
    }

    public void setGrantTime(Date grantTime)
    {
        this.grantTime = grantTime;
    }
}
