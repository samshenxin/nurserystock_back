package kd.bos.asset.inventoryPlugin;

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
 * @Description:TODO(盘点记录加载数据插件)   
 * @author: sam
 * @date:   2021-3-7 14:38:26      
 * @Copyright:
 */
public class InventoryRecord extends AbstractListPlugin {
	// 页面工具栏控件标识
	private static final String KEY_ITEM_LOAD = "zsf_loaddata";

	@Override
	public void beforeItemClick(BeforeItemClickEvent evt){
	
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
				formShowParameter.setCaption("资产盘点数据");
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
//		BillList list = this.getView().getControl("bos_listoptiontpl");
		//刷新列表
//		list.refresh();
		this.getView().updateView();

	}
}
