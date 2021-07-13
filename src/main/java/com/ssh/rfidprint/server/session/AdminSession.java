/**
 * @fileName AdminSession.java
 * @author sam
 * @version 2014-10-21 ����10:14:25
 */
package com.ssh.rfidprint.server.session;
import java.net.InetSocketAddress;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.ssh.rfidprint.server.CacheUtil;

public class AdminSession extends ConnectSession {
    public AdminSession(IoSession session) {
        super(session);
    }

    @Override
    public void closed() {
        // 与客户端连接断开后，从缓存中删除当前连接
        CacheUtil.getManager().removeConnect(this);
        // 更新客户端状态
//        IClientInfoService clientInfoService = ServiceManager.getManager().getBean("clientInfoServiceImpl", ClientInfoServiceImpl.class);
//        clientInfoService.updateClientInfo(this.getDeviceId(), "", Global.CLIENT_STATUS_OFFLINE, this.getClientHost());
    }

    @Override
    public void created() {
        String clientIP = ((InetSocketAddress) this.getIoSession().getRemoteAddress()).getAddress().getHostAddress();
        setClientHost(clientIP);
    }

    @Override
    public <T> void handle(T packet) {
    }

    @Override
    public void idle(IdleStatus status) {
    }

    @Override
    public void opened() {
    }
}
