package kd.bos.asset.apiPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.api.ApiResult;
import kd.bos.entity.api.WebApiContext;
import kd.fi.arapcommon.consts.DBRouteConst;
/**
 * 
 * @ClassName:  GetCheckListApi   
 * @Description:TODO(获取盘点订单列表)   
 * @author: sam
 * @date:   2021-3-9 9:50:52      
 * @Copyright:
 */
public class GetCheckListApi implements IBillWebApiPlugin {
    private static final Logger logger = LoggerFactory.getLogger(GetCheckListApi.class);

	@Override
	public ApiResult doCustomService(Map<String, Object> params) {
		// 获取参数，执行自定义服务逻辑
		ApiResult success = ApiResult.success("Hello world Success!");
		System.err.println(success);
		return IBillWebApiPlugin.super.doCustomService(params);
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return  "1.1";
	}
	
	@Override
	public ApiResult doCustomService(WebApiContext ctx) {
		String result = null;
		Map<String,Object> obj = ctx.getData();//post请求方式
		obj = ctx.getQueryString();//get请求方式
		JSONObject json = new JSONObject(obj);
		try {
			String billno = json.getString("billno");
			//查到对应状态的id
//			String querySql = "select fid from tk_zsf_checkstatus where fnumber=?";
//			Object[] params = { "s0004"};//完成
//			List<Long> idList = DB.query(DBRoute.basedata, querySql, params, rs -> {
//				List<Long> ret = new ArrayList<>();
//				while (rs.next()) {
//					ret.add(rs.getLong(1));
//				}
//				return ret;
//			});
			boolean flag = true ;
				//更新表头订单状态
			Object[] param = {billno,};
				String updateSql = "update tk_zsf_inventory set fk_zsf_checkstatus ='已完成'  where fbillno =? " ;
				flag = DB.execute(DBRouteConst.BASEDATA, updateSql,param);

			if(flag)
				result = "OK";
			else
				result = "false";
			
		} catch (JSONException e) {
			logger.error(e.getMessage());
		}
		return ApiResult.success(result);
	}
}
