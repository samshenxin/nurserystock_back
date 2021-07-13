package kd.pmgt.pmas.formplugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.CloseCallBack;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.form.operate.FormOperate;
import kd.bos.form.plugin.IFormPlugin;
import kd.bos.list.ListShowParameter;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.pmgt.pmas.business.helper.ProjectApprovalHelper;
import kd.pmgt.pmas.common.utils.ImportProjTeamUtils;
import kd.pmgt.pmas.formplugin.base.AbstractPmasBillPlugin;
import kd.pmgt.pmbs.common.enums.BillTypeEnum;
import kd.pmgt.pmbs.common.utils.poi.POIHelper;

public class ProjTeamBillPlugin extends AbstractPmasBillPlugin implements BeforeF7SelectListener
{
    private static final String[] COLUMNKEY;
    private static final String[] FAIL_COLUMNKEY;
    private final String[] failHeader;
    private final String[] header;
    private static final String BACK_USERF7 = "back_userf7";
    private static final String SELECTED_MEMBER_CUREENT_ROW_INDEX = "rowindex";
    
    public ProjTeamBillPlugin() {
        this.failHeader = new String[] { "\u59d3\u540d", "\u89d2\u8272", "\u8054\u7cfb\u7535\u8bdd", "\u5de5\u53f7", "\u4eba\u5458\u7c7b\u578b", "\u5907\u6ce8", "\u5bfc\u5165\u5931\u8d25\u63d0\u793a" };
        this.header = new String[] { "\u59d3\u540d", "\u89d2\u8272", "\u8054\u7cfb\u7535\u8bdd", "\u5de5\u53f7", "\u4eba\u5458\u7c7b\u578b", "\u5907\u6ce8" };
    }
    
    public void registerListener(final EventObject e) {
        super.registerListener(e);
        final BasedataEdit teamRole = (BasedataEdit)this.getControl("role");
        teamRole.addBeforeF7SelectListener((BeforeF7SelectListener)this);
        final BasedataEdit procjet = (BasedataEdit)this.getControl("project");
        procjet.addBeforeF7SelectListener((BeforeF7SelectListener)this);
        final BasedataEdit teamMember = (BasedataEdit)this.getControl("member");
        teamMember.addBeforeF7SelectListener((BeforeF7SelectListener)this);
    }
    
    public void afterCreateNewData(final EventObject e) {
        final Object project = this.getView().getFormShowParameter().getCustomParam("projectId");
        final DynamicObject projectObj = (DynamicObject)this.getModel().getValue("project");
        if (projectObj != null) {
            this.getModel().setValue("project", project);
            this.getModel().setValue("projkind", projectObj.get("group"));
            this.getModel().setValue("billname", (Object)(projectObj.get("name") + ResManager.loadKDString("\u9879\u76ee\u56e2\u961f", "ProjTeamBillPlugin_0", "pmgt-pmas-formplugin", new Object[0])));
            final Object org = this.getModel().getValue("org");
            if (org == null) {
                this.getModel().setValue("org", projectObj.get("pmascreateorg"));
            }
            this.getView().getFormShowParameter().getCustomParams().remove("projectId");
        }
        final Object orgId = this.getView().getFormShowParameter().getCustomParam("orgId");
        if (orgId != null) {
            this.getModel().setValue("org", orgId);
            this.getView().getFormShowParameter().getCustomParams().remove("orgId");
        }
        this.getModel().setValue("type", (Object)BillTypeEnum.NEW.getValue());
        this.setDefaultProManager();
    }
    
