package kd.bos.asset.inventoryPlugin;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;

import kd.bos.asset.utils.FaAssetTypeUtils;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.CloneUtils;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.metadata.IDataEntityType;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.db.tx.TX;
import kd.bos.db.tx.TXHandle;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.cache.AppCache;
import kd.bos.entity.cache.IAppCache;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.validate.BillStatus;
import kd.bos.exception.KDBizException;
import kd.bos.filter.CommonFilterColumn;
import kd.bos.filter.FilterColumn;
import kd.bos.form.CloseCallBack;
import kd.bos.form.ConfirmCallBackListener;
import kd.bos.form.ConfirmTypes;
import kd.bos.form.FormShowParameter;
import kd.bos.form.MessageBoxOptions;
import kd.bos.form.MessageBoxResult;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.FilterContainerInitArgs;
import kd.bos.form.events.HyperLinkClickArgs;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.events.PreOpenFormEventArgs;
import kd.bos.form.events.SetFilterEvent;
import kd.bos.form.field.ComboItem;
import kd.bos.form.plugin.IFormPlugin;
import kd.bos.license.api.LicenseCheckResult;
import kd.bos.list.BillList;
import kd.bos.list.IListView;
import kd.bos.list.ListShowParameter;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.datamanager.DataManagerUtils;
import kd.bos.orm.query.QFilter;
import kd.bos.orm.util.CollectionUtils;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.license.LicenseServiceHelper;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.url.UrlService;
import kd.bos.util.JSONUtils;
import kd.bos.workflow.engine.msg.info.MessageInfo;
import kd.fi.fa.business.BizStatusEnum;
import kd.fi.fa.business.utils.FaOperateLogUtil;
import kd.fi.fa.common.util.ContextUtil;
import kd.fi.fa.common.util.Fa;
import kd.fi.fa.utils.FaUtils;

public class FaInventoryTaskList extends AbstractListPlugin
{
    private static final Log log;
    public static final String ALGOKEY = "kd.fi.fa.formplugin.FaInventoryTaskList";
    private static final int DATA_SIZE;
    
    public void preOpenForm(final PreOpenFormEventArgs e) {
        final String uid = RequestContext.getOrCreate().getUserId();
        final LicenseCheckResult result = LicenseServiceHelper.checkUserInGroup(Long.valueOf(Long.parseLong(uid)), Long.valueOf(4L));
        if (!result.getHasLicense()) {
            final String msg = result.getMsg();
            e.setCancel(true);
            e.setCancelMessage(msg);
        }
        else {
            super.preOpenForm(e);
        }
    }
    
    public void filterContainerInit(final FilterContainerInitArgs args) {
        super.filterContainerInit(args);
        this.setDefaultQuerySelect(args);
    }
    
    public void setFilter(final SetFilterEvent e) {
        final Map<String, Object> customParams = (Map<String, Object>)this.getView().getFormShowParameter().getCustomParams();
        final Object schemeId = customParams.get("schemeId");
        e.getQFilters().add(new QFilter("status", "!=", (Object)"Z"));
        if (schemeId == null) {
            e.getQFilters().add(new QFilter("status", "!=", (Object)"A"));
            final Long userId = ContextUtil.getUserId();
            e.getQFilters().add(new QFilter("inventperson", "=", (Object)userId));
            this.getView().setVisible(Boolean.valueOf(false), new String[] { "inventoryassign" });
            this.getView().setVisible(Boolean.valueOf(false), new String[] { "inventorywithdrawal" });
            final LicenseCheckResult result = LicenseServiceHelper.checkByAppAndBizObj("/OSOW2CPH91+", "fap_apphome", userId);
            if (result != null && !result.getHasLicense()) {
                this.getView().setVisible(Boolean.valueOf(false), new String[] { "inventorynotice" });
            }
        }
        else {
            e.getQFilters().add(new QFilter("inventsscopeid.inventschemeentry", "=", schemeId));
            this.getView().setVisible(Boolean.valueOf(false), new String[] { "inventorycreaterecord" });
            this.getView().setVisible(Boolean.valueOf(false), new String[] { "inventorynotice" });
            this.getView().setVisible(Boolean.valueOf(false), new String[] { "inventorydelete" });
        }
    }
    
    public void click(final EventObject evt) {
        final Control c = (Control)evt.getSource();
        final String key = c.getKey();
        if ("name".equalsIgnoreCase(key)) {
            final Object rowPk = ((IListView)this.getView()).getCurrentSelectedRowInfo().getPrimaryKeyValue();
            this.viewInventoryRecord(rowPk);
        }
        super.click(evt);
    }
    
