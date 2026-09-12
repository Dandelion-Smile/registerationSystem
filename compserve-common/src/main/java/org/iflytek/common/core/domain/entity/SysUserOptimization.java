package org.iflytek.common.core.domain.entity;

import java.util.Date;

public class SysUserOptimization {
    /** 主键 */
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 优化次数 */
    private Integer optimizationCount;
    
    /** 优化成功次数 */
    private Integer successCount;
    
    /** 总优化时长（秒） */
    private Double totalDuration;
    
    /** 最后优化时间 */
    private Date lastOptimizationTime;
    
    /** 创建时间 */
    private Date createTime;
    
    /** 更新时间 */
    private Date updateTime;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getOptimizationCount() { return optimizationCount; }
    public void setOptimizationCount(Integer optimizationCount) { this.optimizationCount = optimizationCount; }
    public Integer getSuccessCount() { return successCount; }
    public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
    public Double getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Double totalDuration) { this.totalDuration = totalDuration; }
    public Date getLastOptimizationTime() { return lastOptimizationTime; }
    public void setLastOptimizationTime(Date lastOptimizationTime) { this.lastOptimizationTime = lastOptimizationTime; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}