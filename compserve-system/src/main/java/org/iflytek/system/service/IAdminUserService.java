package org.iflytek.system.service;

import java.util.List;
import java.util.Map;

import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员端用户管理服务接口
 */
public interface IAdminUserService
{
    /**
     * 批量创建教师账号
     * @param accountPrefix 账号前缀
     * @param count 创建数量
     * @param defaultPassword 初始密码（可选，不传则使用默认规则）
     * @param nicknamePrefix 昵称前缀（可选）
     * @return 创建结果（成功数量、失败数量、账号列表）
     */
    Map<String, Object> batchCreateTeachers(String accountPrefix, Integer count, String defaultPassword, String nicknamePrefix);

    /**
     * 查询教师列表
     * @param user 查询条件
     * @return 教师列表
     */
    List<SysUser> listTeachers(SysUser user);

    /**
     * 新增单个教师账号
     * @param user 教师信息
     * @return 结果
     */
    int insertTeacher(SysUser user);

    /**
     * 更新教师账号
     * @param user 教师信息
     * @return 结果
     */
    int updateTeacher(SysUser user);

    /**
     * 删除教师账号
     * @param userIds 用户ID数组
     * @return 结果
     */
    int deleteTeachers(Long[] userIds);

    /**
     * 重置教师密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 结果
     */
    int resetTeacherPassword(Long userId, String newPassword);

    /**
     * 查询学生列表
     * @param user 查询条件
     * @return 学生列表
     */
    List<SysUser> listStudents(SysUser user);

    /**
     * 更新学生账号
     * @param user 学生信息
     * @return 结果
     */
    int updateStudent(SysUser user);

    /**
     * 重置学生密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 结果
     */
    int resetStudentPassword(Long userId, String newPassword);

    /**
     * 批量获取学生头像
     * @param studentNos 学号集合
     * @return 学号->头像地址 映射
     */
    Map<String, String> getStudentAvatars(List<String> studentNos);

    /**
     * 批量获取用户头像（按用户ID）
     * @param userIds 用户ID集合
     * @return 用户ID->头像地址 映射
     */
    Map<Long, String> getUserAvatarsByIds(List<Long> userIds);

    /**
     * 批量获取学生头像（按姓名）
     * @param names 姓名集合
     * @return 姓名->头像地址 映射
     */
    Map<String, String> getStudentAvatarsByNames(List<String> names);

    /**
     * Excel导入教师账号
     * @param file Excel文件
     * @return 导入结果（成功数量、失败数量）
     */
    Map<String, Object> importTeachers(MultipartFile file);
}