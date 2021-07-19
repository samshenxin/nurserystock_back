package jdy.zsf.nurserystock.outstoragePlugin;

import java.math.BigDecimal;
import java.util.EventObject;
import java.util.List;

import kd.bos.algo.DataSet;
import kd.bos.algo.RowMeta;
import kd.bos.bill.OperationStatus;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.FieldTip;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import jdy.zsf.nurserystock.instoragePlugin.InStorageBillPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
/**
 * 出库明细列表插件
 * @author Administrator
 *
 */
public class OutStorageDetailListPlugin extends AbstractListPlugin {
	private static final Log logger = LogFactory.getLog(OutStorageDetailListPlugin.class);

	DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory()
			.getDistributeSessionlessCache("customRegion");

	private final static String KEY_ORDERNAME = "zsf_ordername";
	/** 筛选组织：优先按用户在过滤面板中，设置的组织筛选数据；列表初始化时，按当前组织筛选数据 */
	private long orgId = 0;

	public void registerListener(final EventObject e) {
		super.registerListener(e);
		Toolbar toolBar = (Toolbar) getView().getControl("toolbarap");
		toolBar.addItemClickListener(this);
	}

	public void itemClick(ItemClickEvent evt) {
		String keyname = evt.getItemKey();
		if (StringUtils.equalsIgnoreCase(keyname, "tblclose")) {
			cache.remove("outsid");// 移除缓存

		}
	}

	@Override
	public void afterCreateNewData(EventObject e) {
		// 获取父页面传过来的参数
		String outsid = this.getView().getFormShowParameter().getCustomParam("zsf_outsid");
		String status = this.getView().getFormShowParameter().getCustomParam("status");
		if (outsid != null) {
			cache.put("outsid", outsid);// 将出库单据id参数加入缓存
		}
		if (status != null && !"A".equals(status)) {
			//单据状态不是暂存的，不允许新增/提交新增
			this.getView().setVisible(false, "tblnew");
		}

		cache.put("qty", this.getView().getFormShowParameter().getCustomParam("qty").toString());
		cache.put("site", this.getView().getFormShowParameter().getCustomParam("site").toString());
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
//		QFilter qfilter = helper.getBaseDataFilter("zsf_outstorageorderdetail", this.orgId);
//		// if(qfilter != null){
//		List<QFilter> filters = e.getQFilters();
//		// 增加入库单id过滤
//		final QFilter filterOutsid = new QFilter("zsf_outsid", "=", cache.get("outsid"));
//		final QFilter filterStatus = new QFilter("zsf_storestatus", "=", '1');
//		filters.add(qfilter);
//		filters.add(filterOutsid);
//		filters.add(filterStatus);
//		e.setQFilters(filters);
//		// }
//		super.setFilter(e);

	}
	
	

}
