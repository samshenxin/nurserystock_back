package kd.bos.asset.commonPlugin;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import com.alibaba.druid.util.StringUtils;

import kd.bos.asset.utils.DateUtil;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.entity.datamodel.AbstractFormDataModel;
import kd.bos.entity.datamodel.TableValueSetter;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ClickListener;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.field.FieldEdit;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.fi.arapcommon.consts.DBRouteConst;

/**
 * 
 * @ClassName: receiveBillImportDataPlugin
 * @Description:TODO(领用资产新增单据组件父页面)
 * @author: sam
 * @date: 2021-2-27 16:59:00
 * @Copyright:
 */
public class ReceiveBillImportDataPlugin extends AbstractFormPlugin implements ClickListener {
	// 页面工具栏控件标识
	private static final String KEY_ITEM_IMPORT = "zsf_item_import";
	// 弹出动态表单页面标识
	private static final String KEY_POP_FORM = "zsf_bas_importdata";
	// 单据体上的菜单栏名（高级面板工具栏）
	private final static String KEY_TOOLBAR_IMPORTDATA = "zsf_toolbarap_receive";
	// 可用单据体名
	private final static String KEY_ENTRYENTITY_SEARCH = "zsf_detail_entryentity";
	// 标题区工具栏
	private final static String KEY_TOOLBAR_TBMAIN = "tbmain";
	// 保存按钮
	private final static String KEY_SAVE = "bar_save";
	// 删除按钮
	private final static String KEY_DEL = "bar_del";

	@Override
	public void registerListener(EventObject e) {
		super.registerListener(e);

		// 侦听单据体菜单栏按钮点击事件
		Toolbar toolbar_select = this.getView().getControl(KEY_TOOLBAR_IMPORTDATA);
		toolbar_select.addItemClickListener(this);
		this.addItemClickListeners(KEY_TOOLBAR_TBMAIN);

	}

	/**
	 * 控件事件
	 * 
	 * @param e
	 */
	@Override
	public void itemClick(ItemClickEvent evt) {
		super.itemClick(evt);
		// 获取改变控件的编码，如果是请假天数被修改，则获取请假天数的具体值，如果大于3天，则弹出编码为9z8e_popdetail的表单
		// 这里弹出表单参数使用FormShowParameter表示弹出动态表单，如果是弹出页面是单据也可使用BillShowParameter
		if (StringUtils.equals(KEY_ITEM_IMPORT, evt.getItemKey())) {
			// 创建弹出页面对象，FormShowParameter表示弹出页面为动态表单
			FormShowParameter ShowParameter = new FormShowParameter();
			// 设置弹出页面的编码
			ShowParameter.setFormId(KEY_POP_FORM);
			// 设置弹出页面标题
			ShowParameter.setCaption("导入数据");
			// 设置页面关闭回调方法
			// CloseCallBack参数：回调插件，回调标识
			ShowParameter.setCloseCallBack(new CloseCallBack(this, KEY_ITEM_IMPORT));
			// 设置弹出页面打开方式，支持模态，新标签等
			ShowParameter.getOpenStyle().setShowType(ShowType.Modal);
			// 弹出页面对象赋值给父页面
			this.getView().showForm(ShowParameter);
		} else if (StringUtils.equals(KEY_SAVE, evt.getItemKey())) {
			// 保存领用单据时，使用状态设置为：在用
			EntryGrid entryGrid = this.getView().getControl(KEY_ENTRYENTITY_SEARCH);
			DynamicObjectCollection entrys = this.getModel().getEntryEntity(KEY_ENTRYENTITY_SEARCH);
			int indexs[] = new int[entrys.size()];
			for (int i = 0; i < entrys.size(); i++) {
				indexs[i] = i;
			}
			entryGrid.selectRows(indexs, 1);

			int[] rows = entryGrid.getSelectRows();
			boolean execute = false;
			if (null != rows && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {

					DynamicObject entity = this.getModel().getEntryEntity(KEY_ENTRYENTITY_SEARCH).get(rows[i]);
					String querySql = "select fid,fnumber from tk_zsf_usestatus where fk_zsf_status=?";
					Object[] params = { "1" };
					//查到对应状态的id
					List<Long> idList = DB.query(DBRoute.basedata, querySql, params, rs -> {
						List<Long> ret = new ArrayList<>();
						while (rs.next()) {
							ret.add(rs.getLong(1));
						}
						return ret;
					});
					Object[] params1 = {idList.get(0),entity.get("zsf_billno")};
					String updateSql = "update tk_zsf_entering set fk_zsf_status =? where fk_zsf_billno =? ";
					//资产录入表使用状态更改为：在用
					execute = DB.execute(DBRouteConst.BASEDATA, updateSql,params1);

				}
			}
			if (execute)
				this.getModel().deleteEntryData(KEY_ENTRYENTITY_SEARCH);

		} else if (StringUtils.equals(KEY_DEL, evt.getItemKey())) {
			// 删除领用单据时，将使用状态设置为：闲置
			EntryGrid entryGrid = this.getView().getControl(KEY_ENTRYENTITY_SEARCH);
			DynamicObjectCollection entrys = this.getModel().getEntryEntity(KEY_ENTRYENTITY_SEARCH);
			int indexs[] = new int[entrys.size()];
			for (int i = 0; i < entrys.size(); i++) {
				indexs[i] = i;
			}
			entryGrid.selectRows(indexs, 1);

			int[] rows = entryGrid.getSelectRows();
			if (null != rows && rows.length > 0) {
				for (int i = 0; i < rows.length; i++) {
					String querySql = "select fid,fnumber from tk_zsf_usestatus where fk_zsf_status=?";
					Object[] params = { "0" };
					//查到对应状态的id
					List<Long> idList = DB.query(DBRoute.basedata, querySql, params, rs -> {
						List<Long> ret = new ArrayList<>();
						while (rs.next()) {
							ret.add(rs.getLong(1));
						}
						return ret;
					});
					DynamicObject entity = this.getModel().getEntryEntity(KEY_ENTRYENTITY_SEARCH).get(rows[i]);
					Object[] params1 = {idList.get(0),entity.get("zsf_billno")};
					String updateSql = "update tk_zsf_entering set fk_zsf_status =? where fk_zsf_billno =? ";
					DB.execute(DBRouteConst.BASEDATA, updateSql,params1);

//					System.err.println(execute);
				}
			}
		}
	
	}

