/*
 * Copyright @ 2015 Goldpac Co. Ltd. All right reserved.
 * @fileName ScheduleUtil.java
 * @author sam
 */
package com.ssh.rfidprint.server;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ssh.rfidprint.common.Global;
import com.ssh.rfidprint.dto.ScheduleJobDto;
import com.ssh.rfidprint.entry.ScheduleJob;
public class ScheduleUtil {
    /** 日志对象 */
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleUtil.class);

    /*
     * 创建任务
     * @param scheduler the scheduler
     * @param scheduleJob the schedule job
     */
    public static void createScheduleJob(Scheduler scheduler, ScheduleJobDto scheduleJob) throws ScheduleException {
        createScheduleJob(scheduler, scheduleJob.getJobName(), scheduleJob.getJobGroup(), scheduleJob.getCronExpression(), scheduleJob.getJobClass(), scheduleJob);
    }

    /**
     * 创建定时任务
     *
     * @param scheduler
     *            the scheduler
     * @param jobName
     *            the job name
     * @param jobGroup
     *            the job group
     * @param cronExpression
     *            the cron expression
     * @param isSync
     *            the is sync
     * @param param
     *            the param
     * @throws ScheduleException
     */
    @SuppressWarnings("unchecked")
    public static void createScheduleJob(Scheduler scheduler, String jobName, String jobGroup, String cronExpression, String jobCls, Object param) throws ScheduleException {
        // 同步或异步
        // Class<? extends Job> jobClass = isSync ? JobSyncFactory.class : JobFactory.class;
        // Class<? extends Job> jobClass = QuartzJobFactory.class;
        Class<? extends Job> jobClass = null;
        try {
            jobClass = (Class<? extends Job>) Class.forName(jobCls);
        } catch (ClassNotFoundException e) {
            LOG.error(e.getMessage(), e);
        }
        // 构建job信息
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup).build();
        // 放入参数，运行时的方法可以获取
        jobDetail.getJobDataMap().put(Global.JOB_PARAM_KEY, param);
        // 表达式调度构建器
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        // 按新的cronExpression表达式构建一个新的trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).withSchedule(scheduleBuilder).build();
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
            LOG.error("创建定时任务失败", e);
            throw new ScheduleException("创建定时任务失败");
        }
    }
    
    /**
     * 获取表达式触发器
     *
     * @param scheduler the scheduler
     * @param jobName the job name
     * @param jobGroup the job group
     * @return cron trigger
     * @throws ScheduleException 
     */
    public static CronTrigger getCronTrigger(Scheduler scheduler, String jobName, String jobGroup) throws ScheduleException {

        try {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
            return (CronTrigger) scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            LOG.error("获取定时任务CronTrigger出现异常", e);
            throw new ScheduleException("获取定时任务CronTrigger出现异常");
        }
    }
    
    
    /**
     * 暂停任务
     * 
     * @param scheduler
     * @param jobName
     * @param jobGroup
     * @throws ScheduleException 
     */
    public static void pauseJob(Scheduler scheduler, String jobName, String jobGroup) throws ScheduleException {

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            LOG.error("暂停定时任务失败", e);
            throw new ScheduleException("暂停定时任务失败");
        }
    }
    
    /**
     * 恢复任务
     *
     * @param scheduler
     * @param jobName
     * @param jobGroup
     * @throws ScheduleException 
     */
    public static void resumeJob(Scheduler scheduler, String jobName, String jobGroup) throws ScheduleException {

        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            LOG.error("暂停定时任务失败", e);
            throw new ScheduleException("暂停定时任务失败");
        }
    }
    
    
    /**
     * 删除定时任务
     *
     * @param scheduler
     * @param jobName
     * @param jobGroup
     * @throws ScheduleException 
     */
    public static void deleteScheduleJob(Scheduler scheduler, String jobName, String jobGroup) throws ScheduleException {
        try {
            scheduler.deleteJob(getJobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            LOG.error("删除定时任务失败", e);
            throw new ScheduleException("删除定时任务失败");
        }
    }
    
    /**
     * 获取jobKey
     *
     * @param jobName the job name
     * @param jobGroup the job group
     * @return the job key
     */
    public static JobKey getJobKey(String jobName, String jobGroup) {

        return JobKey.jobKey(jobName, jobGroup);
    }
    
    /**
     * 获取触发器key
     * 
     * @param jobName
     * @param jobGroup
     * @return
     */
    public static TriggerKey getTriggerKey(String jobName, String jobGroup) {

        return TriggerKey.triggerKey(jobName, jobGroup);
    }
    
    /**
     * 更新定时任务
     *
     * @param scheduler the scheduler
     * @param scheduleJob the schedule job
     * @throws ScheduleException 
     */
    public static void updateScheduleJob(Scheduler scheduler, ScheduleJob scheduleJob) throws ScheduleException {
        updateScheduleJob(scheduler, scheduleJob.getJobName(), scheduleJob.getJobGroup(),
            scheduleJob.getCronExpression(), scheduleJob);
    }

    /**
     * 更新定时任务
     *
     * @param scheduler the scheduler
     * @param jobName the job name
     * @param jobGroup the job group
     * @param cronExpression the cron expression
     * @param isSync the is sync
     * @param param the param
     * @throws ScheduleException 
     */
    public static void updateScheduleJob(Scheduler scheduler, String jobName, String jobGroup,
                                         String cronExpression, Object param) throws ScheduleException {

        try {

            TriggerKey triggerKey = ScheduleUtil.getTriggerKey(jobName, jobGroup);

            //表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);

            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

            //按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder)
                .build();

            //按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        } catch (SchedulerException e) {
            LOG.error("更新定时任务失败", e);
            throw new ScheduleException("更新定时任务失败");
        }
    }


}
