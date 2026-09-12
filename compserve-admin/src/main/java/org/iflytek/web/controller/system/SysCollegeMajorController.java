package org.iflytek.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.core.domain.entity.SysCollegeMajor;
import org.iflytek.system.service.ISysCollegeMajorService;

/**
 * 学院专业Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/college-major")
public class SysCollegeMajorController extends BaseController
{
    @Autowired
    private ISysCollegeMajorService collegeMajorService;

    /**
     * 查询所有学院名称
     */
    @GetMapping("/colleges")
    public AjaxResult getAllColleges()
    {
        List<String> colleges = collegeMajorService.selectAllCollegeNames();
        return success(colleges);
    }

    /**
     * 根据学院名称查询专业列表
     */
    @GetMapping("/majors")
    public AjaxResult getMajorsByCollege(@RequestParam("collegeName") String collegeName)
    {
        List<String> majors = collegeMajorService.selectMajorsByCollege(collegeName);
        return success(majors);
    }

    /**
     * 查询学院专业列表
     */
    @GetMapping("/list")
    public AjaxResult list(SysCollegeMajor collegeMajor)
    {
        List<SysCollegeMajor> list = collegeMajorService.selectCollegeMajorList(collegeMajor);
        return success(list);
    }

    /**
     * 查询学院专业详细
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(collegeMajorService.selectCollegeMajorById(id));
    }

    /**
     * 新增学院专业
     */
    @RequestMapping("/add")
    public AjaxResult add(SysCollegeMajor collegeMajor)
    {
        return toAjax(collegeMajorService.insertCollegeMajor(collegeMajor));
    }

    /**
     * 修改学院专业
     */
    @RequestMapping("/edit")
    public AjaxResult edit(SysCollegeMajor collegeMajor)
    {
        return toAjax(collegeMajorService.updateCollegeMajor(collegeMajor));
    }

    /**
     * 删除学院专业
     */
    @RequestMapping("/remove")
    public AjaxResult remove(Long id)
    {
        return toAjax(collegeMajorService.deleteCollegeMajorById(id));
    }

    /**
     * 批量删除学院专业
     */
    @RequestMapping("/batchRemove")
    public AjaxResult batchRemove(Long[] ids)
    {
        return toAjax(collegeMajorService.deleteCollegeMajorByIds(ids));
    }
}
