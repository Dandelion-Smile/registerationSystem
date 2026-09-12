package org.iflytek.web.controller.admin;

import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

import org.iflytek.common.annotation.Log;
import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.common.core.page.TableDataInfo;
import org.iflytek.common.enums.BusinessType;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.system.service.IAdminUserService;
import org.iflytek.system.service.ISysUserService;
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
 * 管理员端用户管理Controller
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController extends BaseController
{
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    @Autowired
    private IAdminUserService adminUserService;

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 批量创建教师账号
     */
    @PreAuthorize("@ss.hasPermi('admin:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/teachers/batch")
    public AjaxResult batchCreateTeachers(@RequestBody Map<String, Object> body)
    {
        String accountPrefix = (String) body.get("accountPrefix");
        Integer count = body.get("count") == null ? null : Integer.valueOf(body.get("count").toString());
        String defaultPassword = (String) body.get("defaultPassword");
        String nicknamePrefix = (String) body.get("nicknamePrefix");

        Map<String, Object> result = adminUserService.batchCreateTeachers(
            accountPrefix, count, defaultPassword, nicknamePrefix);
        return success(result);
    }

    /**
     * 查询教师列表
     */
    @PreAuthorize("@ss.hasPermi('admin:user:list')")
    @GetMapping("/teachers")
    public TableDataInfo listTeachers(SysUser user)
    {
        startPage();
        List<SysUser> list = adminUserService.listTeachers(user);
        return getDataTable(list);
    }

    /**
     * 获取教师详情
     */
    @PreAuthorize("@ss.hasPermi('admin:user:query')")
    @GetMapping("/teachers/{userId}")
    public AjaxResult getTeacherInfo(@PathVariable Long userId)
    {
        // 使用现有的userService获取用户详情
        SysUser user = sysUserService.selectUserById(userId);
        return success(user);
    }

    /**
     * 新增单个教师账号
     */
    @PreAuthorize("@ss.hasPermi('admin:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/teachers")
    public AjaxResult insertTeacher(@Validated @RequestBody SysUser user)
    {
        if (!checkUserNameUnique(user))
        {
            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        AjaxResult validation = validateTeacherContact(user);
        if (validation != null)
        {
            return validation;
        }
        user.setCreateBy(getUsername());
        int rows = adminUserService.insertTeacher(user);
        return rows > 0 ? success() : error();
    }

    /**
     * 更新教师账号
     */
    @PreAuthorize("@ss.hasPermi('admin:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/teachers")
    public AjaxResult updateTeacher(@Validated @RequestBody SysUser user)
    {
        AjaxResult validation = validateTeacherContact(user);
        if (validation != null)
        {
            return validation;
        }
        user.setUpdateBy(getUsername());
        int rows = adminUserService.updateTeacher(user);
        return rows > 0 ? success() : error();
    }

    /**
     * 删除教师账号
     */
    @PreAuthorize("@ss.hasPermi('admin:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/teachers/{userIds}")
    public AjaxResult deleteTeachers(@PathVariable Long[] userIds)
    {
        int rows = adminUserService.deleteTeachers(userIds);
        return rows > 0 ? success() : error();
    }

    /**
     * 重置教师密码
     */
    @PreAuthorize("@ss.hasPermi('admin:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/teachers/{userId}/resetPwd")
    public AjaxResult resetTeacherPassword(@PathVariable Long userId, @RequestBody Map<String, String> body)
    {
        String newPassword = body.get("newPassword");
        if (StringUtils.isEmpty(newPassword))
        {
            return error("新密码不能为空");
        }
        int rows = adminUserService.resetTeacherPassword(userId, newPassword);
        return rows > 0 ? success() : error();
    }

    /**
     * 修改教师状态
     */
    @PreAuthorize("@ss.hasPermi('admin:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/teachers/{userId}/status")
    public AjaxResult changeTeacherStatus(@PathVariable Long userId, @RequestBody Map<String, String> body)
    {
        String status = body.get("status");
        if (!"0".equals(status) && !"1".equals(status))
        {
            return error("教师状态参数不正确");
        }
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setStatus(status);
        int rows = sysUserService.updateUserStatus(user);
        return rows > 0 ? success() : error();
    }

    /**
     * Excel导入教师账号
     */
    @PreAuthorize("@ss.hasPermi('admin:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping("/teachers/import")
    public AjaxResult importTeachers(MultipartFile file)
    {
        Map<String, Object> result = adminUserService.importTeachers(file);
        return success(result);
    }

    /**
     * 查询学生列表
     */
    @PreAuthorize("@ss.hasPermi('admin:user:list')")
    @GetMapping("/students")
    public TableDataInfo listStudents(SysUser user)
    {
        startPage();
        List<SysUser> list = adminUserService.listStudents(user);
        return getDataTable(list);
    }

    /**
     * 更新学生账号
     */
    @PreAuthorize("@ss.hasPermi('admin:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/students")
    public AjaxResult updateStudent(@Validated @RequestBody SysUser user)
    {
        user.setUpdateBy(getUsername());
        int rows = adminUserService.updateStudent(user);
        return rows > 0 ? success() : error();
    }

    /**
     * 重置学生密码
     */
    @PreAuthorize("@ss.hasPermi('admin:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/students/{userId}/resetPwd")
    public AjaxResult resetStudentPassword(@PathVariable Long userId, @RequestBody Map<String, String> body)
    {
        String newPassword = body.get("newPassword");
        if (StringUtils.isEmpty(newPassword))
        {
            return error("新密码不能为空");
        }
        int rows = adminUserService.resetStudentPassword(userId, newPassword);
        return rows > 0 ? success() : error();
    }

    /**
     * 批量获取学生头像
     */
    @PreAuthorize("@ss.hasRole('student') or @ss.hasRole('teacher') or @ss.hasRole('sys_admin')")
    @PostMapping("/students/avatars")
    public AjaxResult getStudentAvatars(@RequestBody Map<String, Object> body)
    {
        Object arr = body.get("studentNos");
        List<String> studentNos = new java.util.ArrayList<>();
        if (arr instanceof java.util.List)
        {
            for (Object o : (List<?>) arr)
            {
                if (o != null) studentNos.add(String.valueOf(o));
            }
        }
        Map<String, String> map = adminUserService.getStudentAvatars(studentNos);
        return success(map);
    }

    /**
     * 批量获取用户头像（按用户ID）
     */
    @PreAuthorize("@ss.hasRole('student') or @ss.hasRole('teacher') or @ss.hasRole('sys_admin')")
    @PostMapping("/users/avatars")
    public AjaxResult getUserAvatarsByIds(@RequestBody Map<String, Object> body)
    {
        Object arr = body.get("userIds");
        List<Long> ids = new java.util.ArrayList<>();
        if (arr instanceof java.util.List)
        {
            for (Object o : (List<?>) arr)
            {
                try
                {
                    if (o != null) ids.add(Long.valueOf(String.valueOf(o)));
                }
                catch (Exception ignored) {}
            }
        }
        Map<Long, String> map = adminUserService.getUserAvatarsByIds(ids);
        return success(map);
    }

    /**
     * 批量获取学生头像（按姓名）
     */
    @PreAuthorize("@ss.hasRole('student') or @ss.hasRole('teacher') or @ss.hasRole('sys_admin')")
    @PostMapping("/students/avatars-by-names")
    public AjaxResult getStudentAvatarsByNames(@RequestBody Map<String, Object> body)
    {
        Object arr = body.get("names");
        List<String> names = new java.util.ArrayList<>();
        if (arr instanceof java.util.List)
        {
            for (Object o : (List<?>) arr)
            {
                if (o != null) names.add(String.valueOf(o));
            }
        }
        Map<String, String> map = adminUserService.getStudentAvatarsByNames(names);
        return success(map);
    }

    /**
     * 检查用户名是否唯一
     */
    private boolean checkUserNameUnique(SysUser user)
    {
        return sysUserService.checkUserNameUnique(user);
    }

    private AjaxResult validateTeacherContact(SysUser user)
    {
        if (StringUtils.isEmpty(user.getPhonenumber()))
        {
            return error("联系电话不能为空");
        }
        if (!user.getPhonenumber().matches(PHONE_PATTERN))
        {
            return error("联系电话格式不正确，请填写11位手机号");
        }
        if (!sysUserService.checkPhoneUnique(user))
        {
            return error("联系电话已存在，请更换后重试");
        }
        if (StringUtils.isNotEmpty(user.getEmail()))
        {
            if (!user.getEmail().matches(EMAIL_PATTERN))
            {
                return error("电子邮箱格式不正确");
            }
            if (!sysUserService.checkEmailUnique(user))
            {
                return error("电子邮箱已存在，请更换后重试");
            }
        }
        return null;
    }
}
