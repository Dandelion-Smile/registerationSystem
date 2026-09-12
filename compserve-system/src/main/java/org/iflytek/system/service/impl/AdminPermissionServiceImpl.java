package org.iflytek.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iflytek.common.core.domain.entity.SysRole;
import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.common.exception.ServiceException;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionPermission;
import org.iflytek.system.domain.CompetitionRegister;
import org.iflytek.system.domain.TeacherTeam;
import org.iflytek.system.domain.TeacherTeamRel;
import org.iflytek.system.mapper.CompetitionMapper;
import org.iflytek.system.mapper.CompetitionPermissionMapper;
import org.iflytek.system.mapper.CompetitionRegisterMapper;
import org.iflytek.system.mapper.SysUserMapper;
import org.iflytek.system.mapper.TeacherTeamMapper;
import org.iflytek.system.mapper.TeacherTeamRelMapper;
import org.iflytek.system.service.IAdminPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-side review permission service implementation.
 */
@Service
public class AdminPermissionServiceImpl implements IAdminPermissionService
{
    private static final Logger log = LoggerFactory.getLogger(AdminPermissionServiceImpl.class);

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private CompetitionPermissionMapper permissionMapper;

    @Autowired
    private CompetitionRegisterMapper registerMapper;

    @Autowired
    private TeacherTeamMapper teacherTeamMapper;

    @Autowired
    private TeacherTeamRelMapper teacherTeamRelMapper;

    @Autowired
    private org.iflytek.system.mapper.SysRoleMapper roleMapper;

    @Override
    public List<Map<String, Object>> listAllCompetitions()
    {
        List<Competition> competitions = competitionMapper.selectCompetitionList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Competition comp : competitions)
        {
            Map<String, Object> item = new HashMap<>();
            item.put("competitionId", comp.getCompetitionId());
            item.put("competitionName", comp.getCompetitionName());
            item.put("competitionType", comp.getCompetitionType());
            item.put("registerStartTime", comp.getRegisterStartTime());
            item.put("registerEndTime", comp.getRegisterEndTime());
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> listAllTeachers()
    {
        SysUser queryUser = new SysUser();
        queryUser.setDelFlag("0");
        List<SysUser> users = sysUserMapper.selectUserList(queryUser);

        List<Map<String, Object>> result = new ArrayList<>();

        for (SysUser user : users)
        {
            List<SysRole> roles = roleMapper.selectRolePermissionByUserId(user.getUserId());
            boolean isTeacher = roles.stream().anyMatch(r -> "teacher".equals(r.getRoleKey()));

            if (isTeacher)
            {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", user.getUserId());
                item.put("userName", user.getUserName());
                item.put("nickName", user.getNickName());
                item.put("phonenumber", user.getPhonenumber());
                item.put("email", user.getEmail());
                result.add(item);
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchGrantPermission(Long competitionId, List<Long> teacherIds)
    {
        if (competitionId == null)
        {
            throw new ServiceException("Competition ID cannot be null");
        }
        if (teacherIds == null || teacherIds.isEmpty())
        {
            throw new ServiceException("Teacher ID list cannot be empty");
        }

        int successCount = 0;
        for (Long teacherId : teacherIds)
        {
            try
            {
                CompetitionPermission existing = permissionMapper.selectByUserAndCompetition(teacherId, competitionId);
                if (existing != null)
                {
                    if (existing.getPermissionId() != 1)
                    {
                        existing.setPermissionId(1);
                        permissionMapper.updatePermission(existing);
                        successCount++;
                    }
                    continue;
                }

                CompetitionPermission permission = new CompetitionPermission();
                permission.setUserId(teacherId);
                permission.setCompetitionId(competitionId);
                permission.setPermissionId(1);

                permissionMapper.insertPermission(permission);
                successCount++;
            }
            catch (Exception e)
            {
                log.error("Failed to grant review permission for teacher ID {} on competition ID {}", teacherId, competitionId, e);
            }
        }

        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int revokePermission(Long competitionId, Long teacherId)
    {
        if (competitionId == null || teacherId == null)
        {
            throw new ServiceException("Competition ID and teacher ID cannot be null");
        }

        return permissionMapper.deletePermission(teacherId, competitionId);
    }

    @Override
    public List<Map<String, Object>> listPermissions(Long competitionId, Long teacherId)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        List<CompetitionPermission> permissions = permissionMapper.selectAll();

        for (CompetitionPermission permission : permissions)
        {
            if (competitionId != null && !permission.getCompetitionId().equals(competitionId))
            {
                continue;
            }
            if (teacherId != null && !permission.getUserId().equals(teacherId))
            {
                continue;
            }
            if (permission.getPermissionId() != 1)
            {
                continue;
            }

            Competition competition = competitionMapper.selectCompetitionById(permission.getCompetitionId());
            if (competition == null)
            {
                continue;
            }

            SysUser teacher = sysUserMapper.selectUserById(permission.getUserId());
            if (teacher == null)
            {
                continue;
            }

            Map<String, Object> item = new HashMap<>();
            item.put("userId", permission.getUserId());
            item.put("competitionId", permission.getCompetitionId());
            item.put("permissionId", permission.getPermissionId());
            item.put("competitionName", competition.getCompetitionName());
            item.put("competitionType", competition.getCompetitionType());
            item.put("teacherName", teacher.getNickName() != null ? teacher.getNickName() : teacher.getUserName());
            item.put("teacherUserName", teacher.getUserName());
            item.put("teacherPhone", teacher.getPhonenumber());
            item.put("teacherEmail", teacher.getEmail());
            item.put("advisorConflict", false);
            item.put("advisorConflictMessage", null);

            result.add(item);
        }

        return result;
    }

    @Override
    public boolean isAdvisor(Long teacherId, Long competitionId)
    {
        List<CompetitionRegister> registers = registerMapper.selectByCompetitionId(competitionId);
        SysUser teacher = sysUserMapper.selectUserById(teacherId);
        if (teacher == null || StringUtils.isEmpty(teacher.getPhonenumber()) || registers == null || registers.isEmpty())
        {
            return false;
        }

        List<Long> teamIds = new ArrayList<>();
        for (CompetitionRegister register : registers)
        {
            if (register.getTeamId() != null && !teamIds.contains(register.getTeamId()))
            {
                teamIds.add(register.getTeamId());
            }
        }
        if (teamIds.isEmpty())
        {
            return false;
        }

        List<TeacherTeam> baseTeachers = teacherTeamMapper.selectBaseTeachersByPhone(teacher.getPhonenumber());
        if (baseTeachers == null || baseTeachers.isEmpty())
        {
            return false;
        }
        List<Long> teacherIds = new ArrayList<>();
        for (TeacherTeam baseTeacher : baseTeachers)
        {
            teacherIds.add(baseTeacher.getId());
        }

        List<TeacherTeamRel> advisorRows = teacherTeamRelMapper.selectByTeamIds(teamIds);
        if (advisorRows == null || advisorRows.isEmpty())
        {
            return false;
        }
        return advisorRows.stream().anyMatch(row -> teacherIds.contains(row.getTeacherId()));
    }
}
