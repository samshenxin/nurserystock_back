/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName KeepAliveMessageFactoryImpl.java
 * @author sam
 */
package com.ssh.rfidprint.server.session;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳检测
 * @ClassName: KeepAliveMessageFactoryImpl
 * @author sam
 * @version 1.0<br />
 * @Date 2016年3月16日 上午9:03:39<br />
 * @Logs <br />
 * ****************************************************<br />
 */
public class KeepAliveMessageFactoryImpl  implements KeepAliveMessageFactory{
 
    /** 心跳包内容 */  
    private static final String HEARTBEATREQUEST = "0×11";  
    private static final String HEARTBEATRESPONSE = "0×12";  
    private static final Logger log      = LoggerFactory.getLogger(KeepAliveMessageFactoryImpl.class);
    @Override
    public Object getRequest(IoSession arg0) {
//        log.info("请求预设信息: " + HEARTBEATREQUEST);  
        /** 返回预设语句 */  
        return HEARTBEATREQUEST;  
        //被动型心跳机制无请求  因此直接返回null
//        return null;
        
    }

    @Override
    public Object getResponse(IoSession arg0, Object arg1) {
//        System.out.println("响应预设信息: " + HEARTBEATRESPONSE);
//        log.info("响应预设信息: " + HEARTBEATRESPONSE);  
        /** 返回预设语句 */  
        return HEARTBEATRESPONSE; 
    }

    @Override
    public boolean isRequest(IoSession session, Object message) {
        log.info("请求心跳包信息: " + message);  
        if (message.equals(HEARTBEATREQUEST))  
            return true;  
        return false;  
        
    }

    @Override
    public boolean isResponse(IoSession session, Object message) {
//        System.out.println("响应心跳包信息: " + message);
//        log.info("响应心跳包信息: " + message);  
        if (message.equals(HEARTBEATRESPONSE)) { 
            return true;  
        }
        return false;   
    }
}

 