    public void billListHyperLinkClick(final HyperLinkClickArgs args) {
        if (!FaUtils.isF7(this.getView())) {
            final BillList billList = (BillList)this.getControl("BillListAp");
            final HyperLinkClickEvent hyperClick = args.getHyperLinkClickEvent();
            if ("inventsscopeid_assetunit_name".equals(hyperClick.getFieldName())) {
                final Object rowPk = billList.getCurrentSelectedRowInfo().getPrimaryKeyValue();
                args.setCancel(true);
                this.viewInventoryRecord(rowPk);
            }
        }
    }
    
    private void viewInventoryRecord(final Object rowPk) {
        final ListShowParameter parameter = new ListShowParameter();
        parameter.setFormId("bos_list");
        parameter.setCaption(ResManager.loadKDString("\u76d8\u70b9\u8bb0\u5f55", "FaInventoryTaskList_0", "fi-fa-formplugin", new Object[0]));
        parameter.setCustomParam("inventorytaskid", rowPk);
        parameter.setBillFormId("fa_inventory_record");
        parameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        this.getView().showForm((FormShowParameter)parameter);
    }
    
    public void itemClick(final ItemClickEvent evt) {
        super.itemClick(evt);
        final String key = evt.getItemKey();
        final IListView view = (IListView)this.getView();
        String opMsg = ResManager.loadKDString("\u6210\u529f", "FaInventoryTaskList_24", "fi-fa-formplugin", new Object[0]);
        final ListSelectedRowCollection selectedRows = view.getSelectedRows();
        if (selectedRows.size() < 1 && !key.equals("tblrefresh") && !key.equals("tblclose")) {
            this.getView().showTipNotification(ResManager.loadKDString("\u8bf7\u9009\u62e9\u4e00\u884c\u64cd\u4f5c", "FaInventoryTaskList_1", "fi-fa-formplugin", new Object[0]));
            return;
        }
        if ("inventorynotice".equals(key)) {
            final List<Long> inventoryTaskPks = new ArrayList<Long>();
            for (final ListSelectedRow selectedRow : selectedRows) {
                final DynamicObject everyRow = BusinessDataServiceHelper.loadSingle((Object)String.valueOf(selectedRow), "fa_inventory_task");
                final String status = (String)everyRow.get("status");
                if (!BillStatus.C.toString().equals(status)) {
                    throw new KDBizException(ResManager.loadKDString("\u8bf7\u5148\u751f\u6210\u76d8\u70b9\u8bb0\u5f55", "FaInventoryTaskList_2", "fi-fa-formplugin", new Object[0]));
                }
                inventoryTaskPks.add((Long)selectedRow.getPrimaryKeyValue());
            }
            try {
                this.getPageCache().put("inventoryTaskPks", JSONUtils.toString((Object)inventoryTaskPks));
            }
            catch (IOException e2) {
                throw new KDBizException(ResManager.loadKDString("\u76d8\u70b9\u901a\u77e5\u6709\u8bef", "FaInventoryTaskList_3", "fi-fa-formplugin", new Object[0]));
            }
            final FormShowParameter para = new FormShowParameter();
            para.setFormId("fa_countingreport_message");
            para.getOpenStyle().setShowType(ShowType.Modal);
            para.setCloseCallBack(new CloseCallBack((IFormPlugin)this, "getmessage"));
            this.getView().showForm(para);
        }
        if ("inventoryassign".equals(key)) {
            final StringBuilder notRightStatus = new StringBuilder();
            for (final ListSelectedRow selectedRow : selectedRows) {
                final DynamicObject inventoryTask = BusinessDataServiceHelper.loadSingle((Object)String.valueOf(selectedRow), "fa_inventory_task");
                final String assetUnitName = inventoryTask.getDynamicObject("inventsscopeid").getDynamicObject("assetunit").getString("name");
                final long assetUnitId = inventoryTask.getDynamicObject("inventsscopeid").getDynamicObject("assetunit").getLong("id");
                final String status2 = inventoryTask.getString("status");
                if (!BillStatus.A.toString().equals(status2)) {
                    notRightStatus.append(assetUnitName).append(' ');
                    FaOperateLogUtil.addLog("fa_inventory_task", Long.valueOf(assetUnitId), ResManager.loadKDString("\u4e0b\u8fbe\u4efb\u52a1", "FaInventoryTaskList_25", "fi-fa-formplugin", new Object[0]), assetUnitName + ResManager.loadKDString("\u4e0b\u8fbe\u5931\u8d25,\u4efb\u52a1\u72b6\u6001\u4e0d\u662f\u672a\u4e0b\u8fbe", "FaInventoryTaskList_4", "fi-fa-formplugin", new Object[0]));
                }
                else {
                    inventoryTask.set("status", (Object)BillStatus.B.toString());
                    inventoryTask.set("zsf_inventorystate", "B");//默认下达任务的盘点状态为未盘点
                    SaveServiceHelper.save(inventoryTask.getDataEntityType(), (Object[])new DynamicObject[] { inventoryTask });
                    FaOperateLogUtil.addLog("fa_inventory_task", Long.valueOf(assetUnitId), ResManager.loadKDString("\u4e0b\u8fbe\u4efb\u52a1", "FaInventoryTaskList_25", "fi-fa-formplugin", new Object[0]), assetUnitName + ResManager.loadKDString("\u4e0b\u8fbe\u4efb\u52a1\u6210\u529f", "FaInventoryTaskList_27", "fi-fa-formplugin", new Object[0]));
                }
            }
            if (notRightStatus.length() > 0) {
                opMsg = (Object)notRightStatus + ResManager.loadKDString("\u4e0b\u8fbe\u5931\u8d25,\u4efb\u52a1\u72b6\u6001\u4e0d\u662f\u672a\u4e0b\u8fbe", "FaInventoryTaskList_4", "fi-fa-formplugin", new Object[0]);
                this.getView().showTipNotification(opMsg);
            }
            else {
                opMsg = ResManager.loadKDString("\u6240\u6709\u4efb\u52a1\u4e0b\u8fbe\u4efb\u52a1\u6210\u529f", "FaInventoryTaskList_5", "fi-fa-formplugin", new Object[0]);
                this.getView().showSuccessNotification(opMsg);
            }
            this.getView().invokeOperation("refresh");
        }
        if ("inventorywithdrawal".equals(key)) {
            final StringBuilder notRightStatus = new StringBuilder();
            for (final ListSelectedRow selectedRow : selectedRows) {
                final DynamicObject inventoryTask = BusinessDataServiceHelper.loadSingle((Object)String.valueOf(selectedRow), "fa_inventory_task");
                final String assetUnitName = inventoryTask.getDynamicObject("inventsscopeid").getDynamicObject("assetunit").getString("name");
                final long assetUnitId = inventoryTask.getDynamicObject("inventsscopeid").getDynamicObject("assetunit").getLong("id");
                final String status2 = inventoryTask.getString("status");
                if (!BillStatus.B.toString().equals(status2)) {
                    notRightStatus.append(assetUnitName).append(' ');
                    FaOperateLogUtil.addLog("fa_inventory_task", Long.valueOf(assetUnitId), ResManager.loadKDString("\u64a4\u9500\u4efb\u52a1", "FaInventoryTaskList_26", "fi-fa-formplugin", new Object[0]), assetUnitName + ResManager.loadKDString("\u64a4\u9500\u5931\u8d25,\u4efb\u52a1\u72b6\u6001\u4e0d\u662f\u5df2\u4e0b\u8fbe", "FaInventoryTaskList_6", "fi-fa-formplugin", new Object[0]));
                }
                else {
                    inventoryTask.set("status", (Object)"A");
                    SaveServiceHelper.save(inventoryTask.getDataEntityType(), (Object[])new DynamicObject[] { inventoryTask });
                    FaOperateLogUtil.addLog("fa_inventory_task", Long.valueOf(assetUnitId), ResManager.loadKDString("\u64a4\u9500\u4efb\u52a1", "FaInventoryTaskList_26", "fi-fa-formplugin", new Object[0]), assetUnitName + ResManager.loadKDString("\u64a4\u9500\u4efb\u52a1\u6210\u529f", "FaInventoryTaskList_28", "fi-fa-formplugin", new Object[0]));
                }
            }
            if (notRightStatus.length() > 0) {
                opMsg = (Object)notRightStatus + ResManager.loadKDString("\u64a4\u9500\u5931\u8d25,\u4efb\u52a1\u72b6\u6001\u4e0d\u662f\u5df2\u4e0b\u8fbe", "FaInventoryTaskList_6", "fi-fa-formplugin", new Object[0]);
                this.getView().showTipNotification(opMsg);
            }
            else {
                opMsg = ResManager.loadKDString("\u6240\u6709\u4efb\u52a1\u4e0b\u8fbe\u4efb\u52a1\u6210\u529f\u64a4\u9500\u4efb\u52a1\u6210\u529f", "FaInventoryTaskList_7", "fi-fa-formplugin", new Object[0]);
                this.getView().showSuccessNotification(opMsg);
            }
            this.getView().invokeOperation("refresh");
        }
        if ("inventorycreaterecord".equals(key)) {
            final StringBuilder notRightStatus = new StringBuilder();
            for (final ListSelectedRow selectedRow : selectedRows) {
                final DynamicObject inventoryTask = BusinessDataServiceHelper.loadSingle(selectedRow.getPrimaryKeyValue(), "fa_inventory_task");
                final DynamicObject inventorySope = inventoryTask.getDynamicObject("inventsscopeid");
                if (inventorySope == null) {
                    return;
                }
                final String status3 = inventoryTask.getString("status");
                final Object assetUnitId2 = inventorySope.get("assetunit_id");
                final String unitName = inventoryTask.getDynamicObject("inventsscopeid").getDynamicObject("assetunit").getString("name");
                if (!BillStatus.B.toString().equals(status3)) {
                    notRightStatus.append(unitName).append(' ');
                    FaOperateLogUtil.addLog("fa_inventory_task", (Long)assetUnitId2, ResManager.loadKDString("\u751f\u6210\u76d8\u70b9\u8bb0\u5f55", "FaInventoryTaskList_20", "fi-fa-formplugin", new Object[0]), unitName + ResManager.loadKDString("\u751f\u6210\u5931\u8d25,\u4efb\u52a1\u72b6\u6001\u4e0d\u662f\u5df2\u4e0b\u8fbe", "FaInventoryTaskList_9", "fi-fa-formplugin", new Object[0]));
                }
                else {
                    final MainEntityType inventoryRecordType = EntityMetadataCache.getDataEntityType("fa_inventory_record");
                    final Date finaccountdate = inventorySope.getDate("finaccountdate");
                    final QFilter timeLimitCond = new QFilter("realaccountdate", "<=", (Object)finaccountdate);
                    final QFilter billstatusCond = new QFilter("billstatus", "=", (Object)BillStatus.C.toString());
                    final QFilter assetunitCond = new QFilter("assetunit_id", "=", assetUnitId2);
                    final QFilter bizstatusDeleteCond = new QFilter("bizstatus", "!=", (Object)BizStatusEnum.DELETE);
                    final QFilter notIsBackAndOther = new QFilter("isbak", "=", (Object)false);
                    final String splitfieldvalue = inventoryTask.getString("splitfieldvalue");
                    if (!StringUtils.isBlank(splitfieldvalue)) {
                        final Map<String, Object> maps = (Map<String, Object>)JSON.parse(splitfieldvalue);
                        for (final Map.Entry<String, Object> entry : maps.entrySet()) {
                            final String keySet = entry.getKey();
                            final Object valueSet = entry.getValue();
                            if (valueSet == null) {
                                continue;
                            }
                            List<Object> idList = null;
                            final String assetcatIds = (String)valueSet;
                            if (!StringUtils.isBlank(assetcatIds)) {
                                final Set<Object> idSet = Arrays.asList(assetcatIds.split(",")).stream().map(s -> Long.parseLong(StringUtils.isBlank(s) ? "0" : s.trim())).collect((Collector<? super Object, ?, Set<Object>>)Collectors.toSet());
                                idList = new ArrayList<Object>(idSet);
                                if (idList.size() == 1 && idList.get(0).equals(0L)) {
                                    idList = null;
                                }
                            }
                            QFilter assetIds = null;
                            if (idList != null) {
                                if ("assetcat".equals(keySet)) {
                                    assetIds = new QFilter("assetcat_id", "in", (Object)FaAssetTypeUtils.getAllSubAssetTypes(idList));
                                }
                                else {
                                    assetIds = new QFilter(keySet + "_id", "in", (Object)idList);
                                }
                            }
                            if (assetIds == null) {
                                continue;
                            }
                            notIsBackAndOther.and(assetIds);
                        }
                    }
                    final String sicOnlyId = Fa.join(",", new String[] { "id" });
                    final String sic = Fa.join(",", new String[] { "id,number,barcode,assetname,model,assetamount,headuseperson" });
                    final DynamicObjectCollection realCards = QueryServiceHelper.query("fa_card_real", sicOnlyId, new QFilter[] { assetunitCond, timeLimitCond, billstatusCond, bizstatusDeleteCond, notIsBackAndOther });
                    final Set<Long> cardIds = new HashSet<Long>();
                    for (final DynamicObject realCard : realCards) {
                        cardIds.add(realCard.getLong("id"));
                    }
                    int i = 0;
                    Set<Long> cardId = new HashSet<Long>();
                    try {
                        final Iterator<Long> ite = cardIds.iterator();
                        while (ite.hasNext()) {
                            if (i == FaInventoryTaskList.DATA_SIZE) {
                                this.createInventoryRedords(inventoryRecordType, inventoryTask, sic, cardId);
                                cardId = new HashSet<Long>();
                                i = 0;
                            }
                            else {
                                cardId.add(ite.next());
                                ++i;
                            }
                        }
                        if (!cardId.isEmpty()) {
                            this.createInventoryRedords(inventoryRecordType, inventoryTask, sic, cardId);
                        }
                        inventoryTask.set("status", (Object)"C");
                        SaveServiceHelper.save(inventoryTask.getDataEntityType(), (Object[])new DynamicObject[] { inventoryTask });
                        FaOperateLogUtil.addLog("fa_inventory_task", (Long)assetUnitId2, ResManager.loadKDString("\u751f\u6210\u76d8\u70b9\u8bb0\u5f55", "FaInventoryTaskList_20", "fi-fa-formplugin", new Object[0]), unitName + ResManager.loadKDString("\u751f\u6210\u76d8\u70b9\u8bb0\u5f55\u6210\u529f", "FaInventoryTaskList_22", "fi-fa-formplugin", new Object[0]));
                    }
                    catch (Exception e) {
                        FaOperateLogUtil.addLog("fa_inventory_task", (Long)assetUnitId2, ResManager.loadKDString("\u751f\u6210\u76d8\u70b9\u8bb0\u5f55", "FaInventoryTaskList_20", "fi-fa-formplugin", new Object[0]), unitName + ResManager.loadKDString("\u751f\u6210\u76d8\u70b9\u8bb0\u5f55\u4e8b\u52a1\u5931\u8d25,\u8054\u7cfb\u76f8\u5173\u4eba\u5458\u67e5\u770b\u65e5\u5fd7:", "FaInventoryTaskList_8", "fi-fa-formplugin", new Object[0]) + e.getMessage());
                        throw new KDBizException(ResManager.loadKDString("\u751f\u6210\u76d8\u70b9\u8bb0\u5f55\u4e8b\u52a1\u5931\u8d25,\u8054\u7cfb\u76f8\u5173\u4eba\u5458\u67e5\u770b\u65e5\u5fd7:", "FaInventoryTaskList_8", "fi-fa-formplugin", new Object[0]) + e.getMessage());
                    }
                }
            }
            if (notRightStatus.length() > 0) {
                final int unSuccess = notRightStatus.toString().split(" ").length;
                final int success = selectedRows.size() - unSuccess;
                this.getView().showTipNotification(String.format(ResManager.loadKDString("\u6210\u529f%d\u6761\uff0c\u5931\u8d25%d\u6761\uff0c\u5176\u4e2d[%s]\u751f\u6210\u5931\u8d25,\u4efb\u52a1\u72b6\u6001\u4e0d\u662f\u5df2\u4e0b\u8fbe", "FaInventoryTaskList_23", "fi-fa-formplugin", new Object[0]), success, unSuccess, notRightStatus));
            }
            else {
                this.getView().showSuccessNotification(ResManager.loadKDString("\u6240\u6709\u4efb\u52a1\u751f\u6210\u76d8\u70b9\u8bb0\u5f55\u6210\u529f", "FaInventoryTaskList_10", "fi-fa-formplugin", new Object[0]));
            }
            this.getView().invokeOperation("refresh");
        }
        if ("inventorydelete".equals(key)) {
            this.getView().showConfirm(ResManager.loadKDString("\u6b64\u64cd\u4f5c\u5c06\u6e05\u9664\u4efb\u52a1\u4e0b\u6240\u6709\u7684\u76d8\u70b9\u8bb0\u5f55\uff0c\u662f\u5426\u7ee7\u7eed", "FaInventoryTaskList_11", "fi-fa-formplugin", new Object[0]), "", MessageBoxOptions.YesNo, ConfirmTypes.Default, new ConfirmCallBackListener("inventorydelete", (IFormPlugin)this));
        }
    }
    
