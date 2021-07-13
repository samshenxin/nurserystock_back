package kd.bos.asset.commonPlugin;

import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import com.alibaba.druid.util.StringUtils;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.datamodel.AbstractFormDataModel;
import kd.bos.entity.datamodel.TableValueSetter;
import kd.bos.entity.property.BasedataProp;
import kd.bos.form.FormShowParameter;
import kd.bos.form.container.Container;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.field.FieldEdit;
import kd.bos.id.ID;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

public class InventoryList extends AbstractBillPlugIn {

	// 查询条件
	private final static String KEY_FIELDSETPANELAP = "zsf_search";
	// 单据体上的菜单栏名（高级面板工具栏）
	private final static String KEY_TOOLBAR_IMPORTDATA = "zsf_toolbarap_invdata";
	// 可用单据体名
	private final static String KEY_ENTRYENTITY_SEARCH = "zsf_import_inv_entity";
	// 生成查询菜单名
	private final static String KEY_ITEM_SEARCH = "zsf_search_invdata";
	// 页面确认按钮标识
	private final static String KEY_OK = "btnok";
	// 页面取消按钮标识
	private final static String KEY_CANCEL = "btncancel";

	@Override
	public void registerListener(EventObject e) {
		super.registerListener(e);

		// 侦听单据体菜单栏按钮点击事件
		Toolbar toolbar_select = this.getView().getControl(KEY_TOOLBAR_IMPORTDATA);
		toolbar_select.addItemClickListener(this);

		// 页面确认按钮和取消按钮添加监听
		this.addClickListeners(KEY_OK, KEY_CANCEL);

	}

	public void itemClick(ItemClickEvent evt) {
		super.itemClick(evt);
		// 点击查询事件
		if (StringUtils.equals(KEY_ITEM_SEARCH, evt.getItemKey())) {
			FormShowParameter showParameter = this.getView().getFormShowParameter();
			Long inventorytaskid = showParameter.getCustomParam("inventorytaskid");
			String fields = "id,inventperson,inventschemeid";// 主键id，盘点人，任务id
			DynamicObjectCollection taskIds = QueryServiceHelper.query("fa_inventory_task", fields,
					new QFilter[] { new QFilter("id", "=", inventorytaskid) });

			Container myFldPanel = this.getView().getControl(KEY_FIELDSETPANELAP);
			Object assetName = myFldPanel.getModel().getValue("zsf_search_assetname");
			// 根据选中的部门获取对应的人员
			QFilter nameFilter = new QFilter("assetname", "=", assetName);
			nameFilter = nameFilter.or(new QFilter("assetname", "like", assetName + "," + "%"));
			nameFilter = nameFilter.or(new QFilter("assetname", "like", "%," + assetName));
			nameFilter = nameFilter.or(new QFilter("assetname", "like", "%," + assetName + "," + "%"));

			// 使用数据模型的方法获取基础资料数据
			MainEntityType mainType = this.getModel().getDataEntityType();
			DynamicObject billObj = this.getModel().getDataEntity();
			BasedataProp assettypeProp = (BasedataProp) mainType.findProperty("zsf_search_assettype");
			BasedataProp usedeptProp = (BasedataProp) mainType.findProperty("zsf_search_usedept");

			DynamicObject assettypeObj = (DynamicObject) assettypeProp.getValue(billObj);
			DynamicObject usedeptObj = (DynamicObject) usedeptProp.getValue(billObj);

			Long assettypeId = null;
			Long usedeptId = null;
			if (assettypeObj != null) {
				assettypeId = (Long) assettypeObj.get("id");
			}
			if (usedeptObj != null) {
				usedeptId = (Long) usedeptObj.get("id");
			}
			// 基础资料过滤条件
			QFilter assettypeFilter = new QFilter("assetcat", "=", assettypeId);
			QFilter usedeptFilter = new QFilter("headusedept", "=", usedeptId);
			QFilter[] filters = { ((assetName != null && !assetName.equals("")) ? nameFilter : null),
					(assettypeId != null ? assettypeFilter : null), (usedeptId != null ? usedeptFilter : null),
					new QFilter("billstatus", "=", "C") };

			fields = "billno,number,zsf_rfid,assetname,model,assetamount,assetcat,unit,supplier,usestatus,headusedept,headuseperson,storeplace";
			DynamicObjectCollection struCol = QueryServiceHelper.query("fa_card_real", fields, filters);
			// 绑定字段
			this.getModel().deleteEntryData(KEY_ENTRYENTITY_SEARCH);
			AbstractFormDataModel model = (AbstractFormDataModel) this.getModel();
			model.clearDirty();
			model.beginInit();
			EntryGrid entryGrid = this.getControl(KEY_ENTRYENTITY_SEARCH);
			List<FieldEdit> fieldList = entryGrid.getFieldEdits();
			TableValueSetter setter = new TableValueSetter();
			for (int i = 0; i < fieldList.size(); i++) {
				setter.addField(fieldList.get(i).getKey());
			}
			// 绑定选中数据到表单中
			for (int i = 0; i < struCol.size(); i++) {
				setter.addRow(struCol.get(i).getString("billno"), struCol.get(i).getString("number"),
						struCol.get(i).getString("zsf_rfid"), struCol.get(i).getString("assetname"),
						struCol.get(i).getString("model"), struCol.get(i).get("assetamount"),
						taskIds.get(0).getString("inventschemeid"), taskIds.get(0).getString("inventperson"),
						taskIds.get(0).getString("id"), "", struCol.get(i).getString("assetcat"),
						struCol.get(i).getString("headusedept"), struCol.get(i).getString("headuseperson"),
						struCol.get(i).getString("storeplace"), struCol.get(i).getString("usestatus"),
						struCol.get(i).getString("unit"), struCol.get(i).getString("supplier"));

			}
			model.batchCreateNewEntryRow(KEY_ENTRYENTITY_SEARCH, setter);
			model.endInit();
			this.getView().updateView(KEY_ENTRYENTITY_SEARCH);
		}

	}

