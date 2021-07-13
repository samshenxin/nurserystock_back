package kd.bos.asset.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;

public class FaAssetTypeUtils
{
    private static final long EMPTYID = -9999999L;
    
    public static Map<Long, Long> getAssetTypeMap(DynamicObject[] categroyArr) {
        if (categroyArr == null || categroyArr.length == 0) {
            categroyArr = BusinessDataServiceHelper.load("fa_assetcategory", "id,parent", (QFilter[])null);
        }
        final Map<Long, Long> assetTypeMap = new HashMap<Long, Long>(categroyArr.length);
        for (int length = categroyArr.length, i = 0; i < length; ++i) {
            final Long id = categroyArr[i].getLong("id");
            Long parent = categroyArr[i].getLong("parent.id");
            if (parent == null || parent == 0L) {
                parent = 0L;
            }
            assetTypeMap.put(id, parent);
        }
        return assetTypeMap;
    }
    
    public static Map<Long, List<Object>> getSubAssetTypeMap() {
        return getSubAssetTypeMap(null);
    }
    
    public static Map<Long, List<Object>> getSubAssetTypeMap(final DynamicObject[] categroys) {
        final Map<Long, List<Object>> result = new HashMap<Long, List<Object>>();
        final Map<Long, Long> assetTypeMap = getAssetTypeMap(categroys);
        for (final Map.Entry<Long, Long> entry : assetTypeMap.entrySet()) {
            Long curKey = entry.getValue();
            final Long curValue = entry.getKey();
            if (curKey == 0L) {
                curKey = curValue;
            }
            if (result.containsKey(curKey)) {
                result.get(curKey).add(curValue);
            }
            else {
                final List<Object> childs = new ArrayList<Object>();
                childs.add(curValue);
                result.put(curKey, childs);
            }
            if (!result.containsKey(curValue)) {
                final List<Object> childs = new ArrayList<Object>();
                childs.add(curValue);
                result.put(curValue, childs);
            }
            else {
                if (result.get(curValue).contains(curValue)) {
                    continue;
                }
                result.get(curValue).add(curValue);
            }
        }
        return result;
    }
    
    public static List<Object> getAllSubAssetTypes(final List<Object> ids) {
    	DynamicObject[] categroys = null;
        return getAllSubAssetTypes(categroys, ids);
    }
    
    public static List<Object> getAllSubAssetTypes(final DynamicObject[] categroys, final List<Object> ids) {
        final List<Object> result = new ArrayList<Object>();
        final Map<Long, List<Object>> subAssetTypeMap = getSubAssetTypeMap(categroys);
        for (final Object id : ids) {
            if (subAssetTypeMap.containsKey(id)) {
                final List<Object> curAllSubChilds = getAllSubAssetTypes(subAssetTypeMap, id);
                result.addAll(curAllSubChilds);
            }
        }
        return result;
    }
    
    public static List<Object> getAllSubAssetTypes(final Map<Long, List<Object>> assetTypes, final Object id) {
        List<Object> result = new ArrayList<Object>();
        final Queue<Object> queue = new LinkedList<Object>();
        result.add(id);
        final List<Object> firstChilds = assetTypes.get(id);
        if (firstChilds == null) {
            return result;
        }
        queue.addAll((Collection<? extends Object>)firstChilds);
        result.addAll(firstChilds);
        while (!queue.isEmpty()) {
            final Object curId = queue.poll();
            List<Object> curChilds = assetTypes.get(curId);
            curChilds = removeParent(curChilds, curId);
            if (curChilds == null) {
                continue;
            }
            queue.addAll((Collection<? extends Object>)curChilds);
            result.addAll(curChilds);
        }
        result = result.stream().distinct().collect((Collector<? super Object, ?, List<Object>>)Collectors.toList());
        return result;
    }
    
    static List<Object> removeParent(final List<Object> childs, final Object curId) {
        if (childs == null || childs.size() == 0) {
            return new ArrayList<Object>();
        }
        return childs.stream().filter(id -> !id.equals(curId)).collect((Collector<? super Object, ?, List<Object>>)Collectors.toList());
    }
}

