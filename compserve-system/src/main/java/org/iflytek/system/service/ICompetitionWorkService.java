package org.iflytek.system.service;

import java.util.List;
import org.iflytek.system.domain.CompetitionWork;

/**
 * 竞赛作品附件Service接口
 * 
 * @author ruoyi
 * @date 2026-02-07
 */
public interface ICompetitionWorkService 
{
    /**
     * 查询竞赛作品附件
     * 
     * @param workId 竞赛作品附件主键
     * @return 竞赛作品附件
     */
    public CompetitionWork selectCompetitionWorkByWorkId(Long workId);

    /**
     * 查询竞赛作品附件列表
     * 
     * @param competitionWork 竞赛作品附件
     * @return 竞赛作品附件集合
     */
    public List<CompetitionWork> selectCompetitionWorkList(CompetitionWork competitionWork);

    /**
     * 新增竞赛作品附件
     * 
     * @param competitionWork 竞赛作品附件
     * @return 结果
     */
    public int insertCompetitionWork(CompetitionWork competitionWork);

    /**
     * 修改竞赛作品附件
     * 
     * @param competitionWork 竞赛作品附件
     * @return 结果
     */
    public int updateCompetitionWork(CompetitionWork competitionWork);

    /**
     * 批量删除竞赛作品附件
     * 
     * @param workIds 需要删除的竞赛作品附件主键集合
     * @return 结果
     */
    public int deleteCompetitionWorkByWorkIds(Long[] workIds);

    /**
     * 删除竞赛作品附件信息
     * 
     * @param workId 竞赛作品附件主键
     * @return 结果
     */
    public int deleteCompetitionWorkByWorkId(Long workId);
}
