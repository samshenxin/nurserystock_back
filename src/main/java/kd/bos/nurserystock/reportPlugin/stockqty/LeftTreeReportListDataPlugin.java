package kd.bos.nurserystock.reportPlugin.stockqty;

import kd.bos.algo.DataSet;
import kd.bos.algo.GroupbyDataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.FilterItemInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

public class LeftTreeReportListDataPlugin extends AbstractReportListDataPlugin {

	@Override
	public DataSet query(ReportQueryParam reportQueryParam, Object arg1) throws Throwable {
		// 获取过滤条件
		FilterInfo filterInfo = reportQueryParam.getFilter();
		FilterItemInfo itemSite = filterInfo.getFilterItem("zsf_search_basedata_site");

		Long siteId = null;
		QFilter siteQFilter = null;

		// 构造QFilter
		if (itemSite.getValue() instanceof DynamicObject) {
			DynamicObject dynSite = (DynamicObject) itemSite.getValue();
			siteId = (Long) dynSite.get("id");
			siteQFilter = new QFilter("zsf_site", QCP.equals, siteId);

		}
		
		DataSet dataSet = QueryServiceHelper.queryDataSet(this.getClass().getName(), "zsf_instorageorder",
				"id, zsf_site", siteQFilter == null ? null : siteQFilter.toArray(), null);
		
		//对结果dataSet进行按照用户名字分组处理
		GroupbyDataSet groupby = dataSet.groupBy(new String[]{"zsf_site"});
		//对分组结果计数并命名别名count
		groupby = groupby.count("zsf_qty");
		//调用finish得到最终结果集
		 DataSet dataSet3 = groupby.finish();
		
		return dataSet3;
	}

}
