package kd.bos.asset.commonPlugin;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import com.alibaba.druid.util.StringUtils;

import kd.bos.asset.utils.DateUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.AbstractFormDataModel;
import kd.bos.entity.datamodel.TableValueSetter;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.FieldEdit;
import kd.bos.form.plugin.AbstractFormPlugin;
/**
 * 
 * @ClassName:  InventoryPlugin   
 * @Description:TODO(盘点管理插件)   
 * @author: sam
 * @date:   2021-3-7 14:38:26      
 * @Copyright:
 */
public class InventoryPlugin extends AbstractFormPlugin {

	// 单据体上的菜单栏名（高级面板工具栏）
	private final static String KEY_TOOLBAR_INVEN = "zsf_toolbarap_inventory";
	// 可用单据体名
	private final static String KEY_ENTRYENTITY_INVEN = "zsf_inventory_entryentity";
	// 页面工具栏控件标识
	private static final String KEY_ITEM_SEARCH = "zsf_searchdata";
	// 查询条件
//	private final static String KEY_FIELDSETPANELAP = "zsf_search";

	@Override
	public void registerListener(EventObject e) {
		super.registerListener(e);

		// 侦听单据体菜单栏按钮点击事件
		Toolbar toolbar_select = this.getView().getControl(KEY_TOOLBAR_INVEN);
		toolbar_select.addItemClickListener(this);

	}

	@Override
	public void itemClick(ItemClickEvent evt) {
		super.itemClick(evt);
		if (StringUtils.equals(KEY_ITEM_SEARCH, evt.getItemKey())) {

//			// 使用数据模型的方法获取基础资料数据
//			MainEntityType mainType = this.getModel().getDataEntityType();
//			DynamicObject billObj = this.getModel().getDataEntity();
//			BasedataProp assettypeProp = (BasedataProp) mainType.findProperty("zsf_search_assettype");
//			BasedataProp affdeptProp = (BasedataProp) mainType.findProperty("zsf_deptment");
//			BasedataProp usedeptProp = (BasedataProp) mainType.findProperty("zsf_search_usedept");
//			BasedataProp areaProp = (BasedataProp) mainType.findProperty("zsf_search_area");
//
//			DynamicObject assettypeObj = (DynamicObject) assettypeProp.getValue(billObj);
//			DynamicObject affdeptObj = (DynamicObject) affdeptProp.getValue(billObj);
//			DynamicObject usedeptObj = (DynamicObject) usedeptProp.getValue(billObj);
//			DynamicObject areaObj = (DynamicObject) areaProp.getValue(billObj);
//			Long assettypeId = null;
//			Long affdeptId = null;
//			Long areaId = null;
//			Long usedeptId = null;
//			if (assettypeObj != null) {
//				assettypeId = (Long) assettypeObj.get("id");
//			}
//			if (affdeptObj != null) {
//				affdeptId = (Long) affdeptObj.get("id");
//			}
//			if (areaObj != null) {
//				areaId = (Long) areaObj.get("id");
//			}
//			if (usedeptObj != null) {
//				usedeptId = (Long) usedeptObj.get("id");
//			}
//			// 基础资料过滤条件
//			QFilter assettypeFilter = new QFilter("zsf_assettype", "=", assettypeId);
//			QFilter deptFilter = new QFilter("zsf_department", "=", affdeptId);
//			QFilter areaFilter = new QFilter("zsf_area", "=", areaId);
//			QFilter usedeptFilter = new QFilter("zsf_usedept", "=", usedeptId);
//			QFilter[] filters = { (usedeptId != null ? usedeptFilter : null),
//					(assettypeId != null ? assettypeFilter : null), (affdeptId != null ? deptFilter : null),
//					(areaId != null ? areaFilter : null) };
//
//			String fields = "billno,billstatus,zsf_rfid,zsf_assetname,zsf_qrcode,zsf_assettype,zsf_department,zsf_usedept,zsf_user,zsf_area,zsf_store,zsf_spec,zsf_unit,zsf_amount,zsf_qty,zsf_use_timelimit,zsf_buydate,zsf_supplier,zsf_remark,createtime,creator,zsf_status";
//			DynamicObjectCollection struCol = QueryServiceHelper.query("zsf_entering", fields, filters);
//			// 绑定字段
//			this.getModel().deleteEntryData(KEY_ENTRYENTITY_INVEN);
//			AbstractFormDataModel model = (AbstractFormDataModel) this.getModel();
//			model.clearDirty();
//			model.beginInit();
//			EntryGrid entryGrid = this.getControl(KEY_ENTRYENTITY_INVEN);
//			List<FieldEdit> fieldList = entryGrid.getFieldEdits();
//			TableValueSetter setter = new TableValueSetter();
//			for (int i = 0; i < fieldList.size(); i++) {
//				setter.addField(fieldList.get(i).getKey());
//			}
//			// 绑定选中数据到表单中
//			for (int i = 0; i < struCol.size(); i++) {
//				setter.addRow(struCol.get(i).getString("billno"), struCol.get(i).getString("zsf_rfid"),
//						struCol.get(i).getString("zsf_assetname"), struCol.get(i).getString("zsf_qrcode"), 
//						struCol.get(i).getString("zsf_assettype"), struCol.get(i).getString("zsf_department"),
//						struCol.get(i).getString("zsf_usedept"), struCol.get(i).getString("zsf_user"),
//						struCol.get(i).getString("zsf_area"), struCol.get(i).getString("zsf_store"),
//						struCol.get(i).get("zsf_status"));
//
//			}
//			model.batchCreateNewEntryRow(KEY_ENTRYENTITY_INVEN, setter);
//			model.endInit();
//			this.getView().updateView(KEY_ENTRYENTITY_INVEN);
			
			
				// TODO 在此添加业务逻辑						
				FormShowParameter listShowParameter = new FormShowParameter();	
				
				// 设置FormId，列表的FormId（列表表单模板）			
				listShowParameter.setFormId("zsf_import_invdata");			
				// 设置BillFormId，为列表所对应单据的标识			
//				listShowParameter.setBillFormId("zsf_entering");			
				// 设置弹出页面标题			
				listShowParameter.setCaption("选择：");			
				// 设置弹出页面的打开方式			
				listShowParameter.getOpenStyle().setShowType(ShowType.Modal);			
//				StyleCss styleCss = new StyleCss();			
//				styleCss.setWidth("1000");			
//				styleCss.setHeight("600");			
//				listShowParameter.getOpenStyle().setInlineStyleCss(styleCss);			
				// 设置为不能多选，如果为true则表示可以多选			
//				listShowParameter.setMultiSelect(true);			
				// 设置页面回调事件方法			
				listShowParameter.setCloseCallBack(new CloseCallBack(this, "zsf_returndata"));			
				// 绑定子页面到当前页面			
				this.getView().showForm(listShowParameter);					
			
		}
	}

