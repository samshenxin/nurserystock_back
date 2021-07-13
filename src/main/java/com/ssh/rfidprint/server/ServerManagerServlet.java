/**
 * Copyright @ 2015  Goldpac  Co. Ltd. 
 * All right reserved. 
 * @fileName ServerManagerServlet.java 
 * @author sam
 */
package com.ssh.rfidprint.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import kd.bos.debug.mservice.Server;

public class ServerManagerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor of the object.
     */
    public ServerManagerServlet() {
        super();
    }

    /**
     * Destruction of the servlet. <br>
     */
    public void destroy() {
        super.destroy(); // Just puts "destroy" string in log
        // Put your code here
    }

    /**
     * The doGet method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to get.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter out = response.getWriter();
        String strAction = request.getParameter("action");
        if(strAction != null && strAction !=""){
            try {
                doAction(strAction ,response);
            } catch (Exception e) {
                e.printStackTrace();
                
            }
        }
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.println("<HTML>");
        out.println("  <HEAD><TITLE>Manager</TITLE></HEAD>");
        out.println("  <BODY>");
        out.println(" <table cellspacing=\"4\" border=\"0\" width=100%> ");
        out.println("<tr>");
        out.println("<td class='page-title' bordercolor='#000000' align='center' nowrap=''>");
        out.println("<font size='+2'> Application Manager</font>");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<br />");
        out.println("<table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=100%>");
        out.println("<tr>");
        out.println(" <td colspan=\"6\" class=\"title\">Applications</td>");
        out.println("</tr>");
        out.println("<tr>");
        out.println("<td class=\"header-center\"><small>Display Name</small></td>");
        out.println("<td class=\"header-center\"><small>Running</small></td>");
        out.println(" <td class=\"header-left\"><small>Commands</small></td>");
        out.println("</tr>");
        
        out.println("<tr>");
        out.println("<td class=\"header-center\"><small>打印服务平台</small></td>");
        out.println("<td class=\"header-center\"><small>");
        if(Server.instance.isStarted()){
            out.print("<font color=green> true </font>");
            out.print(" </small></td>");
            out.println(" <td class=\"header-left\"><small><a href='/manager?action=shutdown'>关闭</a></small></td>");
        }else{
            out.print("<font color=red> false </font>"); 
            out.print(" </small></td>");
            out.println(" <td class=\"header-left\"><small><a href='/manager?action=start'>启动</a></small></td>");
        }
        out.println("</tr>");
        
        out.println("</table>");
        out.println("<br />");
        Runtime imp = Runtime.getRuntime();
        out.println(" <table border=\"1\" cellspacing=\"0\" cellpadding=\"3\" width=100%> ");
        out.println("<tr>");
        out.println(" <td colspan=\"6\" class=\"title\">JVM</td>");
        out.println("</tr>");
       
        out.println("<tr>");
        out.println("<td align='left' colspan=\"6\">");
        out.println("  Max Memory: "+imp.maxMemory()/1024/1024 +" M &nbsp;&nbsp;");
        out.println("  Total Memory: "+imp.totalMemory() /1024/1024 +" M  &nbsp;");
        out.println("  Free Memory: "+imp.freeMemory()/1024 +" K &nbsp;");
        out.println("</td>");
        out.println("</tr>");
        out.println("</table>");
        
        out.println("  </BODY>");
        out.println("</HTML>");
        out.flush();
        out.close();
    }

    private void doAction(String strAction, HttpServletResponse response) throws Exception {
        if(strAction == "shutdown" || "shutdown".equals(strAction)){
            if(!Server.instance.isStarted()){
                return;
            }
            Server.instance.shutdown();
        }else if(strAction == "start" || "start".equals(strAction)){
            if(Server.instance.isStarted()){
                return;
            }
            Server.instance.launch();
        }
    }

    /**
     * The doPost method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to post.
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        out.println("<HTML>");
        out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
        out.println("  <BODY>");
        out.print("    This is ");
        out.print(this.getClass());
        out.println(", using the POST method");
        out.println("  </BODY>");
        out.println("</HTML>");
        out.flush();
        out.close();
    }

    /**
     * Initialization of the servlet. <br>
     *
     * @throws ServletException if an error occurs
     */
    public void init() throws ServletException {
        // Put your code here
    }
}

 