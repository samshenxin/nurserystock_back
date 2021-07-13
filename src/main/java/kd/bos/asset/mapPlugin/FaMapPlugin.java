package kd.bos.asset.mapPlugin;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import kd.bos.ext.form.control.MapControl;
import kd.bos.ext.form.control.events.MapSelectEvent;
import kd.bos.ext.form.control.events.MapSelectListener;
import kd.bos.ext.form.dto.MapSelectPointOption;
import kd.bos.form.ClientActions;
import kd.bos.form.IClientViewProxy;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.fi.bcm.common.util.Point;

public class FaMapPlugin extends AbstractListPlugin implements MapSelectListener {
	private static final String mapKey = "zsf_mapcontrolap";

	public void registerListener(final EventObject e) {
		super.registerListener(e);
		MapControl mapControl = this.getControl(mapKey);
		mapControl.addSelectListener(this);
	}

	@Override
	public void select(MapSelectEvent evt) {
		// TODO Auto-generated method stub
		Map<String, Object> dataMap = new HashMap<>();
		// 设置显示隐藏搜索框（通用u指令）
		dataMap.put("showSearchBox", false); // 默认true，是否显示地图左上角的搜索框
		// 设置是否可拖动地图标记
		dataMap.put("canMarkerDraggble", false); // 默认false，属性打开后可用鼠标拖动地图上的位置标记
		dataMap.put("k", mapKey);
		evt.setPoint(dataMap);
		IClientViewProxy proxy = this.getView().getService(IClientViewProxy.class);

		proxy.addAction(ClientActions.updateControlStates, dataMap);
		MapSelectListener.super.select(evt);
//		evt.getPoint();
		// 设置地图根据经纬度标记某个位置
		MapControl mapCtl = this.getView().getControl(mapKey);
		MapSelectPointOption mpo = new MapSelectPointOption();
		mpo.setProvince("广东省");
		mpo.setCity("珠海市");
		mpo.setAddress("广东中世发智能科技有限公司");
		// mpo.setLat(113.56244703);
		// mpo.setLng(22.25691465);
		// mapCtl.selectPoint(mpo); //MapSelectPointOption：经纬度封装对象

		// 设置地图根据地址标记某个位置
		// MapControl mapCtl = this.getView().getControl(mapKey);
		mapCtl.selectAddress("广东省珠海市格力集团");

	}

//	@Override
//	public void initialize() {
//		// TODO Auto-generated method stub
//		super.initialize();
		// Map<String, Object> dataMap = new HashMap<>();
		// // 设置显示隐藏搜索框（通用u指令）
		// dataMap.put("showSearchBox", false); // 默认true，是否显示地图左上角的搜索框
		// // 设置是否可拖动地图标记
		// dataMap.put("canMarkerDraggble", false); // 默认false，属性打开后可用鼠标拖动地图上的位置标记
		// dataMap.put("k", mapKey);
		// IClientViewProxy proxy = this.getView().getService(IClientViewProxy.class);
		//
		// proxy.addAction(ClientActions.updateControlStates, dataMap);

		// // 设置地图根据经纬度标记某个位置
		// MapControl mapCtl = this.getView().getControl(mapKey);
		// MapSelectPointOption mpo = new MapSelectPointOption();
		// mpo.setProvince("广东省");
		// mpo.setCity("珠海市");
		// mpo.setAddress("广东中世发智能科技有限公司");
		// // mpo.setLat(113.56244703);
		// // mpo.setLng(22.25691465);
		// // mapCtl.selectPoint(mpo); //MapSelectPointOption：经纬度封装对象
		//
		// // 设置地图根据地址标记某个位置
		// // MapControl mapCtl = this.getView().getControl(mapKey);
		// mapCtl.selectAddress("广东省珠海市格力集团");

//	}

}
