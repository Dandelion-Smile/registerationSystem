package org.iflytek.system.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.iflytek.system.domain.Competition;
import org.iflytek.system.domain.CompetitionParticipation;
import org.iflytek.system.domain.CompetitionRegister;
import org.iflytek.system.domain.CompetitionScore;
import org.iflytek.system.mapper.CompetitionMapper;
import org.iflytek.system.mapper.CompetitionParticipationMapper;
import org.iflytek.system.mapper.CompetitionRegisterMapper;
import org.iflytek.system.mapper.CompetitionScoreMapper;
import org.iflytek.system.service.IAdminScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员端评分管理服务实现
 */
@Service
public class AdminScoreServiceImpl implements IAdminScoreService
{
    private static final Logger log = LoggerFactory.getLogger(AdminScoreServiceImpl.class);

    @Autowired
    private CompetitionMapper competitionMapper;

    @Autowired
    private CompetitionRegisterMapper registerMapper;

    @Autowired
    private CompetitionParticipationMapper participationMapper;

    @Autowired
    private CompetitionScoreMapper scoreMapper;

    @Override
    public List<Map<String, Object>> listScoreResults(Long competitionId)
    {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询所有评分记录
        List<CompetitionScore> allScores = scoreMapper.selectAll();
        
        for (CompetitionScore score : allScores)
        {
            // 获取参赛信息
            CompetitionParticipation participation = participationMapper.selectById(score.getParticipationId());
            if (participation == null)
            {
                continue;
            }
            
            // 获取报名信息
            CompetitionRegister register = registerMapper.selectById(participation.getParticipationId());
            if (register == null)
            {
                continue;
            }
            
            // 如果指定了竞赛ID，进行筛选
            if (competitionId != null && !register.getCompetitionId().equals(competitionId))
            {
                continue;
            }
            
            // 获取竞赛信息
            Competition competition = competitionMapper.selectCompetitionById(register.getCompetitionId());
            if (competition == null)
            {
                continue;
            }
            
            // 获取队伍信息
            if (register.getTeamId() == null)
            {
                continue;
            }
            
            Map<String, Object> item = new HashMap<>();
            item.put("scoreId", score.getScoreId());
            item.put("competitionId", competition.getCompetitionId());
            item.put("competitionName", competition.getCompetitionName());
            item.put("teamId", register.getTeamId());
            item.put("teamName", register.getTeamName());
            item.put("reviewerId", score.getReviewerId());
            item.put("reviewerName", score.getReviewerName());
            item.put("score", score.getScore());
            item.put("comment", score.getComment());
            item.put("scoreTime", score.getScoreTime());
            item.put("participationId", score.getParticipationId());
            
            result.add(item);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getCompetitionRanking(Long competitionId)
    {
        return getCompetitionRanking(competitionId, null);
    }

    @Override
    public List<Map<String, Object>> getCompetitionRanking(Long competitionId, Integer topN)
    {
        if (competitionId == null)
        {
            throw new org.iflytek.common.exception.ServiceException("竞赛ID不能为空");
        }
        
        // 查询该竞赛的所有报名记录
        List<CompetitionRegister> registers = registerMapper.selectByCompetitionId(competitionId);
        
        List<Map<String, Object>> rankingList = new ArrayList<>();
        
        for (CompetitionRegister register : registers)
        {
            // 获取参赛信息
            CompetitionParticipation participation = participationMapper.selectById(register.getRegisterId());
            if (participation == null)
            {
                continue;
            }
            
            // 获取该队伍的所有评分
            List<CompetitionScore> scores = scoreMapper.selectByParticipationId(participation.getParticipationId());
            
            if (scores.isEmpty())
            {
                continue;
            }
            
            // 计算平均分
            BigDecimal totalScore = BigDecimal.ZERO;
            int scoreCount = 0;
            for (CompetitionScore score : scores)
            {
                if (score.getScore() != null)
                {
                    totalScore = totalScore.add(score.getScore());
                    scoreCount++;
                }
            }
            
            BigDecimal avgScore = scoreCount > 0 ? totalScore.divide(new BigDecimal(scoreCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            
            // 获取队伍信息
            if (register.getTeamId() == null)
            {
                continue;
            }
            
            Map<String, Object> item = new HashMap<>();
            item.put("teamId", register.getTeamId());
            item.put("teamName", register.getTeamName());
            item.put("participationId", participation.getParticipationId());
            item.put("avgScore", avgScore);
            item.put("scoreCount", scoreCount);
            item.put("scores", scores);
            
            rankingList.add(item);
        }
        
        // 按平均分降序排序
        rankingList.sort((a, b) -> {
            BigDecimal scoreA = (BigDecimal) a.get("avgScore");
            BigDecimal scoreB = (BigDecimal) b.get("avgScore");
            return scoreB.compareTo(scoreA);
        });
        
        // 添加排名
        int rank = 1;
        for (Map<String, Object> item : rankingList)
        {
            item.put("rank", rank++);
        }
        
        // 如果指定了topN，只返回前N名
        if (topN != null && topN > 0 && rankingList.size() > topN)
        {
            return rankingList.subList(0, topN);
        }
        
        return rankingList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmRanking(Long competitionId, List<Map<String, Object>> rankingList)
    {
        // 这里可以将排名结果保存到数据库或标记为已确认
        // 目前先简单返回成功，后续可以根据需求扩展
        log.info("确认竞赛ID {} 的排名，共 {} 个队伍", competitionId, rankingList != null ? rankingList.size() : 0);
        return 1;
    }
}
