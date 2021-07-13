package com.ssh.rfidprint.webService;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;

import org.springframework.stereotype.Component;

import com.ssh.rfidprint.entry.FaCardRealEntry;

/**
 * @ClassName:UserServiceImpl
 * @Description:测试服务接口实现类
 * @author Maple
 * @date:2018年4月10日下午3:58:58
 */
@WebService(serviceName = "CardRealService", // 对外发布的服务名
		targetNamespace = "http://webService.pushdata.bos.kd", // 指定你想要的名称空间，通常使用使用包名反转
		endpointInterface = "kd.bos.pushdata.webService.CardRealService") // 服务接口全路径, 指定做SEI（Service EndPoint
																			// Interface）服务端点接口
@Component
public class CardRealServiceImpl implements CardRealService {

	private Map<String, FaCardRealEntry> freMap = new HashMap<String, FaCardRealEntry>();

	public CardRealServiceImpl() {
		System.out.println("向实体类插入数据");
		FaCardRealEntry fre = new FaCardRealEntry();
		// user.setUserId(UUID.randomUUID().toString().replace("-", ""));
		// user.setUserName("test1");
		// user.setEmail("maplefix@163.xom");
		// userMap.put(user.getUserId(), user);
		fre.setZsf_rfid("10000000");
		fre.setAssetname("test01");
		fre.setNumber("电脑1");
		fre.setStoreplace("技术研发部");
		freMap.put(fre.getUsedate(), fre);

		// user = new User();
		// user.setUserId(UUID.randomUUID().toString().replace("-", ""));
		// user.setUserName("test2");
		// user.setEmail("maplefix@163.xom");
		// userMap.put(user.getUserId(), user);
		fre = new FaCardRealEntry();
		fre.setZsf_rfid("10000001");
		fre.setAssetname("test02");
		fre.setNumber("电脑2");
		fre.setStoreplace("技术研发部");
		freMap.put(fre.getUsedate(), fre);

		// user = new User();
		// user.setUserId(UUID.randomUUID().toString().replace("-", ""));
		// user.setUserName("test3");
		// user.setEmail("maplefix@163.xom");
		// userMap.put(user.getUserId(), user);
	}

	@Override
	public String getAssetName(String rfId) {
		return "rfId为：" + rfId;
	}

	@Override
	public FaCardRealEntry getFre(String rfId) {
		System.out.println("freMap是:" + freMap);
		return freMap.get(rfId);
	}

}
