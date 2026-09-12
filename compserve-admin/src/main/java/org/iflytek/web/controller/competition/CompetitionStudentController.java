package org.iflytek.web.controller.competition;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.iflytek.common.annotation.Anonymous;
import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionRegister;
import org.iflytek.system.service.ICompetitionStudentService;
import org.iflytek.system.service.ICompetitionFileService;
import org.iflytek.system.domain.CompetitionFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.bind.annotation.RequestBody;

/**
 * 学生端竞赛相关接口
 */
@RestController
@RequestMapping("/student/competition")
public class CompetitionStudentController extends BaseController
{
    @Autowired
    private ICompetitionStudentService competitionStudentService;

    @Autowired
    private ICompetitionFileService competitionFileService;

    /**
     * 查询竞赛信息（支持分页和筛选），并带上是否在报名期的标识
     */
    @GetMapping("/list")
    public AjaxResult list(Integer pageNum, Integer pageSize, String keyword, 
                          String filterType, String filterLevel, String filterStatus,
                          String filterParticipants)
    {
        // 默认值：第1页，每页10条
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }

        // 构建筛选条件Map
        Map<String, String> filters = new HashMap<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            filters.put("keyword", keyword.trim());
        }
        if (filterType != null && !filterType.trim().isEmpty()) {
            filters.put("type", filterType);
        }
        if (filterLevel != null && !filterLevel.trim().isEmpty()) {
            filters.put("level", filterLevel);
        }
        if (filterStatus != null && !filterStatus.trim().isEmpty()) {
            filters.put("status", filterStatus);
        }
        if (filterParticipants != null && !filterParticipants.trim().isEmpty()) {
            filters.put("participants", filterParticipants);
        }

        List<Competition> list = competitionStudentService.selectCompetitionsByPageAndFilters(pageNum, pageSize, filters);
        Long userId = getUserId();
        Date now = new Date();

        // 批量查询当前用户在所有竞赛下的报名状态，避免 N+1 查询
        List<Long> competitionIds = list.stream().map(Competition::getCompetitionId).toList();
        Map<Long, Map<String, Object>> statusBatch = competitionStudentService.getUserCompetitionStatusBatch(competitionIds, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("competitionId", c.getCompetitionId());
            m.put("competitionName", c.getCompetitionName());
            m.put("description", c.getDescription());
            m.put("competitionType", c.getCompetitionType());
            m.put("competitionLink", c.getCompetitionLink());
            m.put("bannerImage", c.getBannerImage());
            m.put("announcement", c.getAnnouncement());
            m.put("learningLink", c.getLearningLink());
            m.put("learningImage", c.getLearningImage());
            m.put("learningDescription", c.getLearningDescription());
            m.put("learningCount", c.getLearningCount());
            m.put("registerStartTime", c.getRegisterStartTime());
            m.put("registerEndTime", c.getRegisterEndTime());
            boolean inPeriod = c.getRegisterStartTime() != null
                    && c.getRegisterEndTime() != null
                    && c.getRegisterStartTime().compareTo(now) <= 0
                    && c.getRegisterEndTime().compareTo(now) >= 0;
            m.put("inRegisterPeriod", inPeriod);
            // 从批量查询结果中取值，不再逐个调用 getUserCompetitionStatus
            Map<String, Object> status = statusBatch.get(c.getCompetitionId());
            m.put("userRegistered", status != null && Boolean.TRUE.equals(status.get("registered")));
            m.put("registerId", status != null ? status.get("registerId") : null);
            return m;
        }).toList());
        result.put("pageNum", pageNum);
        result.put("pageSize", pageSize);
        result.put("hasMore", list.size() == pageSize); // 如果返回的数量等于pageSize，说明可能还有更多数据
        
        return success(result);
    }

    @GetMapping("/types")
    public AjaxResult types()
    {
        return success(competitionStudentService.selectCompetitionTypes());
    }

    /**
     * 获取竞赛的队伍列表
     */
    @GetMapping("/{competitionId}/teams")
    public AjaxResult getTeams(@PathVariable Long competitionId)
    {
        List<Map<String, Object>> teams = competitionStudentService.getCompetitionTeams(competitionId);
        return success(teams);
    }

    /**
     * 获取竞赛详情
     */
    @GetMapping("/{competitionId}")
    public AjaxResult getCompetitionDetail(@PathVariable Long competitionId)
    {
        Competition competition = competitionStudentService.getCompetitionById(competitionId);
        if (competition == null) {
            return error("竞赛不存在");
        }
        Date now = new Date();
        Map<String, Object> result = new HashMap<>();
        result.put("competitionId", competition.getCompetitionId());
        result.put("competitionName", competition.getCompetitionName());
        result.put("description", competition.getDescription());
        result.put("competitionType", competition.getCompetitionType());
        result.put("competitionLink", competition.getCompetitionLink());
        result.put("bannerImage", competition.getBannerImage());
        result.put("announcement", competition.getAnnouncement());
        result.put("learningLink", competition.getLearningLink());
        result.put("learningImage", competition.getLearningImage());
        result.put("learningDescription", competition.getLearningDescription());
        result.put("learningCount", competition.getLearningCount());
        result.put("registerStartTime", competition.getRegisterStartTime());
        result.put("registerEndTime", competition.getRegisterEndTime());
        boolean inPeriod = competition.getRegisterStartTime() != null
                && competition.getRegisterEndTime() != null
                && competition.getRegisterStartTime().compareTo(now) <= 0
                && competition.getRegisterEndTime().compareTo(now) >= 0;
        result.put("inRegisterPeriod", inPeriod);
        
        // 获取竞赛官方文件列表
        List<CompetitionFile> files = competitionFileService.getCompetitionFilesByCompetitionId(competitionId);
        // 为每个文件生成带签名的下载链接
        for (CompetitionFile file : files) {
            String signedUrl = competitionFileService.generateSignedUrl(file.getFileUrl());
            file.setFileUrl(signedUrl);
        }
        result.put("officialFiles", files);
        
        return success(result);
    }

    /**
     * 队长邀请队员加入队伍
     */
    @PostMapping("/team/invite")
    public AjaxResult invite(@RequestBody Map<String, Object> body)
    {
        Long teamId = body.get("teamId") == null ? null : Long.valueOf(body.get("teamId").toString());
        String studentNo = (String) body.get("studentNo");
        Long userId = getUserId();
        boolean ok = competitionStudentService.inviteMember(teamId, studentNo, userId);
        return success(ok);
    }

    /**
     * 队员申请加入队伍
     */
    @PostMapping("/team/apply")
    public AjaxResult apply(@RequestBody Map<String, Object> body)
    {
        Long teamId = body.get("teamId") == null ? null : Long.valueOf(body.get("teamId").toString());
        Long userId = getUserId();
        boolean ok = competitionStudentService.applyJoinTeam(teamId, userId);
        return success(ok);
    }

    /**
     * 同意邀请/申请
     */
    @PostMapping("/team/approve")
    public AjaxResult approve(@RequestBody Map<String, Object> body)
    {
        Long teamId = body.get("teamId") == null ? null : Long.valueOf(body.get("teamId").toString());
        String studentNo = (String) body.get("studentNo");
        Long userId = getUserId();
        boolean ok = competitionStudentService.approveMember(teamId, studentNo, userId);
        return success(ok);
    }

    /**
     * 拒绝邀请/申请
     */
    @PostMapping("/team/reject")
    public AjaxResult reject(@RequestBody Map<String, Object> body)
    {
        Long teamId = body.get("teamId") == null ? null : Long.valueOf(body.get("teamId").toString());
        String studentNo = (String) body.get("studentNo");
        Long userId = getUserId();
        boolean ok = competitionStudentService.rejectMember(teamId, studentNo, userId);
        return success(ok);
    }

    /**
     * 队长移除队员
     */
    @PostMapping("/team/remove")
    public AjaxResult remove(@RequestBody Map<String, Object> body)
    {
        Long teamId = body.get("teamId") == null ? null : Long.valueOf(body.get("teamId").toString());
        String studentNo = (String) body.get("studentNo");
        Long userId = getUserId();
        boolean ok = competitionStudentService.removeMember(teamId, studentNo, userId);
        return success(ok);
    }

    /**
     * 我的待处理邀请/申请列表
     */
    @GetMapping("/team/invitations")
    public AjaxResult invitations()
    {
        Long userId = getUserId();
        List<Map<String, Object>> list = competitionStudentService.listUserInvitations(userId);
        return success(list);
    }


    /**
     * 查询当前用户在指定竞赛下的报名状态
     */
    @GetMapping("/{competitionId}/status")
    public AjaxResult status(@PathVariable Long competitionId, @RequestParam(required = false) Long teamId)
    {
        Long userId = getUserId();
        Map<String, Object> data = competitionStudentService.getUserCompetitionStatus(competitionId, userId, teamId);
        return success(data);
    }

    /**
     * 报名竞赛（支持创建新队伍或加入已有队伍）
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody Map<String, Object> body)
    {
        Long competitionId = body.get("competitionId") == null ? null
                : Long.valueOf(body.get("competitionId").toString());
        Long teamId = body.get("teamId") == null ? null
                : Long.valueOf(body.get("teamId").toString());
        String teamName = (String) body.get("teamName");
        String teamMembers = (String) body.get("teamMembersJson");
        String workName = (String) body.get("workName");
        String workDescription = (String) body.get("workDescription");
        List<Map<String, Object>> teacherList = null;
        Object teacherListObj = body.get("teacherList");
        if (teacherListObj instanceof List<?> rawList)
        {
            teacherList = new ArrayList<>();
            for (Object item : rawList)
            {
                if (item instanceof Map<?, ?> rawMap)
                {
                    Map<String, Object> teacher = new HashMap<>();
                    for (Map.Entry<?, ?> entry : rawMap.entrySet())
                    {
                        if (entry.getKey() != null)
                        {
                            teacher.put(String.valueOf(entry.getKey()), entry.getValue());
                        }
                    }
                    teacherList.add(teacher);
                }
            }
        }
        Long userId = getUserId();

        CompetitionRegister register = competitionStudentService.registerCompetition(
                competitionId, teamId, teamName, teamMembers, workName, workDescription, teacherList, userId);
        return success(register);
    }

    /**
     * 提交材料
     */
    /**
     * 查询已上传的参赛材料信息
     */
    @PostMapping("/team/name")
    public AjaxResult saveTeamName(@RequestBody Map<String, Object> body)
    {
        Long competitionId = body.get("competitionId") == null ? null
                : Long.valueOf(body.get("competitionId").toString());
        Long teamId = body.get("teamId") == null ? null
                : Long.valueOf(body.get("teamId").toString());
        String teamName = (String) body.get("teamName");
        Long userId = getUserId();

        CompetitionRegister register = competitionStudentService.saveTeamName(competitionId, teamId, teamName, userId);
        return success(register);
    }

    @GetMapping("/material/{registerId}")
    public AjaxResult getMaterials(@PathVariable Long registerId)
    {
        Map<String, Object> materials = competitionStudentService.getUploadedMaterials(registerId);
        return success(materials);
    }

    @GetMapping("/work-detail/{registerId}")
    public AjaxResult getWorkDetail(@PathVariable Long registerId)
    {
        Long userId = getUserId();
        Map<String, Object> detail = competitionStudentService.getStudentWorkDetail(registerId, userId);
        return success(detail);
    }

    /**
     * 查询当前用户最近N条报名记录
     */
    @GetMapping("/recent-registrations")
    public AjaxResult getRecentRegistrations(@RequestParam(defaultValue = "10") Integer limit)
    {
        Long userId = getUserId();
        List<Map<String, Object>> registrations = competitionStudentService.getRecentRegistrations(userId, limit);
        return success(registrations);
    }

    @GetMapping("/registered-page")
    public AjaxResult getRegistrationPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize)
    {
        Long userId = getUserId();
        Map<String, Object> registrations = competitionStudentService.getRegistrationPage(userId, pageNum, pageSize);
        return success(registrations);
    }

    @PostMapping("/material")
    public AjaxResult material(@RequestBody Map<String, Object> body)
    {
        Long registerId = body.get("registerId") == null ? null
                : Long.valueOf(body.get("registerId").toString());
        String pptPath = (String) body.get("pptPath");
        String pdfPath = (String) body.get("pdfPath");
        String fileNames = (String) body.get("fileNames"); // JSON string: {ppt: "a.ppt", doc: "b.doc", other: "c.zip"}
        String workName = (String) body.get("workName");
        String workDescription = (String) body.get("workDescription");

        competitionStudentService.submitMaterials(registerId, pptPath, pdfPath, fileNames, workName, workDescription, getUserId());
        return success();
    }

    /**
     * 取消报名
     */
    @DeleteMapping("/register/{registerId}")
    public AjaxResult cancel(@PathVariable Long registerId)
    {
        Long userId = getUserId();
        boolean ok = competitionStudentService.cancelRegistration(registerId, userId);
        return ok ? success() : error("取消报名失败");
    }

    // 添加 @Anonymous 注解，允许匿名访问
    @Anonymous
    @PostMapping("/{id}/learning")
    public AjaxResult recordLearning(@PathVariable("id") Long competitionId) {
        try {
            competitionStudentService.recordLearning(competitionId);
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error();
        }
    }
}
