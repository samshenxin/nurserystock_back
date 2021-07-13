/*
 * Copyright @ 2015 Goldpac Co. Ltd. All right reserved.
 * @fileName PersoRecordServiceImpl.java
 * @author sam
 */
package com.ssh.rfidprint.service;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.goldpac.cardbiz.error.Errors;
import com.goldpac.cardbiz.model.dto.ICData;
import com.goldpac.cardbiz.model.dto.PersoDatas;
import com.goldpac.cardbiz.model.dto.PrintWriteData;
import com.ssh.empire.protocol.data.admin.PushPersoData;
import com.ssh.rfidprint.common.Global;
import com.ssh.rfidprint.dto.PersoDetailDto;
import com.ssh.rfidprint.dto.UploadInfoDto;
import com.ssh.rfidprint.entry.FaCardRealEntry;
import com.ssh.rfidprint.entry.PersoInfo;
import com.ssh.rfidprint.entry.PersoRecord;
import com.ssh.rfidprint.server.CacheUtil;
import com.ssh.rfidprint.server.DateUtil;
import com.ssh.rfidprint.server.session.AdminSession;

import oadd.org.apache.commons.lang3.StringUtils;
import sun.misc.BASE64Encoder;
/**
 * 个人化数据记录实体包括导入状态，制卡状态Service层接口实现类
 * 
 * @ClassName: PersoRecordServiceImpl
 * @author sam
 * @date 2015年4月22日 上午10:47:46
 * @version 1.0<br />
 *          Update Logs:<br />
 *          ****************************************************<br />
 *          Date: <br />
 *          Description:<br />
 ****************************************************** <br />
 */
@Service
public class PersoRecordServiceImpl extends BaseServiceImpl<PersoRecord, Integer> implements IPersoRecordService {
    private Logger                 log = LoggerFactory.getLogger(PersoRecordServiceImpl.class);
    @Autowired
    private IPersoRecordDao        dao;
    @Autowired
    private IPersoInfoDao          persoInfoDao;
//    @Autowired
//    private IMnPersoInfoDao        mnPersoInfoDao;
//    @Autowired
//    private ICardTypeDao           cardTypeService;
//    @Autowired
//    private IPersoRecordLogService persoRecordLogService;
//    @Autowired
//    private ILogPersoRecevieDao logPersoRecevieDao;

    public IPersoRecordDao getDao() {
        return dao;
    }

    @Resource
    public void setDao(IPersoRecordDao dao) {
        this.dao = dao;
        super.setBaseDao(dao);
    }

    /**
     * 根据ID信息，获取指定数据
     * 
     * @Title: getPersoRecordById
     * @param persoRecordId
     * @return
     */
    public PersoDetailDto getPersoRecordDtoById(Integer persoRecordId) {
        PersoRecord persoRecord = dao.get(persoRecordId);
        PersoDetailDto dto = new PersoDetailDto();
        if (persoRecord != null) {
            dto.setPersoRecordId(persoRecord.getPrId());
            dto.setCardNo(persoRecord.getPersoInfo().getCardNo());
            dto.setCardTypeCode(persoRecord.getPersoInfo().getCardTypeCode());
            dto.setHoldName(persoRecord.getPersoInfo().getHoldName());
            dto.setExpiry(persoRecord.getPersoInfo().getExpiry());
            dto.setPersoStatus(persoRecord.getPersoStatusDesc());
            dto.setCardTypeImage(persoRecord.getPersoInfo().getImagePath());
            dto.setInitialData(persoRecord.getPersoInfo().getInitialData());
            dto.setIdNumber(persoRecord.getPersoInfo().getIdNumber());
            dto.setCardNoFormat(persoRecord.getPersoInfo().getCardNoFormat());
            dto.setTransformData(persoRecord.getPersoInfo().getTransformData());
            dto.setPbocDp(persoRecord.getPersoInfo().getPbocDp());
            dto.setBranchCode(persoRecord.getPersoInfo().getBranchCode());
            dto.setCvv(persoRecord.getPersoInfo().getCvv());
            dto.setMag1(persoRecord.getPersoInfo().getMag1());
            dto.setMag2(persoRecord.getPersoInfo().getMag2());
            dto.setMag3(persoRecord.getPersoInfo().getMag3());
            dto.setImagePath(persoRecord.getPersoInfo().getImagePath());
        }
        return dto;
    }