    private void createInventoryRedords(final MainEntityType inventoryRecordType, final DynamicObject inventoryTask, final String sic, final Set<Long> cardId) {
        try (final TXHandle h = TX.requiresNew()) {
            try {
                final DynamicObjectCollection realCards = QueryServiceHelper.query("fa_card_real", sic, new QFilter[] { new QFilter("id", "in", (Object)cardId) });
                final DynamicObject inventoryRecordEntity = (DynamicObject)inventoryRecordType.createInstance();
                final List<DynamicObject> inventoryRecordList = new ArrayList<DynamicObject>();
                for (final DynamicObject realCard : realCards) {
                    final DynamicObject inventoryRecordInstance = (DynamicObject)new CloneUtils(false, true).clone((IDataEntityType)inventoryRecordType, (Object)inventoryRecordEntity);
                    inventoryRecordInstance.set("realCard_id", realCard.get("id"));
                    inventoryRecordInstance.set("number", (Object)realCard.getString("number"));
                    inventoryRecordInstance.set("barcode", (Object)realCard.getString("barcode"));
                    inventoryRecordInstance.set("name", (Object)realCard.getString("assetname"));
                    inventoryRecordInstance.set("model", (Object)realCard.getString("model"));
                    inventoryRecordInstance.set("bookquantity", (Object)realCard.getBigDecimal("assetamount"));
                    inventoryRecordInstance.set("inventoryquantity", (Object)BigDecimal.ZERO);
                    inventoryRecordInstance.set("difference", (Object)BigDecimal.ZERO);
                    inventoryRecordInstance.set("inventorystate", (Object)BillStatus.B.toString());
                    inventoryRecordInstance.set("reason", (Object)" ");
                    inventoryRecordInstance.set("inventoryuser_id", realCard.get("headuseperson"));
                    inventoryRecordInstance.set("inventorytask_id", inventoryTask.getPkValue());
                    inventoryRecordInstance.set("inventschemeentry_id", inventoryTask.getDynamicObject("inventsscopeid").getDynamicObject("inventschemeentry").getPkValue());
                    inventoryRecordInstance.set("billstatus", (Object)"C");
                    inventoryRecordList.add(inventoryRecordInstance);
                }
                if (inventoryRecordList.size() > 0) {
                    SaveServiceHelper.save((IDataEntityType)inventoryRecordType, (Object[])inventoryRecordList.toArray(new DynamicObject[0]));
                }
            }
            catch (Throwable e) {
                h.markRollback();
                throw e;
            }
        }
    }
    
