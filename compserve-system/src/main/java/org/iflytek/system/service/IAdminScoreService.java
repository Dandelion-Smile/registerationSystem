package org.iflytek.system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 管理员端评分管理服务接口
 */
public interface IAdminScoreService
{
    /**
     * 查询评分结果列表（支持分页和筛选）
     * @param competitionId 竞赛ID（可选，用于筛选）
     * @return 评分结果列表
     */
    List<Map<String, Object>> listScoreResults(Long competitionId);

    /**
     * 查询竞赛排名（按平均分排序）
     * @param competitionId 竞赛ID
     * @return 排名列表
     */
    List<Map<String, Object>> getCompetitionRanking(Long competitionId);

    /**
     * 查询竞赛排名（按平均分排序，返回前N名）
     * @param competitionId 竞赛ID
     * @param topN 返回前N名
     * @return 排名列表
     */
    List<Map<String, Object>> getCompetitionRanking(Long competitionId, Integer topN);

    /**
     * 确认竞赛排名（将排名结果保存或标记为已确认）
     * @param competitionId 竞赛ID
     * @param rankingList 排名列表
     * @return 结果
     */
    int confirmRanking(Long competitionId, List<Map<String, Object>> rankingList);
}
