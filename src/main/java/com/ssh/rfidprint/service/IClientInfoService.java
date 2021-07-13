/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName IClientInfoService.java
 * @author sam
 */
package com.ssh.rfidprint.service;

import com.goldpac.cardbiz.error.Errors;
import com.ssh.rfidprint.server.ClientInfo;

/**
 * ClientInfo Service 层接口类
 * @ClassName: IClientInfoService
 * @author sam
 * @version 1.0<br />
 * @Date 2016年3月24日 下午2:23:26<br />
 * @Logs <br />
 * ****************************************************<br />
 */
public interface IClientInfoService extends BaseService<ClientInfo, Integer> {

    /**
     * 更新客户端状态信息
     * @Title: updateClientInfo
     * @param deviceId
     * @param branchId
     * @param status
     * @param clientHost
     */
    void updateClientInfo(String deviceId, String branchId, Integer status, String clientHost);

    /**
     * 通知客户端，获取设备信息
     * @Title: getDeviceInfo
     * @param deviceId
     * @param errors
     */
    void getDeviceInfo(String deviceId, Errors errors);
}

 