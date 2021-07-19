package jdy.zsf.nurserystock.maintenancePlugin;

import java.util.EventObject;
import java.util.List;

import kd.bos.context.RequestContext;
import kd.bos.filter.FilterContainer;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;

/**
 * 名木管理
 * 
 * @author Administrator
 *
 */
public class RareWoodBillPlugin extends AbstractListPlugin {
	final String formId = "zsf_boslist_rarewood";
	/** 筛选组织：优先按用户在过滤面板中，设置的组织筛选数据；列表初始化时，按当前组织筛选数据 */
	private long orgId = 0;
	
	@Override
	public void afterCreateNewData(EventObject e) {
		FilterContainer container = getControl("filtercontainerap");
		container.setBillFormId(formId);		
		
		super.afterCreateNewData(e);
	}
	
	/***
	 * 在开始对列表数据进行过滤取数前，触发此事件
	 * 
	 * @remark 1. 使用本地组织值，生成列表过滤条件，添加到列表过滤条件中
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void setFilter(SetFilterEvent e) {
		this.orgId = RequestContext.get().getOrgId();
		if (this.orgId == 0) {
			return;
		}
		// 调用基础封装的帮助类，生成组织筛选条件：
		// 返回的条件，可能包含了数据授权信息
		BaseDataServiceHelper helper = new BaseDataServiceHelper();
		@SuppressWarnings("static-access")
		QFilter qfilter = helper.getBaseDataFilter("zsf_instorageorderdetail", this.orgId);
		// if(qfilter != null){
		List<QFilter> filters = e.getQFilters();
		// 名木过滤
		final QFilter filterInsid = new QFilter("zsf_checkexpensive", "=", "1");
		filters.add(qfilter);
		filters.add(filterInsid);
		e.setQFilters(filters);
		// }
		super.setFilter(e);

	}
}
