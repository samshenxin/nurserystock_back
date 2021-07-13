/*
 * Copyright @ 2015  Goldpac  Co. Ltd.
 * All right reserved.
 * @fileName IPersoRecordService.java
 * @author sam
 */
package com.ssh.rfidprint.service;

import com.goldpac.cardbiz.error.Errors;
import com.goldpac.cardbiz.model.dto.PersoDatas;
import com.ssh.rfidprint.dto.PersoDetailDto;
import com.ssh.rfidprint.dto.UploadInfoDto;
import com.ssh.rfidprint.entry.PersoInfo;
import com.ssh.rfidprint.entry.PersoRecord;

/**
 * 个人化数据记录实体包括导入状态，制卡状态Service层接口类
 * @ClassName: IPersoRecordService
 * @author sam
 * @date 2015年4月22日 上午10:46:03
 * @version 1.0<br />
 * Update Logs:<br />
 * ****************************************************<br />
 * Date: <br />
 * Description:<br />
 ******************************************************<br />
 */
public interface IPersoRecordService extends BaseService<PersoRecord,Integer> {


    /**
     * 根据ID信息，获取指定数据
     * @Title: getPersoRecordDtoById
     * @param persoRecordId
     * @return
     */
    PersoDetailDto getPersoRecordDtoById(Integer persoRecordId);
    
    /**
     * 根据ID，查询并返回PersoRecord实体类
     * @Title: getPersoRecordById
     * @param persoRecordId
     * @return
     */
    PersoRecord getPersoRecordById(Integer persoRecordId);

    /**
     * 保存制卡数据记录
     * @Title: savePersoRecord
     * @param persoInfo
     */
    void savePersoRecord(PersoInfo persoInfo);

    /**
     * 根据卡号，获取制卡数据信息，并将IC数据更新到制卡记录中。
     * @Title: updatePersoInfo
     * @param dto
     */
//    void updatePersoInfo(ICDataDto dto);

    /**
     * 保存数据集合
     * @Title: savePersoDatas
     * @param datas
     */
    void savePersoDatas(PersoDatas datas) throws Exception;

    /**
     * 主动推送数据到客户端
     * @Title: pushDataToClient
     * @param persoID
     * @param errors 
     */
    void pushDataToClient(Integer persoID, Errors errors);

    /**
     * 保存iPad通过Http请求上传的照片信息
     * @Title: saveRecordByDto
     * @param dto
     */
    void saveRecordByDto(UploadInfoDto dto);
}
 