    public void confirmCallBack(final MessageBoxClosedEvent messageBoxClosedEvent) {
        super.confirmCallBack(messageBoxClosedEvent);
        final IListView view = (IListView)this.getView();
        final ListSelectedRowCollection selectedRows = view.getSelectedRows();
        final String callBackId = messageBoxClosedEvent.getCallBackId();
        if ("inventorydelete".equals(callBackId) && MessageBoxResult.Yes.equals((Object)messageBoxClosedEvent.getResult())) {
            final StringBuilder notRightStatus = new StringBuilder();
            for (final ListSelectedRow selectedRow : selectedRows) {
                final DynamicObject inventoryTask = BusinessDataServiceHelper.loadSingle((Object)String.valueOf(selectedRow), "fa_inventory_task");
                final DynamicObject inventorySope = inventoryTask.getDynamicObject("inventsscopeid");
                if (inventorySope == null) {
                    return;
                }
                final Object assetunitId = inventorySope.get("assetunit_id");
                final String assetUnitName = inventorySope.getDynamicObject("assetunit").getString("name");
                final String status = inventoryTask.getString("status");
                if (!BillStatus.C.toString().equals(status)) {
                    notRightStatus.append(assetUnitName).append(' ');
                    FaOperateLogUtil.addLog("fa_inventory_task", (Long)assetunitId, ResManager.loadKDString("\u6e05\u9664\u76d8\u70b9\u8bb0\u5f55", "FaInventoryTaskList_21", "fi-fa-formplugin", new Object[0]), assetUnitName + ResManager.loadKDString("\u5220\u9664\u76d8\u70b9\u8bb0\u5f55\u5931\u8d25,\u4efb\u52a1\u72b6\u6001\u4e0d\u662f\u5df2\u751f\u6210", "FaInventoryTaskList_12", "fi-fa-formplugin", new Object[0]));
                }
                else {
                    final Object inventoryTaskPk = selectedRow.getPrimaryKeyValue();
                    final QFilter inventoryTaskPkCond = new QFilter("inventorytask_id", "=", inventoryTaskPk);
                    DeleteServiceHelper.delete("fa_inventory_record", new QFilter[] { inventoryTaskPkCond });
                    inventoryTask.set("status", (Object)BillStatus.B.toString());
                    SaveServiceHelper.save(inventoryTask.getDataEntityType(), (Object[])new DynamicObject[] { inventoryTask });
                    FaOperateLogUtil.addLog("fa_inventory_task", (Long)assetunitId, ResManager.loadKDString("\u6e05\u9664\u76d8\u70b9\u8bb0\u5f55", "FaInventoryTaskList_21", "fi-fa-formplugin", new Object[0]), assetUnitName + ResManager.loadKDString("\u5220\u9664\u76d8\u70b9\u8bb0\u5f55\u6210\u529f", "FaInventoryTaskList_13", "fi-fa-formplugin", new Object[0]));
                }
            }
            if (notRightStatus.length() > 0) {
                this.getView().showTipNotification((Object)notRightStatus + ResManager.loadKDString("\u5220\u9664\u76d8\u70b9\u8bb0\u5f55\u5931\u8d25,\u4efb\u52a1\u72b6\u6001\u4e0d\u662f\u5df2\u751f\u6210", "FaInventoryTaskList_12", "fi-fa-formplugin", new Object[0]));
            }
            else {
                this.getView().showSuccessNotification(ResManager.loadKDString("\u5220\u9664\u76d8\u70b9\u8bb0\u5f55\u6210\u529f", "FaInventoryTaskList_13", "fi-fa-formplugin", new Object[0]));
            }
            this.getView().invokeOperation("refresh");
        }
    }
    
