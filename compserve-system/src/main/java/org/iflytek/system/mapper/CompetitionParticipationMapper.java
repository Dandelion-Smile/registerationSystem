package org.iflytek.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.iflytek.system.domain.CompetitionParticipation;

/**
 * 参赛材料 Mapper
 */
public interface CompetitionParticipationMapper
{
    int insertCompetitionParticipation(CompetitionParticipation participation);

    int updateCompetitionParticipation(CompetitionParticipation participation);

    CompetitionParticipation selectById(Long participationId);

    List<CompetitionParticipation> selectByIds(@Param("participationIds") List<Long> participationIds);

    int deleteById(Long participationId);
}

