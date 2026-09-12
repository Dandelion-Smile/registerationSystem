package org.iflytek.web.controller.admin;

import java.util.List;
import java.util.Map;

import org.iflytek.common.annotation.Log;
import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.enums.BusinessType;
import org.iflytek.system.service.IAdminPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端权限管理Controller
 */
@RestController
@RequestMapping("/admin/permission")
public class AdminPermissionController extends BaseController
{
    @Autowired
    private IAdminPermissionService adminPermissionService;

    /**
     * 查询所有竞赛列表（用于赋权选择）
     */
    @PreAuthorize("@ss.hasPermi('admin:permission:query')")
    @GetMapping("/competitions")
    public AjaxResult listAllCompetitions()
    {
        List<Map<String, Object>> list = adminPermissionService.listAllCompetitions();
        return success(list);
    }

    /**
     * 查询所有教师列表（用于赋权选择）
     */
    @PreAuthorize("@ss.hasPermi('admin:permission:query')")
    @GetMapping("/teachers")
    public AjaxResult listAllTeachers()
    {
        List<Map<String, Object>> list = adminPermissionService.listAllTeachers();
        return success(list);
    }

    /**
     * 批量赋权：为多个教师分配竞赛评审权限
     */
    @PreAuthorize("@ss.hasPermi('admin:permission:add')")
    @Log(title = "权限管理", businessType = BusinessType.INSERT)
    @PostMapping("/grant")
    public AjaxResult batchGrantPermission(@RequestBody Map<String, Object> body)
    {
        Long competitionId = body.get("competitionId") == null ? null
                : Long.valueOf(body.get("competitionId").toString());
        
        @SuppressWarnings("unchecked")
        List<Object> teacherIdsObj = (List<Object>) body.get("teacherIds");
        
        // 将teacherIds转换为Long类型列表
        List<Long> teacherIds = new java.util.ArrayList<>();
        if (teacherIdsObj != null)
        {
            for (Object id : teacherIdsObj)
            {
                if (id != null)
                {
                    teacherIds.add(Long.valueOf(id.toString()));
                }
            }
        }
        
        int successCount = adminPermissionService.batchGrantPermission(competitionId, teacherIds);
        return success(successCount);
    }

    /**
     * 撤销权限：删除教师的竞赛评审权限
     */
    @PreAuthorize("@ss.hasPermi('admin:permission:remove')")
    @Log(title = "权限管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/revoke/{competitionId}/{teacherId}")
    public AjaxResult revokePermission(
            @PathVariable Long competitionId,
            @PathVariable Long teacherId)
    {
        int result = adminPermissionService.revokePermission(competitionId, teacherId);
        return success(result);
    }

    /**
     * 查询已赋权的权限列表
     */
    @PreAuthorize("@ss.hasPermi('admin:permission:query')")
    @GetMapping("/list")
    public AjaxResult listPermissions(
            @RequestParam(value = "competitionId", required = false) Long competitionId,
            @RequestParam(value = "teacherId", required = false) Long teacherId)
    {
        List<Map<String, Object>> list = adminPermissionService.listPermissions(competitionId, teacherId);
        return success(list);
    }

    /**
     * 检查教师是否是该竞赛的指导老师
     */
    @PreAuthorize("@ss.hasPermi('admin:permission:query')")
    @GetMapping("/check-advisor")
    public AjaxResult checkAdvisor(
            @RequestParam Long teacherId,
            @RequestParam Long competitionId)
    {
        boolean isAdvisor = adminPermissionService.isAdvisor(teacherId, competitionId);
        return success(isAdvisor);
    }
}
