package kd.bos.nurserystock.reportPlugin.stockqty;

import kd.bos.algo.DataSet;
import kd.bos.algo.GroupbyDataSet;
import kd.bos.algo.JoinDataSet;
import kd.bos.algo.JoinType;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.entity.report.*;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

import java.util.List;

public class RightFormReportListDataPlugin extends AbstractReportListDataPlugin {

	@Override
	public List<AbstractReportColumn> getColumns(List<AbstractReportColumn> columns) throws Throwable {

		columns.add(createReportColumn("zsf_site", ReportColumn.TYPE_BASEDATA, "苗场"));
		columns.add(createReportColumn("zsf_name", ReportColumn.TYPE_BASEDATA, "品名"));
		columns.add(createReportColumn("zsf_type", ReportColumn.TYPE_BASEDATA, "类型"));
		columns.add(createReportColumn("zsf_qty", ReportColumn.TYPE_QTY, "数量"));

		return super.getColumns(columns);
	}

	public ReportColumn createReportColumn(String fieldKey, String fieldType, String caption) {
		ReportColumn column = new ReportColumn();
		column.setFieldKey(fieldKey);
		column.setFieldType(fieldType);
		column.setCaption(new LocaleString(caption));
		return column;
	}

	@Override
	public DataSet query(ReportQueryParam reportQueryParam, Object o) throws Throwable {

		DynamicObject site = (DynamicObject) ((DynamicObject) o).get("zsf_site");
		QFilter siteQFilter = new QFilter("zsf_site", QCP.equals, site.get("id"));

		DataSet dataSet2 = QueryServiceHelper.queryDataSet(this.getClass().getName(), "zsf_instorageorderdetail",
				"id, billno, number, zsf_insid, zsf_type, zsf_name, zsf_qty", null, null);

		DataSet dataSet = QueryServiceHelper.queryDataSet(this.getClass().getName(), "zsf_instorageorder",
				"id, billno, zsf_site", siteQFilter == null ? null : siteQFilter.toArray(), null);

		JoinDataSet join = dataSet2.join(dataSet, JoinType.INNER);

		DataSet result = join.on("zsf_insid", "id")
				.select(new String[] { "zsf_type", "zsf_name", "zsf_qty" }, new String[] { "zsf_site" }).finish();
		//对结果dataSet进行按照用户名字分组处理
		GroupbyDataSet groupby = result.groupBy(new
				String[]{"zsf_type","zsf_name","zsf_site"});
		//对分组结果计数并命名别名count
		groupby = groupby.sum("zsf_qty");
		//调用finish得到最终结果集
		DataSet dataSet3 = groupby.finish();

		return dataSet3;

	}
}
