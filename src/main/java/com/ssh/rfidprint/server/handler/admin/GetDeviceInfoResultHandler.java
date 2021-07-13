/*
 * Copyright @ 2015 Goldpac Co. Ltd. All right reserved.
 * @fileName GetDeviceInfoResultHandler.java
 * @author sam
 */
package com.ssh.rfidprint.server.handler.admin;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ssh.empire.protocol.data.admin.GetDeviceInfoResult;
import com.ssh.protocol.data.AbstractData;
import com.ssh.protocol.handler.IDataHandler;
public class GetDeviceInfoResultHandler implements IDataHandler {
    private Logger logger = LoggerFactory.getLogger(GetDeviceInfoResultHandler.class);

    @Override
    public void handle(AbstractData data) throws Exception {
//        IClientInfoService clientInfoService = ServiceManager.getManager().getBean("clientInfoServiceImpl", ClientInfoServiceImpl.class);
        GetDeviceInfoResult di = (GetDeviceInfoResult) data;
        try {
//            ClientInfo ci = clientInfoService.get("deviceId", di.getDeviceId());
//            if (ci != null) {
                Document doc = Jsoup.parse(di.getContent());
                if (doc == null) {
                    return;
                }
                Element printerSupplies = doc.getElementsByTag("PrinterSupplies2").first();
                String type = printerSupplies.getElementsByTag("PrintRibbonType").first().text();
                String value = printerSupplies.getElementsByTag("RibbonRemaining").first().text();
//                ci.setPrintRibbon(type + " [ " + value + " %]");
                type = printerSupplies.getElementsByTag("IndentRibbonType").first().text();
                value = printerSupplies.getElementsByTag("IndentRibbonRemaining").first().text();
//                ci.setIndentRibbon(type + " [ " + value + " %]");
                type = printerSupplies.getElementsByTag("TopperRibbonType").first().text();
                value = printerSupplies.getElementsByTag("TopperRibbonRemaining").first().text();
//                ci.setTopperRibbon(type + " [ " + value + " %]");
//                ci.setModifyTime(DateUtil.nowTimestamp());
//                clientInfoService.saveOrUpdate(ci);
//            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage(),ex);
        }
    }
}
