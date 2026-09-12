package org.iflytek.system.service;

import java.util.List;

import org.iflytek.system.domain.PracticeRecord;

/**
 * 刷题训练记录 Service 接口
 */
public interface IPracticeRecordService {

    /**
     * 新增训练记录
     */
    int insertPracticeRecord(PracticeRecord record);

    /**
     * 根据用户ID查询训练记录列表
     */
    List<PracticeRecord> selectPracticeRecordListByUserId(Long userId);
}

