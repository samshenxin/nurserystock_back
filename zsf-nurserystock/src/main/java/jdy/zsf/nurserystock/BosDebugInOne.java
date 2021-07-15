package jdy.zsf.nurserystock;

import kd.bos.service.webserver.JettyServer;

public class BosDebugInOne {
  public static void main(String[] args)throws Exception {
    System.setProperty("appName","mservice");
    System.setProperty("mq.debug.queue.tag", System.getenv().get("HOSTNAME"));
    System.setProperty("monitor.user", "admin");
    System.setProperty("monitor.password", "admin");
    System.setProperty("audit.enable","true");
    System.setProperty("mq.consumer.register", "true");
    System.setProperty("db.sql.out", "true");
    JettyServer.main(null);
  }
}