package org.iflytek.common.core.domain.entity;

import java.util.Date;

public class SysUserOptimizationHistory {
    /** 主键 */
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 文件名 */
    private String fileName;
    
    /** 文件类型 */
    private String fileType;
    
    /** 优化状态 */
    private String status;
    
    /** 优化级别 */
    private String optimizationLevel;
    
    /** 优化速度 */
    private String optimizationSpeed;
    
    /** 上传时间 */
    private Date uploadTime;
    
    /** 创建时间 */
    private Date createTime;
    
    /** 更新时间 */
    private Date updateTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOptimizationLevel() {
        return optimizationLevel;
    }

    public void setOptimizationLevel(String optimizationLevel) {
        this.optimizationLevel = optimizationLevel;
    }

    public String getOptimizationSpeed() {
        return optimizationSpeed;
    }

    public void setOptimizationSpeed(String optimizationSpeed) {
        this.optimizationSpeed = optimizationSpeed;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}