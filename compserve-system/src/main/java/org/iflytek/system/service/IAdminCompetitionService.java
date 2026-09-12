package org.iflytek.system.service;

import java.util.List;
import java.util.Map;
import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionRegister;

/**
 * 管理员端竞赛管理服务接口
 */
public interface IAdminCompetitionService
{

    List<Map<String, Object>> selectTeamListByCompId(Long competitionId);

    /** 查询队伍作品详情档案 */
    public CompetitionRegister selectTeamDetailById(Long registerId);

    public Map<String, Object> selectCompetitionCountStats();

    List<String> selectCompetitionTypes();

    /**
     * 查询竞赛列表（支持分页）
     * @param competition 竞赛查询条件
     * @return 竞赛列表
     */
    List<Competition> selectCompetitionList(Competition competition);

    /**
     * 根据ID查询竞赛
     * @param competitionId 竞赛ID
     * @return 竞赛信息
     */
    Competition selectCompetitionById(Long competitionId);

    /**
     * 新增竞赛
     * @param competition 竞赛信息
     * @return 结果
     */
    int insertCompetition(Competition competition);

    /**
     * 修改竞赛
     * @param competition 竞赛信息
     * @return 结果
     */
    int updateCompetition(Competition competition);

    /**
     * 批量删除竞赛
     * @param competitionIds 需要删除的竞赛ID数组
     * @return 结果
     */
    int deleteCompetitionByIds(Long[] competitionIds);

    /**
     * 删除竞赛信息
     * @param competitionId 竞赛ID
     * @return 结果
     */
    int deleteCompetitionById(Long competitionId);

    // 路径：org.iflytek.system.service.IAdminCompetitionService

    // 增加参数支持
    List<Map<String, Object>> selectTeamListByCompId(Long competitionId, String sortType, Double minScore, Double maxScore);

    // 增加查询评分明细接口
    List<Map<String, Object>> selectScoresByParticipationId(Long participationId);

    /**
     * 查询赛事评审老师池
     */
    List<Map<String, Object>> selectReviewersByCompetitionId(Long competitionId);

    /**
     * 查询可加入赛事评审老师池的老师
     */
    List<Map<String, Object>> selectReviewerCandidatesByCompetitionId(Long competitionId);

    /**
     * 添加老师进入赛事评审老师池
     */
    Map<String, Object> addReviewersToCompetition(Long competitionId, List<Long> reviewerIds, Long operatorId);

    /**
     * 自动分配赛事评审老师
     */
    Map<String, Object> autoAssignReviewers(Long competitionId, Integer reviewerCountPerTeam, Long operatorId);

    /**
     * 撤销老师赛事评审资格并处理分配
     */
    Map<String, Object> revokeReviewerAndReassign(Long competitionId, Long reviewerId, Long operatorId);

    /**
     * 手动调整单队评审老师
     */
    Map<String, Object> updateTeamReviewers(Long competitionId, Long registerId, Long teamId, List<Long> reviewerIds, Long operatorId);
}
