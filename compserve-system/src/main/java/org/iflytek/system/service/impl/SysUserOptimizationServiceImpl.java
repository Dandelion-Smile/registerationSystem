// D:\\wqq\\competitionsystem\\compserve\\compserve-system\\src\\main\\java\\org\\iflytek\\system\\service\\impl\\SysUserOptimizationServiceImpl.java
package org.iflytek.system.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.iflytek.common.core.domain.entity.SysUserOptimization;
import org.iflytek.system.mapper.SysUserOptimizationMapper;
import org.iflytek.system.service.ISysUserOptimizationService;

@Service
public class SysUserOptimizationServiceImpl implements ISysUserOptimizationService {

    @Autowired
    private SysUserOptimizationMapper userOptimizationMapper;

    @Override
    public int updateOptimizationCount(Long userId) {
        // 查询是否已存在记录
        SysUserOptimization optimization = userOptimizationMapper.selectByUserId(userId);
        if (optimization == null) {
            // 不存在则新增
            optimization = new SysUserOptimization();
            optimization.setUserId(userId);
            optimization.setOptimizationCount(1);
            optimization.setLastOptimizationTime(new Date());
            return userOptimizationMapper.insert(optimization);
        } else {
            // 存在则更新
            optimization.setOptimizationCount(optimization.getOptimizationCount() + 1);
            optimization.setLastOptimizationTime(new Date());
            return userOptimizationMapper.update(optimization);
        }
    }

    @Override
    public Integer getOptimizationCount(Long userId) {
        SysUserOptimization optimization = userOptimizationMapper.selectByUserId(userId);
        return optimization != null ? optimization.getOptimizationCount() : 0;
    }

    @Override
    public int updateOptimizationRecord(Long userId, boolean success, double duration) {
        // 查询是否已存在记录
        SysUserOptimization optimization = userOptimizationMapper.selectByUserId(userId);
        if (optimization == null) {
            // 不存在则新增
            optimization = new SysUserOptimization();
            optimization.setUserId(userId);
            optimization.setOptimizationCount(1);
            optimization.setSuccessCount(success ? 1 : 0);
            optimization.setTotalDuration(duration);
            optimization.setLastOptimizationTime(new Date());
            return userOptimizationMapper.insert(optimization);
        } else {
            // 存在则更新
            optimization.setOptimizationCount(optimization.getOptimizationCount() + 1);
            if (success) {
                optimization.setSuccessCount(optimization.getSuccessCount() + 1);
            }
            optimization.setTotalDuration(optimization.getTotalDuration() + duration);
            optimization.setLastOptimizationTime(new Date());
            return userOptimizationMapper.update(optimization);
        }
    }

    @Override
    public Map<String, Object> getOptimizationStats(Long userId) {
        SysUserOptimization optimization = userOptimizationMapper.selectByUserId(userId);
        Map<String, Object> stats = new HashMap<>();
        if (optimization != null) {
            stats.put("optimizationCount", optimization.getOptimizationCount());
            int successRate = optimization.getOptimizationCount() > 0 ? 
                (int) Math.round((double) optimization.getSuccessCount() / optimization.getOptimizationCount() * 100) : 0;
            stats.put("successRate", successRate);
            double averageTime = optimization.getOptimizationCount() > 0 ? 
                optimization.getTotalDuration() / optimization.getOptimizationCount() : 0.0;
            stats.put("averageTime", averageTime);
        } else {
            stats.put("optimizationCount", 0);
            stats.put("successRate", 0);
            stats.put("averageTime", 0.0);
        }
        return stats;
    }
}