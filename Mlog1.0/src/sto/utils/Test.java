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
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import sto.common.util.DateUtil;

public class Test {
	static int len = 100;

	static String json = "{\"name\":\"Jane\",\"sex\":\"F\"}";
	static String cardup = "{\"Version\": \"01\",\"Action\": \"\",\"Msg\": {\"CID\": \"0001\",\"IID\": \"01\",\"CardType\":\"01\",\"CardId\": \"C7FC1300\",\"CheckCode\": \"93f4d3d83e314e67a71e8e5cb83ef76e\"}}";
	static String result = "{Version:\"01\",Action:\"1005\",Msg:{Err:\"0\"}}";
    static String start="{\"Version\":\"01\",\"Action\":\"0003\",\"Msg\":{CID: \"0001\"}}";
	public static void main(String[] args) throws UnknownHostException {

		try {
			insertAttend(538);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*Socket socket = null;
		PrintWriter pw = null;
		OutputStream os;
		BufferedReader reader = null;
		PrintWriter wtr = null;
		InetAddress remoteAddr1 = InetAddress.getByName("192.168.15.201");
		// InetAddress localAddr1 = InetAddress.getByName("127.0.0.1");
		try {
			socket = new Socket(remoteAddr1, 18888);
			wtr = new PrintWriter(socket.getOutputStream());
			wtr.println(start);
			wtr.flush();
			//wtr.close();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int i = 0;
		while (true) {
			try {
				// 1、创建客户端Socket，指定服务器端口号和地址
				// socket = new Socket("localhost", 18888,"127.0.0.1",28350);

				System.out.println("keyin before1;");
				String get = "{action:001,i:"+i+"}";
				reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				wtr = new PrintWriter(socket.getOutputStream());
				if (get != null & get.length() > 0) {
					
					wtr.println(cardup);
					wtr.flush();
					//wtr.close();
					System.out.println(get + "发送完毕");
				}

				String line = reader.readLine();
				System.out.println("server发送完毕");
				if (line != null) {
					System.out.println("1111:" + line);
					if(line.equals("开门指令")){
					  wtr = new PrintWriter(socket.getOutputStream());
					  wtr.println("开门成功"+i++); wtr.flush();
					  }
					 
					// socket.shutdownOutput();

				}
				// wtr.close();
				// socket.close();
			} catch (IOException e) {
				e.printStackTrace();

			}
		}*/

	}
	public static ResultSet getLastAttend(int userid, Connection conn) throws SQLException{
		//MySqlDao getcon = new MySqlDao();
		//Connection conn = getcon.getConn();
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "select * from mlog_attend a where a.userid="+userid+" order by a.lasttime desc ";
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			/*if (rs.next()) {
				return rs;
			}*/

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			return rs;
		}
		
	}
	
	/**
	 * 插入考勤记录
	 * 
	 * @param userid
	 * @return
	 * @throws SQLException
	 * @throws ParseException 
	 */
	private static  int insertAttend(int userid) throws SQLException, ParseException {
		MySqlDao getcon = new MySqlDao();
		Connection conn = getcon.getConn();
		Date nowDate = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(nowDate);
		int i = 0;
		ResultSet ad = getLastAttend(userid,conn);
		String sql="";
		PreparedStatement pstmt;
		if(ad.next()==false){
			sql = "insert into  mlog_attend (userid,onlgt,onlat,onaddr,ontime,lasttime,status) values(?,?,?,?,?,?,?)";
			try {
				pstmt = (PreparedStatement) conn.prepareStatement(sql);
				pstmt.setInt(1, userid);
				pstmt.setString(2, "0.0");
				pstmt.setString(3, "0.0");
				pstmt.setString(4, "shijiazhuang");
				pstmt.setString(5, time);
				pstmt.setString(6, time);
				/*pstmt.setDate(5,  (java.sql.Date) format.parse(time));
				pstmt.setDate(6,  (java.sql.Date) format.parse(time));*/
				pstmt.setInt(7,0);
				i = pstmt.executeUpdate();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}else{
			
			Date ondate = DateUtil.stringToDate(ad.getString("ontime"), "yyyy-MM-dd");// 上班卡日期
			Date newdate = DateUtil.stringToDate(time, "yyyy-MM-dd");// 当前日期
			if (ondate.before(newdate)) {
				if(ad.getString("offtime")!=null){//插入一条记录，上班时间为上次打卡时间，下班时间为这次打卡时间
					sql = "insert into  mlog_attend (userid,onlgt,onlat,onaddr,ontime,lasttime,status) values(?,?,?,?,?,?,?)";
					pstmt = (PreparedStatement) conn.prepareStatement(sql);
					pstmt.setInt(1, userid);
					pstmt.setString(2, "0.0");
					pstmt.setString(3, "0.0");
					pstmt.setString(4, "shijiazhuang");
					pstmt.setString(5, time);
					pstmt.setString(6, time);
					/*pstmt.setDate(5,  (java.sql.Date) newdate);
					pstmt.setDate(6,(java.sql.Date) newdate);*/
					pstmt.setInt(7,0);
					i = pstmt.executeUpdate();
					pstmt.close();
					conn.close();
					
				}else{//隔天打卡
					
						sql = "update  mlog_attend  set offlgt='0.0',offlat='0.0',offaddr='shijiazhuang',offtime='"+ad.getDate("ontime")+" 23:59:59',lasttime='"+ad.getDate("ontime")+" 23:59:59',status=1 where userid="+userid+" and ontime='"+ad.getString("ontime")+"'";
						pstmt = (PreparedStatement) conn.prepareStatement(sql);
						i = pstmt.executeUpdate();
						sql = "insert into  mlog_attend (userid,onlgt,onlat,onaddr,ontime,lasttime,status) values(?,?,?,?,?,?,?)";
						pstmt = (PreparedStatement) conn.prepareStatement(sql);
						pstmt.setInt(1, userid);
						pstmt.setString(2, "0.0");
						pstmt.setString(3, "0.0");
						pstmt.setString(4, "shijiazhuang");
						pstmt.setString(5, time);
						pstmt.setString(6, time);
						/*pstmt.setDate(5,  (java.sql.Date) newdate);
						pstmt.setDate(6,(java.sql.Date) newdate);*/
						pstmt.setInt(7,0);
						i = pstmt.executeUpdate();
						pstmt.close();
						conn.close();
						
					}
			}else{
				if(ad.getString("offtime")!=null){//插入一整条记录，上班时间为上次打卡时间，下班时间为这次打卡时间
					sql = "insert into  mlog_attend (userid,onlgt,onlat,onaddr,ontime,offlgt,offlat,offaddr,offtime,lasttime,status) values(?,?,?,?,?,?,?,?,?,?,?)";
					pstmt = (PreparedStatement) conn.prepareStatement(sql);
					pstmt.setInt(1, userid);
					pstmt.setString(2, "0.0");
					pstmt.setString(3, "0.0");
					pstmt.setString(4, "shijiazhuang");
					pstmt.setString(5,  ad.getString("offtime"));
					pstmt.setString(6, "0.0");
					pstmt.setString(7, "0.0");
					pstmt.setString(8, "shijiazhuang");
					pstmt.setString(9, time);
					pstmt.setString(10, time);
					/*pstmt.setDate(9,(java.sql.Date) format.parse(time));
					pstmt.setDate(10,(java.sql.Date) format.parse(time));*/
					pstmt.setInt(11,1);
					i = pstmt.executeUpdate();
					pstmt.close();
					conn.close();
					
				}else{//第二次打卡状态记录
					    sql = "update  mlog_attend  set offlgt='0.0',offlat='0.0',offaddr='shijiazhuang',offtime='"+time+" ',lasttime='"+time+"',status=1 where userid="+userid+" and ontime='"+ad.getString("ontime")+"'";
						//sql = "insert into  mlog_attend (offlgt,offlat,offaddr,offtime,lasttime,status) values(?,?,?,?,?,?,?,?,?,?,?) where userid="+userid;
						pstmt = (PreparedStatement) conn.prepareStatement(sql);
						i = pstmt.executeUpdate();
						pstmt.close();
						conn.close();
						
					}
			}
			
			}
	
		return i;
	}


}
