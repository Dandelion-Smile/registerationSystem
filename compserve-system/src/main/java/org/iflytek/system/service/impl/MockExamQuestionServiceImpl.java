package org.iflytek.system.service.impl;

import java.util.Arrays;
import java.util.List;

import org.iflytek.common.exception.ServiceException;
import org.iflytek.system.domain.MockExamQuestion;
import org.iflytek.system.mapper.MockExamQuestionMapper;
import org.iflytek.system.service.IMockExamQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 模拟考试题库服务实现
 */
@Service
public class MockExamQuestionServiceImpl implements IMockExamQuestionService
{
    @Autowired
    private MockExamQuestionMapper mockExamQuestionMapper;

    @Override
    public List<MockExamQuestion> selectMockExamQuestionList(MockExamQuestion query)
    {
        return mockExamQuestionMapper.selectMockExamQuestionList(query);
    }

    @Override
    public MockExamQuestion selectMockExamQuestionById(Long questionId)
    {
        return mockExamQuestionMapper.selectMockExamQuestionById(questionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMockExamQuestion(MockExamQuestion question)
    {
        validateQuestion(question, true);
        return mockExamQuestionMapper.insertMockExamQuestion(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMockExamQuestion(MockExamQuestion question)
    {
        if (question.getQuestionId() == null)
        {
            throw new ServiceException("题目ID不能为空");
        }
        validateQuestion(question, false);
        return mockExamQuestionMapper.updateMockExamQuestion(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMockExamQuestionByIds(Long[] questionIds)
    {
        if (questionIds == null || questionIds.length == 0)
        {
            throw new ServiceException("题目ID数组不能为空");
        }
        int count = 0;
        for (Long id : questionIds)
        {
            count += mockExamQuestionMapper.deleteMockExamQuestionById(id);
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMockExamQuestionById(Long questionId)
    {
        if (questionId == null)
        {
            throw new ServiceException("题目ID不能为空");
        }
        return mockExamQuestionMapper.deleteMockExamQuestionById(questionId);
    }

    /**
     * 基础字段校验
     */
    private void validateQuestion(MockExamQuestion question, boolean isInsert)
    {
        if (question == null)
        {
            throw new ServiceException("题目信息不能为空");
        }

        if (isInsert)
        {
            if (question.getType() == null || question.getType().trim().isEmpty())
            {
                throw new ServiceException("题目类型不能为空");
            }
            if (question.getContent() == null || question.getContent().trim().isEmpty())
            {
                throw new ServiceException("题目内容不能为空");
            }
        }

        if (question.getType() != null)
        {
            String type = question.getType();
            List<String> allowed = Arrays.asList("judge", "single", "multiple");
            if (!allowed.contains(type))
            {
                throw new ServiceException("题目类型不合法，应为 judge/single/multiple");
            }
        }

        if (question.getScore() != null && question.getScore() < 0)
        {
            throw new ServiceException("分值不能为负数");
        }
    }
}

