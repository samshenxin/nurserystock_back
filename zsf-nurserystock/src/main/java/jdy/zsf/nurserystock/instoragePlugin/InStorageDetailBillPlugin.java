package jdy.zsf.nurserystock.instoragePlugin;

import java.math.BigDecimal;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Map;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.FieldTip;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import jdy.zsf.nurserystock.utils.IdUtils;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.coderule.CodeRuleServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 入库明细单据插件
 * 
 * @author Administrator
 *
 */
public class InStorageDetailBillPlugin extends AbstractFormPlugin {
	private static final Log logger = LogFactory.getLog(InStorageBillPlugin.class);
	DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory()
			.getDistributeSessionlessCache("customRegion");

	final String insid = cache.get("insid");
	final String zsf_qty = cache.get("zsf_qty");
	final String zsf_site = cache.get("zsf_site");
	final String formId = "zsf_instorageorderdetail";

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
			BigDecimal qtyOb = (BigDecimal) this.getModel().getValue("zsf_qty");
			double value = qtyOb.doubleValue();
			boolean checkBox = (boolean) this.getModel().getValue("zsf_checkbox");

			int qty = (int) Math.floor(value);
			// 获取编码规则单据编号zsf_checkbox
			DynamicObject dataInfo = this.getModel().getDataEntity();

			// 批量录入
			if (value != 0 && checkBox) {
				// 获取编码规则批次编码号
				String[] numbers = CodeRuleServiceHelper.getBatchNumber(this.getView().getEntityId().toString(),
						dataInfo, null, qty);

				Object[] objResult = null;
				for (int i = 0; i < qty; i++) {

					if (i > 0) {
						DynamicObject dynobj = BusinessDataServiceHelper.newDynamicObject(formId);
						// dynobj.set("billno", numbers[i]);
						dynobj.set("zsf_name", dataInfo.get("zsf_name"));
						dynobj.set("zsf_type", dataInfo.get("zsf_type"));
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
						dynobj.set("zsf_insid", dataInfo.get("zsf_insid"));
						dynobj.set("number", numbers[i]);

						// dynobj.set("zsf_sale_price", dataInfo.get("zsf_sale_price"));
						dynobj.set("zsf_estimate_price", dataInfo.get("zsf_estimate_price"));
						dynobj.set("zsf_qrcode", IdUtils.fastSimpleUUID());
						dynobj.set("zsf_rfid", IdUtils.getGUID());
						dynobj.set("zsf_buy_price", dataInfo.get("zsf_buy_price"));
						dynobj.set("zsf_qty", 1);
						dynobj.set("zsf_site", dataInfo.get("zsf_site"));
						objResult = SaveServiceHelper.save(new DynamicObject[] { dynobj });

					} else {
						dataInfo.set("zsf_qty", 1);
						objResult = SaveServiceHelper.save(new DynamicObject[] { dataInfo });
					}
					if (objResult == null) {
						boolean recycleNum = CodeRuleServiceHelper.recycleNumber(formId, dataInfo, null, numbers[i]);
						logger.info("保存失败回收编码：" + recycleNum);
					}
					logger.info("入库明细批量保存：" + objResult);

				}

			} else {

				Object[] objResult = SaveServiceHelper.save(new DynamicObject[] { dataInfo });
				logger.info("入库明细保存：" + objResult);
				if (objResult == null) {
					// 指定回收的编号
					CodeRuleServiceHelper.autoRecycleNumber(formId);
					boolean recycleNum = CodeRuleServiceHelper.recycleNumber(formId, dataInfo, null,
							dataInfo.getString("billno"));
					logger.info("保存失败回收编码：" + recycleNum);
				}
			}
		}
	}

	@Override
	public void afterCreateNewData(EventObject e) {
		// 从缓存中获取入库id
		if (insid != null) {
			// 设置入库明细单编码
			this.getModel().setValue("zsf_insid", insid);
			this.getModel().setValue("zsf_site", zsf_site);
		}
		// 设置二维码和RFID
		this.getModel().setValue("zsf_rfid", IdUtils.getGUID());
		this.getModel().setValue("zsf_qrcode", IdUtils.fastSimpleUUID());

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
				zsf_nurserytype = object.getDynamicObject("zsf_nurserytype");
			}
			this.getModel().setValue("zsf_picture", picture);
			this.getModel().setValue("zsf_type", zsf_nurserytype);
		}
		if (propName.equals("zsf_qty")) {
			// 校验入库总数

			int columnCount = 0;
			String algoKey = getClass().getName() + ".query_resume";
			String sql = "select sum(d.fk_zsf_qty) as qty from tk_zsf_instorage i left join tk_zsf_instorage_detail d "
					+ "on i.fid = d.fk_zsf_insid " + " where d.fk_zsf_insid =? ";
			Object[] params = { insid };
			try (DataSet ds = DB.queryDataSet(algoKey, DBRoute.of("fa"), sql, params)) {
				while (ds.hasNext()) {
					Row row = ds.next();
					if (row.getInteger(0) != null) {
						columnCount = row.getInteger(0);
					}
				}
			}

			BigDecimal qtyOb = (BigDecimal) this.getModel().getValue("zsf_qty");
			double realQty = qtyOb.doubleValue() + columnCount;// 输入数量+实际库存数量
			double count = Double.parseDouble(zsf_qty);
			FieldTip qtyFieldTip = new FieldTip(FieldTip.FieldTipsLevel.Info, FieldTip.FieldTipsTypes.others, "zsf_qty",
					"本订单库存数量" + columnCount + ",大于入库总数" + count);
			if (realQty > count) {
				this.getView().showFieldTip(qtyFieldTip);
			} else {
				qtyFieldTip.setSuccess(true);
				this.getView().showFieldTip(qtyFieldTip);
			}
		}
		if(propName.equals("zsf_checklabel")) {
			boolean checkLabel = (boolean) this.getModel().getValue("zsf_checklabel");
			if(checkLabel) {
				//挂牌后，默认标签状态为使用中
				this.getModel().setValue("zsf_labelstatus", "0");
			}else {//无标签无状态
				this.getModel().setValue("zsf_labelstatus", "2");
			}
		}

	}

}

