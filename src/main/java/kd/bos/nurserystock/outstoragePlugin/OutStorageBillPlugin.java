package kd.bos.nurserystock.outstoragePlugin;

import java.math.BigDecimal;
import java.util.EventObject;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.algo.RowMeta;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.FieldTip;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.coderule.CodeRuleServiceHelper;
/**
 * 出库单据插件
 * @author Administrator
 *
 */
public class OutStorageBillPlugin extends AbstractFormPlugin {
	private static final Log logger = LogFactory.getLog(OutStorageBillPlugin.class);
	
	@Override
    public void afterCreateNewData(EventObject e) {
	   //获取编码规则单据编号
		DynamicObject dataInfo = this.getModel().getDataEntity();
		// 获取编码规则编码
		String number = CodeRuleServiceHelper.getNumber(this.getView().getEntityId().toString(), dataInfo, null);
		String ordername = getOrdername(number);
		
	   //设置单据编码
	    this.getModel().setValue("zsf_ordername",ordername);
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
		logger.info("出库单编号："+date+ordername+num);
		return date+ordername+num;
	}
	
	@Override
	public void propertyChanged(PropertyChangedArgs e) {
		String propName = e.getProperty().getName();
		if (propName.equals("zsf_qty")) {
			//校验剩余可出库数量 
			DynamicObject siteOb = (DynamicObject) this.getModel().getValue("zsf_site");
			String site = siteOb != null ? siteOb.get("id").toString() : "";

			int columnCount = 0;
			String algoKey = getClass().getName() + ".query_resume";
			String sql = "select d.fid from tk_zsf_instorage i left join tk_zsf_instorage_detail d "
					+ "on i.fid = d.fk_zsf_insid "
					+ "where i.fk_zsf_site =?  and d.fk_zsf_store_status='0'";
			Object[] params = { site};
			try (DataSet ds = DB.queryDataSet(algoKey, DBRoute.of("fa"), sql, params)) {
				RowMeta md = ds.getRowMeta();
				while (ds.hasNext()) {
					Row row = ds.next();
					columnCount++;
				}
			}
			
			BigDecimal qtyOb = (BigDecimal) this.getModel().getValue("zsf_qty");
			double realQty = qtyOb.doubleValue();
			if (realQty > columnCount) {
				FieldTip qtyFieldTip = new FieldTip(FieldTip.FieldTipsLevel.Info, FieldTip.FieldTipsTypes.others,
						"zsf_qty", "数量不能大于库存数量"+columnCount);
				this.getView().showFieldTip(qtyFieldTip);
			} else {
			}

		}
	}

}
