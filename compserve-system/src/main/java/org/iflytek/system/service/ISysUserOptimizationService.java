// D:\\wqq\\competitionsystem\\compserve\\compserve-system\\src\\main\\java\\org\\iflytek\\system\\service\\ISysUserOptimizationService.java
package org.iflytek.system.service;

import java.util.Map;

public interface ISysUserOptimizationService {
    /**
     * 更新用户优化次数
     */
    int updateOptimizationCount(Long userId);
    
    /**
     * 根据用户ID获取优化次数
     */
    Integer getOptimizationCount(Long userId);
    
    /**
     * 更新用户优化记录（包括成功次数和时长）
     */
    int updateOptimizationRecord(Long userId, boolean success, double duration);
    
    /**
     * 获取用户优化统计数据
     */
    Map<String, Object> getOptimizationStats(Long userId);
}