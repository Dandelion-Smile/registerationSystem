package org.iflytek.system.service;

import java.util.List;
import org.iflytek.system.domain.MockExamQuestion;

/**
 * 模拟考试题库服务接口（管理员管理使用）
 */
public interface IMockExamQuestionService
{
    /**
     * 查询题目列表（支持按类型、难度、分类、关键词等筛选）
     */
    List<MockExamQuestion> selectMockExamQuestionList(MockExamQuestion query);

    /**
     * 根据ID查询题目详情
     */
    MockExamQuestion selectMockExamQuestionById(Long questionId);

    /**
     * 新增题目
     */
    int insertMockExamQuestion(MockExamQuestion question);

    /**
     * 修改题目
     */
    int updateMockExamQuestion(MockExamQuestion question);

    /**
     * 批量删除题目
     */
    int deleteMockExamQuestionByIds(Long[] questionIds);

    /**
     * 删除单个题目
     */
    int deleteMockExamQuestionById(Long questionId);
}

