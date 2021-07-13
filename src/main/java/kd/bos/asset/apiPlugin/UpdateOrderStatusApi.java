package kd.bos.asset.apiPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.db.DB;
import kd.bos.entity.api.ApiResult;
import kd.bos.entity.api.WebApiContext;
import kd.fi.arapcommon.consts.DBRouteConst;

/**
 * 
 * @ClassName: UpdateOrderStatusApi
 * @Description:TODO(更新订单状态)
 * @author: sam
 * @date: 2021-3-9 8:54:11
 * @Copyright:
 */
public class UpdateOrderStatusApi implements IBillWebApiPlugin {
    private static final Logger logger = LoggerFactory.getLogger(UpdateOrderStatusApi.class);

	@Override
	public ApiResult doCustomService(Map<String, Object> params) {
		// 获取参数，执行自定义服务逻辑
		String result = null;
		JSONObject json = new JSONObject(params);
		try {
			String billno = json.getString("billno");
			boolean flag = true;
			 Date dNow = new Date( );
		      SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
					
			// 更新表盘点状态:已盘点
		      Object[] param = {ft.format(dNow),billno,};
			String updateSql = "update t_fa_inventory_record set finventorystate = 'A' , finventorytime = ?  where FINVENTORYTASKID = ?";
			flag = DB.execute(DBRouteConst.BASEDATA, updateSql,param);
			//更新盘点方案的盘点状态为完成
			String updateStateSql = "update T_FA_INVENT_TASKRULE set FK_ZSF_INVENTORYSTATE = 'D' , FK_ZSF_MODIFYTIME =? where FDETAILID =? " ;
			flag = DB.execute(DBRouteConst.BASEDATA, updateStateSql,param);
			if (flag)
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
