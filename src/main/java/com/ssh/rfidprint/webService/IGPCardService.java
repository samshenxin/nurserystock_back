/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName IGPCardService.java
 * @author sam
 */
package com.ssh.rfidprint.webService;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService(name = "GPCardService", targetNamespace = "http://com.ssh.rfidprint/GPCardService/")
public interface IGPCardService {

	/**
	 * 根据数据ID，查找指定数据并下发制卡数据到网点
	 * 
	 * @Title: PushDataToClient
	 * @param List<Map<String,String>>  实物卡数组        
	 * @return 成功返回Success，失败返回Fail
	 * @WebParam(name = "persoID", targetNamespace = "http://com.ssh.rfidprint/GPCardService/") 
	 */
	@WebMethod(action = "http://com.ssh.rfidprint/GPCardService/PushDataToClient")
	String PushDataToClient(@WebParam(name = "persoID",targetNamespace = "http://com.ssh.rfidprint/GPCardService/")String  listMap );

	/**
	 * 获取设备信息，包括耗材等
	 * 
	 * @Title: GetDeviceInfo
	 * @param deviceId
	 * @return
	 */
	@WebMethod(action = "http://com.ssh.rfidprint/GPCardService/GetDeviceInfo")
	String GetDeviceInfo(
			@WebParam(name = "deviceId", targetNamespace = "http://com.ssh.rfidprint/GPCardService/") String deviceId);
}
