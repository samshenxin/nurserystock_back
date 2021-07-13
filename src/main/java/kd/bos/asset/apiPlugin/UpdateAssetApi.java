package kd.bos.asset.apiPlugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import json.JSONArray;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.api.ApiResult;
import kd.bos.entity.api.WebApiContext;
import kd.fi.arapcommon.consts.DBRouteConst;
/**
 * 
 * @ClassName:  InventoryApiPlugin   
 * @Description:TODO(更新单个资产物品状态)   
 * @author: sam
 * @date:   2021-3-8 13:55:05      
 * @Copyright:
 */
public class UpdateAssetApi implements IBillWebApiPlugin {

    private static final Logger logger = LoggerFactory.getLogger(UpdateAssetApi.class);

	@Override
	public ApiResult doCustomService(Map<String, Object> params) {
		// 获取参数，执行自定义服务逻辑
		String result = null;
		JSONObject json = new JSONObject(params);
		//根据单据编号及rfid或qrcode更新资产盘点状态
		try {
			String billno = json.getString("billno");
			String qrcode = json.getString("qrcode");
			String rfid = json.getString("rfid");
			boolean flag = true ;
			if(rfid != null && !"".equals(rfid)) {	
				//根据rfid更新盘点记录表
				Object[] param1 = {new Date(),billno,rfid};
				String updateSql = "update t_fa_inventory_record set finventoryquantity = '1', finventorystate = 'A' , finventorytime =? where FINVENTSCHEMEENTRYID =? and fk_zsf_rfid =?";
				flag = DB.execute(DBRoute.basedata, updateSql,param1);
			}else if(qrcode != null && !"".equals(qrcode)) {	
				//根据qrcode更新盘点记录表
				Object[] param2 = {new Date(),billno,qrcode};
				String updateSql = "update t_fa_inventory_record set finventoryquantity = '1', finventorystate = 'A' , finventorytime =? where FINVENTSCHEMEENTRYID =? and fk_zsf_qrcode =?";
				flag = DB.execute(DBRouteConst.BASEDATA, updateSql,param2);
			}
			if(flag)
				result = "OK";
			else
				result = "false";
			
		} catch (JSONException e) {
			logger.error(e.getMessage());
		}
		return ApiResult.success(result);
	}

//	@Override
//	public String getVersion() {
//		// TODO Auto-generated method stub
//		return  "1.1";
//	}
	
	@Override
	public ApiResult doCustomService(WebApiContext ctx) {
		
		return ApiResult.success("");
	}
}
