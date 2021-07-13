package kd.bos.asset.depreSplitPlugin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.fi.fa.formplugin.DSField;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.GetEntityTypeEventArgs;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.property.EntryProp;
import kd.bos.form.FormShowParameter;
import kd.bos.form.IClientViewProxy;
import kd.bos.form.control.Control;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListFilterParameter;
import kd.bos.list.ListShowParameter;
import kd.bos.login.utils.StringUtils;
import kd.bos.metadata.dao.MetaCategory;
import kd.bos.metadata.dao.MetadataDao;
import kd.bos.metadata.entity.EntityMetadata;
import kd.bos.metadata.entity.businessfield.BasedataField;
import kd.bos.metadata.entity.businessfield.BasedataPropField;
import kd.bos.metadata.entity.commonfield.Field;
import kd.bos.metadata.form.FormMetadata;
import kd.bos.metadata.form.control.EntryFieldAp;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.fi.fa.business.BizStatusEnum;
import kd.fi.fa.business.utils.FaPermissionUtils;
import kd.fi.fa.formplugin.AbstractDepreSplitSetUpForm;

public class DepreSplitSetUpAddNew extends AbstractDepreSplitSetUpForm
{
    private static final String CURRENT_PERIOD = "currentPeriod";
    private static final String KEY_ENTRYENTITY = "entryentity";
    
    public void afterBindData(final EventObject e) {
        super.afterBindData(e);
    }
    
    public void afterCreateNewData(final EventObject e) {
        final FormShowParameter showParam = this.getView().getFormShowParameter();
        final String createOrgID = (String)showParam.getCustomParam("createorgid");
        final IDataModel m = this.getModel();
        long orgid = 0L;
        if (StringUtils.isNotEmpty(createOrgID)) {
            orgid = Long.parseLong(createOrgID);
            m.setValue("org", (Object)orgid);
            showParam.setCustomParam("createorgid", (Object)null);
        }
        else {
            final DynamicObject orgDo = (DynamicObject)m.getValue("org");
            if (orgDo != null) {
                orgid = orgDo.getLong("id");
            }
        }
        if (orgid != 0L) {
            final DynamicObject book = BusinessDataServiceHelper.loadSingle("fa_assetbook", "id,depreuse.id,curperiod.id,curperiod.number", new QFilter[] { new QFilter("org", "=", (Object)orgid), new QFilter("ismainbook", "=", (Object)Boolean.TRUE) });
            if (book != null) {
                m.setValue("depreuse", (Object)book.getLong("depreuse.id"));
                m.setValue("beginperiod", (Object)book.getLong("curperiod.id"));
                this.getPageCache().put("currentPeriod", book.getString("curperiod.number"));
            }
        }
    }
    
    public void afterDoOperation(final AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        final OperationResult result = afterDoOperationEventArgs.getOperationResult();
        if (afterDoOperationEventArgs.getOperateKey().equals("donothing")) {
            if (result.isSuccess()) {
                this.getModel().setDataChanged(false);
                this.getView().close();
            }
            else {
                this.getView().showOperationResult(result);
            }
        }
    }
    
    public void registerListener(final EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(new String[] { "toolbarap" });
        final BasedataEdit beginperiod = (BasedataEdit)this.getControl("beginperiod");
        beginperiod.addBeforeF7SelectListener((BeforeF7SelectListener)this);
        final BasedataEdit org = (BasedataEdit)this.getControl("org");
        org.addBeforeF7SelectListener((BeforeF7SelectListener)this);
        final BasedataEdit asstype = (BasedataEdit)this.getControl("asstype");
        asstype.addBeforeF7SelectListener((BeforeF7SelectListener)this);
        final BasedataEdit realcard = (BasedataEdit)this.getControl("realcard");
        realcard.addBeforeF7SelectListener((BeforeF7SelectListener)this);
        final BasedataEdit depreuse = (BasedataEdit)this.getControl("depreuse");
        depreuse.addBeforeF7SelectListener((BeforeF7SelectListener)this);
    }
    
