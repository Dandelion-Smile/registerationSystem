package org.iflytek.system.service.impl;

import org.iflytek.system.domain.CompetitionFile;
import org.iflytek.system.mapper.CompetitionFileMapper;
import org.iflytek.system.service.ICompetitionFileService;
import org.iflytek.common.utils.file.MinioUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompetitionFileServiceImpl implements ICompetitionFileService {

    @Autowired
    private CompetitionFileMapper competitionFileMapper;

    @Autowired
    private MinioUtils minioUtils;

    @Override
    public int saveCompetitionFiles(Long competitionId, List<CompetitionFile> files, Long uploadUserId) {
        // 先删除该竞赛的所有文件
        competitionFileMapper.deleteCompetitionFileByCompetitionId(competitionId);
        
        // 保存新文件
        int count = 0;
        for (CompetitionFile file : files) {
            file.setCompetitionId(competitionId);
            count += competitionFileMapper.insertCompetitionFile(file);
        }
        return count;
    }

    @Override
    public List<CompetitionFile> getCompetitionFilesByCompetitionId(Long competitionId) {
        return competitionFileMapper.selectCompetitionFilesByCompetitionId(competitionId);
    }

    @Override
    public String generateSignedUrl(String fileUrl) {
        return minioUtils.getPrivateUrl(fileUrl);
    }
}
