/*
 * To change this license header, choose License Headers in Project Properties. To change this template file, choose Tools | Templates and open the template in the editor.
 */
package com.ssh.rfidprint.server;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ssh.rfidprint.server.session.AdminSession;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * @Title CacheUtil
 * @Description (用一句话描述该文件做什么)
 * @author sam
 * @date 2019-12-12 9:34:12
 */
public class CacheUtil {
    private CacheManager                            cacheManager = null;
    private Cache                                   cache        = null;
    private ConcurrentHashMap<String, AdminSession> connectMap   = new ConcurrentHashMap<>();
    private AtomicInteger                           ids          = new AtomicInteger(1);
    // 静态内部类，实现单例
    private static class CacheUtilHolder {
        private static final CacheUtil instance = new CacheUtil();
    }

    private CacheUtil() {
        init();
    }

    private void init() {
        cacheManager = CacheManager.create();
        cache = cacheManager.getCache("kd.bos.pushdata.serverCache");
    }

    // public AdminSession getSession(int sessionId){
    // return connectMap.get(sessionId);
    // }
    //
    // public void removeConnect(AdminSession session) {
    // synchronized (this) {
    // connectMap.remove(session.getId());
    // }
    // }
    /**
     * 获取CacheUtil实例 *
     * 
     * @return CacheUtil
     */
    public static final CacheUtil getManager() {
        return CacheUtilHolder.instance;
    }

    public synchronized String getCardNoCache(String key) {
        Element element = cache.get(key);
        if (element != null) {
            return (String) element.getObjectValue();
        }
        return null;
    }

    public synchronized void addCardNoCache(String key) {
        Element element = new Element(key, key);
        cache.put(element);
    }

    public synchronized void removeCardNoCache(String key) {
        cache.remove(key);
    }

    /**
     * 将客户端连接添加到Map缓存中
     * 
     * @Title: addConnection
     * @param deviceId
     * @param session
     */
    public synchronized void addConnection(String deviceId, AdminSession session) {
        int id = ids.incrementAndGet();
        session.setId(id);
        session.setDeviceId(deviceId);
        connectMap.put(deviceId, session);
        System.out.println(connectMap);
    }

    public synchronized AdminSession getSession(String deviceId) {
        return connectMap.get(deviceId);
    }

    public void removeConnect(AdminSession session) {
        synchronized (this) {
            connectMap.remove(session.getDeviceId());
            System.out.println(connectMap);
        }
    }
}
