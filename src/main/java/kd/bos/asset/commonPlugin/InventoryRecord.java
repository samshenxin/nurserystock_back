package kd.bos.asset.commonPlugin;

import com.alibaba.druid.util.StringUtils;

import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.list.BillList;
import kd.bos.list.plugin.AbstractListPlugin;
/**
 * 
 * @ClassName:  InventoryPlugin   
 * @Description:TODO(盘点记录管理插件)   
 * @author: sam
 * @date:   2021-3-7 14:38:26      
 * @Copyright:
 */
public class InventoryRecord extends AbstractListPlugin {
	// 页面工具栏控件标识
	private static final String KEY_ITEM_LOAD = "zsf_loaddata";

	@Override
	public void beforeItemClick(BeforeItemClickEvent evt) {
	
	}
	

	@Override
	public void itemClick(ItemClickEvent evt) {
		super.itemClick(evt);
		if (StringUtils.equals(KEY_ITEM_LOAD, evt.getItemKey())) {

			FormShowParameter showParameter = this.getView().getFormShowParameter();
			Long inventorytaskid = showParameter.getCustomParam("inventorytaskid");

				// TODO 在此添加业务逻辑						
				FormShowParameter formShowParameter = new FormShowParameter();	
				// 设置FormId，列表的FormId（列表表单模板）			
				formShowParameter.setFormId("zsf_inventorylist");			
				// 设置BillFormId，为列表所对应单据的标识			
//				formShowParameter.setBillFormId("zsf_entering");			
				// 设置弹出页面标题			
				formShowParameter.setCaption("选择：");
				formShowParameter.setCustomParam("inventorytaskid", inventorytaskid);
				// 设置弹出页面的打开方式			
				formShowParameter.getOpenStyle().setShowType(ShowType.Modal);			
//				StyleCss styleCss = new StyleCss();			
//				styleCss.setWidth("1000");			
//				styleCss.setHeight("600");			
//				formShowParameter.getOpenStyle().setInlineStyleCss(styleCss);			
				// 设置为不能多选，如果为true则表示可以多选			
//				formShowParameter.setMultiSelect(true);			
				// 设置页面回调事件方法			
				formShowParameter.setCloseCallBack(new CloseCallBack(this, "zsf_loaddata"));			
				// 绑定子页面到当前页面			
				this.getView().showForm(formShowParameter);					
			
		}
	}

	@Override
	public void closedCallBack(ClosedCallBackEvent e) {		
		super.closedCallBack(e);	
		BillList list = this.getView().getControl("bos_listoptiontpl");
		//刷新列表
		list.refresh();
		this.getView().updateView();

//		@SuppressWarnings("unchecked")
//		HashMap<String, DynamicObjectCollection> returnData = (HashMap<String, DynamicObjectCollection>) e.getReturnData();
//		DynamicObjectCollection objList = returnData.get("fa_inventory_reco");
//		
//		Container myFldPanel = this.getView().getControl("zsf_search");
//		Object assetName = myFldPanel.getModel().getValue("zsf_search_assetname");
//		// 根据选中的部门获取对应的人员
//		QFilter nameFilter = new QFilter("assetname", "=", assetName);
//		nameFilter = nameFilter.or(new QFilter("assetname", "like", assetName + "," + "%"));
//		nameFilter = nameFilter.or(new QFilter("assetname", "like", "%," + assetName));
//		nameFilter = nameFilter.or(new QFilter("assetname", "like", "%," + assetName + "," + "%"));
//
//		// 使用数据模型的方法获取基础资料数据
//		MainEntityType mainType = this.getModel().getDataEntityType();
//		DynamicObject billObj = this.getModel().getDataEntity();
//		BasedataProp assettypeProp = (BasedataProp) mainType.findProperty("zsf_search_assettype");
//		BasedataProp usedeptProp = (BasedataProp) mainType.findProperty("zsf_search_usedept");
//
//		DynamicObject assettypeObj = (DynamicObject) assettypeProp.getValue(billObj);
//		DynamicObject usedeptObj = (DynamicObject) usedeptProp.getValue(billObj);
//
//		Long assettypeId = null;
//		Long usedeptId = null;
//		if (assettypeObj != null) {
//			assettypeId = (Long) assettypeObj.get("id");
//		}
//		if (usedeptObj != null) {
//			usedeptId = (Long) usedeptObj.get("id");
//		}
//		// 基础资料过滤条件
//		QFilter assettypeFilter = new QFilter("assetcat", "=", assettypeId);
//		QFilter usedeptFilter = new QFilter("headusedept", "=", usedeptId);
//		QFilter[] filters = { ((assetName != null && !assetName.equals("")) ? nameFilter : null),
//				(assettypeId != null ? assettypeFilter : null), (usedeptId != null ? usedeptFilter : null),
//				new QFilter("billstatus", "=", "C") };
//
//		String fields = "billno,number,zsf_rfid,assetname,model,assetamount";
//		DynamicObjectCollection struCol = QueryServiceHelper.query("fa_card_real", fields, filters);
//		// 绑定字段
//		this.getModel().deleteEntryData("zsf_import_inv_entity");
//		AbstractFormDataModel model = (AbstractFormDataModel) this.getModel();
//		model.clearDirty();
//		model.beginInit();
//		EntryGrid entryGrid = this.getControl("zsf_import_inv_entity");
//		List<FieldEdit> fieldList = entryGrid.getFieldEdits();
//		TableValueSetter setter = new TableValueSetter();
//		for (int i = 0; i < fieldList.size(); i++) {
//			setter.addField(fieldList.get(i).getKey());
//		}
//		// 绑定选中数据到表单中
//		for (int i = 0; i < struCol.size(); i++) {
//			setter.addRow(struCol.get(i).getString("billno"), struCol.get(i).getString("number"),
//					struCol.get(i).getString("zsf_rfid"), struCol.get(i).getString("assetname"),
//					struCol.get(i).getString("model"), struCol.get(i).get("assetamount"));
////						struCol.get(i).get("billstatus"));
//
//		}
//		model.batchCreateNewEntryRow("zsf_import_inv_entity", setter);
//		model.endInit();
//		this.getView().updateView("zsf_import_inv_entity");
//		this.getView().close();
	}




}
