package org.iflytek.web.controller.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.iflytek.common.annotation.Log;
import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.core.page.TableDataInfo;
import org.iflytek.common.enums.BusinessType;
import org.iflytek.common.exception.ServiceException;
import org.iflytek.common.utils.PageUtils;
import org.iflytek.common.utils.SecurityUtils;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.common.utils.poi.ExcelUtil;
import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionFile;
import org.iflytek.system.domain.CompetitionRegister;
import org.iflytek.system.domain.CompetitionTeamExport;
import org.iflytek.system.service.IAdminCompetitionService;
import org.iflytek.system.service.ICompetitionFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;


/**
 * 管理员端竞赛管理Controller
 */
@RestController
@RequestMapping("/admin/competition")
public class AdminCompetitionController extends BaseController
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private IAdminCompetitionService adminCompetitionService;

    @Autowired
    private ICompetitionFileService competitionFileService;

    @GetMapping("/team-detail/{registerId}")
    public AjaxResult getTeamDetail(@PathVariable("registerId") Long registerId) {
        // 官僚核对：拿到 ID -> 查档 -> 返回
        CompetitionRegister detail = adminCompetitionService.selectTeamDetailById(registerId);
        if (detail == null) {
            return AjaxResult.error("档案不存在");
        }
        return AjaxResult.success(detail);
    }

    @GetMapping("/detail/{competitionId}")
    public AjaxResult getDetail(@PathVariable Long competitionId,
                                @RequestParam(required = false) String sortType,
                                @RequestParam(required = false) Double minScore,
                                @RequestParam(required = false) Double maxScore) {
        AjaxResult ajax = AjaxResult.success();
        // 1. 查询比赛基本信息
        ajax.put("competition", adminCompetitionService.selectCompetitionById(competitionId));
        // 2. 查询该比赛下的队伍列表（确保参数顺序正确：sortType 在前，两个 Double 在后）
        ajax.put("teams", adminCompetitionService.selectTeamListByCompId(competitionId, sortType, minScore, maxScore));
        return ajax;
    }
    /**
     * 获取赛事状态统计数据
     */
    @PreAuthorize("@ss.hasPermi('admin:competition:list')")
    @GetMapping("/stats")
    public AjaxResult getStats() {
        return success(adminCompetitionService.selectCompetitionCountStats());
    }

    @PreAuthorize("@ss.hasPermi('admin:competition:list')")
    @GetMapping("/types")
    public AjaxResult getTypes() {
        return success(adminCompetitionService.selectCompetitionTypes());
    }

    /**
     * 查询竞赛列表（支持分页）
     */
    @PreAuthorize("@ss.hasPermi('admin:competition:list')")
    @GetMapping("/list")
    public TableDataInfo list(Competition competition)
    {
        PageUtils.startPage();
        List<Competition> list = adminCompetitionService.selectCompetitionList(competition);
        return getDataTable(list);
    }

    /**
     * 新增竞赛
     */
    @PreAuthorize("@ss.hasPermi('admin:competition:add')")
    @Log(title = "竞赛管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody Competition competition)
    {
        int result = adminCompetitionService.insertCompetition(competition);
        if (result > 0 && competition.getOfficialFiles() != null && !competition.getOfficialFiles().isEmpty()) {
            competitionFileService.saveCompetitionFiles(competition.getCompetitionId(), competition.getOfficialFiles(), SecurityUtils.getUserId());
        }
        return result > 0 ? success() : error("新增竞赛失败");
    }

    /**
     * 修改竞赛
     */
    @PreAuthorize("@ss.hasPermi('admin:competition:edit')")
    @Log(title = "竞赛管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody Competition competition)
    {
        int result = adminCompetitionService.updateCompetition(competition);
        if (result > 0) {
            competitionFileService.saveCompetitionFiles(competition.getCompetitionId(), competition.getOfficialFiles(), SecurityUtils.getUserId());
        }
        return result > 0 ? success() : error("修改竞赛失败");
    }

    /**
     * 删除竞赛
     */
    @PreAuthorize("@ss.hasPermi('admin:competition:remove')")
    @Log(title = "竞赛管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{competitionIds}")
    public AjaxResult remove(@PathVariable Long[] competitionIds)
    {
        int result = adminCompetitionService.deleteCompetitionByIds(competitionIds);
        return result > 0 ? success() : error("删除竞赛失败");
    }

    // 路径：org.iflytek.web.controller.admin.AdminCompetitionController

    @GetMapping("/{competitionId}")
    public AjaxResult getInfo(@PathVariable Long competitionId,
                              @RequestParam(required = false) String sortType,
                              @RequestParam(required = false) Double minScore,
                              @RequestParam(required = false) Double maxScore)
    {
        // 获取赛事基本信息
        Competition competition = adminCompetitionService.selectCompetitionById(competitionId);

        // 获取报名队伍列表（传入排序和筛选参数）
        List<Map<String, Object>> teams = adminCompetitionService.selectTeamListByCompId(competitionId, sortType, minScore, maxScore);

        // 获取竞赛官方文件列表
        List<CompetitionFile> files = competitionFileService.getCompetitionFilesByCompetitionId(competitionId);
        competition.setOfficialFiles(files);

        AjaxResult ajax = AjaxResult.success();
        ajax.put("competition", competition);
        ajax.put("teams", teams);
        return ajax;
    }

    @Log(title = "导出参赛名单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, @RequestBody Map<String, Object> body)
    {
        Long competitionId = toLong(body.get("competitionId"));
        if (competitionId == null)
        {
            throw new ServiceException("赛事ID不能为空");
        }

        Competition competition = adminCompetitionService.selectCompetitionById(competitionId);
        if (competition == null)
        {
            throw new ServiceException("赛事不存在");
        }

        String sortType = toStringValue(body.get("sortType"));
        Double minScore = toDouble(body.get("minScore"));
        Double maxScore = toDouble(body.get("maxScore"));
        List<Map<String, Object>> teams = adminCompetitionService.selectTeamListByCompId(
                competitionId, sortType, minScore, maxScore);

        List<CompetitionTeamExport> rows = new ArrayList<>();
        int index = 1;
        for (Map<String, Object> team : teams)
        {
            CompetitionTeamExport row = new CompetitionTeamExport();
            row.setSortNo(index++);
            row.setTeamName(defaultText(team.get("team_name")));
            row.setWorkName(defaultWorkText(team.get("work_name")));
            row.setLeaderName(resolveLeaderName(team.get("team_members")));
            row.setAdvisorNames(defaultText(firstNonNull(team.get("advisorNames"), team.get("teacher_name"))));
            row.setReviewerNames(resolveReviewerNames(team.get("assignedReviewers")));
            row.setAvgScore(formatScore(team.get("avg_score")));
            row.setRegisterTime(toDate(team.get("register_time")));
            rows.add(row);
        }

        ExcelUtil<CompetitionTeamExport> util = new ExcelUtil<>(CompetitionTeamExport.class);
        util.exportExcel(response, rows, safeSheetName(competition.getCompetitionName()) + "参赛名单");
    }

    /**
     * 获取单支队伍的评审明细（用于前端大弹窗）
     */
    @GetMapping("/team-scores/{participationId}")
    public AjaxResult getTeamScores(@PathVariable Long participationId)
    {
        return AjaxResult.success(adminCompetitionService.selectScoresByParticipationId(participationId));
    }

    /**
     * 查询赛事评审老师池
     */
    @GetMapping("/{competitionId}/reviewers")
    public AjaxResult getCompetitionReviewers(@PathVariable Long competitionId)
    {
        return AjaxResult.success(adminCompetitionService.selectReviewersByCompetitionId(competitionId));
    }

    /**
     * 查询可加入赛事评审老师池的老师
     */
    @GetMapping("/{competitionId}/reviewers/candidates")
    public AjaxResult getCompetitionReviewerCandidates(@PathVariable Long competitionId)
    {
        return AjaxResult.success(adminCompetitionService.selectReviewerCandidatesByCompetitionId(competitionId));
    }

    /**
     * 添加老师进入赛事评审老师池
     */
    @PostMapping("/{competitionId}/reviewers")
    public AjaxResult addCompetitionReviewers(@PathVariable Long competitionId, @RequestBody Map<String, Object> body)
    {
        @SuppressWarnings("unchecked")
        List<Object> reviewerIdsObj = (List<Object>) body.get("reviewerIds");
        List<Long> reviewerIds = new java.util.ArrayList<>();
        if (reviewerIdsObj != null)
        {
            for (Object reviewerId : reviewerIdsObj)
            {
                reviewerIds.add(Long.valueOf(String.valueOf(reviewerId)));
            }
        }
        return AjaxResult.success(
                adminCompetitionService.addReviewersToCompetition(competitionId, reviewerIds, SecurityUtils.getUserId()));
    }

    /**
     * 自动分配评审老师
     */
    @PostMapping("/{competitionId}/review-assignments/auto")
    public AjaxResult autoAssignReviewers(@PathVariable Long competitionId, @RequestBody Map<String, Object> body)
    {
        Integer reviewerCountPerTeam = body.get("reviewerCountPerTeam") == null ? null
                : Integer.valueOf(body.get("reviewerCountPerTeam").toString());
        return AjaxResult.success(adminCompetitionService.autoAssignReviewers(competitionId, reviewerCountPerTeam,
                SecurityUtils.getUserId()));
    }

    /**
     * 删除赛事老师池中的老师
     */
    @DeleteMapping("/{competitionId}/reviewers/{teacherId}")
    public AjaxResult deleteCompetitionReviewer(@PathVariable Long competitionId, @PathVariable Long teacherId)
    {
        return AjaxResult.success(
                adminCompetitionService.revokeReviewerAndReassign(competitionId, teacherId, SecurityUtils.getUserId()));
    }

    /**
     * 手动调整单支队伍评审老师
     */
    @PutMapping("/{competitionId}/teams/{teamId}/reviewers")
    public AjaxResult updateTeamReviewers(@PathVariable Long competitionId,
            @PathVariable Long teamId,
            @RequestBody Map<String, Object> body)
    {
        Long registerId = body.get("registerId") == null ? null
                : Long.valueOf(String.valueOf(body.get("registerId")));
        @SuppressWarnings("unchecked")
        List<Object> reviewerIdsObj = (List<Object>) body.get("reviewerIds");
        List<Long> reviewerIds = new java.util.ArrayList<>();
        if (reviewerIdsObj != null)
        {
            for (Object reviewerId : reviewerIdsObj)
            {
                reviewerIds.add(Long.valueOf(String.valueOf(reviewerId)));
            }
        }
        return AjaxResult.success(
                adminCompetitionService.updateTeamReviewers(competitionId, registerId, teamId, reviewerIds, SecurityUtils.getUserId()));
    }

    private Long toLong(Object value)
    {
        if (value == null || StringUtils.isEmpty(String.valueOf(value)))
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    private Double toDouble(Object value)
    {
        if (value == null || StringUtils.isEmpty(String.valueOf(value)))
        {
            return null;
        }
        if (value instanceof Number)
        {
            return ((Number) value).doubleValue();
        }
        return Double.valueOf(String.valueOf(value));
    }

    private String toStringValue(Object value)
    {
        if (value == null)
        {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private Date toDate(Object value)
    {
        return value instanceof Date ? (Date) value : null;
    }

    private Object firstNonNull(Object first, Object second)
    {
        return first != null ? first : second;
    }

    private String defaultText(Object value)
    {
        String text = toStringValue(value);
        return StringUtils.isEmpty(text) ? "暂无" : text;
    }

    private String defaultWorkText(Object value)
    {
        String text = toStringValue(value);
        return StringUtils.isEmpty(text) ? "无" : text;
    }

    private String formatScore(Object value)
    {
        if (value == null || StringUtils.isEmpty(String.valueOf(value)))
        {
            return "0.00";
        }
        try
        {
            BigDecimal decimal = new BigDecimal(String.valueOf(value));
            return decimal.setScale(2, RoundingMode.HALF_UP).toPlainString();
        }
        catch (Exception e)
        {
            return String.valueOf(value);
        }
    }

    @SuppressWarnings("unchecked")
    private String resolveLeaderName(Object teamMembers)
    {
        String fallback = "未知";
        if (teamMembers == null)
        {
            return fallback;
        }
        try
        {
            Object parsed = teamMembers;
            if (teamMembers instanceof String)
            {
                String text = String.valueOf(teamMembers).trim();
                if (StringUtils.isEmpty(text))
                {
                    return fallback;
                }
                parsed = OBJECT_MAPPER.readValue(text, Object.class);
            }
            if (parsed instanceof List)
            {
                List<Map<String, Object>> members = (List<Map<String, Object>>) parsed;
                for (Map<String, Object> member : members)
                {
                    if ("leader".equals(toStringValue(member.get("role"))) && StringUtils.isNotEmpty(toStringValue(member.get("name"))))
                    {
                        return toStringValue(member.get("name"));
                    }
                }
                for (Map<String, Object> member : members)
                {
                    if (StringUtils.isNotEmpty(toStringValue(member.get("name"))))
                    {
                        return toStringValue(member.get("name"));
                    }
                }
            }
            if (parsed instanceof Map)
            {
                Map<String, Object> member = (Map<String, Object>) parsed;
                return defaultText(firstNonNull(member.get("leader"), member.get("name")));
            }
        }
        catch (Exception e)
        {
            return fallback;
        }
        return fallback;
    }

    @SuppressWarnings("unchecked")
    private String resolveReviewerNames(Object assignedReviewers)
    {
        if (!(assignedReviewers instanceof List))
        {
            return "暂无";
        }
        List<Object> reviewers = (List<Object>) assignedReviewers;
        String names = reviewers.stream()
                .filter(item -> item instanceof Map)
                .map(item -> toStringValue(((Map<String, Object>) item).get("reviewerName")))
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.joining(" / "));
        return StringUtils.isEmpty(names) ? "暂无" : names;
    }

    private String safeSheetName(String name)
    {
        String text = StringUtils.isEmpty(name) ? "赛事" : name;
        return text.replaceAll("[\\\\/?*\\[\\]:]", "_");
    }
}
