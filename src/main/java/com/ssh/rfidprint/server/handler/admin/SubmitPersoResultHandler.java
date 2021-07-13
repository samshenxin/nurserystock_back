/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName SubmitRersoResultHandler.java
 * @author sam
 */
package com.ssh.rfidprint.server.handler.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ssh.empire.protocol.data.admin.SubmitPersoResult;
import com.ssh.protocol.data.AbstractData;
import com.ssh.protocol.handler.IDataHandler;

/**
 * 客户端提交制卡结果协议处理类
 * @ClassName: SubmitRersoResultHandler
 * @author sam
 * @version 1.0<br />
 * @Date 2016年3月31日 下午4:19:17<br />
 * @Logs <br />
 * ****************************************************<br />
 */
public class SubmitPersoResultHandler  implements IDataHandler{
    private Logger logger = LoggerFactory.getLogger(SubmitPersoResultHandler.class);
    @Override
    public void handle(AbstractData data) throws Exception {
        try{
            SubmitPersoResult result = (SubmitPersoResult)data;
            logger.info("提交结果："+result.getCode());
//            IPersoRecordService persoRecordService = ServiceManager.getManager().getBean("persoRecordServiceImpl", PersoRecordServiceImpl.class);
//            IPersoRecordLogService  persoRecordLogService  = ServiceManager.getManager().getBean("persoRecordLogServiceImpl", PersoRecordLogServiceImpl.class);
//            PersoRecord persoRecord = persoRecordService.get(result.getPersoId());
//            MakeCardService makeCardService = new MakeCardService();
            
//            if(persoRecord != null){
//                if(result.isSuccess()){
//                    persoRecord.setPersoStatus("301");
////                    makeCardService.jobFinished(persoRecord.getPersoInfo().getBatchNum(), 0);
//                }else{
//                    persoRecord.setPersoStatus(result.getCode());
//                    persoRecord.setPersoStatusDesc(result.getMessage());
////                    makeCardService.jobFinished(persoRecord.getPersoInfo().getBatchNum(), 1);
//                }
//                persoRecord.setPersoDate(DateUtil.nowTimestamp());
//                //更新制卡数据状态
//                persoRecordService.saveOrUpdate(persoRecord);
//                persoRecordService.flush();
//                //更新制卡日志状态
//               List<PersoRecordLog> list = persoRecordLogService.getList("cardNo", persoRecord.getPersoInfo().getCardNo());
//                if(list != null && list.size() > 0){
//                    //取最后一条
//                    PersoRecordLog persoRecordLog = list.get(list.size() -1);
//                    if(result.isSuccess()){
//                        persoRecordLog.setBackStatus("301");
//                    }else{
//                        persoRecordLog.setBackStatus(result.getCode());
//                        persoRecordLog.setBackDesc(result.getMessage());
//                    }
//                    persoRecordLog.setBackDate(DateUtil.nowTimestamp());
//                    persoRecordLogService.saveOrUpdate(persoRecordLog);
//                }
//            }
        }catch(Exception ex){
            logger.error(ex.getMessage() , ex);
            ex.printStackTrace();
        }
    }
}

 