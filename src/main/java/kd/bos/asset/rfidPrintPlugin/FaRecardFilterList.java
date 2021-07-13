package kd.bos.asset.rfidPrintPlugin;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.encoding.XMLType;

import org.apache.axis.client.Call;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.validate.BillStatus;
import kd.bos.form.IFormView;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.FilterContainerInitArgs;
import kd.bos.form.events.FilterContainerSearchClickArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.form.field.events.BeforeFilterF7SelectEvent;
import kd.bos.list.BillList;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.JSONUtils;
import kd.fi.fa.business.dao.impl.FaDaoOrmImpl;
import kd.fi.fa.common.util.ThrowableHelper;
import kd.fi.fa.utils.FaFormPermissionUtil;

public class FaRecardFilterList extends AbstractListPlugin {
	private static Log logger;
	private static Map<String, QFilter> filterMap;
	private static String namespaceURI = "http://com.ssh.rfidprint/GPCardService/";
	
	public void itemClick(final ItemClickEvent evt) {
		super.itemClick(evt);
		final String key = evt.getItemKey();
		String isPrint = null;
		final String s = key;
		switch (s) {
		case "zsf_rfidprint": { //tblprint
			Object[] keyValue = null;
			if (this.getView() instanceof IListView) {
				final ListSelectedRowCollection rows = ((IListView) this.getView()).getSelectedRows();
				keyValue = rows.getPrimaryKeyValues();
			} else if (this.getView() instanceof IFormView) {
				keyValue = new Object[] { this.getView().getModel().getDataEntity().getPkValue() };
			}
			final DynamicObject[] realCards = BusinessDataServiceHelper.load("fa_card_real",
					"id,barcode,billno,number,assetname,zsf_rfid,headusedept,storeplace,usedate",
					new QFilter[] { new QFilter("id", "in", (Object) keyValue) });
			DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory()
					.getDistributeSessionlessCache("customRegion");
			String[] arr = {};
			for (int i=0; i< realCards.length;i++) {
				DynamicObject usedept = (DynamicObject)realCards[i].get("headusedept");
				DynamicObject storeplace = (DynamicObject)realCards[i].get("storeplace");
				String dept = null;
				String replace = null;
				if (usedept != null)
					dept = usedept.getString("name");
				if (storeplace != null)
					replace = storeplace.getString("name");
				
				StringBuilder sb = new StringBuilder();
//				arr = insert(arr, obj.get("id").toString(), obj.getString("barcode"), obj.getString("number"),
//						obj.getString("assetname"), obj.getString("zsf_rfid"), obj.getString("headusedept"),
//						obj.getString("storeplace"), obj.getString("usedate"));
				sb.append(realCards[i].get("id").toString()+";");
				sb.append(realCards[i].getString("barcode")+";");
				sb.append(realCards[i].getString("number")+";");
				sb.append(realCards[i].getString("assetname")+";");
				sb.append(realCards[i].getString("zsf_rfid")+";");
				sb.append(dept+";");
				sb.append(replace+";");
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = null;
				String usedate = realCards[i].getString("usedate");
				try {
					date = (Date) sf.parse(usedate);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sb.append(sf.format(date)+";");
				sb.append(realCards[i].getString("billno"));
				cache.put("realCards",sb.toString());//将自定义参数加入缓存
				String result = pushDataToClient(keyValue[i].toString());//推送数据（参数暂时没有用到）
				if(!"SUCCESS".equals(result))
					this.getView().showTipNotification(result, Integer.valueOf(5000));

			}

			// RFIDPrint rp = new RFIDPrint(realCards);
			// isPrint = FaRealCardBarCodeCheckUtil.checkPrint(this.getView());
			// if (isPrint != null) {
			// throw new KDBizException(isPrint);
			// }
			break;
		}
		}
	}

	/**
	 * webservice推送数据到客户端
	 * 
	 * @param entry
	 */
	public String pushDataToClient(String keyValue) {

		String result = null;
		try {
			String wsUrl = "http://192.168.1.148:8066/GPCardService/DataService?wsdl";
			org.apache.axis.client.Service serv = new org.apache.axis.client.Service();
			Call call = null;
			call = (Call) serv.createCall();
			call.setTargetEndpointAddress(new URL(wsUrl));
			call.setOperationName(new QName(namespaceURI, "PushDataToClient"));
			call.addParameter(new QName(namespaceURI, "persoID"), XMLType.SOAP_ARRAY, ParameterMode.IN);
			call.setUseSOAPAction(true);
			call.setReturnType(XMLType.XSD_STRING);
			call.setSOAPActionURI("http://com.ssh.rfidprint/GPCardService/PushDataToClient");
			result = (String) call.invoke(new Object[] { keyValue });

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	// 往字符串数组追加新数据
	private static String[] insert(String[] arr, String... str) {
		int size = arr.length; // 获取原数组长度
		int newSize = size + str.length; // 原数组长度加上追加的数据的总长度

		// 新建临时字符串数组
		String[] tmp = new String[newSize];
		// 先遍历将原来的字符串数组数据添加到临时字符串数组
		for (int i = 0; i < size; i++) {
			tmp[i] = arr[i];
		}
		// 在末尾添加上需要追加的数据
		for (int i = size; i < newSize; i++) {
			tmp[i] = str[i - size];
		}
		return tmp; // 返回拼接完成的字符串数组
	}

	public void registerListener(final EventObject e) {
		super.registerListener(e);
		this.addItemClickListeners(new String[] { "toolbarap" });
	}

	public void setFilter(final SetFilterEvent e) {
		final String billFormId = ((BillList) e.getSource()).getBillFormId();
		final QFilter defaultFilter = this.getDefaultFilter(billFormId);
		if (defaultFilter != null) {
			final List<QFilter> qFilters = (List<QFilter>) e.getQFilters();
			qFilters.add(defaultFilter);
		}
		this.disposeClosePeriod(e);
		if (billFormId.equals("fa_card_real_base")) {
			final String orderBy = "number asc";
			e.setOrderBy(orderBy);
		}
	}

	private void disposeClosePeriod(final SetFilterEvent e) {
		final Map<String, Object> customParams = (Map<String, Object>) this.getView().getFormShowParameter()
				.getCustomParams();
		if (customParams.get("filter") != null) {
			final String filterStr = (String) customParams.get("filter");
			if (StringUtils.isNotBlank((CharSequence) filterStr)) {
				final QFilter filter = QFilter.fromSerializedString(filterStr);
				e.getQFilters().clear();
				e.getQFilters().add(filter);
			}
		}
	}

	private QFilter getDefaultFilter(final String billFormId) {
		return FaRecardFilterList.filterMap.get(billFormId);
	}

	private static QFilter and(final QFilter a, final QFilter b) {
		return a.and(b);
	}

	public void filterContainerInit(final FilterContainerInitArgs filtercontainerinitargs) {
		super.filterContainerInit(filtercontainerinitargs);
		try {
			final String linkQueryInfoJson = this.getPageCache().get("filtercontainerap_linkQueryPkIdCollection");
			final List linkQueryInfoList = (List) JSONUtils.cast(linkQueryInfoJson, (Class) List.class);
			if (!linkQueryInfoList.isEmpty()) {
				final Map<String, Object> linkQueryInfo = (Map<String, Object>) linkQueryInfoList.get(0);
				final Object pkId = linkQueryInfo.get("pkId");
				final DynamicObject realCard = BusinessDataServiceHelper.loadSingle(pkId, "fa_card_real", "assetunit");
				final DynamicObject assetUnit = (DynamicObject) realCard.get("assetunit");
				this.getPageCache().put("assetunit", assetUnit.getPkValue().toString());
			}
		} catch (IOException e) {
			FaRecardFilterList.logger.error(ThrowableHelper.toString((Throwable) e));
		}
		FaFormPermissionUtil.filterContainerInit(filtercontainerinitargs, this.getPageCache());
	}

	public void filterContainerSearchClick(final FilterContainerSearchClickArgs args) {
		super.filterContainerSearchClick(args);
		FaFormPermissionUtil.filterContainerSearchClick(args, this.getPageCache());
	}

	public void filterContainerBeforeF7Select(final BeforeFilterF7SelectEvent e) {
		super.filterContainerBeforeF7Select(e);
		FaFormPermissionUtil.filterContainerBeforeF7Select(e, "fa_card_real");
	}

	static {
		FaRecardFilterList.logger = LogFactory.getLog((Class) FaRecardFilterList.class);
		(FaRecardFilterList.filterMap = new HashMap<String, QFilter>()).put("fa_purchasebill", null);
		FaRecardFilterList.filterMap.put("fa_card_real",
				and(new QFilter("isbak", "=", (Object) "0"), new QFilter("bizstatus", "!=", (Object) "DELETE")));
		FaRecardFilterList.filterMap.put("fa_card_real_base", new QFilter("isbak", "=", (Object) "0"));
		FaRecardFilterList.filterMap.put("fa_initcard_real",
				and(new QFilter("id", "=", (Object) "masterid", true), new QFilter("initialcard", "=", (Object) "1")));
		FaRecardFilterList.filterMap.put("fa_initcard_fin", new QFilter("bizperiod", "=", (Object) 0L));
		FaRecardFilterList.filterMap.put("fa_clearapplybill", null);
		FaRecardFilterList.filterMap.put("fa_clearbill", null);
		FaRecardFilterList.filterMap.put("fa_assetcategory", null);
		FaRecardFilterList.filterMap.put("fa_usestatus", null);
		FaRecardFilterList.filterMap.put("fa_storeplace", null);
		FaRecardFilterList.filterMap.put("fa_assetbook", null);
		FaRecardFilterList.filterMap.put("fa_depre", new QFilter("status", "=", (Object) BillStatus.C.toString()));
		FaRecardFilterList.filterMap.put("fa_periodclose",
				new QFilter("status", "=", (Object) BillStatus.C.toString()));
		FaRecardFilterList.filterMap.put("fa_test_geninsertsql", null);
		FaRecardFilterList.filterMap.put("fa_depre_sum", null);
		FaRecardFilterList.filterMap.put("fa_dispatch", null);
		FaRecardFilterList.filterMap.put("fa_dispatch_in",
				and(new QFilter("billstatus", "!=", (Object) BillStatus.A.toString()),
						new QFilter("billstatus", "!=", (Object) BillStatus.B.toString())));
		FaRecardFilterList.filterMap.put("fa_depremethod", null);
		FaRecardFilterList.filterMap.put("fa_depre_workload", new QFilter(
				FaDaoOrmImpl.dot(new String[] { "fincard", "depremethod", "number" }), "=", (Object) "400"));
		FaRecardFilterList.filterMap.put("fa_rpt_card", null);
		FaRecardFilterList.filterMap.put("fa_changemode", null);
	}
}
