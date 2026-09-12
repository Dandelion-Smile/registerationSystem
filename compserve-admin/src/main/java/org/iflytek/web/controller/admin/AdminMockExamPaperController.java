package org.iflytek.web.controller.admin;

import java.util.List;

import org.iflytek.common.annotation.Log;
import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.core.page.TableDataInfo;
import org.iflytek.common.enums.BusinessType;
import org.iflytek.common.utils.PageUtils;
import org.iflytek.system.domain.MockExamPaper;
import org.iflytek.system.domain.MockExamPaperQuestion;
import org.iflytek.system.service.IMockExamPaperService;
import org.iflytek.system.mapper.MockExamPaperQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端模拟试卷管理 Controller
 *
 * 用于配置模拟考试试卷及其包含的题目。
 */
@RestController
@RequestMapping("/admin/mock-paper")
public class AdminMockExamPaperController extends BaseController
{
    @Autowired
    private IMockExamPaperService mockExamPaperService;

    @Autowired
    private MockExamPaperQuestionMapper mockExamPaperQuestionMapper;

    /**
     * 查询试卷列表（支持分页）
     */
    @PreAuthorize("@ss.hasPermi('admin:mockPaper:list')")
    @GetMapping("/list")
    public TableDataInfo list(MockExamPaper query)
    {
        PageUtils.startPage();
        List<MockExamPaper> list = mockExamPaperService.selectMockExamPaperList(query);
        return getDataTable(list);
    }

    /**
     * 查询单个试卷（基础信息）
     */
    @PreAuthorize("@ss.hasPermi('admin:mockPaper:query')")
    @GetMapping("/{paperId}")
    public AjaxResult getInfo(@PathVariable Long paperId)
    {
        MockExamPaper paper = mockExamPaperService.selectMockExamPaperById(paperId);
        return success(paper);
    }

    /**
     * 查询试卷下的题目关联（仅返回关联表，关联的题目在题库管理中维护）
     */
    @PreAuthorize("@ss.hasPermi('admin:mockPaper:query')")
    @GetMapping("/{paperId}/questions")
    public AjaxResult getPaperQuestions(@PathVariable Long paperId)
    {
        List<MockExamPaperQuestion> list = mockExamPaperQuestionMapper.selectByPaperId(paperId);
        return success(list);
    }

    /**
     * 新增试卷
     */
    @PreAuthorize("@ss.hasPermi('admin:mockPaper:add')")
    @Log(title = "模拟试卷管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MockExamPaper paper)
    {
        int rows = mockExamPaperService.insertMockExamPaper(paper);
        return rows > 0 ? success(paper) : error("新增试卷失败");
    }

    /**
     * 修改试卷基础信息
     */
    @PreAuthorize("@ss.hasPermi('admin:mockPaper:edit')")
    @Log(title = "模拟试卷管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MockExamPaper paper)
    {
        int rows = mockExamPaperService.updateMockExamPaper(paper);
        return rows > 0 ? success() : error("修改试卷失败");
    }

    /**
     * 覆盖保存试卷题目关联（前端提交整套题目列表）
     */
    @PreAuthorize("@ss.hasPermi('admin:mockPaper:edit')")
    @Log(title = "模拟试卷题目配置", businessType = BusinessType.UPDATE)
    @PostMapping("/{paperId}/questions")
    public AjaxResult savePaperQuestions(@PathVariable Long paperId,
                                         @RequestBody List<MockExamPaperQuestion> questions)
    {
        // 先清空原有关联
        mockExamPaperQuestionMapper.deleteByPaperId(paperId);
        // 再插入新配置
        if (questions != null)
        {
            int order = 1;
            for (MockExamPaperQuestion q : questions)
            {
                q.setPaperId(paperId);
                if (q.getSortOrder() == null)
                {
                    q.setSortOrder(order++);
                }
                mockExamPaperQuestionMapper.insertMockExamPaperQuestion(q);
            }
        }
        return success();
    }

    /**
     * 删除试卷（同时会删除其题目关联）
     */
    @PreAuthorize("@ss.hasPermi('admin:mockPaper:remove')")
    @Log(title = "模拟试卷管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{paperId}")
    public AjaxResult remove(@PathVariable Long paperId)
    {
        int rows = mockExamPaperService.deleteMockExamPaperById(paperId);
        return rows > 0 ? success() : error("删除试卷失败");
    }
}

