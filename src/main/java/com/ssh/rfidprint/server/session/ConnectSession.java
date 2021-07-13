/**
 * @fileName ConnectSession.java 
 * @author sam
 * @version 2014-10-21 ����10:14:49
 */
package com.ssh.rfidprint.server.session;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.ssh.protocol.session.Session;


public class ConnectSession extends Session {
    /** 连接ID号*/
    private int                                 id;
    /** 设备号 */
    private String                             deviceId;
    /** 客户端IP */
    private String                             clientHost;
    
    
    /**
     * 设置连接管理ID号
     * @param id    连接管理ID号
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取连接管理ID号
     * @return  连接管理ID号
     */
    public int getId() {
        return this.id;
    }
    
    /**
    * ���캯���ʼ���Ựֵ
    * @param session   �Ựֵ
    */
   public ConnectSession(IoSession session) {
       super(session);
   }

    @Override
    public <T> void handle(T paramT) {
        
        // TODO Auto-generated method stub 
        
    }

    @Override
    public void closed() {
        
        // TODO Auto-generated method stub 
        
    }

    @Override
    public void created() {
        // TODO Auto-generated method stub 
    }

    @Override
    public void opened() {
        
        // TODO Auto-generated method stub 
        
    }

    @Override
    public void idle(IdleStatus paramIdleStatus) {
        
        
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getClientHost() {
        return clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }
    

    
}

 