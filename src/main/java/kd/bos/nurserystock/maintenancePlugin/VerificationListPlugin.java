package kd.bos.nurserystock.maintenancePlugin;

import java.util.EventObject;
import java.util.List;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.BillList;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;

/**
 * 核销列表插件
 * 
 * @author Administrator
 *
 */
public class VerificationListPlugin extends AbstractListPlugin {
	private final static Log logger = LogFactory.getLog(VerificationListPlugin.class);

	/** 筛选组织：优先按用户在过滤面板中，设置的组织筛选数据；列表初始化时，按当前组织筛选数据 */
	private long orgId = 0;
	final String formId = "zsf_verification";

	public void registerListener(final EventObject e) {
		super.registerListener(e);
		Toolbar toolBar = (Toolbar) getView().getControl("toolbarap");
		toolBar.addItemClickListener(this);
	}

	@Override
	public void itemClick(ItemClickEvent evt) {
		super.itemClick(evt);
		String keyName = evt.getItemKey();
		if ("tbldel".equals(keyName)) {
			BillList list = this.getControl("billlistap");

			ListSelectedRowCollection rowIndexs = list.getSelectedRows();
			System.err.println(rowIndexs);
			// for (ListSelectedRow row : rowIndexs) {
			// Object[] param = { row.getNumber() };
			// String updateSql = "update tk_zsf_instorage_detail set fk_zsf_store_status =
			// '0' where fid =? ";
			// boolean updateFlag = DB.execute(DBRoute.basedata, updateSql, param);
			// logger.info("核销保存成功并更新入库库存状态：" + updateFlag);
			// }

		}
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
//		BaseDataServiceHelper helper = new BaseDataServiceHelper();
//		@SuppressWarnings("static-access")
//		QFilter qfilter = helper.getBaseDataFilter("zsf_verification", this.orgId);
//		// if(qfilter != null){
//		List<QFilter> filters = e.getQFilters();
//		// 增加入库单id过滤
//		final QFilter filterStatus = new QFilter("zsf_storestatus", "=", '2');
//		filters.add(qfilter);
//		// filters.add(filterStatus);
//		e.setQFilters(filters);
//		// }
//		super.setFilter(e);

	}

}
