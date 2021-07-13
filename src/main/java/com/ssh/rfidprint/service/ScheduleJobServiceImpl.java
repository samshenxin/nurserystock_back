/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName ScheduleJobServiceImpl.java
 * @author sam
 */
package com.ssh.rfidprint.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ssh.rfidprint.dto.IScheduleJobDao;
import com.ssh.rfidprint.entry.ScheduleJob;


/**
 * 定时任务Service层接口实现类
 * @ClassName: ScheduleJobServiceImpl
 * @author sam
 * @date 2015年5月11日 下午2:15:23
 * @version 1.0<br />
 * Update Logs:<br />
 * ****************************************************<br />
 * Date: <br />
 * Description:<br />
 ******************************************************<br />
 */
@Service
public class ScheduleJobServiceImpl extends BaseServiceImpl<ScheduleJob,Integer> implements IScheduleJobService{
    @Autowired
    private IScheduleJobDao dao;

    public IScheduleJobDao getDao() {
        return dao;
    }

    @Resource
    public void setDao(IScheduleJobDao dao) {
        this.dao = dao;
        super.setBaseDao(dao);
    }
    

}

 