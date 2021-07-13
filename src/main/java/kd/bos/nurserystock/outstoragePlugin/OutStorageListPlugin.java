package kd.bos.nurserystock.outstoragePlugin;

import java.math.BigDecimal;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Map;

import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.ShowType;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.HyperLinkClickArgs;
import kd.bos.list.BillList;
import kd.bos.list.ListShowParameter;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
/**
 * 出库订单列表插件
 * @author Administrator
 *
 */
public class OutStorageListPlugin extends AbstractListPlugin {
	private final static Log log = LogFactory.getLog(OutStorageListPlugin.class);

	private final static String KEY_ORDERNAME = "zsf_ordername";

	public void registerListener(final EventObject e) {
		super.registerListener(e);
		Toolbar toolBar = (Toolbar) getView().getControl("toolbarap");
		toolBar.addItemClickListener(this);
	}

	public void itemClick(ItemClickEvent evt) {
		String keyname = evt.getItemKey();
		if (StringUtils.equalsIgnoreCase(keyname, "tblnew")) {
			// System.out.println(number);

		}
	}

	/**
	 * 用户点击超链接单元格时，触发此事件
	 */
	@Override
	public void billListHyperLinkClick(HyperLinkClickArgs args) {
		super.billListHyperLinkClick(args);
		int selectRow = args.getRowIndex();
		BillList billList = (BillList) args.getHyperLinkClickEvent().getSource();
		ListSelectedRowCollection lsrc = billList.getCurrentListAllRowCollection();
		//根据选中数据id获取数据状态
    	QFilter[] filters = { ((lsrc.get(selectRow) != null ) ? new QFilter("id", "=", lsrc.get(selectRow).toString()) : null)};
    	Map<Object, DynamicObject> map = BusinessDataServiceHelper.loadFromCache("zsf_outstorageorder", "id,name,number,zsf_qty,zsf_site,billstatus", filters);
    	String billstatus = "";
    	BigDecimal qty = null ;
    	Long site =  0L;
    	for(Iterator<java.util.Map.Entry<Object, DynamicObject>> ite = map.entrySet().iterator();ite.hasNext();) {
    		@SuppressWarnings("rawtypes")
			Map.Entry entry = (java.util.Map.Entry) ite.next();
    		DynamicObject object = (DynamicObject) entry.getValue(); //拿到表单对象
    		billstatus = (String) object.getString("billstatus");
    		qty =  (BigDecimal) object.get("zsf_qty");
    		DynamicObject zsf_site = (DynamicObject) object.get("zsf_site");
    		site = (Long) zsf_site.get("id");
    	}
		if (StringUtils.equals(KEY_ORDERNAME, args.getHyperLinkClickEvent().getFieldName())) {

			// 取消系统自动打开本单的处理
			args.setCancel(true);

			// 打开新增界面
			ListShowParameter showParameter = new ListShowParameter();
			showParameter.setFormId("bos_list");
			// showParameter.getOpenStyle().setShowType(ShowType.Modal);
			showParameter.setStatus(OperationStatus.ADDNEW);

			showParameter.setCustomParam("zsf_outsid", lsrc.get(selectRow).toString());
			showParameter.setCustomParam("qty", qty.toString());
			showParameter.setCustomParam("site", site);
			showParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
			showParameter.setBillFormId("zsf_outstorageorderdetail");
			// showParameter.setCloseCallBack(new CloseCallBack(this, KEY_TASKRULE));

			this.getView().showForm(showParameter);
		}
	}

}
