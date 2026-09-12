package org.iflytek.system.service;

import java.util.List;

import org.iflytek.system.domain.MockExamPaper;

/**
 * 模拟试卷管理服务接口（管理员 & 学生端共用）
 */
public interface IMockExamPaperService
{
    /**
     * 查询试卷列表（管理员分页）
     */
    List<MockExamPaper> selectMockExamPaperList(MockExamPaper query);

    /**
     * 查询启用的试卷列表（学生端大厅）
     */
    List<MockExamPaper> selectEnabledPapers(String keyword);

    /**
     * 根据ID查询试卷
     */
    MockExamPaper selectMockExamPaperById(Long paperId);

    /**
     * 新增试卷
     */
    int insertMockExamPaper(MockExamPaper paper);

    /**
     * 修改试卷
     */
    int updateMockExamPaper(MockExamPaper paper);

    /**
     * 删除试卷
     */
    int deleteMockExamPaperById(Long paperId);
}

