package kd.bos.asset.utils;
import java.awt.Desktop;  
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
public class InvokeHTML {

	private  String htmlFile = "https://www.baidu.com/";
	public  void openExplorer() {
		if (java.awt.Desktop.isDesktopSupported()) {
		try {
//		Desktop.getDesktop().open(new File(htmlFile));
			Desktop desktop = Desktop.getDesktop();   
			URI uri = null;
			try {
				uri = new URI(htmlFile);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //创建URI统一资源标识符
			desktop.browse(uri); //使用默认浏览器打开超链接
		} catch (IOException e) {
		e.printStackTrace();

		 }
	   }
	}

		 

		public static void main(String[] args) throws IOException {
//		openExplorer("E://SATOPrinterAPIServices.html");
		

		}
}
