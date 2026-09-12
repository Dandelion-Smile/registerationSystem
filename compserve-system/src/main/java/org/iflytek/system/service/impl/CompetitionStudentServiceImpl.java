package org.iflytek.system.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iflytek.common.exception.ServiceException;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionParticipation;
import org.iflytek.system.domain.CompetitionRegister;
import org.iflytek.system.domain.CompetitionScore;
import org.iflytek.system.domain.TeacherTeam;
import org.iflytek.system.domain.TeacherTeamRel;
import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.common.utils.file.FileUtils;
import org.iflytek.system.domain.CompetitionWork;
import org.iflytek.system.mapper.CompetitionWorkMapper;
import org.iflytek.system.mapper.CompetitionMapper;
import org.iflytek.system.mapper.CompetitionParticipationMapper;
import org.iflytek.system.mapper.CompetitionRegisterMapper;
import org.iflytek.system.mapper.CompetitionReviewAssignmentMapper;
import org.iflytek.system.mapper.CompetitionScoreMapper;
import org.iflytek.system.mapper.SysUserMapper;
import org.iflytek.system.mapper.TeacherTeamMapper;
import org.iflytek.system.mapper.TeacherTeamRelMapper;
import org.iflytek.system.service.ICompetitionStudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Student-side competition registration service implementation.
 */
@Service
public class CompetitionStudentServiceImpl implements ICompetitionStudentService
{
    private static final Logger log = LoggerFactory.getLogger(CompetitionStudentServiceImpl.class);
    private static final ThreadLocal<RecentRegistrationsContext> RECENT_REGISTRATIONS_CONTEXT = new ThreadLocal<>();

    @Autowired
    private CompetitionWorkMapper competitionWorkMapper;

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private CompetitionRegisterMapper competitionRegisterMapper;

    @Autowired
    private CompetitionParticipationMapper competitionParticipationMapper;

    @Autowired
    private CompetitionReviewAssignmentMapper competitionReviewAssignmentMapper;

    @Autowired
    private CompetitionScoreMapper competitionScoreMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private TeacherTeamMapper teacherTeamMapper;

    @Autowired
    private TeacherTeamRelMapper teacherTeamRelMapper;

    @Autowired
    private org.iflytek.common.utils.file.MinioUtils minioUtils;

    @Override
    public List<Competition> selectAllCompetitions()
    {
        return competitionMapper.selectCompetitionList();
    }

    @Override
    public List<Competition> selectCompetitionsByPage(Integer pageNum, Integer pageSize)
    {
        int offset = (pageNum - 1) * pageSize;
        return competitionMapper.selectCompetitionListByPage(offset, pageSize);
    }

    @Override
    public List<Competition> selectCompetitionsByPageAndFilters(Integer pageNum, Integer pageSize, Map<String, String> filters)
    {
        int offset = (pageNum - 1) * pageSize;
        return competitionMapper.selectCompetitionListByPageAndFilters(offset, pageSize, filters);
    }

    @Override
    public List<String> selectCompetitionTypes()
    {
        return competitionMapper.selectDistinctCompetitionTypes();
    }

    @Override
    public List<Map<String, Object>> getCompetitionTeams(Long competitionId)
    {
        List<CompetitionRegister> registers = competitionRegisterMapper.selectByCompetitionId(competitionId);
        Map<Long, Map<String, Object>> uniqueTeams = new HashMap<>();
        for (CompetitionRegister r : registers) {
             if (r.getTeamId() != null && !uniqueTeams.containsKey(r.getTeamId())) {
                 Map<String, Object> map = new HashMap<>();
                 map.put("teamId", r.getTeamId());
                 map.put("teamName", r.getTeamName());
                 map.put("teamMembers", r.getTeamMembers());
                 map.put("competitionId", r.getCompetitionId());
                 map.put("createTime", r.getRegisterTime());
                 uniqueTeams.put(r.getTeamId(), map);
             }
        }
        return new ArrayList<>(uniqueTeams.values());
    }

    @Override
    public Competition getCompetitionById(Long competitionId)
    {
        return competitionMapper.selectCompetitionById(competitionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionRegister registerCompetition(Long competitionId, Long teamId, String teamName, String teamMembers,
            String workName, String workDescription, List<Map<String, Object>> teacherList, Long userId)
    {
        Competition competition = competitionMapper.selectCompetitionById(competitionId);
        if (competition == null)
        {
            throw new ServiceException("Competition not found");
        }
        Date now = new Date();
        if (competition.getRegisterStartTime() != null && competition.getRegisterStartTime().after(now))
        {
            throw new ServiceException("Registration has not started yet");
        }
        if (competition.getRegisterEndTime() != null && competition.getRegisterEndTime().before(now))
        {
            throw new ServiceException("Registration period has ended");
        }
        String finalTeamName;
        String finalTeamMembers = teamMembers;
        String finalWorkName = trimToNull(workName);
        String finalWorkDescription = trimToNull(workDescription);
        String finalTeacherName = joinTeacherNames(teacherList);
        boolean teacherBindingChanged = false;

        if (teamId != null)
        {
            // Join an existing team.
            List<CompetitionRegister> teamRegs = competitionRegisterMapper.selectByTeamId(teamId);
            if (teamRegs == null || teamRegs.isEmpty())
            {
                throw new ServiceException("Team not found");
            }
            CompetitionRegister teamInfo = resolveTeamRegister(teamRegs, null);
            if (!teamInfo.getCompetitionId().equals(competitionId))
            {
                throw new ServiceException("The team does not belong to this competition");
            }
            CompetitionRegister operatorRegister = competitionRegisterMapper.selectByCompetitionAndUserAndTeam(competitionId, userId, teamId);
            if (operatorRegister == null || operatorRegister.getTeamId() == null || !operatorRegister.getTeamId().equals(teamId))
            {
                throw new ServiceException("只有队长才能操作");
            }
            ensureTeamEditable(operatorRegister, userId, true);
            finalTeamName = StringUtils.isEmpty(teamName) ? teamInfo.getTeamName() : teamName;
            if (finalWorkName == null)
            {
                finalWorkName = trimToNull(teamInfo.getWorkName());
            }
            if (finalWorkDescription == null)
            {
                finalWorkDescription = trimToNull(teamInfo.getWorkDescription());
            }
            if (finalTeacherName == null)
            {
                finalTeacherName = trimToNull(teamInfo.getTeacherName());
            }
            teacherBindingChanged = teacherList != null && isTeacherBindingChanged(teamId, teacherList);

            // Update team members for an existing team.
            if (!StringUtils.isEmpty(teamMembers))
            {
                finalTeamMembers = teamMembers;
                competitionRegisterMapper.updateTeamMembersByTeamId(teamId, teamMembers);
            }
            else
            {
                finalTeamMembers = teamInfo.getTeamMembers();
            }
        }
        else
        {
            // Create a new team.
            if (StringUtils.isEmpty(teamName))
            {
                throw new ServiceException("Team name cannot be empty");
            }
            teacherBindingChanged = teacherList != null && !teacherList.isEmpty();
            // Normalize member payload.
            String normalizedMembers = teamMembers;
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> members = new ArrayList<>();
                if (!StringUtils.isEmpty(teamMembers)) {
                    members = mapper.readValue(teamMembers, new TypeReference<List<Map<String, Object>>>() {});
                }
                SysUser creator = sysUserMapper.selectUserById(userId);
                String myNo = null;
                if (creator != null) {
                    myNo = creator.getStudentNo() != null ? creator.getStudentNo() : creator.getUserName();
                }
                boolean hasCreator = false;
                List<Map<String, Object>> out = new ArrayList<>();
                for (Map<String, Object> m : members) {
                    String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                    String incomingStatus = String.valueOf(m.getOrDefault("status", "")).toLowerCase();
                    if (myNo != null && myNo.equals(mNo)) {
                        hasCreator = true;
                        m.put("status", "approved");
                        m.put("role", "leader");
                        m.remove("type");
                        m.remove("inviterId");
                        m.remove("inviterName");
                    } else if ("approved".equals(incomingStatus)) {
                        // 直接添加的成员无需本人确认，保留为正式队员
                        m.put("status", "approved");
                        if (StringUtils.isEmpty(String.valueOf(m.getOrDefault("role", "")))) {
                            m.put("role", "member");
                        }
                        m.remove("type");
                        m.remove("inviterId");
                        m.remove("inviterName");
                    } else {
                        // 邀请入队的成员保持待确认状态
                        m.put("status", "pending");
                        m.put("type", "invite");
                        if (creator != null) {
                            String inviterName = null;
                            String candidate = creator.getStudentName();
                            if (candidate == null || candidate.isEmpty()) {
                                candidate = creator.getNickName();
                            }
                            if ((candidate == null || candidate.isEmpty()) && creator.getUserName() != null && !creator.getUserName().isEmpty()) {
                                candidate = creator.getUserName();
                            }
                            inviterName = candidate;
                            m.put("inviterId", userId);
                            m.put("inviterName", inviterName);
                        }
                    }
                    out.add(m);
                }
                if (!hasCreator && myNo != null) {
                    Map<String, Object> me = new HashMap<>();
                    me.put("studentNo", myNo);
                    me.put("status", "approved");
                    me.put("role", "leader");
                    out.add(me);
                }
                normalizedMembers = mapper.writeValueAsString(out);
            }
            catch (Exception ignore)
            {
            }
            finalTeamName = teamName;
            finalTeamMembers = normalizedMembers;
        }

        if (teacherBindingChanged && !hasCompleteLeaderProfile(userId))
        {
            throw new ServiceException("请先完善队长信息后再添加指导老师");
        }

        ensureMembersUniqueAcrossCompetition(competitionId, teamId, finalTeamMembers);

        CompetitionRegister register = teamId == null
                ? null
                : competitionRegisterMapper.selectByCompetitionAndUserAndTeam(competitionId, userId, teamId);
        if (register == null)
        {
            register = new CompetitionRegister();
            register.setCompetitionId(competitionId);
            register.setTeamName(finalTeamName);
            register.setUserId(userId);
            register.setRegisterTime(now);
            register.setTeamMembers(finalTeamMembers);
            register.setWorkName(finalWorkName);
            register.setWorkDescription(finalWorkDescription);
            register.setTeacherName(finalTeacherName);

            if (teamId != null) {
                register.setTeamId(teamId);
                try {
                    competitionRegisterMapper.insertCompetitionRegister(register);
                } catch (Exception e) {
                    throw new ServiceException("您已加入该队伍，请勿重复操作");
                }
            } else {
                // Insert first so we can derive the new team ID.
                try {
                    competitionRegisterMapper.insertCompetitionRegister(register);
                } catch (Exception e) {
                    throw new ServiceException("创建队伍失败");
                }
                // Use registerId as teamId.
                Long newTeamId = register.getRegisterId();
                register.setTeamId(newTeamId);
                competitionRegisterMapper.updateCompetitionRegister(register);
                teamId = newTeamId;
            }

            CompetitionParticipation participation = new CompetitionParticipation();
            participation.setParticipationId(register.getRegisterId());
            participation.setParticipationStatus("\u672a\u63d0\u4ea4");
            competitionParticipationMapper.insertCompetitionParticipation(participation);
        }
        else
        {
            // Update an existing registration.
            if (teamId == null) {
                 // Fallback: reuse registerId as teamId if the record already exists.
                 teamId = register.getRegisterId();
            }
            register.setTeamId(teamId);
            register.setTeamName(finalTeamName);
            register.setTeamMembers(finalTeamMembers);
            register.setWorkName(finalWorkName == null ? register.getWorkName() : finalWorkName);
            register.setWorkDescription(finalWorkDescription == null ? register.getWorkDescription() : finalWorkDescription);
            register.setTeacherName(finalTeacherName == null ? register.getTeacherName() : finalTeacherName);
            competitionRegisterMapper.updateCompetitionRegister(register);

            CompetitionParticipation participation = competitionParticipationMapper.selectById(register.getRegisterId());
            if (participation == null)
            {
                participation = new CompetitionParticipation();
                participation.setParticipationId(register.getRegisterId());
                participation.setParticipationStatus("\u672a\u63d0\u4ea4");
                competitionParticipationMapper.insertCompetitionParticipation(participation);
            }
        }

        if (teamId != null && !StringUtils.isEmpty(finalTeamName))
        {
            competitionRegisterMapper.updateTeamNameByTeamId(teamId, finalTeamName);
            competitionRegisterMapper.updateTeamProfileByTeamId(teamId, register.getWorkName(), register.getWorkDescription(),
                    register.getTeacherName());
        }
        syncTeamMembersRegistration(competitionId, teamId, finalTeamName, finalTeamMembers, now);
        if (teacherList != null)
        {
            replaceTeacherTeams(teamId, teacherList);
        }

        return register;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CompetitionRegister saveTeamName(Long competitionId, Long teamId, String teamName, Long userId)
    {
        Competition competition = competitionMapper.selectCompetitionById(competitionId);
        if (competition == null)
        {
            throw new ServiceException("Competition not found");
        }
        if (StringUtils.isEmpty(teamName))
        {
            throw new ServiceException("Team name cannot be empty");
        }

        Date now = new Date();
        CompetitionRegister register = teamId == null
                ? null
                : competitionRegisterMapper.selectByCompetitionAndUserAndTeam(competitionId, userId, teamId);
        if (register != null)
        {
            ensureTeamEditable(register, userId, true);
        }
        String teamMembers = register == null ? null : register.getTeamMembers();
        if (StringUtils.isEmpty(teamMembers))
        {
            teamMembers = buildCreatorMemberJson(userId);
        }
        
        if (register == null)
        {
            register = new CompetitionRegister();
            register.setCompetitionId(competitionId);
            register.setUserId(userId);
            register.setTeamName(teamName);
            register.setTeamMembers(teamMembers);
            register.setRegisterTime(now);
            competitionRegisterMapper.insertCompetitionRegister(register);

            Long newTeamId = register.getRegisterId();
            register.setTeamId(newTeamId);
            competitionRegisterMapper.updateCompetitionRegister(register);

        }
        else
        {
            if (teamId == null)
            {
                teamId = register.getTeamId() == null ? register.getRegisterId() : register.getTeamId();
            }
            register.setTeamId(teamId);
            register.setTeamName(teamName);
            register.setTeamMembers(teamMembers);
            competitionRegisterMapper.updateCompetitionRegister(register);
        }

        Long finalTeamId = register.getTeamId() == null ? register.getRegisterId() : register.getTeamId();
        register.setTeamId(finalTeamId);
        competitionRegisterMapper.updateTeamNameByTeamId(finalTeamId, teamName);
        return register;
    }

    private String buildCreatorMemberJson(Long userId)
    {
        List<Map<String, Object>> members = new ArrayList<>();
        SysUser creator = sysUserMapper.selectUserById(userId);
        Map<String, Object> me = new HashMap<>();
        if (creator != null)
        {
            me.put("userId", creator.getUserId());
            me.put("name", firstNonEmptyValue(creator.getStudentName(), creator.getNickName(), creator.getUserName()));
            me.put("studentNo", StringUtils.isEmpty(creator.getStudentNo()) ? creator.getUserName() : creator.getStudentNo());
            me.put("college", creator.getCollegeName());
            me.put("major", creator.getMajorName());
            me.put("phone", creator.getPhonenumber());
            me.put("email", creator.getEmail());
        }
        me.put("status", "approved");
        me.put("role", "leader");
        members.add(me);
        try
        {
            return new ObjectMapper().writeValueAsString(members);
        }
        catch (Exception e)
        {
            return "[]";
        }
    }

    private String firstNonEmptyValue(String... values)
    {
        if (values == null)
        {
            return "";
        }
        for (String value : values)
        {
            if (!StringUtils.isEmpty(value))
            {
                return value;
            }
        }
        return "";
    }

    private void syncTeamMembersRegistration(Long competitionId, Long teamId, String teamName, String teamMembers, Date now)
    {
        if (StringUtils.isEmpty(teamMembers))
        {
            return;
        }
        CompetitionRegister teamProfile = teamId == null ? null
                : competitionRegisterMapper.selectByCompetitionAndTeam(competitionId, teamId);
        List<Map<String, Object>> members;
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            members = mapper.readValue(teamMembers, new TypeReference<List<Map<String, Object>>>() {});
        }
        catch (Exception e)
        {
            return;
        }
        if (members == null || members.isEmpty())
        {
            return;
        }
        for (Map<String, Object> m : members)
        {
            String status = m.get("status") == null ? "" : String.valueOf(m.get("status")).toLowerCase();
            if (!"approved".equals(status))
            {
                continue;
            }
            Object noObj = m.get("studentNo");
            String studentNo = noObj == null ? null : String.valueOf(noObj);
            if (StringUtils.isEmpty(studentNo))
            {
                continue;
            }
            SysUser user = sysUserMapper.selectUserByStudentNo(studentNo);
            if (user == null)
            {
                user = sysUserMapper.selectUserByUserName(studentNo);
            }
            if (user == null || user.getUserId() == null)
            {
                continue;
            }
            // 检查用户是否已在该队伍中
            CompetitionRegister existingInTeam = competitionRegisterMapper.selectByCompetitionAndUserAndTeam(competitionId, user.getUserId(), teamId);
            if (existingInTeam != null) {
                // 用户已在该队伍中，更新队伍信息
                existingInTeam.setTeamMembers(teamMembers);
                existingInTeam.setTeamName(teamName);
                if (teamProfile != null)
                {
                    existingInTeam.setWorkName(teamProfile.getWorkName());
                    existingInTeam.setWorkDescription(teamProfile.getWorkDescription());
                    existingInTeam.setTeacherName(teamProfile.getTeacherName());
                }
                competitionRegisterMapper.updateCompetitionRegister(existingInTeam);
                continue;
            }
            // 用户不在该队伍中，创建新的报名记录（允许同一赛事多个队伍）
            CompetitionRegister r = new CompetitionRegister();
            r.setCompetitionId(competitionId);
            r.setTeamId(teamId);
            r.setTeamName(teamName);
            r.setUserId(user.getUserId());
            r.setRegisterTime(now);
            r.setTeamMembers(teamMembers);
            if (teamProfile != null)
            {
                r.setWorkName(teamProfile.getWorkName());
                r.setWorkDescription(teamProfile.getWorkDescription());
                r.setTeacherName(teamProfile.getTeacherName());
            }
            try
            {
                competitionRegisterMapper.insertCompetitionRegister(r);
            }
            catch (Exception ignored)
            {
                continue;
            }
            CompetitionParticipation p = new CompetitionParticipation();
            p.setParticipationId(r.getRegisterId());
            p.setParticipationStatus("\u672a\u63d0\u4ea4");
            competitionParticipationMapper.insertCompetitionParticipation(p);
        }
    }

    private CompetitionRegister resolveDisplayRegister(CompetitionRegister ownRegister)
    {
        if (ownRegister == null)
        {
            return null;
        }
        if (ownRegister.getTeamId() == null)
        {
            return ownRegister;
        }
        List<CompetitionRegister> teamRegs = getTeamRegistersCached(
                ownRegister.getTeamId(),
                () -> competitionRegisterMapper.selectByTeamId(ownRegister.getTeamId()));
        if (teamRegs == null || teamRegs.isEmpty())
        {
            return ownRegister;
        }

        CompetitionRegister leaderRegister = null;
        CompetitionRegister fallbackRegister = null;
        for (CompetitionRegister candidate : teamRegs)
        {
            if (candidate == null)
            {
                continue;
            }
            if (ownRegister.getCompetitionId() != null && candidate.getCompetitionId() != null
                    && !ownRegister.getCompetitionId().equals(candidate.getCompetitionId()))
            {
                continue;
            }
            if (fallbackRegister == null || isEarlierRegister(candidate, fallbackRegister))
            {
                fallbackRegister = candidate;
            }
            if (isRegisterOwnedByApprovedLeader(candidate)
                    && (leaderRegister == null || isEarlierRegister(candidate, leaderRegister)))
            {
                leaderRegister = candidate;
            }
        }
        if (leaderRegister != null)
        {
            return leaderRegister;
        }
        return fallbackRegister != null ? fallbackRegister : ownRegister;
    }

    private boolean isEarlierRegister(CompetitionRegister candidate, CompetitionRegister baseline)
    {
        if (candidate == null)
        {
            return false;
        }
        if (baseline == null)
        {
            return true;
        }
        if (candidate.getRegisterId() != null && baseline.getRegisterId() != null)
        {
            return candidate.getRegisterId() < baseline.getRegisterId();
        }
        if (candidate.getRegisterTime() != null && baseline.getRegisterTime() != null)
        {
            return candidate.getRegisterTime().before(baseline.getRegisterTime());
        }
        return false;
    }

    private boolean isRegisterOwnedByApprovedLeader(CompetitionRegister register)
    {
        return isUserLeaderForRegister(register, register == null ? null : register.getUserId());
    }

    private boolean isActiveMemberStatus(String status)
    {
        String normalized = status == null ? "" : status.trim().toLowerCase();
        return "".equals(normalized) || "approved".equals(normalized) || "pending".equals(normalized);
    }

    private boolean isUserLeaderForRegister(CompetitionRegister register, Long userId)
    {
        if (register == null || userId == null)
        {
            return false;
        }
        if (register.getTeamId() == null || StringUtils.isEmpty(register.getTeamMembers()))
        {
            return userId.equals(register.getUserId());
        }

        SysUser user = getBaseUserByIdCached(userId, () -> sysUserMapper.selectBaseUserById(userId));
        String myNo = null;
        if (user != null)
        {
            myNo = user.getStudentNo() != null ? user.getStudentNo() : user.getUserName();
        }
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> members = mapper.readValue(register.getTeamMembers(),
                    new TypeReference<List<Map<String, Object>>>() {});
            if (members != null)
            {
                for (Map<String, Object> member : members)
                {
                    String mNo = String.valueOf(member.getOrDefault("studentNo", ""));
                    String status = String.valueOf(member.getOrDefault("status", "")).toLowerCase();
                    String role = String.valueOf(member.getOrDefault("role", "")).toLowerCase();
                    if (myNo != null && myNo.equals(mNo) && isActiveMemberStatus(status) && "leader".equals(role))
                    {
                        return true;
                    }
                }
                for (Map<String, Object> member : members)
                {
                    Object inviterIdObj = member.get("inviterId");
                    Long inviterId = null;
                    try
                    {
                        if (inviterIdObj != null)
                        {
                            inviterId = Long.valueOf(String.valueOf(inviterIdObj));
                        }
                    }
                    catch (Exception ignore)
                    {
                        inviterId = null;
                    }
                    if (inviterId != null && inviterId.equals(userId))
                    {
                        return true;
                    }
                }
            }
        }
        catch (Exception ignore)
        {
        }
        return false;
    }

