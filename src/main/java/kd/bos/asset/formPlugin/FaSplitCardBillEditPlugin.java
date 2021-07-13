package kd.bos.asset.formPlugin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import kd.bos.asset.entry.FieldEntry;
import kd.bos.asset.entry.ScaleEntry;
import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.validate.BillStatus;
import kd.bos.exception.KDBizException;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.DateEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.operate.AbstractOperate;
import kd.bos.form.plugin.IFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.util.StringUtils;
import kd.fi.fa.business.dao.factory.FaBillDaoFactory;
import kd.fi.fa.business.utils.FaBizUtils;
import kd.fi.fa.business.utils.FaPermissionUtils;
import kd.fi.fa.common.util.Fa;
import kd.fi.fa.formplugin.FaBillBaseFormPlugin;
import kd.fi.fa.utils.FaUtils;


public class FaSplitCardBillEditPlugin extends FaBillBaseFormPlugin
{
    private static final String KEY_CALLBACK_SPLIYSCALE = "callback_splitscale";
    private static final String KEY_BAR_AVGSPLIT = "bar_avgsplit";
    private static final String KEY_BAR_SCALESPLIT = "bar_scalesplit";
    private static final String KEY_CACHE_SPLITTYPE = "cache_splittype";
    private static final String SPLIT = "split";
    
    @Override
    public void afterCreateNewData(final EventObject eventobject) {
        super.afterCreateNewData(eventobject);
        this.initValue();
    }
    
    public void beforeBindData(final EventObject e) {
        super.beforeBindData(e);
    }
    
    public void afterBindData(final EventObject e) {
        final IDataModel model = this.getModel();
        this.setDataRange();
        final DynamicObject realDy = model.getDataEntity().getDynamicObject("split_realcard");
        this.setEnableWhenOne(realDy);
        final String splitType = model.getDataEntity().getString("splittype");
        this.getPageCache().put("cache_splittype", splitType);
    }
    
    public void registerListener(final EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(new String[] { "tbmain" });
        final BasedataEdit realcardEdit = (BasedataEdit)this.getView().getControl("split_realcard");
        realcardEdit.addBeforeF7SelectListener(evt -> this.beforeF7RealCardSelect(evt));
        final BasedataEdit org = (BasedataEdit)this.getControl("org");
        org.addBeforeF7SelectListener(evt -> this.beforeOrgSelect(evt));
    }
    
    public void afterLoadData(final EventObject e) {
        final IDataModel model = this.getModel();
    }
    
    public void propertyChanged(final PropertyChangedArgs e) {
        final IDataModel model = this.getModel();
        final String property = e.getProperty().getName();
        final ChangeData changeData = e.getChangeSet()[0];
        final String s = property;
        switch (s) {
            case "split_realcard": {
                if (changeData == null || changeData.getNewValue() == null) {
                    model.deleteEntryData("assetsplitentry");
                    model.deleteEntryData("subassetsplitentry");
                    return;
                }
                final DynamicObject realDy = (DynamicObject)changeData.getNewValue();
                this.splitCardPropertyChanged(realDy);
                break;
            }
            case "splittype": {
                if (changeData == null || changeData.getNewValue() == null) {
                    return;
                }
                final String splitType = changeData.getNewValue().toString();
                this.splitTypePropertyChanged(splitType);
                break;
            }
            case "zsf_aft_realcard":{
            	Object oldData = changeData.getOldValue();
            	System.err.println(oldData);
            	if (changeData == null || changeData.getNewValue() == null || changeData.getOldValue() == null) {
                    return;
                }
            	this.assetNamePropertyChanged( changeData.getNewValue().toString());
                
            	break;
            }
        }
    }
    
    public void itemClick(final ItemClickEvent evt) {
        final String itemKey2;
        final String itemKey = itemKey2 = evt.getItemKey();
        switch (itemKey2) {
            case "bar_avgsplit": {
                this.avgSplit();
                break;
            }
            case "bar_scalesplit": {
                this.scaleSplit();
                break;
            }
        }
    }
    
    public void beforeDoOperation(final BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        final String type = ((AbstractOperate)args.getSource()).getOperateKey();
        final List<String> saveOperateType = Arrays.asList("save", "submit", "submitandnew");
        if (saveOperateType.contains(type)) {
            final String errMsg = this.checkSplitData();
            if (!errMsg.isEmpty()) {
                this.getView().showTipNotification(errMsg);
                args.cancel = true;
                return;
            }
            this.saveSplit();
        }
    }
    
