package kd.bos.debug.mservice;


import java.io.IOException;

import com.ssh.rfidprint.server.ConfigUtil;

import kd.bos.config.client.util.ConfigUtils;
import kd.bos.db.DB;
import kd.bos.db.SqlLogger;
import kd.bos.service.webserver.JettyServer;
import oadd.org.apache.commons.lang3.StringUtils;

public class DebugServer {

	public static Server        instance      = null;
	public static void main(String[] args) throws Exception {
		System.setProperty(ConfigUtils.APP_NAME_KEY, "mservice-biz1.5-cosmic");

		// 设置集群环境名称和配置服务器地址
		System.setProperty(ConfigUtils.CLUSTER_NAME_KEY, "cosmic");
		System.setProperty(ConfigUtils.CONFIG_URL_KEY, "127.0.0.1:2181");
		System.setProperty("configAppName", "mservice,web");
		System.setProperty("webmserviceinone", "true");

		System.setProperty("file.encoding", "utf-8");
		System.setProperty("xdb.enable", "false");

		System.setProperty("mq.consumer.register", "true");
		System.setProperty("MONITOR_HTTP_PORT", "9998");
		System.setProperty("JMX_HTTP_PORT", "9091");
		System.setProperty("dubbo.protocol.port", "28888");
		System.setProperty("dubbo.consumer.url", "dubbo://127.0.0.1:28888");
		System.setProperty("dubbo.consumer.url.qing", "dubbo://127.0.0.1:30880");
		System.setProperty("dubbo.registry.register", "false");
		// System.setProperty("mq.debug.queue.tag", "whb1133");
		System.setProperty("dubbo.service.lookup.local", "false");
		System.setProperty("appSplit", "false");

		System.setProperty("lightweightdeploy", "true");

		System.setProperty("db.sql.out", "false");

		System.setProperty("JETTY_WEB_PORT", "8088");
		System.setProperty("JETTY_WEBAPP_PATH", "../../../mservice-cosmic/webapp");
		System.setProperty("JETTY_WEBRES_PATH", "../../../static-file-service");

		System.setProperty("domain.contextUrl", "http://192.168.1.148:8088/ierp");
		System.setProperty("domain.contextUrl", "http://120.198.215.181:8088/ierp");
		System.setProperty("domain.tenantCode", "cosmic-simple");
		System.setProperty("tenant.code.type", "config");

		System.setProperty("fileserver", "http://192.168.1.148:8100/fileserver/");
		System.setProperty("imageServer.url", "http://192.168.1.148:8100/fileserver/");
		System.setProperty("bos.app.special.deployalone.ids", "");
		System.setProperty("mc.server.url", "http://127.0.0.1:8090/");
		
//		DB.setSqlLogger(new SqlLogger() {
//
//			@Override
//			public void log(String sql, Object... arg1) {
//				// TODO Auto-generated method stub
//				System.out.println(sql);
//			}
//		});

		try {
//          long time = System.currentTimeMillis();
          instance = new Server();
          instance.launch();
//          log.info("启动管理服务...");
          String strServerManager = ConfigUtil.getValue("serverManager");
          if (strServerManager != null && StringUtils.equals(strServerManager, "1")) {
//              System.out.println("启动管理服务...");
              Server.openManagerServlet();
          }
          Server.openGPCardService();
//          System.out.println("login time:" + ((System.currentTimeMillis() - time) / 1000) + "秒");
      } catch (Exception e) {
          e.printStackTrace();
          try {
              System.in.read();
          } catch (IOException e1) {
              e1.printStackTrace();
          }
      }
		JettyServer.main(null);
	}

}