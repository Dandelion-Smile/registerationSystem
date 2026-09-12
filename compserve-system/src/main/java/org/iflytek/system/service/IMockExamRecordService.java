package org.iflytek.system.service;

import java.util.List;
import java.util.Map;

import org.iflytek.system.domain.MockExamRecord;

/**
 * 模拟考试作答记录服务
 */
public interface IMockExamRecordService
{
    /**
     * 提交试卷并自动判分，返回包含总分、明细等信息的结果
     *
     * @param userId 当前用户ID
     * @param paperId 试卷ID
     * @param answers 用户答案列表：每项包含 questionId 和 answer
     */
    Map<String, Object> submitAndMark(Long userId, Long paperId, List<Map<String, Object>> answers);

    /**
     * 查询某用户的考试记录列表
     */
    List<MockExamRecord> selectRecordsByUserId(Long userId);

    /**
     * 根据记录ID查询某次考试的详情（包含题目、作答明细）
     *
     * @param recordId 记录ID
     * @param userId   当前用户ID（用于权限校验）
     */
    Map<String, Object> getRecordDetail(Long recordId, Long userId);
}