    private boolean isReviewLockedStatus(String reviewStatusCode)
    {
        String code = reviewStatusCode == null ? "" : reviewStatusCode.trim().toLowerCase();
        return "in_review".equals(code) || "reviewed".equals(code);
    }

    private String getStudentNoByUserId(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        SysUser user = getBaseUserByIdCached(userId, () -> sysUserMapper.selectBaseUserById(userId));
        if (user == null)
        {
            return null;
        }
        return user.getStudentNo() != null ? user.getStudentNo() : user.getUserName();
    }

    private Map<String, Object> findTeamMember(List<Map<String, Object>> members, String studentNo)
    {
        if (members == null || StringUtils.isEmpty(studentNo))
        {
            return null;
        }
        String normalizedStudentNo = studentNo.trim();
        for (Map<String, Object> member : members)
        {
            if (member == null)
            {
                continue;
            }
            String memberNo = firstNonEmptyValue(
                    String.valueOf(member.getOrDefault("studentNo", "")),
                    String.valueOf(member.getOrDefault("memberStudentNo", "")),
                    String.valueOf(member.getOrDefault("applicantStudentNo", "")),
                    String.valueOf(member.getOrDefault("inviterStudentNo", "")));
            if (normalizedStudentNo.equals(StringUtils.isEmpty(memberNo) ? "" : memberNo.trim()))
            {
                return member;
            }
        }
        return null;
    }

