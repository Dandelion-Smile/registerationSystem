package org.iflytek.system.mapper;

import java.util.List;
import org.iflytek.system.domain.CompetitionScore;

/**
 * 竞赛评分 Mapper
 */
public interface CompetitionScoreMapper
{
    /**
     * 根据ID查询评分记录
     */
    CompetitionScore selectById(Long scoreId);

    /**
     * 根据参赛ID查询所有评分记录
     */
    List<CompetitionScore> selectByParticipationId(Long participationId);

    /**
     * 插入评分记录
     */
    int insertScore(CompetitionScore score);

    /**
     * 更新评分记录
     */
    int updateScore(CompetitionScore score);

    /**
     * 根据ID删除评分记录
     */
    int deleteById(Long scoreId);

    /**
     * 查询所有评分记录
     */
    List<CompetitionScore> selectAll();
}
