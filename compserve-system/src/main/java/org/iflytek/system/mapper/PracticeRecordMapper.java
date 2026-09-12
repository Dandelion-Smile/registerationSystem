package org.iflytek.system.mapper;

import java.util.List;

import org.iflytek.system.domain.PracticeRecord;

/**
 * 刷题训练记录 Mapper
 */
public interface PracticeRecordMapper {

    /**
     * 新增训练记录
     */
    int insertPracticeRecord(PracticeRecord record);

    /**
     * 根据用户ID查询训练记录列表（按时间倒序）
     */
    List<PracticeRecord> selectPracticeRecordListByUserId(Long userId);
}