    public void closedCallBack(final ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        if (closedCallBackEvent.getReturnData() == null) {
            return;
        }
        final String content = closedCallBackEvent.getReturnData().toString();
        if (!content.isEmpty()) {
            final IAppCache cacheDepreCheck = AppCache.get("fa");
            final String title = (String)cacheDepreCheck.get("countingreport_message_title", (Class)String.class);
            this.sendMessage(content, title);
            return;
        }
        throw new KDBizException(ResManager.loadKDString("\u8bf7\u586b\u5199\u76d8\u70b9\u901a\u77e5", "FaInventoryTaskList_14", "fi-fa-formplugin", new Object[0]));
    }
    
    public static void inventoryAssign(final Object[] pkArray) {
        final QFilter filtersPk = new QFilter("id", "in", (Object)pkArray);
        final DynamicObject[] inventoryTasks = BusinessDataServiceHelper.load("fa_inventory_task", "status", new QFilter[] { filtersPk });
        final List<DynamicObject> inventoryTasksNeedChange = new ArrayList<DynamicObject>(inventoryTasks.length);
        for (final DynamicObject inventoryTask : inventoryTasks) {
            inventoryTask.set("status", (Object)BillStatus.B.toString());
            inventoryTasksNeedChange.add(inventoryTask);
        }
        if (inventoryTasksNeedChange.size() > 0) {
            SaveServiceHelper.save(inventoryTasksNeedChange.get(0).getDataEntityType(), inventoryTasksNeedChange.toArray());
        }
    }
    
