package sto.service.account;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import sto.utils.Data;
import sto.utils.SocketListenOperate;
import sto.utils.SocketOperate;
import sto.utils.SocketPcOperate;
import sto.utils.SocketTranOperate;

public class serverTest extends Thread {
	static String result = "{Version:\"01\",Action:\"1005\",Msg:{Err:\"0\"}}";
	Integer count = 0;
	private ServerSocket serverSocket;
	BufferedReader rdr = null;
	PrintWriter wtr = null;
	String line = null;
	int i;
	DataInputStream in=null;
	
	Map controlLockMap = new HashMap();
	public static Object[] object = new Object[] { new Object(), new Object(),
			new Object() };
	public static Map<String, Object> ObjectMap = new HashMap();// 用于控制线程同步
	public static Map<String, Object> DataMap = new HashMap();// 用于线程之间数据传输
	// public static Data[] data = new Data[] { new Data(), new Data(), new
	// Data() };
	// public static Data data = new Data();
	public static Map<String, SocketListenOperate> listenthread = new HashMap();
	public static Map<String, SocketTranOperate> tranthread = new HashMap();
	// SocketListenOperate[] listenthread = new SocketListenOperate[10];
	// SocketTranOperate[] tranthread = new SocketTranOperate[10];

	public int threadnum = 0;

	public static void main(String arg[]) {
		serverTest sTest = new serverTest();
		sTest.init();
		sTest.start();
	}

	public void init() {
		// 从web.xml中context-param节点获取socket端口
		String port = "18889";

		if (serverSocket == null) {
			try {
				this.serverSocket = new ServerSocket(Integer.parseInt(port));// 端口号设定为18888
				System.out.println("socket start");

			} catch (IOException e) {
				System.out.println("SocketThread创建socket服务出错");
				e.printStackTrace();
			}
		}
	}

