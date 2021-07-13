/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName ConfigUtil.java
 * @author sam
 */
package com.ssh.rfidprint.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Config.properties配置文件工具类，读取配置文件中信息
 * @ClassName: ConfigUtil
 * @author sam
 * @version 1.0<br />
 * @Date 2018年12月30日 下午2:43:26<br />
 * @Logs <br />
 * ****************************************************<br />
 */
public class ConfigUtil {
    private static Properties props = new Properties(); 
    static{
        try {
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");  
            BufferedReader bf = new BufferedReader(new    InputStreamReader(inputStream));  
            props.load(bf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String getValue(String key){
        return props.getProperty(key);
    }
}

 