package org.iflytek.web.controller.teacher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.core.page.TableDataInfo;
import org.iflytek.common.utils.StringUtils;
import org.iflytek.system.domain.Competition;
import org.iflytek.system.service.ITeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教师端接口
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController extends BaseController
{
    @Autowired
    private ITeacherService teacherService;

    /**
     * 查询公开竞赛列表（支持分页）
     */
    @GetMapping("/competition/list")
    public TableDataInfo listCompetitions(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "9") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status)
    {
        startPage();
        // 将空字符串转换为null，避免MyBatis判断错误
        String keywordParam = StringUtils.isEmpty(keyword) ? null : keyword;
        String statusParam = StringUtils.isEmpty(status) ? null : status;
        List<Competition> list = teacherService.listPublicCompetitions(keywordParam, statusParam);
        return getDataTable(list);
    }

    /**
     * 获取竞赛统计数据
     */
    @GetMapping("/competition/stats")
    public AjaxResult getCompetitionStats()
    {
        Map<String, Object> stats = teacherService.getCompetitionStats();
        return success(stats);
    }

    /**
     * 查询竞赛详情
     */
    @GetMapping("/competition/{competitionId}")
    public AjaxResult getCompetitionDetail(@PathVariable Long competitionId)
    {
        Competition competition = teacherService.getCompetitionDetail(competitionId);
        return success(competition);
    }

    /**
     * 查询可评审竞赛列表（已赋权的竞赛）
     */
    @GetMapping("/review/competitions")
    public AjaxResult getReviewableCompetitions()
    {
        Long teacherId = getUserId();
        List<Map<String, Object>> list = teacherService.getReviewableCompetitions(teacherId);
        return success(list);
    }

    /**
     * 查询竞赛的队伍列表（用于评审）
     */
    @GetMapping("/review/{competitionId}/teams")
    public AjaxResult getReviewTeams(@PathVariable Long competitionId)
    {
        Long teacherId = getUserId();
        List<Map<String, Object>> teams = teacherService.getReviewTeams(competitionId, teacherId);
        return success(teams);
    }

    /**
     * 查询队伍详情（包含材料路径）
     */
    @GetMapping("/review/{competitionId}/participations/{participationId}")
    public AjaxResult getReviewTeamDetail(@PathVariable Long competitionId, @PathVariable Long participationId)
    {
        Long teacherId = getUserId();
        Map<String, Object> detail = teacherService.getReviewTeamDetail(competitionId, participationId, teacherId);
        return success(detail);
    }

    /**
     * 查询队伍的评分记录
     */
    @GetMapping("/review/{competitionId}/participations/{participationId}/scores")
    public AjaxResult getTeamScores(@PathVariable Long competitionId, @PathVariable Long participationId)
    {
        Long teacherId = getUserId();
        teacherService.getReviewTeamDetail(competitionId, participationId, teacherId);
        List<Map<String, Object>> scores = teacherService.getTeamScores(participationId, teacherId);
        return success(scores);
    }

    /**
     * 提交评分
     */
    @PostMapping("/review/score")
    public AjaxResult submitScore(@RequestBody Map<String, Object> body)
    {
        Long participationId = body.get("participationId") == null ? null
                : Long.valueOf(body.get("participationId").toString());
        BigDecimal score = body.get("score") == null ? null
                : new BigDecimal(body.get("score").toString());
        String comment = (String) body.get("comment");
        Long teacherId = getUserId();
        
        // 获取当前登录教师的昵称
        org.iflytek.common.core.domain.entity.SysUser currentUser = getLoginUser().getUser();
        String teacherNickName = currentUser.getNickName() != null ? currentUser.getNickName() : currentUser.getUserName();

        Long scoreId = teacherService.submitScore(participationId, teacherId, teacherNickName, score, comment);
        return success(scoreId);
    }

    /**
     * 修改评分
     */
    @PutMapping("/review/score/{scoreId}")
    public AjaxResult updateScore(@PathVariable Long scoreId, @RequestBody Map<String, Object> body)
    {
        BigDecimal score = body.get("score") == null ? null
                : new BigDecimal(body.get("score").toString());
        String comment = (String) body.get("comment");
        Long teacherId = getUserId();

        teacherService.updateScore(scoreId, teacherId, score, comment);
        return success();
    }

    /**
     * 删除评分
     */
    @DeleteMapping("/review/score/{scoreId}")
    public AjaxResult deleteScore(@PathVariable Long scoreId)
    {
        Long teacherId = getUserId();
        teacherService.deleteScore(scoreId, teacherId);
        return success();
    }

    /**
     * 查询评审统计信息
     */
    @GetMapping("/review/{competitionId}/stats")
    public AjaxResult getReviewStats(@PathVariable Long competitionId)
    {
        Long teacherId = getUserId();
        Map<String, Object> stats = teacherService.getReviewStats(competitionId, teacherId);
        return success(stats);
    }

    /**
     * 查询当前教师作为指导老师的竞赛列表
     */
    @GetMapping("/my-teams/competitions")
    public AjaxResult getMyTeamCompetitions()
    {
        Long teacherId = getUserId();
        // 获取当前登录教师的昵称
        org.iflytek.common.core.domain.entity.SysUser currentUser = getLoginUser().getUser();
        String teacherNickName = currentUser.getNickName() != null ? currentUser.getNickName() : currentUser.getUserName();
        
        List<Map<String, Object>> list = teacherService.getMyTeamCompetitions(teacherId, teacherNickName);
        return success(list);
    }

    /**
     * 根据手机号获取教师信息
     */
    @GetMapping("/info/search")
    public AjaxResult searchTeachers(@RequestParam(required = false) String phone,
            @RequestParam(required = false) String name)
    {
        return success(teacherService.searchTeachers(phone, name));
    }

    /**
     * 根据手机号获取单个教师详细信息
     */
    @GetMapping("/info/by-phone/{phone}")
    public AjaxResult getTeacherByPhone(@PathVariable String phone)
    {
        return success(teacherService.getTeacherByPhone(phone));
    }

    /**
     * 更新教师信息（同步更新 tb_teacher_team 表）
     */
    @PutMapping("/info/update")
    public AjaxResult updateTeacherInfo(@RequestBody org.iflytek.system.domain.TeacherTeam teacherTeam)
    {
        return toAjax(teacherService.updateTeacherInfo(teacherTeam));
    }
}