	@Override
	public void click(EventObject evt) {
		super.click(evt);
		Control source = (Control) evt.getSource();
		// 获取被点击的控件对象
		if (StringUtils.equals(source.getKey(), KEY_OK)) {
			HashMap<String, DynamicObjectCollection> hashMap = new HashMap<>();
			EntryGrid entryGrid = this.getView().getControl(KEY_ENTRYENTITY_SEARCH);
			int[] rows = entryGrid.getSelectRows();
			if (null != rows && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					DynamicObject entity = this.getModel().getEntryEntity(KEY_ENTRYENTITY_SEARCH).get(rows[i]);
					Long taskid = (Long) entity.get("zsf_detailid");
					DynamicObject schemeid = (DynamicObject) entity.get("zsf_inventschemeid");
					DynamicObject userid = (DynamicObject) entity.get("zsf_inventperson");
					Long supplier = entity.getDynamicObject("zsf_supplier") != null
							? entity.getDynamicObject("zsf_supplier").getLong("id")
							: 0;
					Long usePerson = entity.getDynamicObject("zsf_headuseperson") != null
							? entity.getDynamicObject("zsf_headuseperson").getLong("id")
							: 0;
					// 查询是否已存在未盘点的相同数据
					QFilter nameFilter = new QFilter("number", "=", entity.get("zsf_number"));
					QFilter rfidFilter = new QFilter("zsf_rfid", "=", entity.get("zsf_rfid"));
					QFilter taskidFilter = new QFilter("inventorytask", "=", taskid);
					QFilter stateFilter = new QFilter("inventorystate", "=", "B");// 未盘点
					QFilter[] filters = { nameFilter, rfidFilter, taskidFilter, stateFilter };
					String fields = "id,number,inventorytask";
					DynamicObjectCollection assets = QueryServiceHelper.query("fa_inventory_record", fields, filters);
					if (assets.size() > 0)
						continue;

					// 插入盘点数据到盘点记录表
					String sql = "insert into t_fa_inventory_record "
							+ "(FID,FNUMBER,fk_zsf_rfid,FBARCODE,FNAME,FMODEL,FBOOKQUANTITY,FINVENTORYQUANTITY,FDIFFERENCE,FINVENTORYUSER,"
							+ "FINVENTORYSTATE,FINVENTORYWAY,FINVENTORYTIME,FREASON,FINVENTSCHEMEENTRYID,FINVENTORYTASKID,fk_zsf_qrcode,fk_zsf_assetcatid,"
							+ "fk_zsf_unitid,fk_zsf_supplierid,fk_zsf_usestatusid,fk_zsf_headusedeptid,fk_zsf_headusepersonid,fk_zsf_storeplaceid) "
							+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
					Object[] params = { ID.genLongId(),
							entity.get("zsf_number") != null ? entity.get("zsf_number") : "",
							entity.get("zsf_rfid") != null ? entity.get("zsf_rfid") : "", "",
							entity.get("zsf_assetname") != null ? entity.get("zsf_assetname") : "",
							entity.get("zsf_model") != null ? entity.get("zsf_model") : "",
							entity.get("zsf_qty") != null ? entity.get("zsf_qty") : "", 0, 0,
							userid != null ? userid.getLong("id") : 0, 'B', 'C', new Date(), "",
							schemeid != null ?schemeid.getLong("id"):0, taskid,
							entity.getString("zsf_qrcode") != null ? entity.getString("zsf_qrcode") : "",
							entity.getDynamicObject("zsf_assettype") != null
									? entity.getDynamicObject("zsf_assettype").getLong("id")
									: 0,
							entity.getDynamicObject("zsf_unit") != null
									? entity.getDynamicObject("zsf_unit").getLong("id")
									: 0,
							supplier,
							entity.getDynamicObject("zsf_status") != null
									? entity.getDynamicObject("zsf_status").getLong("id")
									: 0,
							entity.getDynamicObject("zsf_usedept") != null
									? entity.getDynamicObject("zsf_usedept").getLong("id")
									: 0,
							usePerson,
							entity.getDynamicObject("zsf_storeplace") != null
									? entity.getDynamicObject("zsf_storeplace").getLong("id")
									: 0 };

					DB.execute(DBRoute.basedata, sql, params);
				}
			}
			this.getView().returnDataToParent(hashMap);
			this.getView().close();
		} else if (StringUtils.equals(source.getKey(), KEY_CANCEL)) {
			// 被点击控件为取消则设置返回值为空并关闭页面（在页面关闭回调方法中必须验证返回值不为空，否则会报空指针）
			this.getView().returnDataToParent(null);
			this.getView().close();
		}
	}

}
