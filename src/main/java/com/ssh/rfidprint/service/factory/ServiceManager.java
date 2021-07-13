/*
 * Copyright @ 2014 Goldpac Co. Ltd. All right reserved.
 * @fileName ServiceManager.java
 * @author sam
 */
package com.ssh.rfidprint.service.factory;
import java.util.List;

import org.quartz.Scheduler;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ssh.rfidprint.dto.ScheduleJobDto;
import com.ssh.rfidprint.entry.ScheduleJob;
import com.ssh.rfidprint.server.ScheduleException;
import com.ssh.rfidprint.server.ScheduleUtil;
import com.ssh.rfidprint.service.IScheduleJobService;
import com.ssh.rfidprint.service.ScheduleJobServiceImpl;

public class ServiceManager {
    /** 调度工厂Bean */
    
    private Scheduler          scheduler;
    private ApplicationContext context = null;
    
    // 静态内部类，实现单例
    private static class ServiceManagerHolder {
        private static final ServiceManager instance = new ServiceManager();
    }

    private ServiceManager() {
        init();
    }

    /**
     * 获取服务管理类
     * 
     * @return 服务管理类
     */
    public static final ServiceManager getManager() {
        return ServiceManagerHolder.instance;
    }
    
    
    /**
     * 删除定时任务
     * @Title: deleteJobTask
     */
    public void deleteJobTask(){
        scheduler = (Scheduler) context.getBean("schedulerFactoryBean");
        try {
            IScheduleJobService scheduleJobService = this.getBean("scheduleJobServiceImpl", ScheduleJobServiceImpl.class);
            List<ScheduleJob> jobList = scheduleJobService.getAll();
            if(jobList == null || jobList.isEmpty()){
                return;
            }
            for(ScheduleJob scheduleJob : jobList){
                ScheduleJobDto job = new ScheduleJobDto();
                //对象复制
                BeanUtils.copyProperties(scheduleJob, job);
                ScheduleUtil.deleteScheduleJob(scheduler, job.getJobName(), job.getJobGroup());
            }
        } catch (ScheduleException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 初使化定时任务
     * @Title: initTask
     */
    public void initTask(){
        
        scheduler = (Scheduler) context.getBean("schedulerFactoryBean");
        try {
            IScheduleJobService scheduleJobService = this.getBean("scheduleJobServiceImpl", ScheduleJobServiceImpl.class);
            List<ScheduleJob> jobList = scheduleJobService.getAll();
            if(jobList == null || jobList.isEmpty()){
                return;
            }
            for(ScheduleJob scheduleJob : jobList){
                ScheduleJobDto job = new ScheduleJobDto();
                if(scheduleJob.getStatus() != 0){
                    //对象复制
                    BeanUtils.copyProperties(scheduleJob, job);
                    ScheduleUtil.createScheduleJob(scheduler, job);
                }
            }
        } catch (ScheduleException e) {
            e.printStackTrace();
        }
    }

    // 初使化
    private void init() {
        context = new ClassPathXmlApplicationContext(new String[] { "classpath*:**/applicationContext.xml", "classpath*:**/quartz-task.xml"});
//        for(int i = 0 ; i < context.getBeanDefinitionCount(); i++){
//          System.out.println( context.getBeanDefinitionNames()[i]);
//       }
    }

    public ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * 根据spring配置文件中bean名称，获取指定对象
     * 
     * @Title: getBean
     * @param beanName
     * @param clazz
     * @return
     */
    public <T> T getBean(String beanName, Class<T> clazz) {
        return context == null ? null : context.getBean(beanName, clazz);
    }
}
