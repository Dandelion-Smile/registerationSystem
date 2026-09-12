package org.iflytek.web.controller.admin;

import java.util.List;

import org.iflytek.common.annotation.Log;
import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.core.page.TableDataInfo;
import org.iflytek.common.enums.BusinessType;
import org.iflytek.common.utils.PageUtils;
import org.iflytek.system.domain.MockExamQuestion;
import org.iflytek.system.service.IMockExamQuestionService;
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
 * 管理员端模拟题库管理 Controller
 *
 * 用于配置判断题、单选题、多选题。
 */
@RestController
@RequestMapping("/admin/mock-question")
public class AdminMockExamQuestionController extends BaseController
{
    @Autowired
    private IMockExamQuestionService mockExamQuestionService;

    /**
     * 查询题目列表（支持分页与简单筛选）
     */
    @PreAuthorize("@ss.hasPermi('admin:mockQuestion:list')")
    @GetMapping("/list")
    public TableDataInfo list(MockExamQuestion query)
    {
        PageUtils.startPage();
        List<MockExamQuestion> list = mockExamQuestionService.selectMockExamQuestionList(query);
        return getDataTable(list);
    }

    /**
     * 根据ID查询题目详情
     */
    @PreAuthorize("@ss.hasPermi('admin:mockQuestion:query')")
    @GetMapping("/{questionId}")
    public AjaxResult getInfo(@PathVariable Long questionId)
    {
        MockExamQuestion question = mockExamQuestionService.selectMockExamQuestionById(questionId);
        return success(question);
    }

    /**
     * 新增题目
     */
    @PreAuthorize("@ss.hasPermi('admin:mockQuestion:add')")
    @Log(title = "模拟题库管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MockExamQuestion question)
    {
        int result = mockExamQuestionService.insertMockExamQuestion(question);
        return result > 0 ? success() : error("新增题目失败");
    }

    /**
     * 修改题目
     */
    @PreAuthorize("@ss.hasPermi('admin:mockQuestion:edit')")
    @Log(title = "模拟题库管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MockExamQuestion question)
    {
        int result = mockExamQuestionService.updateMockExamQuestion(question);
        return result > 0 ? success() : error("修改题目失败");
    }

    /**
     * 删除题目（支持批量）
     */
    @PreAuthorize("@ss.hasPermi('admin:mockQuestion:remove')")
    @Log(title = "模拟题库管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{questionIds}")
    public AjaxResult remove(@PathVariable Long[] questionIds)
    {
        int result = mockExamQuestionService.deleteMockExamQuestionByIds(questionIds);
        return result > 0 ? success() : error("删除题目失败");
    }
}