    public void itemClick(final ItemClickEvent evt) {
        super.itemClick(evt);
        final String key = evt.getItemKey();
        if ("exit".equalsIgnoreCase(key)) {
            this.getView().close();
        }
    }
    
    public void propertyChanged(final PropertyChangedArgs e) {
        super.propertyChanged(e);
        final IDataModel model = this.getModel();
        final ChangeData[] data = e.getChangeSet();
        final IDataEntityProperty property = e.getProperty();
        final String pkey = property.getName();
        if ("asstype".equalsIgnoreCase(pkey)) {
            final DynamicObjectCollection newData = (DynamicObjectCollection)data[0].getNewValue();
            final List<kd.fi.fa.formplugin.DSField> fields = new ArrayList<DSField>();
            int i = 0;
            int j = 0;
            for (final DynamicObject o : newData) {
                final DynamicObject entityObject = (DynamicObject)o.get("fbasedataid");
                final String fbdtype = entityObject.getString("valuetype");
                final DSField field = new DSField();
                field.setDataType(fbdtype);
                if ("1".equals(fbdtype)) {
                    field.setEntityId(entityObject.getString("valuesource.number"));
                    field.setFieldName(new LocaleString(entityObject.getString("valuesource.name")));
                    field.setFieldKey("basedata" + i);
                    ++i;
                }
                else if ("2".equals(fbdtype)) {
                    field.setEntityId(entityObject.getString("assistanttype.id"));
                    field.setFieldName(new LocaleString(entityObject.getString("assistanttype.name")));
                    field.setFieldKey("assistant" + j);
                    ++j;
                }
                fields.add(field);
            }
            final String dsfieldstr = this.getPageCache().get("DSFields");
            if (StringUtils.isNotEmpty(dsfieldstr)) {
                final List<?> OldFields = (List<?>)SerializationUtils.fromJsonStringToList(dsfieldstr, (Class)DSField.class);
                final DynamicObjectCollection entries = model.getEntryEntity("entryentity");
                final int size = entries.size();
                for (final Object field2 : OldFields) {
                    final DSField oldDs = (DSField)field2;
                    boolean stay = false;
                    for (final DSField newds : fields) {
                        if (newds.getEntityId().equalsIgnoreCase(oldDs.getEntityId()) && newds.getFieldKey().equalsIgnoreCase(oldDs.getFieldKey())) {
                            stay = true;
                        }
                    }
                    if (!stay) {
                        for (int m = 0; m < size; ++m) {
                            model.setValue(oldDs.getFieldKey(), (Object)null, m);
                        }
                    }
                }
            }
            this.getPageCache().put("DSFields", SerializationUtils.toJsonString((Object)fields));
            this.saveFactorDataToModel(fields);
            this.reBuildModelAndGrid(fields, model.getDataEntityType(), true);
        }
        else if ("org".equalsIgnoreCase(pkey)) {
            final DynamicObject newOrg = (DynamicObject)data[0].getNewValue();
            if (newOrg != null) {
                final DynamicObject book = BusinessDataServiceHelper.loadSingle("fa_assetbook", "id,depreuse.id,curperiod.id,curperiod.number", new QFilter[] { new QFilter("org", "=", newOrg.getPkValue()), new QFilter("ismainbook", "=", (Object)Boolean.TRUE) });
                if (book != null) {
                    model.setValue("depreuse", (Object)book.getLong("depreuse.id"));
                    model.setValue("beginperiod", (Object)book.getLong("curperiod.id"));
                    this.getPageCache().put("currentPeriod", book.getString("curperiod.number"));
                }
                model.setValue("asstype", (Object)null);
                model.deleteEntryData("entryentity");
            }
        }
        else if ("depreuse".equalsIgnoreCase(pkey)) {
            final DynamicObject newUse = (DynamicObject)data[0].getNewValue();
            final DynamicObject org = (DynamicObject)model.getValue("org");
            if (newUse != null && org != null) {
                final DynamicObject book2 = BusinessDataServiceHelper.loadSingle("fa_assetbook", "id,depreuse.id,curperiod.id,curperiod.number", new QFilter[] { new QFilter("org", "=", org.getPkValue()), new QFilter("depreuse", "=", newUse.getPkValue()) });
                if (book2 != null) {
                    model.setValue("beginperiod", (Object)book2.getLong("curperiod.id"));
                    this.getPageCache().put("currentPeriod", book2.getString("curperiod.number"));
                }
            }
        }
        else if ("percent".equalsIgnoreCase(pkey)) {
            final BigDecimal percent = (BigDecimal)data[0].getNewValue();
            BigDecimal total = BigDecimal.ZERO;
            final DynamicObject rowData = data[0].getDataEntity();
            if (rowData != null) {
                final DynamicObject realCard = rowData.getDynamicObject("realcard");
                if (realCard != null) {
                    final Long id = realCard.getLong("id");
                    final int currIndex = this.getModel().getEntryCurrentRowIndex("entryentity");
                    final DynamicObjectCollection entrys = this.getModel().getEntryEntity("entryentity");
                    DynamicObject each = null;
                    DynamicObject eachCard = null;
                    for (int k = 0; k < entrys.size(); ++k) {
                        each = (DynamicObject)entrys.get(k);
                        eachCard = each.getDynamicObject("realcard");
                        if (eachCard != null && id.equals(eachCard.getLong("id"))) {
                            if (currIndex == k) {
                                total = total.add(percent);
                            }
                            else {
                                total = total.add(each.getBigDecimal("percent"));
                            }
                        }
                    }
                }
            }
            if (total.compareTo(new BigDecimal("100")) < 0) {
                this.getModel().beginInit();
                final DynamicObject entryObj = model.getEntryRowEntity("entryentity", data[0].getRowIndex());
                final EntryProp entryp = (EntryProp)model.getDataEntityType().getProperty("entryentity");
                final DynamicObjectCollection coll = new DynamicObjectCollection();
                final DynamicObject newObj = ORM.create().newDynamicObject(entryObj.getDynamicObjectType());
                newObj.set("realcard", entryObj.get("realcard"));
                newObj.set("orgduty", entryObj.get("orgduty"));
                newObj.set("percent", (Object)new BigDecimal(100).subtract(total));
                coll.add((DynamicObject)newObj);
                model.batchInsertEntryRow(entryp, data[0].getRowIndex() + 1, coll);
                model.setValue("percent", (Object)new BigDecimal(100).subtract(total), data[0].getRowIndex() + 1);
                this.getModel().endInit();
                this.getView().updateView("entryentity");
            }
        }
        else if ("realcard".equalsIgnoreCase(pkey)) {
            final String costCenterKey = this.getCostCenterKey();
            if (costCenterKey == null) {
                final DynamicObject realcard = (DynamicObject)data[0].getNewValue();
                if (realcard == null) {
                    return;
                }
                final DynamicObject useDept = realcard.getDynamicObject("headusedept");
                if (useDept != null) {
                    model.setValue("orgduty", (Object)this.getOrgDutyID(useDept.getLong("id")), data[0].getRowIndex());
                }
            }
            model.setValue("percent", (Object)100, data[0].getRowIndex());
        }
        else {
            final String costCenterKey = this.getCostCenterKey();
            if (pkey.equalsIgnoreCase(costCenterKey)) {
                final DynamicObject costCenter = (DynamicObject)data[0].getNewValue();
                if (costCenter != null) {
                    final DynamicObject orgduty = costCenter.getDynamicObject("orgduty");
                    model.setValue("orgduty", (Object)orgduty, data[0].getRowIndex());
                }
            }
        }
    }
    
