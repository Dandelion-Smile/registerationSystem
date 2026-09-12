package org.iflytek.system.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.iflytek.common.exception.ServiceException;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionParticipation;
import org.iflytek.system.domain.CompetitionPermission;
import org.iflytek.system.domain.CompetitionRegister;
import org.iflytek.system.domain.CompetitionReviewAssignment;
import org.iflytek.system.domain.CompetitionScore;
import org.iflytek.system.domain.CompetitionWork;
import org.iflytek.common.core.domain.entity.SysUser;
import org.iflytek.system.domain.TeacherTeam;
import org.iflytek.system.domain.TeacherTeamRel;
import org.iflytek.system.mapper.CompetitionMapper;
import org.iflytek.system.mapper.CompetitionParticipationMapper;
import org.iflytek.system.mapper.CompetitionPermissionMapper;
import org.iflytek.system.mapper.CompetitionRegisterMapper;
import org.iflytek.system.mapper.CompetitionReviewAssignmentMapper;
import org.iflytek.system.mapper.CompetitionScoreMapper;
import org.iflytek.system.mapper.CompetitionWorkMapper;
import org.iflytek.system.mapper.SysUserMapper;
import org.iflytek.system.mapper.TeacherTeamMapper;
import org.iflytek.system.mapper.TeacherTeamRelMapper;
import org.iflytek.system.service.ITeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Teacher-side review service implementation.
 */
@Service
public class TeacherServiceImpl implements ITeacherService {

    Logger log = LoggerFactory.getLogger(TeacherServiceImpl.class.getName());
    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private CompetitionPermissionMapper permissionMapper;

    @Autowired
    private CompetitionRegisterMapper registerMapper;

    @Autowired
    private CompetitionParticipationMapper participationMapper;

    @Autowired
    private CompetitionScoreMapper scoreMapper;

    @Autowired
    private CompetitionReviewAssignmentMapper assignmentMapper;

    @Autowired
    private CompetitionWorkMapper competitionWorkMapper;

    @Autowired
    private TeacherTeamMapper teacherTeamMapper;

    @Autowired
    private TeacherTeamRelMapper teacherTeamRelMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<Competition> listPublicCompetitions(String keyword, String status) {
        return competitionMapper.selectCompetitionListWithFilters(keyword, status);
    }

