/*
 * Copyright @ 2015 Goldpac Co. Ltd. All right reserved.
 * @fileName UploadInfoDto.java
 * @author sam
 */
package com.ssh.rfidprint.dto;
// ios客户端传输实体类
public class UploadInfoDto {
    private String cardNo;
    private String holdName;
    private String expiry;
    private String base64Img;
    private String writeIC;  // 0不写IC，1写IC
    private String emboss;   // 0 不打凸字，1打凸字
    private String audit;    // 0 未审核，1已审核

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getHoldName() {
        return holdName;
    }

    public void setHoldName(String holdName) {
        this.holdName = holdName;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getBase64Img() {
        return base64Img;
    }

    public void setBase64Img(String base64Img) {
        this.base64Img = base64Img;
    }

    public String getWriteIC() {
        return writeIC;
    }

    public void setWriteIC(String writeIC) {
        this.writeIC = writeIC;
    }

    public String getEmboss() {
        return emboss;
    }

    public void setEmboss(String emboss) {
        this.emboss = emboss;
    }

    public String getAudit() {
        return audit;
    }

    public void setAudit(String audit) {
        this.audit = audit;
    }
}
