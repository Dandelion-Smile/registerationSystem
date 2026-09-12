package org.iflytek.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.iflytek.system.mapper.CompetitionWorkMapper;
import org.iflytek.system.domain.CompetitionWork;
import org.iflytek.system.service.ICompetitionWorkService;

/**
 * 竞赛作品附件Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
@Service
public class CompetitionWorkServiceImpl implements ICompetitionWorkService 
{
    @Autowired
    private CompetitionWorkMapper competitionWorkMapper;

    /**
     * 查询竞赛作品附件
     * 
     * @param workId 竞赛作品附件主键
     * @return 竞赛作品附件
     */
    @Override
    public CompetitionWork selectCompetitionWorkByWorkId(Long workId)
    {
        return competitionWorkMapper.selectCompetitionWorkByWorkId(workId);
    }

    /**
     * 查询竞赛作品附件列表
     * 
     * @param competitionWork 竞赛作品附件
     * @return 竞赛作品附件
     */
    @Override
    public List<CompetitionWork> selectCompetitionWorkList(CompetitionWork competitionWork)
    {
        return competitionWorkMapper.selectCompetitionWorkList(competitionWork);
    }

    /**
     * 新增竞赛作品附件
     * 
     * @param competitionWork 竞赛作品附件
     * @return 结果
     */
    @Override
    public int insertCompetitionWork(CompetitionWork competitionWork)
    {
        return competitionWorkMapper.insertCompetitionWork(competitionWork);
    }

    /**
     * 修改竞赛作品附件
     * 
     * @param competitionWork 竞赛作品附件
     * @return 结果
     */
    @Override
    public int updateCompetitionWork(CompetitionWork competitionWork)
    {
        return competitionWorkMapper.updateCompetitionWork(competitionWork);
    }

    /**
     * 批量删除竞赛作品附件
     * 
     * @param workIds 需要删除的竞赛作品附件主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionWorkByWorkIds(Long[] workIds)
    {
        return competitionWorkMapper.deleteCompetitionWorkByWorkIds(workIds);
    }

    /**
     * 删除竞赛作品附件信息
     * 
     * @param workId 竞赛作品附件主键
     * @return 结果
     */
    @Override
    public int deleteCompetitionWorkByWorkId(Long workId)
    {
        return competitionWorkMapper.deleteCompetitionWorkByWorkId(workId);
    }
}
