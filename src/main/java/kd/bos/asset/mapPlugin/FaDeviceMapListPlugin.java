package kd.bos.asset.mapPlugin;

import kd.bos.bill.OperationStatus;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.HyperLinkClickArgs;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;

public class FaDeviceMapListPlugin extends AbstractListPlugin  { 
	   private final static Log log = LogFactory.getLog(FaDeviceMapListPlugin.class);

	   private final static String KEY_TEXTFIELD1 = "zsf_map";
	   /**
		 * 用户点击超链接单元格时，触发此事件
		 */
		@Override
		public void billListHyperLinkClick(HyperLinkClickArgs args) {
			if (StringUtils.equals(KEY_TEXTFIELD1,  args.getHyperLinkClickEvent().getFieldName())){
				// 当前点击的是文本1
				
				// 取消系统自动打开本单的处理
				args.setCancel(true);
				
				// 打开物料新增界面
				FormShowParameter showParameter = new FormShowParameter();
				showParameter.setFormId("zsf_map");
				showParameter.getOpenStyle().setShowType(ShowType.Modal);
				showParameter.setStatus(OperationStatus.ADDNEW);
				
				this.getView().showForm(showParameter);
			}
		}
	   
}
