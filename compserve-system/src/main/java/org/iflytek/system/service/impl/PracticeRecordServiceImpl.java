package org.iflytek.system.service.impl;

import java.util.List;

import org.iflytek.system.domain.PracticeRecord;
import org.iflytek.system.mapper.PracticeRecordMapper;
import org.iflytek.system.service.IPracticeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PracticeRecordServiceImpl implements IPracticeRecordService {

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;

    @Override
    public int insertPracticeRecord(PracticeRecord record) {
        if (record == null || record.getUserId() == null || record.getQuestionId() == null) {
            return 0;
        }
        return practiceRecordMapper.insertPracticeRecord(record);
    }

    @Override
    public List<PracticeRecord> selectPracticeRecordListByUserId(Long userId) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        return practiceRecordMapper.selectPracticeRecordListByUserId(userId);
    }
}

