package com.ssh.rfidprint.entry;
import static javax.persistence.GenerationType.IDENTITY;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
/**
 * DtPersoRecord entity. @author MyEclipse Persistence Tools
 */
@Entity
@Table(name = "dt_perso_record")
public class PersoRecord implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    // Fields
    private Integer           prId;
    private PersoInfo         persoInfo;            // 个人化数据
    private String            persoStatus;          // 个人化状态
    private String            persoStatusDesc;
    private Timestamp         persoDate;            // 制卡日期
    private String            operator;             // 制卡员
    private Timestamp         inputDate;            // 导入日期
    private String            inputStatus;          // 导入状态
    private String            inputStatusDesc;
    private Integer           submitNum;            // 制卡提交次数
    private Integer           successNum;           // 制卡成功次数
    private Integer           failNum          = 0;

    // Constructors
    /** default constructor */
    public PersoRecord() {
    }

    // Property accessors
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "pr_id", unique = true, nullable = false)
    public Integer getPrId() {
        return this.prId;
    }

    public void setPrId(Integer prId) {
        this.prId = prId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "perso_info_id")
    public PersoInfo getPersoInfo() {
        return this.persoInfo;
    }

    public void setPersoInfo(PersoInfo persoInfo) {
        this.persoInfo = persoInfo;
    }

    @Column(name = "operator", length = 20)
    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Column(name = "perso_status")
    public String getPersoStatus() {
        return persoStatus;
    }

    public void setPersoStatus(String persoStatus) {
        this.persoStatus = persoStatus;
    }

    @Column(name = "perso_date")
    public Timestamp getPersoDate() {
        return persoDate;
    }

    public void setPersoDate(Timestamp persoDate) {
        this.persoDate = persoDate;
    }

    @Column(name = "input_date")
    public Timestamp getInputDate() {
        return inputDate;
    }

    public void setInputDate(Timestamp inputDate) {
        this.inputDate = inputDate;
    }

    @Column(name = "input_status")
    public String getInputStatus() {
        return inputStatus;
    }

    public void setInputStatus(String inputStatus) {
        this.inputStatus = inputStatus;
    }

//    @Formula("(select t.name from dt_perso_status t where t.code = perso_status)")
    public String getPersoStatusDesc() {
        return persoStatusDesc;
    }

    public void setPersoStatusDesc(String persoStatusDesc) {
        this.persoStatusDesc = persoStatusDesc;
    }

//    @Formula("(select t.name from dt_perso_status t where t.code = input_status)")
    public String getInputStatusDesc() {
        return inputStatusDesc;
    }

    public void setInputStatusDesc(String inputStatusDesc) {
        this.inputStatusDesc = inputStatusDesc;
    }

    @Column(name = "submit_num")
    public Integer getSubmitNum() {
        return submitNum;
    }

    public void setSubmitNum(Integer submitNum) {
        this.submitNum = submitNum;
    }

    @Column(name = "success_num")
    public Integer getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(Integer successNum) {
        this.successNum = successNum;
    }

    @Column(name = "fail_num")
    public Integer getFailNum() {
        return failNum;
    }

    public void setFailNum(Integer failNum) {
        this.failNum = failNum;
    }
}