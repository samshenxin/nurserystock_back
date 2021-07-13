package kd.pmgt.pmas.formplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DataEntityBase;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.form.field.ComboItem;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.api.HasPermOrgResult;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.devportal.BizAppServiceHelp;
import kd.bos.servicehelper.org.OrgServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.permission.PermissionServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.pmgt.pmbs.common.enums.EnableEnum;
import kd.pmgt.pmbs.common.enums.ProjectStageEnum;
import kd.pmgt.pmbs.common.enums.ProjectStatusEnum;
import kd.pmgt.pmbs.common.enums.StatusEnum;

public class ProjectPermissionHelper
{
    public static final String PMASCREATEORGKEY = "pmascreateorg";
    private static final String DEPARTMENTKEY = "department";
    public static final String PROJECT = "project";
    public static final String MEMBER = "member";
    public static final String PMAS_NOWTEAM = "pmas_nowteam";
    public static final String BD_PROJECT = "bd_project";
    public static final String FBASEDATA_ID = "fbasedataId";
    public static final String NUMBER = "number";
    
    public static List<Long> getAllPermOrgsByPermItem(final String orgViewType, List<Long> orgList, final boolean isSubordinate, final String userId, final String appId, final String entityNum, final String permItemId) {
        if (isSubordinate) {
            orgList = (List<Long>)OrgUnitServiceHelper.getAllSubordinateOrgs(Long.valueOf(orgViewType), (List)orgList, isSubordinate);
        }
        final List<Long> allPermOrgs = getAllPermOrgsByPermItem(orgViewType, userId, appId, entityNum, permItemId);
        if (orgList != null && !orgList.isEmpty()) {
            orgList.retainAll(allPermOrgs);
        }
        return orgList;
    }
    
    public static List<Long> getAllPermOrgsByPermItem(final String orgViewType, final String userId, final String appId, final String entityNum, final String permItemId) {
        final HasPermOrgResult result = PermissionServiceHelper.getAllPermOrgs(Long.parseLong(userId), orgViewType, appId, entityNum, permItemId);
        if (result.hasAllOrgPerm()) {
            final List<Long> orgList = new ArrayList<Long>(10);
            orgList.add(OrgUnitServiceHelper.getRootOrgId());
            return (List<Long>)OrgUnitServiceHelper.getAllSubordinateOrgs(Long.valueOf("15"), (List)orgList, true);
        }
        return (List<Long>)result.getHasPermOrgs();
    }
    
    public static List<Long> getAllPermOrgs(final String orgViewType, final String userId, final String appId, final String entityNum, final String operateNum) {
        if (StringUtils.isEmpty((CharSequence)appId)) {
            return null;
        }
        final String apppk = BizAppServiceHelp.getAppIdByAppNumber(appId);
        final Map<String, Object> permItem = getPermObj(entityNum, operateNum);
        final HasPermOrgResult result = PermissionServiceHelper.getAllPermOrgs(Long.parseLong(userId), orgViewType, apppk, entityNum, String.valueOf(permItem.get("id")));
        if (result.hasAllOrgPerm()) {
            final List<Long> orgList = new ArrayList<Long>(10);
            orgList.add(OrgUnitServiceHelper.getRootOrgId());
            return (List<Long>)OrgUnitServiceHelper.getAllSubordinateOrgs(Long.valueOf("15"), (List)orgList, true);
        }
        return (List<Long>)result.getHasPermOrgs();
    }
    
    public static List<Long> getAllPermOrgs(final String userId, final String appId, final String entityNum, final String operateNum) {
        return getAllPermOrgs("15", userId, appId, entityNum, operateNum);
    }
    
    public static List<ComboItem> buildProComboItems(final List<Long> orgList, final boolean isSubordinate, final String entityNum, final String operateNum) {
        final DynamicObject[] projects = getPermProjectByStatus(orgList, true, entityNum, operateNum, null);
        return loadProComboItems(projects);
    }
    
    public static DynamicObject[] getPermContract(final List<Long> orgList, final boolean isSubordinate, final DynamicObject project, final String entityNum, final String operateNum) {
        final QFilter filter = getPermContractFilter(orgList, isSubordinate, project, entityNum, operateNum);
        final DynamicObject[] contracts = BusinessDataServiceHelper.load("pmct_contracttpl", "", new QFilter[] { filter });
        return contracts;
    }
    