    public void beforeDoOperation(final BeforeDoOperationEventArgs args) {
        super.beforeDoOperation(args);
        final FormOperate operate = (FormOperate)args.getSource();
        final String operateKey2;
        final String operateKey = operateKey2 = operate.getOperateKey();
        switch (operateKey2) {
            case "importentry": {
                final Object project = this.getModel().getValue("project");
                if (project == null) {
                    this.getView().showTipNotification("\u8bf7\u5148\u9009\u62e9\u9879\u76ee\u518d\u5bfc\u5165Excel");
                    return;
                }
                final Map<String, Object> params = new HashMap<String, Object>();
                params.put("fileName", "\u9879\u76ee\u6210\u5458\u6a21\u677f");
                params.put("sheetName", "\u9879\u76ee\u6210\u5458\u6a21\u677f");
                ImportProjTeamUtils.openImportExcelPage((AbstractBillPlugIn)this, "pmbs_projteamimportexcel", (Map)params, "importPageClosed");
                break;
            }
            case "templateexport": {
                final String fileName = "\u7533\u8bf7\u9879\u76ee\u6210\u5458\u6a21\u677f";
                final String sheetName = "\u7533\u8bf7\u9879\u76ee\u6210\u5458\u6a21\u677f";
                final JSONArray data = new JSONArray();
                final String url = POIHelper.exportExcel(fileName, sheetName, this.header, ProjTeamBillPlugin.COLUMNKEY, ImportProjTeamUtils.getMustFillColumn(), data);
                this.getView().download(url);
                this.getView().showSuccessNotification("\u6a21\u677f\u5bfc\u51fa\u6210\u529f", Integer.valueOf(3000));
                break;
            }
        }
    }
    
    public void closedCallBack(final ClosedCallBackEvent evt) {
        super.closedCallBack(evt);
        final String actionId2;
        final String actionId = actionId2 = evt.getActionId();
        switch (actionId2) {
            case "importPageClosed": {
                final String url = (String)evt.getReturnData();
                if (url == null || "".equals(url.trim())) {
                    return;
                }
                final String colhash = POIHelper.stringArrayToHash(ProjTeamBillPlugin.COLUMNKEY);
                final JSONArray jsonArray = POIHelper.importProjTeamExcel(url, colhash);
                this.finishImport(jsonArray);
                break;
            }
            case "back_userf7": {
                final ListSelectedRowCollection returnData = (ListSelectedRowCollection)evt.getReturnData();
                if (returnData == null) {
                    break;
                }
                final DynamicObjectCollection teamEntry = this.getView().getModel().getEntryEntity("teamentry");
                final String s = this.getPageCache().get("rowindex");
                final int selectedRowIndex = Integer.parseInt(s);
                teamEntry.remove(selectedRowIndex);
                this.getView().getModel().beginInit();
                for (int i = 0; i < returnData.size(); ++i) {
                    final DynamicObject rowObject = new DynamicObject(teamEntry.getDynamicObjectType());
                    final ListSelectedRow selectedRow = returnData.get(i);
                    final DynamicObject bos_user = BusinessDataServiceHelper.loadSingle(selectedRow.getPrimaryKeyValue(), "bos_user", "id,name,phone");
                    rowObject.set("member", (Object)bos_user);
                    rowObject.set("telno", bos_user.get("phone"));
                    teamEntry.add(i + selectedRowIndex, rowObject);
                    System.out.println(bos_user.get("name").toString());
                }
                this.getView().getModel().endInit();
                this.getModel().updateCache();
                this.getModel().updateEntryCache(teamEntry);
                this.getView().updateView();
                break;
            }
        }
    }
    