    private Long getOrgDutyID(final Object orgid) {
        final String selectFields = "id,orgduty.number number,orgduty.id dutyid";
        final QFilter groupFilter = new QFilter("orgduty.group", "=", (Object)1);
        final QFilter orgFilter = new QFilter("org", "=", orgid);
        final QFilter[] filters = { groupFilter, orgFilter };
        final DynamicObject org = QueryServiceHelper.queryOne("bos_org_dutyrelation", selectFields, filters);
        if (org != null) {
            return org.getLong("dutyid");
        }
        return Long.valueOf("0");
    }
    
    private void saveFactorDataToModel(final List<DSField> refList) {
        final IDataModel model = this.getModel();
        model.deleteEntryData("fieldmapentry");
        for (final DSField field : refList) {
            final int rowIndex = model.createNewEntryRow("fieldmapentry");
            model.setValue("datatype", (Object)field.getDataType(), rowIndex);
            model.setValue("entityid", (Object)field.getEntityId(), rowIndex);
            model.setValue("fieldname", (Object)field.getFieldName(), rowIndex);
            model.setValue("fieldkey", (Object)field.getFieldKey(), rowIndex);
        }
    }
    
    public void getEntityType(final GetEntityTypeEventArgs e) {
        if (this.getPageCache().get("DSFields") != null) {
            final List<DSField> list = (List<DSField>)SerializationUtils.fromJsonStringToList(this.getPageCache().get("DSFields"), (Class)DSField.class);
            final MainEntityType entitytype = e.getOriginalEntityType();
            this.reBuildModelAndGrid(list, entitytype, false);
            e.setNewEntityType(entitytype);
        }
    }
    