    @Override
    public Map<String, Object> getCompetitionStats() {
        List<Competition> allCompetitions = competitionMapper.selectCompetitionList();

        Date now = new Date();
        int totalCount = allCompetitions.size();
        int inProgressCount = 0;
        int endingSoonCount = 0;

        for (Competition comp : allCompetitions) {
            Date startTime = comp.getRegisterStartTime();
            Date endTime = comp.getRegisterEndTime();

            if (now.compareTo(startTime) >= 0 && now.compareTo(endTime) <= 0) {
                inProgressCount++;
                long hoursLeft = (endTime.getTime() - now.getTime()) / (1000 * 60 * 60);
                if (hoursLeft > 0 && hoursLeft <= 24) {
                    endingSoonCount++;
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCompetitions", totalCount);
        stats.put("inProgress", inProgressCount);
        stats.put("endingSoon", endingSoonCount);
        stats.put("avgParticipation", 75);
        stats.put("avgScore", 72.5);
        stats.put("competitionIncrease", 15);
        stats.put("participationIncrease", 3);
        stats.put("scoreDecrease", 1.5);

        return stats;
    }

    @Override
    public Competition getCompetitionDetail(Long competitionId) {
        return competitionMapper.selectCompetitionById(competitionId);
    }

    @Override
    public List<Map<String, Object>> getReviewableCompetitions(Long teacherId) {
        List<Long> competitionIds = permissionMapper.selectCompetitionIdsByUser(teacherId);

        if (competitionIds == null || competitionIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Long competitionId : competitionIds) {
            Competition competition = competitionMapper.selectCompetitionById(competitionId);
            if (competition == null) {
                continue;
            }

            Map<String, Object> stats = getReviewStats(competitionId, teacherId);

            Map<String, Object> item = new HashMap<>();
            item.put("competitionId", competition.getCompetitionId());
            item.put("competitionName", competition.getCompetitionName());
            item.put("competitionType", competition.getCompetitionType());
            item.put("registerStartTime", competition.getRegisterStartTime());
            item.put("registerEndTime", competition.getRegisterEndTime());
            item.put("description", competition.getDescription());
            item.put("totalTeams", stats.get("totalTeams"));
            item.put("reviewedCount", stats.get("reviewedCount"));

            result.add(item);
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getReviewTeams(Long competitionId, Long teacherId) {
        ensureCompetitionReviewAccess(competitionId, teacherId, "You do not have review permission for this competition");

        List<Map<String, Object>> result = new ArrayList<>();
        List<CompetitionReviewAssignment> assignments = assignmentMapper.selectByReviewerAndCompetition(teacherId, competitionId);
        for (CompetitionReviewAssignment assignment : assignments) {
            CompetitionRegister register = registerMapper.selectById(assignment.getParticipationId());
            if (register == null || register.getTeamId() == null) {
                continue;
            }
            CompetitionParticipation participation = participationMapper.selectById(register.getRegisterId());
            List<CompetitionScore> scores = scoreMapper.selectByParticipationId(register.getRegisterId());

            BigDecimal myScore = null;
            boolean reviewed = REVIEWED.equals(assignment.getAssignmentStatus());
            for (CompetitionScore s : scores) {
                if (s.getReviewerId().equals(teacherId)) {
                    reviewed = true;
                    myScore = s.getScore();
                    break;
                }
            }

            int memberCount = countTeamMembers(register.getTeamMembers());
            String advisor = buildAdvisorLabel(register.getTeamId());

            Map<String, Object> item = new HashMap<>();
            item.put("teamId", register.getTeamId());
            item.put("teamName", register.getTeamName());
            item.put("registerId", register.getRegisterId());
            item.put("participationId", register.getRegisterId());
            item.put("assignmentId", assignment.getId());
            item.put("registerTime", register.getRegisterTime());
            item.put("teamMembers", register.getTeamMembers());
            item.put("memberCount", memberCount);
            item.put("advisor", advisor);
            item.put("reviewed", reviewed);
            item.put("score", myScore);
            item.put("submitTime", participation != null ? participation.getSubmitTime() : null);
            item.put("hasMaterials", participation != null &&
                    (participation.getPptPath() != null || participation.getPdfPath() != null));

            result.add(item);
        }

        result.sort((a, b) -> {
            Date t1 = (Date) a.get("registerTime");
            Date t2 = (Date) b.get("registerTime");
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t1.compareTo(t2);
        });

        return result;
    }

    @Override
    public Map<String, Object> getReviewTeamDetail(Long competitionId, Long participationId, Long teacherId) {
        CompetitionRegister register = registerMapper.selectById(participationId);
        if (register == null) {
            throw new ServiceException("This team is not registered for the competition");
        }
        if (!competitionId.equals(register.getCompetitionId())) {
            throw new ServiceException("This team is not registered for the competition");
        }

        boolean isAdvisor = isTeamAdvisor(getTeacherProfileIdsByUserId(teacherId), register.getTeamId());
        CompetitionPermission permission = permissionMapper.selectByUserAndCompetition(teacherId, competitionId);
        boolean hasReviewAccess = permission != null && permission.getPermissionId() == 1;
        CompetitionReviewAssignment assignment = assignmentMapper.selectByParticipationAndReviewer(register.getRegisterId(), teacherId);
        boolean isAssignedReviewer = hasReviewAccess && assignment != null;
        if (!isAdvisor && !isAssignedReviewer) {
            throw new ServiceException("This team is not assigned to you");
        }

        CompetitionParticipation participation = participationMapper.selectById(register.getRegisterId());

        Map<String, Object> result = new HashMap<>();
        result.put("teamId", register.getTeamId());
        result.put("teamName", register.getTeamName());
        Competition competition = competitionMapper.selectCompetitionById(competitionId);
        result.put("competitionName", competition != null ? competition.getCompetitionName() : null);
        result.put("registerId", register.getRegisterId());
        result.put("assignmentId", assignment != null ? assignment.getId() : null);
        result.put("canReview", isAssignedReviewer && !isAdvisor);
        result.put("isAdvisor", isAdvisor);
        result.put("registerTime", register.getRegisterTime());
        
        String teamMembersJson = register.getTeamMembers();
        String synchronizedTeamMembers = syncTeamMembersWithUserTable(teamMembersJson);
        result.put("teamMembers", synchronizedTeamMembers);
        result.put("memberCount", countTeamMembers(synchronizedTeamMembers));
        result.put("advisor", buildAdvisorLabel(register.getTeamId()));
        result.put("pptPath", participation != null ? participation.getPptPath() : null);
        result.put("pdfPath", participation != null ? participation.getPdfPath() : null);
        result.put("submitTime", participation != null ? participation.getSubmitTime() : null);
        List<CompetitionScore> scores = scoreMapper.selectByParticipationId(register.getRegisterId());
        result.put("finalScore", resolveDisplayFinalScore(participation, scores));
        result.put("submissionStatus", participation != null && participation.getSubmitTime() != null ? "已提交" : "未提交");

        List<CompetitionWork> works = loadParticipationWorks(register.getRegisterId());
        result.put("files", buildFileResponses(works));
        result.put("hasMaterials", hasMaterials(participation, works));

        return result;
    }

    @Override
    public List<Map<String, Object>> getTeamScores(Long participationId, Long teacherId) {
        CompetitionRegister register = registerMapper.selectById(participationId);
        boolean isAdvisor = register != null && isTeamAdvisor(getTeacherProfileIdsByUserId(teacherId), register.getTeamId());
        List<CompetitionScore> scores = scoreMapper.selectByParticipationId(participationId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (CompetitionScore score : scores) {
            if (!isAdvisor && !teacherId.equals(score.getReviewerId())) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("scoreId", score.getScoreId());
            item.put("reviewerId", score.getReviewerId());
            item.put("reviewerName", score.getReviewerName());
            item.put("score", score.getScore());
            item.put("comment", score.getComment());
            item.put("scoreTime", score.getScoreTime());
            result.add(item);
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitScore(Long participationId, Long teacherId, String teacherNickName, BigDecimal score, String comment) {
        CompetitionParticipation participation = participationMapper.selectById(participationId);
        if (participation == null) {
            throw new ServiceException("Participation record not found");
        }

        CompetitionRegister register = registerMapper.selectById(participationId);
        if (register == null) {
            throw new ServiceException("Registration record not found");
        }
        ensureCompetitionReviewAccess(register.getCompetitionId(), teacherId, "You do not have review permission for this competition");
        if (isTeamAdvisor(getTeacherProfileIdsByUserId(teacherId), register.getTeamId())) {
            throw new ServiceException("You cannot review your own guided team");
        }
        CompetitionReviewAssignment assignment = assignmentMapper.selectByParticipationAndReviewer(participationId, teacherId);
        if (assignment == null) {
            throw new ServiceException("This team is not assigned to you");
        }

        CompetitionScore scoreRecord = new CompetitionScore();
        scoreRecord.setParticipationId(participationId);
        scoreRecord.setReviewerId(teacherId);
        scoreRecord.setReviewerName(teacherNickName);
        scoreRecord.setScore(score);
        scoreRecord.setComment(comment);
        scoreRecord.setScoreTime(new Date());

        scoreMapper.insertScore(scoreRecord);
        assignment.setAssignmentStatus(REVIEWED);
        assignment.setUpdateTime(new Date());
        assignmentMapper.updateAssignment(assignment);

        return scoreRecord.getScoreId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScore(Long scoreId, Long teacherId, BigDecimal score, String comment) {
        CompetitionScore scoreRecord = scoreMapper.selectById(scoreId);
        if (scoreRecord == null) {
            throw new ServiceException("Score record not found");
        }

        if (!scoreRecord.getReviewerId().equals(teacherId)) {
            throw new ServiceException("You can only update your own score");
        }
        CompetitionRegister register = registerMapper.selectById(scoreRecord.getParticipationId());
        if (register != null) {
            ensureCompetitionReviewAccess(register.getCompetitionId(), teacherId, "You do not have review permission for this competition");
            if (isTeamAdvisor(getTeacherProfileIdsByUserId(teacherId), register.getTeamId())) {
                throw new ServiceException("You cannot review your own guided team");
            }
        }
        CompetitionReviewAssignment assignment = assignmentMapper.selectByParticipationAndReviewer(scoreRecord.getParticipationId(), teacherId);
        if (assignment == null) {
            throw new ServiceException("This team is not assigned to you");
        }

        scoreRecord.setScore(score);
        scoreRecord.setComment(comment);
        scoreRecord.setScoreTime(new Date());

        scoreMapper.updateScore(scoreRecord);
        assignment.setAssignmentStatus(REVIEWED);
        assignment.setUpdateTime(new Date());
        assignmentMapper.updateAssignment(assignment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScore(Long scoreId, Long teacherId) {
        CompetitionScore scoreRecord = scoreMapper.selectById(scoreId);
        if (scoreRecord == null) {
            throw new ServiceException("Score record not found");
        }

        if (!scoreRecord.getReviewerId().equals(teacherId)) {
            throw new ServiceException("You can only delete your own score");
        }
        CompetitionRegister register = registerMapper.selectById(scoreRecord.getParticipationId());
        if (register != null) {
            ensureCompetitionReviewAccess(register.getCompetitionId(), teacherId, "You do not have review permission for this competition");
        }
        CompetitionReviewAssignment assignment = assignmentMapper.selectByParticipationAndReviewer(scoreRecord.getParticipationId(), teacherId);

        scoreMapper.deleteById(scoreId);
        if (assignment != null) {
            assignment.setAssignmentStatus(ASSIGNED);
            assignment.setUpdateTime(new Date());
            assignmentMapper.updateAssignment(assignment);
        }
    }

    @Override
    public Map<String, Object> getReviewStats(Long competitionId, Long teacherId) {
        List<CompetitionReviewAssignment> assignments = assignmentMapper.selectByReviewerAndCompetition(teacherId, competitionId);
        int totalTeams = assignments.size();
        int reviewedCount = (int) assignments.stream().filter(item -> REVIEWED.equals(item.getAssignmentStatus())).count();

        Map<String, Object> result = new HashMap<>();
        result.put("totalTeams", totalTeams);
        result.put("reviewedCount", reviewedCount);
        result.put("unreviewedCount", totalTeams - reviewedCount);

        return result;
    }

    @Override
    public List<Map<String, Object>> getMyTeamCompetitions(Long teacherId, String teacherNickName) {
        org.iflytek.common.core.domain.entity.SysUser teacher = sysUserMapper.selectUserById(teacherId);
        if (teacher == null || StringUtils.isEmpty(teacher.getUserName())) {
            return new ArrayList<>();
        }
        List<Long> myTeacherIds = getTeacherProfileIdsByUserName(teacher.getUserName());
        if (myTeacherIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<CompetitionRegister> allTeams = registerMapper.selectAllTeams();
        if (allTeams == null || allTeams.isEmpty()) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        Map<Long, Map<String, Object>> competitionMap = new HashMap<>();
        Set<Long> processedTeams = new HashSet<>();
        List<Long> teamIds = new ArrayList<>();
        for (CompetitionRegister team : allTeams) {
            if (team.getTeamId() != null && !teamIds.contains(team.getTeamId())) {
                teamIds.add(team.getTeamId());
            }
        }

        Map<Long, List<TeacherTeamRel>> teacherByTeamId = new HashMap<>();
        List<TeacherTeamRel> teacherTeams = teamIds.isEmpty() ? new ArrayList<>() : teacherTeamRelMapper.selectByTeamIds(teamIds);
        if (teacherTeams != null) {
            for (TeacherTeamRel row : teacherTeams) {
                teacherByTeamId.computeIfAbsent(row.getTeamId(), key -> new ArrayList<>()).add(row);
            }
        }

        for (CompetitionRegister team : allTeams) {
            if (team.getTeamId() == null || !processedTeams.add(team.getTeamId())) {
                continue;
            }
            List<TeacherTeamRel> advisors = teacherByTeamId.get(team.getTeamId());
            boolean isAdvisor = advisors != null && advisors.stream()
                    .anyMatch(item -> myTeacherIds.contains(item.getTeacherId()));
            if (!isAdvisor) {
                continue;
            }

            CompetitionRegister register = resolvePreferredTeamRegister(team.getCompetitionId(), team.getTeamId());
            if (register == null) {
                continue;
            }

            Competition competition = competitionMapper.selectCompetitionById(register.getCompetitionId());
            if (competition == null) {
                continue;
            }

            Long competitionId = competition.getCompetitionId();
            if (!competitionMap.containsKey(competitionId)) {
                Map<String, Object> compItem = new HashMap<>();
                compItem.put("competitionId", competition.getCompetitionId());
                compItem.put("competitionName", competition.getCompetitionName());
                compItem.put("competitionType", competition.getCompetitionType());
                compItem.put("registerStartTime", competition.getRegisterStartTime());
                compItem.put("registerEndTime", competition.getRegisterEndTime());
                compItem.put("description", competition.getDescription());
                compItem.put("teams", new ArrayList<>());
                competitionMap.put(competitionId, compItem);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> teams = (List<Map<String, Object>>) competitionMap.get(competitionId).get("teams");

            Map<String, Object> leaderInfo = extractLeaderInfo(team.getTeamMembers());
            Map<String, Object> teamItem = new HashMap<>();
            teamItem.put("teamId", team.getTeamId());
            teamItem.put("teamName", team.getTeamName());
            teamItem.put("competitionId", competitionId);
            teamItem.put("competitionName", competition.getCompetitionName());
            teamItem.put("registerId", register.getRegisterId());
            teamItem.put("registerTime", register.getRegisterTime());
            teamItem.put("teamMembers", team.getTeamMembers());
            teamItem.put("advisor", buildAdvisorLabel(team.getTeamId()));
            teamItem.put("leaderName", leaderInfo.get("name"));
            teamItem.put("leaderStudentNo", leaderInfo.get("studentNo"));
            teamItem.put("leaderMajor", leaderInfo.get("major"));

            CompetitionParticipation participation = participationMapper.selectById(register.getRegisterId());
            List<CompetitionWork> works = loadParticipationWorks(register.getRegisterId());
            List<CompetitionScore> scores = scoreMapper.selectByParticipationId(register.getRegisterId());
            boolean canReview = permissionMapper.selectByUserAndCompetition(teacherId, competitionId) != null
                    && !isTeamAdvisor(myTeacherIds, team.getTeamId());
            boolean reviewed = scores.stream()
                    .anyMatch(s -> s.getReviewerId().equals(teacherId));

            if (participation != null) {
                teamItem.put("pptPath", participation.getPptPath());
                teamItem.put("pdfPath", participation.getPdfPath());
                teamItem.put("submitTime", participation.getSubmitTime());
            }
            teamItem.put("finalScore", resolveDisplayFinalScore(participation, scores));
            teamItem.put("files", buildFileResponses(works));
            teamItem.put("workName", resolveWorkName(works));
            teamItem.put("hasMaterials", hasMaterials(participation, works));
            teamItem.put("materialStatus", hasMaterials(participation, works) ? "已上传材料" : "未上传材料");
            teamItem.put("submissionStatus", participation != null && participation.getSubmitTime() != null ? "已提交" : "未提交");
            teamItem.put("canReview", canReview);
            teamItem.put("reviewed", reviewed);

            teams.add(teamItem);
        }

        result.addAll(competitionMap.values());
        return result;
    }

    private CompetitionRegister resolvePreferredTeamRegister(Long competitionId, Long teamId) {
        if (competitionId == null || teamId == null) {
            return null;
        }
        List<CompetitionRegister> teamRegisters = registerMapper.selectByTeamId(teamId);
        if (teamRegisters == null || teamRegisters.isEmpty()) {
            return null;
        }
        CompetitionRegister leaderRegister = null;
        CompetitionRegister submittedRegister = null;
        CompetitionRegister fallbackRegister = null;
        for (CompetitionRegister candidate : teamRegisters) {
            if (candidate == null || candidate.getCompetitionId() == null
                    || !competitionId.equals(candidate.getCompetitionId())) {
                continue;
            }
            if (fallbackRegister == null
                    || (candidate.getRegisterId() != null && fallbackRegister.getRegisterId() != null
                    && candidate.getRegisterId() < fallbackRegister.getRegisterId())) {
                fallbackRegister = candidate;
            }
            if (candidate.getRegisterId() != null && candidate.getRegisterId().equals(teamId)) {
                leaderRegister = candidate;
            }
            CompetitionParticipation participation = participationMapper.selectById(candidate.getRegisterId());
            if (participation != null && participation.getSubmitTime() != null
                    && !StringUtils.isEmpty(participation.getPptPath())
                    && !StringUtils.isEmpty(participation.getPdfPath())) {
                if (submittedRegister == null
                        || (candidate.getRegisterId() != null && submittedRegister.getRegisterId() != null
                        && candidate.getRegisterId() < submittedRegister.getRegisterId())) {
                    submittedRegister = candidate;
                }
            }
        }
        if (submittedRegister != null) {
            return submittedRegister;
        }
        if (leaderRegister != null) {
            return leaderRegister;
        }
        return fallbackRegister;
    }

    private String buildAdvisorLabel(Long teamId) {
        if (teamId == null) {
            return null;
        }
        List<TeacherTeamRel> relations = teacherTeamRelMapper.selectByTeamId(teamId);
        if (relations == null || relations.isEmpty()) {
            return null;
        }

        List<Long> teacherIds = new ArrayList<>();
        for (TeacherTeamRel relation : relations) {
            if (relation.getTeacherId() != null) {
                teacherIds.add(relation.getTeacherId());
            }
        }
        if (teacherIds.isEmpty()) {
            return null;
        }

        List<TeacherTeam> advisors = teacherTeamMapper.selectBaseTeachersByIds(teacherIds);
        List<String> names = new ArrayList<>();
        for (TeacherTeam item : advisors) {
            if (!StringUtils.isEmpty(item.getTeacherName())) {
                names.add(item.getTeacherName());
            } else if (!StringUtils.isEmpty(item.getPhone())) {
                names.add(item.getPhone());
            }
        }
        return names.isEmpty() ? null : String.join(" / ", names);
    }

    private boolean isTeamAdvisor(Long teacherId, Long teamId) {
        return isTeamAdvisor(getTeacherProfileIdsByUserId(teacherId), teamId);
    }

    private void ensureCompetitionReviewAccess(Long competitionId, Long teacherId, String noPermissionMessage) {
        CompetitionPermission permission = permissionMapper.selectByUserAndCompetition(teacherId, competitionId);
        if (permission == null || permission.getPermissionId() != 1) {
            throw new ServiceException(noPermissionMessage);
        }
    }

    @Override
    public Map<String, Object> searchTeachers(String phone, String name) {
        String normalizedPhone = StringUtils.isEmpty(phone) ? null : phone.trim();
        String normalizedName = StringUtils.isEmpty(name) ? null : name.trim();
        if (StringUtils.isNotEmpty(normalizedPhone)) {
            normalizedName = null;
        }

        List<TeacherTeam> matches;
        if (StringUtils.isNotEmpty(normalizedPhone)) {
            matches = teacherTeamMapper.selectBaseTeachersByPhone(normalizedPhone);
        } else if (StringUtils.isNotEmpty(normalizedName)) {
            matches = teacherTeamMapper.selectBaseTeachersByName(normalizedName);
        } else {
            throw new ServiceException("Phone or name is required");
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (TeacherTeam match : matches) {
            Map<String, Object> item = new HashMap<>();
            item.put("teacherId", match.getId());
            item.put("teacherName", match.getTeacherName());
            item.put("phone", match.getPhone());
            item.put("sex", match.getSex());
            item.put("email", match.getEmail());
            item.put("workUnit", match.getWorkUnit());
            item.put("title", match.getTitle());
            item.put("position", match.getPosition());
            item.put("politicalStatus", match.getPoliticalStatus());
            item.put("birthDate", match.getBirthDate());
            items.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        if (items.isEmpty()) {
            result.put("matchType", "none");
        } else if (items.size() == 1) {
            result.put("matchType", "single");
        } else {
            result.put("matchType", "multiple");
        }
        return result;
    }

    private List<Long> getTeacherProfileIdsByUserId(Long teacherId) {
        if (teacherId == null) {
            return new ArrayList<>();
        }
        org.iflytek.common.core.domain.entity.SysUser teacher = sysUserMapper.selectUserById(teacherId);
        if (teacher == null || StringUtils.isEmpty(teacher.getUserName())) {
            return new ArrayList<>();
        }
        return getTeacherProfileIdsByUserName(teacher.getUserName());
    }

    private List<Long> getTeacherProfileIdsByUserName(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return new ArrayList<>();
        }
        List<TeacherTeam> teachers = teacherTeamMapper.selectBaseTeachersByUserName(userName);
        List<Long> teacherIds = new ArrayList<>();
        if (teachers == null) {
            return teacherIds;
        }
        for (TeacherTeam teacher : teachers) {
            if (teacher.getId() != null) {
                teacherIds.add(teacher.getId());
            }
        }
        return teacherIds;
    }

    private boolean isTeamAdvisor(List<Long> teacherIds, Long teamId) {
        if (teacherIds == null || teacherIds.isEmpty() || teamId == null) {
            return false;
        }
        List<TeacherTeamRel> advisors = teacherTeamRelMapper.selectByTeamId(teamId);
        if (advisors == null || advisors.isEmpty()) {
            return false;
        }
        return advisors.stream().anyMatch(item -> teacherIds.contains(item.getTeacherId()));
    }

    private boolean isCompetitionAdvisor(Long teacherId, Long competitionId) {
        if (teacherId == null || competitionId == null) {
            return false;
        }
        List<Long> teacherProfileIds = getTeacherProfileIdsByUserId(teacherId);
        if (teacherProfileIds.isEmpty()) {
            return false;
        }
        List<CompetitionRegister> registers = registerMapper.selectByCompetitionId(competitionId);
        if (registers == null || registers.isEmpty()) {
            return false;
        }
        Set<Long> teamIds = new HashSet<>();
        for (CompetitionRegister register : registers) {
            if (register.getTeamId() != null) {
                teamIds.add(register.getTeamId());
            }
        }
        if (teamIds.isEmpty()) {
            return false;
        }
        List<TeacherTeamRel> advisorRows = teacherTeamRelMapper.selectByTeamIds(new ArrayList<>(teamIds));
        if (advisorRows == null || advisorRows.isEmpty()) {
            return false;
        }
        return advisorRows.stream().anyMatch(row -> teacherProfileIds.contains(row.getTeacherId()));
    }

    private Map<String, Object> extractLeaderInfo(String teamMembers) {
        Map<String, Object> leader = new HashMap<>();
        leader.put("name", null);
        leader.put("studentNo", null);
        leader.put("major", null);
        if (StringUtils.isEmpty(teamMembers)) {
            return leader;
        }
        try {
            com.alibaba.fastjson2.JSONArray members = com.alibaba.fastjson2.JSON.parseArray(teamMembers);
            if (members == null) {
                return leader;
            }
            for (int i = 0; i < members.size(); i++) {
                com.alibaba.fastjson2.JSONObject member = members.getJSONObject(i);
                if (member == null) {
                    continue;
                }
                String role = member.getString("role");
                if ("leader".equalsIgnoreCase(role)) {
                    leader.put("name", member.getString("name"));
                    leader.put("studentNo", member.getString("studentNo"));
                    leader.put("major", member.getString("major"));
                    return leader;
                }
            }
            if (!members.isEmpty()) {
                com.alibaba.fastjson2.JSONObject first = members.getJSONObject(0);
                if (first != null) {
                    leader.put("name", first.getString("name"));
                    leader.put("studentNo", first.getString("studentNo"));
                    leader.put("major", first.getString("major"));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse team members for leader info: {}", teamMembers, e);
        }
        return leader;
    }

    private int countTeamMembers(String teamMembers) {
        if (StringUtils.isEmpty(teamMembers)) {
            return 0;
        }
        try {
            com.alibaba.fastjson2.JSONArray members = com.alibaba.fastjson2.JSON.parseArray(teamMembers);
            return members == null ? 0 : members.size();
        } catch (Exception e) {
            log.warn("Failed to parse team members: {}", teamMembers, e);
            return 0;
        }
    }

    private String resolveSubmissionStatus(CompetitionParticipation participation, List<CompetitionScore> scores) {
        if (participation == null) {
            return "未提交";
        }
        if (StringUtils.isNotEmpty(participation.getParticipationStatus())) {
            return participation.getParticipationStatus();
        }
        if (scores != null && !scores.isEmpty()) {
            return "已评分";
        }
        if (participation.getSubmitTime() != null) {
            return "已提交";
        }
        return "未提交";
    }

    private BigDecimal resolveDisplayFinalScore(CompetitionParticipation participation, List<CompetitionScore> scores) {
        if (participation != null && participation.getFinalScore() != null) {
            return participation.getFinalScore();
        }
        if (scores == null || scores.isEmpty()) {
            return null;
        }
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (CompetitionScore score : scores) {
            if (score == null || score.getScore() == null) {
                continue;
            }
            total = total.add(score.getScore());
            count++;
        }
        if (count == 0) {
            return null;
        }
        return total.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
    }

    private List<CompetitionWork> loadParticipationWorks(Long participationId) {
        CompetitionWork query = new CompetitionWork();
        query.setParticipationId(participationId);
        List<CompetitionWork> works = competitionWorkMapper.selectCompetitionWorkList(query);
        return works == null ? new ArrayList<>() : works;
    }

    private List<Map<String, String>> buildFileResponses(List<CompetitionWork> works) {
        List<Map<String, String>> files = new ArrayList<>();
        if (works == null) {
            return files;
        }
        for (CompetitionWork work : works) {
            Map<String, String> file = new HashMap<>();
            file.put("name", work.getFileName());
            file.put("url", work.getFilePath());
            file.put("type", work.getFileType());
            files.add(file);
        }
        return files;
    }

    private String resolveWorkName(List<CompetitionWork> works) {
        if (works == null || works.isEmpty()) {
            return null;
        }
        for (CompetitionWork work : works) {
            if (!StringUtils.isEmpty(work.getFileName())) {
                return work.getFileName();
            }
        }
        return null;
    }

    private boolean hasMaterials(CompetitionParticipation participation, List<CompetitionWork> works) {
        if (participation != null && (!StringUtils.isEmpty(participation.getPptPath()) || !StringUtils.isEmpty(participation.getPdfPath()))) {
            return true;
        }
        return works != null && !works.isEmpty();
    }

    private static final String ASSIGNED = "ASSIGNED";

    private static final String REVIEWED = "REVIEWED";

    private String syncTeamMembersWithUserTable(String teamMembersJson) {
        if (StringUtils.isEmpty(teamMembersJson)) {
            return teamMembersJson;
        }
        try {
            com.alibaba.fastjson2.JSONArray members = com.alibaba.fastjson2.JSON.parseArray(teamMembersJson);
            if (members == null || members.isEmpty()) {
                return teamMembersJson;
            }
            for (int i = 0; i < members.size(); i++) {
                com.alibaba.fastjson2.JSONObject member = members.getJSONObject(i);
                if (member == null) {
                    continue;
                }
                String studentNo = member.getString("studentNo");
                if (StringUtils.isEmpty(studentNo)) {
                    continue;
                }
                SysUser user = sysUserMapper.selectUserByStudentNo(studentNo);
                if (user == null) {
                    user = sysUserMapper.selectUserByUserName(studentNo);
                }
                if (user != null) {
                    String name = user.getStudentName();
                    if (StringUtils.isEmpty(name)) {
                        name = user.getNickName();
                    }
                    if (StringUtils.isEmpty(name)) {
                        name = user.getUserName();
                    }
                    if (StringUtils.isNotEmpty(name)) {
                        member.put("name", name);
                    }
                    if (user.getCollegeName() != null) {
                        member.put("college", user.getCollegeName());
                    }
                    if (user.getMajorName() != null) {
                        member.put("major", user.getMajorName());
                    }
                    if (user.getPhonenumber() != null) {
                        member.put("phone", user.getPhonenumber());
                    }
                    if (user.getEmail() != null) {
                        member.put("email", user.getEmail());
                    }
                }
            }
            return members.toJSONString();
        } catch (Exception e) {
            log.warn("Failed to sync team members with user table: {}", teamMembersJson, e);
            return teamMembersJson;
        }
    }

    @Override
    public TeacherTeam getTeacherByPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return null;
        }
        return teacherTeamMapper.selectBaseTeacherByPhone(phone);
    }

    @Override
    public int updateTeacherInfo(TeacherTeam teacherTeam) {
        if (teacherTeam == null || StringUtils.isEmpty(teacherTeam.getPhone())) {
            return 0;
        }
        // 先尝试根据手机号更新
        int result = teacherTeamMapper.updateBaseTeacherByPhone(teacherTeam);
        if (result == 0 && teacherTeam.getId() != null) {
            // 如果手机号更新失败，尝试根据ID更新
            result = teacherTeamMapper.updateBaseTeacherById(teacherTeam);
        }
        return result;
    }
}
