package kd.bos.asset.apiPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.algo.RowMeta;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.api.ApiResult;

/**
 * 
 * @ClassName: QueryInventschemeApi
 * @Description:TODO(根据用户获取盘点方案任务)
 * @author: sam
 * @date: 2021-3-8 13:55:05
 * @Copyright:
 */
public class QueryInventschemeApi implements IBillWebApiPlugin {
    private static final Logger logger = LoggerFactory.getLogger(QueryInventschemeApi.class);

	@SuppressWarnings("unchecked")
	@Override
	public ApiResult doCustomService(Map<String, Object> params) {
		// 获取参数，执行自定义服务逻辑,返回接口所需要参数
		@SuppressWarnings("rawtypes")
		List list = new ArrayList();
		JSONObject json = new JSONObject(params);
		try {
			String userID = json.getString("userID");

			String algoKey = getClass().getName() + ".query_resume";
			String sql = "select i.fid , i.fname ,t.FDETAILID ,it.FK_ZSF_INVENTORYSTATE, it.FTASKRULE from t_fa_inventscheme i  , t_fa_invent_taskrule t ,"
					+ "t_fa_inventschemeentry it where i.FMASTERID = t.FINVENTSCHEMEID and t.FENTRYID = it.FENTRYID and (t.FK_ZSF_INVENTORYSTATE = 'B' or t.FK_ZSF_INVENTORYSTATE = 'C') "
					+ "and t.FINVENTPERSON =? ORDER BY i.fid desc";
			Object[] params1 = { userID };
			logger.info(sql);
			try (DataSet ds = DB.queryDataSet(algoKey, DBRoute.of("fa"), sql, params1)) {
				RowMeta md = ds.getRowMeta();
				int columnCount = md.getFieldCount();
				while (ds.hasNext()) {
					Row row = ds.next();
					@SuppressWarnings("rawtypes")
					Map rowData = new HashMap();
					for (int i = 0; i < columnCount; i++) {
						rowData.put(md.getField(i), row.get(i));
					}
					list.add(rowData);
				}
			}

		} catch (JSONException e) {
			logger.error( e.getMessage());
		}
		return ApiResult.success(list);
	}

}
