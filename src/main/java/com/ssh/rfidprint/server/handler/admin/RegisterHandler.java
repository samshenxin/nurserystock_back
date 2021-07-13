/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName RegisterHandler.java
 * @author sam
 */
package com.ssh.rfidprint.server.handler.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ssh.empire.protocol.data.admin.Register;
import com.ssh.empire.protocol.data.admin.RegisterResult;
import com.ssh.protocol.data.AbstractData;
import com.ssh.protocol.handler.IDataHandler;
import com.ssh.rfidprint.server.CacheUtil;
import com.ssh.rfidprint.server.session.AdminSession;
/**
 * 客户端注册 协议 - 服务端Handler类
 * @ClassName: RegisterHandler
 * @author sam
 * @version 1.0<br />
 * @Date 2016年3月23日 上午9:30:50<br />
 * @Logs <br />
 * ****************************************************<br />
 */
public class RegisterHandler implements IDataHandler{
    private Logger logger = LoggerFactory.getLogger(RegisterHandler.class);

    public void handle(AbstractData data) throws Exception {
        Register register = (Register) data;
        AdminSession session = (AdminSession) data.getHandlerSource();
        RegisterResult result = new RegisterResult();
//        IClientLoginService clientService = ServiceManager.getManager().getBean("clientLoginServiceImpl", ClientLoginServiceImpl.class);
//        IClientInfoService  clientInfoService = ServiceManager.getManager().getBean("clientInfoServiceImpl", ClientInfoServiceImpl.class);
        try{
            CacheUtil.getManager().addConnection(register.getDeviceId(), session);
            result.setMsg("注册成功");
            //添加客户端日志
//            clientService.saveClientLogin(register.getDeviceId(), register.getBranchId(), Global.LOGIN_STATUS_SUCCESS, "注册", "", session.getClientHost());
            //更新客户端状态
//            clientInfoService.updateClientInfo(register.getDeviceId(), register.getBranchId(), Global.CLIENT_STATUS_ONLINE,session.getClientHost());
            
        }catch(Exception ex){
            logger.error(ex.getMessage(), ex);
            ex.printStackTrace();
            result.setMsg("注册异常，请重试");
//            clientService.saveClientLogin(register.getDeviceId(), register.getBranchId(), Global.LOGIN_STATUS_FAIL, "注册", ex.getMessage(), session.getClientHost());
        }finally{
            session.write(result); 
        }
    }
}

 