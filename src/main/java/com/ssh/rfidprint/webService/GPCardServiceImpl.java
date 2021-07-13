/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName GPCardServiceImpl.java
 * @author sam
 */
package com.ssh.rfidprint.webService;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.BindingType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.goldpac.cardbiz.error.Errors;
import com.ssh.empire.protocol.data.admin.PushPersoData;
import com.ssh.rfidprint.entry.FaCardRealEntry;
import com.ssh.rfidprint.server.CacheUtil;
import com.ssh.rfidprint.server.session.AdminSession;
import com.ssh.rfidprint.service.ClientInfoServiceImpl;
import com.ssh.rfidprint.service.IClientInfoService;
import com.ssh.rfidprint.service.factory.ServiceManager;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.list.plugin.AbstractListPlugin;

@WebService(endpointInterface = "com.ssh.rfidprint.webService.IGPCardService", serviceName = "GPCardService", targetNamespace = "http://kd.bos.pushdata/GPCardService/")
@BindingType(value = javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_MTOM_BINDING)
public class GPCardServiceImpl extends AbstractListPlugin implements IGPCardService {
	private static final Logger log = LoggerFactory.getLogger(GPCardServiceImpl.class);

	/**
	 * 根据数据ID，查找指定数据并下发制卡数据到网点
	 * 
	 * @Title: PushDataToClient
	 * @param List<Map<String,String>>
	 *            实物卡数组
	 * @return 成功返回Success，失败返回Fail
	 */
	public String PushDataToClient(
			@WebParam(name = "persoID", targetNamespace = "http://com.ssh.rfidprint/GPCardService/") String keyValue) {
		String result = null;
		try {
			Errors errors = new Errors();

			DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("customRegion");
            String realCards = cache.get("realCards");
			String[] splits = realCards.split(";");
			
			FaCardRealEntry rfe = new FaCardRealEntry();
			for (int i = 0; i < splits.length; i++) {
				switch(i) {
				case 0:
					break;
				case 1:
					rfe.setBarcode(splits[i].toString());
					break;
				case 2:
					rfe.setNumber(splits[i].toString());
					break;
				case 3:
					rfe.setAssetname(splits[i].toString());
					break;
				case 4:
					rfe.setZsf_rfid(splits[i].toString());
					break;
				case 5:
					rfe.setHeadusedept(splits[i].toString());
					break;
				case 6:
					rfe.setStoreplace(splits[i].toString());
					break;
				case 7:
					rfe.setUsedate(splits[i].toString());
					break;
				case 8:
					rfe.setBillno(splits[i].toString());
					break;
					default :
				}
			}

				PushPersoData pushData = new PushPersoData();
				pushData.setOriginalJson(JSON.toJSONString(rfe));
				AdminSession session = CacheUtil.getManager().getSession("010001");
				String statusCode = "100";
				String statusDesc = "";

				if (session != null && session.getIoSession().isConnected()) {
					session.write(pushData);
					statusCode = "310";
					result = "SUCCESS";
				} else {
					errors.reject("client.close", "客户端已关闭。");
					statusCode = "312";
					result = "客户端已关闭";
				}

		} catch (Exception ex) {

			ex.printStackTrace();
			return "Fail: " + ex.getMessage();
		}

		return result;
	}

	@Override
	public String GetDeviceInfo(
			@WebParam(name = "deviceId", targetNamespace = "http://com.ssh.rfidprint/GPCardService/") String deviceId) {
		try {
			Errors errors = new Errors();
			IClientInfoService clientInfoService = ServiceManager.getManager().getBean("clientInfoServiceImpl",
					ClientInfoServiceImpl.class);
			clientInfoService.getDeviceInfo(deviceId, errors);
			if (errors.hasErrors()) {
				return "Fail: " + errors.getAllErrors().get(0).getMessage();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return "Fail: " + ex.getMessage();
		}

		return "SUCCESS";
	}

}
