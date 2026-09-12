package org.iflytek.system.service.impl;

import java.util.List;

import org.iflytek.common.exception.ServiceException;
import org.iflytek.system.domain.MockExamPaper;
import org.iflytek.system.mapper.MockExamPaperMapper;
import org.iflytek.system.mapper.MockExamPaperQuestionMapper;
import org.iflytek.system.service.IMockExamPaperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 模拟试卷管理服务实现
 */
@Service
public class MockExamPaperServiceImpl implements IMockExamPaperService
{
    @Autowired
    private MockExamPaperMapper mockExamPaperMapper;

    @Autowired
    private MockExamPaperQuestionMapper mockExamPaperQuestionMapper;

    @Override
    public List<MockExamPaper> selectMockExamPaperList(MockExamPaper query)
    {
        return mockExamPaperMapper.selectMockExamPaperList(query);
    }

    @Override
    public List<MockExamPaper> selectEnabledPapers(String keyword)
    {
        return mockExamPaperMapper.selectEnabledPapers(keyword);
    }

    @Override
    public MockExamPaper selectMockExamPaperById(Long paperId)
    {
        return mockExamPaperMapper.selectMockExamPaperById(paperId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMockExamPaper(MockExamPaper paper)
    {
        if (paper == null)
        {
            throw new ServiceException("试卷信息不能为空");
        }
        if (paper.getTitle() == null || paper.getTitle().trim().isEmpty())
        {
            throw new ServiceException("试卷标题不能为空");
        }
        if (paper.getDuration() == null || paper.getDuration() <= 0)
        {
            throw new ServiceException("考试时长必须大于0");
        }
        if (paper.getEnabled() == null)
        {
            paper.setEnabled(1);
        }
        return mockExamPaperMapper.insertMockExamPaper(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMockExamPaper(MockExamPaper paper)
    {
        if (paper == null || paper.getPaperId() == null)
        {
            throw new ServiceException("试卷ID不能为空");
        }
        return mockExamPaperMapper.updateMockExamPaper(paper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMockExamPaperById(Long paperId)
    {
        if (paperId == null)
        {
            throw new ServiceException("试卷ID不能为空");
        }
        // 先删关联题目
        mockExamPaperQuestionMapper.deleteByPaperId(paperId);
        // 再删试卷
        return mockExamPaperMapper.deleteMockExamPaperById(paperId);
    }
}

