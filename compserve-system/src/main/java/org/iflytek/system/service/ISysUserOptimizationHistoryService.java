package org.iflytek.system.service;

import java.util.List;
import org.iflytek.common.core.domain.entity.SysUserOptimizationHistory;

public interface ISysUserOptimizationHistoryService {
    /**
     * 获取用户优化历史记录
     */
    List<SysUserOptimizationHistory> getOptimizationHistory(Long userId);
    
    /**
     * 添加优化历史记录
     */
    int addOptimizationHistory(SysUserOptimizationHistory history);
}