    private void reBuildModelAndGrid(final List<kd.fi.fa.formplugin.DSField> fields, final MainEntityType mainEntityType, final boolean rebuildGrid) {
        final EntryProp entry = (EntryProp)mainEntityType.getProperty("entryentity");
        this.setFactorColumnMeta(fields, mainEntityType, entry);
        this.drawGrid(fields, mainEntityType, rebuildGrid);
    }
    
    private void drawGrid(final List<DSField> refList, final MainEntityType mainEntityType, final boolean rebuildGrid) {
        final String entityId = MetadataDao.getIdByNumber("fa_depresplitaddnew", MetaCategory.Entity);
        final FormMetadata formmeta = (FormMetadata)MetadataDao.readRuntimeMeta(entityId, MetaCategory.Form);
        final EntityMetadata entitymeta = (EntityMetadata)MetadataDao.readRuntimeMeta(entityId, MetaCategory.Entity);
        formmeta.bindEntityMetadata(entitymeta);
        final List<Map<String, Object>> cols = new ArrayList<Map<String, Object>>();
        if (rebuildGrid) {
            cols.add(this.createSeqColumn());
            cols.add(this.createRealCardColumn());
            cols.add(this.createRealCardPropColumn(entitymeta, "assetname", "cardname", ResManager.loadKDString("\u8d44\u4ea7\u540d\u79f0", "DepreSplitSetUpAddNew_2", "fi-fa-formplugin", new Object[0]), "200"));
            cols.add(this.createRealCardPropColumn(entitymeta, "headusedept.name", "carddept", ResManager.loadKDString("\u4f7f\u7528\u90e8\u95e8", "DepreSplitSetUpAddNew_3", "fi-fa-formplugin", new Object[0]), "200"));
            cols.add(this.createRealCardPropColumn(entitymeta, "headuseperson.name", "carduser", ResManager.loadKDString("\u4f7f\u7528\u4eba", "DepreSplitSetUpAddNew_4", "fi-fa-formplugin", new Object[0]), "100"));
            cols.add(this.createRealCardPropColumn(entitymeta, "storeplace.name", "cardaddress", ResManager.loadKDString("\u5b58\u653e\u5730\u70b9", "DepreSplitSetUpAddNew_5", "fi-fa-formplugin", new Object[0]), "200"));
            cols.add(this.createDeptUseColumn());
        }
        if (refList != null) {
            for (final DSField ref : refList) {
                final Map<String, Object> col = this.createBaseDataColumn(ref);
                cols.add(col);
            }
        }
        if (rebuildGrid) {
            final Map<String, Object> assCol = this.createPercentColumn(entitymeta);
            if (assCol != null) {
                cols.add(assCol);
            }
            final Map<String, Object> meta = new HashMap<String, Object>();
            meta.put("rk", "rk");
            meta.put("seq", "fseq");
            meta.put("columns", cols);
            final IClientViewProxy clientViewProxy = (IClientViewProxy)this.getView().getService((Class)IClientViewProxy.class);
            clientViewProxy.preInvokeControlMethod("entryentity", "createGridColumns", new Object[] { meta });
        }
    }
    
