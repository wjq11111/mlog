package sto.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TestPc {
	static int len = 100;

	/*static String json = "{\"name\":\"Jane\",\"sex\":\"F\"}";
	static String cardup = "{Version: \"01\",Action: \"0001\",Msg: {CID: \"0001\",IID: \"01\",CardType:\"01\",CardId: \"C7FC1300\",CheckCode: \"93f4d3d83e314e67a71e8e5cb83ef76e\"}}";
	static String result = "{Version:\"01\",Action:\"1005\",Msg:{Err:\"0\"}}";
*/
	public static void main(String[] args) {

		Socket socket = null;
		PrintWriter pw = null;
		OutputStream os;
		BufferedReader reader = null;
		PrintWriter wtr = null;
		try {
			// 1、创建客户端Socket，指定服务器端口号和地址
			socket = new Socket("localhost", 18888);
			System.out.println("Pckeyin before1;");			
			String get = "{\"Version\":\"01\",\"Action\":\"0002\",\"Msg\":{\"CID\":\"0001\",\"IID\":\"01\",\"Do\":1}}";
			reader = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));	
			wtr = new PrintWriter(socket.getOutputStream());
			if (get != null & get.length() > 0)
			{http://localhost:8080/Mlog1.0/
				wtr.println(get);
				wtr.flush();
				
				System.out.println(get + "发送完毕");
				
			}
			
			String line = reader.readLine();
			if (line!=null) {
				System.out.println("开门指令等待结果:" + line);
					

			}	
			wtr.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();

		}

	}



}
