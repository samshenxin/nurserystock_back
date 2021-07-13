/*
 * Copyright @ 2015 Goldpac Co. Ltd. All right reserved.
 * @fileName PersoDetialDto.java
 * @author sam
 */
package com.ssh.rfidprint.dto;
public class PersoDetailDto {
    private Integer persoRecordId;
    private String  cardTypeCode;  // 卡类型代码
    private String  cardTypeName;  // 卡款名称
    private String  cardNo;        // 卡号
    private String  cardNoFormat;  // 格式化卡号
    private String  holdName;      // 持卡人姓名
    private Integer branchId;      // 网点id
    private String  branchName;    // 网点名称
    private String  branchCode;    // 网点
    private String  expiry;        // 有效期
    private Integer hopper;        // 卡槽
    private String  idNumber;      // 身份证号
    private String  coverConfig;   // 卡函配置
    private String  cardTypeImage; // 卡版图案 Base64编码
    private String  cardTypeConfig; // 卡款打印配置
    private String  initialData;   // 原始数据
    private String  pbocDp;
    private String  transformData; // 转换后数据
    private String  persoStatus;   // 个人化状态
    private String  cvv;           // CVV
    private String  mag1;
    private String  mag2;
    private String  mag3;
    private String  imagePath;     // DIY照片绝对路径

    public Integer getPersoRecordId() {
        return persoRecordId;
    }

    public void setPersoRecordId(Integer persoRecordId) {
        this.persoRecordId = persoRecordId;
    }

    public String getCardTypeCode() {
        return cardTypeCode;
    }

    public void setCardTypeCode(String cardTypeCode) {
        this.cardTypeCode = cardTypeCode;
    }

    public String getCardTypeName() {
        return cardTypeName;
    }

    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getCardNoFormat() {
        return cardNoFormat;
    }

    public void setCardNoFormat(String cardNoFormat) {
        this.cardNoFormat = cardNoFormat;
    }

    public String getHoldName() {
        return holdName;
    }

    public void setHoldName(String holdName) {
        this.holdName = holdName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public Integer getHopper() {
        return hopper;
    }

    public void setHopper(Integer hopper) {
        this.hopper = hopper;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getCoverConfig() {
        return coverConfig;
    }

    public void setCoverConfig(String coverConfig) {
        this.coverConfig = coverConfig;
    }

    public String getCardTypeImage() {
        return cardTypeImage;
    }

    public void setCardTypeImage(String cardTypeImage) {
        this.cardTypeImage = cardTypeImage;
    }

    public String getCardTypeConfig() {
        return cardTypeConfig;
    }

    public void setCardTypeConfig(String cardTypeConfig) {
        this.cardTypeConfig = cardTypeConfig;
    }

    public String getInitialData() {
        return initialData;
    }

    public void setInitialData(String initialData) {
        this.initialData = initialData;
    }

    public String getPbocDp() {
        return pbocDp;
    }

    public void setPbocDp(String pbocDp) {
        this.pbocDp = pbocDp;
    }

    public String getTransformData() {
        return transformData;
    }

    public void setTransformData(String transformData) {
        this.transformData = transformData;
    }

    public String getPersoStatus() {
        return persoStatus;
    }

    public void setPersoStatus(String persoStatus) {
        this.persoStatus = persoStatus;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public Integer getBranchId() {
        return branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getMag1() {
        return mag1;
    }

    public void setMag1(String mag1) {
        this.mag1 = mag1;
    }

    public String getMag2() {
        return mag2;
    }

    public void setMag2(String mag2) {
        this.mag2 = mag2;
    }

    public String getMag3() {
        return mag3;
    }

    public void setMag3(String mag3) {
        this.mag3 = mag3;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