    private Map<String, Object> createRealCardPropColumn(final EntityMetadata entitymeta, final String propName, final String propKey, final String columnName, final String width) {
        final EntryFieldAp f7Field = new EntryFieldAp();
        f7Field.setKey(propKey);
        f7Field.setName(new LocaleString(columnName));
        f7Field.setFieldTextAlign("center");
        f7Field.setTextAlign("center");
        f7Field.setAlignSelf("center");
        f7Field.setWidth(new LocaleString(width));
        f7Field.setLock("");
        final BasedataPropField basedata = new BasedataPropField();
        basedata.setEntityMetadata(entitymeta);
        basedata.setRefBaseFieldId("realcard");
        basedata.setRefDisplayProp(propName);
        basedata.setKey(propKey);
        f7Field.setField((Field)basedata);
        return (Map<String, Object>) f7Field.createColumns().get(0);
    }
    
    private Map<String, Object> createRealCardColumn() {
        final EntryFieldAp f7Field = new EntryFieldAp();
        f7Field.setKey("realcard");
        f7Field.setName(new LocaleString(ResManager.loadKDString("\u5361\u7247\u7f16\u53f7", "DepreSplitSetUpAddNew_6", "fi-fa-formplugin", new Object[0])));
        f7Field.setFieldTextAlign("center");
        f7Field.setTextAlign("center");
        f7Field.setAlignSelf("center");
        f7Field.setWidth(new LocaleString("200"));
        f7Field.setLock("");
        f7Field.setDisplayFormatString("number");
        final BasedataField basedata = new BasedataField();
        basedata.setBaseEntityId("fa_card_real_base");
        basedata.setKey("realcard");
        basedata.setDisplayProp("number");
        basedata.setNumberProp("number");
        f7Field.setField((Field)basedata);
        return (Map<String, Object>) f7Field.createColumns().get(0);
    }
    