    private CompetitionRegister resolveTeamRegister(List<CompetitionRegister> teamRegs, String studentNo)
    {
        if (teamRegs == null || teamRegs.isEmpty())
        {
            return null;
        }
        CompetitionRegister fallback = null;
        for (CompetitionRegister candidate : teamRegs)
        {
            if (candidate == null)
            {
                continue;
            }
            if (fallback == null)
            {
                fallback = candidate;
            }
            String membersJson = candidate.getTeamMembers();
            if (StringUtils.isEmpty(membersJson))
            {
                continue;
            }
            if (StringUtils.isEmpty(studentNo))
            {
                return candidate;
            }
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> members = mapper.readValue(membersJson,
                        new TypeReference<List<Map<String, Object>>>() {});
                if (findTeamMember(members, studentNo) != null)
                {
                    return candidate;
                }
            }
            catch (Exception ignore)
            {
            }
        }
        return fallback;
    }

    private void ensureTeamEditable(CompetitionRegister register, Long userId, boolean requireLeader)
    {
        if (register == null)
        {
            throw new ServiceException("队伍不存在");
        }
        if (requireLeader && !isUserLeaderForRegister(register, userId))
        {
            throw new ServiceException("只有队长才能操作");
        }
        register = cleanupPendingMembersForLockedTeam(register);
        CompetitionRegister displayRegister = resolveDisplayRegister(register);
        Long displayRegisterId = displayRegister == null ? null : displayRegister.getRegisterId();
        Map<String, String> reviewStatus = resolveStudentReviewStatus(displayRegisterId);
        if (isReviewLockedStatus(reviewStatus.get("code")))
        {
            throw new ServiceException("队伍已进入评审中或已评审，不能再修改报名信息");
        }
    }

    private List<CompetitionRegister> listUserRegisters(Long competitionId, Long userId)
    {
        if (competitionId == null || userId == null)
        {
            return new ArrayList<>();
        }
        List<CompetitionRegister> registers = competitionRegisterMapper.selectListByCompetitionAndUser(competitionId, userId);
        return registers == null ? new ArrayList<>() : registers;
    }

    private boolean isUserStillMemberOfRegister(CompetitionRegister register, Long userId)
    {
        if (register == null || userId == null)
        {
            return false;
        }
        if (register.getTeamId() == null || StringUtils.isEmpty(register.getTeamMembers()))
        {
            return userId.equals(register.getUserId());
        }
        String studentNo = getStudentNoByUserId(userId);
        if (StringUtils.isEmpty(studentNo))
        {
            return false;
        }
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> members = mapper.readValue(register.getTeamMembers(),
                    new TypeReference<List<Map<String, Object>>>() {});
            Map<String, Object> member = findTeamMember(members, studentNo);
            if (member == null)
            {
                return false;
            }
            String status = String.valueOf(member.getOrDefault("status", "")).toLowerCase();
            return isActiveMemberStatus(status);
        }
        catch (Exception ignore)
        {
            return true;
        }
    }

    private CompetitionRegister selectDefaultCompetitionRegister(Long competitionId, Long userId)
    {
        CompetitionRegister latestVisible = null;
        for (CompetitionRegister candidate : listUserRegisters(competitionId, userId))
        {
            CompetitionRegister cleaned = cleanupPendingMembersForLockedTeam(candidate);
            if (isUserStillMemberOfRegister(cleaned, userId))
            {
                if (isSubmittedRegistration(cleaned))
                {
                    return cleaned;
                }
                if (latestVisible == null)
                {
                    latestVisible = cleaned;
                }
            }
        }
        return latestVisible;
    }

    private CompetitionRegister cleanupPendingMembersForLockedTeam(CompetitionRegister register)
    {
        if (register == null || register.getTeamId() == null || StringUtils.isEmpty(register.getTeamMembers()))
        {
            return register;
        }
        CompetitionRegister displayRegister = resolveDisplayRegister(register);
        Long displayRegisterId = displayRegister == null ? null : displayRegister.getRegisterId();
        Map<String, String> reviewStatus = resolveStudentReviewStatus(displayRegisterId);
        if (!isReviewLockedStatus(reviewStatus.get("code")))
        {
            return register;
        }
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> members = mapper.readValue(register.getTeamMembers(),
                    new TypeReference<List<Map<String, Object>>>() {});
            if (members == null || members.isEmpty())
            {
                return register;
            }
            boolean changed = false;
            long now = System.currentTimeMillis();
            for (Map<String, Object> member : members)
            {
                if (member == null)
                {
                    continue;
                }
                String status = String.valueOf(member.getOrDefault("status", "")).toLowerCase();
                if (!"pending".equals(status))
                {
                    continue;
                }
                String type = String.valueOf(member.getOrDefault("type", "invite")).toLowerCase();
                if ("apply".equals(type))
                {
                    member.put("status", "rejected");
                    member.put("decisionTime", now);
                    changed = true;
                }
                else
                {
                    member.put("status", "expired");
                    member.put("decisionTime", now);
                    changed = true;
                }
            }
            if (!changed)
            {
                return register;
            }
            String newJson = mapper.writeValueAsString(members);
            competitionRegisterMapper.updateTeamMembersByTeamId(register.getTeamId(), newJson);
            CompetitionRegister refreshed = getRegisterByCompetitionUserTeamCached(
                    register.getCompetitionId(),
                    register.getUserId(),
                    register.getTeamId(),
                    () -> competitionRegisterMapper.selectByCompetitionAndUserAndTeam(
                            register.getCompetitionId(), register.getUserId(), register.getTeamId()));
            if (refreshed != null)
            {
                return refreshed;
            }
            CompetitionRegister teamRegister = getRegisterByCompetitionTeamCached(
                    register.getCompetitionId(),
                    register.getTeamId(),
                    () -> competitionRegisterMapper.selectByCompetitionAndTeam(
                            register.getCompetitionId(), register.getTeamId()));
            if (teamRegister != null)
            {
                return teamRegister;
            }
            register.setTeamMembers(newJson);
        }
        catch (Exception ignore)
        {
        }
        return register;
    }

    private boolean isSubmittedRegistration(CompetitionRegister register)
    {
        if (register == null)
        {
            return false;
        }
        CompetitionRegister displayRegister = resolveDisplayRegister(register);
        Long displayRegisterId = displayRegister == null ? null : displayRegister.getRegisterId();
        if (displayRegisterId == null)
        {
            return false;
        }
        CompetitionParticipation participation = getParticipationCached(
                displayRegisterId,
                () -> competitionParticipationMapper.selectById(displayRegisterId));
        return participation != null
                && participation.getSubmitTime() != null
                && hasText(participation.getPptPath())
                && hasText(getDocumentPath(participation.getPdfPath()));
    }

    private boolean shouldDisplayRegistrationInProfile(CompetitionRegister register, Long userId)
    {
        if (register == null || userId == null)
        {
            return false;
        }
        if (!isUserStillMemberOfRegister(register, userId))
        {
            return false;
        }
        if (isSubmittedRegistration(register))
        {
            return true;
        }
        if (register.getTeamId() == null || StringUtils.isEmpty(register.getTeamMembers()))
        {
            return false;
        }
        return true;
    }

    private CompetitionRegister findActiveCompetitionTeamForStudent(Long competitionId, String studentNo, Long excludedTeamId)
    {
        if (competitionId == null || StringUtils.isEmpty(studentNo))
        {
            return null;
        }
        List<CompetitionRegister> registers = competitionRegisterMapper.selectByCompetitionId(competitionId);
        if (registers == null || registers.isEmpty())
        {
            return null;
        }
        Set<Long> visitedTeamIds = new LinkedHashSet<>();
        ObjectMapper mapper = new ObjectMapper();
        for (CompetitionRegister register : registers)
        {
            if (register == null || register.getTeamId() == null)
            {
                continue;
            }
            if (excludedTeamId != null && excludedTeamId.equals(register.getTeamId()))
            {
                continue;
            }
            if (!visitedTeamIds.add(register.getTeamId()))
            {
                continue;
            }
            String membersJson = register.getTeamMembers();
            if (StringUtils.isEmpty(membersJson))
            {
                continue;
            }
            try
            {
                List<Map<String, Object>> members = mapper.readValue(membersJson,
                        new TypeReference<List<Map<String, Object>>>() {});
                if (members == null)
                {
                    continue;
                }
                for (Map<String, Object> member : members)
                {
                    String memberNo = String.valueOf(member.getOrDefault("studentNo", ""));
                    String status = String.valueOf(member.getOrDefault("status", ""));
                    if (studentNo.equals(memberNo) && isActiveMemberStatus(status))
                    {
                        return register;
                    }
                }
            }
            catch (Exception ignore)
            {
            }
        }
        return null;
    }

    private void ensureStudentHasSingleTeamSlot(Long competitionId, String studentNo, Long excludedTeamId)
    {
        // 允许一个学生参加同一赛事的多个队伍
    }

    private void ensureMembersUniqueAcrossCompetition(Long competitionId, Long teamId, String teamMembers)
    {
        if (competitionId == null || StringUtils.isEmpty(teamMembers))
        {
            return;
        }
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> members = mapper.readValue(teamMembers, new TypeReference<List<Map<String, Object>>>() {});
            if (members == null)
            {
                return;
            }
            for (Map<String, Object> member : members)
            {
                if (member == null)
                {
                    continue;
                }
                String studentNo = String.valueOf(member.getOrDefault("studentNo", ""));
                String status = String.valueOf(member.getOrDefault("status", ""));
                if (StringUtils.isEmpty(studentNo) || !isActiveMemberStatus(status))
                {
                    continue;
                }
                ensureStudentHasSingleTeamSlot(competitionId, studentNo, teamId);
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception ignore)
        {
        }
    }

    @Override
    public Map<String, Object> getUserCompetitionStatus(Long competitionId, Long userId)
    {
        return getUserCompetitionStatus(competitionId, userId, null);
    }

    @Override
    public Map<String, Object> getUserCompetitionStatus(Long competitionId, Long userId, Long teamId)
    {
        Map<String, Object> result = new HashMap<>();
        CompetitionRegister register = null;

        if (teamId != null) {
            register = competitionRegisterMapper.selectByCompetitionAndUserAndTeam(competitionId, userId, teamId);
        }

        if (register == null && teamId == null) {
            register = selectDefaultCompetitionRegister(competitionId, userId);
        }
        if (register == null)
        {
            result.put("registered", false);
            result.put("hasRecord", false);
            result.put("draft", false);
            result.put("complete", false);
            result.put("recordStatus", "");
            result.put("recordStatusText", "");
            result.put("materialSubmitted", false);
            result.put("submitTime", null);
            result.put("missingFields", new ArrayList<>());
            result.put("teacherList", new ArrayList<>());
            result.put("workName", "");
            result.put("workDescription", "");
            result.put("reviewStatusCode", "");
            result.put("reviewStatusText", "");
            result.put("teamMembers", "[]");
            result.put("isLeader", false);
            result.put("canCancel", false);
            result.put("displayRegisterId", null);
            return result;
        }

        register = cleanupPendingMembersForLockedTeam(register);
        CompetitionRegister displayRegister = resolveDisplayRegister(register);
        if (displayRegister == null)
        {
            displayRegister = register;
        }

        if (!isUserStillMemberOfRegister(displayRegister, userId)) {
            result.put("registered", false);
            result.put("hasRecord", false);
            result.put("draft", false);
            result.put("complete", false);
            result.put("recordStatus", "");
            result.put("recordStatusText", "");
            result.put("materialSubmitted", false);
            result.put("submitTime", null);
            result.put("missingFields", new ArrayList<>());
            result.put("teacherList", new ArrayList<>());
            result.put("workName", "");
            result.put("workDescription", "");
            result.put("reviewStatusCode", "");
            result.put("reviewStatusText", "");
            result.put("teamMembers", "[]");
            result.put("isLeader", false);
            result.put("canCancel", false);
            result.put("displayRegisterId", null);
            result.put("teamId", null);
            result.put("teamName", "");
            return result;
        }

        List<Map<String, Object>> teacherResponse = buildTeacherResponse(displayRegister.getTeamId());
        CompetitionParticipation participation = competitionParticipationMapper.selectById(displayRegister.getRegisterId());
        boolean hasSubmittedMaterials = participation != null
                && hasText(participation.getPptPath())
                && hasText(getDocumentPath(participation.getPdfPath()));
        boolean legacySubmittedRecord = hasSubmittedMaterials
                && !hasText(displayRegister.getWorkName())
                && !hasText(displayRegister.getWorkDescription());

        result.put("hasRecord", true);
        result.put("registerId", register.getRegisterId());
        result.put("displayRegisterId", displayRegister.getRegisterId());
        result.put("teamId", displayRegister.getTeamId());
        result.put("teamName", displayRegister.getTeamName());
        result.put("registerTime", register.getRegisterTime());
        result.put("teacherList", teacherResponse);
        result.put("workName", legacySubmittedRecord ? "无" : defaultString(displayRegister.getWorkName()));
        result.put("workDescription", legacySubmittedRecord ? "无" : defaultString(displayRegister.getWorkDescription()));
        result.put("materialSubmitted", hasSubmittedMaterials);
        result.put("submitTime", participation == null ? null : participation.getSubmitTime());
        Map<String, String> reviewStatus = resolveStudentReviewStatus(displayRegister.getRegisterId());
        result.put("reviewStatusCode", reviewStatus.get("code"));
        result.put("reviewStatusText", reviewStatus.get("text"));

        List<Map<String, Object>> visibleMembers = new ArrayList<>();
        boolean isLeader = isUserLeaderForRegister(register, userId);
        String membersJson = displayRegister.getTeamMembers();
        if (membersJson != null)
        {
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> members = mapper.readValue(membersJson, new TypeReference<List<Map<String, Object>>>() {});
                if (members != null)
                {
                    SysUser user = sysUserMapper.selectUserById(userId);
                    String myNo = null;
                    if (user != null)
                    {
                        myNo = user.getStudentNo() != null ? user.getStudentNo() : user.getUserName();
                    }
                    for (Map<String, Object> m : members)
                    {
                        String status = String.valueOf(m.getOrDefault("status", "")).toLowerCase();
                        // 保留已通过、待同意以及历史无状态的成员
                        if ("approved".equals(status) || "pending".equals(status) || "".equals(status))
                        {
                            String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                            // 仅当 team_members 字段为空时，才从 sys_user 补充，保留队长手动编辑值
                            SysUser u2 = null;
                            if (mNo != null && !mNo.isEmpty())
                            {
                                u2 = sysUserMapper.selectUserByStudentNo(mNo);
                                if (u2 == null)
                                {
                                    u2 = sysUserMapper.selectUserByUserName(mNo);
                                }
                            }
                            if (u2 != null)
                            {
                                // 仅在 team_members 中对应字段为空时，才从 sys_user 补充，
                                // 保留队长手动编辑的值，避免编辑后被 sys_user 旧值覆盖
                                String curName = m.get("name") == null ? "" : String.valueOf(m.get("name")).trim();
                                if (curName.isEmpty())
                                {
                                    String nn2 = null;
                                    if (u2.getStudentName() != null && !u2.getStudentName().isEmpty()) {
                                        nn2 = u2.getStudentName();
                                    } else if (u2.getNickName() != null && !u2.getNickName().isEmpty()) {
                                        nn2 = u2.getNickName();
                                    } else {
                                        String uname = u2.getUserName();
                                        if (uname != null && !uname.isEmpty() && (mNo == null || !uname.equals(mNo))) {
                                            nn2 = uname;
                                        }
                                    }
                                    if (nn2 != null) m.put("name", nn2);
                                }

                                if (m.get("college") == null || String.valueOf(m.get("college")).trim().isEmpty())
                                {
                                    if (u2.getCollegeName() != null) m.put("college", u2.getCollegeName());
                                }

                                if (m.get("major") == null || String.valueOf(m.get("major")).trim().isEmpty())
                                {
                                    if (u2.getMajorName() != null) m.put("major", u2.getMajorName());
                                }

                                if (m.get("phone") == null || String.valueOf(m.get("phone")).trim().isEmpty())
                                {
                                    if (u2.getPhonenumber() != null) m.put("phone", u2.getPhonenumber());
                                }

                                if (m.get("email") == null || String.valueOf(m.get("email")).trim().isEmpty())
                                {
                                    if (u2.getEmail() != null) m.put("email", u2.getEmail());
                                }
                            }
                            visibleMembers.add(m);
                        }
                    }
                }
                else
                {
                    visibleMembers = new ArrayList<>();
                }
            }
            catch (Exception e)
            {
                visibleMembers = new ArrayList<>();
            }
        }
        List<String> missingFields = buildRegistrationMissingFields(displayRegister, teacherResponse, visibleMembers, hasSubmittedMaterials,
                legacySubmittedRecord);
        boolean infoComplete = isRegistrationInfoComplete(displayRegister, teacherResponse, visibleMembers, legacySubmittedRecord);
        Map<String, String> recordStatus = buildRecordStatus(infoComplete, hasSubmittedMaterials, reviewStatus.get("code"));
        boolean draft = "draft".equals(recordStatus.get("code"));
        boolean complete = !draft;

        if (membersJson == null)
        {
            result.put("teamMembers", null);
        }
        else
        {
            try
            {
                result.put("teamMembers", new ObjectMapper().writeValueAsString(visibleMembers));
            }
            catch (Exception e)
            {
                result.put("teamMembers", membersJson);
            }
        }
        result.put("registered", !draft);
        result.put("draft", draft);
        result.put("complete", complete);
        result.put("recordStatus", recordStatus.get("code"));
        result.put("recordStatusText", recordStatus.get("text"));
        result.put("missingFields", missingFields);
        result.put("isLeader", isLeader);
        result.put("canCancel", isLeader && !isReviewLockedStatus(reviewStatus.get("code")));
        return result;
    }

    @Override
    public Map<Long, Map<String, Object>> getUserCompetitionStatusBatch(List<Long> competitionIds, Long userId)
    {
        Map<Long, Map<String, Object>> statusMap = new HashMap<>();
        if (competitionIds == null || competitionIds.isEmpty() || userId == null)
        {
            return statusMap;
        }
        // 一次 SQL 查出该用户在所有指定竞赛下的全部报名记录
        List<CompetitionRegister> registers = competitionRegisterMapper.selectListByUserAndCompetitionIds(userId, competitionIds);
        // 按 competitionId 分组，每组取最新一条
        Map<Long, CompetitionRegister> latestByCompetition = new HashMap<>();
        for (CompetitionRegister r : registers)
        {
            Long cid = r.getCompetitionId();
            if (cid == null)
            {
                continue;
            }
            // registers 已按 competition_id, register_time desc 排序
            // 第一次出现的就是该竞赛下最新的记录
            if (!latestByCompetition.containsKey(cid))
            {
                latestByCompetition.put(cid, r);
            }
        }
        // 为每个竞赛构建轻量状态结果
        for (Long cid : competitionIds)
        {
            CompetitionRegister register = latestByCompetition.get(cid);
            Map<String, Object> m = new HashMap<>();
            if (register != null)
            {
                // 有报名记录即视为已报名（与列表页原有行为一致：registered = !draft）
                m.put("registered", true);
                m.put("registerId", register.getRegisterId());
                m.put("teamId", register.getTeamId());
                m.put("teamName", register.getTeamName());
            }
            else
            {
                m.put("registered", false);
                m.put("registerId", null);
                m.put("teamId", null);
                m.put("teamName", null);
            }
            statusMap.put(cid, m);
        }
        return statusMap;
    }

    private String stripQueryString(String url) {
        if (url == null) return null;
        int idx = url.indexOf("?");
        if (idx != -1) {
            return url.substring(0, idx);
        }
        return url;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String getDocumentPath(String pdfPath) {
        if (!hasText(pdfPath)) {
            return "";
        }
        String[] parts = pdfPath.split("\\|", -1);
        return parts.length > 0 ? parts[0].trim() : "";
    }

    private String trimToNull(String value)
    {
        if (value == null)
        {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultString(String value)
    {
        return value == null ? "" : value;
    }

    private String joinTeacherNames(List<Map<String, Object>> teacherList)
    {
        if (teacherList == null || teacherList.isEmpty())
        {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (Map<String, Object> teacher : teacherList)
        {
            if (teacher == null)
            {
                continue;
            }
            Object rawName = teacher.get("name");
            String name = trimToNull(rawName == null ? null : String.valueOf(rawName));
            if (name != null)
            {
                names.add(name);
            }
        }
        return names.isEmpty() ? null : String.join("、", names);
    }

    private List<String> buildRegistrationMissingFields(CompetitionRegister register, List<Map<String, Object>> teacherList,
            List<Map<String, Object>> members, boolean hasSubmittedMaterials, boolean legacySubmittedRecord)
    {
        List<String> missingFields = new ArrayList<>();
        if (register == null || !hasText(register.getTeamName()))
        {
            missingFields.add("队伍名称");
        }
        if (teacherList == null || teacherList.isEmpty())
        {
            missingFields.add("指导老师");
        }
        if (!hasCompleteLeaderProfile(register == null ? null : register.getUserId()))
        {
            missingFields.add("队长信息");
        }
        if (!hasAnyCompleteNonLeaderMember(members))
        {
            missingFields.add("至少一位队员");
        }
        if (!legacySubmittedRecord && !hasText(register == null ? null : register.getWorkName()))
        {
            missingFields.add("作品名称");
        }
        if (!legacySubmittedRecord && !hasText(register == null ? null : register.getWorkDescription()))
        {
            missingFields.add("作品简介");
        }
        if (!hasSubmittedMaterials)
        {
            missingFields.add("申报书资料/PPT演示资料");
        }
        if (hasPendingMembers(members))
        {
            missingFields.add("待队员确认");
        }
        return missingFields;
    }

    private boolean isRegistrationInfoComplete(CompetitionRegister register, List<Map<String, Object>> teacherList,
            List<Map<String, Object>> members, boolean legacySubmittedRecord)
    {
        return hasBaseRegistrationInfo(register, teacherList, members, legacySubmittedRecord)
                && hasApprovedNonLeaderMember(members)
                && !hasPendingMembers(members);
    }

    private List<Map<String, Object>> resolveVisibleTeamMembers(CompetitionRegister displayRegister, Long userId)
    {
        List<Map<String, Object>> visibleMembers = new ArrayList<>();
        if (displayRegister == null)
        {
            return visibleMembers;
        }
        String membersJson = displayRegister.getTeamMembers();
        if (membersJson == null)
        {
            return visibleMembers;
        }
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> members = mapper.readValue(membersJson, new TypeReference<List<Map<String, Object>>>() {});
            if (members == null)
            {
                return visibleMembers;
            }
            SysUser currentUser = getBaseUserByIdCached(userId, () -> sysUserMapper.selectBaseUserById(userId));
            String currentStudentNo = currentUser == null ? null
                    : (currentUser.getStudentNo() != null ? currentUser.getStudentNo() : currentUser.getUserName());
            for (Map<String, Object> member : members)
            {
                if (member == null)
                {
                    continue;
                }
                String status = String.valueOf(member.getOrDefault("status", "")).toLowerCase();
                if (!"approved".equals(status) && !"pending".equals(status) && !"".equals(status))
                {
                    continue;
                }
                String studentNo = String.valueOf(member.getOrDefault("studentNo", ""));
                SysUser boundUser = null;
                if (studentNo != null && !studentNo.isEmpty())
                {
                    boundUser = getBaseUserByStudentNoCached(studentNo, () -> sysUserMapper.selectBaseUserByStudentNo(studentNo));
                    if (boundUser == null)
                    {
                        boundUser = getBaseUserByUserNameCached(studentNo, () -> sysUserMapper.selectBaseUserByUserName(studentNo));
                    }
                }
                if (boundUser != null)
                {
                    String curName = member.get("name") == null ? "" : String.valueOf(member.get("name")).trim();
                    if (curName.isEmpty())
                    {
                        if (boundUser.getStudentName() != null && !boundUser.getStudentName().isEmpty())
                        {
                            member.put("name", boundUser.getStudentName());
                        }
                        else if (boundUser.getNickName() != null && !boundUser.getNickName().isEmpty())
                        {
                            member.put("name", boundUser.getNickName());
                        }
                    }
                    if (member.get("college") == null || String.valueOf(member.get("college")).trim().isEmpty())
                    {
                        if (boundUser.getCollegeName() != null) member.put("college", boundUser.getCollegeName());
                    }
                    if (member.get("major") == null || String.valueOf(member.get("major")).trim().isEmpty())
                    {
                        if (boundUser.getMajorName() != null) member.put("major", boundUser.getMajorName());
                    }
                    if (member.get("phone") == null || String.valueOf(member.get("phone")).trim().isEmpty())
                    {
                        if (boundUser.getPhonenumber() != null) member.put("phone", boundUser.getPhonenumber());
                    }
                    if (member.get("email") == null || String.valueOf(member.get("email")).trim().isEmpty())
                    {
                        if (boundUser.getEmail() != null) member.put("email", boundUser.getEmail());
                    }
                }
                if (currentStudentNo != null && currentStudentNo.equals(studentNo))
                {
                    member.put("isCurrentUser", true);
                }
                visibleMembers.add(member);
            }
        }
        catch (Exception ignore)
        {
            return new ArrayList<>();
        }
        return visibleMembers;
    }

    private boolean hasBaseRegistrationInfo(CompetitionRegister register, List<Map<String, Object>> teacherList,
            List<Map<String, Object>> members, boolean legacySubmittedRecord)
    {
        return register != null
                && hasText(register.getTeamName())
                && teacherList != null
                && !teacherList.isEmpty()
                && hasCompleteLeaderProfile(register.getUserId())
                && hasAnyCompleteNonLeaderMember(members)
                && (legacySubmittedRecord || hasText(register.getWorkName()))
                && (legacySubmittedRecord || hasText(register.getWorkDescription()));
    }

    private boolean hasCompleteLeaderProfile(Long userId)
    {
        if (userId == null)
        {
            return false;
        }
        SysUser user = getBaseUserByIdCached(userId, () -> sysUserMapper.selectBaseUserById(userId));
        return isCompleteStudentProfile(user);
    }

    private boolean isCompleteStudentProfile(SysUser user)
    {
        if (user == null)
        {
            return false;
        }
        return hasText(firstNonEmptyValue(user.getStudentName(), user.getNickName(), user.getUserName()))
                && hasText(firstNonEmptyValue(user.getStudentNo(), user.getUserName()))
                && hasText(firstNonEmptyValue(user.getCollegeName()))
                && hasText(firstNonEmptyValue(user.getMajorName()))
                && hasText(firstNonEmptyValue(user.getPhonenumber()))
                && hasText(firstNonEmptyValue(user.getEmail()));
    }

    private boolean hasCompleteLeader(List<Map<String, Object>> members)
    {
        if (members == null || members.isEmpty())
        {
            return false;
        }
        Map<String, Object> leader = null;
        for (Map<String, Object> member : members)
        {
            if ("leader".equalsIgnoreCase(String.valueOf(member.getOrDefault("role", ""))))
            {
                leader = member;
                break;
            }
        }
        if (leader == null)
        {
            leader = members.get(0);
        }
        return isCompleteStudentMember(leader);
    }

    private boolean hasAnyCompleteNonLeaderMember(List<Map<String, Object>> members)
    {
        if (members == null || members.isEmpty())
        {
            return false;
        }
        for (Map<String, Object> member : members)
        {
            if ("rejected".equalsIgnoreCase(String.valueOf(member.getOrDefault("status", ""))))
            {
                continue;
            }
            if ("leader".equalsIgnoreCase(String.valueOf(member.getOrDefault("role", ""))))
            {
                continue;
            }
            if (isCompleteStudentMember(member))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasApprovedNonLeaderMember(List<Map<String, Object>> members)
    {
        if (members == null || members.isEmpty())
        {
            return false;
        }
        for (Map<String, Object> member : members)
        {
            if (member == null)
            {
                continue;
            }
            String role = String.valueOf(member.getOrDefault("role", "")).toLowerCase();
            String status = String.valueOf(member.getOrDefault("status", "")).toLowerCase();
            if ("leader".equals(role))
            {
                continue;
            }
            if (("approved".equals(status) || "".equals(status)) && isCompleteStudentMember(member))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasPendingMembers(List<Map<String, Object>> members)
    {
        if (members == null || members.isEmpty())
        {
            return false;
        }
        for (Map<String, Object> member : members)
        {
            if (member == null)
            {
                continue;
            }
            String status = String.valueOf(member.getOrDefault("status", "")).toLowerCase();
            if ("pending".equals(status))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isCompleteStudentMember(Map<String, Object> member)
    {
        if (member == null)
        {
            return false;
        }
        return hasText(memberText(member, "name"))
                && hasText(memberText(member, "studentNo"))
                && hasText(memberText(member, "college"))
                && hasText(memberText(member, "major"))
                && hasText(memberText(member, "phone"))
                && hasText(memberText(member, "email"));
    }

    private String memberText(Map<String, Object> member, String key)
    {
        if (member == null || key == null)
        {
            return "";
        }
        Object value = member.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private void preloadRegistrationPageContext(List<Map<String, Object>> raw)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || raw == null || raw.isEmpty())
        {
            return;
        }

        Set<Long> registerIds = new LinkedHashSet<>();
        for (Map<String, Object> item : raw)
        {
            Long registerId = parseLong(item == null ? null : item.get("registerId"));
            if (registerId != null)
            {
                registerIds.add(registerId);
            }
        }
        if (registerIds.isEmpty())
        {
            return;
        }

        List<CompetitionRegister> registers = competitionRegisterMapper.selectByIds(new ArrayList<>(registerIds));
        if (registers != null)
        {
            for (CompetitionRegister register : registers)
            {
                if (register != null && register.getRegisterId() != null)
                {
                    context.registerByIdCache.put(register.getRegisterId(), register);
                }
            }
        }

        Set<Long> teamIds = new LinkedHashSet<>();
        Set<Long> participationIds = new LinkedHashSet<>();
        for (CompetitionRegister register : context.registerByIdCache.values())
        {
            if (register == null)
            {
                continue;
            }
            if (register.getTeamId() != null)
            {
                teamIds.add(register.getTeamId());
            }
            if (register.getRegisterId() != null)
            {
                participationIds.add(register.getRegisterId());
            }
        }

        for (Long teamId : teamIds)
        {
            List<CompetitionRegister> teamRegisters = competitionRegisterMapper.selectByTeamId(teamId);
            context.teamRegistersCache.put(teamId, teamRegisters == null ? Collections.emptyList() : teamRegisters);
        }

        List<CompetitionParticipation> participations = competitionParticipationMapper.selectByIds(new ArrayList<>(participationIds));
        if (participations != null)
        {
            for (CompetitionParticipation participation : participations)
            {
                if (participation != null && participation.getParticipationId() != null)
                {
                    context.participationCache.put(participation.getParticipationId(), participation);
                }
            }
        }

        List<org.iflytek.system.domain.CompetitionReviewAssignment> assignments =
                competitionReviewAssignmentMapper.selectByParticipationIds(new ArrayList<>(participationIds));
        if (assignments != null)
        {
            for (org.iflytek.system.domain.CompetitionReviewAssignment assignment : assignments)
            {
                if (assignment == null || assignment.getParticipationId() == null)
                {
                    continue;
                }
                context.reviewAssignmentsCache
                        .computeIfAbsent(assignment.getParticipationId(), key -> new ArrayList<>())
                        .add(assignment);
            }
        }

        List<TeacherTeamRel> relations = teamIds.isEmpty()
                ? Collections.emptyList()
                : teacherTeamRelMapper.selectByTeamIds(new ArrayList<>(teamIds));
        Map<Long, List<TeacherTeamRel>> relationsByTeamId = new HashMap<>();
        if (relations != null)
        {
            for (TeacherTeamRel relation : relations)
            {
                if (relation == null || relation.getTeamId() == null)
                {
                    continue;
                }
                relationsByTeamId.computeIfAbsent(relation.getTeamId(), key -> new ArrayList<>()).add(relation);
            }
        }
        for (Long teamId : teamIds)
        {
            context.teacherRelationsCache.put(teamId, relationsByTeamId.getOrDefault(teamId, Collections.emptyList()));
        }
    }

    private Long parseLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.valueOf(String.valueOf(value));
        }
        catch (Exception ignore)
        {
            return null;
        }
    }

    private List<Map<String, Object>> buildStudentReviewerItems(
            List<org.iflytek.system.domain.CompetitionReviewAssignment> assignments,
            List<CompetitionScore> scores)
    {
        List<Map<String, Object>> items = new ArrayList<>();
        Map<Long, CompetitionScore> scoreByReviewerId = new HashMap<>();
        if (scores != null)
        {
            for (CompetitionScore score : scores)
            {
                if (score != null && score.getReviewerId() != null)
                {
                    scoreByReviewerId.put(score.getReviewerId(), score);
                }
            }
        }
        if (assignments == null)
        {
            return items;
        }
        for (org.iflytek.system.domain.CompetitionReviewAssignment assignment : assignments)
        {
            if (assignment == null)
            {
                continue;
            }
            CompetitionScore score = scoreByReviewerId.get(assignment.getReviewerId());
            boolean reviewed = "REVIEWED".equalsIgnoreCase(String.valueOf(assignment.getAssignmentStatus())) || score != null;
            Map<String, Object> item = new HashMap<>();
            item.put("assignmentId", assignment.getId());
            item.put("reviewerId", assignment.getReviewerId());
            item.put("reviewerName", defaultString(assignment.getReviewerName()));
            item.put("assignmentStatus", assignment.getAssignmentStatus());
            item.put("reviewed", reviewed);
            item.put("reviewStatusText", reviewed ? "已评审" : "未评审");
            item.put("score", score == null ? null : score.getScore());
            item.put("comment", score == null ? "" : defaultString(score.getComment()));
            item.put("scoreTime", score == null ? null : score.getScoreTime());
            items.add(item);
        }
        items.sort((left, right) -> String.valueOf(left.get("reviewerName")).compareTo(String.valueOf(right.get("reviewerName"))));
        return items;
    }

    private Map<String, String> resolveStudentReviewStatus(Long registerId)
    {
        Map<String, String> status = new HashMap<>();
        status.put("code", "");
        status.put("text", "");
        if (registerId == null)
        {
            return status;
        }
        CompetitionParticipation participation = getParticipationCached(
                registerId,
                () -> competitionParticipationMapper.selectById(registerId));
        if (participation == null || participation.getSubmitTime() == null)
        {
            return status;
        }

        List<org.iflytek.system.domain.CompetitionReviewAssignment> assignments =
                getReviewAssignmentsCached(
                        registerId,
                        () -> competitionReviewAssignmentMapper.selectByParticipationId(registerId));
        if (assignments == null || assignments.isEmpty())
        {
            status.put("code", "pending_review");
            status.put("text", "待审核");
            return status;
        }

        boolean allReviewed = true;
        for (org.iflytek.system.domain.CompetitionReviewAssignment assignment : assignments)
        {
            if (assignment == null || !"REVIEWED".equalsIgnoreCase(String.valueOf(assignment.getAssignmentStatus())))
            {
                allReviewed = false;
                break;
            }
        }
        if (allReviewed)
        {
            status.put("code", "reviewed");
            status.put("text", "已评审");
        }
        else
        {
            status.put("code", "in_review");
            status.put("text", "评审中");
        }
        return status;
    }

    private Map<String, String> buildRecordStatus(boolean infoComplete, boolean hasSubmittedMaterials, String reviewStatusCode)
    {
        Map<String, String> status = new HashMap<>();
        String normalizedReviewStatus = reviewStatusCode == null ? "" : reviewStatusCode.trim().toLowerCase();
        if ("reviewed".equals(normalizedReviewStatus))
        {
            status.put("code", "reviewed");
            status.put("text", "已评审");
            return status;
        }
        if ("in_review".equals(normalizedReviewStatus))
        {
            status.put("code", "in_review");
            status.put("text", "评审中");
            return status;
        }
        if (hasSubmittedMaterials || "pending_review".equals(normalizedReviewStatus))
        {
            status.put("code", "submitted");
            status.put("text", "待审核");
            return status;
        }
        if (infoComplete)
        {
            status.put("code", "ready");
            status.put("text", "可提交资料");
            return status;
        }
        status.put("code", "draft");
        status.put("text", "待完善信息");
        return status;
    }

    private BigDecimal calculateAverageScore(List<CompetitionScore> scores)
    {
        if (scores == null || scores.isEmpty())
        {
            return null;
        }
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (CompetitionScore score : scores)
        {
            if (score == null || score.getScore() == null)
            {
                continue;
            }
            total = total.add(score.getScore());
            count++;
        }
        if (count == 0)
        {
            return null;
        }
        return total.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public void submitMaterials(Long registerId, String pptPath, String pdfPath, String fileNames, String workName,
            String workDescription, Long userId)
    {
        if (!hasText(pptPath) || !hasText(getDocumentPath(pdfPath)))
        {
            throw new ServiceException("申报书资料和PPT演示资料均为必填项");
        }
        if (!hasText(workName))
        {
            throw new ServiceException("请输入作品名称");
        }
        if (!hasText(workDescription))
        {
            throw new ServiceException("请输入作品简介");
        }
        CompetitionRegister register = competitionRegisterMapper.selectById(registerId);
        if (register == null)
        {
            throw new ServiceException("报名记录不存在");
        }
        ensureTeamEditable(register, userId, true);
        List<Map<String, Object>> members = parseTeamMembers(register.getTeamMembers());
        if (hasPendingMembers(members))
        {
            throw new ServiceException("请等待所有队员确认后再提交资料");
        }
        if (!hasApprovedNonLeaderMember(members))
        {
            throw new ServiceException("请至少保证一位队员确认加入后再提交资料");
        }
        CompetitionParticipation participation = competitionParticipationMapper.selectById(registerId);
        if (participation == null)
        {
            participation = new CompetitionParticipation();
            participation.setParticipationId(registerId);
            participation.setParticipationStatus("\u672a\u63d0\u4ea4");
            competitionParticipationMapper.insertCompetitionParticipation(participation);
        }

        if (pptPath != null) pptPath = stripQueryString(pptPath);
        if (pdfPath != null) {
            if (pdfPath.contains("|")) {
                String[] parts = pdfPath.split("\\|");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) sb.append("|");
                    sb.append(stripQueryString(parts[i]));
                }
                pdfPath = sb.toString();
            } else {
                pdfPath = stripQueryString(pdfPath);
            }
        }
        
        participation.setPptPath(pptPath);
        participation.setPdfPath(pdfPath);
        participation.setSubmitTime(new Date());
        participation.setParticipationStatus("\u5df2\u63d0\u4ea4");

        competitionParticipationMapper.updateCompetitionParticipation(participation);
        register.setWorkName(workName.trim());
        register.setWorkDescription(workDescription.trim());
        competitionRegisterMapper.updateCompetitionRegister(register);
        if (register.getTeamId() != null)
        {
            competitionRegisterMapper.updateTeamProfileByTeamId(register.getTeamId(), register.getWorkName(),
                    register.getWorkDescription(), register.getTeacherName());
        }

        competitionWorkMapper.deleteCompetitionWorkByParticipationId(registerId);

        Map<String, String> namesMap = new HashMap<>();
        if (fileNames != null && !fileNames.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                namesMap = mapper.readValue(fileNames, new TypeReference<Map<String, String>>(){});
            } catch (Exception ignore) {}
        }

        if (pptPath != null && !pptPath.isEmpty()) {
            CompetitionWork work = new CompetitionWork();
            work.setParticipationId(registerId);
            String name = namesMap.get("ppt");
            if (name == null || name.isEmpty()) {
                name = FileUtils.getName(pptPath);
            }
            work.setFileName(name);
            work.setFilePath(pptPath);
            work.setFileType("ppt");
            work.setCreateTime(new Date());
            competitionWorkMapper.insertCompetitionWork(work);
        }

        if (pdfPath != null && !pdfPath.isEmpty()) {
            String[] parts = pdfPath.split("\\|");
            
            for (String path : parts) {
                if (path == null || path.isEmpty()) continue;
                CompetitionWork work = new CompetitionWork();
                work.setParticipationId(registerId);
                work.setFilePath(path);
                
                String lower = path.toLowerCase();
                String name = "";
                if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) {
                    work.setFileType("zip");
                    name = namesMap.get("other");
                } else {
                    work.setFileType("pdf"); 
                    name = namesMap.get("doc");
                }
                
                if (name == null || name.isEmpty()) {
                    name = FileUtils.getName(path);
                }
                work.setFileName(name);
                work.setCreateTime(new Date());
                competitionWorkMapper.insertCompetitionWork(work);
            }
        }
    }

    @Override
    public Map<String, Object> getUploadedMaterials(Long registerId)
    {
        CompetitionParticipation participation = competitionParticipationMapper.selectById(registerId);
        Map<String, Object> result = new HashMap<>();
        if (participation != null)
        {
            CompetitionWork query = new CompetitionWork();
            query.setParticipationId(registerId);
            List<CompetitionWork> works = competitionWorkMapper.selectCompetitionWorkList(query);
            
            String pptPath = null;
            String pdfPath = null;
            String pptName = null;
            String docName = null;
            String otherName = null;
            String docPath = null;
            String zipPath = null;
            
            if (works != null && !works.isEmpty()) {
                 for (CompetitionWork work : works) {
                     if ("ppt".equals(work.getFileType())) {
                         pptPath = work.getFilePath();
                         pptName = work.getFileName();
                     } else if ("zip".equals(work.getFileType())) {
                         zipPath = work.getFilePath();
                         otherName = work.getFileName();
                     } else {
                         docPath = work.getFilePath();
                         docName = work.getFileName();
                     }
                 }
                 
                 if (docPath != null) {
                     pdfPath = docPath;
                 }
                 if (zipPath != null) {
                     if (pdfPath != null) pdfPath += "|" + zipPath;
                     else pdfPath = "|" + zipPath;
                 }
            } else {
                pptPath = participation.getPptPath();
                pdfPath = participation.getPdfPath();
            }

            if (pptPath != null && !pptPath.isEmpty()) {
                pptPath = minioUtils.getPrivateUrl(pptPath);
            }
            if (pdfPath != null && !pdfPath.isEmpty()) {
                if (pdfPath.contains("|")) {
                    String[] parts = pdfPath.split("\\|");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length; i++) {
                        if (i > 0) sb.append("|");
                        sb.append(minioUtils.getPrivateUrl(parts[i]));
                    }
                    pdfPath = sb.toString();
                } else {
                    if (pdfPath.startsWith("|")) {
                         String p = pdfPath.substring(1);
                         pdfPath = "|" + minioUtils.getPrivateUrl(p);
                    } else {
                         pdfPath = minioUtils.getPrivateUrl(pdfPath);
                    }
                }
            }

            result.put("pptPath", pptPath);
            result.put("pdfPath", pdfPath);
            result.put("pptName", pptName);
            String finalDocName = docName == null ? "" : docName;
            String finalOtherName = otherName == null ? "" : otherName;
            if (docName != null || otherName != null) {
                result.put("pdfNames", finalDocName + "|" + finalOtherName);
            } else {
                result.put("pdfNames", "");
            }
            
            result.put("submitTime", participation.getSubmitTime());
            result.put("participationStatus", participation.getParticipationStatus());
        }
        else
        {
            result.put("pptPath", null);
            result.put("pdfPath", null);
            result.put("pptName", null);
            result.put("pdfNames", null);
            result.put("submitTime", null);
            result.put("participationStatus", null);
        }
        return result;
    }

    @Override
    public Map<String, Object> getStudentWorkDetail(Long registerId, Long userId)
    {
        CompetitionRegister ownRegister = competitionRegisterMapper.selectById(registerId);
        if (ownRegister == null)
        {
            throw new ServiceException("报名记录不存在");
        }

        ownRegister = cleanupPendingMembersForLockedTeam(ownRegister);
        if (!shouldDisplayRegistrationInProfile(ownRegister, userId))
        {
            throw new ServiceException("无权查看该作品详情");
        }

        CompetitionRegister displayRegister = resolveDisplayRegister(ownRegister);
        if (displayRegister == null)
        {
            displayRegister = ownRegister;
        }

        Competition competition = competitionMapper.selectCompetitionById(displayRegister.getCompetitionId());
        CompetitionParticipation participation = competitionParticipationMapper.selectById(displayRegister.getRegisterId());
        Map<String, Object> materials = getUploadedMaterials(displayRegister.getRegisterId());
        List<Map<String, Object>> teacherList = buildTeacherResponse(displayRegister.getTeamId());
        List<Map<String, Object>> teamMembers = resolveVisibleTeamMembers(displayRegister, userId);
        Map<String, String> reviewStatus = resolveStudentReviewStatus(displayRegister.getRegisterId());
        List<org.iflytek.system.domain.CompetitionReviewAssignment> assignments =
                competitionReviewAssignmentMapper.selectByParticipationId(displayRegister.getRegisterId());
        List<CompetitionScore> scores = competitionScoreMapper.selectByParticipationId(displayRegister.getRegisterId());
        List<Map<String, Object>> reviewerItems = buildStudentReviewerItems(assignments, scores);

        boolean hasSubmittedMaterials = participation != null
                && participation.getSubmitTime() != null
                && hasText(participation.getPptPath())
                && hasText(getDocumentPath(participation.getPdfPath()));
        boolean legacySubmittedRecord = hasSubmittedMaterials
                && !hasText(displayRegister.getWorkName())
                && !hasText(displayRegister.getWorkDescription());
        boolean infoComplete = isRegistrationInfoComplete(displayRegister, teacherList, teamMembers, legacySubmittedRecord);
        Map<String, String> recordStatus = buildRecordStatus(infoComplete, hasSubmittedMaterials, reviewStatus.get("code"));

        Map<String, Object> result = new HashMap<>();
        result.put("registerId", ownRegister.getRegisterId());
        result.put("displayRegisterId", displayRegister.getRegisterId());
        result.put("competitionId", displayRegister.getCompetitionId());
        result.put("competitionName", competition == null ? "" : defaultString(competition.getCompetitionName()));
        result.put("competitionType", competition == null ? "" : defaultString(competition.getCompetitionType()));
        result.put("teamId", displayRegister.getTeamId());
        result.put("teamName", defaultString(displayRegister.getTeamName()));
        result.put("registerTime", ownRegister.getRegisterTime());
        result.put("teacherList", teacherList);
        result.put("teamMembers", teamMembers);
        result.put("workName", legacySubmittedRecord ? "无" : defaultString(displayRegister.getWorkName()));
        result.put("workDescription", legacySubmittedRecord ? "无" : defaultString(displayRegister.getWorkDescription()));
        result.put("reviewStatusCode", reviewStatus.get("code"));
        result.put("reviewStatusText", reviewStatus.get("text"));
        result.put("recordStatus", recordStatus.get("code"));
        result.put("recordStatusText", recordStatus.get("text"));
        result.put("submissionStatus", participation != null && participation.getSubmitTime() != null ? "已提交" : "未提交");
        result.put("submitTime", participation == null ? null : participation.getSubmitTime());
        result.put("pptPath", materials.get("pptPath"));
        result.put("pdfPath", materials.get("pdfPath"));
        result.put("pptName", materials.get("pptName"));
        result.put("pdfNames", materials.get("pdfNames"));
        result.put("reviewers", reviewerItems);
        result.put("reviewerCount", reviewerItems.size());
        result.put("reviewedCount", reviewerItems.stream().filter(item -> Boolean.TRUE.equals(item.get("reviewed"))).count());
        result.put("finalScore", calculateAverageScore(scores));
        result.put("hasMaterials", hasSubmittedMaterials);
        return result;
    }

    @Override
    public Map<String, Object> getRegistrationPage(Long userId, Integer pageNum, Integer pageSize)
    {
        long requestStart = System.currentTimeMillis();
        int safePageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int safePageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        String studentNo = getStudentNoByUserId(userId);
        long countStart = System.currentTimeMillis();
        long total = competitionRegisterMapper.countRegistrationsByUser(userId, studentNo);
        long countCost = System.currentTimeMillis() - countStart;

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("rows", new ArrayList<>());
        if (total <= 0)
        {
            log.info("registered page timing userId={} pageNum={} pageSize={} countMs={} total=0 totalMs={}",
                    userId, safePageNum, safePageSize, countCost, System.currentTimeMillis() - requestStart);
            return result;
        }

        RECENT_REGISTRATIONS_CONTEXT.set(new RecentRegistrationsContext());
        try
        {
            int offset = (safePageNum - 1) * safePageSize;
            long rawStart = System.currentTimeMillis();
            List<Map<String, Object>> raw = competitionRegisterMapper.selectRegistrationsByUserPage(
                    userId,
                    studentNo,
                    offset,
                    safePageSize
            );
            long rawCost = System.currentTimeMillis() - rawStart;
            List<Map<String, Object>> pageRows = new ArrayList<>();
            long preloadCost = 0L;
            long enrichCost = 0L;
            if (raw != null)
            {
                long preloadStart = System.currentTimeMillis();
                preloadRegistrationPageContext(raw);
                preloadCost = System.currentTimeMillis() - preloadStart;
                long enrichStart = System.currentTimeMillis();
                for (Map<String, Object> item : raw)
                {
                    Map<String, Object> enriched = buildVisibleRegistrationItem(item, userId);
                    if (enriched != null)
                    {
                        pageRows.add(enriched);
                    }
                }
                enrichCost = System.currentTimeMillis() - enrichStart;
            }

            result.put("rows", pageRows);
            log.info(
                    "registered page timing userId={} pageNum={} pageSize={} total={} countMs={} rawMs={} preloadMs={} enrichMs={} rowCount={} totalMs={}",
                    userId, safePageNum, safePageSize, total, countCost, rawCost, preloadCost, enrichCost,
                    pageRows.size(), System.currentTimeMillis() - requestStart);
            return result;
        }
        finally
        {
            RECENT_REGISTRATIONS_CONTEXT.remove();
        }
    }

    @Override
    public List<Map<String, Object>> getRecentRegistrations(Long userId, Integer limit)
    {
        RECENT_REGISTRATIONS_CONTEXT.set(new RecentRegistrationsContext());
        try
        {
            List<Map<String, Object>> raw = (limit == null || limit <= 0)
                    ? competitionRegisterMapper.selectAllRegistrationsByUser(userId)
                    : competitionRegisterMapper.selectRecentRegistrationsByUser(userId, limit);
            if (raw == null || raw.isEmpty())
            {
                return raw;
            }
            List<Map<String, Object>> filtered = new ArrayList<>();
            for (Map<String, Object> item : raw)
            {
                Map<String, Object> enriched = buildVisibleRegistrationItem(item, userId);
                if (enriched != null)
                {
                    filtered.add(enriched);
                }
            }
            return filtered;
        }
        finally
        {
            RECENT_REGISTRATIONS_CONTEXT.remove();
        }
    }

    private Map<String, Object> buildVisibleRegistrationItem(Map<String, Object> item, Long userId)
    {
        if (item == null)
        {
            return null;
        }

        Long registerId = null;
        try
        {
            Object registerIdObj = item.get("registerId");
            if (registerIdObj != null)
            {
                registerId = Long.valueOf(String.valueOf(registerIdObj));
            }
        }
        catch (Exception ignore)
        {
            registerId = null;
        }

        final Long finalRegisterId = registerId;
        CompetitionRegister ownRegister = finalRegisterId == null ? null
                : getRegisterByIdCached(finalRegisterId, () -> competitionRegisterMapper.selectById(finalRegisterId));
        ownRegister = cleanupPendingMembersForLockedTeam(ownRegister);
        if (!shouldDisplayRegistrationInProfile(ownRegister, userId))
        {
            return null;
        }

        CompetitionRegister displayRegister = resolveDisplayRegister(ownRegister);
        Long displayRegisterId = displayRegister == null ? registerId : displayRegister.getRegisterId();
        Map<String, String> reviewStatus = resolveStudentReviewStatus(displayRegisterId);
        List<Map<String, Object>> visibleMembers = parseTeamMembers(displayRegister == null ? null : displayRegister.getTeamMembers());
        List<Map<String, Object>> teacherResponse = buildTeacherResponse(displayRegister == null ? null : displayRegister.getTeamId());
        boolean hasSubmittedMaterials = isSubmittedRegistration(ownRegister);
        boolean legacySubmittedRecord = hasSubmittedMaterials
                && displayRegister != null
                && !hasText(displayRegister.getWorkName())
                && !hasText(displayRegister.getWorkDescription());
        boolean infoComplete = isRegistrationInfoComplete(displayRegister, teacherResponse, visibleMembers, legacySubmittedRecord);
        Map<String, String> recordStatus = buildRecordStatus(infoComplete, hasSubmittedMaterials, reviewStatus.get("code"));

        Map<String, Object> enriched = new HashMap<>(item);
        enriched.put("reviewStatus", reviewStatus.get("code"));
        enriched.put("reviewStatusCode", reviewStatus.get("code"));
        enriched.put("reviewStatusText", reviewStatus.get("text"));
        enriched.put("status", reviewStatus.get("code"));
        enriched.put("recordStatus", recordStatus.get("code"));
        enriched.put("recordStatusText", recordStatus.get("text"));
        enriched.put("statusText", recordStatus.get("text"));
        enriched.put("displayRegisterId", displayRegisterId);
        boolean isLeader = isUserLeaderForRegister(ownRegister, userId);
        enriched.put("isLeader", isLeader);
        enriched.put("canCancel", isLeader && !isReviewLockedStatus(reviewStatus.get("code")));
        if (displayRegister != null && hasText(displayRegister.getTeamName()))
        {
            enriched.put("teamName", displayRegister.getTeamName());
        }
        return enriched;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRegistration(Long registerId, Long userId)
    {
        CompetitionRegister register = competitionRegisterMapper.selectById(registerId);
        if (register == null)
        {
            throw new ServiceException("Registration record not found");
        }
        if (!register.getUserId().equals(userId))
        {
            throw new ServiceException("You do not have permission to cancel this registration");
        }
        ensureTeamEditable(register, userId, true);
        if (register.getTeamId() != null && !StringUtils.isEmpty(register.getTeamMembers()))
        {
            String studentNo = null;
            SysUser user = sysUserMapper.selectUserById(userId);
            if (user != null)
            {
                studentNo = StringUtils.isEmpty(user.getStudentNo()) ? user.getUserName() : user.getStudentNo();
            }
            try
            {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> members = mapper.readValue(register.getTeamMembers(), new TypeReference<List<Map<String, Object>>>() {});
                if (members != null && !members.isEmpty())
                {
                    List<Map<String, Object>> filtered = new ArrayList<>();
                    for (Map<String, Object> m : members)
                    {
                        Object noObj = m.get("studentNo");
                        String mNo = noObj == null ? null : String.valueOf(noObj);
                        if (studentNo != null && studentNo.equals(mNo))
                        {
                            continue;
                        }
                        filtered.add(m);
                    }
                    String newJson = mapper.writeValueAsString(filtered);
                    // Update all members with this new JSON
                    competitionRegisterMapper.updateTeamMembersByTeamId(register.getTeamId(), newJson);
                }
            }
            catch (Exception ignore)
            {
            }
        }

        // 如果是队长取消报名，删除整个队伍的所有报名记录
        if (register.getTeamId() != null) {
            List<CompetitionRegister> teamRegs = competitionRegisterMapper.selectByTeamId(register.getTeamId());
            if (teamRegs != null && !teamRegs.isEmpty()) {
                for (CompetitionRegister reg : teamRegs) {
                    competitionWorkMapper.deleteCompetitionWorkByParticipationId(reg.getRegisterId());
                    competitionParticipationMapper.deleteById(reg.getRegisterId());
                    competitionRegisterMapper.deleteById(reg.getRegisterId());
                }
            }
            // 强制删除教师队伍关联
            teacherTeamRelMapper.deleteByTeamId(register.getTeamId());
        } else {
            // 个人报名，只删除自己的记录
            competitionWorkMapper.deleteCompetitionWorkByParticipationId(registerId);
            competitionParticipationMapper.deleteById(registerId);
            competitionRegisterMapper.deleteById(registerId);
        }
        cleanupTeacherTeamsIfEmpty(register.getTeamId());
        return true;
    }

    private CompetitionRegister getRegisterByIdCached(Long registerId, Supplier<CompetitionRegister> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || registerId == null)
        {
            return loader.get();
        }
        return context.registerByIdCache.computeIfAbsent(registerId, key -> loader.get());
    }

    private CompetitionRegister getRegisterByCompetitionUserTeamCached(Long competitionId, Long userId, Long teamId,
            Supplier<CompetitionRegister> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || competitionId == null || userId == null || teamId == null)
        {
            return loader.get();
        }
        String key = competitionId + ":" + userId + ":" + teamId;
        return context.registerByCompetitionUserTeamCache.computeIfAbsent(key, unused -> loader.get());
    }

    private CompetitionRegister getRegisterByCompetitionTeamCached(Long competitionId, Long teamId,
            Supplier<CompetitionRegister> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || competitionId == null || teamId == null)
        {
            return loader.get();
        }
        String key = competitionId + ":" + teamId;
        return context.registerByCompetitionTeamCache.computeIfAbsent(key, unused -> loader.get());
    }

    private List<CompetitionRegister> getTeamRegistersCached(Long teamId, Supplier<List<CompetitionRegister>> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || teamId == null)
        {
            return loader.get();
        }
        return context.teamRegistersCache.computeIfAbsent(teamId, key -> loader.get());
    }

    private CompetitionParticipation getParticipationCached(Long registerId, Supplier<CompetitionParticipation> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || registerId == null)
        {
            return loader.get();
        }
        return context.participationCache.computeIfAbsent(registerId, key -> loader.get());
    }

    private List<org.iflytek.system.domain.CompetitionReviewAssignment> getReviewAssignmentsCached(Long registerId,
            Supplier<List<org.iflytek.system.domain.CompetitionReviewAssignment>> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || registerId == null)
        {
            return loader.get();
        }
        return context.reviewAssignmentsCache.computeIfAbsent(registerId, key -> loader.get());
    }

    private SysUser getUserByIdCached(Long userId, Supplier<SysUser> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || userId == null)
        {
            return loader.get();
        }
        return context.userByIdCache.computeIfAbsent(userId, key -> loader.get());
    }

    private SysUser getBaseUserByIdCached(Long userId, Supplier<SysUser> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || userId == null)
        {
            return loader.get();
        }
        return context.baseUserByIdCache.computeIfAbsent(userId, key -> loader.get());
    }

    private SysUser getBaseUserByStudentNoCached(String studentNo, Supplier<SysUser> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || studentNo == null)
        {
            return loader.get();
        }
        return context.baseUserByStudentNoCache.computeIfAbsent(studentNo, key -> loader.get());
    }

    private SysUser getBaseUserByUserNameCached(String userName, Supplier<SysUser> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || userName == null)
        {
            return loader.get();
        }
        return context.baseUserByUserNameCache.computeIfAbsent(userName, key -> loader.get());
    }

    private List<TeacherTeamRel> getTeacherRelationsCached(Long teamId, Supplier<List<TeacherTeamRel>> loader)
    {
        RecentRegistrationsContext context = RECENT_REGISTRATIONS_CONTEXT.get();
        if (context == null || teamId == null)
        {
            return loader.get();
        }
        return context.teacherRelationsCache.computeIfAbsent(teamId, key -> loader.get());
    }

    private static class RecentRegistrationsContext
    {
        private final Map<Long, CompetitionRegister> registerByIdCache = new HashMap<>();
        private final Map<String, CompetitionRegister> registerByCompetitionUserTeamCache = new HashMap<>();
        private final Map<String, CompetitionRegister> registerByCompetitionTeamCache = new HashMap<>();
        private final Map<Long, List<CompetitionRegister>> teamRegistersCache = new HashMap<>();
        private final Map<Long, CompetitionParticipation> participationCache = new HashMap<>();
        private final Map<Long, List<org.iflytek.system.domain.CompetitionReviewAssignment>> reviewAssignmentsCache = new HashMap<>();
        private final Map<Long, SysUser> userByIdCache = new HashMap<>();
        private final Map<Long, SysUser> baseUserByIdCache = new HashMap<>();
        private final Map<String, SysUser> baseUserByStudentNoCache = new HashMap<>();
        private final Map<String, SysUser> baseUserByUserNameCache = new HashMap<>();
        private final Map<Long, List<TeacherTeamRel>> teacherRelationsCache = new HashMap<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMember(Long teamId, String studentNo, Long operatorUserId)
    {
        if (teamId == null || StringUtils.isEmpty(studentNo))
        {
            throw new ServiceException("Parameters are incomplete");
        }
        // Get team info from any register
        List<CompetitionRegister> teamRegs = competitionRegisterMapper.selectByTeamId(teamId);
        if (teamRegs == null || teamRegs.isEmpty()) {
            throw new ServiceException("Team not found");
        }
        CompetitionRegister teamInfo = resolveTeamRegister(teamRegs, studentNo);
        // 使用队伍主记录来检查权限，而不是用户的报名记录
        // 因为一个用户可能在同一竞赛中有多个报名记录
        if (!isUserLeaderForRegister(teamInfo, operatorUserId)) {
            throw new ServiceException("Only the team leader can remove members");
        }
        ensureTeamEditable(teamInfo, operatorUserId, true);
        
        boolean operatorIsLeader = false;
        String operatorNo = null;
        SysUser operator = sysUserMapper.selectUserById(operatorUserId);
        if (operator != null)
        {
            operatorNo = operator.getStudentNo() != null ? operator.getStudentNo() : operator.getUserName();
        }
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> members = new ArrayList<>();
            if (!StringUtils.isEmpty(teamInfo.getTeamMembers()))
            {
                members = mapper.readValue(teamInfo.getTeamMembers(), new TypeReference<List<Map<String, Object>>>() {});
            }
            for (Map<String, Object> m : members)
            {
                String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                String status = String.valueOf(m.getOrDefault("status", "")).toLowerCase();
                String role = String.valueOf(m.getOrDefault("role", "")).toLowerCase();
                if (operatorNo != null && operatorNo.equals(mNo) && "approved".equals(status) && "leader".equals(role))
                {
                    operatorIsLeader = true;
                    break;
                }
            }
            if (!operatorIsLeader)
            {
                for (Map<String, Object> m : members)
                {
                    Object inviterIdObj = m.get("inviterId");
                    Long inviterId = null;
                    try { if (inviterIdObj != null) inviterId = Long.valueOf(String.valueOf(inviterIdObj)); } catch (Exception ignore) { inviterId = null; }
                    if (inviterId != null && inviterId.equals(operatorUserId))
                    {
                        operatorIsLeader = true;
                        break;
                    }
                }
            }
            if (!operatorIsLeader)
            {
                throw new ServiceException("Only the team leader can remove members");
            }
            List<Map<String, Object>> filtered = new ArrayList<>();
            boolean removedApprovedMember = false;
            for (Map<String, Object> m : members)
            {
                String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                if (studentNo.equals(mNo))
                {
                    String status = String.valueOf(m.getOrDefault("status", "")).toLowerCase();
                    if ("approved".equals(status))
                    {
                        removedApprovedMember = true;
                    }
                    continue;
                }
                filtered.add(m);
            }
            String newJson = mapper.writeValueAsString(filtered);
            competitionRegisterMapper.updateTeamMembersByTeamId(teamId, newJson);

            // 无论成员状态如何，被移除后都应该删除其报名记录
            SysUser removedUser = sysUserMapper.selectUserByStudentNo(studentNo);
            if (removedUser == null)
            {
                removedUser = sysUserMapper.selectUserByUserName(studentNo);
            }
            if (removedUser != null && removedUser.getUserId() != null)
            {
                // 使用精确查询，确保删除的是被移除队伍的报名记录
                CompetitionRegister register = competitionRegisterMapper.selectByCompetitionAndUserAndTeam(teamInfo.getCompetitionId(), removedUser.getUserId(), teamId);
                if (register != null)
                {
                    competitionParticipationMapper.deleteById(register.getRegisterId());
                    competitionRegisterMapper.deleteById(register.getRegisterId());
                }
            }
            cleanupTeacherTeamsIfEmpty(teamId);
            return true;
        }
        catch (ServiceException se)
        {
            throw se;
        }
        catch (Exception e)
        {
            throw new ServiceException("Failed to remove team member");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean inviteMember(Long teamId, String studentNo, Long inviterUserId)
    {
        List<CompetitionRegister> teamRegs = competitionRegisterMapper.selectByTeamId(teamId);
        if (teamRegs == null || teamRegs.isEmpty()) {
            throw new ServiceException("Team not found");
        }
        CompetitionRegister teamInfo = resolveTeamRegister(teamRegs, studentNo);
        // 使用队伍主记录来检查权限，而不是用户的报名记录
        // 因为一个用户可能在同一竞赛中有多个报名记录
        if (!isUserLeaderForRegister(teamInfo, inviterUserId)) {
            throw new ServiceException("只有队长才能操作");
        }
        ensureTeamEditable(teamInfo, inviterUserId, true);

        SysUser inviter = sysUserMapper.selectUserById(inviterUserId);
        String inviterName = null;
        if (inviter != null) {
            String candidate = inviter.getStudentName();
            if (candidate == null || candidate.isEmpty()) {
                candidate = inviter.getNickName();
            }
            if ((candidate == null || candidate.isEmpty()) && inviter.getUserName() != null && !inviter.getUserName().isEmpty()) {
                candidate = inviter.getUserName();
            }
            inviterName = candidate;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String, Object>> members = new ArrayList<>();
            if (teamInfo.getTeamMembers() != null && !teamInfo.getTeamMembers().isEmpty()) {
                members = mapper.readValue(teamInfo.getTeamMembers(), new TypeReference<List<Map<String, Object>>>() {});
            }
            SysUser invited = sysUserMapper.selectUserByStudentNo(studentNo);
            if (invited == null) {
                invited = sysUserMapper.selectUserByUserName(studentNo);
            }
            
            // 检查被邀请用户是否已在同一竞赛的其他队伍中报名
            ensureStudentHasSingleTeamSlot(teamInfo.getCompetitionId(), studentNo, teamId);
            
            String invitedName = null;
            String invitedMajor = null;
            String invitedPhone = null;
            String invitedEmail = null;
            if (invited != null) {
                String candidate = invited.getStudentName();
                if (candidate == null || candidate.isEmpty()) {
                    candidate = invited.getNickName();
                }
                if ((candidate == null || candidate.isEmpty()) && invited.getUserName() != null && !invited.getUserName().isEmpty()) {
                    if (!invited.getUserName().equals(studentNo)) {
                        candidate = invited.getUserName();
                    }
                }
                invitedName = (candidate != null && !candidate.isEmpty()) ? candidate : null;
                invitedMajor = invited.getMajorName();
                invitedPhone = invited.getPhonenumber();
                invitedEmail = invited.getEmail();
            }
            boolean exists = false;
            for (Map<String, Object> m : members) {
                String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                if (studentNo.equals(mNo)) {
                    exists = true;
                    m.put("status", "pending");
                    m.put("type", "invite");
                    m.put("inviterId", inviterUserId);
                    if (invited != null && invited.getUserId() != null) {
                        m.put("invitedUserId", invited.getUserId());
                    }
                    if (inviterName != null) m.put("inviterName", inviterName);
                    if (invitedName != null) m.put("name", invitedName);
                    if (invitedMajor != null) m.put("major", invitedMajor);
                    if (invitedPhone != null && !invitedPhone.isEmpty()) m.put("phone", invitedPhone);
                    if (invitedEmail != null && !invitedEmail.isEmpty()) m.put("email", invitedEmail);
                }
            }
            if (!exists) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("studentNo", studentNo);
                entry.put("status", "pending");
                entry.put("type", "invite");
                entry.put("inviterId", inviterUserId);
                if (invited != null && invited.getUserId() != null) {
                    entry.put("invitedUserId", invited.getUserId());
                }
                if (inviterName != null) entry.put("inviterName", inviterName);
                if (invitedName != null) entry.put("name", invitedName);
                if (invitedMajor != null) entry.put("major", invitedMajor);
                if (invitedPhone != null && !invitedPhone.isEmpty()) entry.put("phone", invitedPhone);
                if (invitedEmail != null && !invitedEmail.isEmpty()) entry.put("email", invitedEmail);
                members.add(entry);
            }
            String newJson = mapper.writeValueAsString(members);
            competitionRegisterMapper.updateTeamMembersByTeamId(teamId, newJson);
            return true;
        } catch (Exception e) {
            throw new ServiceException("邀请失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyJoinTeam(Long teamId, Long applicantUserId)
    {
        List<CompetitionRegister> teamRegs = competitionRegisterMapper.selectByTeamId(teamId);
        if (teamRegs == null || teamRegs.isEmpty()) {
            throw new ServiceException("Team not found");
        }
        SysUser user = sysUserMapper.selectUserById(applicantUserId);
        if (user == null) {
            throw new ServiceException("User not found");
        }
        String studentNo = user.getStudentNo() != null ? user.getStudentNo() : user.getUserName();
        CompetitionRegister teamInfo = resolveTeamRegister(teamRegs, studentNo);
        ensureTeamEditable(teamInfo, applicantUserId, false);
        // 提取申请人资料（后续构建成员条目仍需使用）
        String applicantName = firstNonEmptyValue(user.getStudentName(), user.getNickName(), user.getUserName());
        String collegeName = firstNonEmptyValue(user.getCollegeName());
        String majorName = firstNonEmptyValue(user.getMajorName());
        String phone = firstNonEmptyValue(user.getPhonenumber());
        String email = firstNonEmptyValue(user.getEmail());
        // 资料完整性校验（复用统一方法，与建队/邀请等入口保持一致）
        if (!isCompleteStudentProfile(user))
        {
            throw new ServiceException("请先到个人中心完善个人信息后再申请加入队伍");
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            ensureStudentHasSingleTeamSlot(teamInfo.getCompetitionId(), studentNo, teamId);
            List<Map<String, Object>> members = new ArrayList<>();
            if (teamInfo.getTeamMembers() != null && !teamInfo.getTeamMembers().isEmpty()) {
                members = mapper.readValue(teamInfo.getTeamMembers(), new TypeReference<List<Map<String, Object>>>() {});
            }
            boolean updatedExisting = false;
            for (Map<String, Object> m : members) {
                String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                if (studentNo.equals(mNo)) {
                    String status = String.valueOf(m.getOrDefault("status", "")).toLowerCase();
                    String type = String.valueOf(m.getOrDefault("type", "")).toLowerCase();
                    if ("approved".equals(status) || "".equals(status)) {
                        throw new ServiceException("您已在该队伍中");
                    }
                    if ("pending".equals(status)) {
                        if ("invite".equals(type)) {
                            m.put("type", "apply");
                            m.put("applicantUserId", applicantUserId);
                            m.put("applicantName", applicantName);
                            m.put("name", applicantName);
                            m.put("college", collegeName);
                            m.put("major", majorName);
                            m.put("phone", phone);
                            m.put("email", email);
                            m.put("createTime", new Date().getTime());
                            m.remove("inviterId");
                            m.remove("inviterName");
                            updatedExisting = true;
                            break;
                        }
                        throw new ServiceException("您已提交过申请，请等待队长审批");
                    }
                    if ("rejected".equals(status) && "apply".equals(type)) {
                        throw new ServiceException("您对该队伍的申请已被驳回，暂不可重复申请");
                    }
                }
            }
            if (!updatedExisting) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("studentNo", studentNo);
                entry.put("status", "pending");
                entry.put("type", "apply");
                entry.put("role", "member");
                entry.put("applicantUserId", applicantUserId);
                entry.put("applicantName", applicantName);
                entry.put("name", applicantName);
                entry.put("college", collegeName);
                entry.put("major", majorName);
                entry.put("phone", phone);
                entry.put("email", email);
                entry.put("createTime", new Date().getTime());
                members.add(entry);
            }
            String newJson = mapper.writeValueAsString(members);
            competitionRegisterMapper.updateTeamMembersByTeamId(teamId, newJson);
            return true;
        } catch (Exception e) {
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            }
            throw new ServiceException("Failed to submit application");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveMember(Long teamId, String studentNo, Long approverUserId)
    {
        List<CompetitionRegister> teamRegs = competitionRegisterMapper.selectByTeamId(teamId);
        if (teamRegs == null || teamRegs.isEmpty()) {
            throw new ServiceException("Team not found");
        }
        CompetitionRegister teamInfo = resolveTeamRegister(teamRegs, studentNo);

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String, Object>> members = new ArrayList<>();
            if (teamInfo.getTeamMembers() != null && !teamInfo.getTeamMembers().isEmpty()) {
                members = mapper.readValue(teamInfo.getTeamMembers(), new TypeReference<List<Map<String, Object>>>() {});
            }
            Map<String, Object> targetMember = findTeamMember(members, studentNo);
            if (targetMember == null) {
                throw new ServiceException("Pending member not found");
            }
            String targetStatus = String.valueOf(targetMember.getOrDefault("status", "")).toLowerCase();
            if (!"pending".equals(targetStatus)) {
                throw new ServiceException("Pending member not found");
            }
            String targetType = String.valueOf(targetMember.getOrDefault("type", "invite")).toLowerCase();
            if ("apply".equals(targetType)) {
                CompetitionRegister approverRegister = competitionRegisterMapper.selectByCompetitionAndUserAndTeam(
                        teamInfo.getCompetitionId(), approverUserId, teamId);
                if (approverRegister == null || approverRegister.getTeamId() == null || !approverRegister.getTeamId().equals(teamId))
                {
                    throw new ServiceException("只有队长才能操作");
                }
                ensureTeamEditable(approverRegister, approverUserId, true);
            } else {
                ensureTeamEditable(teamInfo, approverUserId, false);
                String approverStudentNo = getStudentNoByUserId(approverUserId);
                Long invitedUserId = parseLongValue(targetMember.get("invitedUserId"));
                boolean sameUser = invitedUserId != null && invitedUserId.equals(approverUserId);
                boolean sameStudentNo = !StringUtils.isEmpty(approverStudentNo) && studentNo.equals(approverStudentNo);
                if (!sameUser && !sameStudentNo)
                {
                    throw new ServiceException("只有被邀请学生才能处理该邀请");
                }
            }

            ensureStudentHasSingleTeamSlot(teamInfo.getCompetitionId(), studentNo, teamId);
            boolean updated = false;
            for (Map<String, Object> m : members) {
                String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                if (studentNo.equals(mNo)) {
                    m.put("status", "approved");
                    SysUser u = sysUserMapper.selectUserByStudentNo(studentNo);
                    if (u == null) u = sysUserMapper.selectUserByUserName(studentNo);
                    if (u != null) {
                        Object nameObj = m.get("name");
                        String nm = nameObj == null ? null : String.valueOf(nameObj);
                        if (nm == null || nm.isEmpty()) {
                            String nn = u.getStudentName();
                            if (nn == null) nn = u.getNickName();
                            if (nn == null) nn = u.getUserName();
                            if (nn != null) m.put("name", nn);
                        }
                        Object collegeObj = m.get("college");
                        if ((collegeObj == null || String.valueOf(collegeObj).isEmpty()) && u.getCollegeName() != null) {
                            m.put("college", u.getCollegeName());
                        }
                        Object majorObj = m.get("major");
                        if ((majorObj == null || String.valueOf(majorObj).isEmpty()) && u.getMajorName() != null) {
                            m.put("major", u.getMajorName());
                        }
                        Object phoneObj = m.get("phone");
                        if ((phoneObj == null || String.valueOf(phoneObj).isEmpty()) && u.getPhonenumber() != null) {
                            m.put("phone", u.getPhonenumber());
                        }
                        Object emailObj = m.get("email");
                        if ((emailObj == null || String.valueOf(emailObj).isEmpty()) && u.getEmail() != null) {
                            m.put("email", u.getEmail());
                        }
                    }
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                throw new ServiceException("Pending member not found");
            }
            String newJson = mapper.writeValueAsString(members);
            competitionRegisterMapper.updateTeamMembersByTeamId(teamId, newJson);

            // Sync
            syncTeamMembersRegistration(teamInfo.getCompetitionId(), teamInfo.getTeamId(), teamInfo.getTeamName(), newJson, new Date());
            return true;
        } catch (ServiceException se) {
            throw se;
        } catch (Exception e) {
            throw new ServiceException("Approval failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectMember(Long teamId, String studentNo, Long approverUserId)
    {
        List<CompetitionRegister> teamRegs = competitionRegisterMapper.selectByTeamId(teamId);
        if (teamRegs == null || teamRegs.isEmpty()) {
            throw new ServiceException("Team not found");
        }
        CompetitionRegister teamInfo = resolveTeamRegister(teamRegs, studentNo);

        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String, Object>> members = new ArrayList<>();
            if (teamInfo.getTeamMembers() != null && !teamInfo.getTeamMembers().isEmpty()) {
                members = mapper.readValue(teamInfo.getTeamMembers(), new TypeReference<List<Map<String, Object>>>() {});
            }
            Map<String, Object> targetMember = findTeamMember(members, studentNo);
            if (targetMember == null) {
                throw new ServiceException("Pending member not found");
            }
            String targetStatus = String.valueOf(targetMember.getOrDefault("status", "")).toLowerCase();
            if (!"pending".equals(targetStatus)) {
                throw new ServiceException("Pending member not found");
            }
            String targetType = String.valueOf(targetMember.getOrDefault("type", "invite")).toLowerCase();
            if ("apply".equals(targetType)) {
                CompetitionRegister approverRegister = competitionRegisterMapper.selectByCompetitionAndUserAndTeam(
                        teamInfo.getCompetitionId(), approverUserId, teamId);
                if (approverRegister == null || approverRegister.getTeamId() == null || !approverRegister.getTeamId().equals(teamId))
                {
                    throw new ServiceException("只有队长才能操作");
                }
                ensureTeamEditable(approverRegister, approverUserId, true);
            } else {
                ensureTeamEditable(teamInfo, approverUserId, false);
                String approverStudentNo = getStudentNoByUserId(approverUserId);
                Long invitedUserId = parseLongValue(targetMember.get("invitedUserId"));
                boolean sameUser = invitedUserId != null && invitedUserId.equals(approverUserId);
                boolean sameStudentNo = !StringUtils.isEmpty(approverStudentNo) && studentNo.equals(approverStudentNo);
                if (!sameUser && !sameStudentNo)
                {
                    throw new ServiceException("只有被邀请学生才能处理该邀请");
                }
            }
            List<Map<String, Object>> filtered = new ArrayList<>();
            boolean rejected = false;
            for (Map<String, Object> m : members) {
                String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                if (studentNo.equals(mNo)) {
                    m.put("status", "rejected");
                    m.put("decisionTime", new Date().getTime());
                    rejected = true;
                    filtered.add(m);
                    continue;
                }
                filtered.add(m);
            }
            if (!rejected) {
                throw new ServiceException("Pending member not found");
            }
            String newJson = mapper.writeValueAsString(filtered);
            competitionRegisterMapper.updateTeamMembersByTeamId(teamId, newJson);
            
            // 删除用户在该队伍中的报名记录
            SysUser user = sysUserMapper.selectUserByStudentNo(studentNo);
            if (user != null) {
                CompetitionRegister userRegister = competitionRegisterMapper.selectByCompetitionAndUserAndTeam(teamInfo.getCompetitionId(), user.getUserId(), teamId);
                if (userRegister != null) {
                    competitionRegisterMapper.deleteById(userRegister.getRegisterId());
                }
            }
            
            return true;
        } catch (ServiceException e) {
            // 业务异常原样抛出，保留原始错误信息
            throw e;
        } catch (Exception e) {
            throw new ServiceException("操作失败，请稍后重试");
        }
    }

    @Override
    public List<Map<String, Object>> listUserInvitations(Long userId)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        SysUser user = sysUserMapper.selectUserById(userId);
        if (user == null) {
            return result;
        }
        String myNo = user.getStudentNo() != null ? user.getStudentNo() : user.getUserName();

        List<CompetitionRegister> allTeams = competitionRegisterMapper.selectAllTeams();
        ObjectMapper mapper = new ObjectMapper();
        for (CompetitionRegister team : allTeams) {
            team = cleanupPendingMembersForLockedTeam(team);
            if (team.getTeamMembers() == null || team.getTeamMembers().isEmpty()) continue;
            try {
                List<Map<String, Object>> members = mapper.readValue(team.getTeamMembers(), new TypeReference<List<Map<String, Object>>>() {});
                boolean isTeamLeader = isUserLeaderForRegister(team, userId);
                Set<String> pendingApplyNos = new LinkedHashSet<>();
                for (Map<String, Object> m : members) {
                    String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                    String status = String.valueOf(m.getOrDefault("status", "pending"));
                    String type = String.valueOf(m.getOrDefault("type", "invite"));
                    if ("pending".equalsIgnoreCase(status) && "apply".equalsIgnoreCase(type)) {
                        pendingApplyNos.add(mNo);
                    }
                }
                for (Map<String, Object> m : members) {
                    String status = String.valueOf(m.getOrDefault("status", "pending"));
                    String type = String.valueOf(m.getOrDefault("type", "invite"));
                    String mNo = String.valueOf(m.getOrDefault("studentNo", ""));
                    Long invitedUserId = parseLongValue(m.get("invitedUserId"));
                    
                    if ("apply".equalsIgnoreCase(type) && isTeamLeader && "pending".equalsIgnoreCase(status)) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", team.getTeamId() + "-" + mNo + "-apply");
                        item.put("teamId", team.getTeamId());
                        item.put("teamName", team.getTeamName());
                        item.put("competitionId", team.getCompetitionId());
                        item.put("type", "apply");
                        
                        String applicantName = m.get("name") != null ? String.valueOf(m.get("name")) : "";
                        if (applicantName == null || applicantName.trim().isEmpty()) {
                            SysUser appUser = sysUserMapper.selectUserByStudentNo(mNo);
                            if (appUser == null) appUser = sysUserMapper.selectUserByUserName(mNo);
                            if (appUser != null) {
                                applicantName = appUser.getStudentName();
                                if (applicantName == null) applicantName = appUser.getNickName();
                                if (applicantName == null) applicantName = appUser.getUserName();
                            }
                        }
                        
                        item.put("memberName", applicantName != null ? applicantName : "Unknown member");
                        item.put("applicantName", applicantName != null ? applicantName : "Unknown member");
                        item.put("memberStudentNo", mNo);
                        item.put("status", "pending");
                        Competition comp = competitionMapper.selectCompetitionById(team.getCompetitionId());
                        if (comp != null) {
                            item.put("competitionName", comp.getCompetitionName());
                        }
                        result.add(item);
                    }

                    if ("invite".equalsIgnoreCase(type)
                            && "pending".equalsIgnoreCase(status)
                            && (myNo.equals(mNo) || (invitedUserId != null && invitedUserId.equals(userId)))
                            && !pendingApplyNos.contains(mNo)) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", team.getTeamId() + "-" + mNo + "-invite");
                        item.put("teamId", team.getTeamId());
                        item.put("teamName", team.getTeamName());
                        item.put("competitionId", team.getCompetitionId());
                        item.put("type", "invite");
                        item.put("memberName", m.getOrDefault("name", ""));
                        item.put("memberStudentNo", mNo);
                        if (invitedUserId != null) {
                            item.put("invitedUserId", invitedUserId);
                        }
                        item.put("status", "pending");
                        // ... inviter name logic ...
                        Object invNameObj = m.get("inviterName");
                        String invName = invNameObj != null ? String.valueOf(invNameObj) : "";
                        if (invName == null || invName.trim().isEmpty()) {
                            Object inviterIdObj = m.get("inviterId");
                            try {
                                if (inviterIdObj != null) {
                                    Long inviterId = Long.valueOf(String.valueOf(inviterIdObj));
                                    SysUser invUser = sysUserMapper.selectUserById(inviterId);
                                    if (invUser != null) {
                                         // ...
                                        String candidate = invUser.getStudentName();
                                        if (candidate == null) candidate = invUser.getNickName();
                                        if (candidate == null) candidate = invUser.getUserName();
                                        invName = candidate;
                                    }
                                }
                            } catch (Exception ignore) {}
                        }
                        if (invName != null) item.put("inviterName", invName);

                        Competition comp = competitionMapper.selectCompetitionById(team.getCompetitionId());
                        if (comp != null) {
                            item.put("competitionName", comp.getCompetitionName());
                        }
                        result.add(item);
                    }

                    if ("apply".equalsIgnoreCase(type) && myNo.equals(mNo) && !"pending".equalsIgnoreCase(status)) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", team.getTeamId() + "-" + mNo + "-apply-result");
                        item.put("teamId", team.getTeamId());
                        item.put("teamName", team.getTeamName());
                        item.put("competitionId", team.getCompetitionId());
                        item.put("type", "apply-result");
                        item.put("memberName", m.getOrDefault("name", ""));
                        item.put("applicantName", m.getOrDefault("applicantName", m.getOrDefault("name", "")));
                        item.put("memberStudentNo", mNo);
                        item.put("status", status.toLowerCase());
                        item.put("read", false);
                        item.put("createTime", m.getOrDefault("decisionTime", m.getOrDefault("createTime", "")));
                        Competition comp = competitionMapper.selectCompetitionById(team.getCompetitionId());
                        if (comp != null) {
                            item.put("competitionName", comp.getCompetitionName());
                        }
                        result.add(item);
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return result;
    }
    @Override
    public void recordLearning(Long competitionId) {
        Competition competition = competitionMapper.selectCompetitionById(competitionId);
        if (competition != null) {
            competition.setLearningCount((competition.getLearningCount() == null ? 0 : competition.getLearningCount()) + 1);
            competitionMapper.updateCompetition(competition);
        }
    }

    private void replaceTeacherTeams(Long teamId, List<Map<String, Object>> teacherList)
    {
        if (teamId == null)
        {
            return;
        }
        teacherTeamRelMapper.deleteByTeamId(teamId);
        List<TeacherTeamRel> rows = new ArrayList<>();
        Set<Long> teacherIds = new LinkedHashSet<>();
        if (teacherList != null)
        {
            for (Map<String, Object> teacher : teacherList)
            {
                if (teacher == null)
                {
                    continue;
                }
                Long teacherId = parseLongValue(teacher.get("teacherId"));
                if (teacherId == null || !teacherIds.add(teacherId))
                {
                    continue;
                }
                TeacherTeamRel row = new TeacherTeamRel();
                row.setTeamId(teamId);
                row.setTeacherId(teacherId);
                rows.add(row);
            }
        }
        if (!rows.isEmpty())
        {
            teacherTeamRelMapper.batchInsert(rows);
        }
    }

    private boolean isTeacherBindingChanged(Long teamId, List<Map<String, Object>> teacherList)
    {
        if (teamId == null)
        {
            return teacherList != null && !teacherList.isEmpty();
        }
        return !buildTeacherSignatures(buildTeacherResponse(teamId)).equals(buildTeacherSignatures(teacherList));
    }

    private Set<String> buildTeacherSignatures(List<Map<String, Object>> teacherList)
    {
        Set<String> signatures = new LinkedHashSet<>();
        if (teacherList == null)
        {
            return signatures;
        }
        for (Map<String, Object> teacher : teacherList)
        {
            if (teacher == null)
            {
                continue;
            }
            Object rawTeacherId = teacher.get("teacherId");
            String teacherId = trimToNull(rawTeacherId == null ? "" : String.valueOf(rawTeacherId));
            Object rawName = teacher.get("name") != null ? teacher.get("name") : teacher.get("teacherName");
            String name = trimToNull(rawName == null ? "" : String.valueOf(rawName));
            Object rawPhone = teacher.get("phone");
            String phone = trimToNull(rawPhone == null ? "" : String.valueOf(rawPhone));
            teacherId = teacherId == null ? "" : teacherId;
            name = name == null ? "" : name;
            phone = phone == null ? "" : phone;
            if (teacherId.isEmpty() && name.isEmpty() && phone.isEmpty())
            {
                continue;
            }
            signatures.add(teacherId + "|" + name + "|" + phone);
        }
        return signatures;
    }

    private List<Map<String, Object>> parseTeamMembers(String teamMembers)
    {
        if (StringUtils.isEmpty(teamMembers))
        {
            return new ArrayList<>();
        }
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> members = mapper.readValue(teamMembers, new TypeReference<List<Map<String, Object>>>() {});
            return members == null ? new ArrayList<>() : members;
        }
        catch (Exception ignore)
        {
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> buildTeacherResponse(Long teamId)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        if (teamId == null)
        {
            return result;
        }
        List<TeacherTeamRel> relations = getTeacherRelationsCached(
                teamId,
                () -> teacherTeamRelMapper.selectByTeamId(teamId));
        if (relations == null || relations.isEmpty())
        {
            return result;
        }
        List<Long> teacherIds = new ArrayList<>();
        for (TeacherTeamRel relation : relations)
        {
            if (relation.getTeacherId() != null)
            {
                teacherIds.add(relation.getTeacherId());
            }
        }
        if (teacherIds.isEmpty())
        {
            return result;
        }
        List<TeacherTeam> teachers = teacherTeamMapper.selectBaseTeachersByIds(teacherIds);
        Map<Long, TeacherTeam> teacherMap = new HashMap<>();
        for (TeacherTeam teacher : teachers)
        {
            teacherMap.put(teacher.getId(), teacher);
        }
        for (TeacherTeamRel relation : relations)
        {
            TeacherTeam teacher = teacherMap.get(relation.getTeacherId());
            if (teacher != null)
            {
                result.add(buildTeacherItem(teacher));
            }
        }
        return result;
    }

    private Map<String, Object> buildTeacherItem(TeacherTeam teacher)
    {
        Map<String, Object> item = new HashMap<>();
        item.put("teacherId", teacher.getId());
        item.put("name", teacher.getTeacherName() == null ? "" : teacher.getTeacherName());
        item.put("gender", resolveTeacherGender(teacher.getSex()));
        item.put("birthDate", teacher.getBirthDate() == null ? "" : teacher.getBirthDate());
        item.put("enrollYear", "");
        item.put("phone", teacher.getPhone() == null ? "" : teacher.getPhone());
        item.put("email", teacher.getEmail() == null ? "" : teacher.getEmail());
        item.put("unit", teacher.getWorkUnit() == null ? "" : teacher.getWorkUnit());
        item.put("title", teacher.getTitle() == null ? "" : teacher.getTitle());
        item.put("position", teacher.getPosition() == null ? "" : teacher.getPosition());
        item.put("politicalStatus", teacher.getPoliticalStatus() == null ? "" : teacher.getPoliticalStatus());
        return item;
    }

    private String resolveTeacherGender(String sex)
    {
        if ("0".equals(sex))
        {
            return "male";
        }
        if ("1".equals(sex))
        {
            return "female";
        }
        return "";
    }
    private void cleanupTeacherTeamsIfEmpty(Long teamId)
    {
        if (teamId == null)
        {
            return;
        }
        List<CompetitionRegister> teamRegs = competitionRegisterMapper.selectByTeamId(teamId);
        if (teamRegs == null || teamRegs.isEmpty())
        {
            teacherTeamRelMapper.deleteByTeamId(teamId);
        }
    }

    private Long parseLongValue(Object value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