    private void sendMessage(final String content, final String title) {
        final MessageInfo message = new MessageInfo();
        message.setType("message");
        if (StringUtils.isNotEmpty(title)) {
            message.setTitle(title);
        }
        else {
            message.setTitle(ResManager.loadKDString("\u4eba\u4eba\u8d44\u4ea7-\u76d8\u70b9", "FaInventoryTaskList_15", "fi-fa-formplugin", new Object[0]));
        }
        final List<Long> inventoryTaskPks = (List<Long>)SerializationUtils.fromJsonString(String.valueOf(this.getPageCache().get("inventoryTaskPks")), (Class)List.class);
        for (final Long inventoryTaskPk : inventoryTaskPks) {
            final List<Long> assetInventors = this.getAssetInventors(inventoryTaskPk);
            message.setUserIds((List)assetInventors);
            message.setSenderId(ContextUtil.getUserId());
            message.setSendTime(new Date(System.currentTimeMillis()));
            message.setEntityNumber("fa_inventory_task");
            final Long pkid = inventoryTaskPk;
            message.setBizDataId(pkid);
            message.setTag(ResManager.loadKDString("\u4eba\u4eba\u8d44\u4ea7", "FaInventoryTaskList_16", "fi-fa-formplugin", new Object[0]));
            final String clientUrl = UrlService.getDomainContextUrl();
            final StringBuilder urlWithEncodedParams = new StringBuilder(clientUrl);
            if (!clientUrl.trim().endsWith("/")) {
                urlWithEncodedParams.append("/");
            }
            final String strMobContentUrl = "%sintegration/yzjShareOpen.do?mb_formId=fa_mobile_inventpage&pkId=%s&device=mob&accountId=%s";
            message.setMobContentUrl(String.format(strMobContentUrl, urlWithEncodedParams.toString(), pkid, RequestContext.get().getAccountId()));
            FaInventoryTaskList.log.info("FaInventoryTaskList-message-MobContentUrl-is " + message.getMobContentUrl());
            final String strContentUrl = "%sindex.html?formId=fa_inventory_task&pkId=%s";
            message.setContentUrl(String.format(strContentUrl, urlWithEncodedParams.toString(), pkid));
            FaInventoryTaskList.log.info("FaInventoryTaskList-message-ContentUrl-is " + message.getContentUrl());
            message.setContent(content);
            FaInventoryTaskList.log.info("FaInventoryTaskList-message-Content-is " + message.getContent());
            message.setPubaccNumber("systempubacc");
            MessageCenterServiceHelper.sendMessage(message);
            this.getView().showMessage(ResManager.loadKDString("\u53d1\u9001\u666e\u901a\u6d88\u606f\u7ed9[\u7cfb\u7edf\u7ea7\u6d88\u606f\u52a9\u624b]\u6210\u529f", "FaInventoryTaskList_17", "fi-fa-formplugin", new Object[0]));
        }
    }
    
