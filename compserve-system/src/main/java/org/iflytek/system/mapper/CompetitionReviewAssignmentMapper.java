package org.iflytek.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.iflytek.system.domain.CompetitionReviewAssignment;

/**
 * 赛事评审分配 Mapper
 */
public interface CompetitionReviewAssignmentMapper
{
    CompetitionReviewAssignment selectById(Long id);

    List<CompetitionReviewAssignment> selectByCompetitionId(Long competitionId);

    List<CompetitionReviewAssignment> selectByParticipationId(Long participationId);

    List<CompetitionReviewAssignment> selectByParticipationIds(@Param("participationIds") List<Long> participationIds);

    List<CompetitionReviewAssignment> selectByCompetitionAndParticipation(@Param("competitionId") Long competitionId,
            @Param("participationId") Long participationId);

    List<CompetitionReviewAssignment> selectByReviewerAndCompetition(@Param("reviewerId") Long reviewerId,
            @Param("competitionId") Long competitionId);

    CompetitionReviewAssignment selectByParticipationAndReviewer(@Param("participationId") Long participationId,
            @Param("reviewerId") Long reviewerId);

    int insertAssignment(CompetitionReviewAssignment assignment);

    int updateAssignment(CompetitionReviewAssignment assignment);

    int deleteById(Long id);

    int deletePendingByCompetitionId(Long competitionId);

    int deletePendingByCompetitionAndReviewer(@Param("competitionId") Long competitionId,
            @Param("reviewerId") Long reviewerId);

    int deletePendingByParticipationId(Long participationId);
}
