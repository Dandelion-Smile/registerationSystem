package org.iflytek.system.mapper;

import org.iflytek.system.domain.CompetitionFile;

import java.util.List;

public interface CompetitionFileMapper {
    int insertCompetitionFile(CompetitionFile competitionFile);
    int deleteCompetitionFileByCompetitionId(Long competitionId);
    List<CompetitionFile> selectCompetitionFilesByCompetitionId(Long competitionId);
}
