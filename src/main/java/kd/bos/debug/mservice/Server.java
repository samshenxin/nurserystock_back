/**
 * @fileName Server.java
 * @author sam
 * @version 2014-10-21 上午9:19:20
 */
package kd.bos.debug.mservice;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.util.StringUtils;
import com.ssh.empire.protocol.Protocol;
import com.ssh.net.ProtocolFactory;
import com.ssh.protocol.session.SessionHandler;
import com.ssh.protocol.session.SessionRegistry;
import com.ssh.rfidprint.server.ConfigUtil;
import com.ssh.rfidprint.server.ServerManagerServlet;
import com.ssh.rfidprint.server.UploadServlet;
import com.ssh.rfidprint.server.session.AdminSessionHandler;
import com.ssh.rfidprint.server.session.KeepAliveMessageFactoryImpl;
import com.ssh.rfidprint.webService.GPCardServiceImpl;
import com.ssh.rfidprint.webService.IGPCardService;

public class Server {
	private static final Logger log = LoggerFactory.getLogger(Server.class);
	public static Server instance = null;
	private NioSocketAcceptor acceptor;
	private boolean started = false;
	/** 60秒发送一次心跳包 */
	private static final int HEARTBEATRATE = 60;

	/**
	 * 主函数，启动服务器
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			long time = System.currentTimeMillis();
			instance = new Server();
			instance.launch();
			log.info("启动管理服务...");
			String strServerManager = ConfigUtil.getValue("serverManager");
			if (strServerManager != null && StringUtils.equals(strServerManager, "1")) {
				System.out.println("启动管理服务...");
				openManagerServlet();
			}
			 openGPCardService();
			System.out.println("login time:" + ((System.currentTimeMillis() - time) / 1000) + "秒");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				System.in.read();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 启动WebServer 服务
	 * 
	 * @Title: openGPCardService
	 */
	public static void openGPCardService() throws Exception {
		log.info("开启Webservice服务");
		IGPCardService gpCardService = new GPCardServiceImpl();
//		CardRealService userService = new CardRealServiceImpl();
		String address = "http://192.168.1.148:8066/GPCardService/DataService";
		JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
		// sf.setWsdlLocation("http://localhost:8003/GPCardService/helloService?wsdl");
		sf.setAddress(address);
		sf.setServiceClass(IGPCardService.class);
		sf.setServiceBean(gpCardService);
		sf.getInInterceptors().add(new LoggingInInterceptor());
		// sf.getOutInterceptors().add(new LoggingOutInterceptor());
		sf.create();

		// Endpoint.publish(address, userService);
		log.info("Webservice服务完成");

//		 socketPushData();
	}

