package org.iflytek.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.iflytek.common.core.domain.entity.SysUserOptimizationHistory;
import org.iflytek.system.mapper.SysUserOptimizationHistoryMapper;
import org.iflytek.system.service.ISysUserOptimizationHistoryService;

@Service
public class SysUserOptimizationHistoryServiceImpl implements ISysUserOptimizationHistoryService {

    @Autowired
    private SysUserOptimizationHistoryMapper historyMapper;

    @Override
    public List<SysUserOptimizationHistory> getOptimizationHistory(Long userId) {
        return historyMapper.selectByUserId(userId);
    }

    @Override
    public int addOptimizationHistory(SysUserOptimizationHistory history) {
        return historyMapper.insert(history);
    }
}