    public void beforeF7Select(final BeforeF7SelectEvent arg0) {
        final Object source = arg0.getSource();
        final Control sourceCtl = (Control)source;
        final String key = sourceCtl.getKey();
        final IDataModel model = this.getModel();
        if ("beginperiod".equals(key)) {
            final List<QFilter> qFilters = new ArrayList<QFilter>();
            final DynamicObject org = (DynamicObject)model.getValue("org");
            if (org == null) {
                this.getView().showMessage(ResManager.loadKDString("\u8bf7\u9009\u62e9\u6838\u7b97\u7ec4\u7ec7\uff01", "DepreSplitSetUpAddNew_7", "fi-fa-formplugin", new Object[0]));
                arg0.setCancel(true);
                return;
            }
            final DynamicObject depreuse = (DynamicObject)model.getValue("depreuse");
            if (depreuse == null) {
                this.getView().showMessage(ResManager.loadKDString("\u8bf7\u9009\u62e9\u6298\u65e7\u7528\u9014\uff01", "DepreSplitSetUpAddNew_8", "fi-fa-formplugin", new Object[0]));
                arg0.setCancel(true);
                return;
            }
            final DynamicObject book = BusinessDataServiceHelper.loadSingle("fa_assetbook", "id,depreuse.id,depresystem.id,depresystem.periodtype", new QFilter[] { new QFilter("org", "=", org.getPkValue()), new QFilter("depreuse", "=", depreuse.getPkValue()) });
            if (book != null) {
                qFilters.add(new QFilter("periodtype", "=", (Object)book.getLong("depresystem.periodtype.id")));
            }
            final String currentPeriodNumber = this.getPageCache().get("currentPeriod");
            if (currentPeriodNumber != null) {
                qFilters.add(new QFilter("number", ">=", (Object)currentPeriodNumber));
            }
            final ListShowParameter showParameter = (ListShowParameter)arg0.getFormShowParameter();
            final ListFilterParameter filterParam = showParameter.getListFilterParameter();
            filterParam.getQFilters().addAll(qFilters);
        }
        else if ("org".equals(key)) {
            final List<QFilter> qFilters = new ArrayList<QFilter>();
            qFilters.add(new QFilter("id", "in", (Object)FaPermissionUtils.getAllEnableBookAndPermissionOrgs("fa_depresplitsetup", "47156aff000000ac")));
            final ListShowParameter showParameter2 = (ListShowParameter)arg0.getFormShowParameter();
            final ListFilterParameter filterParam2 = showParameter2.getListFilterParameter();
            filterParam2.getQFilters().addAll(qFilters);
        }
        else if ("asstype".equals(key)) {
            final DynamicObject org2 = (DynamicObject)model.getValue("org");
            if (org2 == null) {
                this.getView().showMessage(ResManager.loadKDString("\u8bf7\u9009\u62e9\u6838\u7b97\u7ec4\u7ec7\uff01", "DepreSplitSetUpAddNew_7", "fi-fa-formplugin", new Object[0]));
                arg0.setCancel(true);
                return;
            }
            final DynamicObject depreuse2 = (DynamicObject)model.getValue("depreuse");
            if (depreuse2 == null) {
                this.getView().showMessage(ResManager.loadKDString("\u8bf7\u9009\u62e9\u6298\u65e7\u7528\u9014\uff01", "DepreSplitSetUpAddNew_8", "fi-fa-formplugin", new Object[0]));
                arg0.setCancel(true);
                return;
            }
            final List<QFilter> qFilters2 = this.getAssTypeFilter(org2.getPkValue(), depreuse2.getPkValue());
            final ListShowParameter showParameter3 = (ListShowParameter)arg0.getFormShowParameter();
            final ListFilterParameter filterParam3 = showParameter3.getListFilterParameter();
            filterParam3.getQFilters().addAll(qFilters2);
        }
        else if ("realcard".equals(key)) {
            final List<QFilter> qFilters = new ArrayList<QFilter>();
            qFilters.add(new QFilter("org", "=", ((DynamicObject)model.getValue("org")).getPkValue()));
            qFilters.add(new QFilter("bizstatus", "!=", (Object)BizStatusEnum.DELETE));
            qFilters.add(new QFilter("isbak", "=", (Object)false));
            final ListShowParameter showParameter2 = (ListShowParameter)arg0.getFormShowParameter();
            final ListFilterParameter filterParam2 = showParameter2.getListFilterParameter();
            filterParam2.getQFilters().addAll(qFilters);
        }
        else if ("depreuse".equals(key)) {
            final DynamicObject org2 = (DynamicObject)model.getValue("org");
            if (org2 == null) {
                this.getView().showMessage(ResManager.loadKDString("\u8bf7\u9009\u62e9\u6838\u7b97\u7ec4\u7ec7\uff01", "DepreSplitSetUpAddNew_7", "fi-fa-formplugin", new Object[0]));
                arg0.setCancel(true);
                return;
            }
            final List<Long> ids = (List<Long>)DB.query(DBRoute.of("fa"), "select fdepreuse from t_fa_assetbook where FORGID = ?", new Object[] { org2.getPkValue() }, e -> {
                final List<Long> depreUseIds = new ArrayList<Long>(e.getRow());
                while (e.next()) {
                    depreUseIds.add(e.getLong("fdepreuse"));
                }
                return depreUseIds;
            });
            final List<QFilter> qFilters2 = new ArrayList<QFilter>();
            qFilters2.add(new QFilter("id", "in", (Object)ids));
            final ListShowParameter showParameter3 = (ListShowParameter)arg0.getFormShowParameter();
            final ListFilterParameter filterParam3 = showParameter3.getListFilterParameter();
            filterParam3.getQFilters().addAll(qFilters2);
        }
        if ("bos_costcenter".equals(this.getPageCache().get(key))) {
            final List<QFilter> qFilters = new ArrayList<QFilter>();
            qFilters.add(new QFilter("enable", "=", (Object)"1"));
            qFilters.add(new QFilter("accountorg", "=", ((DynamicObject)model.getValue("org")).getPkValue()));
            final ListShowParameter showParameter2 = (ListShowParameter)arg0.getFormShowParameter();
            final ListFilterParameter filterParam2 = showParameter2.getListFilterParameter();
            filterParam2.getQFilters().addAll(qFilters);
        }
    }
}

