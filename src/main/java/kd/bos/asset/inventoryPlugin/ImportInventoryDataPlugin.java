package kd.bos.asset.inventoryPlugin;

import java.util.ArrayList;
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
import kd.bos.form.container.Container;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.CellClickEvent;
import kd.bos.form.control.events.CellClickListener;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.control.events.RowClickEvent;
import kd.bos.form.control.events.RowClickEventListener;
import kd.bos.form.control.events.SelectRowsEventListener;
import kd.bos.form.field.FieldEdit;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

/**
 * 
 * @ClassName: ImportInventoryDataPlugin
 * @Description:TODO(导入盘点资产数据，弹出动态表单)
 * @author: sam
 * @date: 2021-2-27 16:58:28
 * @Copyright:
 */
public class ImportInventoryDataPlugin extends AbstractBillPlugIn {
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
			Container myFldPanel = this.getView().getControl(KEY_FIELDSETPANELAP);
			Object assetName = myFldPanel.getModel().getValue("zsf_search_assetname");
			// 根据选中的部门获取对应的人员
			QFilter nameFilter = new QFilter("zsf_assetname", "=", assetName);
			nameFilter = nameFilter.or(new QFilter("zsf_assetname", "like", assetName + "," + "%"));
			nameFilter = nameFilter.or(new QFilter("zsf_assetname", "like", "%," + assetName));
			nameFilter = nameFilter.or(new QFilter("zsf_assetname", "like", "%," + assetName + "," + "%"));

			// 使用数据模型的方法获取基础资料数据
			MainEntityType mainType = this.getModel().getDataEntityType();
			DynamicObject billObj = this.getModel().getDataEntity();
			BasedataProp assettypeProp = (BasedataProp) mainType.findProperty("zsf_search_assettype");
			BasedataProp affdeptProp = (BasedataProp) mainType.findProperty("zsf_search_deptment");
			BasedataProp areaProp = (BasedataProp) mainType.findProperty("zsf_search_area");
			BasedataProp usedeptProp = (BasedataProp) mainType.findProperty("zsf_search_usedept");

			DynamicObject assettypeObj = (DynamicObject) assettypeProp.getValue(billObj);
			DynamicObject affdeptObj = (DynamicObject) affdeptProp.getValue(billObj);
			DynamicObject areaObj = (DynamicObject) areaProp.getValue(billObj);
			DynamicObject usedeptObj = (DynamicObject) usedeptProp.getValue(billObj);

			Long assettypeId = null;
			Long affdeptId = null;
			Long areaId = null;
			Long usedeptId = null;
			if (assettypeObj != null) {
				assettypeId = (Long) assettypeObj.get("id");
			}
			if (affdeptObj != null) {
				affdeptId = (Long) affdeptObj.get("id");
			}
			if (areaObj != null) {
				areaId = (Long) areaObj.get("id");
			}
			if (usedeptObj != null) {
				usedeptId = (Long) usedeptObj.get("id");
			}
			// 基础资料过滤条件
			QFilter assettypeFilter = new QFilter("zsf_assettype", "=", assettypeId);
			QFilter deptFilter = new QFilter("zsf_department", "=", affdeptId);
			QFilter areaFilter = new QFilter("zsf_area", "=", areaId);
			QFilter usedeptFilter = new QFilter("zsf_usedept", "=", usedeptId);
			QFilter[] filters = { ((assetName != null && !assetName.equals("")) ? nameFilter : null),
					(assettypeId != null ? assettypeFilter : null), (affdeptId != null ? deptFilter : null),
					(areaId != null ? areaFilter : null), (usedeptId != null ? usedeptFilter : null) };

			String fields = "billno,billstatus,zsf_rfid,zsf_assetname,zsf_qrcode,zsf_assettype,zsf_department,zsf_usedept,zsf_user,zsf_area,zsf_store,zsf_spec,zsf_unit,zsf_amount,zsf_qty,zsf_use_timelimit,zsf_buydate,zsf_supplier,zsf_remark,createtime,creator,zsf_status";
			DynamicObjectCollection struCol = QueryServiceHelper.query("zsf_entering", fields, filters);
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
				setter.addRow(struCol.get(i).getString("billno"), struCol.get(i).getString("zsf_rfid"),
						struCol.get(i).getString("zsf_assetname"), struCol.get(i).getString("zsf_qrcode"),
						struCol.get(i).getString("zsf_assettype"), struCol.get(i).getString("zsf_department"),
						struCol.get(i).getString("zsf_usedept"), struCol.get(i).getString("zsf_user"),
						struCol.get(i).getString("zsf_area"), struCol.get(i).getString("zsf_store"),
						struCol.get(i).get("zsf_status"));

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
			// 如果被点击控件为确认，则获取页面相关控件值，组装数据传入returnData返回给父页面，最后关闭页面
			EntryGrid entryGrid = this.getView().getControl(KEY_ENTRYENTITY_SEARCH);
			int[] rows = entryGrid.getSelectRows();
			DynamicObjectCollection objList = new DynamicObjectCollection();
			if (null != rows && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					DynamicObject entity = this.getModel().getEntryEntity(KEY_ENTRYENTITY_SEARCH).get(rows[i]);
					// 新增页面使用状态修改为：在用
//			entity.set("zsf_status", "1");
					objList.add(entity);
				}
				hashMap.put(KEY_ENTRYENTITY_SEARCH, objList);
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
