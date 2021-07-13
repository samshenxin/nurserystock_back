/*
 * Copyright @ 2014 Goldpac Co. Ltd. All right reserved.
 * @fileName Global.java
 * @author sam
 */
package com.ssh.rfidprint.common;
public class Global {
    /**
     * 最大返回记录数 10
     */
    public static final int     MAX_RESULTS               = 10;
    /** 删除标识： 0 未删除状态 */
    public static Integer       DELSIGN_NOT_DELETED       = 0;
    /** 删除标识： 1 已删除状态 */
    public static Integer       DELSIGN_HAS_DELETED       = 1;
    /** 1： 已授权审核标识 */
    public static Integer       FLAG_HAS_AUDIT            = 1;
    /** 100 : 初使状态码 */
    public static String        PERSO_CODE_INIT           = "100";
    /** 200 : 数据导入成功 */
    public static String        INPUT_CODE_SUCCESS        = "200";
    /** 400 : 提交成功 */
    public static String        PERSO_CODE_SUBMIT_SUCCESS = "400";
    /** 401 : 提交失败 */
    public static String        PERSO_CODE_SUBMIT_FAIL    = "401";
    /** 300 : 个人化制卡中 */
    public static String        PERSO_CODE_PRINTING       = "300";
    /** 301 : 个人化制卡成功 */
    public static String        PERSO_CODE_SUCCESS        = "301";
    /** 302 : 个人化制卡失败 */
    public static String        PERSO_CODE_FAIL           = "302";
    /** 399 ： 个人化状态，失效 */
    public static String        PERSO_CODE_INVALID        = "399";
    /** 任务调度的参数key */
    public static final String  JOB_PARAM_KEY             = "jobParam";
    /** 监控类型 1 本地目录 */
    public static final Integer MONIT_TYPE_LOCAL          = 1;
    /** 监控类型 2 FTP目录 */
    public static final Integer MONIT_TPYE_FTP            = 2;
    /** 文件列表 */
    public static final String  GLOBAL_SERVER_FILE_LIST   = "fileList";
    /** 默认次数 0 */
    public static final Integer DEFAUL_NUMBER             = 0;
    /** 磁条文件目录 */
    public static final String  FILE_MAGNETIC_DIRECTORY   = "cpsfile";
    /** IC 文件目录 */
    public static final String  FILE_IC_DIRECTORY         = "icrush";
    /** 本地文件目录 */
    public static final String  FILE_LOCAL_DIRECTORY      = "tmp";
    /** 1 登陆成功 */
    public static final Integer LOGIN_STATUS_SUCCESS      = 1;
    /** 0 登陆失败 */
    public static final Integer LOGIN_STATUS_FAIL         = 0;
    /** 1 登陆成功 */
    public static final String  LOGIN_STATUS_SUCCESS_DESC = "成功";
    /** 0 登陆失败 */
    public static final String  LOGIN_STATUS_FAIL_DESC    = "失败";
    /** 客户端状态 1 在线 */
    public static final Integer CLIENT_STATUS_ONLINE      = 1;
    /** 客户端状态 0 离线 */
    public static final Integer CLIENT_STATUS_OFFLINE     = 0;
    /** 客户端当前版本号 */
    public static final String  KEY_CLIENT_VERSION        = "key_client_version_";
    /** 客户端更新URL */
    public static final String  KEY_CLIENT_UPDATE_URL     = "key_client_update_url_";
    /** 默认图片文件名 */
    public static final String  DEFAULT_IMG_FILE_NAME     = "img_default.png";
}