    public static QFilter getPermContractFilter(final List<Long> orgList, final boolean isSubordinate, final DynamicObject project, final String entityNum, final String operateNum) {
        QFilter filter = null;
        if (project == null) {
            filter = new QFilter("project", "=", (Object)0);
            final QFilter orgFilter = new QFilter("org", "in", (Object)orgList);
            filter.and(orgFilter);
            final String[] excludeStatusIds = { ProjectStatusEnum.FINANCIAL_CLOSE.getId() };
            final DynamicObject[] filterProjects = getPermProjectByStatus(orgList, isSubordinate, entityNum, operateNum, excludeStatusIds);
            if (filterProjects.length != 0) {
                final Object[] ids = new Object[filterProjects.length];
                for (int i = 0; i < filterProjects.length; ++i) {
                    ids[i] = filterProjects[i].getPkValue();
                }
                final QFilter projectFilter = new QFilter("project", "in", (Object)ids);
                filter.or(projectFilter);
            }
        }
        else {
            filter = new QFilter("project", "=", project.getPkValue());
        }
        return filter;
    }
    
    public static List<ComboItem> buildProComboItemsNormal(final List<Long> orgList, final String entityNum, final String operateNum) {
        final DynamicObject[] projects = getPermProject(orgList);
        return loadProComboItems(projects);
    }
    
    public static List<ComboItem> loadProComboItems(final DynamicObject[] projects) {
        final List<ComboItem> combos = new ArrayList<ComboItem>(10);
        for (final DynamicObject project : projects) {
            final ComboItem item = new ComboItem();
            item.setId(project.getPkValue().toString());
            item.setCaption(new LocaleString(project.getString("name")));
            item.setValue(project.getPkValue().toString());
            combos.add(item);
        }
        return combos;
    }
    
    public static List<ComboItem> buildProManagerProItem(final List<Long> orgIds, final String userId) {
        final DynamicObject[] projects = getTeamManagerProj(orgIds, userId);
        return loadProComboItems(projects);
    }
    
    public static DynamicObject[] getPermProject(final List<Long> orgList) {
        final String[] excludeStatusIds = new String[0];
        return getPermProject(orgList, excludeStatusIds);
    }
    
    public static DynamicObject[] getPermProject(final List<Long> orgList, final String[] excludeStatusIds) {
        final List<QFilter> filters = new ArrayList<QFilter>();
        if (excludeStatusIds != null && excludeStatusIds.length > 0) {
            final QFilter filter1 = new QFilter("prostatus", "not in", (Object)excludeStatusIds);
            filters.add(filter1);
        }
        if (orgList != null && !orgList.isEmpty()) {
            final QFilter orgfilter = new QFilter("pmascreateorg", "in", (Object)orgList);
            filters.add(orgfilter);
        }
        final QFilter filter2 = new QFilter("prostatus", "!=", (Object)0);
        filters.add(filter2);
        final DynamicObject[] projects = BusinessDataServiceHelper.load("bd_project", "", (QFilter[])filters.toArray(new QFilter[filters.size()]));
        if (projects.length < 1) {
            return projects;
        }
        final Object[] projectIds = new Object[projects.length];
        for (int i = 0; i < projects.length; ++i) {
            projectIds[i] = projects[i].getPkValue();
        }
        final QFilter filter3 = new QFilter("projectstage", "=", (Object)ProjectStageEnum.PROPOSALSTAGE_S.getId());
        final QFilter filter4 = new QFilter("projectstatus", "=", (Object)ProjectStatusEnum.APPROVAL_SUCC.getId());
        final QFilter filter5 = new QFilter("project", "in", (Object)projectIds);
        filter3.and(filter4).and(filter5);
        final DynamicObject[] statgeEntries = BusinessDataServiceHelper.load("pmas_prostatus", "project", new QFilter[] { filter3 });
        DynamicObject[] filterProjects = new DynamicObject[0];
        if (statgeEntries.length != 0) {
            filterProjects = new DynamicObject[statgeEntries.length];
            for (int j = 0; j < statgeEntries.length; ++j) {
                filterProjects[j] = statgeEntries[j].getDynamicObject("project");
            }
        }
        return filterProjects;
//        return projects;
    }
    