    private void fillOutStaffEntry(final List<JSONArray> outerStaffList) {
        if (CollectionUtils.isEmpty((Collection)outerStaffList)) {
            return;
        }
//        final Map<String, String> outerStaffMap;
        final Iterator<Object> iterator = null;
//        Object field;
//        JSONObject fieldObj;
//        String columnKey;
//        String value;
        final List<Map<String, String>> outerStaffMapList = outerStaffList.stream().map(lineFieldArray -> {
        	Map<String, String> outerStaffMap = new HashMap<String, String>(lineFieldArray.size());
            lineFieldArray.iterator();
            while (iterator.hasNext()) {
                Object field = iterator.next();
                JSONObject fieldObj = (JSONObject)field;
                String columnKey = fieldObj.getString("columnKey");
                String value = fieldObj.getString("cellValue");
                outerStaffMap.put(columnKey, value);
            }
            return outerStaffMap;
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty((Collection<?>)outerStaffMapList)) {
            return;
        }
        this.getModel().deleteEntryData("outerteamentry");
        this.getModel().beginInit();
        for (final Map<String, String> member : outerStaffMapList) {
            final int rowIndex = this.getModel().createNewEntryRow("outerteamentry");
            this.getModel().setValue("outerteamuser", (Object)member.get("member"), rowIndex);
            this.getModel().setValue("outteamrole", (Object)member.get("role"), rowIndex);
            this.getModel().setValue("outerteamtelno", (Object)member.get("telno"), rowIndex);
            this.getModel().setValue("outerteamdescription", (Object)member.get("note"), rowIndex);
        }
        this.getModel().endInit();
        this.getView().updateView("outerteamentry");
    }
    
    private void finishImport(final JSONArray jsonArray) {
        if (jsonArray == null || jsonArray.isEmpty()) {
            this.getView().showTipNotification("\u5bfc\u5165\u5185\u5bb9\u4e3a\u7a7a\uff0c\u53ef\u80fd\u662f\u6a21\u677f\u4e0d\u5339\u914d\u6216\u8005\u5bfc\u5165\u6587\u4ef6\u5185\u5bb9\u4e3a\u7a7a\u3002");
            return;
        }
        final Map<String, Object> resultMap = (Map<String, Object>)ImportProjTeamUtils.matchUserRole(jsonArray);
        final List<Map<String, Object>> matchedList = (List<Map<String, Object>>) resultMap.get("matchedList");
        final JSONArray matchFailedList = (JSONArray) resultMap.get("matchFailedList");
        final Set<Long> matchedUserIds = (Set<Long>) resultMap.get("matchedUserIds");
        final Map<Long, String> idPhones = (Map<Long, String>) resultMap.get("idPhones");
        final List<JSONArray> outerStaffList = (List<JSONArray>) resultMap.get("outerStaffs");
        this.fillOutStaffEntry(outerStaffList);
        final DynamicObject projObj = (DynamicObject)this.getModel().getValue("project");
        final QFilter filter = new QFilter("project", "=", projObj.getPkValue());
        filter.and("member", "in", (Object)matchedUserIds);
        final DynamicObjectCollection prtojNowMember = QueryServiceHelper.query("pmas_nowteam", "member,role", new QFilter[] { filter });
        final Map<Long, List<DynamicObject>> memberRoles = prtojNowMember.stream().collect(Collectors.groupingBy(o -> o.getLong("member")));
        final Iterator<Map<String, Object>> iterator = matchedList.iterator();
        while (iterator.hasNext()) {
            final Map<String, Object> entry = iterator.next();
            final Long matchedUserId = (Long) entry.get("matchedUserId");
            final String matchedRoleId = (String) entry.get("matchedRoleId");
            final String memberName = (String) entry.get("member");
            final String roleName = (String) entry.get("role");
            final String telno = (String) entry.get("telno");
            final List<DynamicObject> roles = memberRoles.get(matchedUserId);
            boolean isExists = false;
            if (roles != null) {
                for (final DynamicObject role : roles) {
                    final String roleId = role.get("role").toString();
                    if (StringUtils.isNotBlank((CharSequence)matchedRoleId) && matchedRoleId.equals(roleId)) {
                        isExists = true;
                        entry.put("failmsg", String.format("\u59d3\u540d\uff1a%s\uff0c\u89d2\u8272\uff1a%s\uff0c\u5728\u8be5\u9879\u76ee\u56e2\u961f\u4e2d\u5df2\u5b58\u5728\uff0c\u65e0\u6cd5\u5bfc\u5165\u3002", memberName, roleName));
                        entry.remove("matchedRoleId");
                        final JSONObject failObj = ImportProjTeamUtils.mapToJsonObject((Map)entry);
                        matchFailedList.add((Object)failObj);
                        iterator.remove();
                    }
                }
            }
            if (!isExists && StringUtils.isBlank((CharSequence)telno)) {
                final String phone = idPhones.get(matchedUserId);
                entry.put("telno", phone);
            }
        }
        final int successCount = matchedList.size();
        if (successCount > 0) {
            this.getModel().deleteEntryData("teamentry");
            this.getModel().beginInit();
            for (final Map<String, Object> member : matchedList) {
                final int rowIndex = this.getModel().createNewEntryRow("teamentry");
                this.getModel().setValue("member", member.get("matchedUserId"), rowIndex);
                this.getModel().setValue("role", member.get("matchedRoleId"), rowIndex);
                this.getModel().setValue("telno", member.get("telno"), rowIndex);
                this.getModel().setValue("note", member.get("note"), rowIndex);
            }
            this.getModel().endInit();
            this.getView().updateView("teamentry");
        }
        final int failCount = matchFailedList.size();
        if (failCount > 0) {
            final String fileName = "\u5bfc\u5165\u7ed3\u679c";
            final String sheetName = "\u5bfc\u5165\u7ed3\u679c";
            final String exportUrl = POIHelper.exportExcel(fileName, sheetName, this.failHeader, ProjTeamBillPlugin.FAIL_COLUMNKEY, ImportProjTeamUtils.getMustFillColumn(), matchFailedList);
            this.getView().download(exportUrl);
            this.getView().showSuccessNotification(String.format("\u5bfc\u5165\u6210\u529f%s\u884c\uff0c\u5bfc\u5165\u5931\u8d25%s\u884c\uff0c\u8bf7\u68c0\u67e5\u300a\u5bfc\u5165\u7ed3\u679c.xls\u300b", successCount + outerStaffList.size(), failCount), Integer.valueOf(3000));
        }
        else {
            this.getView().showSuccessNotification("\u5bfc\u5165\u6210\u529f");
        }
    }
    
    public void afterDoOperation(final AfterDoOperationEventArgs afterDoOperationEventArgs) {
        super.afterDoOperation(afterDoOperationEventArgs);
        final String operateKey = afterDoOperationEventArgs.getOperateKey();
        if (StringUtils.equals((CharSequence)operateKey, (CharSequence)"newentry")) {
            final DynamicObjectCollection entryEntity = this.getModel().getEntryEntity("teamentry");
            if (entryEntity.size() == 1) {
                final int index = 0;
                this.getModel().setValue("ischarge", (Object)Boolean.TRUE, index);
            }
        }
    }
    
    public void propertyChanged(final PropertyChangedArgs e) {
        final String name = e.getProperty().getName();
        final ChangeData changeData = e.getChangeSet()[0];
        final Object newValue = changeData.getNewValue();
        final int curIndex = changeData.getRowIndex();
        final String s = name;
        switch (s) {
            case "member": {
                this.getModel().setValue("role", (Object)null, curIndex);
                if (newValue == null) {
                    this.getModel().setValue("telno", (Object)null, curIndex);
                    break;
                }
                final DynamicObject member = (DynamicObject)newValue;
                this.getModel().setValue("telno", (Object)member.getString("phone"), curIndex);
                break;
            }
            case "role": {
                final Map<String, String> paraMap = new HashMap<String, String>();
                paraMap.put("member", "member");
                paraMap.put("role", "role");
                paraMap.put("entry", "teamentry");
                final boolean isUnique = ImportProjTeamUtils.isUserRoleUnique((AbstractBillPlugIn)this, curIndex, (Map)paraMap);
                if (!isUnique) {
                    this.getModel().setValue("role", changeData.getOldValue(), curIndex);
                    break;
                }
                break;
            }
            case "ischarge": {
                final Boolean isCharge = (Boolean)newValue;
                if (isCharge && this.getModel().getValue("role") == null) {
                    this.getModel().setValue("role", (Object)"05KG88CICWSH", curIndex);
                    break;
                }
                break;
            }
            case "project": {
                final DynamicObject projectObj = (DynamicObject)newValue;
                if (projectObj != null) {
                    this.getModel().setValue("projkind", projectObj.get("group"));
                    this.getModel().setValue("billname", (Object)(projectObj.get("name") + ResManager.loadKDString("\u9879\u76ee\u56e2\u961f", "ProjTeamBillPlugin_0", "pmgt-pmas-formplugin", new Object[0])));
                    final DynamicObject nowTeamObj = ProjectApprovalHelper.getNowTeamObj(projectObj.getPkValue());
                    if (nowTeamObj != null) {
                        this.getModel().setValue("proleader", (Object)nowTeamObj.getDynamicObject("member"));
                        this.getModel().setValue("leaderconttype", (Object)nowTeamObj.getString("telno"));
                    }
                    break;
                }
                break;
            }
        }
    }
    
    private void setDefaultProManager() {
        final DynamicObjectCollection teamEntryEntity = this.getModel().getEntryEntity("teamentry");
        if (teamEntryEntity.size() == 1) {
            final String userId = RequestContext.get().getUserId();
            final DynamicObject proAppTeam = (DynamicObject)teamEntryEntity.get(0);
            final DynamicObject user = BusinessDataServiceHelper.loadSingle((Object)userId, "bos_user");
            proAppTeam.set("member", (Object)user);
            proAppTeam.set("telno", user.get("phone"));
            proAppTeam.set("ischarge", (Object)Boolean.TRUE);
            this.getModel().updateEntryCache(teamEntryEntity);
            this.getModel().setValue("role", (Object)"05KG88CICWSH", 0);
        }
    }
    
    public void beforeF7Select(final BeforeF7SelectEvent e) {
        final String key = e.getProperty().getName();
        final ListShowParameter param = (ListShowParameter)e.getFormShowParameter();
        final int curRowIndex = this.getModel().getEntryCurrentRowIndex("teamentry");
        final String s = key;
        switch (s) {
            case "member": {
                this.getPageCache().put("rowindex", String.valueOf(curRowIndex));
                e.getFormShowParameter().setCloseCallBack(new CloseCallBack((IFormPlugin)this, "back_userf7"));
                break;
            }
            case "role": {
                final DynamicObject member = (DynamicObject)this.getModel().getValue("member", curRowIndex);
                if (member == null) {
                    this.getView().showTipNotification(ResManager.loadKDString("\u8bf7\u5148\u9009\u4e2d\u4eba\u5458", "ProjTeamBillPlugin_2", "pmgt-pmas-formplugin", new Object[0]));
                    e.setCancel(true);
                    break;
                }
                break;
            }
            case "project": {
                final DynamicObject org = (DynamicObject)this.getModel().getValue("org");
                if (org != null) {
                    @SuppressWarnings("unchecked")
					final DynamicObject[] permProjects = ProjectPermissionHelper.getPermProject((List)Stream.of(org.getPkValue()).collect(Collectors.toList()));
                    final Set<Object> projIds = Arrays.stream(permProjects).map(o -> (Long)o.getPkValue()).collect((Collector<? super Object, ?, Set<Object>>)Collectors.toSet());
                    final QFilter newFilter = new QFilter("type", "=", (Object)BillTypeEnum.NEW.getValue());
                    final DynamicObject[] projTeams = BusinessDataServiceHelper.load("pmas_team", "id,project", new QFilter[] { newFilter });
                    final Set<Object> existsProjId = Arrays.stream(projTeams).filter(obj -> obj.getDynamicObject("project") != null).map(o -> Long.valueOf(o.getDynamicObject("project").getPkValue().toString())).collect((Collector<? super Object, ?, Set<Object>>)Collectors.toSet());
//                    projIds.removeAll(existsProjId);
                    param.getListFilterParameter().getQFilters().clear();
                    param.getListFilterParameter().getQFilters().add(new QFilter("id", "in", (Object)projIds));
                    break;
                }
                this.getView().showTipNotification(ResManager.loadKDString("\u6ca1\u6709\u521b\u5efa\u7ec4\u7ec7\uff0c\u65e0\u6cd5\u9009\u62e9\u9879\u76ee\u3002", "ProjTeamBillPlugin_3", "pmgt-pmas-formplugin", new Object[0]));
                e.setCancel(true);
                break;
            }
        }
    }
    
    static {
        COLUMNKEY = new String[] { "member", "role", "telno", "empnum", "stafftype", "note" };
        FAIL_COLUMNKEY = new String[] { "member", "role", "telno", "empnum", "stafftype", "note", "failmsg" };
    }
}
