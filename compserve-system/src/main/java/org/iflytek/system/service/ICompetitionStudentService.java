package org.iflytek.system.service;

import java.util.List;
import java.util.Map;

import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionRegister;

/**
 * 学生端竞赛报名相关服务
 */
public interface ICompetitionStudentService
{
    /**
     * 查询全部竞赛
     */
    List<Competition> selectAllCompetitions();

    /**
     * 分页查询竞赛
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页条数
     * @return 竞赛列表
     */
    List<Competition> selectCompetitionsByPage(Integer pageNum, Integer pageSize);

    /**
     * 分页查询竞赛（支持筛选）
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页条数
     * @param filters  筛选条件（keyword, type, level, status）
     * @return 竞赛列表
     */
    List<Competition> selectCompetitionsByPageAndFilters(Integer pageNum, Integer pageSize, Map<String, String> filters);

    /**
     * 查询 competition 表中的竞赛类型选项
     */
    List<String> selectCompetitionTypes();

    /**
     * 获取竞赛的队伍列表
     *
     * @param competitionId 竞赛ID
     * @return 队伍列表（包含队伍ID、名称、成员数等信息）
     */
    List<Map<String, Object>> getCompetitionTeams(Long competitionId);
    /**
     * 根据ID获取竞赛详情
     *
     * @param competitionId 竞赛ID
     * @return 竞赛详情
     */
    Competition getCompetitionById(Long competitionId);
    /**
     * 报名竞赛：创建队伍或加入已有队伍，并生成报名记录和参赛记录
     *
     * @param competitionId 竞赛ID
     * @param teamId         队伍ID（如果为null则创建新队伍，否则加入已有队伍）
     * @param teamName       队伍名称（创建新队伍时必填）
     * @param teamMembers    队伍成员 JSON 串
     * @param userId         报名人用户ID
     * @return 报名记录
     */
    CompetitionRegister registerCompetition(Long competitionId, Long teamId, String teamName, String teamMembers,
            String workName, String workDescription, List<Map<String, Object>> teacherList, Long userId);

    CompetitionRegister saveTeamName(Long competitionId, Long teamId, String teamName, Long userId);

    /**
     * 提交材料
     *
     * @param registerId 报名ID
     * @param pptPath    PPT 路径
     * @param pdfPath    PDF 路径
     */




    void submitMaterials(Long registerId, String pptPath, String pdfPath, String fileNames, String workName,
            String workDescription, Long userId);

    /**
     * 查询当前用户在某个竞赛下的报名状态
     *
     * @param competitionId 竞赛ID
     * @param userId        用户ID
     * @return 报名状态信息（是否已报名、队伍信息等）
     */
    Map<String, Object> getUserCompetitionStatus(Long competitionId, Long userId);

    /**
     * 查询用户在指定竞赛下的报名状态（支持指定队伍）
     *
     * @param competitionId 竞赛ID
     * @param userId        用户ID
     * @param teamId        队伍ID（可选）
     * @return 报名状态
     */
    Map<String, Object> getUserCompetitionStatus(Long competitionId, Long userId, Long teamId);

    /**
     * 批量查询用户在多个竞赛下的报名状态（轻量版，仅供列表页使用）
     * 一次 SQL 查出所有竞赛的报名记录，避免 N+1 查询
     *
     * @param competitionIds 竞赛ID列表
     * @param userId        用户ID
     * @return Map: competitionId -> {registered, registerId, teamId, teamName}
     */
    Map<Long, Map<String, Object>> getUserCompetitionStatusBatch(List<Long> competitionIds, Long userId);

    /**
     * 查询已上传的参赛材料信息
     *
     * @param registerId 报名ID
     * @return 参赛材料信息（PPT路径、PDF路径、提交时间等）
     */
    Map<String, Object> getUploadedMaterials(Long registerId);

    /**
     * 查询学生已提交作品的详情，包括作品、队伍、指导老师和评审信息。
     *
     * @param registerId 报名ID
     * @param userId     当前学生用户ID
     * @return 作品详情
     */
    Map<String, Object> getStudentWorkDetail(Long registerId, Long userId);

    /**
     * 队长向队员发出邀请（在队伍成员JSON中添加pending条目）
     *
     * @param teamId 队伍ID
     * @param studentNo 被邀请队员学号
     * @param inviterUserId 邀请人用户ID
     * @return 是否成功
     */
    boolean inviteMember(Long teamId, String studentNo, Long inviterUserId);

    /**
     * 队员申请加入队伍（在队伍成员JSON中添加pending条目，type=apply）
     *
     * @param teamId 队伍ID
     * @param applicantUserId 申请人用户ID
     * @return 是否成功
     */
    boolean applyJoinTeam(Long teamId, Long applicantUserId);

    /**
     * 同意邀请或申请（将队员状态改为approved并同步报名）
     *
     * @param teamId 队伍ID
     * @param studentNo 队员学号
     * @param approverUserId 操作人用户ID
     * @return 是否成功
     */
    boolean approveMember(Long teamId, String studentNo, Long approverUserId);

    /**
     * 拒绝邀请或申请（从队伍成员JSON中移除该pending条目）
     *
     * @param teamId 队伍ID
     * @param studentNo 队员学号
     * @param approverUserId 操作人用户ID
     * @return 是否成功
     */
    boolean rejectMember(Long teamId, String studentNo, Long approverUserId);

    /**
     * 查询与当前用户相关的待处理邀请/申请
     *
     * @param userId 当前用户ID
     * @return 待处理消息列表
     */
    java.util.List<java.util.Map<String, Object>> listUserInvitations(Long userId);

    /**
     * 队长移除队员（仅队伍创建者/队长可操作）
     *
     * @param teamId 队伍ID
     * @param studentNo 被移除队员学号
     * @param operatorUserId 操作人用户ID
     * @return 是否成功
     */
    boolean removeMember(Long teamId, String studentNo, Long operatorUserId);

    /**
     * 查询当前用户最近N条报名记录
     *
     * @param userId 用户ID
     * @param limit  限制条数
     * @return 报名记录列表（包含竞赛名称、报名时间等）
     */
    List<Map<String, Object>> getRecentRegistrations(Long userId, Integer limit);

    /**
     * 分页查询当前用户的报名记录。
     *
     * @param userId   用户ID
     * @param pageNum  页码，从1开始
     * @param pageSize 每页条数
     * @return 包含 rows 和 total 的分页结果
     */
    Map<String, Object> getRegistrationPage(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 取消报名
     *
     * @param registerId 报名ID
     * @param userId     操作用户ID
     * @return 是否成功
     */
    boolean cancelRegistration(Long registerId, Long userId);

    // 添加记录学习次数的方法
    void recordLearning(Long competitionId);


}