    public static DynamicObject[] getPermProjByOrgList(final long orgid, final String[] excludeStatusIds) {
        final List<Long> orgIdList = new ArrayList<Long>();
        orgIdList.add(orgid);
        return getPermProjByOrgList(orgIdList, excludeStatusIds);
    }
    
    public static DynamicObject[] getPermProjByOrgList(final List<Long> orgIdList, final String[] excludeStatusIds) {
        final Map<Object, String> projects = getProjectWithNoRoles(orgIdList);
        return getPermProjectByStatus(projects.keySet(), excludeStatusIds);
    }
    
    private static Map<Object, String> getProjectWithNoRoles(final List<Long> orgIdList) {
        final Map<Object, String> projects = new HashMap<Object, String>();
        final String userId = RequestContext.get().getUserId();
        final Map<Object, String> chargerProj = getChargerProjByOrgList(orgIdList, userId);
        projects.putAll(chargerProj);
        final Map<Object, String> additionProj = getAdditionProjByOrgList(orgIdList, userId);
        projects.putAll(additionProj);
        final Map<Object, String> teamMemberProj = getTeamMemberProjByOrgList(orgIdList, userId);
        projects.putAll(teamMemberProj);
        return projects;
    }
    
    public static DynamicObject[] getPermProjectByStatus(final List<Long> orgList, final boolean isSubordinate, final String entityNum, final String operateNum) {
        final String[] excludeStatusIds = { ProjectStatusEnum.BUSSINESS_CLOSE.getId(), ProjectStatusEnum.FINANCIAL_CLOSE.getId() };
        return getPermProjectByStatus(orgList, isSubordinate, entityNum, operateNum, excludeStatusIds);
    }
    
    public static DynamicObject[] getPermProjectByStatus(final List<Long> orgList, final boolean isSubordinate, final String entityNum, final String operateNum, final String[] excludeStatusIds) {
        final Map<Object, String> projectIds = getEntityPermProjByOrgList(orgList, isSubordinate, entityNum, operateNum);
        return getPermProjectByStatus(projectIds.keySet(), excludeStatusIds);
    }
    
    public static DynamicObject[] getPermProjectByStatus(final Set<Object> projectIds, final String[] excludeStatusIds) {
        final List<Long> ids = projectIds.stream().map(o -> Long.valueOf(o.toString())).collect(Collectors.toList());
        Object[] pids = null;
        if (excludeStatusIds != null && excludeStatusIds.length > 0) {
            final List<QFilter> filters = new ArrayList<QFilter>();
            final QFilter projectFilter = new QFilter("id", "in", (Object)ids);
            filters.add(projectFilter);
            final QFilter filter1 = new QFilter("prostatus", "not in", (Object)kd.pmgt.pmbs.common.utils.StringUtils.stringArrToLongArr(excludeStatusIds));
            filters.add(filter1);
            final QFilter filter2 = new QFilter("prostatus", "!=", (Object)0);
            filters.add(filter2);
            final DynamicObject[] projects = BusinessDataServiceHelper.load("bd_project", "", (QFilter[])filters.toArray(new QFilter[filters.size()]));
            if (projects.length < 1) {
                return projects;
            }
            pids = new Object[projects.length];
            for (int i = 0; i < projects.length; ++i) {
                pids[i] = projects[i].getPkValue();
            }
        }
        final QFilter filter3 = new QFilter("projectstage", "=", (Object)Long.valueOf(ProjectStageEnum.PROPOSALSTAGE_S.getId()));
        final QFilter filter4 = new QFilter("projectstatus", "=", (Object)Long.valueOf(ProjectStatusEnum.APPROVAL_SUCC.getId()));
        if (pids == null) {
            final QFilter filter5 = new QFilter("project", "in", (Object)ids);
            filter3.and(filter4).and(filter5);
        }
        else {
            final QFilter filter5 = new QFilter("project", "in", (Object)pids);
            filter3.and(filter4).and(filter5);
        }
        final DynamicObject[] statgeEntries = BusinessDataServiceHelper.load("pmas_prostatus", "project", new QFilter[] { filter3 });
        DynamicObject[] filterProjects = new DynamicObject[0];
        if (statgeEntries.length != 0) {
            filterProjects = new DynamicObject[statgeEntries.length];
            for (int j = 0; j < statgeEntries.length; ++j) {
                filterProjects[j] = statgeEntries[j].getDynamicObject("project");
            }
        }
        return filterProjects;
    }
    
