package sto.utils;

import java.io.BufferedInputStream;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import sto.common.util.DateUtil;
import sto.common.util.Parameter;
import sto.dao.account.DoorRulesDao;
import sto.model.account.Attend;
import sto.model.account.Door;
import sto.model.account.UnitSettings;
import sto.model.account.User;
import sto.service.account.DoorService;
import sto.service.account.UserService;
import sto.web.account.DoorController;
import sto.web.interfaces.MobileController;

public class SocketOperate extends Thread {
	private Socket socket;
	private String socketinfo;
	@Resource
	UserService userService;
	@Resource
	DoorService doorService;
	@Autowired
	DoorRulesDao doorRulesDao;
	@Resource
	DoorController doorControl;

	public SocketOperate(Socket socket, String socketinfo) {
		this.socket = socket;
		this.socketinfo = socketinfo;
	}

	@SuppressWarnings("unused")
	public void run() {

		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		OutputStream os = null;
		PrintWriter pw = null;
		boolean flag = true;
		try {
			int userid = 0;
			String randcode = null;
			String cid = null;
			String iid = null;
			
			socket.setSoTimeout(5000);
			while (flag) {
				
				// 获取一个输入流，并读取客户端的信息
				is = socket.getInputStream();
				//BufferedInputStream	in=new BufferedInputStream(is);
				isr = new InputStreamReader(is); // 将字节流转化为字符流
				br = new BufferedReader(isr); // 添加缓冲
				String info = "";
				if (!"".equals(socketinfo) && socketinfo != null) {
					info = socketinfo;
					socketinfo = "";

				} else {
					info = br.readLine();
					
				}
				
				
				// 循环读取数据
				if (info != null) {
					System.out.println("我是服务器，客户端说: " + info);

					JSONObject json = new JSONObject(info);
					System.out.println(json);
					System.out.println(json.get("Version"));

					// int userid=0;

					// 获取上传的信息
					String action = json.get("Action").toString();
					String msg = json.get("Msg").toString();
					JSONObject json1 = new JSONObject(msg);

					// 判断是否为刷卡信息上传 0001为刷卡上传指令
					if ("0001".equals(action)) {
						// 获取刷卡上传指令信息
						cid = json1.get("CID").toString();
						iid = json1.get("IID").toString();
						String cardtype = json1.get("CardType").toString();

						// 判读门禁状态,doorinfo:0 门禁锁定 1:开启
						System.out.println("cid=" + cid + "   iid=" + iid);

						int doorinfo = getDoor(cid, iid);
						// System.out.println(user1.size());

						if (doorinfo != 0) {
							// 卡类型判断，1和2：代表刷卡开门 3 ：代表超级卡开门 4：代表按键开门 刷卡开门需要校验动态码
							// 超级卡和按键不需要
							if ("01".equals(cardtype) || "02".equals(cardtype)) {
								// 获取卡相关信息 包括卡ID及动态码
								String cardid=json1.get("CardId").toString();
								/*String cardid=Long.toHexString(Long.parseLong(json1.get("CardId").toString()));
								   while (cardid.length() < 8) {
									   cardid = "0" + cardid;
								     }*/
								String checkcode = Long.toHexString(Long.parseLong(json1.get("CheckCode").toString()));
								 while (checkcode.length() < 8) {
									 checkcode = "0" + checkcode;
								     }
								// 根据卡ID校验动态码
								userid = isUserExist(cardid.toUpperCase(), checkcode.toUpperCase());

								// 生成新的动态验证码
								randcode = getUUID();
								//Long newrandcode=hexString2Bytes(randcode);
								//根据卡ID查找用户信息
								//userid = isUserExist(cardid);
								// userid不为空则生成新的动态验证码，检查用户门禁策略，符合门禁策略则将验证码传给控制器
								// 1:代表有权限 0：代表无权限
								if (userid != 0) {
									// 检查用户是否有该门禁权限
									boolean auth = isDoorAuthExist(userid,
											doorinfo);
									if (auth) {
										// 插入考勤信息
										/*
										 * 插入考勤记录
										 */
										try {
											insertAttend(userid);
										} catch (ParseException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										
										// 将生成的随机码存到user表中的新动态码中
										int resutl = saveCode(userid, randcode);
										if (resutl != 0) {
											// 获取输出流，响应客户端的请求
                                            System.out.println("新生成的随机码"+Long.parseLong(randcode.toString(),16));
											String reply = "{\"Version\":\"01\",\"Action\":\"1001\",\"Msg\":{\"CID\": \""+cid+"\",\"IID\": \""+iid+"\",\"Err\":0,\"Data\":"
													+ Long.parseLong(randcode.toString(),16)+"}}";
											PrintWriter wtr = new PrintWriter(
													socket.getOutputStream());
											wtr.println(reply);
											wtr.flush(); // 将缓存输出
										}

									} else {
										// 获取输出流，响应客户端的请求

										String reply = "{\"Version\":\"01\",\"Action\":\"1001\",\"Msg\":{\"CID\":\""+cid+"\",\"IID\":\""+iid+"\",\"Err\":6,\"ErrMsg\":\"请求解析失败或有错误请求\"}";
										PrintWriter wtr = new PrintWriter(
												socket.getOutputStream());
										wtr.println(reply);
										wtr.flush(); // 将缓存输出用户没有开门权限
									    flag = false;
										break;
									}

								} else {
									// 获取输出流，响应客户端的请求

									String reply = "{\"Version\":\"01\",\"Action\":\"1001\",\"Msg\":{\"CID\":\""+cid+"\",\"IID\":\""+iid+"\",\"Err\":1,\"ErrMsg\":\"未注册卡\"}}";
									PrintWriter wtr = new PrintWriter(
											socket.getOutputStream());
									wtr.println(reply);
									wtr.flush();
									flag = false;
									break;
								}

							} else if ("03".equals(cardtype)
									|| "04".equals(cardtype)) {// 直接发送指令开门
								
								/*//插入开门流水
								int open=insertRecord(userid,cid,iid);*/
								// 获取输出流，响应客户端的请求

								/*String reply = "{\"Version\":\"01\",\"Action\":\"1002\",\"Msg\": {\"CID\":\""+cid+"\",\"IID\":\""+iid+"\",\"Do\":1}}";

								PrintWriter wtr = new PrintWriter(
										socket.getOutputStream());
								wtr.println(reply);
								wtr.flush();
								break;*/
								flag = false;
								break;
							}
						} else {
							// 获取输出流，响应客户端的请求

							String reply = "{\"Version\":\"01\",\"Action\":\"1001\",\"Msg\":{\"CID\":\""+cid+"\",\"IID\":\""+iid+"\",\"Err\":5,\"ErrMsg\":\"门禁处于锁定状态\"}}";
							PrintWriter wtr = new PrintWriter(
									socket.getOutputStream());
							wtr.println(reply);
							wtr.flush(); // 将缓存输出
							flag = false;
							break;
						}

					} else if ("0005".equals(action)) {
						// 获取刷卡响应指令信息
						String err = json1.getString("Err").toString();
						if ("0".equals(err)) {
							// 更新动态验证码，将新的更新至动态码字段

							int result = updateCode(userid, randcode);

							// 插入开门流水记录
							int open=insertRecord(userid,cid,iid);
							// 发送开门指令
							// 获取输出流，响应客户端的请求

							String reply = "{\"Version\":\"01\",\"Action\":\"1002\",\"Msg\": {\"CID\":\""+cid+"\",\"IID\":\""+iid+"\",\"Do\":1}}";
							PrintWriter wtr = new PrintWriter(
									socket.getOutputStream());
							wtr.println(reply);
							wtr.flush(); // 将缓存输出
							
						}
						flag = false;
						break;// 跳出循环
					}

				}else{
					flag=false;
				}
			}
			socket.shutdownInput(); // 关闭输入流
		}catch (SocketTimeoutException e1) {
			System.out.println("错误宋宁宋");
			flag=false;
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SocketException e1) {
			System.out.println("错误宋宁宋");
			flag=false;
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			System.out.println("结束socket"+socket.getPort());
			try {
				// 关闭资源
				if (pw != null)
					pw.close();
				if (os != null)
					os.close();
				if (is != null)
					is.close();
				if (isr != null)
					isr.close();
				if (br != null)
					br.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();

			}

		}

	}

	/**
	 * 根据卡ID及动态码检查用户是否存在
	 * 
	 * @param cardid
	 *            checkcode
	 * @return
	 * @throws SQLException
	 */
	private int isUserExist(String cardid, String checkcode)
			throws SQLException {
		int i = 0;

		MySqlDao getcon = new MySqlDao();
		Connection conn = getcon.getConn();
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "select * from platform_t_user where cardno='" + cardid
				+ "' and isdelete=0 and oldrandomcode='" + checkcode + "'";

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				i = Integer.parseInt(rs.getString("id"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return i;
	}

	/**
	 * 根据卡ID及动态码检查用户是否存在
	 * 
	 * @param cardid
	 *            checkcode
	 * @return
	 * @throws SQLException
	 */
	/*private int isUserExist(String cardid)
			throws SQLException {
		int i = 0;

		MySqlDao getcon = new MySqlDao();
		Connection conn = getcon.getConn();
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "select * from platform_t_user where cardno='" + cardid
				+ "' and isdelete=0 ";

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				i = Integer.parseInt(rs.getString("id"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return i;
	}*/
	 
	/**
	 * 生成16位动态验证码
	 * 
	 * @param
	 * @return
	 */
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		String str = uuid.toString();
		// 去掉"-"符号
		/*String temp = str.substring(0, 8) + str.substring(9, 13)
				+ str.substring(14, 18) + str.substring(19, 23)
				+ str.substring(24);*/
		String temp=str.substring(0,8);
		/*
		 * int length = temp.length(); char[] value = new char[length << 1]; for
		 * (int i=0, j=0; i<length; ++i, j = i << 1) { value[j] =
		 * temp.charAt(i); value[1 + j] = ' '; } return new String(" "+value);
		 */
		return temp.toUpperCase();
	}

	/**
	 * 根据门禁及端口号检查门禁是否锁定
	 * 
	 * @param cid
	 *            iid
	 * @return
	 * @throws SQLException
	 */
	public int getDoor(String cid, String iid) throws SQLException {
		int i = 0;
		Date nowDate = new Date();
		DateFormat format = new SimpleDateFormat("HH:mm:ss");
		String time = format.format(nowDate);

		MySqlDao getcon = new MySqlDao();
		Connection conn = getcon.getConn();
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "select * from platform_t_accesscontrol where doorstatus=1 and (regularflag=0 or (regularflag=1 and ('"
				+ time
				+ "' not BETWEEN regularstarttime and regularendtime)))and controllerid='"
				+ cid + "' and portid='" + iid + "'";
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				i = Integer.parseInt(rs.getString("id"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return i;
	}

	/**
	 * 根据用户ID检查用户是否有开门权限
	 * 
	 * @param userid
	 * @return
	 * @throws SQLException
	 */
	private boolean isDoorAuthExist(int userid, int cid) throws SQLException {
		boolean isUserExist = false;
		MySqlDao getcon = new MySqlDao();
		Connection conn = getcon.getConn();
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "select * from mlog_door_rules where accesscontrolid="
				+ cid + " and userid=" + userid;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				isUserExist = true;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close();
		}
		return isUserExist;
	}

	/**
	 * 临时保存动态验证码
	 * 
	 * @param userid
	 * @return
	 * @throws SQLException
	 */
	private static int saveCode(int userid, String newcode) {
		MySqlDao getcon = new MySqlDao();
		Connection conn = getcon.getConn();
		int i = 0;
		String sql = "update platform_t_user set newrandomcode='" + newcode
				+ "' where id=" + userid;
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			i = pstmt.executeUpdate();
			System.out.println("resutl: " + i);
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}

	/**
	 * 更新验证码
	 * 
	 * @param userid
	 * @return
	 * @throws SQLException
	 */
	private static int updateCode(int userid, String newcode) {
		MySqlDao getcon = new MySqlDao();
		Connection conn = getcon.getConn();
		int i = 0;
		String sql = "update platform_t_user set oldrandomcode='" + newcode
				+ "' where id=" + userid;
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			i = pstmt.executeUpdate();
			System.out.println("resutl: " + i);
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}

	/**
	 * 根据控制器ID和端口号得到门禁ID，
	 * 
	 * @param userid
	 *            ，CID,IID
	 * @return
	 * @throws SQLException
	 */
	private static int getDoorId(String cid, String iid) throws SQLException {
		MySqlDao getcon = new MySqlDao();
		Connection conn = getcon.getConn();
		int i = 0;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = "select * from platform_t_accesscontrol a where  a.controllerid='"
				+ cid + "' and a.portid='" + iid + "'";
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				i = Integer.parseInt(rs.getString("id"));
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.close();
		}
		return i;
	}

	/**
	 * 插入开门流水记录
	 * 
	 * @param userid
	 * @return
	 * @throws SQLException
	 */
	private static int insertRecord(int userid, String cid, String iid
			) {
		MySqlDao getcon = new MySqlDao();
		Connection conn = getcon.getConn();
		Date nowDate = new Date();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = format.format(nowDate);
		int i = 0;
		String sql = "insert into platform_t_accessrecord (accesscontrolid,userid,recordtime,type,status) values(?,?,?,?,?)";
		PreparedStatement pstmt;
		try {
			pstmt = (PreparedStatement) conn.prepareStatement(sql);
			pstmt.setInt(1, getDoorId(cid, iid));
			pstmt.setInt(2, userid);
			pstmt.setString(3, time);
			pstmt.setInt(4, 1);
			pstmt.setInt(5, 1);
			i = pstmt.executeUpdate();
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return i;
	}
	/**
	 * 根据userid得到最新一条考勤记录信息，
	 * 
	 * @param userid
	 *            ，CID,IID
	 * @return
	 * @throws SQLException
	 */
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
