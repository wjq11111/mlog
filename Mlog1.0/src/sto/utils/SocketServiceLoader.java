package sto.utils;


import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class SocketServiceLoader implements ServletContextListener {
	private SocketThread socketThread;

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		if (null != socketThread && !socketThread.isInterrupted()) {
			socketThread.closeSocketServer();
			socketThread.interrupt();
		}

	}

	@Override
	public void contextInitialized(ServletContextEvent e) {
		// TODO Auto-generated method stub
		ServletContext servletContext = e.getServletContext();

		if (null == socketThread) {
			// 新建线程类
			socketThread = new SocketThread(null, servletContext);//主线程（服务端）
			// 启动线程
			socketThread.start();
		}

	}

}
