package org.iflytek.system.service;

import org.iflytek.system.domain.CompetitionFile;

import java.util.List;

public interface ICompetitionFileService {
    int saveCompetitionFiles(Long competitionId, List<CompetitionFile> files, Long uploadUserId);
    List<CompetitionFile> getCompetitionFilesByCompetitionId(Long competitionId);
    String generateSignedUrl(String fileUrl);
}
