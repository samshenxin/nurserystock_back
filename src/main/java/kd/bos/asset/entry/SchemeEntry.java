package kd.bos.asset.entry;

import java.util.Map;

public class SchemeEntry {

	private String taskRule;

	private String schemeCode;
	private String inventoryState;
	private String entryId;
	
	public SchemeEntry(@SuppressWarnings("rawtypes") Map map){
//		taskRule = (map.get("FTASKRULE") != null ?map.get("FTASKRULE").toString() :"");
//		schemeCode = map.get("FID") != null ?map.get("FID").toString():"";
		inventoryState = map.get("FK_ZSF_INVENTORYSTATE") != null?map.get("FK_ZSF_INVENTORYSTATE").toString():"";
//		entryId = map.get("FENTRYID") != null ?map.get("FENTRYID").toString() :"";
	}
	
	public String getTaskRule() {
		return taskRule;
	}
	public void setTaskRule(String taskRule) {
		this.taskRule = taskRule;
	}
	public String getSchemeCode() {
		return schemeCode;
	}
	public void setSchemeCode(String schemeCode) {
		this.schemeCode = schemeCode;
	}
	public String getInventoryState() {
		return inventoryState;
	}
	public void setInventoryState(String inventoryState) {
		this.inventoryState = inventoryState;
	}
	public String getEntryId() {
		return entryId;
	}
	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	@Override
	public String toString() {
		return "SchemeEntry [taskRule=" + taskRule + ", schemeCode=" + schemeCode + ", inventoryState=" + inventoryState
				+ ", entryId=" + entryId + "]";
	}
	
	
}
