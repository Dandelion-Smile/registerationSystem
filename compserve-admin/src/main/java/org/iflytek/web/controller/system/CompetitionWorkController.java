package org.iflytek.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.iflytek.common.annotation.Log;
import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.core.page.TableDataInfo;
import org.iflytek.common.enums.BusinessType;
import org.iflytek.common.utils.poi.ExcelUtil;
import org.iflytek.system.domain.CompetitionWork;
import org.iflytek.system.service.ICompetitionWorkService;

/**
 * 竞赛作品附件Controller
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
@RestController
@RequestMapping("/system/work")
public class CompetitionWorkController extends BaseController
{
    @Autowired
    private ICompetitionWorkService competitionWorkService;

    /**
     * 查询竞赛作品附件列表
     */
    @PreAuthorize("@ss.hasPermi('system:work:list') or @ss.hasRole('student') or @ss.hasRole('teacher')")
    @GetMapping("/list")
    public TableDataInfo list(CompetitionWork competitionWork)
    {
        startPage();
        List<CompetitionWork> list = competitionWorkService.selectCompetitionWorkList(competitionWork);
        return getDataTable(list);
    }

    /**
     * 导出竞赛作品附件列表
     */
    @PreAuthorize("@ss.hasPermi('system:work:export')")
    @Log(title = "竞赛作品附件", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CompetitionWork competitionWork)
    {
        List<CompetitionWork> list = competitionWorkService.selectCompetitionWorkList(competitionWork);
        ExcelUtil<CompetitionWork> util = new ExcelUtil<CompetitionWork>(CompetitionWork.class);
        util.exportExcel(response, list, "竞赛作品附件数据");
    }

    /**
     * 获取竞赛作品附件详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:work:query') or @ss.hasRole('student') or @ss.hasRole('teacher')")
    @GetMapping(value = "/{workId}")
    public AjaxResult getInfo(@PathVariable("workId") Long workId)
    {
        return success(competitionWorkService.selectCompetitionWorkByWorkId(workId));
    }

    /**
     * 新增竞赛作品附件
     */
    @PreAuthorize("@ss.hasPermi('system:work:add') or @ss.hasRole('student')")
    @Log(title = "竞赛作品附件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CompetitionWork competitionWork)
    {
        return toAjax(competitionWorkService.insertCompetitionWork(competitionWork));
    }

    /**
     * 修改竞赛作品附件
     */
    @PreAuthorize("@ss.hasPermi('system:work:edit') or @ss.hasRole('student')")
    @Log(title = "竞赛作品附件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CompetitionWork competitionWork)
    {
        return toAjax(competitionWorkService.updateCompetitionWork(competitionWork));
    }

    /**
     * 删除竞赛作品附件
     */
    @PreAuthorize("@ss.hasPermi('system:work:remove') or @ss.hasRole('student')")
    @Log(title = "竞赛作品附件", businessType = BusinessType.DELETE)
	@DeleteMapping("/{workIds}")
    public AjaxResult remove(@PathVariable Long[] workIds)
    {
        return toAjax(competitionWorkService.deleteCompetitionWorkByWorkIds(workIds));
    }
}
