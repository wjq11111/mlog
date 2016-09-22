package sto.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
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

public class SocketTranOperate extends Thread {
	private Socket socket;
	private Data data = null;
	private Object object = null;
	@Resource
	UserService userService;
	@Resource
	DoorService doorService;
	@Autowired
	DoorRulesDao doorRulesDao;
	@Resource
	DoorController doorControl;

	public SocketTranOperate(Socket socket, Data data, Object object) {
		this.socket = socket;
		this.data = data;
		this.object = object;
	}

	@SuppressWarnings("unused")
	public void run() {

		// BufferedReader reader = null;
		PrintWriter wtr = null;
		boolean flag = true;
		while (flag) {
			synchronized (object) {
				try {
					System.out
							.println("sockettranopertor000000000000000000000");
					object.wait();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {

					/*
					 * reader = new BufferedReader(new InputStreamReader(socket
					 * .getInputStream()));
					 */
					wtr = new PrintWriter(socket.getOutputStream());
					if (data.getInfo() == null || "".equals(data.getInfo())) {
						flag = false;
						break;

					} else {
						wtr.println(data.getInfo());
						wtr.flush();
						// wtr.close();
						System.out.println(data.getInfo() + "发送完毕");
						data.setResult(data.getInfo() + "指令成功");
						data.setInfo("");
						try {
							sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				} catch (SocketException e) {
					flag = false;
					break;

				} catch (IOException e) {
					e.printStackTrace();

				}
			}
		}
		try {
			socket.close();
			System.out.println("数据传输线程结束ttttttttttttttt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
