package com.ssh.rfidprint.entry;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
/**
 * DtPersoInfo entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "dt_perso_info")
public class PersoInfo implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    // Fields
    private Integer           piId;
    private String            cardTypeCode;         // 卡类型代码
    private Integer           cardTypeId;           // 卡类型ID
    private String            cardNo;               // 卡号
    private String            cardNoFormat;         // 格式化卡号
    private String            holdName;             // 持卡人姓名
    private String            idNumber;             // 身份证号
    private String            initialData;          // 原始数据
    private String            transformData;        // 转换后数据
    private Integer           branchId;             // 网点号
    private String            branchCode;           // 网点代码
    private String            expiry;               // 有效期
    private String            imagePath;            // DIY照片绝对路径
    private String            cvv;                  // CVV
    private String            pbocDp;               // IC数据
    private Timestamp         createTime;
//    private Integer           pdId;                 // 批次id
    private String            mag1;
    private String            mag2;
    private String            mag3;
//    private String            cretID;               // 金融卡号
    private String            batchNum;        //批次号

    // Constructors
    /** default constructor */
    public PersoInfo() {
    }

    // Property accessors
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "pi_id", unique = true, nullable = false)
    public Integer getPiId() {
        return this.piId;
    }

    public void setPiId(Integer piId) {
        this.piId = piId;
    }

    @Column(name = "card_type_code", length = 20)
    public String getCardTypeCode() {
        return this.cardTypeCode;
    }

    public void setCardTypeCode(String cardTypeCode) {
        this.cardTypeCode = cardTypeCode;
    }

    @Column(name = "card_type_id")
    public Integer getCardTypeId() {
        return this.cardTypeId;
    }

    public void setCardTypeId(Integer cardTypeId) {
        this.cardTypeId = cardTypeId;
    }

    @Column(name = "card_no", length = 25)
    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    @Column(name = "hold_name", length = 50)
    public String getHoldName() {
        return this.holdName;
    }

    public void setHoldName(String holdName) {
        this.holdName = holdName;
    }

    @Column(name = "initial_data")
    public String getInitialData() {
        return this.initialData;
    }

    public void setInitialData(String initialData) {
        this.initialData = initialData;
    }

    @Column(name = "transform_data")
    public String getTransformData() {
        return this.transformData;
    }

    public void setTransformData(String transformData) {
        this.transformData = transformData;
    }

    @Column(name = "branch_id")
    public Integer getBranchId() {
        return this.branchId;
    }

    public void setBranchId(Integer branchId) {
        this.branchId = branchId;
    }

    @Column(name = "expiry", length = 10)
    public String getExpiry() {
        return this.expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    @Column(name = "cvv", length = 10)
    public String getCvv() {
        return this.cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    @Column(name = "pboc_dp", length = 65535)
    public String getPbocDp() {
        return this.pbocDp;
    }

    public void setPbocDp(String pbocDp) {
        this.pbocDp = pbocDp;
    }

    @Column(name = "card_no_format", length = 25)
    public String getCardNoFormat() {
        return cardNoFormat;
    }

    public void setCardNoFormat(String cardNoFormat) {
        this.cardNoFormat = cardNoFormat;
    }

    @Column(name = "branch_code", length = 20)
    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    @Column(name = "id_number")
    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Column(name = "image_path")
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

//    @Column(name = "pd_id")
//    public Integer getPdId() {
//        return pdId;
//    }
//
//    public void setPdId(Integer pdId) {
//        this.pdId = pdId;
//    }

//    @Column(name = "cret_id")
//    public String getCretID() {
//        return cretID;
//    }
//
//    public void setCretID(String cretID) {
//        this.cretID = cretID;
//    }
    
    @Column(name = "mag1")
    public String getMag1() {
        return mag1;
    }

    public void setMag1(String mag1) {
        this.mag1 = mag1;
    }

    @Column(name = "mag2")
    public String getMag2() {
        return mag2;
    }

    public void setMag2(String mag2) {
        this.mag2 = mag2;
    }

    public String getMag3() {
        return mag3;
    }

    @Column(name = "mag3")
    public void setMag3(String mag3) {
        this.mag3 = mag3;
    }

    @Column(name = "batch_num")
    public String getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(String batchNum) {
        this.batchNum = batchNum;
    }
}