package jdy.zsf.nurserystock.instoragePlugin;

import java.util.EventObject;

import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
/**
 * 入库订单单据插件
 * @author Administrator
 *
 */
public class InStorageBillPlugin extends AbstractFormPlugin {
	private static final Log logger = LogFactory.getLog(InStorageBillPlugin.class);
	
	@Override
    public void afterCreateNewData(EventObject e) {

	}
	
	@Override
	public void afterBindData(EventObject e) {
		super.afterBindData(e);
		// 获取编码规则编码
		String billno = (String) this.getModel().getValue("billno");
//		String ordername = getOrdername(billno);
		int last = billno.lastIndexOf("-");
		billno = billno.substring(last+1);
		
	   //设置单据编码
	    this.getModel().setValue("zsf_ordername",billno);
	}
	/**
	 * 获取当前单据编号的日期+流水号后缀作为订单名称
	 * @param billno
	 * @return
	 */
	public String getOrdername(String billno) {
		int last = billno.lastIndexOf("-");
		billno = billno.substring(last+1);
		String date = billno.substring(0,8);
		String number = billno.substring(8,billno.length());
		Integer num = Integer.valueOf(number)+1;
		//流水号补零
		int length = billno.length()-8-(String.valueOf(num).length());
		String ordername = "";
		for(int i=0; i<length; i++) {
			ordername += "0"; 
		}
		logger.info("入库单编号："+date+ordername+num);
		return date+ordername+num;
	}

}