	@Override
	public void closedCallBack(ClosedCallBackEvent e) {		
		super.closedCallBack(e);		
		@SuppressWarnings("unchecked")
		HashMap<String, DynamicObjectCollection> returnData = (HashMap<String, DynamicObjectCollection>) e.getReturnData();
		DynamicObjectCollection objList = returnData.get("zsf_import_inv_entity");
		this.getModel().deleteEntryData(KEY_ENTRYENTITY_INVEN);
		AbstractFormDataModel model = (AbstractFormDataModel) this.getModel();
		model.clearDirty();
		model.beginInit();
		EntryGrid entryGrid = this.getControl(KEY_ENTRYENTITY_INVEN);
		List<FieldEdit> fieldList = entryGrid.getFieldEdits();
		TableValueSetter setter = new TableValueSetter();
		for (int i = 0; i < fieldList.size(); i++) {
			setter.addField(fieldList.get(i).getKey());
		}
		//获取未盘点状态的id
//		String querySql = "select fid from tk_zsf_checkstatus where fnumber=?";
//		Object[] params = { "s0001" };//未盘点
//		List<Long> idList = DB.query(DBRoute.basedata, querySql, params, rs -> {
//			List<Long> ret = new ArrayList<>();
//			while (rs.next()) {
//				ret.add(rs.getLong(1));
//			}
//			return ret;
//		});
		// 绑定选中数据到表单中
		for (int i = 0; i < objList.size(); i++) {
			// 基础资料数据转换
			DynamicObject assettypeObj = (DynamicObject) objList.get(i).get("zsf_assettype");
			DynamicObject affdeptObj = (DynamicObject) objList.get(i).get("zsf_department");
			DynamicObject areaObj = (DynamicObject) objList.get(i).get("zsf_area");
			DynamicObject statusObj = (DynamicObject) objList.get(i).get("zsf_status");

			Long assettypeId = null;
			if (assettypeObj != null)
				assettypeId = assettypeObj.getLong("id");
			Long affdeptId = null;
			if (affdeptObj != null)
				affdeptId = affdeptObj.getLong("id");
			Long areaId = null;
			if (areaObj != null)
				areaId = areaObj.getLong("id");
			Long statusId = null;
			if(statusObj != null)
				statusId = statusObj.getLong("id");
			setter.addRow(objList.get(i).getString("zsf_billno"), objList.get(i).getString("zsf_rfid"),
					objList.get(i).getString("zsf_assetname"), objList.get(i).getString("zsf_qrcode"), assettypeId,
					affdeptId, objList.get(i).getString("zsf_usedept"), objList.get(i).getString("zsf_user"),
					areaId, objList.get(i).getString("zsf_store"),statusId,"未盘点");

		}
		model.batchCreateNewEntryRow(KEY_ENTRYENTITY_INVEN, setter);
		model.endInit();
		this.getView().updateView(KEY_ENTRYENTITY_INVEN);
//		this.getView().close();
	}




}
