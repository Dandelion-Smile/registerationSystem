package org.iflytek.system.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.iflytek.common.core.domain.entity.SysRole;
import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.common.exception.ServiceException;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionParticipation;
import org.iflytek.system.domain.CompetitionPermission;
import org.iflytek.system.domain.CompetitionRegister;
import org.iflytek.system.domain.CompetitionReviewAssignment;
import org.iflytek.system.domain.CompetitionScore;
import org.iflytek.system.domain.TeacherTeam;
import org.iflytek.system.domain.TeacherTeamRel;
import org.iflytek.system.mapper.CompetitionMapper;
import org.iflytek.system.mapper.CompetitionParticipationMapper;
import org.iflytek.system.mapper.CompetitionPermissionMapper;
import org.iflytek.system.mapper.CompetitionRegisterMapper;
import org.iflytek.system.mapper.CompetitionReviewAssignmentMapper;
import org.iflytek.system.mapper.CompetitionScoreMapper;
import org.iflytek.system.mapper.SysRoleMapper;
import org.iflytek.system.mapper.SysUserMapper;
import org.iflytek.system.mapper.TeacherTeamMapper;
import org.iflytek.system.mapper.TeacherTeamRelMapper;
import org.iflytek.system.service.IAdminCompetitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员端竞赛管理服务实现
 */
@Service
public class AdminCompetitionServiceImpl implements IAdminCompetitionService
{
    private static final String ASSIGNED = "ASSIGNED";
    private static final String REVIEWED = "REVIEWED";

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private CompetitionPermissionMapper competitionPermissionMapper;

    @Autowired
    private CompetitionRegisterMapper competitionRegisterMapper;

    @Autowired
    private CompetitionParticipationMapper competitionParticipationMapper;

    @Autowired
    private CompetitionScoreMapper competitionScoreMapper;

    @Autowired
    private CompetitionReviewAssignmentMapper competitionReviewAssignmentMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private TeacherTeamMapper teacherTeamMapper;

    @Autowired
    private TeacherTeamRelMapper teacherTeamRelMapper;

    @Override
    public CompetitionRegister selectTeamDetailById(Long registerId)
    {
        CompetitionRegister detail = competitionMapper.selectTeamDetailById(registerId);
        return ensureWorkInfo(detail);
    }

    @Override
    public List<Map<String, Object>> selectTeamListByCompId(Long competitionId)
    {
        List<Map<String, Object>> teams = competitionMapper.selectTeamListByCompId(competitionId);
        backfillMissingWorkInfo(teams);
        attachAssignmentInfo(competitionId, teams);
        return teams;
    }

    @Override
    public Map<String, Object> selectCompetitionCountStats()
    {
        return competitionMapper.selectCompetitionCountStats();
    }

    @Override
    public List<String> selectCompetitionTypes()
    {
        return competitionMapper.selectDistinctCompetitionTypes();
    }

    @Override
    public List<Competition> selectCompetitionList(Competition competition)
    {
        return competitionMapper.selectCompetitionList(competition);
    }

