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
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.api.ApiResult;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

/**
 * 
 * @ClassName: QueryInventoryDetaillApi
 * @Description:TODO(根据任务ID获取盘点任务明细)
 * @author: sam
 * @date: 2021-3-16 08:55:05
 * @Copyright:
 */
public class QueryInventoryDetaillApi implements IBillWebApiPlugin {
    private static final Logger logger = LoggerFactory.getLogger(QueryInventoryDetaillApi.class);

	@SuppressWarnings("unchecked")
	@Override
	public ApiResult doCustomService(Map<String, Object> params1) {
		// 获取参数，执行自定义服务逻辑,返回接口所需要参数
		@SuppressWarnings("rawtypes")
		List list = new ArrayList();
		JSONObject json = new JSONObject(params1);
		try {
			String taskID = json.getString("taskID");
			String userID = json.getString("userID");

			String algoKey = getClass().getName() + ".query_resume";
			String sql = "select fid ,fnumber,fbarcode, fname, fk_zsf_rfid,fmodel,finventorystate,freason,finventoryuser,finventorytime,"
					+ "finventschemeentryid,finventorytaskid ,fk_zsf_qrcode,fk_zsf_assetcatid,fk_zsf_unitid,fk_zsf_supplierid,"
					+ "fk_zsf_usestatusid,fk_zsf_headusedeptid,fk_zsf_headusepersonid ,fk_zsf_storeplaceid "
					+ "from t_fa_inventory_record where  finventoryway = 'C' "
					+ "and finventschemeentryid=? and finventoryuser=?";
			Object[] params = { taskID,userID };
			try (DataSet ds = DB.queryDataSet(algoKey, DBRoute.of("fa"), sql, params)) {
				RowMeta md = ds.getRowMeta();
				int columnCount = md.getFieldCount();
				while (ds.hasNext()) {
					Row row = ds.next();
					@SuppressWarnings("rawtypes")
					Map rowData = new HashMap();
					for (int i = 0; i < columnCount; i++) {
						//基础资料取中文名称显示
						String fields = "masterid,name,number";
						QFilter idFilter = new QFilter("id", "=", row.get(i).toString());
						QFilter[] filters = {idFilter};							
						DynamicObjectCollection doc = null;
						String ob = md.getField(i).getName();
						switch(ob) {
						case "fk_zsf_assetcatid":
							doc = QueryServiceHelper.query("fa_assetcategory", fields, filters);
							rowData.put(md.getField(i), doc.size() > 0 ?doc.get(0).get("name"):"");
							break;
						case "fk_zsf_unitid":
							doc = QueryServiceHelper.query("bd_measureunits", fields, filters);
							rowData.put(md.getField(i), doc.size() > 0 ?doc.get(0).get("name"):"");
							break;
						case "fk_zsf_supplierid":
							doc = QueryServiceHelper.query("bd_supplier", fields, filters);
							rowData.put(md.getField(i), doc.size() > 0 ?doc.get(0).get("name"):"");
							break;
						case "fk_zsf_usestatusid":
							doc = QueryServiceHelper.query("fa_usestatus", fields, filters);
							rowData.put(md.getField(i), doc.size() > 0 ?doc.get(0).get("name"):"");
							break;
						case "fk_zsf_headusedeptid":
							doc = QueryServiceHelper.query("bos_org", fields, filters);
							rowData.put(md.getField(i), doc.size() > 0 ?doc.get(0).get("name"):"");
							break;
						case "fk_zsf_headusepersonid":
							doc = QueryServiceHelper.query("bos_user", fields, filters);
							rowData.put(md.getField(i), doc.size() > 0 ?doc.get(0).get("name"):"");
							break;
						case "fk_zsf_storeplaceid":
							doc = QueryServiceHelper.query("fa_storeplace", fields, filters);
							rowData.put(md.getField(i), doc.size() > 0 ?doc.get(0).get("name"):"");
							break;
							default:
								rowData.put(md.getField(i), row.get(i));
						}
					}
					list.add(rowData);
				}
			}

		} catch (JSONException e) {
			logger.error(e.getMessage());
		}
		return ApiResult.success(list);
	}

}