	/**
	 * 页面关闭回调事件
	 * 
	 * @param closedCallBackEvent
	 */
	@Override
	public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
		super.closedCallBack(closedCallBackEvent);
		DateUtil dateUtil = new DateUtil();
		// 判断标识是否匹配，并验证返回值不为空，不验证返回值可能会报空指针
		if (StringUtils.equals(closedCallBackEvent.getActionId(), KEY_ITEM_IMPORT)
				&& null != closedCallBackEvent.getReturnData()) {
			// 这里返回对象为Object，可强转成相应的其他类型，
			// 单条数据可用String类型传输，返回多条数据可放入map中，也可使用json等方式传输
			@SuppressWarnings("unchecked")
			HashMap<String, DynamicObjectCollection> returnData = (HashMap<String, DynamicObjectCollection>) closedCallBackEvent
					.getReturnData();

			DynamicObjectCollection objList = returnData.get("zsf_import_entryentity");
			this.getModel().deleteEntryData(KEY_ENTRYENTITY_SEARCH);
			AbstractFormDataModel model = (AbstractFormDataModel) this.getModel();
			model.clearDirty();
			model.beginInit();
			EntryGrid entryGrid = this.getControl(KEY_ENTRYENTITY_SEARCH);
			List<FieldEdit> fieldList = entryGrid.getFieldEdits();
			TableValueSetter setter = new TableValueSetter();
			for (int i = 0; i < fieldList.size(); i++) {
				setter.addField(fieldList.get(i).getKey());
			}
			// 绑定选中数据到表单中
			for (int i = 0; i < objList.size(); i++) {
				// 基础资料数据转换
				DynamicObject assettypeObj = (DynamicObject) objList.get(i).get("zsf_assettype");
				DynamicObject affdeptObj = (DynamicObject) objList.get(i).get("zsf_department");
				DynamicObject areaObj = (DynamicObject) objList.get(i).get("zsf_area");
				DynamicObject creatorObj = (DynamicObject) objList.get(i).get("zsf_creator");
				DynamicObject supObj = (DynamicObject) objList.get(i).get("zsf_supplier");

				Long assettypeId = null;
				if (assettypeObj != null)
					assettypeId = assettypeObj.getLong("id");
				Long affdeptId = null;
				if (affdeptObj != null)
					affdeptId = affdeptObj.getLong("id");
				Long areaId = null;
				if (areaObj != null)
					areaId = areaObj.getLong("id");
				Object creator = null;
				if (creatorObj != null)
					creator = creatorObj.get("name");
				Long supId = null;
				if (supObj != null)
					supId = supObj.getLong("id");
				setter.addRow(objList.get(i).getString("zsf_billno"), objList.get(i).getString("zsf_rfid"),
						objList.get(i).getString("zsf_assetname"), objList.get(i).getString("zsf_qrcode"), assettypeId,
						affdeptId, objList.get(i).getString("zsf_usedept"), objList.get(i).getString("zsf_user"),
						areaId, objList.get(i).getString("zsf_store"), objList.get(i).get("zsf_spec"),
						objList.get(i).get("zsf_unit"), objList.get(i).get("zsf_amount"),
						objList.get(i).get("zsf_use_timelimit"),
						objList.get(i).getString("zsf_buydate") != null
								? dateUtil.stringToString(objList.get(i).getString("zsf_buydate"))
								: null,
						supId, objList.get(i).get("zsf_remark"),
						objList.get(i).getString("zsf_createtime") != null
								? dateUtil.stringToString(objList.get(i).getString("zsf_createtime"))
								: null,
						creator, objList.get(i).get("zsf_status"));

			}
			model.batchCreateNewEntryRow(KEY_ENTRYENTITY_SEARCH, setter);
			model.endInit();
			this.getView().updateView(KEY_ENTRYENTITY_SEARCH);
//			this.getView().close();
		}
	}
}
