package org.iflytek.system.mapper;

import java.util.List;
import org.iflytek.system.domain.CompetitionPermission;

/**
 * 竞赛权限 Mapper
 */
public interface CompetitionPermissionMapper
{
    /**
     * 根据用户ID和竞赛ID查询权限
     */
    CompetitionPermission selectByUserAndCompetition(Long userId, Long competitionId);

    /**
     * 查询用户有权限的竞赛ID列表
     */
    List<Long> selectCompetitionIdsByUser(Long userId);

    /**
     * 插入权限记录
     */
    int insertPermission(CompetitionPermission permission);

    /**
     * 更新权限记录
     */
    int updatePermission(CompetitionPermission permission);

    /**
     * 删除权限记录
     */
    int deletePermission(Long userId, Long competitionId);

    /**
     * 查询所有权限记录
     */
    List<CompetitionPermission> selectAll();

    /**
     * 根据赛事查询权限记录
     */
    List<CompetitionPermission> selectByCompetitionId(Long competitionId);
}