	public void run() {
		try {
			Integer count = 0;

			while (!this.isInterrupted()) {

				Socket socket = serverSocket.accept();
				count++;
				System.out.println("Server SocketThread start:" + count);
				System.out.println("目前连接的SOCKETTHREAD" + socket);

				// 根据action判断属于哪类子线程
				InputStreamReader isr = null;
				BufferedReader br = null;
				InputStream is = null;
				is = socket.getInputStream();
				BufferedInputStream in1 = new BufferedInputStream(is);
				if (socket != null && !socket.isClosed()) {
					System.out.println("readLine before 1;");
					wtr = new PrintWriter(socket.getOutputStream());
					System.out.println("readLine before 2;");
					/*
					 * rdr = new BufferedReader(new InputStreamReader(
					 * socket.getInputStream()));
					 */
		//			in=new DataInputStream(socket.getInputStream());
				/*	byte buf[]=new byte[10];
					 i=in1.read(buf);
					line=new String(buf);*/
					rdr = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					// System.out.println("readLine before s;");
					line = rdr.readLine();
					
					System.out.println("readLine end1;" + line);
					/* i=in1.read(buf);
						line=new String(buf);
						System.out.println("readLine end2;" + line);*/
					// System.out.println("readLine before s2;");
					JSONObject json = new JSONObject(line);
					// System.out.println("readLine before s222;");
					String action = null;
					if (json.get("Action") != null) {
						action = json.get("Action").toString();
					}
					String msg = json.get("Msg").toString();
					JSONObject json1 = new JSONObject(msg);
					String cid = json1.get("CID").toString();
					System.out.println("readLine before 3333;" + cid);
					// 判断启动哪个线程
					if ("0003".equals(action)) {
						System.out.println("readLine before 444;");
						
						if (ObjectMap.size() == 0) {   //判断线程控制对象是否存在，不存在则新创建，存在使用之前的
							ObjectMap.put(cid, new Object());
						} else {
							boolean contains = ObjectMap.containsKey(cid); // 判断是否包含指定的键值
							if (contains) { // 如果条件为真
								System.out.println("在Map集合中包含键名" + cid); // 输出信息
							} else {
								System.out.println("在Map集合中不包含键名" + cid);
								ObjectMap.put(cid, new Object());
							}

						}
						if (DataMap.size() == 0) { //判断线程数据传输类是否存在
							DataMap.put(cid, new Data());
						} else {
							boolean contains = DataMap.containsKey(cid); // 判断是否包含指定的键值
							if (contains) { // 如果条件为真
								System.out.println("在Map集合中包含键名" + cid); // 输出信息
							} else {
								System.out.println("在Map集合中不包含键名" + cid);
								DataMap.put(cid, new Data());
							}

						}


						System.out.println("readLine before 55555;");
						wtr.println("{\"Version\":\"01\",\"Action\":\"1003\",\"Msg\":{Err:\"0\"}}");
						wtr.flush();
						System.out.println("readLine before 66666666;"
								+ (Data) DataMap.get(cid));
						
						System.out.println("readLine before 88888;");
						
						if (listenthread.size() == 0) { //判断线程数据传输类是否存在
							listenthread.put(cid, new SocketListenOperate(socket,ObjectMap.get(cid)));
							listenthread.get(cid).start();
						} else {
							boolean contains = listenthread.containsKey(cid); // 判断是否包含指定的键值
							if (contains) { // 如果条件为真
								System.out.println("在Map集合中包含键名" + cid); // 输出信息
								
							} else {
								System.out.println("在Map集合中不包含键名" + cid);
								listenthread.put(cid, new SocketListenOperate(socket,ObjectMap.get(cid)));
							}

						}
						if (tranthread.size() == 0) { //判断线程数据传输类是否存在
							tranthread.put(cid, new SocketTranOperate(socket,
									(Data) DataMap.get(cid), ObjectMap.get(cid)));
							tranthread.get(cid).start();
						} else {
							boolean contains = tranthread.containsKey(cid); // 判断是否包含指定的键值
							if (contains) { // 如果条件为真
								System.out.println("在Map集合中包含键名" + cid); // 输出信息
								//tranthread.get(cid).stop();
							} else {
								System.out.println("在Map集合中不包含键名" + cid);
								tranthread.put(cid, new SocketTranOperate(socket,
										(Data) DataMap.get(cid), ObjectMap.get(cid)));
							}

						}
						//listenthread.put(cid, new SocketListenOperate(socket));
						/*tranthread.put(cid, new SocketTranOperate(socket,
								(Data) DataMap.get(cid), ObjectMap.get(cid)));*/
						System.out.println("listenthread999999999"+listenthread.get(cid).isAlive());
						System.out.println("listenthread999999r555555"+tranthread.get(cid).isAlive());
						if(!listenthread.get(cid).isAlive()){
							System.out.println("listenthread999999r56666"+listenthread.get(cid));
							listenthread.remove(cid);
							listenthread.put(cid, new SocketListenOperate(socket,ObjectMap.get(cid)));
							listenthread.get(cid).start();
							System.out.println("listenthread999999r5777"+listenthread.get(cid));
							
						}
						if(!tranthread.get(cid).isAlive()){
							tranthread.remove(cid);
							tranthread.put(cid, new SocketTranOperate(socket,
									(Data) DataMap.get(cid), ObjectMap.get(cid)));
							System.out.println("listenthread999999r5755667"+tranthread.get(cid));
							tranthread.get(cid).start();
						}
						
						threadnum++;
						// controlLockMap.put("SerID", object);
					}
					if ("0002".equals(action)) {
						System.out.println("readLine before 0002手机999999;"
								+ cid);
						new SocketPcOperate(socket, (Data) DataMap.get(cid),
								ObjectMap.get(cid), line).start();
						/*
						 * new SocketPcOperate(socket, data[1],
						 * object[1],line).start();
						 */
					}
					if ("0001".equals(action)) {
						new SocketOperate(socket, line).start();
					}

				}

			}
		} catch (Exception ex) {
			System.out.println("SocketThread err:" + ex.getMessage());
			ex.getCause();
		}
	}

	public void closeSocketServer() {
		try {
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();

			}

		} catch (Exception ex) {
			System.out.println("SocketThread err:" + ex.getMessage());
		}
	}
}
