package org.iflytek.system.mapper;

import java.util.List;

import org.iflytek.system.domain.MockExamPaperQuestion;

/**
 * 模拟试卷题目关联 Mapper
 */
public interface MockExamPaperQuestionMapper
{
    /**
     * 根据试卷ID查询关联的题目
     */
    List<MockExamPaperQuestion> selectByPaperId(Long paperId);

    /**
     * 新增一条关联
     */
    int insertMockExamPaperQuestion(MockExamPaperQuestion relation);

    /**
     * 根据试卷ID删除全部关联
     */
    int deleteByPaperId(Long paperId);
}

