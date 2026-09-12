package org.iflytek.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.iflytek.system.domain.MockExamRecord;

/**
 * 模拟考试作答记录 Mapper
 */
public interface MockExamRecordMapper
{
    /**
     * 插入一条记录
     */
    int insertMockExamRecord(MockExamRecord record);

    /**
     * 根据ID查询
     */
    MockExamRecord selectMockExamRecordById(Long recordId);

    /**
     * 查询某用户的记录列表（按提交时间倒序）
     */
    List<MockExamRecord> selectRecordsByUserId(@Param("userId") Long userId);
}