	public static void socketPushData() {
		final int PORT = 5209;
		ServerSocket server = null;
		Socket socket = null;
		DataOutputStream out = null;
		try {
			server = new ServerSocket(PORT);
			socket = server.accept();
			out = new DataOutputStream(socket.getOutputStream());
			while (true) {
				Thread.sleep(1000);
				out.writeUTF(getRandomStr());
				out.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String getRandomStr() {
		String str = "";
		int ID = (int) (Math.random() * 30);
		int x = (int) (Math.random() * 200);
		int y = (int) (Math.random() * 300);
		int z = (int) (Math.random() * 10);
		str = "ID:" + ID + "/x:" + x + "/y:" + y + "/z:" + z;
		return str;
	}

	/**
	 * 后台管理服务
	 * 
	 * @throws Exception
	 */
	public static void openManagerServlet() throws Exception {
		// 设置jetty线程池
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(10);
		threadPool.setMaxThreads(100);
		// BoundedThreadPool threadPool = new BoundedThreadPool();
		// // 设置连接参数
		// threadPool.setMinThreads(10);
		// threadPool.setMaxThreads(50);
		// 设置监听端口，ip地址
		// SelectChannelConnector connector = new SelectChannelConnector ();
		org.eclipse.jetty.server.Server server = new org.eclipse.jetty.server.Server(threadPool);
		ServerConnector connector = new ServerConnector(server);
		connector.setHost("0.0.0.0");
		connector.setPort(8002);
		server.addConnector(connector);
		// // 访问项目地址
		// ServletContextHandler root = new ServletContextHandler(server, "/",
		// Context.SESSIONS);
		ServletContextHandler root = new ServletContextHandler(ServletContextHandler.SESSIONS);
		root.setContextPath("/");
		server.setHandler(root);
		//
		ServletHolder managerkServlet = new ServletHolder(new ServerManagerServlet());
		root.addServlet(managerkServlet, "/manager/*");
		ServletHolder upServlet = new ServletHolder(new UploadServlet());
		root.addServlet(upServlet, "/up/*");
		server.start();
		// server.join();
	}

	/**
	 * 关闭服务
	 * 
	 * @Title: shutdown
	 * @throws Exception
	 */
	public void shutdown() {
		System.out.println("删除定时任务");
		// ServiceManager.getManager().deleteJobTask();
		closeSessions();
		acceptor.unbind();
		acceptor.dispose(true);
		started = false;
		log.info("Server closed successfully. Good bye.\n\n");
		System.out.println("Server closed successfully. Good bye.\n\n");
	}

	/**
	 * 关闭所有连接
	 */
	private void closeSessions() {
		for (IoSession session : acceptor.getManagedSessions().values()) {
			session.close(true);
		}
	}

	/**
	 * 描述：启动服务
	 * 
	 * @throws Exception
	 */
	public void launch() throws Exception {
		// 加载协议BeanData和Handler类及对象
		ProtocolFactory.init(Protocol.class, "com.ssh.empire.protocol.data", "com.ssh.rfidprint.server.handler");
		// 创建SessionRegistry会话注册类，用于快速查找IoSession与Session子类的映射关系。
		SessionRegistry registry = new SessionRegistry();
		AdminSessionHandler adminSessionHandler = new AdminSessionHandler(registry);
		bindAdmin(adminSessionHandler);
		log.info("服务器启动...");
		// System.out.println("服务器启动...");
		// System.out.println("初使化定时任务...");
		// ServiceManager.getManager().initTask();
	}

	/**
	 * @param sessionHandler
	 * @throws Exception
	 */
	private void bindAdmin(SessionHandler sessionHandler) throws Exception {
		acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() + 1);
		acceptor.setReuseAddress(true);
		//  设置读取数据的缓冲区大小
		// acceptor.getSessionConfig().setReadBufferSize(2048);
		/** 获取默认过滤器 **/
		DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
		// SSL过滤器
		// SslFilter sslFilter = new
		// SslFilter(BogusSslContextFactory.getInstance(true));
		// chain.addLast("sslFilter", sslFilter);
		int maxNioCache = 10 * 1024 * 1024;
		ObjectSerializationCodecFactory objSFactory = new ObjectSerializationCodecFactory();
		objSFactory.setDecoderMaxObjectSize(maxNioCache);
		objSFactory.setEncoderMaxObjectSize(maxNioCache);
		chain.addLast("codec", new ProtocolCodecFilter(objSFactory));
		/**
		 * chain.addFirst("uwap2databean", new DataBeanFilter());
		 * chain.addFirst("uwap2codec", new ProtocolCodecFilter(new S2SEncoder(), new
		 * S2SDecoder()));
		 */
		KeepAliveMessageFactory heartBeatFactory = new KeepAliveMessageFactoryImpl();
		KeepAliveFilter heartBeat = new KeepAliveFilter(heartBeatFactory, IdleStatus.BOTH_IDLE);
		// 设置是否forward到下一个filter
		heartBeat.setForwardEvent(false);
		// heartBeat.setRequestTimeout(60);
		// 设置心跳频率
		heartBeat.setRequestInterval(HEARTBEATRATE);
		acceptor.getFilterChain().addLast("heartbeat", heartBeat);
		chain.addLast("threadPool", new ExecutorFilter(4, 16));
		acceptor.setHandler(sessionHandler);
		// 会话闲置200S后关闭
		// acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 200);
		acceptor.setDefaultLocalAddress(new InetSocketAddress("0.0.0.0", 9001));
		acceptor.bind();
		started = true;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}
}