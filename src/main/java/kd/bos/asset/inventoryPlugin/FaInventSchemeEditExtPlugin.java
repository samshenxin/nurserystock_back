package kd.bos.asset.inventoryPlugin;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.BeforeDeleteRowEventArgs;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.operate.AbstractOperate;
import kd.bos.license.api.LicenseCheckResult;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.license.LicenseServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.StringUtils;
import kd.fi.fa.constants.InventoryTaskRule;
import kd.fi.fa.constants.InventoryTaskRuleRow;
import kd.fi.fa.inventory.FaInventoryTaskRuleSetPlugin;
import kd.fi.fa.utils.FaUtils;

public class FaInventSchemeEditExtPlugin extends AbstractBillPlugIn {
	    private static final String KEY_ENTRYENTITY = "entryentity";
	    private static final String KEY_ASSETUNIT = "assetunit";
	    private static final String KEY_TASKRULE = "taskrule";
	    private static final String KEY_TASKSTATUS = "taskstatus";
	    private static final String KEY_SPLITDETAILENTITY = "splitdetailentity";
	    private static final String KEY_SPLITFIELDVALUE = "splitfieldvalue";
	    private static final String KEY_INVENTPERSON = "inventperson";
	    private static final String KEY_ENTRYSTATUS = "entrystatus";
	    private static final String KEY_TASKID = "taskid";
	    private static final String KEY_SPLITFIELDENTITY = "splitfieldentity";
	    private static final String KEY_SPLITFIELD = "splitfield";

    public void registerListener(final EventObject e) {
        this.addClickListeners(new String[] { "taskrule" });
        final BasedataEdit assetUnitF7 = (BasedataEdit)this.getControl("assetunit");
        assetUnitF7.addBeforeF7SelectListener(v -> this.setF7Filter(v, this::getAssetUnitF7Filter));
        this.addItemClickListeners(new String[] { "tbmain" });
    }