    public static Set<Object> getPermProjectIds(final String userId) {
        final Set<Object> proIdObjs = new HashSet<Object>();
        final Map<Object, String> chargerProjByOrgList = getChargerProjByOrgList(null, userId);
        final Map<Object, String> additionProjByOrgList = getAdditionProjByOrgList(null, userId);
        final Map<Object, String> teamMemberProjByOrgList = getTeamMemberProjByOrgList(null, userId);
        proIdObjs.addAll(chargerProjByOrgList.keySet());
        proIdObjs.addAll(additionProjByOrgList.keySet());
        proIdObjs.addAll(teamMemberProjByOrgList.keySet());
        final Set<Object> proIds = proIdObjs.stream().map(o -> o).collect(Collectors.toSet());
        return proIds;
    }
    
    public static Map<Object, String> getEntityPermProjByOrgList(final List<Long> orgIdList, final boolean isSubordinate, final String entityNum, final String operateNum) {
        final Map<Object, String> projects = new HashMap<Object, String>();
        final String userId = RequestContext.get().getUserId();
        final Map<Object, String> chargerProj = getChargerProjByOrgList(orgIdList, userId);
        if (!chargerProj.isEmpty()) {
            projects.putAll(chargerProj);
        }
        final Map<Object, String> additionProj = getAdditionProjByOrgList(orgIdList, userId);
        final Map<Object, String> teamMemberProj = getTeamMemberProjByOrgList(orgIdList, userId);
        final Map<Object, Set<Object>> teamProjRoles = getTeamProjRoles(userId);
        final Map<Object, Set<Object>> additionProjRoles = getAdditionProjRoles(orgIdList, userId);
        final HashSet<Object> allRolesId = new HashSet<Object>(16);
        for (final Map.Entry<Object, Set<Object>> entry : teamProjRoles.entrySet()) {
            allRolesId.addAll(entry.getValue());
        }
        for (final Map.Entry<Object, Set<Object>> entry : additionProjRoles.entrySet()) {
            allRolesId.addAll(entry.getValue());
        }
        final String permNumber = (String) getPermNum(entityNum, operateNum);
        if (!StringUtils.isBlank((CharSequence)permNumber)) {
            final HashSet<Object> noPermRoles = getNoPermRoles(entityNum, allRolesId, permNumber);
            final Set<Object> result = new HashSet<Object>(16);
            final Set<Object> teamnoPermProjs = new HashSet<Object>(16);
            for (final Map.Entry<Object, Set<Object>> en : teamProjRoles.entrySet()) {
                final Set<Object> roles = en.getValue();
                result.clear();
                result.addAll(roles);
                result.removeAll(noPermRoles);
                if (result.isEmpty()) {
                    teamnoPermProjs.add(en.getKey());
                }
            }
            final Set<Object> addtionNopermObjs = new HashSet<Object>(16);
            for (final Map.Entry<Object, Set<Object>> en2 : additionProjRoles.entrySet()) {
                final Set<Object> roles2 = en2.getValue();
                result.clear();
                result.addAll(roles2);
                result.removeAll(noPermRoles);
                if (result.isEmpty()) {
                    addtionNopermObjs.add(en2.getKey());
                }
            }
            Iterator<Map.Entry<Object, String>> it = teamMemberProj.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<Object, String> next = it.next();
                if (teamnoPermProjs.contains(next.getKey())) {
                    it.remove();
                }
            }
            it = additionProj.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<Object, String> next = it.next();
                if (addtionNopermObjs.contains(next.getKey())) {
                    it.remove();
                }
            }
        }
        projects.putAll(teamMemberProj);
        projects.putAll(additionProj);
        return projects;
    }
    
    public static DynamicObject[] getTeamManagerProj(List<Long> orgIds, final String userId) {
        if (orgIds == null) {
            orgIds = new ArrayList<Long>(10);
        }
        final QFilter filter = new QFilter("member", "=", (Object)userId);
        final QFilter filter2 = new QFilter("project.prostatus.id", "not in", (Object)0);
        final QFilter orgFilter = new QFilter("project.pmascreateorg", "in", (Object)orgIds);
        filter.and("ischarge", "=", (Object)"1");
        final DynamicObject[] projects = BusinessDataServiceHelper.load("pmas_nowteam", "project", new QFilter[] { filter, filter2, orgFilter });
        if (projects == null || projects.length < 1) {
            return projects;
        }
        final Object[] projIds = new Object[projects.length];
        for (int i = 0; i < projects.length; ++i) {
            projIds[i] = ((DynamicObject)projects[i].get("project")).getPkValue();
        }
        final QFilter filter3 = new QFilter("projectstage", "=", (Object)ProjectStageEnum.PROPOSALSTAGE_S.getId());
        final QFilter filter4 = new QFilter("projectstatus", "=", (Object)Long.valueOf(ProjectStatusEnum.APPROVAL_SUCC.getId()));
        final QFilter filter5 = new QFilter("project", "in", (Object)projIds);
        filter3.and(filter4).and(filter5);
        final DynamicObject[] statgeEntries = BusinessDataServiceHelper.load("pmas_prostatus", "project", new QFilter[] { filter3 });
        DynamicObject[] filterProjects = new DynamicObject[0];
        if (statgeEntries.length != 0) {
            filterProjects = new DynamicObject[statgeEntries.length];
            for (int j = 0; j < statgeEntries.length; ++j) {
                filterProjects[j] = statgeEntries[j].getDynamicObject("project");
            }
        }
        return filterProjects;
    }
    
    public static List<Object> getCustomFilterValue(final List<Map<String, List<Object>>> customFilters, final String fieldName) {
        List<Object> value = null;
        if (customFilters == null) {
            return value;
        }
        for (final Map<String, List<Object>> entry : customFilters) {
            final List<Object> field = entry.get("FieldName");
            if (field != null && !field.isEmpty() && fieldName.equals(field.get(0).toString())) {
                value = entry.get("Value");
                break;
            }
        }
        return value;
    }
    
    private static Map<Object, String> getChargerProjByOrgList(final List<Long> orgIdList, final String userId) {
        Map<Object, String> chargeProMap = new HashMap<Object, String>();
        final Set<Long> chargeOrgIds = getChargerOrgIds(userId);
        final List<QFilter> qFilters = new ArrayList<QFilter>();
        final QFilter deporgFilter = new QFilter("department", "in", (Object)chargeOrgIds);
        qFilters.add(deporgFilter);
        if (orgIdList != null && orgIdList.size() > 0) {
            final QFilter orgFilter = new QFilter("pmascreateorg", "in", (Object)orgIdList);
            qFilters.add(orgFilter);
        }
        final QFilter statusFilter = new QFilter("status", "=", (Object)StatusEnum.CHECKED.getValue());
        qFilters.add(statusFilter);
        final QFilter enableFilter = new QFilter("enable", "=", (Object)EnableEnum.ENABLE.getValue());
        qFilters.add(enableFilter);
        final DynamicObject[] chargeProjects = BusinessDataServiceHelper.load("bd_project", "id,name", (QFilter[])qFilters.toArray(new QFilter[qFilters.size()]));
        if (chargeProjects != null && chargeProjects.length != 0) {
            chargeProMap = Stream.of(chargeProjects).collect(Collectors.toMap((Function<? super DynamicObject, ?>)DataEntityBase::getPkValue, key -> key.getString("name")));
        }
        return chargeProMap;
    }
    
    private static Map<Object, String> getAdditionProjByOrgList(final List<Long> orgList, final String userId) {
        final Map<Object, String> chargeProMap = new HashMap<Object, String>(16);
        final Set<Long> allChargeSubordinateOrgs = new HashSet<Long>(16);
        final QFilter userFilter = new QFilter("user", "=", (Object)Long.parseLong(userId));
        final QFilter enbaleFilter = new QFilter("enable", "=", (Object)Boolean.TRUE);
        final String selectors = Stream.of(new String[] { "org", "issubordinate", "excluprojstr", "rolesstr", "exclusionproj" }).collect(Collectors.joining(","));
        final DynamicObject[] perms = BusinessDataServiceHelper.load("pmbs_propermission", selectors, new QFilter[] { userFilter, enbaleFilter });
        final Set<Object> excluProjs = new HashSet<Object>(16);
        for (final DynamicObject obj : perms) {
            final DynamicObjectCollection multiObjs = obj.getDynamicObjectCollection("exclusionproj");
            final Object pkValue = obj.getDynamicObject("org").getPkValue();
            final boolean objIsSubordinate = obj.getBoolean("issubordinate");
            final Set<Object> excluProjIds = multiObjs.stream().map(pro -> pro.getDynamicObject("fbasedataId").getPkValue()).collect((Collector<? super Object, ?, Set<Object>>)Collectors.toSet());
            excluProjs.addAll(excluProjIds);
            if (objIsSubordinate) {
                final List<Long> allSubOrges = (List<Long>)OrgServiceHelper.getAllSubordinateOrgs("01", (List)Stream.of(Long.valueOf(pkValue.toString())).collect(Collectors.toList()), true);
                allChargeSubordinateOrgs.addAll(allSubOrges);
            }
            else {
                allChargeSubordinateOrgs.add(Long.valueOf(pkValue.toString()));
            }
        }
        if (!allChargeSubordinateOrgs.isEmpty()) {
            final QFilter orgFilter1 = new QFilter("department", "in", (Object)allChargeSubordinateOrgs);
            if (orgList != null && orgList.size() > 0) {
                orgFilter1.and("pmascreateorg", "in", (Object)orgList);
            }
            orgFilter1.and("status", "=", (Object)StatusEnum.CHECKED.getValue());
            orgFilter1.and("enable", "=", (Object)EnableEnum.ENABLE.getValue());
            final QFilter excluFilter1 = new QFilter("id", "not in", (Object)excluProjs);
            final DynamicObject[] permProjs = BusinessDataServiceHelper.load("bd_project", "id,name", new QFilter[] { orgFilter1, excluFilter1 });
            final Map<Object, String> perProj = Arrays.stream(permProjs).collect(Collectors.toMap((Function<? super DynamicObject, ?>)DataEntityBase::getPkValue, key -> key.getString("name")));
            chargeProMap.putAll(perProj);
        }
        return chargeProMap;
    }
    
    private static Map<Object, String> getTeamMemberProjByOrgList(final List<Long> orgList, final String userId) {
        final Map<Object, String> chargeProMap = new HashMap<Object, String>();
        final DynamicObject[] nowteams = BusinessDataServiceHelper.load("pmas_nowteam", "project", new QFilter[] { new QFilter("member", "=", (Object)Long.parseLong(userId)) });
        if (nowteams.length > 0) {
            for (final DynamicObject nowteam : nowteams) {
                final DynamicObject project = nowteam.getDynamicObject("project");
                if (project != null) {
                    final DynamicObject org = project.getDynamicObject("pmascreateorg");
                    if (org != null) {
                        if (orgList == null || orgList.isEmpty()) {
                            chargeProMap.put(project.getPkValue(), project.getString("name"));
                        }
                        else if (orgList.contains(org.getPkValue())) {
                            chargeProMap.put(project.getPkValue(), project.getString("name"));
                        }
                    }
                }
            }
        }
        return chargeProMap;
    }
    
    private static Map<Object, Set<Object>> getTeamProjRoles(final String userId) {
        final Map<Object, Set<Object>> projRolesMap = new HashMap<Object, Set<Object>>();
        final QFilter filter = new QFilter("member", "=", (Object)Long.parseLong(userId));
        filter.and("project.status", "=", (Object)StatusEnum.CHECKED.getValue());
        filter.and("project.enable", "=", (Object)EnableEnum.ENABLE.getValue());
        final DynamicObject[] projRoles = BusinessDataServiceHelper.load("pmas_nowteam", "project,member,role", new QFilter[] { filter });
        Set<Object> roles = null;
        for (final DynamicObject obj : projRoles) {
            if (obj.getDynamicObject("project") != null) {
                final Object projId = obj.getDynamicObject("project").getPkValue();
                roles = projRolesMap.get(projId);
                if (roles == null || roles.isEmpty()) {
                    roles = new HashSet<Object>(16);
                    projRolesMap.put(projId, roles);
                }
                if (obj.getDynamicObject("role") != null) {
                    roles.add(obj.getDynamicObject("role").getPkValue());
                }
            }
        }
        return projRolesMap;
    }
    
    private static Map<Object, Set<Object>> getAdditionProjRoles(final List<Long> orgIdList, final String userId) {
        final Map<Object, Set<Object>> projRolesMap = new HashMap<Object, Set<Object>>();
        final QFilter userFilter = new QFilter("user", "=", (Object)Long.parseLong(userId));
        final QFilter enableFilter = new QFilter("enable", "=", (Object)Boolean.TRUE);
        final DynamicObject[] additionUser = BusinessDataServiceHelper.load("pmbs_propermission", "user,org,exclusionproj,roles,issubordinate,enable", new QFilter[] { userFilter, enableFilter });
        final Set<Object> exluProjs = new HashSet<Object>(16);
        Set<Object> addtionRolsesIds = new HashSet<Object>(16);
        final Set<Long> allSubChargeOrgIds = new HashSet<Long>(16);
        final Map<Long, Set<Object>> orgRoles = new HashMap<Long, Set<Object>>(16);
        if (additionUser.length > 0) {
            for (final DynamicObject o : additionUser) {
                final Object org = o.getDynamicObject("org").getPkValue();
                final boolean isSubordinate = o.getBoolean("issubordinate");
                List<Long> allSubordinateOrgs = new ArrayList<Long>(10);
                if (isSubordinate) {
                    final List<Long> orgList = new ArrayList<Long>(10);
                    orgList.add(Long.valueOf(org.toString()));
                    allSubordinateOrgs = (List<Long>)OrgServiceHelper.getAllSubordinateOrgs((List)orgList, true);
                    allSubChargeOrgIds.addAll(allSubordinateOrgs);
                }
                else {
                    final Long orgLong = Long.valueOf(org.toString());
                    allSubordinateOrgs.add(orgLong);
                    allSubChargeOrgIds.add(orgLong);
                }
                final DynamicObjectCollection multiObjs = o.getDynamicObjectCollection("exclusionproj");
                final Set<Object> excluprojIds = multiObjs.stream().map(pro -> pro.getDynamicObject("fbasedataId").getPkValue()).collect((Collector<? super Object, ?, Set<Object>>)Collectors.toSet());
                final DynamicObjectCollection mutiRoleObjs = o.getDynamicObjectCollection("roles");
                addtionRolsesIds = mutiRoleObjs.stream().map(pro -> pro.getDynamicObject("fbasedataId").getPkValue()).collect((Collector<? super Object, ?, Set<Object>>)Collectors.toSet());
                exluProjs.addAll(excluprojIds);
                for (final Long l : allSubordinateOrgs) {
                    orgRoles.put(l, addtionRolsesIds);
                }
            }
            final QFilter orgFilter1 = new QFilter("department", "in", (Object)allSubChargeOrgIds);
            orgFilter1.and("pmascreateorg", "in", (Object)orgIdList);
            final QFilter excluFilter1 = new QFilter("id", "not in", (Object)exluProjs);
            excluFilter1.and("status", "=", (Object)StatusEnum.CHECKED.getValue());
            excluFilter1.and("enable", "=", (Object)EnableEnum.ENABLE.getValue());
            final DynamicObject[] bdProjects = BusinessDataServiceHelper.load("bd_project", "id,name,department", new QFilter[] { orgFilter1, excluFilter1 });
            final int size = bdProjects.length;
            for (final DynamicObject bdProj : bdProjects) {
                final Long depOrgId = (Long)bdProj.getDynamicObject("department").getPkValue();
                final Object projId = bdProj.getPkValue();
                final Set<Object> orgRoleIds = orgRoles.get(depOrgId);
                Set<Object> projRoleIds = projRolesMap.get(projId);
                if (projRoleIds == null) {
                    projRoleIds = new HashSet<Object>(size);
                    projRolesMap.put(projId, projRoleIds);
                }
                else {
                    projRoleIds.clear();
                }
                projRoleIds.addAll(orgRoleIds);
            }
        }
        return projRolesMap;
    }
    
    private static Object getPermNum(final String entityNum, final String operateNum) {
        final Map<String, Object> permObj = getPermObj(entityNum, operateNum);
        return permObj.get("permNumber");
    }
    
    public static Map<String, Object> getPermObj(final String entityNum, final String operateNum) {
        final Map<String, Object> map = new HashMap<String, Object>();
        final List<Map<String, Object>> dataEntityOperate = (List<Map<String, Object>>)EntityMetadataCache.getDataEntityOperate(entityNum);
        for (final Map<String, Object> operate : dataEntityOperate) {
            final String operateKey = (String) operate.get("key");
            if (operateKey.equals(operateNum)) {
                final String permItemPk = (String) operate.get("permission");
                if (permItemPk != null) {
                    final DynamicObject object = BusinessDataServiceHelper.loadSingle((Object)permItemPk, "perm_permitem");
                    final String permNumber = object.getString("number");
                    final String permName = object.getString("name");
                    map.put("id", permItemPk);
                    map.put("permNumber", permNumber);
                    map.put("permName", permName);
                    break;
                }
                break;
            }
        }
        return map;
    }
    
    private static HashSet<Object> getNoPermRoles(final String entityNum, final HashSet<Object> allRolesId, final String permNumber) {
        final HashSet<Object> hasPermRoles = new HashSet<Object>(16);
        for (final Object id : allRolesId) {
            final DynamicObject[] rightPermDataByRoleId;
            final DynamicObject[] rightPermData = rightPermDataByRoleId = PermissionServiceHelper.getRightPermDataByRoleId(id.toString());
        Label_0205:
            for (final DynamicObject obj : rightPermDataByRoleId) {
                final DynamicObjectCollection roleperms = obj.getDynamicObjectCollection("roleperm");
                if (roleperms != null) {
                    for (final DynamicObject perm : roleperms) {
                        final DynamicObject entity = perm.getDynamicObject("entity");
                        final DynamicObject perItem = perm.getDynamicObject("permitem");
                        String entityNumber = "";
                        String perItemNumber = "";
                        if (entity != null) {
                            entityNumber = entity.getString("number");
                        }
                        if (perItem != null) {
                            perItemNumber = perItem.getString("number");
                        }
                        if (entityNumber.equals(entityNum) && perItemNumber.equals(permNumber)) {
                            hasPermRoles.add(id);
                            break Label_0205;
                        }
                    }
                }
            }
        }
        final HashSet<Object> noPermRoles = new HashSet<Object>(16);
        noPermRoles.addAll(allRolesId);
        noPermRoles.removeAll(hasPermRoles);
        return noPermRoles;
    }
    
    private static Set<Long> getChargerOrgIds(final String userId) {
        final List<Long> userids = new ArrayList<Long>(10);
        userids.add(Long.valueOf(userId));
        final List position = UserServiceHelper.getPosition((List)userids);
        final Set<Long> charOrgIds = new HashSet<Long>(16);
        for (final Object o : position) {
            final List<HashMap> entries = (List<HashMap>)((HashMap)o).get("entryentity");
            if (entries != null) {
                for (final HashMap map : entries) {
                    final Boolean isincharge = (Boolean) map.get("isincharge");
                    final DynamicObject org = (DynamicObject) map.get("dpt");
                    if (org != null && isincharge) {
                        final List<Long> allSubordinateOrgs = (List<Long>)OrgServiceHelper.getAllSubordinateOrgs("01", (List)Stream.of(Long.valueOf(org.getPkValue().toString())).collect(Collectors.toList()), true);
                        charOrgIds.addAll(allSubordinateOrgs);
                    }
                }
            }
        }
        return charOrgIds;
    }
}

