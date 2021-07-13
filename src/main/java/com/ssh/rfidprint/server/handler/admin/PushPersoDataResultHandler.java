/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName PushPersoDataResultHandler.java
 * @author sam
 */
package com.ssh.rfidprint.server.handler.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ssh.empire.protocol.data.admin.PushPersoDataResult;
import com.ssh.protocol.data.AbstractData;
import com.ssh.protocol.handler.IDataHandler;

/**
 * 客户端返回制卡数据接收成功或者失败信息
 * @ClassName: PushPersoDataResultHandler
 * @author sam
 * @version 1.0<br />
 * @Date 2016年3月29日 下午4:17:10<br />
 * @Logs <br />
 * ****************************************************<br />
 */
public class PushPersoDataResultHandler  implements IDataHandler{
    private Logger logger = LoggerFactory.getLogger(PushPersoDataResultHandler.class);

    @Override
    public void handle(AbstractData data) throws Exception {
        try{
            PushPersoDataResult result = (PushPersoDataResult)data;
//            logger.info(result.getPersoId().toString());
//            IPersoRecordService persoRecordService = ServiceManager.getManager().getBean("persoRecordServiceImpl", PersoRecordServiceImpl.class);
//            IPersoRecordLogService  persoRecordLogService  = ServiceManager.getManager().getBean("persoRecordLogServiceImpl", PersoRecordLogServiceImpl.class);
//            PersoRecord persoRecord = persoRecordService.get(result.getPersoId());
//            if(persoRecord != null){
//                if(result.isSuccess()){
//                    persoRecord.setPersoStatus("311");
//                }else{
//                    persoRecord.setPersoStatus("312");
//                }
//                //更新制卡数据状态
//                persoRecordService.saveOrUpdate(persoRecord);
//                //更新制卡日志状态
//                //<code> </code>
//               List<PersoRecordLog> list = persoRecordLogService.getList("cardNo", persoRecord.getPersoInfo().getCardNo());
//                if(list != null && list.size() > 0){
//                    PersoRecordLog persoRecordLog = list.get(list.size() -1);
//                    if(result.isSuccess()){
//                        persoRecordLog.setSubmitStatus("311");
//                    }else{
//                        persoRecordLog.setSubmitStatus("312");
//                        persoRecordLog.setSubmitDesc(result.getMessage());
//                    }
//                    persoRecordLog.setSubmitDate(DateUtil.nowTimestamp());
//                    persoRecordLogService.saveOrUpdate(persoRecordLog);
//                }
//            }
            
        }catch(Exception ex){
            logger.error(ex.getMessage(),ex);
            ex.printStackTrace();
        }
    }
}

 