    public void preOpenForm(PreOpenFormEventArgs e) {
        LicenseCheckResult result = LicenseServiceHelper.checkUserInGroup(Long.valueOf(Long.parseLong(RequestContext.getOrCreate().getUserId())), Long.valueOf(4));
        if (result.getHasLicense().booleanValue()) {
            super.preOpenForm(e);
            return;
        }
        String msg = result.getMsg();
        e.setCancel(true);
        e.setCancelMessage(msg);
    }

    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        getModel().setDataChanged(false);
    }

    public void afterLoadData(EventObject e) {
        setEnableByStatus(getModel().getDataEntity());
    }

    public void click(EventObject evt) {
        if (((Control) evt.getSource()).getKey().equalsIgnoreCase(KEY_TASKRULE)) {
            showTaskRuleSet(getModel().getEntryCurrentRowIndex("entryentity"));
        }
    }

    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        if (((AbstractOperate) args.getSource()).getOperateKey().equalsIgnoreCase("save")) {
            setEnableByStatus(getModel().getDataEntity(true));
        }
    }

    public void itemClick(ItemClickEvent evt) {
        String itemKey = evt.getItemKey();
        IDataModel model = getModel();
        if ("assign".equalsIgnoreCase(itemKey)) {
            model.clearNoDataRow();
            Object id = model.getValue(FaUtils.ID);
            if (SaveServiceHelper.saveOperate("fa_inventscheme_new", new DynamicObject[]{model.getDataEntity(true)}).isSuccess()) {
                DynamicObjectCollection entries = model.getDataEntity(true).getDynamicObjectCollection("entryentity");
                Set<Object> pkSet = new HashSet();
                List<String> statusList = Arrays.asList(new String[]{"A", "B", "Z"});
                Iterator it = entries.iterator();
                while (it.hasNext()) {
                    Iterator it2 = ((DynamicObject) it.next()).getDynamicObjectCollection(KEY_SPLITDETAILENTITY).iterator();
                    while (it2.hasNext()) {
                        DynamicObject taskDy = (DynamicObject) it2.next();
                        if (statusList.contains(taskDy.getString(KEY_ENTRYSTATUS))) {
                            pkSet.add(taskDy.getPkValue());
                        }
                    }
                }
                if (pkSet.size() == 0) {
                    getView().showTipNotification(ResManager.loadKDString("不存在未下达状态的盘点任务，可能是盘点任务均已下达，或者新增的盘点范围未保存", "FaInventSchemeEditPlugin_1", "kd.bos.asset.inventoryPlugin", new Object[0]));
                    return;
                }
                FaInventoryTaskListExt.inventoryAssign(pkSet.toArray(new Object[pkSet.size()]));
                DynamicObject inventScheme = BusinessDataServiceHelper.loadSingle(id, "fa_inventscheme_new");
                setEnableByStatus(inventScheme);
                refreshTaskStatus(inventScheme);
                getView().showSuccessNotification(ResManager.loadKDString("下达成功，可在盘点方案列表界面联查盘点任务", "FaInventSchemeEditPlugin_2", "kd.bos.asset.inventoryPlugin", new Object[0]));
                getView().updateView();
                return;
            }
            getView().showTipNotification(ResManager.loadKDString("盘点方案尝试保存失败，请在点击保存按钮尝试！", "FaInventSchemeEditPlugin_0", "kd.bos.asset.inventoryPlugin", new Object[0]));
        }
    }

    void refreshTaskStatus(DynamicObject inventScheme) {
        Map<Long, String> curStatusMap = new HashMap();
        Iterator it = inventScheme.getDynamicObjectCollection("entryentity").iterator();
        while (it.hasNext()) {
            Iterator it2 = ((DynamicObject) it.next()).getDynamicObjectCollection(KEY_SPLITDETAILENTITY).iterator();
            while (it2.hasNext()) {
                DynamicObject taskDy = (DynamicObject) it2.next();
                curStatusMap.put(Long.valueOf(taskDy.getLong(FaUtils.ID)), taskDy.getString(KEY_ENTRYSTATUS));
            }
        }
        it = getModel().getDataEntity(true).getDynamicObjectCollection("entryentity").iterator();
        while (it.hasNext()) {
        	Iterator it2 = ((DynamicObject) it.next()).getDynamicObjectCollection(KEY_SPLITDETAILENTITY).iterator();
            while (it2.hasNext()) {
            	DynamicObject taskDy = (DynamicObject) it2.next();
                Long id = Long.valueOf(taskDy.getLong(FaUtils.ID));
                if (curStatusMap.containsKey(id)) {
                    taskDy.set(KEY_ENTRYSTATUS, curStatusMap.get(id));
                }
            }
        }
    }

    Boolean getTaskStatus(DynamicObject inventScheme) {
        Boolean result = Boolean.valueOf(true);
        DynamicObjectCollection entries = inventScheme.getDynamicObjectCollection("entryentity");
        if (entries != null && entries.size() > 0) {
            Iterator it = entries.iterator();
            while (it.hasNext()) {
                Iterator it2 = ((DynamicObject) it.next()).getDynamicObjectCollection(KEY_SPLITDETAILENTITY).iterator();
                while (it2.hasNext()) {
                    String status = ((DynamicObject) it2.next()).getString(KEY_ENTRYSTATUS);
                    if (!"A".equalsIgnoreCase(status) && !"Z".equalsIgnoreCase(status)) {
                        return Boolean.valueOf(false);
                    }
                }
            }
        }
        return result;
    }

    void setEnableByStatus(DynamicObject inventScheme) {
        DynamicObjectCollection entries = inventScheme.getDynamicObjectCollection("entryentity");
        if (entries != null && entries.size() > 0) {
            int rowIndex = 0;
            Iterator it = entries.iterator();
            while (it.hasNext()) {
                DynamicObject rowDy = (DynamicObject) it.next();
                Boolean rowEnable = Boolean.valueOf(true);
                Iterator it2 = rowDy.getDynamicObjectCollection(KEY_SPLITDETAILENTITY).iterator();
                while (it2.hasNext()) {
                    String status = ((DynamicObject) it2.next()).getString(KEY_ENTRYSTATUS);
                    if (!"A".equalsIgnoreCase(status) && !"Z".equalsIgnoreCase(status)) {
                        rowEnable = Boolean.valueOf(false);
                        break;
                    }
                }
                if (Boolean.valueOf(StringUtils.isEmpty(rowDy.getString(KEY_TASKRULE))).booleanValue()) {
                    if (!rowEnable.booleanValue()) {
                        getView().setEnable(Boolean.valueOf(false), rowIndex, new String[]{"assetunit"});
                        getView().setEnable(Boolean.valueOf(false), rowIndex, new String[]{"finaccountdate"});
                        getView().setEnable(Boolean.valueOf(false), rowIndex, new String[]{KEY_TASKRULE});
                    }
                } else if (!rowEnable.booleanValue()) {
                    getView().setEnable(Boolean.valueOf(false), rowIndex, new String[]{"assetunit"});
                    getView().setEnable(Boolean.valueOf(false), rowIndex, new String[]{"finaccountdate"});
                }
                rowIndex++;
            }
        }
    }

    private QFilter getAssetUnitF7Filter() {
        IDataModel model = getModel();
        int rowCount = model.getEntryRowCount("entryentity");
        Set<Long> assetunits = new HashSet();
        for (int i = 0; i < rowCount; i++) {
            DynamicObject assetunit = (DynamicObject) model.getValue("assetunit", i);
            if (assetunit != null) {
                assetunits.add(Long.valueOf(assetunit.getLong(FaUtils.ID)));
            }
        }
        QFilter treeTypeFilter = new QFilter("view.treetype", "=", "09");
        QFilter leafFilter = new QFilter("isleaf", "=", Boolean.valueOf(true));
        DynamicObjectCollection orgs = QueryServiceHelper.query("bos_org_structure", "org.id", new QFilter[]{treeTypeFilter, leafFilter});
        Set<Long> leafOrgs = new HashSet();
        Iterator it = orgs.iterator();
        while (it.hasNext()) {
            DynamicObject s = (DynamicObject) it.next();
            if (!assetunits.contains(Long.valueOf(s.getLong("org.id")))) {
                leafOrgs.add(Long.valueOf(s.getLong("org.id")));
            }
        }
        return new QFilter(FaUtils.ID, "in", leafOrgs);
    }

    private void setF7Filter(BeforeF7SelectEvent e, Supplier<QFilter> filterFn) {
        ((ListShowParameter) e.getFormShowParameter()).getListFilterParameter().getQFilters().add(filterFn.get());
    }

    public void beforeDeleteRow(BeforeDeleteRowEventArgs e) {
        int[] rowIndexs = e.getRowIndexs();
        DynamicObjectCollection entries = getModel().getEntryEntity("entryentity");
        int length = rowIndexs.length;
        int i = 0;
        while (i < length) {
            DynamicObjectCollection taskDys = ((DynamicObject) entries.get(rowIndexs[i])).getDynamicObjectCollection(KEY_SPLITDETAILENTITY);
            Boolean rowEnable = Boolean.valueOf(true);
            Iterator it = taskDys.iterator();
            while (it.hasNext()) {
                String status = ((DynamicObject) it.next()).getString(KEY_ENTRYSTATUS);
                if (!"A".equalsIgnoreCase(status) && !"Z".equalsIgnoreCase(status)) {
                    rowEnable = Boolean.valueOf(false);
                    break;
                }
            }
            if (rowEnable.booleanValue()) {
                i++;
            } else {
                getView().showTipNotification(ResManager.loadKDString("存在已下达的盘点任务，不允许删除", "FaInventSchemeEditPlugin_3", "kd.bos.asset.inventoryPlugin", new Object[0]));
                e.setCancel(true);
                return;
            }
        }
    }

    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (KEY_TASKRULE.equalsIgnoreCase(closedCallBackEvent.getActionId())) {
            receiveTaskRuleSet(closedCallBackEvent);
        }
    }

    private void receiveTaskRuleSet(ClosedCallBackEvent closedCallBackEvent) {
        IDataModel model = getModel();
        if (closedCallBackEvent.getReturnData() != null) {
            String str = (String) closedCallBackEvent.getReturnData();
            if (!StringUtils.isBlank(str)) {
                InventoryTaskRuleRow rowEntry;
                InventoryTaskRule inventoryTaskRule = (InventoryTaskRule) SerializationUtils.fromJsonString(str, InventoryTaskRule.class);
                int rowIndex = getModel().getEntryCurrentRowIndex("entryentity");
                getModel().setValue(KEY_TASKRULE, inventoryTaskRule.getRulename(), rowIndex);
                model.setEntryCurrentRowIndex("entryentity", rowIndex);
                Map<Long, List<InventoryTaskRuleRow>> rowMap = new LinkedHashMap();
                for (InventoryTaskRuleRow rowEntry2 : inventoryTaskRule.getEntryRows()) {
                    long taskId = rowEntry2.getTaskId() == null ? 0 : rowEntry2.getTaskId().longValue();
                    if (rowMap.containsKey(Long.valueOf(taskId))) {
                        ((List) rowMap.get(Long.valueOf(taskId))).add(rowEntry2);
                    } else {
                        List<InventoryTaskRuleRow> rows = new ArrayList();
                        rows.add(rowEntry2);
                        rowMap.put(Long.valueOf(taskId), rows);
                    }
                }
                DynamicObject curDy = (DynamicObject) model.getEntryEntity("entryentity").get(rowIndex);
                DynamicObjectCollection taskRuleEntries = curDy.getDynamicObjectCollection(KEY_SPLITDETAILENTITY);
                Set<Long> existsIds = new HashSet();
                int rowcount = taskRuleEntries.size();
                int i = 0;
                while (i < rowcount) {
                    long rowId = ((DynamicObject) taskRuleEntries.get(i)).getLong(KEY_TASKID);
                    if (rowMap.containsKey(Long.valueOf(rowId))) {
                        existsIds.add(Long.valueOf(rowId));
                    } else {
                        model.deleteEntryRow(KEY_SPLITDETAILENTITY, i);
                        i--;
                        rowcount--;
                    }
                    i++;
                }
                taskRuleEntries = curDy.getDynamicObjectCollection(KEY_SPLITDETAILENTITY);
                rowcount = taskRuleEntries.size();
                for (i = 0; i < rowcount; i++) {
                	InventoryTaskRuleRow rowEntry2 = (InventoryTaskRuleRow) ((List) rowMap.get(Long.valueOf(((DynamicObject) taskRuleEntries.get(i)).getLong(KEY_TASKID)))).get(0);
                    model.setValue(KEY_SPLITFIELDVALUE, SerializationUtils.toJsonString(rowEntry2.getSplitFieldMap()), i, rowIndex);
                    model.setValue(KEY_INVENTPERSON, rowEntry2.getInventperson(), i, rowIndex);
                    model.setValue(KEY_ENTRYSTATUS, rowEntry2.getTaskStatus(), i, rowIndex);
                }
                for (InventoryTaskRuleRow rowEntry22 : inventoryTaskRule.getEntryRows()) {
                    if (!existsIds.contains(rowEntry22.getTaskId())) {
                        int rowIndex1 = model.createNewEntryRow(KEY_SPLITDETAILENTITY);
                        model.setValue(KEY_SPLITFIELDVALUE, SerializationUtils.toJsonString(rowEntry22.getSplitFieldMap()), rowIndex1, rowIndex);
                        model.setValue(KEY_INVENTPERSON, rowEntry22.getInventperson(), rowIndex1, rowIndex);
                        model.setValue(KEY_ENTRYSTATUS, rowEntry22.getTaskStatus(), rowIndex1, rowIndex);
                    }
                }
                model.deleteEntryData(KEY_SPLITFIELDENTITY);
                if (!StringUtils.isBlank(inventoryTaskRule.getSplitfields())) {
                    for (String field : inventoryTaskRule.getSplitfields().trim().split(",")) {
                        if (!StringUtils.isBlank(field)) {
                            model.setValue(KEY_SPLITFIELD, field, model.createNewEntryRow(KEY_SPLITFIELDENTITY), rowIndex);
                        }
                    }
                }
            }
        }
    }

    void showTaskRuleSet(int rowIndex) {
        IDataModel model = getModel();
        FormShowParameter showParameter = new FormShowParameter();
        showParameter.setFormId(FaInventoryTaskRuleSetPlugin.FORMID);
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setCustomParam("taskstatus", model.getValue("taskstatus", rowIndex).toString());
        showParameter.setCustomParam(FaInventoryTaskRuleSetPlugin.PARAMKEY_PARAMJSON, SerializationUtils.toJsonString(getTaskRuleObj(rowIndex)));
        showParameter.setCloseCallBack(new CloseCallBack(this, KEY_TASKRULE));
        getView().showForm(showParameter);
    }

    InventoryTaskRule getTaskRuleObj(int rowIndex) {
        IDataModel model = getModel();
        model.setEntryCurrentRowIndex("entryentity", rowIndex);
        InventoryTaskRule taskRule = new InventoryTaskRule();
        Object ruleName = model.getValue(KEY_TASKRULE, rowIndex);
        taskRule.setRulename(ruleName == null ? "" : ruleName.toString());
        if (StringUtils.isEmpty(taskRule.getRulename())) {
            taskRule.setSplitfields("");
            taskRule.setEntryRows(new ArrayList());
        } else {
            taskRule.setSplitfields(getSplitFieldStr(rowIndex));
            taskRule.setEntryRows(getTaskRuleEntries(rowIndex));
        }
        return taskRule;
    }

    List<InventoryTaskRuleRow> getTaskRuleEntries(int rowIndex) {
        IDataModel model = getModel();
        List<InventoryTaskRuleRow> rowList = new ArrayList();
        DynamicObjectCollection entries = model.getEntryEntity("entryentity");
        if (entries.size() > 0) {
            DynamicObject curDy = (DynamicObject) entries.get(rowIndex);
            model.setEntryCurrentRowIndex("entryentity", rowIndex);
            long curIndex = 1;
            Iterator it = curDy.getDynamicObjectCollection(KEY_SPLITDETAILENTITY).iterator();
            while (it.hasNext()) {
                DynamicObject dy = (DynamicObject) it.next();
                InventoryTaskRuleRow row = new InventoryTaskRuleRow();
                row.setSplitFieldMap(new LinkedHashMap());
                String jsonStr = dy.getString(KEY_SPLITFIELDVALUE);
                if (!StringUtils.isBlank(jsonStr)) {
                    row.setSplitFieldMap((LinkedHashMap) SerializationUtils.fromJsonString(jsonStr, LinkedHashMap.class));
                }
                if (dy.getDynamicObject(KEY_INVENTPERSON) != null) {
                    row.setInventperson(Long.valueOf(dy.getDynamicObject(KEY_INVENTPERSON).getLong(FaUtils.ID)));
                }
                row.setTaskStatus(dy.getString(KEY_ENTRYSTATUS));
                row.setTaskId(Long.valueOf(curIndex));
                dy.set(KEY_TASKID, Long.valueOf(curIndex));
                rowList.add(row);
                curIndex++;
            }
        }
        return rowList;
    }

    String getSplitFieldStr(int rowIndex) {
        String result = "";
        IDataModel model = getModel();
        DynamicObjectCollection entries = model.getEntryEntity("entryentity");
        if (entries.size() <= 0) {
            return result;
        }
        DynamicObject curDy = (DynamicObject) entries.get(rowIndex);
        model.setEntryCurrentRowIndex("entryentity", rowIndex);
        DynamicObjectCollection splitFieldEntries = curDy.getDynamicObjectCollection(KEY_SPLITFIELDENTITY);
        List<String> fieldList = new ArrayList();
        Iterator it = splitFieldEntries.iterator();
        while (it.hasNext()) {
            String str = ((DynamicObject) it.next()).getString(KEY_SPLITFIELD);
            if (!StringUtils.isBlank(str)) {
                fieldList.add(str);
            }
        }
        return String.join(",", fieldList);
    }
}