    private List<Long> getAssetInventors(final Long inventoryTaskPk) {
        final Set<Long> assetInventors = new HashSet<Long>();
        final String sic = Fa.join(",", new String[] { "inventoryuser" });
        final QFilter filters1 = new QFilter("inventorytask", "=", (Object)inventoryTaskPk);
        final DynamicObjectCollection inventoryRecords = QueryServiceHelper.query("fa_inventory_record", sic, new QFilter[] { filters1 });
        for (final DynamicObject dynamicObject : inventoryRecords) {
            assetInventors.add(dynamicObject.getLong("inventoryuser"));
        }
        return new ArrayList<Long>(assetInventors);
    }
    
    private void setDefaultQuerySelect(final FilterContainerInitArgs filtercontainerinitargs) {
        final List<FilterColumn> listFilterColumns = (List<FilterColumn>)filtercontainerinitargs.getFilterContainerInitEvent().getCommonFilterColumns();
        final Map<String, Object> customParams = (Map<String, Object>)this.getView().getFormShowParameter().getCustomParams();
        final Object schemeId = customParams.get("schemeId");
        final Long userId = ContextUtil.getUserId();
        final List<ComboItem> comboItemsStatus = new ArrayList<ComboItem>();
        final ComboItem b = new ComboItem(new LocaleString(ResManager.loadKDString("\u5df2\u4e0b\u8fbe", "FaInventoryTaskList_18", "fi-fa-formplugin", new Object[0])), BillStatus.B.toString());
        final ComboItem c = new ComboItem(new LocaleString(ResManager.loadKDString("\u5df2\u751f\u6210", "FaInventoryTaskList_19", "fi-fa-formplugin", new Object[0])), "C");
        if (schemeId != null) {
            final ListIterator<FilterColumn> deleteListFilterColumns = listFilterColumns.listIterator();
            while (deleteListFilterColumns.hasNext()) {
                final String fieldName = deleteListFilterColumns.next().getFieldName();
                if ("inventsscopeid.inventschemeentry.name".equals(fieldName)) {
                    deleteListFilterColumns.remove();
                }
            }
        }
        else {
            for (final FilterColumn listFilter : listFilterColumns) {
                final CommonFilterColumn commFilter = (CommonFilterColumn)listFilter;
                final String fieldName2 = commFilter.getFieldName();
                if ("inventsscopeid.inventschemeentry.name".equals(fieldName2)) {
                    final Set<ComboItem> comboItemsTrys = new HashSet<ComboItem>();
                    final QFilter filtersUserid = new QFilter("inventperson", "=", (Object)userId);
                    final QFilter filterStatus = new QFilter("status", "!=", (Object)"A");
                    final QFilter filterStatusZ = new QFilter("status", "!=", (Object)"Z");
                    final DynamicObjectCollection invenTasks = QueryServiceHelper.query("fa_inventory_task", "inventsscopeid", new QFilter[] { filtersUserid, filterStatus, filterStatusZ });
                    final Set<Object> scopeIds = invenTasks.stream().map(s -> s.getLong("inventsscopeid")).collect((Collector<? super Object, ?, Set<Object>>)Collectors.toSet());
                    final QFilter scopeIdsQ = new QFilter("id", "in", (Object)scopeIds);
                    final DynamicObjectCollection invenschemeetry = QueryServiceHelper.query("fa_inventory_sope", "inventschemeentry", new QFilter[] { scopeIdsQ }, "createtime desc");
                    final Set<Object> schemeIds = invenschemeetry.stream().map(s -> s.getLong("inventschemeentry")).collect((Collector<? super Object, ?, Set<Object>>)Collectors.toSet());
                    final QFilter schemeIdsQ = new QFilter("id", "in", (Object)schemeIds);
                    final DynamicObjectCollection inventschemes = QueryServiceHelper.query("fa_inventscheme_new", "id,name", new QFilter[] { schemeIdsQ }, "createtime desc");
                    for (final DynamicObject inventscheme : inventschemes) {
                        final ComboItem item = new ComboItem(new LocaleString(inventscheme.getString("name")), inventscheme.getString("id"));
                        comboItemsTrys.add(item);
                    }
                    if (!CollectionUtils.isEmpty((Collection)inventschemes)) {
                        commFilter.setDefaultValue(((DynamicObject)inventschemes.get(0)).getString("id"));
                    }
                    final List<ComboItem> allInventoryName = new ArrayList<ComboItem>(comboItemsTrys);
                    commFilter.setComboItems((List)allInventoryName);
                    commFilter.setMustInput(true);
                }
                else {
                    if (!"status".equals(fieldName2)) {
                        continue;
                    }
                    comboItemsStatus.add(b);
                    comboItemsStatus.add(c);
                    commFilter.setComboItems((List)comboItemsStatus);
                }
            }
        }
    }
    
    static {
        log = LogFactory.getLog((Class)FaInventoryTaskList.class);
        DATA_SIZE = DataManagerUtils.getBatchSize();
    }
}
