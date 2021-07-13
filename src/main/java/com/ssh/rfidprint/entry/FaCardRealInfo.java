package com.ssh.rfidprint.entry;

import java.io.Serializable;

public class FaCardRealInfo implements Serializable{

    private static final long serialVersionUID = -3628469724795296287L;

    protected  String barcode;
    protected  String number;
    protected  String assetname;
    protected  String zsf_rfid;
    protected  String headusedept;
    protected  String storeplace;
    protected  String usedate;
    
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
	@Override
	public String toString() {
		return "FaCardRealInfo [barcode=" + barcode + ", number=" + number + ", assetname=" + assetname + ", zsf_rfid="
				+ zsf_rfid + ", headusedept=" + headusedept + ", storeplace=" + storeplace + ", usedate=" + usedate
				+ "]";
	}
	
	
}
