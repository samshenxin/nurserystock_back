/*
 * Copyright @ 2015 Goldpac Co. Ltd. All right reserved.
 * @fileName ScheduleJob.java
 * @author sam
 */
package com.ssh.rfidprint.dto;

import java.sql.Timestamp;

public class ScheduleJobDto {
    /** 任务id */
    private Integer   jobId;
    /** 任务名称 */
    private String    jobName;
    /** 任务分组 */
    private String    jobGroup;
    /** 任务执行类 */
    private String    jobClass;
    
    /** 任务状态 0禁用 1启用 2删除*/
    private Short     status;
    /** 任务运行时间表达式 */
    private String    cronExpression;
    /** 任务描述 */
    private String    description;
    
    /** 监控类型：1本地目录，2FTP*/
    private Integer  monitType;
    /** 监控参数，如本地目录或 FTP URL，用户名等 ，多个参数用逗号隔开 */
    private String   monitParam;
    
    private Timestamp createTime;

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getJobClass() {
        return jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Integer getMonitType() {
        return monitType;
    }

    public void setMonitType(Integer monitType) {
        this.monitType = monitType;
    }

    public String getMonitParam() {
        return monitParam;
    }

    public void setMonitParam(String monitParam) {
        this.monitParam = monitParam;
    }
    

   
}
