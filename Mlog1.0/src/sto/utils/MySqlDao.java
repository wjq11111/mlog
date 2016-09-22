package sto.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.mysql.jdbc.Statement;
import com.sun.mail.util.PropUtil;

public class MySqlDao {
	static private String driver = null;
    static private String url = null;
    static private String user = null;
    static private String password = null;
    static{
        loads();
    }
    synchronized static public void loads(){
        if(driver == null || url == null || user == null || password == null){
            InputStream is = MySqlDao.class.getResourceAsStream("/db.properties");
            Properties dbproperties = new Properties();
            try {
                dbproperties.load(is);
                        
                driver = dbproperties.getProperty("jdbc.driver").toString();
                url = dbproperties.getProperty("jdbc.url").toString();  
                user = dbproperties.getProperty("jdbc.username").toString();  
                password = dbproperties.getProperty("jdbc.password").toString(); 
                    
            }
            catch (Exception e) {
                System.err.println("不能读取属性文件. " + "请确保db.properties在CLASSPATH指定的路径中");
            }
        }
    }
      
    public static String getDriver() {
        if(driver==null)
            loads();
        return driver;
    }
     
    public static String getUrl() {
        if(url==null)
            loads();
        return url;
    }
     
    public static String getUser() {
        if(user==null)
            loads();
        return user;
    }
     
    public static String getPassword() {
        if(password==null)
            loads();
        return password;
    }


	 
	public Connection getConn()
	 {
		
		Connection conn=null;
	  try {
		 

	  // Class.forName("com.mysql.jdbc.Driver");
			Class.forName(driver);
			try {
				conn=DriverManager.getConnection(url,user,password);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("数据库连接不成功");
			}
	   //conn=DriverManager.getConnection(url, "root", "123456");
	  } catch (ClassNotFoundException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  }
	

	 /* String url="jdbc:mysql://127.0.0.1:3306/mlog0910";
	 // String url="jdbc:mysql://192.168.15.52:3306/mlog0910";
	  try {
	   conn=DriverManager.getConnection(url, "root", "123456");
	  } catch (SQLException e) {
	   // TODO Auto-generated catch block
	   e.printStackTrace();
	  }  */
	  return conn;
	 }
	 public void closeAll(ResultSet rs,Statement stat,Connection conn)
	 {
	  if(rs!=null)
	   try {
	    rs.close();
	   } catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	   }
	  if(stat!=null)
	   try {
	    stat.close();
	   } catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	   }
	  if(conn!=null)
	   try {
	    conn.close();
	   } catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	   }
	 }

}
