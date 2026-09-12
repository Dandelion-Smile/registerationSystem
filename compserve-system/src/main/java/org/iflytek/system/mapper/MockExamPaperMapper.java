package org.iflytek.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.iflytek.system.domain.MockExamPaper;

/**
 * 模拟试卷 Mapper
 */
public interface MockExamPaperMapper
{
    /**
     * 查询试卷列表（用于管理员分页）
     */
    List<MockExamPaper> selectMockExamPaperList(MockExamPaper query);

    /**
     * 查询启用的试卷列表（用于学生端大厅）
     */
    List<MockExamPaper> selectEnabledPapers(@Param("keyword") String keyword);

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

