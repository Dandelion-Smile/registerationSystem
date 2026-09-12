package org.iflytek.system.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.iflytek.system.domain.CompetitionRegister;

/**
 * 竞赛报名 Mapper
 */
public interface CompetitionRegisterMapper
{
    int insertCompetitionRegister(CompetitionRegister register);

    CompetitionRegister selectById(Long registerId);

    CompetitionRegister selectByCompetitionAndTeam(Long competitionId, Long teamId);

    CompetitionRegister selectByCompetitionAndUser(Long competitionId, Long userId);

    /**
     * 根据竞赛ID和用户ID查询全部报名记录（按最新优先）
     *
     * @param competitionId 竞赛ID
     * @param userId 用户ID
     * @return 报名记录列表
     */
    List<CompetitionRegister> selectListByCompetitionAndUser(@Param("competitionId") Long competitionId,
                                                             @Param("userId") Long userId);

    /**
     * 根据用户ID和多个竞赛ID批量查询报名记录（用于列表页消除 N+1 查询）
     *
     * @param userId 用户ID
     * @param competitionIds 竞赛ID列表
     * @return 报名记录列表（按 competition_id 分组，每组内按时间倒序）
     */
    List<CompetitionRegister> selectListByUserAndCompetitionIds(@Param("userId") Long userId,
                                                                @Param("competitionIds") List<Long> competitionIds);

    /**
     * 根据竞赛ID、用户ID和队伍ID查询报名记录
     * @param competitionId 竞赛ID
     * @param userId 用户ID
     * @param teamId 队伍ID
     * @return 报名记录
     */
    CompetitionRegister selectByCompetitionAndUserAndTeam(@Param("competitionId") Long competitionId, @Param("userId") Long userId, @Param("teamId") Long teamId);

    int updateCompetitionRegister(CompetitionRegister register);

    /**
     * 查询当前用户最近N条报名记录（包含竞赛名称）
     * @param userId 用户ID
     * @param limit 限制条数
     * @return 报名记录列表（包含竞赛名称）
     */
    List<Map<String, Object>> selectRecentRegistrationsByUser(Long userId, Integer limit);

    /**
     * 分页查询当前用户报名记录原始行。
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit  查询条数
     * @return 报名记录列表
     */
    List<Map<String, Object>> selectRegistrationsByUserPage(@Param("userId") Long userId,
                                                            @Param("studentNo") String studentNo,
                                                            @Param("offset") Integer offset,
                                                            @Param("limit") Integer limit);

    /**
     * 统计当前用户报名记录原始总数。
     *
     * @param userId 用户ID
     * @return 原始总数
     */
    Long countRegistrationsByUser(@Param("userId") Long userId,
                                  @Param("studentNo") String studentNo);

    /**
     * 查询当前用户全部报名记录（包含竞赛名称）
     *
     * @param userId 用户ID
     * @return 报名记录列表
     */
    List<Map<String, Object>> selectAllRegistrationsByUser(Long userId);

    /**
     * 按报名ID集合批量查询报名记录。
     *
     * @param registerIds 报名ID集合
     * @return 报名记录列表
     */
    List<CompetitionRegister> selectByIds(@Param("registerIds") List<Long> registerIds);

    /**
     * 查询用户已正式提交材料的报名记录
     *
     * @param userId 用户ID
     * @param limit 限制条数，null 或小于等于 0 表示不限制
     * @return 已提交材料的报名记录
     */
    List<Map<String, Object>> selectSubmittedRegistrationsByUser(@Param("userId") Long userId,
                                                                 @Param("limit") Integer limit);

    /**
     * 根据竞赛ID查询所有报名记录
     * @param competitionId 竞赛ID
     * @return 报名记录列表
     */
    List<CompetitionRegister> selectByCompetitionId(Long competitionId);

    /**
     * 根据队伍ID查询所有报名记录
     * @param teamId 队伍ID
     * @return 报名记录列表
     */
    List<CompetitionRegister> selectByTeamId(Long teamId);

    int deleteById(Long registerId);

    /**
     * 更新队伍成员
     * @param teamId 队伍ID
     * @param teamMembers 队伍成员JSON
     * @return 影响行数
     */
    int updateTeamMembersByTeamId(@Param("teamId") Long teamId, @Param("teamMembers") String teamMembers);

    /**
     * 更新队伍名称
     * @param teamId 队伍ID
     * @param teamName 队伍名称
     * @return 影响行数
     */
    int updateTeamNameByTeamId(@Param("teamId") Long teamId, @Param("teamName") String teamName);

    /**
     * 按队伍同步作品与指导老师摘要信息
     */
    int updateTeamProfileByTeamId(@Param("teamId") Long teamId,
                                  @Param("workName") String workName,
                                  @Param("workDescription") String workDescription,
                                  @Param("teacherName") String teacherName);

    /**
     * 查询所有队伍
     * @return 队伍列表
     */
    List<CompetitionRegister> selectAllTeams();
}
