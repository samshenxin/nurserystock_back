package kd.bos.asset.inventoryPlugin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import kd.bos.algo.DataSet;
import kd.bos.algo.Field;
import kd.bos.algo.Row;
import kd.bos.algo.RowMeta;
import kd.bos.asset.entry.SchemeEntry;
import kd.bos.asset.entry.TaskEntry;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.AppMetadataCache;
import kd.bos.entity.datamodel.AbstractFormDataModel;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.TableValueSetter;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Label;
import kd.bos.form.control.ProgressBar;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.control.events.RowClickEventListener;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.fi.fa.common.util.ContextUtil;
import kd.fi.fa.common.util.PermissonType;

/**
 * 
 * @ClassName: FaInventoryReportExtPlugin
 * @Description:TODO(盘点报告插件)
 * @author: sam
 * @date: 2021-3-20 9:48:06
 * @Copyright:
 */
public class FaInventoryReportExtPlugin extends AbstractFormPlugin
		implements HyperLinkClickListener, RowClickEventListener {
	private static final Log logger = LogFactory.getLog(FaInventoryReportExtPlugin.class);
	private static final String algoXalgoKey = "kd.fi.fa.inventory.report.FaAssetInverntoryPlugin";
	private static final int defaultLength = 5;

	public void registerListener(EventObject e) {
		EntryGrid entryGrid = (EntryGrid) getView().getControl("entryentity");
		entryGrid.addHyperClickListener(this);
		entryGrid.addRowClickListener(this); // 行点击
		Toolbar toolBar = (Toolbar) getView().getControl("toolbar");
		toolBar.addItemClickListener(this);
		Toolbar toolBar2 = (Toolbar) getView().getControl("toolbarap");
		toolBar2.addItemClickListener(this);
	}

	/**
	 * 
	 * <p>
	 * Title: beforeDoOperation
	 * </p>
	 * <p>
	 * Description: 点击盘点审核操作列事件
	 * </p>
	 * 
	 * @param args
	 * @see kd.bos.form.plugin.AbstractFormPlugin#beforeDoOperation(kd.bos.form.events.BeforeDoOperationEventArgs)
	 */
	@Override
	public void beforeDoOperation(BeforeDoOperationEventArgs args) {
		super.beforeDoOperation(args);
		FormOperate formOperate = (FormOperate) args.getSource();
		if (StringUtils.equals("auditpass", formOperate.getOperateKey())) {
			// 盘点审核通过
			EntryGrid entryGrid = this.getView().getControl("entryentity");
			int[] rows = entryGrid.getSelectRows();
			if (null != rows && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					DynamicObject entity = this.getModel().getEntryEntity("entryentity").get(rows[i]);
					String taskid = entity.get("inventorytaskid").toString();
					boolean flag = updateSchemeEntryByTaskID(taskid, "D","C");
					if (!flag) {
						getView().showTipNotification(ResManager.loadKDString("审核出错", "FaInventoryReportExtPlugin_0",
								"kd.bos.asset.inventoryPlugin", new Object[0]));
						return;
					}
				}
			}
		} else if (StringUtils.equals("unauditpass", formOperate.getOperateKey())) {
			// 盘点审核不通过
			EntryGrid entryGrid = this.getView().getControl("entryentity");
			int[] rows = entryGrid.getSelectRows();
			if (null != rows && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					DynamicObject entity = this.getModel().getEntryEntity("entryentity").get(rows[i]);
					String taskid = entity.get("inventorytaskid").toString();
					boolean flag = updateSchemeEntryByTaskID(taskid, "B","B");
					if (!flag) {
						getView().showTipNotification(ResManager.loadKDString("审核出错", "FaInventoryReportExtPlugin_0",
								"kd.bos.asset.inventoryPlugin", new Object[0]));
						return;
					}
					StyleCss styleCss = new StyleCss();			
					styleCss.setWidth("1000");			
					styleCss.setHeight("600");
					
				}
			}
		}
		// 刷新单据
		DynamicObject tempSchemeId = (DynamicObject) getModel().getValue("q_inventoryschemeid");
		if (tempSchemeId == null) {
			getView().showTipNotification(
					ResManager.loadKDString("盘点方案为空", "FaInventoryReportPlugin_0", "kd.bos.asset.inventoryPlugin", new Object[0]));
			return;
		}
		getPageCache().put("refreshscheme", tempSchemeId.getString("id"));
		getModel().createNewData();
	}

	public void itemClick(ItemClickEvent evt) {
		String keyname = evt.getItemKey();
		if (StringUtils.equalsIgnoreCase(keyname, "viewall")) {
			getPageCache().put("showLength", "show");
			String schemeentrys = getPageCache().get("schemeentrys");
			@SuppressWarnings({ "unchecked", "rawtypes" })
			HashMap<String, ArrayList<TaskEntry>> schemeEntrys = (HashMap) SerializationUtils
					.fromJsonString(schemeentrys, HashMap.class);
			initValue(schemeEntrys);
		} else if (StringUtils.equalsIgnoreCase(keyname, "refreshrp")) {
			DynamicObject tempSchemeId = (DynamicObject) getModel().getValue("q_inventoryschemeid");
			if (tempSchemeId == null) {
				getView().showTipNotification(ResManager.loadKDString("盘点方案为空", "FaInventoryReportPlugin_0",
						"kd.bos.asset.inventoryPlugin", new Object[0]));
				return;
			}
			getPageCache().put("refreshscheme", tempSchemeId.getString("id"));
			getModel().createNewData();
		}
	}

	public void afterCreateNewData(EventObject e) {
		if (hasInventoryScheme()) {
			HashMap<String, ArrayList<TaskEntry>> schemeEntrys = getSchemeEntrys();
			if (schemeEntrys.size() <= 0) {
				getView().showTipNotification(ResManager.loadKDString("不存在可访问的盘点方案", "FaInventoryReportPlugin_1",
						"kd.bos.asset.inventoryPlugin", new Object[0]));
				getView().setVisible(Boolean.FALSE, new String[] { "viewall" });
				return;
			}
			getPageCache().put("showLength", "hide");
			getPageCache().put("schemeentrys", SerializationUtils.toJsonString(schemeEntrys));
			initInventoryScheme(schemeEntrys);
			initValue(schemeEntrys);
		}
	}

	public void propertyChanged(PropertyChangedArgs e) {
		String propName = e.getProperty().getName();
		if (propName.equals("q_inventoryschemeid")) {
			String schemeentrys = getPageCache().get("schemeentrys");
			@SuppressWarnings({ "unchecked", "rawtypes" })
			HashMap<String, ArrayList<TaskEntry>> schemeEntrys = (HashMap) SerializationUtils
					.fromJsonString(schemeentrys, HashMap.class);
			getPageCache().put("showLength", "hide");
			initValue(schemeEntrys);
		}
	}

	private void initValue(HashMap<String, ArrayList<TaskEntry>> schemeEntrys) {
		DynamicObject scheme = (DynamicObject) getModel().getValue("q_inventoryschemeid");
		if (scheme == null) {
			getView().showTipNotification(
					ResManager.loadKDString("未选择盘点方案", "FaInventoryReportPlugin_2", "kd.bos.asset.inventoryPlugin", new Object[0]));
			return;
		}
		String schemeId = String.valueOf(scheme.getPkValue());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ArrayList<TaskEntry> schemeEntry = (ArrayList) schemeEntrys.get(schemeId);
		Map<String, TaskEntry> rowEntrys = getTask_Records(schemeId);
		if (schemeEntry != null) {
			schemeEntry = fillEntry(rowEntrys, schemeEntry);
			fillCntAndRate(schemeEntry);
		}
	}

	private ArrayList<TaskEntry> fillEntry(Map<String, TaskEntry> results, ArrayList<TaskEntry> schemeEntry) {
		ArrayList<TaskEntry> schemeEntry2 = new ArrayList<TaskEntry>();
		AbstractFormDataModel model = (AbstractFormDataModel) getModel();
		model.deleteEntryData("entryentity");
		model.beginInit();

		TableValueSetter vs = new TableValueSetter(new String[0]);
		vs.addField("accounting", new Object[0]);
		vs.addField("chargeperson", new Object[0]);
		vs.addField("inventorypercent", new Object[0]);
		vs.addField("papernumber", new Object[0]);
		vs.addField("realnumber", new Object[0]);
		vs.addField("lossnumber", new Object[0]);
		vs.addField("profitnumber", new Object[0]);
		vs.addField("inventorytaskid", new Object[0]);
		vs.addField("zsf_inventorystate", new Object[0]);
		vs.addField("operationcolumn", new Object[0]);
		int allTotal = 0;
		int allHasInvent = 0;
		int length = schemeEntry.size();

		for (int i = 0; i < length; i++) {
			TaskEntry row = getRow(schemeEntry.get(i));
			TaskEntry result = (TaskEntry) results.get(row.getTaskid());

			if (result != null) {
				row.setRealnumber(result.getRealnumber());
				row.setBookquantity(result.getBookquantity());
				row.setLossnumber(result.getLossnumber());
				row.setProfitnumber(result.getProfitnumber());
				row.setSchemeid(result.getSchemeid());
				row.setTaskid(result.getTaskid());
				row.setHasInvent(result.getHasInvent());
				row.setTotal(result.getTotal());

			}
			SchemeEntry sEntry = getTaskInventoryState(row.getTaskid() != null ? row.getTaskid() : "");
			vs.set("accounting", row.getAssetunit(), i);
			vs.set("chargeperson", row.getChargeperson(), i);
			if ("A".equals(row.getStatus())) {
				vs.set("inventorypercent", row.getInventorypercent() + "%", i);
				vs.set("papernumber", Integer.valueOf(row.getBookquantity()), i);
				vs.set("realnumber", Integer.valueOf(row.getRealnumber()), i);
				vs.set("lossnumber", Integer.valueOf(row.getLossnumber()), i);
				vs.set("profitnumber", Integer.valueOf(row.getProfitnumber()), i);
			} else if ("B".equals(row.getStatus())) {
				//已下达的任务，显示进度（后增加）
				row.setInventorypercent(getPercentRate(row.getHasInvent() * 100, row.getTotal()));
				vs.set("inventorypercent", row.getInventorypercent() + "%", i);
			} else {
				//已完成的任务，显示进度情况
				if (row.getBookquantity() == 0) {
					vs.set("inventorypercent", row.getInventorypercent() + "%", i);
				}
				row.setInventorypercent(getPercentRate(row.getHasInvent() * 100, row.getTotal()));
				vs.set("inventorypercent", row.getInventorypercent() + "%", i);
				logger.info(row.getTaskid() + "---" + row.getAssetunit() + "---" + row.getChargeperson() + ":"
						+ row.getBookquantity());
				logger.info("进度" + getPercentRate(row.getHasInvent() * 100, row.getTotal()));
			}
			vs.set("papernumber", Integer.valueOf(row.getBookquantity()), i);
			vs.set("realnumber", Integer.valueOf(row.getRealnumber()), i);
			vs.set("lossnumber", Integer.valueOf(row.getLossnumber()), i);
			vs.set("profitnumber", Integer.valueOf(row.getProfitnumber()), i);
			vs.set("inventorytaskid", row.getTaskid(), i);
			vs.set("zsf_inventorystate", sEntry.getInventoryState(), i);
			vs.set("operationcolumn",
					ResManager.loadKDString("盘点记录", "FaInventoryReportPlugin_3", "kd.bos.asset.inventoryPlugin", new Object[0]), i);
			schemeEntry2.add(row);
		}
		getPageCache().put("alltotal", allTotal + "");
		getPageCache().put("allhasinvent", allHasInvent + "");

		getView().setVisible(Boolean.valueOf(false), new String[] { "viewall" });
		if (("hide".equals(getPageCache().get("showLength"))) && (5 < length)) {
			getView().setVisible(Boolean.valueOf(true), new String[] { "viewall" });

			TableValueSetter vs2 = new TableValueSetter(new String[0]);
			vs.addField("accounting", new Object[0]);
			vs.addField("chargeperson", new Object[0]);
			vs.addField("inventorypercent", new Object[0]);
			vs.addField("papernumber", new Object[0]);
			vs.addField("realnumber", new Object[0]);
			vs.addField("lossnumber", new Object[0]);
			vs.addField("profitnumber", new Object[0]);
			vs.addField("inventorytaskid", new Object[0]);
			vs.addField("zsf_inventorystate", new Object[0]);
			for (int j = 0; j < 5; j++) {
				vs2.set("accounting", vs.get("accounting", j), j);
				vs2.set("chargeperson", vs.get("chargeperson", j), j);
				vs2.set("inventorypercent", vs.get("inventorypercent", j), j);
				vs2.set("papernumber", vs.get("papernumber", j), j);
				vs2.set("realnumber", vs.get("realnumber", j), j);
				vs2.set("lossnumber", vs.get("lossnumber", j), j);
				vs2.set("profitnumber", vs.get("profitnumber", j), j);
				vs2.set("inventorytaskid", vs.get("inventorytaskid", j), j);
				vs.set("zsf_inventorystate", vs.get("zsf_inventorystate", j), j);
				vs2.set("operationcolumn",
						ResManager.loadKDString("盘点记录", "FaInventoryReportPlugin_3", "kd.bos.asset.inventoryPlugin", new Object[0]),
						j);
			}
			vs = vs2;
		}
		model.batchCreateNewEntryRow("entryentity", vs);
		model.endInit();
		getView().updateView("entryentity");
		return schemeEntry2;
	}

	private TaskEntry getRow(Object taskEntry) {
		TaskEntry row = new TaskEntry();
		if ((taskEntry instanceof HashMap)) {
			try {
				BeanUtils.populate(row, (Map) taskEntry);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			row = (TaskEntry) taskEntry;
		}
		return row;
	}

	private Map<String, TaskEntry> getTask_Records(String schemeId) {
		QFilter a1 = new QFilter("inventschemeentry", "in", Long.valueOf(Long.parseLong(schemeId)));
		DataSet result = QueryServiceHelper.queryDataSet("kd.fi.fa.inventory.report.FaAssetInverntoryPlugin",
				"fa_inventory_record", getSelectFields(), new QFilter[] { a1 }, null);

		List<String> allFields = getFields(result);
		allFields.add("case when difference >= 0 then 0 else difference end as lossnumber");

		allFields.add("case when difference <= 0 then 0 else difference end as profitnumber");

		result = result.select((String[]) allFields.toArray(new String[0]));
		Map<String, TaskEntry> reportEntrys = initRowEntrys(result);
		return reportEntrys;
	}

	private Map<String, TaskEntry> initRowEntrys(DataSet result) {
		Map<String, TaskEntry> reportEntrys = new HashMap();
		while (result.hasNext()) {
			Row row = result.next();
			if (reportEntrys.containsKey(row.getString("inventorytask"))) {
				TaskEntry reportEntry = (TaskEntry) reportEntrys.get(row.getString("inventorytask"));
				reportEntry.setRealnumber(reportEntry.getRealnumber() + row.getInteger("inventoryquantity").intValue());
				reportEntry.setBookquantity(reportEntry.getBookquantity() + row.getInteger("bookquantity").intValue());
				int lossnumber = row.getInteger("lossnumber").intValue();
				reportEntry.setLossnumber(reportEntry.getLossnumber() + Math.abs(lossnumber));
				int profitnumber = row.getInteger("profitnumber").intValue();
				reportEntry.setProfitnumber(reportEntry.getProfitnumber() + Math.abs(profitnumber));
				String state = row.getString("inventorystate");
				if ("A".equals(state)) {
					reportEntry.setHasInvent(reportEntry.getHasInvent() + 1);
				}
				reportEntry.setTotal(reportEntry.getTotal() + 1);
			} else {
				TaskEntry reportEntry = new TaskEntry();
				reportEntry.setRealnumber(row.getInteger("inventoryquantity").intValue());
				reportEntry.setBookquantity(row.getInteger("bookquantity").intValue());
				int lossnumber = row.getInteger("lossnumber").intValue();
				reportEntry.setLossnumber(Math.abs(lossnumber));
				int profitnumber = row.getInteger("profitnumber").intValue();
				reportEntry.setProfitnumber(Math.abs(profitnumber));
				reportEntry.setSchemeid(row.getString("inventschemeentry"));
				reportEntry.setTaskid(row.getString("inventorytask"));
				reportEntry.setInventorypercent(0);
				String state = row.getString("inventorystate");
				if ("A".equals(state)) {
					reportEntry.setHasInvent(1);
				} else {
					reportEntry.setHasInvent(0);
				}
				reportEntry.setTotal(reportEntry.getTotal() + 1);
				reportEntrys.put(row.getString("inventorytask"), reportEntry);
			}
		}
		return reportEntrys;
	}

	private int getPercentRate(int hasInvent, int total) {
		if (total == 0) {
			return 0;
		}
		BigDecimal percent = new BigDecimal(0);
		percent = new BigDecimal(hasInvent).divide(new BigDecimal(total), 4);
		return percent.intValue();
	}

	private List<String> getFields(DataSet queryDs) {
		if (queryDs == null) {
			return null;
		}
		RowMeta rowMeta = queryDs.getRowMeta();
		Field[] fields = rowMeta.getFields();
		List<String> selFields = new ArrayList(fields.length);
		for (Field field : fields) {
			selFields.add(field.getAlias());
		}
		return selFields;
	}

	private void fillCntAndRate(ArrayList<TaskEntry> schemeEntry) {
		int countPaper = 0;
		int countReal = 0;
		int countLoss = 0;
		int countProfit = 0;
		int length = schemeEntry.size();
		int percent = 0;
		for (int i = 0; i < length; i++) {
			TaskEntry row = getRow(schemeEntry.get(i));
			countPaper += row.getBookquantity();
			countReal += row.getRealnumber();
			countLoss += row.getLossnumber();
			countProfit += row.getProfitnumber();
			percent += row.getInventorypercent();
		}
		Label label = (Label) getView().getControl("papercnt");
		label.setText(String.valueOf(countPaper));

		Label labelReal = (Label) getView().getControl("realcnt");
		labelReal.setText(String.valueOf(countReal));

		Label labelLoss = (Label) getView().getControl("losscnt");
		labelLoss.setText(String.valueOf(countLoss));

		Label labelProfit = (Label) getView().getControl("profitcnt");
		labelProfit.setText(String.valueOf(countProfit));

		((ProgressBar) getControl("inventoryrate")).setPercent(getPercentRate(percent, length));
	}

	private void initInventoryScheme(HashMap<String, ArrayList<TaskEntry>> schemeEntrys) {
		IDataModel model = getModel();
		Long schemeId = (Long) getView().getFormShowParameter().getCustomParam("schemeId");

		BasedataEdit schemeBd = (BasedataEdit) getControl("q_inventoryschemeid");
		List<QFilter> list = new ArrayList<QFilter>();
		List<Long> ids = getAllSchemeId(schemeEntrys);
		getPageCache().put("schemeid", SerializationUtils.toJsonString(ids));
		list.add(new QFilter("id", "in", ids));
		schemeBd.setQFilters(list);
		if (schemeId == null) {
			model.setValue("q_inventoryschemeid", ids.get(ids.size() - 1));
		} else {
			model.setValue("q_inventoryschemeid", schemeId);
		}
		String refresh = getPageCache().get("refreshscheme");
		if (refresh != null) {
			model.setValue("q_inventoryschemeid", refresh);
		}
	}

	private HashMap<String, ArrayList<TaskEntry>> getSchemeEntrys() {
		LinkedHashMap<String, ArrayList<TaskEntry>> schemeEntrys = new LinkedHashMap();
		DynamicObject[] inventoryTasks = BusinessDataServiceHelper.load("fa_inventory_task",
				"inventsscopeid.inventschemeentry,inventsscopeid.assetunit.name,inventperson.name,status", null,
				"inventsscopeid.inventschemeentry.createtime");
		for (DynamicObject inventoryTask : inventoryTasks) {
			TaskEntry taskEntry = new TaskEntry();
			DynamicObject inventscheme = inventoryTask.getDynamicObject("inventsscopeid.inventschemeentry");
			taskEntry.setSchemeid(String.valueOf(inventscheme.getPkValue()));
			taskEntry.setTaskid(String.valueOf(inventoryTask.getPkValue()));
			taskEntry.setAssetunit(inventoryTask.getString("inventsscopeid.assetunit.name"));
			taskEntry.setChargeperson(inventoryTask.getString("inventperson.name"));
			taskEntry.setRealnumber(0);
			taskEntry.setBookquantity(0);
			taskEntry.setLossnumber(0);
			taskEntry.setProfitnumber(0);
			taskEntry.setHasInvent(0);
			taskEntry.setTotal(0);
			taskEntry.setStatus(inventoryTask.getString("status"));
			String id = String.valueOf(inventscheme.getPkValue());
			if (schemeEntrys.containsKey(id)) {
				ArrayList<TaskEntry> list = (ArrayList) schemeEntrys.get(id);
				list.add(taskEntry);
				schemeEntrys.put(id, list);
			} else {
				ArrayList<TaskEntry> list = new ArrayList();
				list.add(taskEntry);
				schemeEntrys.put(id, list);
			}
		}
		return schemeEntrys;
	}

	private boolean hasInventoryScheme() {
		String appId = AppMetadataCache.getAppInfo("fa").getId();
		HasPermOrgResult permOrgRs = PermissionServiceHelper.getAllPermOrgs(ContextUtil.getUserId(), appId,
				getView().getEntityId(), PermissonType.VIEW.getPermId());
		List<Long> orgIds = permOrgRs.getHasPermOrgs();
		if ((orgIds.isEmpty()) && (!permOrgRs.hasAllOrgPerm())) {
			getView().showTipNotification(ResManager.loadKDString("未找到有权限的核算组织，无法找到可访问的盘点方案",
					"FaInventoryReportPlugin_4", "kd.bos.asset.inventoryPlugin", new Object[0]));
			return false;
		}
		return true;
	}

	public List<Long> getAllSchemeId(HashMap<String, ArrayList<TaskEntry>> inventoryScheme) {
		List<Long> ids = new LinkedList();
		for (String key : inventoryScheme.keySet()) {
			ids.add(Long.valueOf(Long.parseLong(key)));
		}
		return ids;
	}

	private String getSelectFields() {
		return "id,inventoryquantity,bookquantity,inventorystate,difference,inventschemeentry,inventorytask";
	}

	public void viewRecord(HyperLinkClickEvent evt) {
		int selectRow = evt.getRowIndex();
		String taskid = getModel().getEntryRowEntity("entryentity", selectRow).getString("inventorytaskid");

		ListShowParameter parameter = new ListShowParameter();
		parameter.setFormId("bos_list");
		parameter.setCustomParam("inventorytaskid", Long.valueOf(Long.parseLong(taskid)));
		parameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
		parameter.setBillFormId("fa_inventory_record");
		getView().showForm(parameter);
	}

	public void hyperLinkClick(HyperLinkClickEvent evt) {
		Control ce = (Control) evt.getSource();
		String entryname = ce.getKey();
		if ("entryentity".equals(entryname)) {
			viewRecord(evt);
		}
	}

	/**
	 * 
	 * @Title: getTaskInventoryState @Description:
	 *         TODO(根据任务Id获取该任务的盘点状态) @param: @param taskID @param: @return @return:
	 *         SchemeEntry @throws
	 */
	@SuppressWarnings("unchecked")
	public SchemeEntry getTaskInventoryState(String taskID) {
		String algoKey = getClass().getName() + ".query_resume";
		String sql = "SELECT FENTRYID,FK_ZSF_INVENTORYSTATE  FROM T_FA_INVENT_TASKRULE " + " WHERE  FDETAILID =? ";
		Object[] params = { taskID };
		SchemeEntry entry = null;
		try (DataSet ds = DB.queryDataSet(algoKey, DBRoute.of("fa"), sql, params)) {
			RowMeta md = ds.getRowMeta();
			int columnCount = md.getFieldCount();
			while (ds.hasNext()) {
				Row row = ds.next();
				@SuppressWarnings("rawtypes")
				Map<String, String> rowData = new HashMap();
				for (int i = 0; i < columnCount; i++) {
					rowData.put(md.getField(i).toString(), row.get(i).toString());
				}
				entry = new SchemeEntry(rowData);
			}
		}
		return entry;
	}

	/**
	 * 
	 * @Title: updateSchemeEntryByTaskID   
	 * @Description: TODO(根据任务id更新盘点任务的盘点状态)   
	 * @param: @param taskId
	 * @param: @param status
	 * @param: @param taskStatus
	 * @param: @return      
	 * @return: boolean      
	 * @throws
	 */
	public boolean updateSchemeEntryByTaskID(String taskId, String status,String taskStatus) {
		String sql = "UPDATE T_FA_INVENT_TASKRULE set fentrystatus=?, FK_ZSF_INVENTORYSTATE =? " + " WHERE FDETAILID =? ";
		Object[] params = {taskStatus, status, taskId };
		boolean flag = DB.execute(DBRoute.basedata, sql, params);
		return flag;
	}

}
