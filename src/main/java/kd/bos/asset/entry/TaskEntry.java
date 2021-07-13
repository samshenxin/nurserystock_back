package kd.bos.asset.entry;

public class TaskEntry {
	
	private String schemeid;
	  private String taskid;
	  private String assetunit;
	  private String chargeperson;
	  private int realnumber;
	  private int bookquantity;
	  private int lossnumber;
	  private int profitnumber;
	  private int hasInvent;
	  private int total;
	  private int inventorypercent;
	  private String status;
	  private String inventoryState;//盘点状态
	  private String audit;//盘点审核
	  
	  public int getInventorypercent()
	  {
	    return this.inventorypercent;
	  }
	  
	  public void setInventorypercent(int inventorypercent)
	  {
	    this.inventorypercent = inventorypercent;
	  }
	  
	  public int getHasInvent()
	  {
	    return this.hasInvent;
	  }
	  
	  public void setHasInvent(int hasInvent)
	  {
	    this.hasInvent = hasInvent;
	  }
	  
	  public int getTotal()
	  {
	    return this.total;
	  }
	  
	  public void setTotal(int total)
	  {
	    this.total = total;
	  }
	  
	  public String getSchemeid()
	  {
	    return this.schemeid;
	  }
	  
	  public void setSchemeid(String schemeid)
	  {
	    this.schemeid = schemeid;
	  }
	  
	  public String getTaskid()
	  {
	    return this.taskid;
	  }
	  
	  public void setTaskid(String taskid)
	  {
	    this.taskid = taskid;
	  }
	  
	  public String getAssetunit()
	  {
	    return this.assetunit;
	  }
	  
	  public void setAssetunit(String assetunit)
	  {
	    this.assetunit = assetunit;
	  }
	  
	  public String getChargeperson()
	  {
	    return this.chargeperson;
	  }
	  
	  public void setChargeperson(String chargeperson)
	  {
	    this.chargeperson = chargeperson;
	  }
	  
	  public int getRealnumber()
	  {
	    return this.realnumber;
	  }
	  
	  public void setRealnumber(int realnumber)
	  {
	    this.realnumber = realnumber;
	  }
	  
	  public int getBookquantity()
	  {
	    return this.bookquantity;
	  }
	  
	  public void setBookquantity(int bookquantity)
	  {
	    this.bookquantity = bookquantity;
	  }
	  
	  public int getLossnumber()
	  {
	    return this.lossnumber;
	  }
	  
	  public void setLossnumber(int lossnumber)
	  {
	    this.lossnumber = lossnumber;
	  }
	  
	  public int getProfitnumber()
	  {
	    return this.profitnumber;
	  }
	  
	  public void setProfitnumber(int profitnumber)
	  {
	    this.profitnumber = profitnumber;
	  }
	  
	  public String getStatus()
	  {
	    return this.status;
	  }
	  
	  public void setStatus(String status)
	  {
	    this.status = status;
	  }

	public String getInventoryState() {
		return inventoryState;
	}

	public void setInventoryState(String inventoryState) {
		this.inventoryState = inventoryState;
	}

	public String getAudit() {
		return audit;
	}

	public void setAudit(String audit) {
		this.audit = audit;
	}

	@Override
	public String toString() {
		return "TaskEntry [schemeid=" + schemeid + ", taskid=" + taskid + ", assetunit=" + assetunit + ", chargeperson="
				+ chargeperson + ", realnumber=" + realnumber + ", bookquantity=" + bookquantity + ", lossnumber="
				+ lossnumber + ", profitnumber=" + profitnumber + ", hasInvent=" + hasInvent + ", total=" + total
				+ ", inventorypercent=" + inventorypercent + ", status=" + status + ", inventoryState=" + inventoryState
				+ ", audit=" + audit + "]";
	}
	
	  
}
