package sto.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.json.JSONObject;

class SocketThread extends Thread
{
    Integer count = 0;
    String line = null;
    private ServletContext servletContext;
    private ServerSocket serverSocket;
    public static Map<String, Object> ObjectMap = new HashMap();// 用于控制线程同步
	public static Map<String, Object> DataMap = new HashMap();// 用于线程之间数据传输
	
	public static Map<String, SocketListenOperate> listenthread = new HashMap();
	public static Map<String, SocketTranOperate> tranthread = new HashMap();
	
	BufferedReader rdr = null;
	PrintWriter wtr = null;
	
	
	public int threadnum = 0;
    
   	public SocketThread(ServerSocket serverSocket,ServletContext servletContext)
    {
    	 this.servletContext = servletContext;
         // 从web.xml中context-param节点获取socket端口
         String port = this.servletContext.getInitParameter("socketPort");
       
        if (serverSocket == null)
        {
            try
            {
                this.serverSocket = new ServerSocket(Integer.parseInt(port));//端口号设定为18888
                System.out.println("socket start");
            }
            catch (IOException e)
            {
            	System.out.println("SocketThread创建socket服务出错");
                e.printStackTrace();
            }
        }
    }
    public void run()
    {
        try
        {
            Integer count = 0;
            //Integer threadFlag=0;//0:代表短连接  1：代表长连接 2：代表服务端接口发起的短连接
           
            
            while (!this.isInterrupted())
            {                
            		Socket socket = serverSocket.accept();
    				count++;
    				System.out.println("Server SocketThread start:" + count);
    				System.out.println("目前连接的SOCKETTHREAD" + socket);
    				System.out.println("目前线程连接数"+ listenthread.size());

    				// 根据action判断属于哪类子线程
    				
    				if (socket != null && !socket.isClosed()) {
    					//System.out.println("readLine before 1;");
    					wtr = new PrintWriter(socket.getOutputStream());
    					//System.out.println("readLine before 2;");
    					rdr = new BufferedReader(new InputStreamReader(
    							socket.getInputStream()));
    					
    					line = rdr.readLine();
    					System.out.println("readLine end;" + line);
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
    					//System.out.println("readLine before 3333;" + cid);
    					// 判断启动哪个线程
    					if ("0003".equals(action)) {
    						
    						//System.out.println("readLine before 444;");
    						
    						if (ObjectMap.get(cid) == null) {   //判断线程控制对象是否存在，不存在则新创建，存在使用之前的
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
    						if (DataMap.get(cid) == null) { //判断线程数据传输类是否存在
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


    						//System.out.println("readLine before 55555;");
    						/*wtr.println("{\"Version\":\"01\",\"Action\":\"1003\",\"Msg\":{Err:\"0\"}}");
    						wtr.flush();
    						System.out.println("readLine before 66666666;"
    								+ (Data) DataMap.get(cid));*/
    						
    						//System.out.println("readLine before 88888;");
    						
    						if (listenthread.get(cid) == null) { //判断线程数据传输类是否存在
    							listenthread.put(cid, new SocketListenOperate(socket,ObjectMap.get(cid)));
    							listenthread.get(cid).start();
    							
    						}/* else {
    							boolean contains = listenthread.containsKey(cid); // 判断是否包含指定的键值
    							if (contains) { // 如果条件为真
    								System.out.println("在Map集合中包含键名" + cid); // 输出信息
    								
    							} else {
    								System.out.println("在Map集合中不包含键名" + cid);
    								listenthread.put(cid, new SocketListenOperate(socket,ObjectMap.get(cid)));
    								listenthread.get(cid).start();
    							}

    						}*/
    						if (tranthread.get(cid) == null) { //判断线程数据传输类是否存在
    							tranthread.put(cid, new SocketTranOperate(socket,
    									(Data) DataMap.get(cid), ObjectMap.get(cid)));
    							tranthread.get(cid).start();
    							wtr.println("{\"Version\":\"01\",\"Action\":\"1003\",\"Msg\":{\"Err\":0}}");
        						wtr.flush();
    						} else {
    							boolean contains = tranthread.containsKey(cid); // 判断是否包含指定的键值
    							if (contains) { // 如果条件为真
    								System.out.println("在Map集合中包含键名" + cid); // 输出信息
    								if(listenthread.get(cid).isAlive() && tranthread.get(cid).isAlive()){
    									wtr.println("{\"Version\":\"01\",\"Action\":\"1003\",\"Msg\":{\"Err\":1}}");
                						wtr.flush();
    								}
    							} /*else {//这个判断基本不会进入，前期已判断完
    								System.out.println("在Map集合中不包含键名" + cid);
    								tranthread.put(cid, new SocketTranOperate(socket,
    										(Data) DataMap.get(cid), ObjectMap.get(cid)));
    								tranthread.get(cid).start();
    								wtr.println("{\"Version\":\"01\",\"Action\":\"1003\",\"Msg\":{\"Err\":0}}");
            						wtr.flush();
    							}*/

    						}
    						
    						if(!listenthread.get(cid).isAlive()){
    							System.out.println("listenthread999999r56666"+listenthread.get(cid));
    							listenthread.remove(cid);
    							listenthread.put(cid, new SocketListenOperate(socket,ObjectMap.get(cid)));
    							listenthread.get(cid).start();
    							//System.out.println("listenthread999999r5777"+listenthread.get(cid));
    							
    						}
    						if(!tranthread.get(cid).isAlive()){
    							tranthread.remove(cid);
    							tranthread.put(cid, new SocketTranOperate(socket,
    									(Data) DataMap.get(cid), ObjectMap.get(cid)));
    							System.out.println("listenthread999999r5755667"+tranthread.get(cid));
    							tranthread.get(cid).start();
    							wtr.println("{\"Version\":\"01\",\"Action\":\"1003\",\"Msg\":{\"Err\":0}}");
        						wtr.flush();
    						}
    						   						
    						threadnum++;
    						
    					}
    					if ("0002".equals(action)) {
    						System.out.println("readLine before 0002手机999999;"
    								+ cid);
    						if(DataMap.get(cid)!=null && ObjectMap.get(cid)!=null){
    							new SocketPcOperate(socket, (Data) DataMap.get(cid),
        								ObjectMap.get(cid), line).start();
    						}else{
    							
    						}
    						
    						
    					}
    					if ("0001".equals(action)) {
    						new SocketOperate(socket, line).start();
    					}

    				}
            }
        }
        catch (Exception ex)
        {
            System.out.println("SocketThread err:"+ex.getMessage());
        }
    }
    
    public void closeSocketServer()
    {
        try
        {
            if (serverSocket != null && !serverSocket.isClosed())
            {
                serverSocket.close();
               
            }

        }
        catch (Exception ex)
        {
            System.out.println("SocketThread err:"+ex.getMessage());
        }
    }
}