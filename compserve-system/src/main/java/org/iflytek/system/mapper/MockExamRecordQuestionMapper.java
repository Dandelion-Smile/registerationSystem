package org.iflytek.system.mapper;

import java.util.List;

import org.iflytek.system.domain.MockExamRecordQuestion;

/**
 * 模拟考试作答明细 Mapper
 */
public interface MockExamRecordQuestionMapper
{
    /**
     * 批量插入答题明细
     */
    int batchInsert(List<MockExamRecordQuestion> list);

    /**
     * 根据记录ID查询答题明细
     */
    List<MockExamRecordQuestion> selectByRecordId(Long recordId);
}

