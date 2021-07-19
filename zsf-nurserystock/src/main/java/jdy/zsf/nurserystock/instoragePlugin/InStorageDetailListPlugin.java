package jdy.zsf.nurserystock.instoragePlugin;

import java.util.EventObject;
import java.util.List;
import java.util.Map;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;

/**
 * 入库明细列表插件
 * 
 * @author Administrator
 *
 */
public class InStorageDetailListPlugin extends AbstractListPlugin {
	private static final Log logger = LogFactory.getLog(InStorageBillPlugin.class);

	DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory()
			.getDistributeSessionlessCache("customRegion");

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
			cache.remove("insid");// 移除缓存
			cache.remove("zsf_qty");

		}
	}

	@Override
	public void afterCreateNewData(EventObject e) {

		// 获取父页面传过来的参数
		Map<?, ?> mapParam = this.getView().getFormShowParameter().getCustomParams();

		String insid = mapParam.get("zsf_insid") == null ? "" : mapParam.get("zsf_insid").toString();
		String status = mapParam.get("status") == null ? "" : mapParam.get("status").toString();
		String zsf_qty = mapParam.get("zsf_qty") == null ? "" : mapParam.get("zsf_qty").toString();
		String zsf_site = mapParam.get("zsf_site") == null ? "" : mapParam.get("zsf_site").toString();

		cache.put("insid", insid);// 将入库单据id参数加入缓存
		cache.put("zsf_qty", zsf_qty);// 将入库数量参数加入缓存
		cache.put("zsf_site", zsf_site);// 将苗场参数加入缓存
		if (!"A".equals(status)) {
			// 单据状态不是暂存的，不允许新增/提交新增
			this.getView().setVisible(false, "tblnew");
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
		BaseDataServiceHelper helper = new BaseDataServiceHelper();
		@SuppressWarnings("static-access")
		QFilter qfilter = helper.getBaseDataFilter("zsf_instorageorderdetail", this.orgId);
		// if(qfilter != null){
		List<QFilter> filters = e.getQFilters();
		// 增加入库单id及状态过滤
		final QFilter filterInsid = new QFilter("zsf_insid", "=", cache.get("insid"));
		// final QFilter filterStatus = new QFilter("zsf_storestatus", "=", '0');
		filters.add(qfilter);
		filters.add(filterInsid);
		// filters.add(filterStatus);
		e.setQFilters(filters);
		// }
		super.setFilter(e);

	}

}
