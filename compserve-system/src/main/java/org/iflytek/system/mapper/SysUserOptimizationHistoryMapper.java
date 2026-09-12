package org.iflytek.system.mapper;

import java.util.List;
import org.iflytek.common.core.domain.entity.SysUserOptimizationHistory;

public interface SysUserOptimizationHistoryMapper {
    /**
     * 根据用户ID获取优化历史记录
     */
    List<SysUserOptimizationHistory> selectByUserId(Long userId);
    
    /**
     * 插入优化历史记录
     */
    int insert(SysUserOptimizationHistory history);
}