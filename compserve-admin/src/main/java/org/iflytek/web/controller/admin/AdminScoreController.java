package org.iflytek.web.controller.admin;

import java.util.List;
import java.util.Map;
import org.iflytek.common.annotation.Log;
import org.iflytek.common.core.controller.BaseController;
import org.iflytek.common.core.domain.AjaxResult;
import org.iflytek.common.enums.BusinessType;
import org.iflytek.system.service.IAdminScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端评分管理Controller
 */
@RestController
@RequestMapping("/admin/score")
public class AdminScoreController extends BaseController
{
    @Autowired
    private IAdminScoreService adminScoreService;

    /**
     * 查询评分结果列表
     */
    @PreAuthorize("@ss.hasPermi('admin:score:query')")
    @GetMapping("/result/list")
    public AjaxResult listScoreResults(@RequestParam(value = "competitionId", required = false) Long competitionId)
    {
        List<Map<String, Object>> list = adminScoreService.listScoreResults(competitionId);
        return success(list);
    }

    /**
     * 查询竞赛排名
     */
    @PreAuthorize("@ss.hasPermi('admin:score:ranking')")
    @GetMapping("/ranking/{competitionId}")
    public AjaxResult getRanking(
            @PathVariable Long competitionId,
            @RequestParam(value = "topN", required = false) Integer topN)
    {
        List<Map<String, Object>> rankingList;
        if (topN != null && topN > 0)
        {
            rankingList = adminScoreService.getCompetitionRanking(competitionId, topN);
        }
        else
        {
            rankingList = adminScoreService.getCompetitionRanking(competitionId);
        }
        return success(rankingList);
    }

    /**
     * 确认竞赛排名
     */
    @PreAuthorize("@ss.hasPermi('admin:score:confirm')")
    @Log(title = "评分管理", businessType = BusinessType.UPDATE)
    @PostMapping("/ranking/confirm")
    public AjaxResult confirmRanking(@RequestBody Map<String, Object> body)
    {
        Long competitionId = body.get("competitionId") == null ? null
                : Long.valueOf(body.get("competitionId").toString());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rankingList = (List<Map<String, Object>>) body.get("rankingList");
        
        int result = adminScoreService.confirmRanking(competitionId, rankingList);
        return result > 0 ? success() : error("确认排名失败");
    }
}
