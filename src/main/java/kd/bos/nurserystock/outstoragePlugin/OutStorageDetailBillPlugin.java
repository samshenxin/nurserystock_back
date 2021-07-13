package kd.bos.nurserystock.outstoragePlugin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import json.JSONObject;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.algo.RowMeta;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.form.FieldTip;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.nurserystock.instoragePlugin.InStorageBillPlugin;
import kd.bos.nurserystock.serviceHelper.ABillServiceHelper;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.coderule.CodeRuleServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 出库订单明细单据插件
 * 
 * @author Administrator
 *
 */
public class OutStorageDetailBillPlugin extends AbstractFormPlugin {
	private static final Log logger = LogFactory.getLog(InStorageBillPlugin.class);
	DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory()
			.getDistributeSessionlessCache("customRegion");

	final String site = cache.get("site");
	final String qty = cache.get("qty") != null ? cache.get("qty") : "";
	final String outsid = cache.get("outsid");

	final String formId = "zsf_outstorageorderdetail";

	public void registerListener(final EventObject e) {
		super.registerListener(e);
		Toolbar toolBar = (Toolbar) getView().getControl("tbmain");
		toolBar.addItemClickListener(this);
		this.addClickListeners("bar_save");
	}

	@Override
	public void beforeDoOperation(BeforeDoOperationEventArgs args) {
		super.beforeDoOperation(args);

	}

	public void itemClick(ItemClickEvent evt) {
		String keyname = evt.getItemKey();
		if (StringUtils.equalsIgnoreCase(keyname, "bar_save")) {
			// 获取编码规则单据编号zsf_checkbox
			DynamicObject dataInfo = this.getModel().getDataEntity();
			BigDecimal qtyOb = (BigDecimal) dataInfo.get("zsf_qty");
			boolean checkBox = (boolean) dataInfo.get("zsf_checkbox");
			DynamicObject nameOb = (DynamicObject) dataInfo.get("zsf_name");
			double realQty = qtyOb.doubleValue();
			String name = nameOb != null ? nameOb.get("id").toString() : "";

			int qty = (int) Math.floor(realQty);

			// 先查询库存数量

			String algoKey = getClass().getName() + ".query_resume";
			String sql = "select d.fid,d.fk_zsf_name,d.fk_zsf_type,d.fk_zsf_qrcode,d.fk_zsf_rfid,d.fk_zsf_qty,d.fk_zsf_sale_price from tk_zsf_instorage i left join tk_zsf_instorage_detail d "
					+ "on i.fid = d.fk_zsf_insid "
					+ "where i.fk_zsf_site =?  and d.fk_zsf_name =? and d.fk_zsf_store_status='0'";
			Object[] params = { site, name };
			List<JSONObject> list = new ArrayList<JSONObject>();
			try (DataSet ds = DB.queryDataSet(algoKey, DBRoute.of("fa"), sql, params)) {
				RowMeta md = ds.getRowMeta();
				int columnCount = md.getFieldCount();
				while (ds.hasNext()) {
					Row row = ds.next();
					Map<String, Object> rowData = new HashMap<String, Object>();
					for (int k = 0; k < columnCount; k++) {
						rowData.put(md.getField(k).toString(), row.get(k));
					}
					JSONObject json = new JSONObject(rowData);
					list.add(json);
				}
			}

			// 批量录入
			if (qty != 0 && checkBox) {
				// 获取编码规则批次编码号,注意：会消耗编码流水号
				String[] numbers = CodeRuleServiceHelper.getBatchNumber(this.getView().getEntityId().toString(),
						dataInfo, null, qty);

				for (int i = 0; i < qty; i++) {

					if (list.size() > 0) {
						Object[] objResult = null;
						if (i > 0) {
							DynamicObject dynobj = BusinessDataServiceHelper.newDynamicObject(formId);
							// dynobj.set("billno", numbers[i]);
							dynobj.set("zsf_name", list.get(i).get("fk_zsf_name"));
							dynobj.set("zsf_type", list.get(i).get("fk_zsf_type"));
							dynobj.set("zsf_ground_diameter", dataInfo.get("zsf_ground_diameter"));
							dynobj.set("zsf_dbh", dataInfo.get("zsf_dbh"));
							dynobj.set("zsf_coronal_diameter", dataInfo.get("zsf_coronal_diameter"));
							dynobj.set("zsf_meter_diameter", dataInfo.get("zsf_meter_diameter"));
							dynobj.set("zsf_height", dataInfo.get("zsf_height"));
							dynobj.set("zsf_storestatus", dataInfo.get("zsf_storestatus"));
							dynobj.set("zsf_remark", dataInfo.get("zsf_remark"));
							dynobj.set("createtime", dataInfo.get("createtime"));
							dynobj.set("billstatus", dataInfo.get("billstatus"));
							dynobj.set("creator", dataInfo.get("creator"));
							dynobj.set("modifier", dataInfo.get("modifier"));
							dynobj.set("modifytime", dataInfo.get("modifytime"));
							dynobj.set("zsf_outsid", dataInfo.get("zsf_outsid"));
							dynobj.set("number", numbers[i]);

							dynobj.set("zsf_sale_price", dataInfo.get("zsf_sale_price"));
							dynobj.set("zsf_buy_price", dataInfo.get("zsf_buy_price"));
							dynobj.set("zsf_estimate_price", dataInfo.get("zsf_estimate_price"));
							dynobj.set("zsf_qrcode", list.get(i).get("fk_zsf_qrcode"));
							dynobj.set("zsf_rfid", list.get(i).get("fk_zsf_rfid"));
							dynobj.set("zsf_qty", 1);
							objResult = SaveServiceHelper.save(new DynamicObject[] { dynobj });

						} else {
							dataInfo.set("zsf_qrcode", list.get(i).get("fk_zsf_qrcode"));
							dataInfo.set("zsf_rfid", list.get(i).get("fk_zsf_rfid"));
							dataInfo.set("zsf_qty", 1);
							objResult = SaveServiceHelper.save(new DynamicObject[] { dataInfo });

						}

						logger.info("出库明细批量保存：" + objResult);
						// 更新入库明细对应id数据的库存状态为出库
						if (objResult.length > 0) {
							Object[] param = { list.get(i).get("fid") };
							String updateSql = "update tk_zsf_instorage_detail set fk_zsf_store_status = '1' where fid =? ";
							boolean updateFlag = DB.execute(DBRoute.basedata, updateSql, param);
							logger.info("出库保存成功并更新入库库存状态：" + updateFlag);
						} else {
							// 指定回收的编号
							CodeRuleServiceHelper.autoRecycleNumber(formId);
							boolean recycleNum = CodeRuleServiceHelper.recycleNumber(formId, dataInfo, null,
									numbers[i]);
							logger.info("保存失败回收编码：" + recycleNum);
						}

					} else {
						// 指定回收的编号
						CodeRuleServiceHelper.autoRecycleNumber(formId);
						boolean recycleNum = CodeRuleServiceHelper.recycleNumber(formId, dataInfo, null, numbers[i]);
						logger.info("保存失败回收编码：" + recycleNum);
						this.getView().showTipNotification("库存数量不足");
						break;
					}
				}
			} else {
				OperationResult operationResult = ABillServiceHelper.saveOperate(this.getView());
				logger.info("出库明细保存：" + operationResult.getMessage());
				if (!operationResult.isSuccess()) {
					// 指定回收的编号
					CodeRuleServiceHelper.autoRecycleNumber(formId);
					boolean recycleNum = CodeRuleServiceHelper.recycleNumber(formId, dataInfo, null, dataInfo.getString("billno"));
					logger.info("保存失败回收编码：" + recycleNum);
				}
			}
		}
	}