    public void afterDoOperation(final AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);
        final String type = ((AbstractOperate)args.getSource()).getOperateKey();
        final List<String> doOperateType = Collections.singletonList("unaudit");
        if (doOperateType.contains(type)) {
            this.resetAftSplitCard();
        }
        else if (type.equalsIgnoreCase("audit")) {
            this.resetAftAudit();
        }
    }
    
    public void closedCallBack(final ClosedCallBackEvent closedCallBackEvent) {
        super.closedCallBack(closedCallBackEvent);
        final String actionId = closedCallBackEvent.getActionId();
        if ("callback_splitscale".equalsIgnoreCase(actionId)) {
            this.callBackSplitScaleSet(closedCallBackEvent);
        }
    }
    
    private void initValue() {
        final IDataModel model = this.getModel();
        final Object isChangingMainOrg = this.getModel().getContextVariable("isChangingMainOrg");
        if (isChangingMainOrg == null && this.getView().getParentView() != null && this.getView().getParentView().toString().indexOf("FormView-fa_mainpage_grid") == 0) {
            final List<Long> accountUnits = (List<Long>)FaPermissionUtils.getAllBookAndPermissionOrgs("fa_assetsplitbill");
            final Long org = FaPermissionUtils.getDefaultAcctOrg((List)accountUnits);
            this.getModel().setValue("fa_assetsplitbill", (Object)org);
        }
        this.setDataRange();
        model.setValue("billstatus", (Object)BillStatus.A);
        model.setValue("appliant", model.getValue("creator"));
        final Object orgObj = this.getModel().getValue("org");
        if (orgObj != null) {
            final Long org = ((DynamicObject)orgObj).getLong("id");
            final DynamicObject mainBook = FaBizUtils.getAsstBookByOrg(org);
            if (mainBook != null) {
                this.getModel().setValue("splitperiod", (Object)mainBook.getLong("curperiod"));
            }
        }
    }
    
    private void setDataRange() {
        FaBizUtils.setDate(this.getView(), "splitdate", (DateEdit)this.getControl("splitdate"), false, this.getView().getFormShowParameter().getStatus().equals((Object)OperationStatus.ADDNEW));
    }
    
    private void beforeOrgSelect(final BeforeF7SelectEvent evt) {
        final List<Long> permissionOrgs = (List<Long>)FaPermissionUtils.getViewPermissionLeafOrg("fa_assetsplitbill");
        final QFilter bookFilter = new QFilter("status", "=", (Object)"C");
        final DynamicObjectCollection assBooks = QueryServiceHelper.query("fa_assetbook", "org,ismainbook", new QFilter[] { bookFilter });
        final Map<Long, List<DynamicObject>> assBooksMap = assBooks.stream().collect(Collectors.groupingBy(assBook -> assBook.getLong("org")));
        final List<Long> onlyMainOrgs = assBooksMap.entrySet().stream().filter(kv -> kv.getValue().size() == 1).map(kv -> kv.getKey()).collect(Collectors.toList());
        onlyMainOrgs.retainAll(permissionOrgs);
        final QFilter qFilter = new QFilter("id", "in", (Object)onlyMainOrgs);
        final List<QFilter> filters = (List<QFilter>)((ListShowParameter)evt.getFormShowParameter()).getListFilterParameter().getQFilters();
        filters.add(qFilter);
    }
    
    private void beforeF7RealCardSelect(final BeforeF7SelectEvent evt) {
        final List<QFilter> filterList = this.getSelectCardFilter();
        final ListShowParameter param = (ListShowParameter)evt.getFormShowParameter();
        final List<QFilter> filters = (List<QFilter>)param.getListFilterParameter().getQFilters();
        if (filterList != null && filterList.size() > 0) {
            filters.addAll(filterList);
        }
    }
    
    private List<QFilter> getSelectCardFilter() {
        final IDataModel model = this.getModel();
        final List<QFilter> filterList = new ArrayList<QFilter>();
        final Set<Long> realCardIds = new HashSet<Long>();
        for (int rowcount = model.getEntryRowCount("assetsplitentry"), i = 0; i < rowcount; ++i) {
            final DynamicObject realCard = (DynamicObject)model.getValue("realcard", i);
            if (realCard != null) {
                realCardIds.add((Long)realCard.getPkValue());
            }
        }
        final long orgId = (long)model.getValue("org_id");
        final DynamicObject mainBook = FaUtils.getMainBookByOrg(orgId);
        if (mainBook == null) {
            throw new KDBizException(ResManager.loadKDString("\u8bf7\u8bbe\u7f6e\u4e3b\u8d26\u7c3f", "FaSplitCardBillEditPlugin_0", "fi-fa-formplugin", new Object[0]));
        }
        final List<QFilter> finFilters = new ArrayList<QFilter>();
        finFilters.add(new QFilter("assetbook", "=", (Object)mainBook.getLong("id")));
        finFilters.add(new QFilter("org", "=", (Object)orgId));
        final QFilter[] finFilterArr = finFilters.toArray(new QFilter[0]);
        final DynamicObjectCollection cards = QueryServiceHelper.query("fa_card_fin", "realcard", finFilterArr);
        final Set<Long> cardList = new HashSet<Long>(cards.size());
        for (final DynamicObject card : cards) {
            cardList.add(card.getLong("realcard"));
        }
        cardList.removeAll(realCardIds);
        filterList.add(new QFilter("isbak", "=", (Object)'0'));
        if (cardList.size() > 0) {
            filterList.add(new QFilter("id", "in", (Object)cardList));
            filterList.add(new QFilter("bizstatus", "=", (Object)"READY"));
            filterList.add(new QFilter("billstatus", "=", (Object)"C"));
        }
        else {
            filterList.add(new QFilter("1", "=", (Object)"2"));
        }
        return filterList;
    }
    
    private void resetAftSplitCard() {
        final IDataModel model = this.getModel();
        final DynamicObject dateEntity = model.getDataEntity();
        final DynamicObjectCollection befRowDys = dateEntity.getDynamicObjectCollection("assetsplitentry");
        for (final DynamicObject befRowDy : befRowDys) {
            final long befRealId = befRowDy.getLong("realcard_id");
            final long befFinId = befRowDy.getLong("bef_fincard_id");
            final DynamicObjectCollection aftRowDys = befRowDy.getDynamicObjectCollection("subassetsplitentry");
            for (final DynamicObject aftRowDy : aftRowDys) {
                final DynamicObject aftRealDy = aftRowDy.getDynamicObject("aft_realcard");
                final long aftRealId = (aftRealDy == null) ? 0L : aftRealDy.getLong("id");
                final DynamicObject aftFinDy = aftRowDy.getDynamicObject("aft_fincard");
                final long aftFinId = (aftFinDy == null) ? 0L : aftFinDy.getLong("id");
                if (befRealId != aftRealId) {
                    aftRowDy.set("aft_realcard", (Object)null);
//                    aftRowDy.set("zsf_aft_realcard", (Object)null);
                    aftRowDy.set("aft_cardbillno", (Object)"");
                    aftRowDy.set("aft_cardnumber", (Object)"");
                }
                if (befFinId != aftFinId) {
                    aftRowDy.set("aft_fincard", (Object)null);
//                    aftRowDy.set("zsf_aft_realcard", (Object)null);
                }
            }
        }
        final String splitType = model.getValue("splittype").toString();
        this.splitTypePropertyChanged(splitType);
        this.getView().updateView("subassetsplitentry");
    }
    
    private void resetAftAudit() {
        final IDataModel model = this.getModel();
        final DynamicObject dateEntity = model.getDataEntity(true);
        final DynamicObjectCollection befRowDys = dateEntity.getDynamicObjectCollection("assetsplitentry");
        int index = 0;
        for (final DynamicObject befRowDy : befRowDys) {
            final long befFinId = befRowDy.getLong("bef_fincard_id");
            final DynamicObjectCollection aftRowDys = befRowDy.getDynamicObjectCollection("subassetsplitentry");
            int subIndex = 0;
            for (final DynamicObject aftRowDy : aftRowDys) {
                model.setEntryCurrentRowIndex("assetsplitentry", index);
                final long aftFinId = aftRowDy.getLong("aft_fincard_id");
                final long aftRealId = aftRowDy.getLong("aft_realcard_id");
                if (befFinId != aftFinId) {
                    model.setValue("aft_realcard", (Object)aftRealId, subIndex, index);
                    model.setValue("zsf_aft_realcard", (Object)aftRowDy.getString("zsf_aft_realcard"), subIndex, index);
                }
                ++subIndex;
            }
            ++index;
        }
    }
    
    private String checkSplitData() {
        String errMsg = "";
        final IDataModel model = this.getModel();
        final String splitType = model.getValue("splittype").toString();
        final String curSplitType = this.getPageCache().get("cache_splittype");
        if (!splitType.equalsIgnoreCase(curSplitType)) {
            errMsg = ResManager.loadKDString("\u62c6\u5206\u65b9\u5f0f\u5df2\u53d1\u751f\u53d8\u5316\uff0c\u8bf7\u91cd\u65b0\u6267\u884c\u62c6\u5206", "FaSplitCardBillEditPlugin_1", "fi-fa-formplugin", new Object[0]);
            return errMsg;
        }
        final int befCount = model.getEntryRowCount("assetsplitentry");
        if (befCount == 0) {
            errMsg = ResManager.loadKDString("\u62c6\u5206\u524d\u7684\u5361\u7247\u4e0d\u80fd\u4e3a\u7a7a", "FaSplitCardBillEditPlugin_2", "fi-fa-formplugin", new Object[0]);
            return errMsg;
        }
        final List<String> errorList = new ArrayList<String>();
        final String fieldName = splitType.equalsIgnoreCase("A") ? "aft_assetamount" : "aft_originalval";
        final int splitQty = (int)model.getValue("splitqty");
        for (int befIndex = 0; befIndex < befCount; ++befIndex) {
            final DynamicObject realDy = (DynamicObject)model.getValue("realcard", befIndex);
            if (realDy != null) {
                final BigDecimal totalQty = (BigDecimal)model.getValue("bef_assetamount", befIndex);
                final BigDecimal totalOriginalval = (BigDecimal)model.getValue("bef_originalval", befIndex);
                final DynamicObjectCollection aftSplitEntrys = (DynamicObjectCollection)model.getValue("subassetsplitentry", befIndex);
                final String assetNumber = realDy.getString("number");
                if (totalQty.compareTo(BigDecimal.ONE) == 0 && splitType.equalsIgnoreCase("A")) {
                    final String errStr = String.format(ResManager.loadKDString("\u5361\u7247[ %s ] \u62c6\u5206\u5361\u7247\u6570\u91cf\u4e3a1\u65f6\uff0c\u4e0d\u80fd\u8fdb\u884c\u6570\u91cf\u62c6\u5206", "FaSplitCardBillEditPlugin_3", "fi-fa-formplugin", new Object[0]), assetNumber);
                    errorList.add(errStr);
                }
                else if (splitQty != aftSplitEntrys.size()) {
                    final String errStr = String.format(ResManager.loadKDString("\u5361\u7247[ %s ] \u62c6\u5206\u540e\u5361\u7247\u6570\u91cf\u548c\u62c6\u5206\u5361\u7247\u6570\u91cf\u4e0d\u4e00\u81f4\uff0c\u91cd\u65b0\u6267\u884c\u62c6\u5206", "FaSplitCardBillEditPlugin_4", "fi-fa-formplugin", new Object[0]), assetNumber);
                    errorList.add(errStr);
                }
                else if (aftSplitEntrys == null || aftSplitEntrys.size() == 0) {
                    final String errStr = String.format(ResManager.loadKDString("\u5361\u7247[ %s ] \u6ca1\u6709\u6267\u884c\u5361\u7247\u62c6\u5206", "FaSplitCardBillEditPlugin_5", "fi-fa-formplugin", new Object[0]), assetNumber);
                    errorList.add(errStr);
                }
                else {
                    BigDecimal sumData = BigDecimal.ZERO;
                    BigDecimal sumOriData = BigDecimal.ZERO;
                    for (final DynamicObject aftDy : aftSplitEntrys) {
                        final BigDecimal curData = aftDy.getBigDecimal(fieldName);
                        sumData = sumData.add(curData);
                        final BigDecimal oriData = aftDy.getBigDecimal("aft_originalval");
                        sumOriData = sumOriData.add(oriData);
                    }
                    if (splitType.equalsIgnoreCase("A")) {
                        if (sumData.compareTo(totalQty) != 0) {
                            final String errStr2 = String.format(ResManager.loadKDString("\u5361\u7247[ %s ] \u62c6\u5206\u524d\u540e\u7684\u8d44\u4ea7\u6570\u91cf\u4e0d\u4e00\u81f4\uff0c\u8bf7\u68c0\u67e5", "FaSplitCardBillEditPlugin_6", "fi-fa-formplugin", new Object[0]), assetNumber);
                            errorList.add(errStr2);
                        }
                        else if (sumOriData.compareTo(totalOriginalval) != 0) {
                            final String errStr2 = String.format(ResManager.loadKDString("\u5361\u7247[ %s ] \u62c6\u5206\u524d\u540e\u7684\u8d44\u4ea7\u539f\u503c\u4e0d\u4e00\u81f4\uff0c\u8bf7\u68c0\u67e5", "FaSplitCardBillEditPlugin_7", "fi-fa-formplugin", new Object[0]), assetNumber);
                            errorList.add(errStr2);
                        }
                    }
                    else if (splitType.equalsIgnoreCase("B") && sumData.compareTo(totalOriginalval) != 0) {
                        final String errStr2 = String.format(ResManager.loadKDString("\u5361\u7247[ %s ] \u62c6\u5206\u524d\u540e\u7684\u8d44\u4ea7\u539f\u503c\u4e0d\u4e00\u81f4\uff0c\u8bf7\u68c0\u67e5", "FaSplitCardBillEditPlugin_7", "fi-fa-formplugin", new Object[0]), assetNumber);
                        errorList.add(errStr2);
                    }
                }
            }
        }
        if (errorList.size() > 0) {
            errMsg = String.join(",", errorList);
        }
        return errMsg;
    }
    
    void assetNamePropertyChanged(final String assetname) {
        final IDataModel model = this.getModel();
        int index = model.getEntryCurrentRowIndex("subassetsplitentry");
        this.getPageCache().put("zsf_aft_realcard_"+index, assetname);
        System.err.println("set入值："+assetname);
    }
    
    void splitTypePropertyChanged(final String splitType) {
        final IFormView view = this.getView();
        final IDataModel model = this.getModel();
        for (int rowcount = model.getEntryRowCount("subassetsplitentry"), i = 0; i < rowcount; ++i) {
            if (splitType.equalsIgnoreCase("A")) {
                view.setEnable(Boolean.valueOf(true), i, new String[] { "aft_assetamount" });
                view.setEnable(Boolean.valueOf(true), i, new String[] { "aft_originalval" });
            }
            else if (splitType.equalsIgnoreCase("B")) {
                view.setEnable(Boolean.valueOf(false), i, new String[] { "aft_assetamount" });
                view.setEnable(Boolean.valueOf(true), i, new String[] { "aft_originalval" });
            }
        }
        view.updateView("subassetsplitentry");
    }
    
    void splitCardPropertyChanged(final DynamicObject realDy) {
        final IDataModel model = this.getModel();
        if (realDy == null) {
            model.deleteEntryData("assetsplitentry");
            model.deleteEntryData("subassetsplitentry");
            return;
        }
        final Set<Object> realCardPKSet = new HashSet<Object>();
        realCardPKSet.add(realDy.getPkValue());
        model.deleteEntryData("assetsplitentry");
        final Map<String, List<FieldEntry>> fieldsMap = this.getSplitFields();
        final Map<String, DynamicObject> finMap = this.getFinCardByReal(realCardPKSet, fieldsMap.get("fa_card_fin"));
        int befIndex = 0;
        for (final DynamicObject finDy : finMap.values()) {
            final DynamicObject curRrealDy = finDy.getDynamicObject("realcard");
            befIndex = model.insertEntryRow("assetsplitentry", befIndex);
            model.setValue("realcard", curRrealDy.getPkValue(), befIndex);
            final DynamicObject unitDy = curRrealDy.getDynamicObject("unit");
            final Object unitPK = (unitDy == null) ? Integer.valueOf(0) : unitDy.getPkValue();
            model.setValue("bef_unit", unitPK, befIndex);
            model.setValue("bef_fincard", finDy.getPkValue(), befIndex);
            model.setValue("bef_basecurrency", finDy.get("basecurrency"), befIndex);
            model.setValue("bef_assetamount", (Object)curRrealDy.getBigDecimal("assetamount"), befIndex);
            model.setValue("bef_originalval", (Object)finDy.getBigDecimal("originalval"), befIndex);
            final BigDecimal monthdepre = finDy.getBigDecimal("monthdepre");
            final BigDecimal accumdepre = finDy.getBigDecimal("accumdepre");
            final BigDecimal addupyeardepre = finDy.getBigDecimal("addupyeardepre");
            final BigDecimal netWorth = finDy.getBigDecimal("networth");
            final BigDecimal newAmount = finDy.getBigDecimal("netamount");
            final long curPeriodId = this.getCurPeriod(finDy);
            final long bizPeriodId = finDy.getLong("bizperiod_id");
            if (bizPeriodId == curPeriodId) {
                model.setValue("bef_accumdepre", (Object)accumdepre.subtract(monthdepre), befIndex);
                model.setValue("bef_addupyeardepre", (Object)addupyeardepre.subtract(monthdepre), befIndex);
            }
            else {
                model.setValue("bef_accumdepre", (Object)accumdepre, befIndex);
                model.setValue("bef_addupyeardepre", (Object)addupyeardepre, befIndex);
            }
            model.setValue("bef_decval", (Object)finDy.getBigDecimal("decval"), befIndex);
            model.setValue("bef_preresidualval", (Object)finDy.getBigDecimal("preresidualval"), befIndex);
            model.setValue("bef_networth", (Object)netWorth.add(monthdepre), befIndex);
            model.setValue("bef_netamount", (Object)newAmount.add(monthdepre), befIndex);
            model.setValue("bef_originalamount", (Object)finDy.getBigDecimal("originalamount"), befIndex);
            model.setValue("bef_incometax", (Object)finDy.getBigDecimal("incometax"), befIndex);
            model.setValue("bef_monthorigvalchg", (Object)finDy.getBigDecimal("monthorigvalchg"), befIndex);
            model.setValue("bef_yearorigvalchg", (Object)finDy.getBigDecimal("yearorigvalchg"), befIndex);
            model.setValue("bef_monthdepre", (Object)finDy.getBigDecimal("monthdepre"), befIndex);
            model.setValue("bef_monthdeprechg", (Object)finDy.getBigDecimal("monthdeprechg"), befIndex);
            final BigDecimal partClearDepre = this.getPartClearDepre(finDy.getLong("id"));
            model.setValue("bef_partcleardepre", (Object)partClearDepre, befIndex);
        }
        this.setEnableWhenOne(realDy);
    }
    
    void setEnableWhenOne(final DynamicObject realDy) {
        if (realDy == null) {
            return;
        }
        final IDataModel model = this.getModel();
        final BigDecimal qty = realDy.getBigDecimal("assetamount");
        this.getView().setEnable(Boolean.valueOf(true), new String[] { "splittype" });
        if (qty.compareTo(BigDecimal.ONE) == 0) {
            model.setValue("splittype", (Object)"B");
            this.getView().setEnable(Boolean.valueOf(false), new String[] { "splittype" });
            this.getView().updateView("splittype");
        }
        final String splitType = model.getValue("splittype").toString();
        this.splitTypePropertyChanged(splitType);
    }
    
    long getCurPeriod(final DynamicObject finDy) {
        final long orgId = (long)this.getModel().getValue("org_id");
        final long depureId = finDy.getLong("depreuse_id");
        final DynamicObject curPeriodDy = FaBizUtils.getAsstBookByOrgAndDepreuse(Long.valueOf(orgId), Long.valueOf(depureId), "curperiod");
        final long curPeriod = curPeriodDy.getLong("curperiod");
        return curPeriod;
    }
    
    public Map<String, DynamicObject> queryRealCards(final Set<Object> realCardPKSet, final List<FieldEntry> fieldEntrys) {
        final Map<String, DynamicObject> realCardMap = new LinkedHashMap<String, DynamicObject>(fieldEntrys.size());
        final Set<Long> realCardPK = new HashSet<Long>();
        realCardPKSet.forEach(r -> realCardPK.add(Long.parseLong(String.valueOf(r))));
        final QFilter[] filters = { new QFilter("id", "in", (Object)realCardPK) };
        final Set<String> fields = fieldEntrys.stream().map(field -> field.filed).collect(Collectors.toSet());
        fields.add("unit");
        final String selector = String.join(",", fields);
        final DynamicObject[] query;
        final DynamicObject[] realCards = query = FaBillDaoFactory.getInstance("fa_card_real").query(selector, filters);
        for (final DynamicObject realCard : query) {
            final String realCardId = realCard.getPkValue().toString();
            realCardMap.put(realCardId, realCard);
        }
        return realCardMap;
    }
    
    private Map<String, DynamicObject> getFinCardByReal(final Set<Object> realCards, final List<FieldEntry> fieldEntrys) {
        final Map<String, DynamicObject> finCardMap = new LinkedHashMap<String, DynamicObject>(realCards.size());
        final Set<String> fieldsSet = fieldEntrys.stream().map(field -> field.filed).collect(Collectors.toSet());
        fieldsSet.add("basecurrency");
        final List<DynamicObject> finCardLst = FaUtils.queryFinCardList(realCards, fieldsSet);
        for (final DynamicObject finCard : finCardLst) {
            final String readCardStr = finCard.getDynamicObject("realcard").getPkValue().toString();
            final String depreuseStr = finCard.getDynamicObject("depreuse").getPkValue().toString();
            final String keyStr = String.format("%s_%s", readCardStr, depreuseStr);
            finCardMap.put(keyStr, finCard);
        }
        return finCardMap;
    }
    
    private void saveSplit() {
        final IDataModel model = this.getModel();
        final Object realCard = model.getValue("split_realcard");
        if (realCard == null) {
            this.getView().showErrorNotification(ResManager.loadKDString("\u8bf7\u5148\u9009\u53d6\u8981\u62c6\u5206\u7684\u5361\u7247", "FaSplitCardBillEditPlugin_8", "fi-fa-formplugin", new Object[0]));
            return;
        }
        for (int rowcount = model.getEntryRowCount("assetsplitentry"), befIndex = 0; befIndex < rowcount; ++befIndex) {
            final List<BigDecimal> scaleList = this.getScaleList(befIndex);
            this.createSplitCard(befIndex, scaleList, "save");
        }
        final String splitType = model.getDataEntity().getString("splittype");
        this.splitTypePropertyChanged(splitType);
    }
    
    private void avgSplit() {
        final IDataModel model = this.getModel();
        final Object realCard = model.getValue("split_realcard");
        final String splitType = model.getValue("splittype").toString();
        if (realCard == null) {
            this.getView().showErrorNotification(ResManager.loadKDString("\u8bf7\u5148\u9009\u53d6\u8981\u62c6\u5206\u7684\u5361\u7247", "FaSplitCardBillEditPlugin_8", "fi-fa-formplugin", new Object[0]));
            return;
        }
        final DynamicObject realDy = (DynamicObject)realCard;
        final BigDecimal assetAmount = realDy.getBigDecimal("assetamount");
        if (assetAmount.compareTo(BigDecimal.ONE) == 0 && splitType.equalsIgnoreCase("A")) {
            this.getView().showErrorNotification(ResManager.loadKDString("\u62c6\u5206\u5361\u7247\u6570\u91cf\u4e3a1\u65f6\uff0c\u4e0d\u80fd\u8fdb\u884c\u6570\u91cf\u62c6\u5206", "FaSplitCardBillEditPlugin_9", "fi-fa-formplugin", new Object[0]));
            return;
        }
        final Object qtyObj = model.getValue("splitqty");
        if (qtyObj == null) {
            this.getView().showErrorNotification(ResManager.loadKDString("\u8bf7\u5148\u8f93\u5165\u62c6\u5206\u540e\u5361\u7247\u6570\u91cf", "FaSplitCardBillEditPlugin_10", "fi-fa-formplugin", new Object[0]));
            return;
        }
        final int qty = (int)qtyObj;
        final List<BigDecimal> scaleList = new ArrayList<BigDecimal>(qty);
        for (int i = 0; i < qty; ++i) {
            scaleList.add(new BigDecimal(1));
        }
        this.createSplitCards(scaleList);
        this.splitTypePropertyChanged(splitType);
    }
    
    private void scaleSplit() {
        final IDataModel model = this.getModel();
        final Object realCard = model.getValue("split_realcard");
        final String splitType = model.getValue("splittype").toString();
        if (realCard == null) {
            this.getView().showErrorNotification(ResManager.loadKDString("\u8bf7\u5148\u9009\u53d6\u8981\u62c6\u5206\u7684\u5361\u7247", "FaSplitCardBillEditPlugin_8", "fi-fa-formplugin", new Object[0]));
            return;
        }
        final DynamicObject realDy = (DynamicObject)realCard;
        final BigDecimal assetAmount = realDy.getBigDecimal("assetamount");
        if (assetAmount.compareTo(BigDecimal.ONE) == 0 && splitType.equalsIgnoreCase("A")) {
            this.getView().showErrorNotification(ResManager.loadKDString("\u62c6\u5206\u5361\u7247\u6570\u91cf\u4e3a1\u65f6\uff0c\u4e0d\u80fd\u8fdb\u884c\u6570\u91cf\u62c6\u5206", "FaSplitCardBillEditPlugin_9", "fi-fa-formplugin", new Object[0]));
            return;
        }
        final Object qty = model.getValue("splitqty");
        if (qty == null || qty.toString().equalsIgnoreCase("0")) {
            this.getView().showErrorNotification(ResManager.loadKDString("\u8bf7\u5148\u8f93\u5165\u62c6\u5206\u540e\u5361\u7247\u6570\u91cf", "FaSplitCardBillEditPlugin_10", "fi-fa-formplugin", new Object[0]));
            return;
        }
        this.showSplitScaleSetForm();
    }
    
    List<BigDecimal> getScaleList(final int befIndex) {
        final IDataModel model = this.getModel();
        final DynamicObjectCollection aftSplitEntrys = (DynamicObjectCollection)model.getValue("subassetsplitentry", befIndex);
        final List<BigDecimal> result = new ArrayList<BigDecimal>(aftSplitEntrys.size());
        final String splitType = model.getValue("splittype").toString();
        final String fieldName = "aft_originalval";
        for (final DynamicObject aftSplitEntry : aftSplitEntrys) {
            result.add(aftSplitEntry.getBigDecimal(fieldName));
        }
        return result;
    }
    
    List<BigDecimal> getAssetamount() {
        final IDataModel model = this.getModel();
        final DynamicObjectCollection aftSplitEntrys = (DynamicObjectCollection)model.getValue("subassetsplitentry");
        final List<BigDecimal> result = new ArrayList<BigDecimal>(aftSplitEntrys.size());
        for (final DynamicObject aftSplitEntry : aftSplitEntrys) {
            result.add(aftSplitEntry.getBigDecimal("aft_assetamount"));
        }
        return result;
    }
    
    void createSplitCards(final List<BigDecimal> scaleList) {
        final IDataModel model = this.getModel();
        for (int rowcount = model.getEntryRowCount("assetsplitentry"), befIndex = 0; befIndex < rowcount; ++befIndex) {
            this.createSplitCard(befIndex, scaleList, "split");
        }
        this.getPageCache().put("cache_splittype", model.getValue("splittype").toString());
    }
    
    void createSplitCard(final int befIndex, List<BigDecimal> scaleList, final String type) {
        final IDataModel model = this.getModel();
        final IFormView view = this.getView();
        int aftIndex = 0;
        final int lastPos = scaleList.size() - 1;
        model.setEntryCurrentRowIndex("assetsplitentry", befIndex);
        if (scaleList == null || scaleList.size() == 0) {
            scaleList = this.getScaleList(befIndex);
        }
        final List<BigDecimal> assetamountList = this.getAssetamount();
        model.deleteEntryData("subassetsplitentry");
        final DynamicObject realDy = (DynamicObject)model.getValue("realcard", befIndex);
        if (realDy == null) {
            return;
        }
        final DynamicObject unitDy = (DynamicObject)model.getValue("bef_unit", befIndex);
        final int unitPrecision = (unitDy == null) ? 15 : unitDy.getInt("precision");
        final List<ScaleEntry> unitScales = this.getScaleDataByList(scaleList, unitPrecision);
        final DynamicObject baseCurrencyDy = (DynamicObject)model.getValue("bef_basecurrency", befIndex);
        final int currencyPrecision = (baseCurrencyDy == null) ? 15 : baseCurrencyDy.getInt("amtprecision");
        final List<ScaleEntry> currencyScales = this.getScaleDataByList(scaleList, currencyPrecision);
        final DynamicObject finDy = (DynamicObject)model.getValue("bef_fincard", befIndex);
        BigDecimal sumAssetamount = BigDecimal.ZERO;
        BigDecimal sumOriginalval = BigDecimal.ZERO;
        BigDecimal sumAccumdepre = BigDecimal.ZERO;
        BigDecimal sumAddupyeardepre = BigDecimal.ZERO;
        BigDecimal sumDecval = BigDecimal.ZERO;
        BigDecimal sumPreresidualval = BigDecimal.ZERO;
        BigDecimal sumNetworth = BigDecimal.ZERO;
        BigDecimal sumNetamount = BigDecimal.ZERO;
        BigDecimal sumOriginalamount = BigDecimal.ZERO;
        BigDecimal sumIncometax = BigDecimal.ZERO;
        BigDecimal sumMonthorigvalchg = BigDecimal.ZERO;
        BigDecimal sumYearorigvalchg = BigDecimal.ZERO;
        BigDecimal sumMonthdepre = BigDecimal.ZERO;
        BigDecimal sumMonthdeprechg = BigDecimal.ZERO;
        BigDecimal sumPartcleardepre = BigDecimal.ZERO;
        final BigDecimal befAssetamount = (BigDecimal)model.getValue("bef_assetamount", befIndex);
        final BigDecimal befOriginalval = (BigDecimal)model.getValue("bef_originalval", befIndex);
        final BigDecimal befAccumdepre = (BigDecimal)model.getValue("bef_accumdepre", befIndex);
        final BigDecimal befAddupyeardepre = (BigDecimal)model.getValue("bef_addupyeardepre", befIndex);
        final BigDecimal befDecval = (BigDecimal)model.getValue("bef_decval", befIndex);
        final BigDecimal befPreresidualval = (BigDecimal)model.getValue("bef_preresidualval", befIndex);
        final BigDecimal befNetworth = (BigDecimal)model.getValue("bef_networth", befIndex);
        final BigDecimal befNetamount = (BigDecimal)model.getValue("bef_netamount", befIndex);
        final BigDecimal befOriginalamount = (BigDecimal)model.getValue("bef_originalamount", befIndex);
        final BigDecimal befIncometax = (BigDecimal)model.getValue("bef_incometax", befIndex);
        final BigDecimal befMonthorigvalchg = (BigDecimal)model.getValue("bef_monthorigvalchg", befIndex);
        final BigDecimal befYearorigvalchg = (BigDecimal)model.getValue("bef_yearorigvalchg", befIndex);
        final BigDecimal befMonthdepre = (BigDecimal)model.getValue("bef_monthdepre", befIndex);
        final BigDecimal befMonthdeprechg = (BigDecimal)model.getValue("bef_monthdeprechg", befIndex);
        final BigDecimal befPartcleardepre = (BigDecimal)model.getValue("bef_partcleardepre", befIndex);
        final BigDecimal monDepre = finDy.getBigDecimal("monthdepre");
        final BigDecimal initBefAccumdepre = befAccumdepre;
        final BigDecimal initBefAddupyeardepre = befAddupyeardepre;
        final Boolean isQtySplit = model.getValue("splittype").toString().equalsIgnoreCase("A");
        int index = 0;
        for (final ScaleEntry currencyScale : currencyScales) {
            aftIndex = model.createNewEntryRow("subassetsplitentry");
            BigDecimal assetamount = BigDecimal.ZERO;
            BigDecimal originalval = BigDecimal.ZERO;
            BigDecimal accumdepre = BigDecimal.ZERO;
            BigDecimal addupyeardepre = BigDecimal.ZERO;
            BigDecimal decval = BigDecimal.ZERO;
            BigDecimal preresidualval = BigDecimal.ZERO;
            BigDecimal networth = BigDecimal.ZERO;
            BigDecimal netamount = BigDecimal.ZERO;
            BigDecimal originalamount = BigDecimal.ZERO;
            BigDecimal incometax = BigDecimal.ZERO;
            BigDecimal monthorigvalchg = BigDecimal.ZERO;
            BigDecimal yearorigvalchg = BigDecimal.ZERO;
            BigDecimal monthdepre = BigDecimal.ZERO;
            BigDecimal monthdeprechg = BigDecimal.ZERO;
            BigDecimal partclearepre = BigDecimal.ZERO;
            if (index == lastPos) {
                assetamount = befAssetamount.subtract(sumAssetamount);
                originalval = befOriginalval.subtract(sumOriginalval);
                accumdepre = initBefAccumdepre.subtract(sumAccumdepre);
                addupyeardepre = initBefAddupyeardepre.subtract(sumAddupyeardepre);
                decval = befDecval.subtract(sumDecval);
                preresidualval = befPreresidualval.subtract(sumPreresidualval);
                networth = befNetworth.subtract(sumNetworth);
                netamount = befNetamount.subtract(sumNetamount);
                originalamount = befOriginalamount.subtract(sumOriginalamount);
                incometax = befIncometax.subtract(sumIncometax);
                monthorigvalchg = befMonthorigvalchg.subtract(sumMonthorigvalchg);
                yearorigvalchg = befYearorigvalchg.subtract(sumYearorigvalchg);
                monthdepre = befMonthdepre.subtract(sumMonthdepre);
                monthdeprechg = befMonthdeprechg.subtract(sumMonthdeprechg);
                partclearepre = befPartcleardepre.subtract(sumPartcleardepre);
            }
            else {
                assetamount = this.getScaleValue(befAssetamount, unitScales.get(index));
                originalval = this.getScaleValue(befOriginalval, currencyScale);
                accumdepre = this.getScaleValue(initBefAccumdepre, currencyScale);
                addupyeardepre = this.getScaleValue(initBefAddupyeardepre, currencyScale);
                decval = this.getScaleValue(befDecval, currencyScale);
                preresidualval = this.getScaleValue(befPreresidualval, currencyScale);
                networth = this.getScaleValue(befNetworth, currencyScale);
                netamount = this.getScaleValue(befNetamount, currencyScale);
                originalamount = this.getScaleValue(befOriginalamount, currencyScale);
                incometax = this.getScaleValue(befIncometax, currencyScale);
                monthorigvalchg = this.getScaleValue(befMonthorigvalchg, currencyScale);
                yearorigvalchg = this.getScaleValue(befYearorigvalchg, currencyScale);
                monthdepre = this.getScaleValue(befMonthdepre, currencyScale);
                monthdeprechg = this.getScaleValue(befMonthdeprechg, currencyScale);
                partclearepre = this.getScaleValue(befPartcleardepre, currencyScale);
                sumAssetamount = sumAssetamount.add(assetamount);
                sumOriginalval = sumOriginalval.add(originalval);
                sumAccumdepre = sumAccumdepre.add(accumdepre);
                sumAddupyeardepre = sumAddupyeardepre.add(addupyeardepre);
                sumPreresidualval = sumPreresidualval.add(preresidualval);
                sumDecval = sumDecval.add(decval);
                sumNetworth = sumNetworth.add(networth);
                sumNetamount = sumNetamount.add(netamount);
                sumOriginalamount = sumOriginalamount.add(originalamount);
                sumIncometax = sumIncometax.add(incometax);
                sumMonthorigvalchg = sumMonthorigvalchg.add(monthorigvalchg);
                sumYearorigvalchg = sumYearorigvalchg.add(yearorigvalchg);
                sumMonthdepre = sumMonthdepre.add(monthdepre);
                sumMonthdeprechg = sumMonthdeprechg.add(monthdeprechg);
                sumPartcleardepre = sumPartcleardepre.add(partclearepre);
            }
            //拆分后卡片赋值
//            if (index == 0) {
                model.setValue("aft_cardbillno", (Object)realDy.getString("billno"), aftIndex, befIndex);
                model.setValue("aft_cardnumber", (Object)realDy.getString("number"), aftIndex, befIndex);
                model.setValue("aft_realcard", realDy.getPkValue(), aftIndex, befIndex); 
                Map<String, String> map = this.getPageCache().getAll();
                Iterator<String> iter = map.keySet().iterator();  // Set类型的key值集合，并转换为迭代器
                if("save".equals(type)) {                	
                	while(iter.hasNext()){                        
                		String key=(String) iter.next();
                		if(key != null && ("zsf_aft_realcard_"+index).equals(key)) {
                			
                			String value = map.get(key);
                			model.setValue("zsf_aft_realcard",value ,aftIndex,befIndex);         
                			break;
                		}
                	}
                }else {                	
                	model.setValue("zsf_aft_realcard",realDy.getString("assetname"),aftIndex,befIndex); 
                }
                model.setValue("aft_fincard", finDy.getPkValue(), aftIndex, befIndex);
//            }
            final Object unitPK = (unitDy == null) ? Integer.valueOf(0) : unitDy.getPkValue();
            model.setValue("aft_unit", unitPK, aftIndex, befIndex);
            if (isQtySplit) {
                if ("split".equals(type)) {
                    model.setValue("aft_assetamount", (Object)assetamount, aftIndex, befIndex);
                }
                else {
                    model.setValue("aft_assetamount", (Object)assetamountList.get(index), aftIndex, befIndex);
                }
            }
            else {
                model.setValue("aft_assetamount", (Object)befAssetamount, aftIndex, befIndex);
            }
            final Object baseCurrencyPK = (baseCurrencyDy == null) ? Integer.valueOf(0) : baseCurrencyDy.getPkValue();
            model.setValue("aft_basecurrency", baseCurrencyPK, aftIndex, befIndex);
            model.setValue("aft_originalval", (Object)originalval, aftIndex, befIndex);
            model.setValue("aft_accumdepre", (Object)accumdepre, aftIndex, befIndex);
            model.setValue("aft_addupyeardepre", (Object)addupyeardepre, aftIndex, befIndex);
            model.setValue("aft_decval", (Object)decval, aftIndex, befIndex);
            model.setValue("aft_preresidualval", (Object)preresidualval, aftIndex, befIndex);
            model.setValue("aft_networth", (Object)networth, aftIndex, befIndex);
            model.setValue("aft_netamount", (Object)netamount, aftIndex, befIndex);
            model.setValue("aft_originalamount", (Object)originalamount, aftIndex, befIndex);
            model.setValue("aft_incometax", (Object)incometax, aftIndex, befIndex);
            model.setValue("aft_monthdepre", (Object)BigDecimal.ZERO, aftIndex, befIndex);
            if (index == 0) {
                final BigDecimal curMonthorigvalchg = originalval.subtract(befOriginalval).add(befMonthorigvalchg);
                final BigDecimal curYearorigvalchg = originalval.subtract(befOriginalval).add(befYearorigvalchg);
                final BigDecimal curDecvalChg = decval.subtract(befDecval).add(befMonthdeprechg);
                model.setValue("aft_monthorigvalchg", (Object)curMonthorigvalchg, aftIndex, befIndex);
                model.setValue("aft_yearorigvalchg", (Object)curYearorigvalchg, aftIndex, befIndex);
                model.setValue("aft_monthdeprechg", (Object)curDecvalChg, aftIndex, befIndex);
            }
            else {
                final BigDecimal curMonthorigvalchg = originalval;
                final BigDecimal curYearorigvalchg = originalval;
                final BigDecimal curDecvalChg = decval;
                model.setValue("aft_monthorigvalchg", (Object)curMonthorigvalchg, aftIndex, befIndex);
                model.setValue("aft_yearorigvalchg", (Object)curYearorigvalchg, aftIndex, befIndex);
                model.setValue("aft_monthdeprechg", (Object)curDecvalChg, aftIndex, befIndex);
            }
            model.setValue("aft_partcleardepre", (Object)partclearepre, aftIndex, befIndex);
            ++index;
        }
    }
    
    private BigDecimal getScaleValue(final BigDecimal totalValue, final ScaleEntry scaleEntry) {
        if (totalValue.compareTo(BigDecimal.ZERO) == 0 || scaleEntry.totalValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalValue.multiply(scaleEntry.value).divide(scaleEntry.totalValue, scaleEntry.Precise, 4);
    }
    
    private void showSplitScaleSetForm() {
        final IDataModel model = this.getModel();
        final String splitType = model.getValue("splittype").toString();
        BigDecimal splitQty = BigDecimal.ZERO;
        final Object qty = model.getValue("splitqty");
        splitQty = new BigDecimal(qty.toString());
        final FormShowParameter showParameter = new FormShowParameter();
        showParameter.setFormId("fa_splitscale");
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        showParameter.setCustomParam("splittype", (Object)splitType);
        showParameter.setCustomParam("splitqty", (Object)splitQty);
        final CloseCallBack closeCallBack = new CloseCallBack((IFormPlugin)this, "callback_splitscale");
        showParameter.setCloseCallBack(closeCallBack);
        this.getView().showForm(showParameter);
    }
    
    private void callBackSplitScaleSet(final ClosedCallBackEvent closedCallBackEvent) {
        final IDataModel model = this.getModel();
        if (closedCallBackEvent.getReturnData() != null) {
            final String splitScaleStr = (String)closedCallBackEvent.getReturnData();
            if (StringUtils.isBlank(splitScaleStr)) {
                return;
            }
            final String[] scaleArr = splitScaleStr.split(":");
            final List<BigDecimal> scaleList = Arrays.asList(scaleArr).stream().map(str -> new BigDecimal(str)).collect(Collectors.toList());
            this.createSplitCards(scaleList);
            final String splitType = model.getDataEntity().getString("splittype");
            this.splitTypePropertyChanged(splitType);
        }
    }
    
    private List<ScaleEntry> getScaleDataByList(final List<BigDecimal> scaleList, final int precise) {
        final BigDecimal totalDecimal = scaleList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        final List<ScaleEntry> scaleEntrys = new ArrayList<ScaleEntry>(scaleList.size());
        for (final BigDecimal scale : scaleList) {
            final ScaleEntry scaleEntry = new ScaleEntry();
            scaleEntry.value = scale;
            scaleEntry.totalValue = totalDecimal;
            scaleEntry.Precise = precise;
            scaleEntrys.add(scaleEntry);
        }
        return scaleEntrys;
    }
    
    BigDecimal getPartClearDepre(final Long finId) {
        BigDecimal result = BigDecimal.ZERO;
        final long periodId = (long)this.getModel().getValue("splitperiod_id");
        final QFilter statusFilter = new QFilter("billstatus", "=", (Object)"C");
        final QFilter periodFilter = new QFilter("clearperiod", "=", (Object)periodId);
        final QFilter finFilter = new QFilter(Fa.dot(new String[] { "detail_entry", "fincard" }), "=", (Object)finId);
        final QFilter isClearAllFilter = new QFilter(Fa.dot(new String[] { "detail_entry", "isclearall" }), "!=", (Object)"1");
        final String selectFields = Fa.dot(new String[] { "detail_entry", "addupdepre" }) + " " + "addupdepre";
        final DynamicObjectCollection itemDys = QueryServiceHelper.query("fa_clearbill", selectFields, new QFilter[] { statusFilter, periodFilter, finFilter, isClearAllFilter });
        if (itemDys != null && itemDys.size() > 0) {
            result = itemDys.stream().map(dy -> dy.getBigDecimal("addupdepre")).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return result;
    }
    
    private Map<String, List<FieldEntry>> getSplitFields() {
        final HashMap<String, List<FieldEntry>> result = new HashMap<String, List<FieldEntry>>();
        final List<FieldEntry> realFieldEntrys = new ArrayList<FieldEntry>();
        realFieldEntrys.add(this.getFieldEntry("assetamount", ResManager.loadKDString("\u8d44\u4ea7", "FaSplitCardBillEditPlugin_11", "fi-fa-formplugin", new Object[0]), "fa_card_real"));
        final List<FieldEntry> finFieldEntrys = new ArrayList<FieldEntry>();
        finFieldEntrys.add(this.getFieldEntry("originalval", ResManager.loadKDString("\u539f\u503c", "FaSplitCardBillEditPlugin_12", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("accumdepre", ResManager.loadKDString("\u7d2f\u8ba1\u6298\u65e7", "FaSplitCardBillEditPlugin_13", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("addupyeardepre", ResManager.loadKDString("\u672c\u5e74\u7d2f\u8ba1\u6298\u65e7", "FaSplitCardBillEditPlugin_14", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("decval", ResManager.loadKDString("\u51cf\u503c\u51c6\u5907", "FaSplitCardBillEditPlugin_15", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("preresidualval", ResManager.loadKDString("\u51c0\u6b8b\u503c", "FaSplitCardBillEditPlugin_16", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("networth", ResManager.loadKDString("\u51c0\u503c", "FaSplitCardBillEditPlugin_17", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("netamount", ResManager.loadKDString("\u51c0\u989d", "FaSplitCardBillEditPlugin_18", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("originalamount", ResManager.loadKDString("\u539f\u5e01\u91d1\u989d", "FaSplitCardBillEditPlugin_19", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("incometax", ResManager.loadKDString("\u8fdb\u9879\u7a0e\u989d", "FaSplitCardBillEditPlugin_20", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("monthorigvalchg", ResManager.loadKDString("\u672c\u671f\u539f\u503c\u53d8\u52a8", "FaSplitCardBillEditPlugin_21", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("yearorigvalchg", ResManager.loadKDString("\u672c\u5e74\u539f\u503c\u53d8\u52a8", "FaSplitCardBillEditPlugin_22", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("monthdepre", ResManager.loadKDString("\u672c\u671f\u6298\u65e7", "FaSplitCardBillEditPlugin_23", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        finFieldEntrys.add(this.getFieldEntry("monthdeprechg", ResManager.loadKDString("\u672c\u671f\u51cf\u503c\u53d8\u52a8", "FaSplitCardBillEditPlugin_24", "fi-fa-formplugin", new Object[0]), "fa_card_fin"));
        result.put("fa_card_real", realFieldEntrys);
        result.put("fa_card_fin", finFieldEntrys);
        return result;
    }
    
    FieldEntry getFieldEntry(final String filed, final String filedName, final String formMeta) {
        final FieldEntry fieldEntry = new FieldEntry();
        fieldEntry.filed = filed;
        fieldEntry.filedName = filedName;
        fieldEntry.formMeta = formMeta;
        return fieldEntry;
    }
}

