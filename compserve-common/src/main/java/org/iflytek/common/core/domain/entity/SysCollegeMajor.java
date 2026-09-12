package org.iflytek.common.core.domain.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.iflytek.common.annotation.Excel;
import org.iflytek.common.core.domain.BaseEntity;

/**
 * 学院专业表 sys_college_major
 * 
 * @author ruoyi
 */
public class SysCollegeMajor extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @Excel(name = "主键ID", cellType = Excel.ColumnType.NUMERIC)
    private Long id;

    /** 学院名称 */
    @Excel(name = "学院名称")
    @NotBlank(message = "学院名称不能为空")
    @Size(min = 0, max = 100, message = "学院名称长度不能超过100个字符")
    private String collegeName;

    /** 专业名称 */
    @Excel(name = "专业名称")
    @NotBlank(message = "专业名称不能为空")
    @Size(min = 0, max = 100, message = "专业名称长度不能超过100个字符")
    private String majorName;

    /** 排序号 */
    @Excel(name = "排序号", cellType = Excel.ColumnType.NUMERIC)
    private Long sortOrder;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getCollegeName()
    {
        return collegeName;
    }

    public void setCollegeName(String collegeName)
    {
        this.collegeName = collegeName;
    }

    public String getMajorName()
    {
        return majorName;
    }

    public void setMajorName(String majorName)
    {
        this.majorName = majorName;
    }

    public Long getSortOrder()
    {
        return sortOrder;
    }

    public void setSortOrder(Long sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("collegeName", getCollegeName())
            .append("majorName", getMajorName())
            .append("sortOrder", getSortOrder())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
