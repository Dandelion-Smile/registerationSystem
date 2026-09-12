package org.iflytek.system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.TeacherTeam;

/**
 * 教师端服务接口
 */
public interface ITeacherService
{
    /**
     * 查询所有公开竞赛列表（教师端查看）
     * @param keyword 关键词（竞赛名称）
     * @param status 状态（not_started/in_progress/ended）
     * @return 竞赛列表
     */
    List<Competition> listPublicCompetitions(String keyword, String status);

    /**
     * 获取竞赛统计数据
     * @return 统计数据（总竞赛数、进行中竞赛数等）
     */
    Map<String, Object> getCompetitionStats();

    /**
     * 查询竞赛详情
     * @param competitionId 竞赛ID
     * @return 竞赛详情
     */
    Competition getCompetitionDetail(Long competitionId);

    /**
     * 查询当前教师可评审的竞赛列表（已赋权的竞赛）
     * @param teacherId 教师ID
     * @return 可评审竞赛列表（包含评审进度信息）
     */
    List<Map<String, Object>> getReviewableCompetitions(Long teacherId);

    /**
     * 查询竞赛的队伍列表（用于评审）
     * @param competitionId 竞赛ID
     * @param teacherId 教师ID（用于检查权限）
     * @return 队伍列表
     */
    List<Map<String, Object>> getReviewTeams(Long competitionId, Long teacherId);

    /**
     * 查询队伍详情（包含材料路径）
     * @param competitionId 竞赛ID
     * @param participationId 参赛记录ID
     * @param teacherId 教师ID（用于检查权限）
     * @return 队伍详情
     */
    Map<String, Object> getReviewTeamDetail(Long competitionId, Long participationId, Long teacherId);

    /**
     * 查询队伍的评分记录
     * @param participationId 参赛ID（registerId）
     * @return 评分记录列表
     */
    List<Map<String, Object>> getTeamScores(Long participationId, Long teacherId);

    /**
     * 提交评分
     * @param participationId 参赛ID
     * @param teacherId 教师ID
     * @param teacherNickName 教师昵称
     * @param score 分数
     * @param comment 评语
     * @return 评分记录ID
     */
    Long submitScore(Long participationId, Long teacherId, String teacherNickName, BigDecimal score, String comment);

    /**
     * 修改评分
     * @param scoreId 评分ID
     * @param teacherId 教师ID（用于验证权限）
     * @param score 分数
     * @param comment 评语
     */
    void updateScore(Long scoreId, Long teacherId, BigDecimal score, String comment);

    /**
     * 删除评分
     * @param scoreId 评分ID
     * @param teacherId 教师ID（用于验证权限）
     */
    void deleteScore(Long scoreId, Long teacherId);

    /**
     * 查询评审统计信息
     * @param competitionId 竞赛ID
     * @param teacherId 教师ID
     * @return 统计信息（已评审/未评审数量等）
     */
    Map<String, Object> getReviewStats(Long competitionId, Long teacherId);

    /**
     * 查询当前教师作为指导老师的竞赛列表
     * @param teacherId 教师ID
     * @param teacherNickName 教师昵称（用于匹配队伍中的指导老师）
     * @return 竞赛和队伍信息列表
     */
    List<Map<String, Object>> getMyTeamCompetitions(Long teacherId, String teacherNickName);

    /**
     * 根据手机号获取教师信息
     * @param phone 手机号
     * @return 教师信息
     */
    Map<String, Object> searchTeachers(String phone, String name);

    /**
     * 根据手机号获取单个教师详细信息
     * @param phone 手机号
     * @return 教师详细信息
     */
    TeacherTeam getTeacherByPhone(String phone);

    /**
     * 更新教师信息（同步更新 tb_teacher_team 表）
     * @param teacherTeam 教师信息
     * @return 更新结果
     */
    int updateTeacherInfo(TeacherTeam teacherTeam);
}
