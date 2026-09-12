package org.iflytek.system.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import com.github.pagehelper.PageHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.iflytek.common.core.domain.entity.SysRole;
import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.common.core.page.PageDomain;
import org.iflytek.common.core.page.TableSupport;
import org.iflytek.common.exception.ServiceException;
import org.iflytek.common.utils.PageUtils;
import org.iflytek.common.utils.SecurityUtils;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.common.utils.sql.SqlUtil;
import org.iflytek.system.domain.TeacherTeam;
import org.iflytek.system.mapper.SysRoleMapper;
import org.iflytek.system.mapper.SysUserMapper;
import org.iflytek.system.mapper.SysUserRoleMapper;
import org.iflytek.system.mapper.TeacherTeamMapper;
import org.iflytek.system.mapper.TeacherTeamRelMapper;
import org.iflytek.system.service.IAdminUserService;
import org.iflytek.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * 管理员端用户管理服务实现
 */
@Service
public class AdminUserServiceImpl implements IAdminUserService
{
    private static final Logger log = LoggerFactory.getLogger(AdminUserServiceImpl.class);

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private TeacherTeamMapper teacherTeamMapper;

    @Autowired
    private TeacherTeamRelMapper teacherTeamRelMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchCreateTeachers(String accountPrefix, Integer count, String defaultPassword, String nicknamePrefix)
    {
        if (StringUtils.isEmpty(accountPrefix))
        {
            throw new ServiceException("账号前缀不能为空");
        }
        if (count == null || count <= 0)
        {
            throw new ServiceException("创建数量必须大于0");
        }
        if (count > 100)
        {
            throw new ServiceException("一次最多创建100个账号");
        }

        // 获取教师角色ID
        SysRole teacherRole = roleMapper.checkRoleKeyUnique("teacher");
        if (teacherRole == null)
        {
            throw new ServiceException("系统中不存在教师角色，请先创建角色");
        }
        Long teacherRoleId = teacherRole.getRoleId();

        List<Map<String, Object>> successAccounts = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (int i = 1; i <= count; i++)
        {
            try
            {
                // 生成账号（补零，如：001, 002, 010）
                String account = accountPrefix + String.format("%03d", i);

                // 检查账号是否已存在
                SysUser existUser = userMapper.checkUserNameUnique(account);
                if (existUser != null)
                {
                    log.warn("账号已存在，跳过：{}", account);
                    failCount++;
                    continue;
                }

                // 生成昵称
                String nickname = (StringUtils.isNotEmpty(nicknamePrefix) ? nicknamePrefix : "教师")
                    + String.format("%03d", i);

                // 生成密码（如果未指定，使用默认规则：账号+123456）
                String password = StringUtils.isNotEmpty(defaultPassword)
                    ? defaultPassword
                    : account + "123456";
                String encryptedPassword = SecurityUtils.encryptPassword(password);

                // 创建用户
                SysUser user = new SysUser();
                user.setUserName(account);
                user.setNickName(nickname);
                user.setPassword(encryptedPassword);
                user.setStatus("0"); // 正常状态
                user.setCreateBy("admin"); // 创建人
                user.setRoleIds(new Long[] { teacherRoleId }); // 分配教师角色

                // 插入用户
                int rows = sysUserService.insertUser(user);
                if (rows > 0)
                {
                    // 记录成功
                    Map<String, Object> accountInfo = new HashMap<>();
                    accountInfo.put("userId", user.getUserId());
                    accountInfo.put("account", account);
                    accountInfo.put("password", password); // 返回明文密码供导出
                    accountInfo.put("nickname", nickname);
                    successAccounts.add(accountInfo);
                    successCount++;
                }
                else
                {
                    failCount++;
                }
            }
            catch (Exception e)
            {
                log.error("创建教师账号失败：{}", e.getMessage(), e);
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("accounts", successAccounts);
        return result;
    }

    @Override
    public List<SysUser> listTeachers(SysUser user)
    {
        PageHelper.clearPage();
        SysRole teacherRole = roleMapper.checkRoleKeyUnique("teacher");
        if (teacherRole == null)
        {
            return new ArrayList<>();
        }
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
        user.setRoleId(teacherRole.getRoleId());
        return sysUserService.selectAllocatedList(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTeacher(SysUser user)
    {
        if (StringUtils.isEmpty(user.getPhonenumber()))
        {
            throw new ServiceException("新增教师失败，联系电话不能为空");
        }
        // 获取教师角色ID
        SysRole teacherRole = roleMapper.checkRoleKeyUnique("teacher");
        if (teacherRole == null)
        {
            throw new ServiceException("系统中不存在教师角色，请先创建角色");
        }
        user.setRoleIds(new Long[] { teacherRole.getRoleId() });
        int rows = sysUserService.insertUser(user);
        if (rows > 0)
        {
            upsertTeacherProfile(user, null);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTeacher(SysUser user)
    {
        String oldPhone = null;
        if (user.getUserId() != null)
        {
            SysUser oldUser = sysUserService.selectUserById(user.getUserId());
            oldPhone = oldUser == null ? null : oldUser.getPhonenumber();
        }
        // 确保角色仍然是教师角色
        SysRole teacherRole = roleMapper.checkRoleKeyUnique("teacher");
        if (teacherRole != null)
        {
            // 如果用户没有指定角色，保持教师角色
            if (user.getRoleIds() == null || user.getRoleIds().length == 0)
            {
                user.setRoleIds(new Long[] { teacherRole.getRoleId() });
            }
        }
        int rows = sysUserService.updateUser(user);
        if (rows > 0)
        {
            upsertTeacherProfile(user, oldPhone);
        }
        return rows;
    }

    private void upsertTeacherProfile(SysUser user, String oldPhone)
    {
        if (StringUtils.isEmpty(user.getPhonenumber()))
        {
            return;
        }
        String phone = user.getPhonenumber();
        TeacherTeam teacherTeam = new TeacherTeam();
        teacherTeam.setTeacherName(StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName());
        teacherTeam.setSex(user.getSex());
        teacherTeam.setPhone(phone);
        teacherTeam.setEmail(user.getEmail());
        teacherTeam.setWorkUnit(user.getWorkUnit());
        teacherTeam.setTitle(user.getTitle());
        teacherTeam.setPosition(user.getPosition());
        teacherTeam.setPoliticalStatus(user.getPoliticalStatus());
        teacherTeam.setBirthDate(user.getBirthDate());
        if (StringUtils.isNotEmpty(oldPhone) && !oldPhone.equals(phone))
        {
            TeacherTeam oldProfile = teacherTeamMapper.selectBaseTeacherByPhone(oldPhone);
            if (oldProfile != null)
            {
                teacherTeam.setId(oldProfile.getId());
                teacherTeamMapper.updateBaseTeacherById(teacherTeam);
                return;
            }
        }
        TeacherTeam exists = teacherTeamMapper.selectBaseTeacherByPhone(phone);
        if (exists == null)
        {
            teacherTeamMapper.insertBaseTeacher(teacherTeam);
        }
        else
        {
            teacherTeamMapper.updateBaseTeacherByPhone(teacherTeam);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTeachers(Long[] userIds)
    {
        log.info("开始删除教师，用户ID列表: {}", java.util.Arrays.toString(userIds));
        // 先获取要删除用户的手机号
        List<SysUser> users = userMapper.selectUsersByIds(java.util.Arrays.asList(userIds));
        for (SysUser user : users) {
            if (user != null && StringUtils.isNotEmpty(user.getPhonenumber())) {
                // 获取教师表中的记录（可能有多条）
                List<TeacherTeam> teacherTeams = teacherTeamMapper.selectBaseTeachersByPhone(user.getPhonenumber());
                for (TeacherTeam teacherTeam : teacherTeams) {
                    // 先删除团队关系表中的记录
                    int relDeleted = teacherTeamRelMapper.deleteByTeacherId(teacherTeam.getId());
                    log.info("删除教师团队关系记录，教师ID: {}, 删除条数: {}", teacherTeam.getId(), relDeleted);
                }
                // 删除教师团队表中的所有相关记录
                int deleted = teacherTeamMapper.deleteByPhone(user.getPhonenumber());
                log.info("删除教师团队表记录，手机号: {}, 删除条数: {}", user.getPhonenumber(), deleted);
            }
        }
        // 删除用户相关记录
        int result = sysUserService.deleteUserByIds(userIds);
        log.info("删除教师完成，删除条数: {}", result);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resetTeacherPassword(Long userId, String newPassword)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        return userMapper.updateUser(user);
    }

    @Override
    public List<SysUser> listStudents(SysUser user)
    {
        PageHelper.clearPage();
        SysRole studentRole = roleMapper.checkRoleKeyUnique("student");
        if (studentRole == null)
        {
            return new ArrayList<>();
        }
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
        user.setRoleId(studentRole.getRoleId());
        return sysUserService.selectAllocatedList(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStudent(SysUser user)
    {
        // 确保角色仍然是学生角色
        SysRole studentRole = roleMapper.checkRoleKeyUnique("student");
        if (studentRole != null)
        {
            if (user.getRoleIds() == null || user.getRoleIds().length == 0)
            {
                user.setRoleIds(new Long[] { studentRole.getRoleId() });
            }
        }
        return sysUserService.updateUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resetStudentPassword(Long userId, String newPassword)
    {
        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setPassword(SecurityUtils.encryptPassword(newPassword));
        return userMapper.updateUser(user);
    }

    @Override
    public Map<String, String> getStudentAvatars(List<String> studentNos)
    {
        Map<String, String> map = new HashMap<>();
        if (studentNos == null || studentNos.isEmpty())
        {
            return map;
        }
        // 规范化学号，去空格、去空
        List<String> normalized = new java.util.ArrayList<>();
        for (String no : studentNos)
        {
            if (StringUtils.isNotEmpty(no))
            {
                String t = no.trim();
                if (StringUtils.isNotEmpty(t) && !normalized.contains(t))
                {
                    normalized.add(t);
                }
            }
        }
        if (normalized.isEmpty())
        {
            return map;
        }
        List<SysUser> users = userMapper.selectUsersByStudentNos(normalized);
        if (users == null)
        {
            return map;
        }
        for (SysUser u : users)
        {
            if (u != null && StringUtils.isNotEmpty(u.getStudentNo()))
            {
                map.put(u.getStudentNo(), StringUtils.nvl(u.getAvatar(), ""));
            }
        }
        // 对于未命中的学号，尝试用姓名兜底（常见原因：队伍里学号书写错误，但姓名正确）
        List<String> missingNos = new java.util.ArrayList<>();
        for (String no : normalized)
        {
            if (!map.containsKey(no))
            {
                missingNos.add(no);
            }
        }
        if (!missingNos.isEmpty())
        {
            // 这里无法直接拿到姓名；由调用方传入 studentNames 更合适，此处保持简单：不做二次查找
        }
        return map;
    }

    @Override
    public Map<Long, String> getUserAvatarsByIds(List<Long> userIds)
    {
        Map<Long, String> map = new HashMap<>();
        if (userIds == null || userIds.isEmpty())
        {
            return map;
        }
        List<Long> normalized = new java.util.ArrayList<>();
        for (Long id : userIds)
        {
            if (id != null && id > 0 && !normalized.contains(id))
            {
                normalized.add(id);
            }
        }
        if (normalized.isEmpty())
        {
            return map;
        }
        List<SysUser> users = userMapper.selectUsersByIds(normalized);
        if (users == null)
        {
            return map;
        }
        for (SysUser u : users)
        {
            if (u != null && u.getUserId() != null)
            {
                map.put(u.getUserId(), StringUtils.nvl(u.getAvatar(), ""));
            }
        }
        return map;
    }

    @Override
    public Map<String, String> getStudentAvatarsByNames(List<String> names)
    {
        Map<String, String> map = new HashMap<>();
        if (names == null || names.isEmpty())
        {
            return map;
        }
        List<String> normalized = new java.util.ArrayList<>();
        for (String n : names)
        {
            if (StringUtils.isNotEmpty(n))
            {
                String t = n.trim();
                if (StringUtils.isNotEmpty(t) && !normalized.contains(t))
                {
                    normalized.add(t);
                }
            }
        }
        if (normalized.isEmpty())
        {
            return map;
        }
        List<SysUser> users = userMapper.selectUsersByStudentNames(normalized);
        if (users == null)
        {
            return map;
        }
        for (SysUser u : users)
        {
            if (u != null && StringUtils.isNotEmpty(u.getStudentName()))
            {
                map.put(u.getStudentName().trim(), StringUtils.nvl(u.getAvatar(), ""));
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> importTeachers(MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new ServiceException("导入文件不能为空");
        }

        // 获取教师角色ID
        SysRole teacherRole = roleMapper.checkRoleKeyUnique("teacher");
        if (teacherRole == null)
        {
            throw new ServiceException("系统中不存在教师角色，请先创建角色");
        }
        Long teacherRoleId = teacherRole.getRoleId();

        int successCount = 0;
        int failCount = 0;
        // 用于记录已处理的教师信息，检测同一文件内的重复
        java.util.Set<String> existingUserNames = new java.util.HashSet<>();
        java.util.Set<String> existingPhones = new java.util.HashSet<>();
        java.util.Set<String> existingEmails = new java.util.HashSet<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null)
            {
                throw new ServiceException("Excel文件中没有工作表");
            }

            // 遍历行（跳过表头）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                {
                    continue;
                }

                try {
                    // 读取单元格数据
                    String nickName = getCellValue(row.getCell(0));
                    String sexText = getCellValue(row.getCell(1));
                    String phonenumber = getCellValue(row.getCell(2));
                    // 清理电话号码，移除所有非数字字符
                    if (StringUtils.isNotEmpty(phonenumber)) {
                        phonenumber = phonenumber.replaceAll("\\D", "");
                    }
                    String email = getCellValue(row.getCell(3));
                    String userName = getCellValue(row.getCell(4));
                    String password = getCellValue(row.getCell(5));
                    String workUnit = getCellValue(row.getCell(6));
                    String title = getCellValue(row.getCell(7));
                    String position = getCellValue(row.getCell(8));
                    String politicalStatus = getCellValue(row.getCell(9));
                    String birthDate = getCellValue(row.getCell(10));

                    // 格式化出生年月为 YYYY-MM 格式（支持只填写年月的情况）
                    birthDate = formatBirthDate(birthDate);

                    // 处理性别字段，将文本转换为数字
                    String sex = "0"; // 默认男
                    if ("女".equals(sexText)) {
                        sex = "1";
                    } else if ("男".equals(sexText)) {
                        sex = "0";
                    }

                    // 状态默认为正常
                    String status = "0";

                    // 验证账号
                    if (StringUtils.isEmpty(userName))
                    {
                        log.warn("第{}行：账号不能为空", i + 1);
                        failCount++;
                        continue;
                    }

                    // 检查账号是否已存在（数据库中）
                    SysUser existUser = userMapper.checkUserNameUnique(userName);
                    if (existUser != null)
                    {
                        log.warn("第{}行：账号已存在于系统中，跳过：{}", i + 1, userName);
                        failCount++;
                        continue;
                    }

                    // 检查账号是否在当前文件中重复
                    if (existingUserNames.contains(userName))
                    {
                        log.warn("第{}行：账号在当前文件中重复，跳过：{}", i + 1, userName);
                        failCount++;
                        continue;
                    }

                    // 验证手机号码
                    if (StringUtils.isEmpty(phonenumber))
                    {
                        log.warn("第{}行：手机号码不能为空", i + 1);
                        failCount++;
                        continue;
                    }

                    // 验证手机号码格式（必须是11位数字）
                    if (phonenumber.length() != 11 || !phonenumber.matches("^1[3-9]\\d{9}$"))
                    {
                        log.warn("第{}行：手机号码[{}]格式不正确，长度={}，必须是11位数字", i + 1, phonenumber, phonenumber.length());
                        failCount++;
                        continue;
                    }

                    // 检查手机号码是否已存在（数据库中）
                    SysUser existPhoneUser = userMapper.checkPhoneUnique(phonenumber);
                    if (existPhoneUser != null)
                    {
                        log.warn("第{}行：手机号码已存在于系统中，跳过：{}", i + 1, phonenumber);
                        failCount++;
                        continue;
                    }

                    // 检查手机号码是否在当前文件中重复
                    if (existingPhones.contains(phonenumber))
                    {
                        log.warn("第{}行：手机号码在当前文件中重复，跳过：{}", i + 1, phonenumber);
                        failCount++;
                        continue;
                    }

                    // 检查邮箱是否已存在（数据库中）
                    if (StringUtils.isNotEmpty(email))
                    {
                        SysUser existEmailUser = userMapper.checkEmailUnique(email);
                        if (existEmailUser != null)
                        {
                            log.warn("第{}行：邮箱已存在于系统中，跳过：{}", i + 1, email);
                            failCount++;
                            continue;
                        }

                        // 检查邮箱是否在当前文件中重复
                        if (existingEmails.contains(email))
                        {
                            log.warn("第{}行：邮箱在当前文件中重复，跳过：{}", i + 1, email);
                            failCount++;
                            continue;
                        }
                    }

                    // 生成密码（如果未指定，使用默认规则：账号+123456）
                    if (StringUtils.isEmpty(password))
                    {
                        password = userName + "123456";
                    }
                    String encryptedPassword = SecurityUtils.encryptPassword(password);

                    // 创建用户
                    SysUser user = new SysUser();
                    user.setUserName(userName);
                    user.setNickName(StringUtils.isNotEmpty(nickName) ? nickName : userName);
                    user.setSex(StringUtils.isNotEmpty(sex) ? sex : "0");
                    user.setPhonenumber(phonenumber);
                    user.setEmail(email);
                    user.setPassword(encryptedPassword);
                    user.setStatus(StringUtils.isNotEmpty(status) ? status : "0"); // 正常状态
                    user.setWorkUnit(workUnit);
                    user.setTitle(title);
                    user.setPosition(position);
                    user.setPoliticalStatus(politicalStatus);
                    user.setBirthDate(birthDate);
                    user.setCreateBy("admin"); // 创建人
                    user.setRoleIds(new Long[] { teacherRoleId }); // 分配教师角色

                    // 插入用户（使用编程式事务确保独立）
                    try {
                        log.info("准备插入教师数据 - 账号:{}, 姓名:{}, 手机号:{}, 手机号长度:{}", userName, nickName, phonenumber, phonenumber.length());
                        transactionTemplate.execute(txStatus -> {
                            sysUserService.insertUser(user);
                            upsertTeacherProfile(user, null);
                            return null;
                        });
                        // 记录已成功导入的信息，用于检测后续重复
                        existingUserNames.add(userName);
                        existingPhones.add(phonenumber);
                        if (StringUtils.isNotEmpty(email))
                        {
                            existingEmails.add(email);
                        }
                        successCount++;
                    } catch (Exception e) {
                        log.error("导入教师账号失败（第{}行）：{}", i + 1, e.getMessage(), e);
                        failCount++;
                    }
                }
                catch (Exception e)
                {
                    log.error("导入教师账号失败（第{}行）：{}", i + 1, e.getMessage(), e);
                    failCount++;
                }
            }
        }
        catch (IOException e)
        {
            log.error("读取Excel文件失败：{}", e.getMessage(), e);
            throw new ServiceException("读取Excel文件失败");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        return result;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell)
    {
        if (cell == null)
        {
            return "";
        }

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return StringUtils.trim(cell.getStringCellValue());
                case NUMERIC:
                    // 尝试判断是否为日期类型（Excel日期序列号范围）
                    double numericValue = cell.getNumericCellValue();
                    // Excel日期序列号：1900-01-01 = 1, 2100-12-31 ≈ 730485
                    // 也包含负数日期（1900年之前）
                    if (DateUtil.isCellDateFormatted(cell) || (numericValue > 0 && numericValue < 730485) || (numericValue > -693594 && numericValue < 0)) {
                        try {
                            // 将日期格式化为 YYYY-MM 格式（数据库字段为 varchar(7)）
                            Date dateValue = cell.getDateCellValue();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                            sdf.setLenient(false);
                            String formattedDate = sdf.format(dateValue);
                            log.debug("日期解析成功：原始值={}, 格式化后={}", numericValue, formattedDate);
                            return formattedDate;
                        } catch (Exception e) {
                            log.warn("日期解析失败：原始值={}, 错误={}", numericValue, e.getMessage());
                            // 如果日期解析失败，当作普通数字处理
                        }
                    }
                    // 处理数字类型，避免科学计数法，确保电话号码正确
                    if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                        // 整数类型，转换为长整型再转字符串，避免科学计数法
                        return StringUtils.trim(String.valueOf((long) numericValue));
                    } else {
                        return StringUtils.trim(String.valueOf(numericValue));
                    }
                case BOOLEAN:
                    return StringUtils.trim(String.valueOf(cell.getBooleanCellValue()));
                case FORMULA:
                    return StringUtils.trim(cell.getStringCellValue());
                default:
                    return "";
            }
        } catch (Exception e) {
            // 尝试将单元格类型设置为字符串，然后获取值
            try {
                cell.setCellType(CellType.STRING);
                return StringUtils.trim(cell.getStringCellValue());
            } catch (Exception ex) {
                return "";
            }
        }
    }

    /**
     * 格式化出生年月为 YYYY-MM 格式
     * 支持多种输入格式：
     * - YYYY-MM-DD -> YYYY-MM
     * - YYYY/MM/DD -> YYYY-MM
     * - YYYY-MM -> YYYY-MM
     * - YYYY/MM -> YYYY-MM
     * - YYYY年MM月DD日 -> YYYY-MM
     * - YYYY年MM月 -> YYYY-MM
     * - YYYYMMDD -> YYYY-MM
     * - YYYYMM -> YYYY-MM
     * - 不规范格式如 1905/6/8 -> YYYY-MM
     */
    private String formatBirthDate(String birthDate) {
        if (StringUtils.isEmpty(birthDate)) {
            return birthDate;
        }
        
        // 去除首尾空格
        birthDate = StringUtils.trim(birthDate);
        
        // 首先尝试解析为Excel日期序列号（如 32933 对应某个日期）
        // Excel日期序列号：1900-01-01 = 1, 2100-12-31 ≈ 730485
        try {
            double excelSerial = Double.parseDouble(birthDate);
            if (excelSerial > 0 && excelSerial < 730485) {
                // 使用Apache POI的日期工具转换Excel序列号
                Date dateValue = DateUtil.getJavaDate(excelSerial);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                String formattedDate = sdf.format(dateValue);
                log.debug("Excel日期序列号解析成功：原始值={}, 格式化后={}", excelSerial, formattedDate);
                return formattedDate;
            }
        } catch (Exception e) {
            // 不是有效数字，继续尝试其他格式
        }
        
        // 定义可能的日期格式
        String[] patterns = {
            "yyyy-MM-dd", "yyyy/MM/dd", "yyyy-MM", "yyyy/MM",
            "yyyy年MM月dd日", "yyyy年MM月",
            "yyyy.MM.dd", "yyyy.MM",
            "yyyyMMdd", "yyyyMM"
        };
        
        // 尝试解析日期字符串
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                Date date = sdf.parse(birthDate);
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM");
                return outputFormat.format(date);
            } catch (Exception e) {
                // 尝试下一个格式
                continue;
            }
        }
        
        // 尝试处理不规范的日期格式（如 1905/6/8）
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{4})[/\\-.年](\\d{1,2})[/\\-.月]?(\\d{1,2})?");
            java.util.regex.Matcher matcher = pattern.matcher(birthDate);
            if (matcher.find()) {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                if (month >= 1 && month <= 12) {
                    return String.format("%04d-%02d", year, month);
                }
            }
        } catch (Exception e) {
            // 忽略解析错误
        }
        
        // 如果都无法解析，返回原值（后续验证会报错）
        return birthDate;
    }
}
