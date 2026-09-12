package org.iflytek.system.mapper;

import java.util.List;
import org.iflytek.system.domain.MockExamQuestion;

/**
 * 模拟考试题库 Mapper
 */
public interface MockExamQuestionMapper
{
    /**
     * 查询题目列表（用于 PageHelper 分页）
     */
    List<MockExamQuestion> selectMockExamQuestionList(MockExamQuestion query);

    /**
     * 根据ID查询题目
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
     * 删除单个题目
     */
    int deleteMockExamQuestionById(Long questionId);
}

