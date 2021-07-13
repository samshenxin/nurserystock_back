package com.ssh.rfidprint.entry;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * GcScheduleJob entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "gc_schedule_job" )
public class ScheduleJob implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;

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

    // Constructors
    /** default constructor */
    public ScheduleJob() {
    }


    // Property accessors
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "job_id", unique = true, nullable = false)
    public Integer getJobId() {
        return this.jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    @Column(name = "job_name", length = 50)
    public String getJobName() {
        return this.jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Column(name = "job_group", length = 20)
    public String getJobGroup() {
        return this.jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    @Column(name = "job_class", length = 50)
    public String getJobClass() {
        return this.jobClass;
    }

    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    @Column(name = "status")
    public Short getStatus() {
        return this.status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    @Column(name = "cron_expression", length = 50)
    public String getCronExpression() {
        return this.cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Column(name = "description")
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "create_time", length = 19)
    public Timestamp getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }


    @Override
    public String toString() {
        return "ScheduleJob [jobId=" + jobId + ", jobName=" + jobName + ", jobGroup=" + jobGroup + ", description=" + description + "]";
    }


    @Column(name = "monit_type")
    public Integer getMonitType() {
        return monitType;
    }


    public void setMonitType(Integer monitType) {
        this.monitType = monitType;
    }


    @Column(name = "monit_param")
    public String getMonitParam() {
        return monitParam;
    }


    public void setMonitParam(String monitParam) {
        this.monitParam = monitParam;
    }
    
    
}