    @Override
    public Competition selectCompetitionById(Long competitionId)
    {
        return competitionMapper.selectCompetitionById(competitionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCompetition(Competition competition)
    {
        if (competition.getCompetitionName() == null || competition.getCompetitionName().trim().isEmpty())
        {
            throw new ServiceException("竞赛名称不能为空");
        }
        if (competition.getRegisterStartTime() == null)
        {
            throw new ServiceException("报名开始时间不能为空");
        }
        if (competition.getRegisterEndTime() == null)
        {
            throw new ServiceException("报名结束时间不能为空");
        }
        if (competition.getRegisterStartTime().after(competition.getRegisterEndTime()))
        {
            throw new ServiceException("报名开始时间不能晚于结束时间");
        }

        return competitionMapper.insertCompetition(competition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCompetition(Competition competition)
    {
        if (competition.getCompetitionId() == null)
        {
            throw new ServiceException("竞赛ID不能为空");
        }

        if (competition.getCompetitionName() != null && competition.getCompetitionName().trim().isEmpty())
        {
            throw new ServiceException("竞赛名称不能为空");
        }
        if (competition.getRegisterStartTime() != null && competition.getRegisterEndTime() != null
                && competition.getRegisterStartTime().after(competition.getRegisterEndTime()))
        {
            throw new ServiceException("报名开始时间不能晚于结束时间");
        }

        return competitionMapper.updateCompetition(competition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCompetitionByIds(Long[] competitionIds)
    {
        if (competitionIds == null || competitionIds.length == 0)
        {
            throw new ServiceException("竞赛ID数组不能为空");
        }

        int count = 0;
        for (Long competitionId : competitionIds)
        {
            count += competitionMapper.deleteCompetitionById(competitionId);
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCompetitionById(Long competitionId)
    {
        if (competitionId == null)
        {
            throw new ServiceException("竞赛ID不能为空");
        }
        return competitionMapper.deleteCompetitionById(competitionId);
    }

    @Override
    public List<Map<String, Object>> selectTeamListByCompId(Long competitionId, String sortType, Double minScore,
            Double maxScore)
    {
        List<Map<String, Object>> teams = competitionMapper.selectTeamListByCompId(competitionId, sortType, minScore, maxScore);
        backfillMissingWorkInfo(teams);
        attachAssignmentInfo(competitionId, teams);
        return teams;
    }

    @Override
    public List<Map<String, Object>> selectScoresByParticipationId(Long participationId)
    {
        return competitionMapper.selectScoresByParticipationId(participationId);
    }

    @Override
    public List<Map<String, Object>> selectReviewersByCompetitionId(Long competitionId)
    {
        ensureCompetitionExists(competitionId);

        List<CompetitionReviewAssignment> assignments = competitionReviewAssignmentMapper.selectByCompetitionId(competitionId);
        Map<Long, Long> assignedCountMap = new HashMap<>();
        for (CompetitionReviewAssignment assignment : assignments)
        {
            assignedCountMap.merge(assignment.getReviewerId(), 1L, Long::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (SysUser reviewer : listCompetitionReviewers(competitionId).values())
        {
            Map<String, Object> item = new HashMap<>();
            item.put("userId", reviewer.getUserId());
            item.put("teacherName", getDisplayName(reviewer));
            item.put("phone", reviewer.getPhonenumber());
            item.put("assignedCount", assignedCountMap.getOrDefault(reviewer.getUserId(), 0L));
            item.put("assigned", assignedCountMap.containsKey(reviewer.getUserId()));
            result.add(item);
        }

        result.sort(Comparator.comparing(item -> String.valueOf(item.get("teacherName"))));
        return result;
    }

    @Override
    public List<Map<String, Object>> selectReviewerCandidatesByCompetitionId(Long competitionId)
    {
        ensureCompetitionExists(competitionId);

        Map<Long, SysUser> currentPool = listCompetitionReviewers(competitionId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (SysUser teacher : listAllTeacherUsers())
        {
            if (teacher.getUserId() == null || currentPool.containsKey(teacher.getUserId()))
            {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("userId", teacher.getUserId());
            item.put("teacherName", getDisplayName(teacher));
            item.put("phone", teacher.getPhonenumber());
            item.put("disabled", false);
            item.put("disabledReason", null);
            result.add(item);
        }
        result.sort((a, b) -> {
            boolean aDisabled = Boolean.TRUE.equals(a.get("disabled"));
            boolean bDisabled = Boolean.TRUE.equals(b.get("disabled"));
            if (aDisabled != bDisabled)
            {
                return aDisabled ? 1 : -1;
            }
            return String.valueOf(a.get("teacherName")).compareTo(String.valueOf(b.get("teacherName")));
        });
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addReviewersToCompetition(Long competitionId, List<Long> reviewerIds, Long operatorId)
    {
        ensureCompetitionExists(competitionId);
        if (reviewerIds == null || reviewerIds.isEmpty())
        {
            throw new ServiceException("请选择要加入老师池的老师");
        }

        Set<Long> uniqueReviewerIds = new LinkedHashSet<>(reviewerIds);
        Map<Long, SysUser> usersById = loadUsersByIds(uniqueReviewerIds);

        int addedCount = 0;
        int skippedCount = 0;

        for (Long reviewerId : uniqueReviewerIds)
        {
            SysUser reviewer = usersById.get(reviewerId);
            if (reviewer == null)
            {
                skippedCount++;
                continue;
            }

            CompetitionPermission existing = competitionPermissionMapper.selectByUserAndCompetition(reviewerId, competitionId);
            if (existing != null)
            {
                if (existing.getPermissionId() == null || existing.getPermissionId() != 1)
                {
                    existing.setPermissionId(1);
                    competitionPermissionMapper.updatePermission(existing);
                    addedCount++;
                }
                else
                {
                    skippedCount++;
                }
                continue;
            }

            CompetitionPermission permission = new CompetitionPermission();
            permission.setUserId(reviewerId);
            permission.setCompetitionId(competitionId);
            permission.setPermissionId(1);
            competitionPermissionMapper.insertPermission(permission);
            addedCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("addedCount", addedCount);
        result.put("skippedCount", skippedCount);
        result.put("message", "已加入 " + addedCount + " 位老师进入评审老师池");
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> autoAssignReviewers(Long competitionId, Integer reviewerCountPerTeam, Long operatorId)
    {
        if (reviewerCountPerTeam == null || reviewerCountPerTeam <= 0)
        {
            throw new ServiceException("每支队伍的评审老师人数必须大于0");
        }
        ensureCompetitionExists(competitionId);
        return reassignCompetition(competitionId, reviewerCountPerTeam, operatorId, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> revokeReviewerAndReassign(Long competitionId, Long reviewerId, Long operatorId)
    {
        ensureCompetitionExists(competitionId);
        SysUser reviewer = sysUserMapper.selectUserById(reviewerId);
        if (reviewer == null)
        {
            throw new ServiceException("评审老师不存在");
        }

        List<CompetitionReviewAssignment> assignmentsBeforeDelete = competitionReviewAssignmentMapper.selectByCompetitionId(competitionId);
        List<CompetitionReviewAssignment> affectedAssignments = assignmentsBeforeDelete.stream()
                .filter(item -> reviewerId.equals(item.getReviewerId()))
                .collect(Collectors.toList());
        boolean hadAssignments = !affectedAssignments.isEmpty();
        Integer inferredCount = inferReviewerCountPerTeam(assignmentsBeforeDelete);
        Set<Long> affectedParticipationIds = affectedAssignments.stream()
                .map(CompetitionReviewAssignment::getParticipationId)
                .collect(Collectors.toSet());

        competitionPermissionMapper.deletePermission(reviewerId, competitionId);
        competitionReviewAssignmentMapper.deletePendingByCompetitionAndReviewer(competitionId, reviewerId);

        Map<String, Object> result = new HashMap<>();
        result.put("teacherName", getDisplayName(reviewer));

        if (hadAssignments && inferredCount != null && inferredCount > 0)
        {
            Map<String, Object> autoResult = reassignCompetition(competitionId, inferredCount, operatorId, affectedParticipationIds);
            result.put("reassigned", true);
            result.put("message", "由于评审老师改动，已自动重排受影响队伍");
            result.put("assignmentResult", autoResult);
        }
        else
        {
            result.put("reassigned", false);
            result.put("message", "已禁止该老师参与此次评审");
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateTeamReviewers(Long competitionId, Long registerId, Long teamId, List<Long> reviewerIds, Long operatorId)
    {
        if (reviewerIds == null)
        {
            throw new ServiceException("评审老师列表不能为空");
        }

        CompetitionRegister register = null;
        if (registerId != null)
        {
            register = competitionRegisterMapper.selectById(registerId);
            if (register != null && !competitionId.equals(register.getCompetitionId()))
            {
                register = null;
            }
        }
        if (register == null)
        {
            register = competitionRegisterMapper.selectByCompetitionAndTeam(competitionId, teamId);
        }
        if (register == null)
        {
            throw new ServiceException("参赛队伍不存在");
        }
        ensureParticipationRecord(register.getRegisterId());

        Set<Long> reviewerIdSet = new HashSet<>(reviewerIds);
        if (reviewerIds.size() != reviewerIdSet.size())
        {
            throw new ServiceException("评审老师不能重复");
        }

        List<CompetitionReviewAssignment> currentAssignments = competitionReviewAssignmentMapper
                .selectByCompetitionAndParticipation(competitionId, register.getRegisterId());
        Map<Long, CompetitionReviewAssignment> assignmentMap = currentAssignments.stream()
                .collect(Collectors.toMap(CompetitionReviewAssignment::getReviewerId, item -> item, (a, b) -> a));

        Map<Long, SysUser> reviewerPool = listCompetitionReviewers(competitionId);
        Set<Long> invalidReviewerIds = reviewerIdSet.stream()
                .filter(reviewerId -> !reviewerPool.containsKey(reviewerId))
                .collect(Collectors.toSet());
        Set<Long> historicalReviewerIds = assignmentMap.keySet();
        invalidReviewerIds.removeIf(historicalReviewerIds::contains);
        for (Long reviewerId : invalidReviewerIds)
        {
            throw new ServiceException("存在未在当前评审老师池中的老师，请重新勾选后保存");
        }

        Set<Long> advisorUserIds = getAdvisorUserIdsByTeamId(teamId);
        for (Long reviewerId : reviewerIdSet)
        {
            if (advisorUserIds.contains(reviewerId))
            {
                throw new ServiceException("评分老师不能评审自己指导的队伍");
            }
        }

        List<CompetitionScore> scores = competitionScoreMapper.selectByParticipationId(register.getRegisterId());
        Set<Long> reviewedTeacherIds = scores.stream()
                .map(CompetitionScore::getReviewerId)
                .filter(item -> item != null)
                .collect(Collectors.toSet());

        if (!reviewerIdSet.containsAll(reviewedTeacherIds))
        {
            throw new ServiceException("已评分的评审老师不能移除");
        }

        competitionReviewAssignmentMapper.deletePendingByParticipationId(register.getRegisterId());
        Date now = new Date();
        for (Long reviewerId : reviewerIdSet)
        {
            if (reviewedTeacherIds.contains(reviewerId))
            {
                CompetitionReviewAssignment existing = assignmentMap.get(reviewerId);
                if (existing == null)
                {
                    insertAssignment(competitionId, register.getRegisterId(), register.getTeamId(), reviewerPool.get(reviewerId),
                            REVIEWED, operatorId, now);
                }
                else if (!REVIEWED.equals(existing.getAssignmentStatus()))
                {
                    existing.setAssignmentStatus(REVIEWED);
                    existing.setAssignedBy(operatorId);
                    existing.setAssignedTime(now);
                    existing.setUpdateTime(now);
                    competitionReviewAssignmentMapper.updateAssignment(existing);
                }
                continue;
            }

            insertAssignment(competitionId, register.getRegisterId(), register.getTeamId(), reviewerPool.get(reviewerId),
                    ASSIGNED, operatorId, now);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("teamId", teamId);
        result.put("participationId", register.getRegisterId());
        result.put("assignedReviewers", buildAssignedReviewerItems(
                competitionReviewAssignmentMapper.selectByCompetitionAndParticipation(competitionId, register.getRegisterId()),
                loadUsersByIds(reviewerIdSet)));
        return result;
    }

    private Map<String, Object> reassignCompetition(Long competitionId, Integer reviewerCountPerTeam, Long operatorId,
            Set<Long> targetParticipationIds)
    {
        Map<Long, SysUser> reviewerPool = listCompetitionReviewers(competitionId);
        if (reviewerPool.isEmpty())
        {
            throw new ServiceException("当前赛事没有可用的评审老师");
        }

        List<CompetitionRegister> teamRegisters = competitionRegisterMapper.selectByCompetitionId(competitionId).stream()
                .filter(register -> register.getTeamId() != null)
                .collect(Collectors.toList());
        if (teamRegisters.isEmpty())
        {
            throw new ServiceException("当前赛事没有可分配的参赛队伍");
        }
        for (CompetitionRegister register : teamRegisters)
        {
            ensureParticipationRecord(register.getRegisterId());
        }

        Map<String, CompetitionReviewAssignment> reviewedAssignments = buildReviewedAssignmentMap(competitionId, teamRegisters, operatorId);
        if (targetParticipationIds == null)
        {
            competitionReviewAssignmentMapper.deletePendingByCompetitionId(competitionId);
        }
        else
        {
            for (Long participationId : targetParticipationIds)
            {
                competitionReviewAssignmentMapper.deletePendingByParticipationId(participationId);
            }
        }

        Map<Long, Integer> reviewerLoadMap = new HashMap<>();
        for (CompetitionReviewAssignment assignment : competitionReviewAssignmentMapper.selectByCompetitionId(competitionId))
        {
            reviewerLoadMap.merge(assignment.getReviewerId(), 1, Integer::sum);
        }

        List<Map<String, Object>> skippedTeams = new ArrayList<>();
        int successTeams = 0;
        Random random = new Random();
        Date now = new Date();

        for (CompetitionRegister register : teamRegisters)
        {
            if (targetParticipationIds != null && !targetParticipationIds.contains(register.getRegisterId()))
            {
                continue;
            }
            List<CompetitionReviewAssignment> fixedAssignments = reviewedAssignments.values().stream()
                    .filter(item -> register.getRegisterId().equals(item.getParticipationId()))
                    .collect(Collectors.toList());

            Set<Long> chosenReviewerIds = fixedAssignments.stream()
                    .map(CompetitionReviewAssignment::getReviewerId)
                    .collect(Collectors.toCollection(HashSet::new));
            Set<Long> teamAdvisorUserIds = getAdvisorUserIdsByTeamId(register.getTeamId());

            List<SysUser> candidates = reviewerPool.values().stream()
                    .filter(item -> !teamAdvisorUserIds.contains(item.getUserId()))
                    .filter(item -> !chosenReviewerIds.contains(item.getUserId()))
                    .collect(Collectors.toList());

            int remainingNeeded = reviewerCountPerTeam - fixedAssignments.size();
            if (remainingNeeded < 0)
            {
                remainingNeeded = 0;
            }
            if (candidates.size() < remainingNeeded)
            {
                Map<String, Object> skipItem = new HashMap<>();
                skipItem.put("teamId", register.getTeamId());
                skipItem.put("teamName", register.getTeamName());
                skipItem.put("reason", "候选老师不足");
                skipItem.put("eligibleReviewerCount", candidates.size() + fixedAssignments.size());
                skippedTeams.add(skipItem);
                continue;
            }

            List<SysUser> selectedCandidates = pickBalancedCandidates(candidates, reviewerLoadMap, remainingNeeded, random);
            for (SysUser reviewer : selectedCandidates)
            {
                insertAssignment(competitionId, register.getRegisterId(), register.getTeamId(), reviewer, ASSIGNED, operatorId, now);
            }
            successTeams++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("reviewerCountPerTeam", reviewerCountPerTeam);
        result.put("successTeamCount", successTeams);
        result.put("skippedTeamCount", skippedTeams.size());
        result.put("skippedTeams", skippedTeams);
        result.put("affectedTeamCount", targetParticipationIds == null ? teamRegisters.size() : targetParticipationIds.size());
        result.put("message", skippedTeams.isEmpty() ? "分配完成" : "部分队伍因候选老师不足被跳过");
        return result;
    }

    private Map<String, CompetitionReviewAssignment> buildReviewedAssignmentMap(Long competitionId,
            List<CompetitionRegister> registers, Long operatorId)
    {
        Map<String, CompetitionReviewAssignment> reviewedAssignments = competitionReviewAssignmentMapper.selectByCompetitionId(competitionId)
                .stream()
                .filter(item -> REVIEWED.equals(item.getAssignmentStatus()))
                .collect(Collectors.toMap(item -> buildAssignmentKey(item.getParticipationId(), item.getReviewerId()), item -> item,
                        (a, b) -> a, LinkedHashMap::new));

        Date now = new Date();
        for (CompetitionRegister register : registers)
        {
            List<CompetitionScore> scores = competitionScoreMapper.selectByParticipationId(register.getRegisterId());
            for (CompetitionScore score : scores)
            {
                if (score.getReviewerId() == null)
                {
                    continue;
                }
                String key = buildAssignmentKey(register.getRegisterId(), score.getReviewerId());
                if (reviewedAssignments.containsKey(key))
                {
                    continue;
                }
                SysUser reviewer = sysUserMapper.selectUserById(score.getReviewerId());
                if (reviewer == null)
                {
                    continue;
                }
                CompetitionReviewAssignment created = insertAssignment(competitionId, register.getRegisterId(), register.getTeamId(),
                        reviewer, REVIEWED, operatorId, now);
                reviewedAssignments.put(key, created);
            }
        }
        return reviewedAssignments;
    }

    private CompetitionReviewAssignment insertAssignment(Long competitionId, Long participationId, Long teamId, SysUser reviewer,
            String status, Long operatorId, Date now)
    {
        CompetitionReviewAssignment existing = competitionReviewAssignmentMapper
                .selectByParticipationAndReviewer(participationId, reviewer.getUserId());
        if (existing != null)
        {
            existing.setReviewerName(getDisplayName(reviewer));
            existing.setAssignmentStatus(status);
            existing.setAssignedBy(operatorId);
            existing.setAssignedTime(now);
            existing.setUpdateTime(now);
            competitionReviewAssignmentMapper.updateAssignment(existing);
            return existing;
        }

        CompetitionReviewAssignment assignment = new CompetitionReviewAssignment();
        assignment.setCompetitionId(competitionId);
        assignment.setParticipationId(participationId);
        assignment.setTeamId(teamId);
        assignment.setReviewerId(reviewer.getUserId());
        assignment.setReviewerName(getDisplayName(reviewer));
        assignment.setAssignmentStatus(status);
        assignment.setAssignedBy(operatorId);
        assignment.setAssignedTime(now);
        assignment.setUpdateTime(now);
        competitionReviewAssignmentMapper.insertAssignment(assignment);
        return assignment;
    }

    private void ensureParticipationRecord(Long participationId)
    {
        if (participationId == null || competitionParticipationMapper.selectById(participationId) != null)
        {
            return;
        }
        CompetitionParticipation participation = new CompetitionParticipation();
        participation.setParticipationId(participationId);
        participation.setParticipationStatus("未提交");
        competitionParticipationMapper.insertCompetitionParticipation(participation);
    }

    private void attachAssignmentInfo(Long competitionId, List<Map<String, Object>> teams)
    {
        Map<Long, SysUser> reviewerPool = listCompetitionReviewers(competitionId);
        List<CompetitionReviewAssignment> competitionAssignments = competitionReviewAssignmentMapper.selectByCompetitionId(competitionId);
        Map<Long, SysUser> reviewerById = loadUsersByIds(competitionAssignments.stream()
                .map(CompetitionReviewAssignment::getReviewerId)
                .collect(Collectors.toSet()));
        Set<Long> teamIds = teams.stream()
                .map(team -> toLong(team.get("team_id")))
                .filter(item -> item != null)
                .collect(Collectors.toSet());
        Map<Long, Set<Long>> advisorUserIdsByTeam = loadAdvisorUserIdsByTeams(teamIds);
        Map<Long, String> advisorLabelByTeam = loadAdvisorLabelsByTeams(teamIds);
        Map<Long, List<CompetitionReviewAssignment>> assignmentByParticipation = competitionAssignments.stream()
                .collect(Collectors.groupingBy(CompetitionReviewAssignment::getParticipationId));

        int index = 1;
        for (Map<String, Object> team : teams)
        {
            Long participationId = toLong(team.get("register_id"));
            Long teamId = toLong(team.get("team_id"));
            team.put("sortNo", index++);

            List<CompetitionReviewAssignment> assignments = assignmentByParticipation.getOrDefault(participationId, Collections.emptyList());
            Set<Long> teamAdvisorUserIds = advisorUserIdsByTeam.getOrDefault(teamId, Collections.emptySet());
            assignments = assignments.stream()
                    .filter(item -> !teamAdvisorUserIds.contains(item.getReviewerId()))
                    .collect(Collectors.toList());
            team.put("assignedReviewers", buildAssignedReviewerItems(assignments, reviewerById));

            String advisorNames = advisorLabelByTeam.get(teamId);
            team.put("advisorNames", StringUtils.isEmpty(advisorNames) ? team.get("teacher_name") : advisorNames);

            int eligibleReviewerCount = (int) reviewerPool.values().stream()
                    .filter(item -> !teamAdvisorUserIds.contains(item.getUserId()))
                    .count();
            team.put("eligibleReviewerCount", eligibleReviewerCount);
            team.put("canAutoAssign", eligibleReviewerCount > 0);
            team.put("advisorUserIds", new ArrayList<>(teamAdvisorUserIds));
        }
    }

    private List<Map<String, Object>> buildAssignedReviewerItems(List<CompetitionReviewAssignment> assignments,
            Map<Long, SysUser> reviewerById)
    {
        List<Map<String, Object>> items = new ArrayList<>();
        for (CompetitionReviewAssignment assignment : assignments)
        {
            SysUser reviewer = reviewerById.get(assignment.getReviewerId());
            Map<String, Object> item = new HashMap<>();
            item.put("reviewerId", assignment.getReviewerId());
            item.put("reviewerName", assignment.getReviewerName());
            item.put("phone", reviewer != null ? reviewer.getPhonenumber() : null);
            item.put("reviewed", REVIEWED.equals(assignment.getAssignmentStatus()));
            item.put("assignmentStatus", assignment.getAssignmentStatus());
            items.add(item);
        }
        items.sort(Comparator.comparing(it -> String.valueOf(it.get("reviewerName"))));
        return items;
    }

    private Integer inferReviewerCountPerTeam(List<CompetitionReviewAssignment> assignments)
    {
        Map<Long, Long> countMap = assignments.stream()
                .collect(Collectors.groupingBy(CompetitionReviewAssignment::getParticipationId, Collectors.counting()));
        long max = 0L;
        for (Long value : countMap.values())
        {
            if (value != null && value > max)
            {
                max = value;
            }
        }
        return max <= 0 ? null : Long.valueOf(max).intValue();
    }

    private List<SysUser> pickBalancedCandidates(List<SysUser> candidates, Map<Long, Integer> reviewerLoadMap, int count, Random random)
    {
        List<SysUser> remaining = new ArrayList<>(candidates);
        List<SysUser> selected = new ArrayList<>();
        for (int i = 0; i < count; i++)
        {
            remaining.sort((a, b) -> {
                int diff = reviewerLoadMap.getOrDefault(a.getUserId(), 0) - reviewerLoadMap.getOrDefault(b.getUserId(), 0);
                if (diff != 0)
                {
                    return diff;
                }
                return a.getUserId().compareTo(b.getUserId());
            });

            int minLoad = reviewerLoadMap.getOrDefault(remaining.get(0).getUserId(), 0);
            List<SysUser> bucket = remaining.stream()
                    .filter(item -> reviewerLoadMap.getOrDefault(item.getUserId(), 0) == minLoad)
                    .collect(Collectors.toList());
            SysUser picked = bucket.get(random.nextInt(bucket.size()));
            selected.add(picked);
            remaining.removeIf(item -> item.getUserId().equals(picked.getUserId()));
            reviewerLoadMap.merge(picked.getUserId(), 1, Integer::sum);
        }
        return selected;
    }

    private List<CompetitionRegister> uniqueTeamRegisters(Long competitionId)
    {
        Map<Long, CompetitionRegister> uniqueTeams = new LinkedHashMap<>();
        for (CompetitionRegister register : competitionRegisterMapper.selectByCompetitionId(competitionId))
        {
            if (register.getTeamId() != null && !uniqueTeams.containsKey(register.getTeamId()))
            {
                uniqueTeams.put(register.getTeamId(), register);
            }
        }
        return new ArrayList<>(uniqueTeams.values());
    }

    private void backfillMissingWorkInfo(List<Map<String, Object>> teams)
    {
        if (teams == null || teams.isEmpty())
        {
            return;
        }
        Map<Long, String[]> generatedByTeam = new HashMap<>();
        for (Map<String, Object> team : teams)
        {
            if (team == null)
            {
                continue;
            }
            String currentName = toStringValue(team.get("work_name"));
            String currentDesc = toStringValue(team.get("work_description"));
            if (hasActualWorkInfo(currentName, currentDesc))
            {
                continue;
            }
            Long teamId = toLongValue(team.get("team_id"));
            String[] generated = teamId == null ? buildGeneratedWorkInfo(team) : generatedByTeam.computeIfAbsent(teamId, key -> buildGeneratedWorkInfo(team));
            team.put("work_name", generated[0]);
            team.put("work_description", generated[1]);
            if (teamId != null)
            {
                competitionRegisterMapper.updateTeamProfileByTeamId(teamId, generated[0], generated[1], toStringValue(team.get("teacher_name")));
            }
        }
    }

    private CompetitionRegister ensureWorkInfo(CompetitionRegister detail)
    {
        if (detail == null || hasActualWorkInfo(detail.getWorkName(), detail.getWorkDescription()))
        {
            return detail;
        }
        Map<String, Object> team = new HashMap<>();
        team.put("team_name", detail.getTeamName());
        team.put("team_members", detail.getTeamMembers());
        team.put("teacher_name", detail.getTeacherName());
        team.put("team_id", detail.getTeamId());
        String[] generated = buildGeneratedWorkInfo(team);
        detail.setWorkName(generated[0]);
        detail.setWorkDescription(generated[1]);
        if (detail.getTeamId() != null)
        {
            competitionRegisterMapper.updateTeamProfileByTeamId(detail.getTeamId(), generated[0], generated[1], detail.getTeacherName());
        }
        else if (detail.getRegisterId() != null)
        {
            competitionRegisterMapper.updateCompetitionRegister(detail);
        }
        return detail;
    }

    private boolean hasActualWorkInfo(String workName, String workDescription)
    {
        if (isLegacyGenericWorkInfo(workName, workDescription))
        {
            return false;
        }
        return StringUtils.isNotEmpty(workName)
                && !"无".equals(workName)
                && StringUtils.isNotEmpty(workDescription)
                && !"无".equals(workDescription);
    }

    private boolean isLegacyGenericWorkInfo(String workName, String workDescription)
    {
        return "创新实践助手".equals(workName)
                && "一款服务校园实践项目管理的轻量化应用，可辅助团队记录过程、整理资料和展示成果。".equals(workDescription);
    }

    private String toStringValue(Object value)
    {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String[] buildGeneratedWorkInfo(Map<String, Object> team)
    {
        String teamName = toStringValue(team.get("team_name"));
        String teacherName = toStringValue(team.get("teacher_name"));
        String memberText = toStringValue(team.get("team_members"));
        String context = (teamName + " " + teacherName + " " + memberText).toLowerCase();
        if (containsAny(context, "海洋", "全域水", "水眸", "近岸", "观测"))
        {
            return new String[] {
                    "蓝海数据浮标",
                    "一套用于近岸环境监测的数据采集终端，可实时记录水温、盐度等基础信息。"
            };
        }
        if (containsAny(context, "生物", "生命", "实验室"))
        {
            return new String[] {
                    "实验室预约管家",
                    "用于实验室设备预约、使用登记和排期管理的小型服务系统，方便日常教学安排。"
            };
        }
        if (containsAny(context, "经济", "金融", "商赛", "创业"))
        {
            return new String[] {
                    "商赛决策沙盘",
                    "基于数据看板与情景推演的商业决策训练平台，帮助团队快速验证经营策略。"
            };
        }
        if (containsAny(context, "外国语", "英语", "翻译", "语言"))
        {
            return new String[] {
                    "跨语种智能翻译箱",
                    "面向多语沟通场景的便携式翻译工具，可提供基础语音识别与术语辅助。"
            };
        }
        if (containsAny(context, "安全", "网络", "巡检", "信息安全"))
        {
            return new String[] {
                    "智巡校园安防系统",
                    "结合图像识别与任务派发的校园巡检工具，用于发现并上报常见安全隐患。"
            };
        }
        if (containsAny(context, "物理", "电子", "实验"))
        {
            return new String[] {
                    "星图实验助手",
                    "面向实验教学场景的智能辅助系统，可帮助学生完成实验数据记录、结果分析和过程提醒。"
            };
        }
        if (containsAny(context, "测试", "演练", "验证"))
        {
            return selectByFingerprint(context,
                    new String[] {
                            "流程联调演练台",
                            "竞赛报名演示助手",
                            "多角色协同验证台",
                            "赛事流程模拟器"
                    },
                    new String[] {
                            "用于验证报名、审核和资料提交流程的演练系统，方便团队快速检查整体链路。",
                            "面向赛事流程测试场景的演示工具，可辅助检查队伍信息、材料与状态展示。",
                            "支持学生、老师与管理员协同验证的测试平台，用于排查赛事流程中的关键问题。",
                            "一套用于模拟报名、提交和评分流程的验证工具，便于开展功能联调与演示。"
                    });
        }
        if (containsAny(context, "善", "希望", "成长", "学习", "陪护", "机器人"))
        {
            return selectByFingerprint(context,
                    new String[] {
                            "智学成长伙伴",
                            "希望号学习助手",
                            "至善成长引擎",
                            "陪伴式学习机器人"
                    },
                    new String[] {
                            "围绕学习陪伴与任务提醒设计的智能助手，帮助学生完成日常学习规划和反馈记录。",
                            "一套面向校园学习场景的智能辅助工具，可提供任务管理、提醒和简单问答支持。",
                            "聚焦学生成长记录与学习管理的轻量化系统，便于团队开展校园服务实践。",
                            "面向学习陪伴场景的智能机器人方案，可提供基础答疑、提醒和互动辅助功能。"
                    });
        }
        if (containsAny(context, "校园", "管理", "预约", "服务", "队", "项目"))
        {
            return selectByFingerprint(context,
                    new String[] {
                            "慧校园事务助手",
                            "轻量项目协作台",
                            "智联服务中枢",
                            "校园流程管家",
                            "实践项目协同端",
                            "任务进度导航台"
                    },
                    new String[] {
                            "服务校园事务处理与流程跟踪的智能工具，可帮助团队统一记录任务与阶段成果。",
                            "一套面向学生项目协作的小型平台，用于整理资料、分配任务和展示进度。",
                            "聚焦校园服务流程整合的实践项目，可提升信息收集、协作和结果反馈效率。",
                            "用于处理预约、登记和提醒等校园事务的轻量化系统，适合日常服务场景使用。",
                            "面向竞赛实践项目协同的应用工具，可辅助开展团队分工、资料汇总与节点跟踪。",
                            "聚焦任务拆解与进度可视化的团队辅助工具，用于提升项目执行效率。"
                    });
        }
        return selectByFingerprint(context,
                new String[] {
                        "创想任务工坊",
                        "协同创研平台",
                        "灵犀实践中台",
                        "启明项目助手",
                        "聚智协作终端",
                        "星火方案工坊",
                        "创行执行助手",
                        "青创智联台"
                },
                new String[] {
                        "围绕团队实践任务管理与资料沉淀设计的辅助平台，可支持过程记录和成果展示。",
                        "一套服务项目协作与进度推进的轻量化系统，便于团队开展校园实践与创新尝试。",
                        "面向团队协同和任务跟踪的实践工具，可帮助成员统一整理资料并推进节点执行。",
                        "用于项目过程管理、阶段汇报和成果展示的辅助应用，适合校园创新实践场景。",
                        "聚焦成员协作、信息整合与任务分发的团队工具，可提升项目执行与沟通效率。",
                        "服务方案设计、过程管理和材料整理的校园实践平台，适合多成员协同使用。",
                        "面向创新项目推进与任务跟踪的应用工具，可协助团队完成资料整理和流程管理。",
                        "一套支持项目协同、过程留痕与成果管理的轻量化实践平台，适用于赛事团队使用。"
                });
    }

    private boolean containsAny(String text, String... keywords)
    {
        if (text == null || keywords == null)
        {
            return false;
        }
        for (String keyword : keywords)
        {
            if (keyword != null && text.contains(keyword.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }

    private String[] selectByFingerprint(String seed, String[] names, String[] descriptions)
    {
        if (names == null || descriptions == null || names.length == 0 || names.length != descriptions.length)
        {
            return new String[] { "创新作品", "用于展示团队实践成果的简短作品说明。" };
        }
        int index = Math.abs((seed == null ? "" : seed).hashCode()) % names.length;
        return new String[] { names[index], descriptions[index] };
    }

    private Long toLongValue(Object value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            return Long.valueOf(String.valueOf(value));
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private Map<Long, SysUser> listCompetitionReviewers(Long competitionId)
    {
        Map<Long, SysUser> reviewers = new LinkedHashMap<>();
        List<CompetitionPermission> permissions = competitionPermissionMapper.selectByCompetitionId(competitionId);
        Map<Long, SysUser> usersById = loadUsersByIds(permissions.stream()
                .filter(permission -> permission.getPermissionId() != null && permission.getPermissionId() == 1)
                .map(CompetitionPermission::getUserId)
                .collect(Collectors.toSet()));
        for (CompetitionPermission permission : permissions)
        {
            if (permission.getPermissionId() == null || permission.getPermissionId() != 1)
            {
                continue;
            }
            SysUser user = usersById.get(permission.getUserId());
            if (user != null)
            {
                reviewers.put(user.getUserId(), user);
            }
        }
        return reviewers;
    }

    private void sanitizeCompetitionAdvisorReviewers(Long competitionId)
    {
        Set<Long> advisorUserIds = loadCompetitionAdvisorUserIds(competitionId);
        if (advisorUserIds.isEmpty())
        {
            return;
        }

        for (Long advisorUserId : advisorUserIds)
        {
            competitionPermissionMapper.deletePermission(advisorUserId, competitionId);
        }

        List<CompetitionReviewAssignment> assignments = competitionReviewAssignmentMapper.selectByCompetitionId(competitionId);
        for (CompetitionReviewAssignment assignment : assignments)
        {
            if (advisorUserIds.contains(assignment.getReviewerId()))
            {
                competitionReviewAssignmentMapper.deleteById(assignment.getId());
            }
        }

        for (CompetitionRegister register : competitionRegisterMapper.selectByCompetitionId(competitionId))
        {
            if (register.getRegisterId() == null)
            {
                continue;
            }
            List<CompetitionScore> scores = competitionScoreMapper.selectByParticipationId(register.getRegisterId());
            for (CompetitionScore score : scores)
            {
                if (score.getReviewerId() != null && advisorUserIds.contains(score.getReviewerId()))
                {
                    competitionScoreMapper.deleteById(score.getScoreId());
                }
            }
        }
    }

    private List<SysUser> listAllTeacherUsers()
    {
        SysUser queryUser = new SysUser();
        queryUser.setDelFlag("0");
        List<SysUser> users = sysUserMapper.selectUserList(queryUser);
        List<SysUser> teachers = new ArrayList<>();
        for (SysUser user : users)
        {
            if (user.getUserId() == null)
            {
                continue;
            }
            List<SysRole> roles = roleMapper.selectRolePermissionByUserId(user.getUserId());
            if (roles == null || roles.isEmpty())
            {
                continue;
            }
            boolean isTeacher = roles.stream().anyMatch(role -> "teacher".equals(role.getRoleKey()));
            if (isTeacher)
            {
                teachers.add(user);
            }
        }
        return teachers;
    }

    private Set<Long> loadCompetitionAdvisorUserIds(Long competitionId)
    {
        Set<Long> teamIds = uniqueTeamRegisters(competitionId).stream()
                .map(CompetitionRegister::getTeamId)
                .filter(item -> item != null)
                .collect(Collectors.toSet());
        if (teamIds.isEmpty())
        {
            return Collections.emptySet();
        }
        return loadAdvisorUserIdsByTeams(teamIds).values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private Set<Long> getAdvisorUserIdsByTeamId(Long teamId)
    {
        return loadAdvisorUserIdsByTeams(Collections.singleton(teamId)).getOrDefault(teamId, Collections.emptySet());
    }

    private Map<Long, Set<Long>> loadAdvisorUserIdsByTeams(Set<Long> teamIds)
    {
        if (teamIds == null || teamIds.isEmpty())
        {
            return Collections.emptyMap();
        }
        List<TeacherTeamRel> relations = teacherTeamRelMapper.selectByTeamIds(new ArrayList<>(teamIds));
        if (relations == null || relations.isEmpty())
        {
            return Collections.emptyMap();
        }

        List<Long> teacherIds = relations.stream()
                .map(TeacherTeamRel::getTeacherId)
                .filter(item -> item != null)
                .distinct()
                .collect(Collectors.toList());
        if (teacherIds.isEmpty())
        {
            return Collections.emptyMap();
        }

        List<TeacherTeam> teachers = teacherTeamMapper.selectBaseTeachersByIds(teacherIds);
        Map<Long, Long> userIdByTeacherId = new HashMap<>();
        for (TeacherTeam teacher : teachers)
        {
            if (StringUtils.isEmpty(teacher.getPhone()))
            {
                continue;
            }
            SysUser user = sysUserMapper.selectUserByPhone(teacher.getPhone());
            if (user != null && user.getUserId() != null)
            {
                userIdByTeacherId.put(teacher.getId(), user.getUserId());
            }
        }
        Map<Long, Set<Long>> result = new HashMap<>();
        for (TeacherTeamRel relation : relations)
        {
            Long userId = userIdByTeacherId.get(relation.getTeacherId());
            if (userId == null)
            {
                continue;
            }
            result.computeIfAbsent(relation.getTeamId(), key -> new HashSet<>()).add(userId);
        }
        return result;
    }

    private String buildAdvisorLabel(Long teamId)
    {
        return loadAdvisorLabelsByTeams(Collections.singleton(teamId)).get(teamId);
    }

    private Map<Long, String> loadAdvisorLabelsByTeams(Set<Long> teamIds)
    {
        if (teamIds == null || teamIds.isEmpty())
        {
            return Collections.emptyMap();
        }
        List<TeacherTeamRel> relations = teacherTeamRelMapper.selectByTeamIds(new ArrayList<>(teamIds));
        if (relations == null || relations.isEmpty())
        {
            return Collections.emptyMap();
        }

        List<Long> teacherIds = relations.stream()
                .map(TeacherTeamRel::getTeacherId)
                .filter(item -> item != null)
                .distinct()
                .collect(Collectors.toList());
        if (teacherIds.isEmpty())
        {
            return Collections.emptyMap();
        }

        List<TeacherTeam> teachers = teacherTeamMapper.selectBaseTeachersByIds(teacherIds);
        Map<Long, String> teacherNameById = teachers.stream().collect(Collectors.toMap(TeacherTeam::getId,
                item -> !StringUtils.isEmpty(item.getTeacherName()) ? item.getTeacherName() : item.getPhone(),
                (a, b) -> a));
        Map<Long, List<String>> namesByTeam = new LinkedHashMap<>();
        for (TeacherTeamRel relation : relations)
        {
            String name = teacherNameById.get(relation.getTeacherId());
            if (StringUtils.isEmpty(name))
            {
                continue;
            }
            namesByTeam.computeIfAbsent(relation.getTeamId(), key -> new ArrayList<>()).add(name);
        }
        Map<Long, String> result = new HashMap<>();
        for (Map.Entry<Long, List<String>> entry : namesByTeam.entrySet())
        {
            result.put(entry.getKey(), String.join(" / ", entry.getValue()));
        }
        return result;
    }

    private Map<Long, SysUser> loadUsersByIds(Set<Long> userIds)
    {
        if (userIds == null || userIds.isEmpty())
        {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectUsersByIds(new ArrayList<>(userIds)).stream()
                .collect(Collectors.toMap(SysUser::getUserId, user -> user, (a, b) -> a));
    }

    private void ensureCompetitionExists(Long competitionId)
    {
        if (competitionId == null || competitionMapper.selectCompetitionById(competitionId) == null)
        {
            throw new ServiceException("赛事不存在");
        }
    }

    private String getDisplayName(SysUser user)
    {
        if (user == null)
        {
            return null;
        }
        return StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : user.getUserName();
    }

    private Long toLong(Object value)
    {
        if (value == null)
        {
            return null;
        }
        if (value instanceof Long)
        {
            return (Long) value;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private String buildAssignmentKey(Long participationId, Long reviewerId)
    {
        return participationId + ":" + reviewerId;
    }
}
