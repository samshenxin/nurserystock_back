package kd.bos.nurserystock.maintenancePlugin;

import java.util.EventObject;
import java.util.List;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IListModel;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.filter.FilterContainer;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;

/**
 * 标签注销管理
 * 
 * @author Administrator
 *
 */
public class CancelLabelListPlugin extends AbstractListPlugin {
	final String formId = "zsf_cancel_label";
	/** 筛选组织：优先按用户在过滤面板中，设置的组织筛选数据；列表初始化时，按当前组织筛选数据 */
	private long orgId = 0;

	@Override
	public void afterCreateNewData(EventObject e) {
		FilterContainer container = getControl("filtercontainerap");
		container.setBillFormId(formId);

		super.afterCreateNewData(e);
	}

	public void registerListener(final EventObject e) {
		super.registerListener(e);
		Toolbar toolBar = (Toolbar) getView().getControl("toolbarap");
		toolBar.addItemClickListener(this);
	}

	public void itemClick(ItemClickEvent evt) {
//		String keyname = evt.getItemKey();
//		if (StringUtils.equalsIgnoreCase(keyname, "tblcancel")) {
//			Object[] objResult = null;
//			DynamicObject dataInfo = this.getModel().getDataEntity();
//			IListView listV = (IListView) this.getView();
//			ListSelectedRowCollection rows = listV.getSelectedRows();
//			for (ListSelectedRow row : rows) {
//				objResult = SaveServiceHelper.save(new DynamicObject[] { dataInfo });
//				System.out.println(objResult);
//			}
//		}
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
		List<QFilter> filters = e.getQFilters();
		// 增加入库单id及状态过滤
		final QFilter filterLabelStat = new QFilter("zsf_labelstatus", QFilter.not_equals, "2");// 标签使用状态
		final QFilter filterStatus = new QFilter("zsf_storestatus", QFilter.not_equals, '0');// 非入库状态
		filters.add(qfilter);
		filters.add(filterLabelStat);
		filters.add(filterStatus);
		e.setQFilters(filters);
		super.setFilter(e);

	}

}
