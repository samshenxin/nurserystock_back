/*
 * Copyright @ 2015 Goldpac Co. Ltd. All right reserved.
 * @fileName DownloadServlet.java
 * @author sam
 */
package com.ssh.rfidprint.server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.ssh.rfidprint.dto.UploadInfoDto;
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    /** 申明日志处理类 */
    private Logger            logger           = Logger.getLogger(UploadServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info("================上传请求===================");
        PrintWriter out = resp.getWriter();
        try {
            String json = getPostParameter(req);
            UploadInfoDto dto = JSON.parseObject(json, UploadInfoDto.class);
//            IPersoRecordService persoRecordService = ServiceManager.getManager().getBean("persoRecordServiceImpl", PersoRecordServiceImpl.class);
//            persoRecordService.saveRecordByDto(dto);
            
            resp.setContentType("text/html;charset=utf-8");
            out.write("satus:OK");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage(), ex);
        } finally {
            out.flush();
            out.close();
        }
    }

    /**
     * 根据request获取Post参数
     * 
     * @param request
     * @return
     * @throws IOException
     */
    private String getPostParameter(HttpServletRequest request) throws IOException {
        BufferedReader br = null;
        StringBuffer sb = new StringBuffer("");
        try {
            br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "utf-8"));
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
        } catch (IOException e) {
            throw new IOException("Parse data error!", e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