	@Override
	public void afterCreateNewData(EventObject e) {
		// 从缓存中获取入库id
		if (outsid != null) {
			// 设置入库明细单编码
			this.getModel().setValue("zsf_outsid", outsid);
		}

	}

	@Override
	public void propertyChanged(PropertyChangedArgs e) {
		String propName = e.getProperty().getName();
		// 获取品名中的缩略图并显示
		if (propName.equals("zsf_name")) {
			DynamicObject pictureOb = (DynamicObject) this.getModel().getValue("zsf_name");
			Long id = (Long) pictureOb.get("id");
			QFilter[] filters = { ((id != null) ? new QFilter("id", "=", id) : null) };
			Map<Object, DynamicObject> map = BusinessDataServiceHelper.loadFromCache("zsf_nurseryname",
					"id,name,number,zsf_picture,zsf_nurserytype", filters);
			String picture = null;
			DynamicObject zsf_nurserytype = null;
			for (Iterator<java.util.Map.Entry<Object, DynamicObject>> ite = map.entrySet().iterator(); ite.hasNext();) {
				@SuppressWarnings("rawtypes")
				Map.Entry entry = (java.util.Map.Entry) ite.next();
				DynamicObject object = (DynamicObject) entry.getValue(); // 拿到表单对象
				picture = (String) object.getString("zsf_picture");
				zsf_nurserytype =  object.getDynamicObject("zsf_nurserytype");
			}
			this.getModel().setValue("zsf_picture", picture);
			this.getModel().setValue("zsf_type", zsf_nurserytype);
		}
		if (propName.equals("zsf_qty")) {
			// 校验剩余可出库数量
			DynamicObject nameOb = (DynamicObject) this.getModel().getValue("zsf_name");
			String name = nameOb != null ? nameOb.get("id").toString() : "";

			int columnCount = 0;
			String algoKey = getClass().getName() + ".query_resume";
			String sql = "select d.fid from tk_zsf_instorage i left join tk_zsf_instorage_detail d "
					+ "on i.fid = d.fk_zsf_insid "
					+ "where i.fk_zsf_site =?  and d.fk_zsf_name =? and d.fk_zsf_store_status='0'";
			Object[] params = { site, name };
			try (DataSet ds = DB.queryDataSet(algoKey, DBRoute.of("fa"), sql, params)) {
				while (ds.hasNext()) {
					Row row = ds.next();
					if(row.getInteger(0)!= null) {
						columnCount = row.getInteger(0);
					}	
				}
			}

			BigDecimal qtyOb = (BigDecimal) this.getModel().getValue("zsf_qty");
			double realQty = qtyOb.doubleValue();
			FieldTip qtyFieldTip = new FieldTip(FieldTip.FieldTipsLevel.Info, FieldTip.FieldTipsTypes.others, "zsf_qty",
					"数量不能大于库存数量" + columnCount);
			if (realQty > columnCount) {
				this.getView().showFieldTip(qtyFieldTip);
			} else {
				qtyFieldTip.setSuccess(true);
				this.getView().showFieldTip(qtyFieldTip);
			}

		}

	}

}
