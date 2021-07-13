/*
 * Copyright @ 2015 Goldpac Co. Ltd. All right reserved.
 * @fileName CheckUpdateHandler.java
 * @author sam
 */
package com.ssh.rfidprint.server.handler.admin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ssh.empire.protocol.data.admin.CheckUpdate;
import com.ssh.empire.protocol.data.admin.CheckUpdateResult;
import com.ssh.protocol.data.AbstractData;
import com.ssh.protocol.handler.IDataHandler;
import com.ssh.rfidprint.server.session.AdminSession;
/**
 * 客户端自动更新协议处理类
 * 
 * @ClassName: CheckUpdateHandler
 * @author sam
 * @version 1.0<br />
 * @Date 2016年3月31日 上午10:34:56<br />
 * @Logs <br />
 *       ****************************************************<br />
 */
public class CheckUpdateHandler implements IDataHandler {
    private Logger logger = LoggerFactory.getLogger(CheckUpdateHandler.class);

    @Override
    public void handle(AbstractData data) throws Exception {
        CheckUpdate checkUpdate = (CheckUpdate) data;
        CheckUpdateResult result = new CheckUpdateResult();
        AdminSession session = (AdminSession) data.getHandlerSource();
        logger.info("客户端更新："+result);
        try {
            //从数据库中读取当前版本号信息
//            IParamService paramService = ServiceManager.getManager().getBean("paramServiceImpl", ParamServiceImpl.class);
//            Param param = paramService.get("PKey", Global.KEY_CLIENT_VERSION);
//            if (param == null) {
//                return;
//            }
//            int diff = compareVersion(param.getPValue() , checkUpdate.getVersion() );
//            if(diff > 0){
//                result.setUpdateFlag(true);
//                //从数据库中读取客户端更新文件下载地址
//                param = paramService.get("PKey", Global.KEY_CLIENT_UPDATE_URL);
//                result.setCurVersion(param.getPValue());
//                result.setUpdateUrl(param.getPValue());
//                session.write(result);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage(), ex);
        }
    }
    /** 
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0 
     * @param version1 
     * @param version2 
     * @return 
     */  
    public int compareVersion(String version1, String version2) throws Exception {  
        if (version1 == null || version2 == null) {  
            throw new Exception("compareVersion error:illegal params.");  
        }  
        String[] versionArray1 = version1.split("\\.");//注意此处为正则匹配，不能用"."；  
        String[] versionArray2 = version2.split("\\.");  
        int idx = 0;  
        int minLength = Math.min(versionArray1.length, versionArray2.length);//取最小长度值  
        int diff = 0;  
        while (idx < minLength  
                && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0//先比较长度  
                && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {//再比较字符  
            ++idx;  
        }  
        //如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；  
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;  
        return diff;  
    }  
   
}
