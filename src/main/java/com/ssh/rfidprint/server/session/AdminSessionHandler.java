/**
 * @fileName AdminSessionHandler.java 
 * @author sam
 * @version 2014-10-21 ����10:05:02
 */
package com.ssh.rfidprint.server.session;

import org.apache.mina.core.session.IoSession;

import com.ssh.protocol.session.Session;
import com.ssh.protocol.session.SessionHandler;
import com.ssh.protocol.session.SessionRegistry;

public class AdminSessionHandler extends SessionHandler{
    /** 心跳包内容 */  
    private static final String HEARTBEATRESPONSE = "0×12"; 
    @Override
    public Session createSession(IoSession session) {
        AdminSession ret = new AdminSession(session);
        return ret;
    }

    public AdminSessionHandler(SessionRegistry paramSessionRegistry) {
        super(paramSessionRegistry);
    }

    @Override
    public void messageReceived(IoSession session, Object msg) throws Exception {
        if (msg.equals(HEARTBEATRESPONSE))  {
            return;
        }
        super.messageReceived(session, msg);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable arg1) throws Exception {
        super.exceptionCaught(session, arg1);
        
    }

    public void inputClosed(IoSession arg0) throws Exception {
        
    }
}

 