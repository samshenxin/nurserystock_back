package kd.bos.asset.utils;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

import kd.bos.dataentity.entity.DynamicObject;

public class RFIDPrint {

	private static ActiveXComponent dotnetCom = null;
	private static Dispatch formats = null;

	public RFIDPrint(DynamicObject[] realCards) {
		// 控件 BarTender.Application
		dotnetCom = new ActiveXComponent("CLSID:{B9425246-4131-11D2-BE48-004005A04EDF}");
		formats = dotnetCom.getProperty("Formats").toDispatch();
		// 初始化com的线程
				ComThread.InitSTA();
				InvokeHTML invokeHTML = new InvokeHTML();
				invokeHTML.openExplorer();
				try {
					for (final DynamicObject realCard : realCards) {
			            final String barCode = realCard.getString("barcode");
			            final String number = realCard.getString("number");
			            final String assetname = realCard.getString("assetname");
			            String rfid = realCard.getString("zsf_rfid");
			            String headusedept = realCard.getString("headusedept");//使用部門
			            String storeplace = realCard.getString("storeplace");//開始使用日期
			            String usedate = realCard.getString("usedate");//使用日期
			            
					// 打开btw文件
					Dispatch doc = Dispatch.invoke(formats, "Open", Dispatch.Method,
							new Object[] { new Variant("E:\\rfidlabel.btw"), new Variant(false), new Variant(true) },
							new int[1]).toDispatch();
					// 设置属性
					Dispatch.invoke(doc, "SetNamedSubStringValue", Dispatch.Method, new Object[] { "name", assetname },
							new int[2]);
					Dispatch.invoke(doc, "SetNamedSubStringValue", Dispatch.Method, new Object[] { "code", number },
							new int[2]);
					Dispatch.invoke(doc, "SetNamedSubStringValue", Dispatch.Method, new Object[] { "date", usedate },
							new int[2]);
					Dispatch.invoke(doc, "SetNamedSubStringValue", Dispatch.Method, new Object[] { "dept", headusedept },
							new int[2]);
					Dispatch.invoke(doc, "SetNamedSubStringValue", Dispatch.Method, new Object[] { "rfid", rfid },
							new int[2]);
//					//设置需要打印的序列数
//					boolean numberSerializedLabels = Dispatch.call(doc, "NumberSerializedLabels",new Object[] {}).toBoolean();
//					System.out.println(setname);
//					Dispatch.put(numberSerializedLabels, "numberSerializedLabels", 1);
					// 取得某个属性值
//					Variant find = Dispatch.call(doc, "GetNamedSubStringValue", "name");
//					System.out.println(find);

//					// 保存文件
//					Dispatch.invoke(doc, "Save", Dispatch.Method, new Object[] {}, new int[1]);
//					// 打印（打印设置对话框和状态框）
					Dispatch.invoke(doc, "PrintOut", Dispatch.Method, new Object[] { new Variant(false), new Variant(true) },
							new int[2]);
//					Dispatch.invoke(doc,"Print",Dispatch.Method,new Object[] { new Variant(false), new Variant(true) },new int[1]).toDispatch();
					Variant f = new Variant(false);
					// 关闭文档
					Dispatch.call(doc, "Close", f);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					// 关闭bartender
					dotnetCom.invoke("Quit", new Variant(false));
					dotnetCom = null;
					// 关闭com的线程
					ComThread.Release();
				}
	}

	public static void main(String[] args) throws Exception{
		// 初始化com的线程
		ComThread.InitSTA();

		// 要打印的btw模板
		String inFile = "‪E:\\RFID固定资产标签.btw";

//		ax = new ActiveXComponent("CLSID:{8786AEA4-17EC-11D1-8AD8-006097D76312}");

//		RFIDPrint jic = new RFIDPrint();
		// 启动bartender,生成一个ActivexComponent对象
//		ActiveXComponent dotnetCom = new ActiveXComponent(DLL_NAME);
//		Dispatch formats = dotnetCom.getProperty("Formats").toDispatch();
		try {
			// 打开btw文件
			Dispatch doc = Dispatch.invoke(formats, "Open", Dispatch.Method,
					new Object[] { new Variant("E:\\rfidlabel.btw"), new Variant(false), new Variant(true) },
					new int[1]).toDispatch();
			// 设置属性
			Dispatch.invoke(doc, "SetNamedSubStringValue", Dispatch.Method, new Object[] { "title", "RFID固定资产标签" },
					new int[2]);
			Dispatch.invoke(doc, "SetNamedSubStringValue", Dispatch.Method, new Object[] { "name", "中世发智能科技" },
					new int[2]);
//			//设置需要打印的序列数
//			boolean numberSerializedLabels = Dispatch.call(doc, "NumberSerializedLabels",new Object[] {}).toBoolean();
//			System.out.println(setname);
//			Dispatch.put(numberSerializedLabels, "numberSerializedLabels", 1);
			// 取得某个属性值
			Variant find = Dispatch.call(doc, "GetNamedSubStringValue", "name");
			System.out.println(find);

//			// 保存文件
//			Dispatch.invoke(doc, "Save", Dispatch.Method, new Object[] {}, new int[1]);
//			// 打印（打印设置对话框和状态框）
			Dispatch.invoke(doc, "PrintOut", Dispatch.Method, new Object[] { new Variant(false), new Variant(true) },
					new int[2]);
//			Dispatch.invoke(doc,"Print",Dispatch.Method,new Object[] { new Variant(false), new Variant(true) },new int[1]).toDispatch();
			Variant f = new Variant(false);
			// 关闭文档
			Dispatch.call(doc, "Close", f);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭bartender
			dotnetCom.invoke("Quit", new Variant(false));
			dotnetCom = null;
			// 关闭com的线程
			ComThread.Release();
		}
	}

}
