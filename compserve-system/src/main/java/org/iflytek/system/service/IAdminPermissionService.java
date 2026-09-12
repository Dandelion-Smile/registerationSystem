package org.iflytek.system.service;

import java.util.List;
import java.util.Map;

/**
 * 管理员端权限管理服务接口
 */
public interface IAdminPermissionService
{
    /**
     * 查询所有竞赛列表（用于赋权选择）
     * @return 竞赛列表
     */
    List<Map<String, Object>> listAllCompetitions();

    /**
     * 查询所有教师列表（用于赋权选择）
     * @return 教师列表
     */
    List<Map<String, Object>> listAllTeachers();

    /**
     * 批量赋权：为多个教师分配竞赛评审权限
     * @param competitionId 竞赛ID
     * @param teacherIds 教师ID列表
     * @return 成功赋权的数量
     */
    int batchGrantPermission(Long competitionId, List<Long> teacherIds);

    /**
     * 撤销权限：删除教师的竞赛评审权限
     * @param competitionId 竞赛ID
     * @param teacherId 教师ID
     * @return 操作结果
     */
    int revokePermission(Long competitionId, Long teacherId);

    /**
     * 查询已赋权的权限列表（支持分页）
     * @param competitionId 竞赛ID（可选，用于筛选）
     * @param teacherId 教师ID（可选，用于筛选）
     * @return 权限列表
     */
    List<Map<String, Object>> listPermissions(Long competitionId, Long teacherId);

    /**
     * 检查教师是否是该竞赛的指导老师（不能评分）
     * @param teacherId 教师ID
     * @param competitionId 竞赛ID
     * @return true表示是指导老师，不能评分
     */
    boolean isAdvisor(Long teacherId, Long competitionId);
}
