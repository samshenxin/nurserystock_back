package com.ssh.rfidprint.entry;

import java.io.Serializable;

public class FaCardRealEntry implements Serializable{

    private static final long serialVersionUID = -3628469724795296287L;
	protected String barcode;//条码
	protected String number;//资产编码
	protected String assetname;//资产名称 
	protected String zsf_rfid;//rfid编号
	protected String headusedept;//使用部门
	protected String storeplace;//使用位置
	protected String usedate;//使用日期
	protected String billno;//卡片编号
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getAssetname() {
		return assetname;
	}
	public void setAssetname(String assetname) {
		this.assetname = assetname;
	}
	public String getZsf_rfid() {
		return zsf_rfid;
	}
	public void setZsf_rfid(String zsf_rfid) {
		this.zsf_rfid = zsf_rfid;
	}
	public String getHeadusedept() {
		return headusedept;
	}
	public void setHeadusedept(String headusedept) {
		this.headusedept = headusedept;
	}
	public String getStoreplace() {
		return storeplace;
	}
	public void setStoreplace(String storeplace) {
		this.storeplace = storeplace;
	}
	public String getUsedate() {
		return usedate;
	}
	public void setUsedate(String usedate) {
		this.usedate = usedate;
	}
	public String getBillno() {
		return billno;
	}
	public void setBillno(String billno) {
		this.billno = billno;
	}

}
