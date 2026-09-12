// D:\\wqq\\competitionsystem\\compserve\\compserve-system\\src\\main\\java\\org\\iflytek\\system\\mapper\\SysUserOptimizationMapper.java
package org.iflytek.system.mapper;

import org.iflytek.common.core.domain.entity.SysUserOptimization;

public interface SysUserOptimizationMapper {
    /**
     * 根据用户ID查询优化信息
     */
    SysUserOptimization selectByUserId(Long userId);
    
    /**
     * 新增优化信息
     */
    int insert(SysUserOptimization optimization);
    
    /**
     * 更新优化信息
     */
    int update(SysUserOptimization optimization);
}