package org.iflytek.system.mapper;

import java.util.List;
import org.iflytek.common.core.domain.entity.SysCollegeMajor;

/**
 * 学院专业表 数据层
 * 
 * @author ruoyi
 */
public interface SysCollegeMajorMapper
{
    /**
     * 根据条件分页查询学院专业数据
     * 
     * @param collegeMajor 学院专业信息
     * @return 学院专业数据集合信息
     */
    public List<SysCollegeMajor> selectCollegeMajorList(SysCollegeMajor collegeMajor);

    /**
     * 查询所有学院名称（去重）
     * 
     * @return 学院名称集合
     */
    public List<String> selectAllCollegeNames();

    /**
     * 根据学院名称查询专业列表
     * 
     * @param collegeName 学院名称
     * @return 专业名称集合
     */
    public List<String> selectMajorsByCollege(String collegeName);

    /**
     * 根据ID查询学院专业信息
     * 
     * @param id 主键ID
     * @return 学院专业信息
     */
    public SysCollegeMajor selectCollegeMajorById(Long id);

    /**
     * 新增学院专业信息
     * 
     * @param collegeMajor 学院专业信息
     * @return 结果
     */
    public int insertCollegeMajor(SysCollegeMajor collegeMajor);

    /**
     * 修改学院专业信息
     * 
     * @param collegeMajor 学院专业信息
     * @return 结果
     */
    public int updateCollegeMajor(SysCollegeMajor collegeMajor);

    /**
     * 删除学院专业信息
     * 
     * @param id 主键ID
     * @return 结果
     */
    public int deleteCollegeMajorById(Long id);

    /**
     * 批量删除学院专业信息
     * 
     * @param ids 需要删除的主键ID
     * @return 结果
     */
    public int deleteCollegeMajorByIds(Long[] ids);
}
