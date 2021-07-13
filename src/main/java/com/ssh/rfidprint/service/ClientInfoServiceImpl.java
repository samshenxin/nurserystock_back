/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName ClientInfoServiceImpl.java
 * @author sam
 */
package com.ssh.rfidprint.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.goldpac.cardbiz.error.Errors;
import com.ssh.empire.protocol.data.admin.GetDeviceInfo;
import com.ssh.rfidprint.dto.IClientInfoDao;
import com.ssh.rfidprint.server.CacheUtil;
import com.ssh.rfidprint.server.ClientInfo;
import com.ssh.rfidprint.server.DateUtil;
import com.ssh.rfidprint.server.session.AdminSession;
@Service
public class ClientInfoServiceImpl extends BaseServiceImpl<ClientInfo,Integer> implements IClientInfoService{

    @Autowired
    private IClientInfoDao  clientInfoDao;

    public IClientInfoDao getClientInfoDao() {
        return clientInfoDao;
    }

    @Resource
    public void setClientInfoDao(IClientInfoDao clientInfoDao) {
        this.clientInfoDao = clientInfoDao;
        super.setBaseDao(clientInfoDao);
    }
    /**
     * 更新客户端状态信息
     * @Title: updateClientInfo
     * @param deviceId
     * @param branchId
     * @param status
     * @param clientHost
     */
   public void updateClientInfo(String deviceId, String branchId, Integer status, String clientHost){
       ClientInfo clientInfo = clientInfoDao.get("deviceId", deviceId);
       if(clientInfo == null){
           clientInfo = new ClientInfo(); 
       }
       clientInfo.setBranchId(branchId);
       clientInfo.setDeviceId(deviceId);
       clientInfo.setHost(clientHost);
       clientInfo.setStatus(status);
       clientInfo.setModifyTime(DateUtil.nowTimestamp());
       clientInfoDao.saveOrUpdate(clientInfo);
   }
   /**
    * 通知客户端，获取设备信息
    */
   public  void getDeviceInfo(String deviceId, Errors errors){
       GetDeviceInfo pushData = new GetDeviceInfo();
       AdminSession session = CacheUtil.getManager().getSession(deviceId);
       if (session != null && session.getIoSession().isConnected()) {
           session.write(pushData);
       } else {
           errors.reject("client.close", "客户端已关闭。");
       }
   }

}

 