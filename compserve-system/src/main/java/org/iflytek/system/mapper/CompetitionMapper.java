package org.iflytek.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionRegister;

/**
 * 竞赛信息 Mapper
 */
public interface CompetitionMapper
{

    // 添加到接口中
    public Map<String, Object> selectCompetitionCountStats();


    /**
     * 根据报名ID查询队伍作品详细档案
     * * @param registerId 报名记录ID
     * @return 包含作品描述、成员信息及平均分的报名实体对象
     */
    public CompetitionRegister selectTeamDetailById(Long registerId);

    /**
     * 根据比赛ID查询报名的队伍列表
     */
    public List<Map<String, Object>> selectTeamListByCompId(Long competitionId);


    /**
     * 查询竞赛列表
     * @param competition 包含查询条件（名称、状态等）
     * @return 结果
     */
    public List<Competition> selectCompetitionList(Competition competition);

    /**
     * 查询全部竞赛
     */
    List<Competition> selectCompetitionList();

    List<String> selectDistinctCompetitionTypes();

    /**
     * 分页查询竞赛
     *
     * @param offset 偏移量
     * @param limit  每页条数
     */
    List<Competition> selectCompetitionListByPage(int offset, int limit);

    /**
     * 分页查询竞赛（支持筛选）
     *
     * @param offset  偏移量
     * @param limit   每页条数
     * @param filters 筛选条件
     */
    List<Competition> selectCompetitionListByPageAndFilters(@Param("offset") int offset,
                                                            @Param("limit") int limit,
                                                            @Param("filters") Map<String, String> filters);

    /**
     * 根据ID查询竞赛
     */
    Competition selectCompetitionById(Long competitionId);

    /**
     * 查询竞赛列表（支持关键词和状态筛选，用于PageHelper分页）
     *
     * @param keyword 关键词（竞赛名称或描述）
     * @param status  状态（not_started/in_progress/ended）
     */
    List<Competition> selectCompetitionListWithFilters(@Param("keyword") String keyword, @Param("status") String status);

    /**
     * 新增竞赛
     */
    int insertCompetition(Competition competition);

    /**
     * 修改竞赛
     */
    int updateCompetition(Competition competition);

    /**
     * 删除竞赛
     */
    int deleteCompetitionById(Long competitionId);

    // 路径：org.iflytek.system.mapper.CompetitionMapper

    public List<Map<String, Object>> selectTeamListByCompId(@Param("competitionId") Long competitionId,
                                                            @Param("sortType") String sortType,
                                                            @Param("minScore") Double minScore,
                                                            @Param("maxScore") Double maxScore);

    public List<Map<String, Object>> selectScoresByParticipationId(Long participationId);
}

