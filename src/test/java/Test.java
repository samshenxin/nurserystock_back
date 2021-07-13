
/*
 * Copyright @ 2015 Goldpac Co. Ltd. All right reserved.
 * @fileName Test.java
 * @author sam
 */

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.encoding.XMLType;
import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;


public class Test {
	private static String namespaceURI = "http://kd.bos.pushdata/GPCardService/";

	public static void main(String[] args) {
		try {
			 String wsUrl = "http://192.168.1.148:8066/GPCardService/DataService";
	            org.apache.axis.client.Service serv = new org.apache.axis.client.Service();
	            Call call = null;
	            call = (Call) serv.createCall();
	            call.setTargetEndpointAddress(new URL(wsUrl));
	            call.setOperationName(new QName(namespaceURI, "PushDataToClient"));
	            call.addParameter(new QName(namespaceURI, "persoID"), XMLType.XSD_STRING, ParameterMode.IN);
	            call.setUseSOAPAction(true);
	            call.setReturnType(XMLType.XSD_STRING);
	            call.setSOAPActionURI("http://kd.bos.pushdata/GPCardService/PushDataToClient");
	            String result = (String) call.invoke(new Object[] {"496"});
	            System.err.println(result);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
