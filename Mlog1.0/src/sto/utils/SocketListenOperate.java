package sto.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import sto.common.util.Parameter;
import sto.dao.account.DoorRulesDao;
import sto.model.account.Door;
import sto.model.account.User;
import sto.service.account.DoorService;
import sto.service.account.UserService;
import sto.web.account.DoorController;
import sto.web.interfaces.MobileController;

public class SocketListenOperate extends Thread {
	private Socket socket;
	private Object object;

	@Resource
	UserService userService;
	@Resource
	DoorService doorService;
	@Autowired
	DoorRulesDao doorRulesDao;
	@Resource
	DoorController doorControl;

	public SocketListenOperate(Socket socket, Object object) {
		this.socket = socket;
		this.object = object;
	}

	@SuppressWarnings("unused")
	public void run() {

		boolean flag = true;
		while (flag) {
			InputStream is = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			//BufferedReader rdr = null;
			OutputStream os = null;
			PrintWriter pw = null;
			String info = null;

			/*
			 * try { socket.setKeepAlive(true); socket.setSoTimeout(60000);
			 * 
			 * try { while (true) { socket.sendUrgentData(0xFF); sleep(10000); }
			 * 
			 * } catch (SocketTimeoutException e1) {
			 * System.out.println("socket超时异常66666666666666"); flag = false;
			 * synchronized (object) { object.notify();
			 * 
			 * } break; } catch (IOException e) { flag = false; synchronized
			 * (object) { object.notify();
			 * 
			 * } break; } catch (Exception e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); flag = false; synchronized (object) {
			 * object.notify();
			 * 
			 * } break; } } catch (SocketException e) { // TODO Auto-generated
			 * catch block flag = false; synchronized (object) {
			 * object.notify();
			 * 
			 * } break; }
			 */

			// try { socket.setSoTimeout(5 * 1000); } catch (SocketException e)
			// { // TODO Auto-generated catch block e.printStackTrace(); }

			try {
				socket.setSoTimeout(70 * 1000);// 设定SOCKET超时时间
				is = socket.getInputStream();
				isr = new InputStreamReader(is);
				// 将字节流转化为字符流
				br = new BufferedReader(isr); // 添加缓冲 //
				/*rdr = new BufferedReader(new InputStreamReader(
						socket.getInputStream())); //
				System.out.println("readLine before s;"); //
				info = rdr.readLine();*/
				info = br.readLine();
				System.out.println("心跳111" + info);
			} catch (SocketException e) {
				System.out.println("socket异常66666666666666");
				flag = false;
				synchronized (object) {
					object.notify();

				}
				break;

			} catch (SocketTimeoutException e1) {
				System.out.println("socket超时异常66666666666666");
				flag = false;
				synchronized (object) {
					object.notify();

				}
				break;
			} catch (IOException e1) { // TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// 循环读取数据
			if (info != null) {
				System.out.println("我是服务器，客户端说: " + info);
				/*try {
					PrintWriter wtr = new PrintWriter(socket.getOutputStream());
					String reply = "{\"Version\":\"01\",\"Action\":\"1004\",\"Msg\":{\"Err\":\"0\"}}";
					wtr.println(reply);
					wtr.flush(); // wtr.close();
					System.out.println("监听返回数据" + reply + "发送完毕");
					// sleep(30);
				} catch (IOException e) { // TODO Auto-generated catch block
					e.printStackTrace();
				}*/

			} else {
				flag = false;
				synchronized (object) {
					object.notify();
				}
				break;
			}

		}
		System.out.println("监听线程结束ooooooooo");
	}

}
