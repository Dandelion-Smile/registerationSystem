package org.iflytek.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.iflytek.common.core.domain.entity.SysCollegeMajor;
import org.iflytek.system.mapper.SysCollegeMajorMapper;
import org.iflytek.system.service.ISysCollegeMajorService;

/**
 * 学院专业 业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysCollegeMajorServiceImpl implements ISysCollegeMajorService
{
    @Autowired
    private SysCollegeMajorMapper collegeMajorMapper;

    /**
     * 根据条件分页查询学院专业数据
     * 
     * @param collegeMajor 学院专业信息
     * @return 学院专业数据集合信息
     */
    @Override
    public List<SysCollegeMajor> selectCollegeMajorList(SysCollegeMajor collegeMajor)
    {
        return collegeMajorMapper.selectCollegeMajorList(collegeMajor);
    }

    /**
     * 查询所有学院名称（去重）
     * 
     * @return 学院名称集合
     */
    @Override
    public List<String> selectAllCollegeNames()
    {
        return collegeMajorMapper.selectAllCollegeNames();
    }

    /**
     * 根据学院名称查询专业列表
     * 
     * @param collegeName 学院名称
     * @return 专业名称集合
     */
    @Override
    public List<String> selectMajorsByCollege(String collegeName)
    {
        return collegeMajorMapper.selectMajorsByCollege(collegeName);
    }

    /**
     * 根据ID查询学院专业信息
     * 
     * @param id 主键ID
     * @return 学院专业信息
     */
    @Override
    public SysCollegeMajor selectCollegeMajorById(Long id)
    {
        return collegeMajorMapper.selectCollegeMajorById(id);
    }

    /**
     * 新增学院专业信息
     * 
     * @param collegeMajor 学院专业信息
     * @return 结果
     */
    @Override
    public int insertCollegeMajor(SysCollegeMajor collegeMajor)
    {
        return collegeMajorMapper.insertCollegeMajor(collegeMajor);
    }

    /**
     * 修改学院专业信息
     * 
     * @param collegeMajor 学院专业信息
     * @return 结果
     */
    @Override
    public int updateCollegeMajor(SysCollegeMajor collegeMajor)
    {
        return collegeMajorMapper.updateCollegeMajor(collegeMajor);
    }

    /**
     * 删除学院专业信息
     * 
     * @param id 主键ID
     * @return 结果
     */
    @Override
    public int deleteCollegeMajorById(Long id)
    {
        return collegeMajorMapper.deleteCollegeMajorById(id);
    }

    /**
     * 批量删除学院专业信息
     * 
     * @param ids 需要删除的主键ID
     * @return 结果
     */
    @Override
    public int deleteCollegeMajorByIds(Long[] ids)
    {
        return collegeMajorMapper.deleteCollegeMajorByIds(ids);
    }
}
