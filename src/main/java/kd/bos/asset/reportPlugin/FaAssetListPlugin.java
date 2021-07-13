package kd.bos.asset.reportPlugin;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import kd.bos.base.BaseShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.AppMetadataCache;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.report.FilterInfo;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.form.IFormView;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickEvent;
import kd.bos.form.events.HyperLinkClickListener;
import kd.bos.form.field.BasedataEdit;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.report.ReportList;
import kd.bos.report.filter.ReportFilter;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.fi.fa.business.utils.FaPeriodUtils;
import kd.fi.fa.common.util.ContextUtil;
import kd.fi.fa.common.util.PermissonType;
import kd.fi.fa.report.constants.RptAssetList;
import kd.fi.fa.report.constants.RptDepreciation;
import kd.fi.fa.report.util.FaReportUtils;


public class FaAssetListPlugin extends AbstractReportFormPlugin implements HyperLinkClickListener {
    private static final Log logger = LogFactory.getLog(FaAssetListPlugin.class);
    private static String showFilterFields;

    private static /* synthetic */ void lambda$registerListener$1(kd.bos.form.field.events.BeforeF7SelectEvent r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-custom' in method: kd.fi.fa.report.formplugin.FaAssetListPlugin.lambda$registerListener$1(kd.bos.form.field.events.BeforeF7SelectEvent):void, dex: 
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:115)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
	at jadx.core.ProcessClass.process(ProcessClass.java:31)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JavaClass.getCode(JavaClass.java:48)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-custom'
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:590)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:101)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: kd.fi.fa.report.formplugin.FaAssetListPlugin.lambda$registerListener$1(kd.bos.form.field.events.BeforeF7SelectEvent):void");
    }

    public void registerListener(java.util.EventObject r1) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception: Unknown instruction: 'invoke-custom' in method: kd.fi.fa.report.formplugin.FaAssetListPlugin.registerListener(java.util.EventObject):void, dex: 
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:115)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
	at jadx.core.ProcessClass.process(ProcessClass.java:31)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:282)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JavaClass.getCode(JavaClass.java:48)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: 'invoke-custom'
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:590)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:101)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
//        throw new UnsupportedOperationException("Method not decompiled: kd.fi.fa.report.formplugin.FaAssetListPlugin.registerListener(java.util.EventObject):void");
    }

    public void initialize() {
        showFilterFields = ((ReportFilter) getView().getControl("reportfilterap")).getShowFilterFields();
    }

    public void afterQuery(ReportQueryParam queryParam) {
        ReportFilter rf = (ReportFilter) getView().getControl("reportfilterap");
        if (queryParam.getFilter().getBoolean(RptAssetList.SELECT_INIT_CARD)) {
            rf.setShowFilterFields(showFilterFields.replace(",\"q_period\"", ""));
        } else {
            rf.setShowFilterFields(showFilterFields);
        }
    }

    private static /* synthetic */ boolean lambda$null$0(QFilter f) {
        return !"id = -1".equals(f.toString());
    }

    public void afterCreateNewData(EventObject e) {
        Long curLoginOrg = Long.valueOf(RequestContext.get().getOrgId());
        HasPermOrgResult permOrgRs = PermissionServiceHelper.getAllPermOrgs(ContextUtil.getUserId(), AppMetadataCache.getAppInfo("fa").getId(), getView().getEntityId(), PermissonType.VIEW.getPermId());
        List<Long> orgIds = permOrgRs.getHasPermOrgs();
        Set<Long> ids = new HashSet();
        BasedataEdit orgBd = (BasedataEdit) getControl("q_org");
        List<QFilter> list = new ArrayList();
        boolean isShowDepre = false;
        if (!orgIds.isEmpty() || permOrgRs.hasAllOrgPerm()) {
            if (permOrgRs.hasAllOrgPerm()) {
                for (DynamicObject assetBook : BusinessDataServiceHelper.load("fa_assetbook", "org,startperiod,curperiod", null)) {
                    if (!(assetBook.getDynamicObject("org") == null || assetBook.getDynamicObject("startperiod") == null || assetBook.getDynamicObject("curperiod") == null)) {
                        ids.add((Long) assetBook.getDynamicObject("org").getPkValue());
                    }
                }
                if (ids.size() == 0) {
                    getView().showTipNotification(ResManager.loadKDString("未找到有折旧用途的核算组织", "FaAssetListPlugin_1", "fi-fa-report", new Object[0]));
                }
            } else {
                for (DynamicObject assetBook2 : BusinessDataServiceHelper.load("fa_assetbook", "id,org,startperiod,depreuse,curperiod", new QFilter[]{new QFilter("org", "in", orgIds)})) {
                    if (!(assetBook2.getDynamicObject("depreuse") == null || assetBook2.getDynamicObject("startperiod") == null || assetBook2.getDynamicObject("curperiod") == null)) {
                        ids.add((Long) assetBook2.getDynamicObject("org").getPkValue());
                    }
                }
            }
            if (ids.size() == 0) {
                getView().showTipNotification(ResManager.loadKDString("未找到有折旧用途的核算组织", "FaAssetListPlugin_1", "fi-fa-report", new Object[0]));
                return;
            }
            list.add(new QFilter("id", "in", ids));
            orgBd.setQFilters(list);
            if (!ids.contains(curLoginOrg)) {
                curLoginOrg = (Long) new ArrayList(ids).get(0);
            }
            getModel().setValue("q_org", new Long[]{curLoginOrg});
            DynamicObject[] assetBook3 = BusinessDataServiceHelper.load("fa_assetbook", "id,org,startperiod,depreuse,curperiod,periodtype", new QFilter[]{new QFilter("org", "in", curLoginOrg)});
            if (assetBook3.length > 1) {
                isShowDepre = true;
            }
            getModel().setValue("depreuse", assetBook3[0].getDynamicObject("depreuse").getPkValue());
            getView().setVisible(Boolean.valueOf(isShowDepre), new String[]{"depreuse"});
            changeFilterPanel(assetBook3, true);
            return;
        }
        list.add(new QFilter("id", "in", new ArrayList()));
        orgBd.setQFilters(list);
        getView().showTipNotification(ResManager.loadKDString("未找到有权限的核算组织", "FaAssetListPlugin_0", "fi-fa-report", new Object[0]));
    }

    private void changeFilterPanel(DynamicObject[] assetbooks, boolean isHideDepre) {
        IDataModel model = getModel();
        if (assetbooks == null || assetbooks.length <= 0) {
            getView().showTipNotification(ResManager.loadKDString("该资产账簿不存在!", "FaAssetListPlugin_2", "fi-fa-report", new Object[0]));
            return;
        }
        BasedataEdit q_peroid = (BasedataEdit) getControl("q_period");
        BasedataEdit q_Depre = (BasedataEdit) getControl("depreuse");
        BasedataEdit q_peroidType = (BasedataEdit) getControl("periodtype");
        long mixPeriod = 0;
        long compareMixPeriod = 0;
        long maxPeriod = 99999999;
        DynamicObject compareMaxAssetBook = null;
        Map<String, DynamicObject> regDepreuseMap = new HashMap();
        Set<Long> depres = new HashSet(6);
        Set<Long> hashSet = new HashSet(20);
        hashSet = new HashSet(assetbooks.length + 1);
        long periodTypeIdTemp = ((Long) assetbooks[0].getDynamicObject("periodtype").getPkValue()).longValue();
        hashSet.add(Long.valueOf(periodTypeIdTemp));
        String key;
        for (DynamicObject assetBook : assetbooks) {
            hashSet.add(Long.valueOf(assetBook.getDynamicObject("org").getLong("id")));
            DynamicObject assetPeriodType = assetBook.getDynamicObject("periodtype");
            hashSet.add(Long.valueOf(assetPeriodType.getLong("id")));
            depres.add((Long) assetBook.getDynamicObject("depreuse").getPkValue());
            if (periodTypeIdTemp == assetPeriodType.getLong("id")) {
                DynamicObject startPeriod = assetBook.getDynamicObject("startperiod");
                if (startPeriod != null) {
                    long startPeriodId = Long.parseLong(startPeriod.getString("number"));
                    DynamicObject currentPeriod = assetBook.getDynamicObject("curperiod");
                    if (currentPeriod != null) {
                        long currentPeriodId = Long.parseLong(currentPeriod.getString("number"));
                        if (compareMixPeriod == 0 || startPeriodId < compareMixPeriod) {
                            mixPeriod = ((Long) startPeriod.getPkValue()).longValue();
                            compareMixPeriod = startPeriodId;
                        }
                        key = assetBook.getDynamicObject("depreuse").getString("number");
                        DynamicObject tempAssetBook = (DynamicObject) regDepreuseMap.get(key);
                        if (tempAssetBook == null) {
                            regDepreuseMap.put(key, assetBook);
                        } else if (currentPeriodId < Long.parseLong(tempAssetBook.getDynamicObject("curperiod").getString("number"))) {
                            regDepreuseMap.put(key, assetBook);
                        }
                    }
                }
            }
        }
        DynamicObject q_DepreNum = (DynamicObject) getModel().getValue("depreuse");
        if (q_DepreNum != null) {
            compareMaxAssetBook = (DynamicObject) regDepreuseMap.get(q_DepreNum.getString("number"));
            maxPeriod = ((Long) compareMaxAssetBook.getDynamicObject("curperiod").getPkValue()).longValue();
        } else {
            DynamicObject ismainAssetBook = null;
            String firstDepreuseNumber = null;
            DynamicObject firstDepreuseAssetBook = null;
            for (Entry<String, DynamicObject> entryDet : regDepreuseMap.entrySet()) {
                key = (String) entryDet.getKey();
                DynamicObject valueObj = (DynamicObject) entryDet.getValue();
                if (Boolean.valueOf(valueObj.getBoolean("ismainbook")).booleanValue()) {
                    ismainAssetBook = valueObj;
                    break;
                } else if (firstDepreuseNumber == null) {
                    firstDepreuseNumber = key;
                    firstDepreuseAssetBook = valueObj;
                } else if (Long.getLong(firstDepreuseNumber).longValue() > Long.getLong(key).longValue()) {
                    firstDepreuseNumber = key;
                    firstDepreuseAssetBook = valueObj;
                }
            }
            if (ismainAssetBook != null) {
                compareMaxAssetBook = ismainAssetBook;
                maxPeriod = ((Long) compareMaxAssetBook.getDynamicObject("curperiod").getPkValue()).longValue();
            } else if (firstDepreuseAssetBook != null) {
                compareMaxAssetBook = firstDepreuseAssetBook;
                maxPeriod = ((Long) firstDepreuseAssetBook.getDynamicObject("curperiod").getPkValue()).longValue();
            }
        }
        if (compareMaxAssetBook == null) {
            getView().showTipNotification(ResManager.loadKDString("未找到查询条件期间的默认值!", "FaAssetListPlugin_13", "fi-fa-report", new Object[0]));
            return;
        }
        List<Long> periodIds = FaPeriodUtils.getPeriodIdByRange(mixPeriod, maxPeriod);
        if (isHideDepre) {
            q_Depre.setQFilter(new QFilter("id", "in", depres));
        }
        q_peroid.setQFilter(new QFilter("id", "in", periodIds));
        model.setValue("q_period", Long.valueOf(maxPeriod));
        q_peroidType.setQFilter(new QFilter("id", "in", hashSet));
        model.setValue("depreuse", compareMaxAssetBook.getDynamicObject("depreuse").getPkValue());
        model.setValue("periodtype", hashSet.toArray()[0]);
        boolean visible1 = true;
        if (depres.size() == 1 && hashSet.size() <= 0 && isHideDepre) {
            visible1 = false;
        }
        getView().setVisible(Boolean.valueOf(visible1), new String[]{"depreuse"});
        boolean visible2 = true;
        if (hashSet.size() == 1) {
            visible2 = false;
        }
        getView().setVisible(Boolean.valueOf(visible2), new String[]{"periodtype"});
    }

    public boolean verifyQuery(ReportQueryParam queryParam) {
        FilterInfo filterCondition = queryParam.getFilter();
        IFormView view = getView();
        DynamicObjectCollection orgs = filterCondition.getDynamicObjectCollection("q_org");
        if (orgs == null || orgs.size() <= 0) {
            view.showTipNotification(ResManager.loadKDString("请先选择核算组织!", "FaAssetListPlugin_4", "fi-fa-report", new Object[0]));
            return false;
        } else if (filterCondition.getDynamicObject("depreuse") == null) {
            view.showTipNotification(ResManager.loadKDString("请先选择折旧用途!", "FaAssetListPlugin_5", "fi-fa-report", new Object[0]));
            return false;
        } else if (filterCondition.getDynamicObject("q_period") == null) {
            view.showTipNotification(ResManager.loadKDString("请先选择会计期间!", "FaAssetListPlugin_6", "fi-fa-report", new Object[0]));
            return false;
        } else if (filterCondition.getDynamicObject("periodtype") == null) {
            view.showTipNotification(ResManager.loadKDString("请先选择会计期间类型!", "FaAssetListPlugin_14", "fi-fa-report", new Object[0]));
            return false;
        } else {
            List<Object> orgIds = new LinkedList();
            Iterator it = orgs.iterator();
            while (it.hasNext()) {
                orgIds.add(((DynamicObject) it.next()).getPkValue());
            }
            QFilter qorg = new QFilter("org", "in", orgIds);
            QFilter qdepreUse = new QFilter("depreuse", "=", filterCondition.getDynamicObject("depreuse").getPkValue());
            if (BusinessDataServiceHelper.loadSingle("fa_assetbook", RptDepreciation.BASECURRENCY, new QFilter[]{qorg, qdepreUse}) != null) {
                return true;
            }
            view.showTipNotification(ResManager.loadKDString("不存在此资产账簿！", "FaAssetListPlugin_7", "fi-fa-report", new Object[0]));
            return false;
        }
    }

    public void propertyChanged(PropertyChangedArgs e) {
        String propName = e.getProperty().getName();
        IDataModel model = getModel();
        DynamicObjectCollection orgs = (DynamicObjectCollection) model.getValue("q_org");
        if (orgs == null || orgs.size() <= 0) {
            getView().showTipNotification(ResManager.loadKDString("核算组织不能为空", "FaAssetListPlugin_8", "fi-fa-report", new Object[0]));
            return;
        }
        List<Object> orgIds = new LinkedList();
        Iterator it = orgs.iterator();
        while (it.hasNext()) {
            orgIds.add(((DynamicObject) it.next()).getDynamicObject("fbasedataid").getPkValue());
        }
        QFilter forg = new QFilter("org", "in", orgIds);
        QFilter fdepreuse;
        DynamicObject[] assetBooks;
        if (propName.equals("q_org")) {
            assetBooks = BusinessDataServiceHelper.load("fa_assetbook", "id,org,startperiod,depreuse,curperiod,periodtype,ismainbook", new QFilter[]{forg});
            DynamicObject mainAssetbook = getMainAssetbook(assetBooks);
            if (mainAssetbook != null) {
                getModel().setValue("depreuse", mainAssetbook.getDynamicObject("depreuse"));
            } else {
                getView().showErrorNotification(ResManager.loadKDString("所选核算组织没有主账簿", "FaAssetListPlugin_16", "fi-fa-report", new Object[0]));
            }
            changeFilterPanel(assetBooks, true);
        } else if (propName.equals("depreuse")) {
            DynamicObject depreuse = (DynamicObject) model.getValue("depreuse");
            if (depreuse == null) {
                getView().showTipNotification(ResManager.loadKDString("折旧用途不能为空", "FaAssetListPlugin_9", "fi-fa-report", new Object[0]));
                return;
            }
            fdepreuse = new QFilter("depreuse", "=", depreuse.getPkValue());
            changeFilterPanel(BusinessDataServiceHelper.load("fa_assetbook", "id,org,startperiod,depreuse,curperiod,periodtype,ismainbook", new QFilter[]{forg, fdepreuse}), false);
        } else if (propName.equals("periodtype")) {
            BasedataEdit q_peroid = (BasedataEdit) getControl("q_period");
            long mixPeriod = 0;
            long compareMixPeriod = 0;
            long maxPeriod = 99999999;
            long compareMaxPeriod = 99999999;
            DynamicObject periodType = (DynamicObject) model.getValue("periodtype");
            fdepreuse = new QFilter("depreuse", "=", ((DynamicObject) model.getValue("depreuse")).getPkValue());
            QFilter qFilter = new QFilter("periodtype", "=", periodType.getPkValue());
            assetBooks = BusinessDataServiceHelper.load("fa_assetbook", "id,org,startperiod,depreuse,curperiod,periodtype", new QFilter[]{forg, fdepreuse, qFilter});
            if (assetBooks.length == 0) {
                getView().showTipNotification(ResManager.loadKDString("不存在此期间类型的资产账簿", "FaAssetListPlugin_15", "fi-fa-report", new Object[0]));
                return;
            }
            for (DynamicObject assetBook : assetBooks) {
                DynamicObject startPeriod = assetBook.getDynamicObject("startperiod");
                if (startPeriod != null) {
                    long startPeriodId = Long.parseLong(startPeriod.getString("number"));
                    DynamicObject currentPeriod = assetBook.getDynamicObject("curperiod");
                    if (currentPeriod != null) {
                        long currentPeriodId = Long.parseLong(currentPeriod.getString("number"));
                        if (compareMixPeriod == 0 || startPeriodId < compareMixPeriod) {
                            mixPeriod = ((Long) startPeriod.getPkValue()).longValue();
                            compareMixPeriod = startPeriodId;
                        }
                        if (compareMaxPeriod == 99999999 || currentPeriodId < compareMaxPeriod) {
                            maxPeriod = ((Long) currentPeriod.getPkValue()).longValue();
                            compareMaxPeriod = currentPeriodId;
                        }
                    }
                }
            }
            q_peroid.setQFilter(new QFilter("id", "in", FaPeriodUtils.getPeriodIdByRange(mixPeriod, maxPeriod)));
            model.setValue("q_period", Long.valueOf(maxPeriod));
        }
    }

    private DynamicObject getMainAssetbook(DynamicObject[] assetBooks) {
        for (DynamicObject assetbook : assetBooks) {
            if (assetbook.getBoolean("ismainbook")) {
                return assetbook;
            }
        }
        return null;
    }

    public void hyperLinkClick(HyperLinkClickEvent event) {
        DynamicObject rowData = ((ReportList) event.getSource()).getReportModel().getRowData(event.getRowIndex());
        Object pkValue = null;
        if (rowData.get("fid") != null) {
            pkValue = rowData.getDynamicObject("fid").getPkValue();
        }
        if (pkValue != null && !StringUtils.isBlank(pkValue) && !RptAssetList.NULL_LONG.equals(pkValue.toString())) {
            showTabForm("fa_card_fin", ResManager.loadKDString("资产财务卡片", "FaAssetListPlugin_11", "fi-fa-report", new Object[0]), pkValue);
        }
    }

    private void showTabForm(String formId, String formName, Object pkValue) {
        IFormView view = getView();
        BaseShowParameter parameter = new BaseShowParameter();
        parameter.setPkId(pkValue);
        parameter.setCaption(formName);
        parameter.getOpenStyle().setTargetKey(FaReportUtils.TABAP);
        parameter.setParentFormId(view.getFormShowParameter().getParentFormId());
        parameter.setStatus(OperationStatus.ADDNEW);
        parameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);
        parameter.setFormId(formId);
        getView().showForm(parameter);
    }
}