    /**
     * 根据ID，查询并返回PersoRecord实体类
     */
    public PersoRecord getPersoRecordById(Integer persoRecordId) {
        return dao.get(persoRecordId);
    }

    /**
     * 根据卡号，获取制卡数据有效的记录
     * 
     * @Title: getPersoRecordByCardNo
     * @param cardNo
     * @return
     */
    public PersoRecord getPersoRecordByCardNo(String cardNo) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(PersoRecord.class);
        detachedCriteria.createAlias("persoInfo", "p");
        detachedCriteria.add(Restrictions.eq("p.cardNo", cardNo));
        detachedCriteria.add(Restrictions.eq("persoStatus", Global.PERSO_CODE_INIT));
        try {
            List<PersoRecord> list = dao.getList(detachedCriteria);
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 保存制卡数据记录
     * 
     * @Title: savePersoRecord
     * @param persoInfo
     */
    @Override
    public void savePersoRecord(PersoInfo persoInfo) {
        // PersoInfo p = persoInfoDao.get("cardNo", persoInfo.getCardNo());
        PersoRecord persoRecord = getPersoRecordByCardNo(persoInfo.getCardNo());
//        MnPersoInfo entity = new MnPersoInfo();
//        entity.setCreateTime(DateUtil.timestampToString(DateUtil.nowTimestamp(), "yyyy-MM-dd HH:mm:ss"));
        if (persoRecord != null) {
            // 卡号重复日志，
            log.error("卡号已存在,导入失败: " + persoInfo.getCardNo());
//            entity.setLevel("ERROR ");
//            entity.setContent("卡号已存在,导入失败:　:" + persoInfo.getCardNo());
//            mnPersoInfoDao.save(entity);
            return;
        }
        // 保存新的制卡数据记录
//        Integer persoInfoId = persoInfoDao.save(persoInfo);
//        persoInfo.setPiId(persoInfoId);
        // 保存制卡数据记录实体对象
        persoRecord = new PersoRecord();
        // persoRecord.setInputDate(DateUtil.nowTimestamp());
        persoRecord.setInputDate(persoInfo.getCreateTime());
        persoRecord.setInputStatus(Global.INPUT_CODE_SUCCESS);
        persoRecord.setPersoInfo(persoInfo);
        persoRecord.setPersoStatus(Global.PERSO_CODE_INIT);
        persoRecord.setSubmitNum(Global.DEFAUL_NUMBER);
        persoRecord.setSuccessNum(Global.DEFAUL_NUMBER);
        persoRecord.setFailNum(Global.DEFAUL_NUMBER);
        dao.save(persoRecord);
        // 保存导入日志信息
        log.info("数据导入成功　:" + persoInfo.getCardNo());
//        entity.setLevel("INFO");
//        entity.setContent("数据导入成功　:" + persoInfo.getCardNo());
//        mnPersoInfoDao.save(entity);
    }

    public IPersoInfoDao getPersoInfoDao() {
        return persoInfoDao;
    }

    public void setPersoInfoDao(IPersoInfoDao persoInfoDao) {
        this.persoInfoDao = persoInfoDao;
    }

    /**
     * 根据卡号，获取制卡数据信息，并将IC数据更新到制卡记录中。
     * 
     * @Title: updatePersoInfo
     * @param dto
     */
//    @Override
//    public void updatePersoInfo(ICDataDto dto) {
//        PersoRecord persoRecord = getPersoRecordByCardNo(dto.getCardNo());
//        MnPersoInfo entity = new MnPersoInfo();
//        entity.setCreateTime(DateUtil.timestampToString(DateUtil.nowTimestamp(), "yyyy-MM-dd HH:mm:ss"));
//        if (persoRecord != null) {
//            PersoInfo persoInfo = persoRecord.getPersoInfo();
//            persoInfo.setPbocDp(dto.getPbocDp());
//            persoInfoDao.saveOrUpdate(persoInfo);
//            log.info("IC数据导入成功　:" + persoInfo.getCardNo());
//            entity.setLevel("INFO");
//            entity.setContent("IC数据匹配成功 :" + dto.getCardNo());
//        } else {
//            entity.setLevel("ERROR");
//            entity.setContent("未找到匹配记录:" + dto.getCardNo() + ",IC导入失败");
//            log.info("未找到匹配记录：" + dto.getCardNo() + ",IC导入失败");
//        }
//        mnPersoInfoDao.save(entity);
//    }
    

//    public IMnPersoInfoDao getMnPersoInfoDao() {
//        return mnPersoInfoDao;
//    }

//    public void setMnPersoInfoDao(IMnPersoInfoDao mnPersoInfoDao) {
//        this.mnPersoInfoDao = mnPersoInfoDao;
//    }

    @Override
    public void savePersoDatas(PersoDatas datas) throws Exception {
        if (datas == null) {
            throw new Exception("数据为空！");
        }
        PersoInfo persoInfo;
        try {
            if(!datas.getPrintWriteDataList().isEmpty()  && datas.getPrintWriteDataList().size() >0){
//                LogPersoRecevie lpr = new LogPersoRecevie();
//                lpr.setBatchNo(datas.getBatchNo());
//                lpr.setReceiveTime(DateUtil.nowTimestamp());
//                lpr.setRecordNum(datas.count());
//                logPersoRecevieDao.saveOrUpdate(lpr);
            }
            // 保存制卡数据信息
            for (PrintWriteData p : datas.getPrintWriteDataList()) {
                persoInfo = new PersoInfo();
                persoInfo.setBranchCode(p.getBranchCode());
                persoInfo.setCardNo(p.getCardNo());
                persoInfo.setCardNoFormat(p.getCardNoFormat());
                persoInfo.setCardTypeCode(p.getCardTypeCode());
                persoInfo.setCreateTime(DateUtil.nowTimestamp());
                // persoInfo.setCreateTime(p.getCreateDate());
                persoInfo.setCvv(p.getCvv());
                persoInfo.setExpiry(p.getExpiry());
                persoInfo.setHoldName(p.getHoldName());
                persoInfo.setIdNumber(p.getIdNumber());
                persoInfo.setInitialData(p.getInitialData());
                persoInfo.setTransformData(p.getTransformData());
                persoInfo.setPbocDp(p.getPbocDp());
                persoInfo.setMag1(p.getTrack1());
                persoInfo.setMag2(p.getTrack2());
                persoInfo.setMag3(p.getTrack3());
                persoInfo.setBatchNum(datas.getBatchNo());
                this.savePersoRecord(persoInfo);
            }
            if (datas.getIcDataList() != null) {
//                ICDataDto dto;
                for (ICData ic : datas.getIcDataList()) {
//                    dto = new ICDataDto();
//                    dto.setCardNo(ic.getCardNo());
//                    dto.setPbocDp(ic.getPbocDp());
////                    System.out.println("IC" +ic.getPbocDp());
//                    this.updatePersoInfo(dto);
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * 主动推送数据到客户端
     * 
     * @Title: pushDataToClient
     * @param persoID
     */
    @Override
    public void pushDataToClient(Integer persoID, Errors errors) {
        try {
//            PersoDetailDto dto = this.getPersoRecordDtoById(persoID);
//            //emboss|2|350|750|$cardNo$|$$|emboss|1|1455|430|$expiry$|$$|emboss|4|350|275|$holdName$|$$|emboss|2|1750|1300|$cvv$|$$|mag1|$mag1$|$$|mag2|$mag2$|$$|mag3|$mag3$
//            CardType cardType = cardTypeService.get("ctCode", dto.getCardTypeCode());
//            String strType = cardType.getCtResource();
//            //字段为空时不打印该字段卡面信息
//            boolean  flagCN = false;
//            boolean flagCNF = false;
//            boolean flagC = false;
//            boolean flagHN = false;
//            boolean flagE = false;
//            String cardNo = "" ;
//            if(StringUtils.isNotEmpty(strType)){
//                if(StringUtils.isEmpty(dto.getCardNo())){
////                    strType = strType.replace("$cardNo$", "");
//                    strType = strType.replace(strType.substring(0,strType.indexOf("$cardNo$")+11),"").trim();
//                    flagCN = true;
//                }else{
//                    cardNo = dto.getCardNo();
//                    if(cardNo.length() == 16){
//                        cardNo = dto.getCardNo().substring(0,4)+" " +dto.getCardNo().substring(4,8) +" "+dto.getCardNo().substring(8,12) + " " +dto.getCardNo().substring(12,16);
//                        strType = strType.replace("$cardNo$", cardNo);
//                    }else{
//                        strType = strType.replace("$cardNo$", cardNo);
//                    }
//                }
//                if(StringUtils.isEmpty(dto.getCardNoFormat())){
////                    strType = strType.replace("$componyName$", "");
//                    if(flagCN) {
//                    	//卡号为空,要去除"$$text"中的$$符号
//                    	strType = strType.replace(strType.substring(0,strType.indexOf("$componyName$")+16),"").trim(); 
//                    }else {
//                    	strType = strType.replace(strType.substring(strType.indexOf(cardNo)+cardNo.length(),strType.indexOf("$componyName$")+13),"").trim();                  	
//                    }
//                    flagCNF = true;
//                }else{
//                    strType = strType.replace("$componyName$", dto.getCardNoFormat());//公司名称
//
//                }
//                if(StringUtils.isEmpty(dto.getCvv())){
////                    strType = strType.replace("$chipModel$", "");
//                	if(flagCNF && flagCN) {
//                        strType = strType.replace(strType.substring(0,strType.indexOf("$chipModel$")+14),"").trim();
//                	}else if(!flagCN && flagCNF) {
//                        strType = strType.replace(strType.substring(strType.indexOf(cardNo)+cardNo.length(),strType.indexOf("$chipModel$")+11),"").trim();
//                	}else if(flagCN && !flagCNF) {
//                        strType = strType.replace(strType.substring(strType.indexOf(dto.getCardNoFormat())+dto.getCardNoFormat().length(),strType.indexOf("$chipModel$")+11),"").trim();
//                	}else {               		
//                		strType = strType.replace(strType.substring(strType.indexOf(dto.getCardNoFormat())+dto.getCardNoFormat().length(),strType.indexOf("$chipModel$")+11),"").trim();
//                	}
//                	flagC = true;
//                }else{
//                    strType = strType.replace("$chipModel$", dto.getCvv());//芯片型号
//                    
//                }
//                if(StringUtils.isEmpty(dto.getHoldName())){
////                	strType = strType.replace("$customerName$", "");
//                	if(flagCNF && flagCN && flagC) {
//                    	strType = strType.replace(strType.substring(0,strType.indexOf("$customerName$")+17),"").trim();
//                	}else if(!flagCN && flagCNF && flagC) {
//                    	strType = strType.replace(strType.substring(strType.indexOf(cardNo)+cardNo.length(),strType.indexOf("$customerName$")+14),"").trim();
//                	}else if(flagCN && !flagCNF && flagC) {
//                    	strType = strType.replace(strType.substring(strType.indexOf(dto.getCardNoFormat())+dto.getCardNoFormat().length(),strType.indexOf("$customerName$")+14),"").trim();
//                	}else if(flagCN && flagCNF && !flagC) {
//                    	strType = strType.replace(strType.substring(strType.indexOf(dto.getCvv())+dto.getCvv().length(),strType.indexOf("$customerName$")+14),"").trim();
//                	}else {
//                    	strType = strType.replace(strType.substring(strType.indexOf(dto.getCvv())+dto.getCvv().length(),strType.indexOf("$customerName$")+14),"").trim();
//                	}
//                	flagHN = true;
//                }else{
//                	strType = strType.replace("$customerName$", dto.getHoldName());//客户名称
//                	
//                }
//                if(StringUtils.isEmpty(dto.getExpiry())){
////                    strType = strType.replace("$persoDate$", "");
//                	if(flagCNF && flagCN && flagC && flagHN) {
//                        strType = strType.replace(strType.substring(0,strType.indexOf("$persoDate$")+14),"").trim();
//                	}else if(!flagCN && flagCNF && flagC && flagHN) {
//                		strType = strType.replace(strType.substring(strType.indexOf(cardNo)+cardNo.length(),strType.indexOf("$persoDate$")+11),"").trim();
//                	}else if(flagCN && !flagCNF && flagC && flagHN) {
//                		strType = strType.replace(strType.substring(strType.indexOf(dto.getCardNoFormat())+dto.getCardNoFormat().length(),strType.indexOf("$persoDate$")+11),"").trim();
//                	}else if(flagCN && flagCNF && !flagC && flagHN) {
//                		strType = strType.replace(strType.substring(strType.indexOf(dto.getCvv())+dto.getCvv().length(),strType.indexOf("$persoDate$")+11),"").trim();
//                	}else if(flagCN && flagCNF && flagC && !flagHN) {
//                		strType = strType.replace(strType.substring(strType.indexOf(dto.getHoldName())+dto.getHoldName().length(),strType.indexOf("$persoDate$")+11),"").trim();
//                	}else {                		
//                		strType = strType.replace(strType.substring(strType.indexOf(dto.getHoldName())+dto.getHoldName().length(),strType.indexOf("$persoDate$")+11),"").trim();
//                	}
//                	flagE = true;
//                }else{
//                    strType = strType.replace("$persoDate$", dto.getExpiry());
//                    
//                }
//             
//                if(StringUtils.isEmpty(dto.getMag1()) && StringUtils.isEmpty(dto.getMag2()) && StringUtils.isEmpty(dto.getMag3())) {
//                	strType = strType.replace(strType.substring(strType.indexOf("$$mag1"),strType.indexOf("$mag3$")+7),"");
//                }else {                	
//                	if(StringUtils.isEmpty(dto.getMag1())){
//                		strType = strType.replace("$mag1$", "null");
//                	}else{
//                		strType = strType.replace("$mag1$", dto.getMag1());
//                	}
//                	if(StringUtils.isEmpty(dto.getMag2())){
//                		strType = strType.replace("$mag2$", "null");
//                	}else{
//                		strType = strType.replace("$mag2$", dto.getMag2());
//                	}
//                	if(StringUtils.isEmpty(dto.getMag3())){
//                		strType = strType.replace("$mag3$", "null");
//                	}else{
//                		strType = strType.replace("$mag3$", dto.getMag3());
//                	}
//                }
//                String base64Img = "";
//                if(StringUtils.isEmpty(dto.getImagePath())){
//                    base64Img =  this.getBase64Image(".\\img\\" + Global.DEFAULT_IMG_FILE_NAME);
//                }else{
//                    base64Img = this.getBase64Image(dto.getImagePath());
//                }
//                if(StringUtils.isEmpty(base64Img)){
//                    strType = strType.replace("$base64Image$", "");
//                }else{
//                    strType = strType.replace("$base64Image$", base64Img);
//                }
//            
//            }
//            dto.setCardTypeConfig(strType);
//            
        	FaCardRealEntry rfe = new FaCardRealEntry();
        	rfe.setAssetname("test09");
        	rfe.setBarcode("12345678");
        	rfe.setNumber("10000001");
        	rfe.setZsf_rfid("10000001");
            PushPersoData pushData = new PushPersoData();
            pushData.setOriginalJson(JSON.toJSONString(rfe));
//            System.out.println(JSON.toJSONString(dto));
//            pushData.setCardTypeConfig(strType);
            AdminSession session = CacheUtil.getManager().getSession("010001");
            String statusCode = "100";
            String statusDesc = "";
            if (session != null && session.getIoSession().isConnected()) {
                session.write(pushData);
                statusCode = "310";
            } else {
                errors.reject("client.close", "客户端已关闭。");
                statusCode = "312";
                statusDesc = "客户端已关闭";
            }
            // 保存制卡过程日志
//            persoRecordLogService.saveLog(rfe, statusCode, statusDesc);
        } catch (Exception ex) {
            ex.printStackTrace();
            errors.reject("client.close", "服务器错误，请联系管理员。");
        }
    }
    
    /**
     * 将图片转成Base64编码
     * 
     * @Title: getBase64Image
     * @param imgFilePath
     * @return
     */
    private String getBase64Image(String imgFilePath) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        if(StringUtils.isEmpty(imgFilePath)){
            return null;
        }
        byte[] data = null;
        // 读取图片字节数组
        InputStream in = null;
        try {
            File file = new File(imgFilePath);
            if(!file.exists()){
                return null;
            }
            in = new FileInputStream(file);
            data = new byte[in.available()];
            in.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(in != null){
                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);// 返回Base64编码过的字节数组字符串
    }

    @Override
    public void saveRecordByDto(UploadInfoDto dto) {
        try {
            // String fileName = file.getName();
            if (StringUtils.isNotEmpty(dto.getBase64Img())) {
//                String fileName = DateUtil.timestampToString(DateUtil.nowTimestamp(), "yyMMddHHmmssSSS") + ".png";
                File tagetFile = new File(".\\img\\" + Global.DEFAULT_IMG_FILE_NAME);
//                FileUtil.GenerateImage(dto.getBase64Img(), tagetFile.getAbsolutePath());
            }
            // 保存新的制卡数据记录
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(), ex);
        }
        
    }
    
}
