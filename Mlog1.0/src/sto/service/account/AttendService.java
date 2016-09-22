package sto.service.account;

import java.io.File;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.stereotype.Service;

import other.AuthProfile;
import sto.common.service.BaseServiceImpl;
import sto.common.util.DateUtil;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.dao.account.AttendDao;
import sto.dao.account.DeptDao;
import sto.dao.account.FestDao;
import sto.dao.account.LeaveDao;
import sto.form.AttendForm1;
import sto.form.AttendForm2;
import sto.form.AttendForm3;
import sto.model.account.Attend;
import sto.model.account.Dept;
import sto.model.account.Fest;
import sto.model.account.Role;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @ClassName: RoleService
 * @Description: service
 * @author zzh
 * @date 2014-11-5 11:07:12
 * 
 */
@Service
@SuppressWarnings("unchecked")
public class AttendService extends BaseServiceImpl<Attend>{
	@Autowired
	private AttendDao attendDao;
	@Resource
	private SysSettingsService sysSettingsService;
	@Resource
	private DataRulesService dataRulesService;
	@Resource
	 private FestDao festDao;
	@Resource
	 private LeaveDao leaveDao;
	@Resource
	 private DeptService deptService;
	public Attend getLastAttend(Integer userid){
		String hql = "from Attend a where a.userid=:p1 order by a.lasttime desc ";
		List<Attend> list = attendDao.find(hql, new Parameter(userid));
		if(list.size() > 0){
			return list.get(0);
		}else {
			return null;
		}
	}
	
	public int getFestCount(String divid,String startdate,String enddate){
		// 获得某个单位设定的节日管理天数
		String hql="select count(*) From mlog_fest Where divid='"+divid+"'  and date Between '"+startdate+"' and '"+enddate+"'"; 
		List<BigInteger> list = festDao.findBySql(hql);
	    return new Integer(list.get(0).toString());
		
	}
	
	public int getLeaveCount(String userid,String startdate,String enddate){
		// 获得某个人员的请假天数
		String hql="select count(*) From mlog_leave Where userid="+Integer.parseInt(userid)+" and leavedate Between '"+startdate+"' and '"+enddate+"'"; 
	    List<BigInteger> list = leaveDao.findBySql(hql);
	    return  new Integer(list.get(0).toString());
	   
		
	}
	
	public AttendForm1 getBadAttendMonthStatistics(Integer userid,String getuserid,String date){
		
		Parameter parameter = new Parameter();
		parameter.put("p1", sysSettingsService.findUniqueBy("skey", "worktime").getValue());
		parameter.put("p2", getuserid);
		parameter.put("p3", date);
		parameter.put("p4", date);
		if(StringUtils.isBlank(date)){
			date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		String whereSql = " and z.isbad='true' and z.userid=:p2 and str_to_date(z.date,'%Y-%m-%d')<=str_to_date(:p3,'%Y-%m-%d') and date_format(z.date,'%Y-%m')=date_format(:p4,'%Y-%m') ";
		
		String sql = "select count(1) baddays,max(z.userid) userid,max(z.name) username from ( "
				+" select e.userid,e.username,e.name,e.date,time_format(sec_to_time(ifnull(sum(unix_timestamp(e.offtime)-unix_timestamp(e.ontime)),0)),'%H:%i:%s') worktime, "  
				+" case when sec_to_time(sum(unix_timestamp(e.offtime)-unix_timestamp(e.ontime)))>=time(:p1) then 'false' else 'true' end isbad "
				+" from (select c.id,c.userid,c.username,c.name,c.date,d.ontime,d.offtime "
				+" from (select b.id,a.id userid,a.username,a.name,date_format(b.ontime,'%Y-%m-%d') date "
				+" from platform_t_user a cross join mlog_attend b group by a.id,date "
				+" ) c "
				+" left join mlog_attend d on c.userid=d.userid  and c.date=date_format(d.ontime,'%Y-%m-%d') "
				+" ) e group by e.date,e.userid "
				+ ") z where 1=1 "
				+ whereSql
				+ " group by z.userid order by z.userid,z.date ";
		List<Object[]> list = attendDao.findBySql(sql, parameter);
		AttendForm1 form1 = new AttendForm1();
		if(list.size()>0){
			Object[] o = list.get(0);
			form1.setBaddays(String.valueOf(o[0]));
			form1.setUserid(String.valueOf(o[1]));
			form1.setUsername(String.valueOf(o[2]));
		}
		return form1;
	}
	
	public AttendForm1 getBadAttendMonthStatistics1(Integer userid,String getuserid,String date){
		AttendForm1 form1 = new AttendForm1();
		if(StringUtils.isBlank(date)){
			date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		Connection conn = null;
		CallableStatement cstmt  = null;
		try {
			conn = SessionFactoryUtils.getDataSource(attendDao.getSession().getSessionFactory()).getConnection();
			cstmt  = conn.prepareCall("{Call p_attendstatistic(?,?,?)}");  
			cstmt.setString(1, sysSettingsService.findUniqueBy("skey", "worktime").getValue());
			cstmt.setString(2, getuserid);
			cstmt.setString(3, date);
			cstmt.execute();
			ResultSet rs = cstmt.getResultSet();
			while(rs.next()){
				form1.setBaddays(rs.getString(1));
				form1.setUserid(rs.getString(2));
				form1.setUsername(rs.getString(3));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(cstmt != null){
				try {
					cstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				cstmt = null;
			}
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				conn = null;
			}
		}
		
		return form1;
	}
	
	public List<AttendForm2> getAttendListByUserid(Page<Object[]> page,String getuserid,String date,String badonly){
		Parameter parameter = new Parameter();
		parameter.put("p1", sysSettingsService.findUniqueBy("skey", "worktime").getValue());
		//parameter.put("p2", getuserid);
		String tempdate = date;
		if(StringUtils.isBlank(date)){
			date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		}
		parameter.put("p3", date);
		parameter.put("p4", date);
		String whereSql = " and z.userid in("+getuserid+") and str_to_date(z.date,'%Y-%m-%d')<=str_to_date(:p3,'%Y-%m-%d') and date_format(z.date,'%Y-%m')=date_format(:p4,'%Y-%m') ";
		if(!StringUtils.isBlank(badonly) && badonly.equals("true")){
			whereSql = " and z.isbad='true' ";
		}
		
		String sql = "select date,worktime,isbad from ( "
				+" select e.userid,e.date,time_format(sec_to_time(ifnull(sum(unix_timestamp(e.offtime)-unix_timestamp(e.ontime)),0)),'%H:%i:%s') worktime, "  
				+" case when sec_to_time(sum(unix_timestamp(e.offtime)-unix_timestamp(e.ontime)))>=time(:p1) then 'false' else 'true' end isbad "
				+" from (select c.id,c.userid,c.date,d.ontime,d.offtime "
				+" from (select b.id,a.id userid,date_format(b.ontime,'%Y-%m-%d') date "
				+" from platform_t_user a cross join mlog_attend b group by a.id,date "
				+" ) c "
				+" left join mlog_attend d on c.userid=d.userid  and c.date=date_format(d.ontime,'%Y-%m-%d') "
				+" ) e group by e.date,e.userid "
				+ ") z where 1=1 "
				+ whereSql
				+ " order by z.userid,z.date desc ";
		List<Object[]> list = attendDao.findBySql(page, sql, parameter).getResult();
		List<AttendForm2> list1 = new ArrayList<AttendForm2>();
		if(list != null){
			for(Object[] o : list){
				AttendForm2 form2 = new AttendForm2(); 
				form2.setDate(String.valueOf(o[0]));
				form2.setWorktime(String.valueOf(o[1]));
				form2.setIsbad(String.valueOf(o[2]));
				list1.add(form2);
			}
		}
		return list1;
	}
	
	public JSONObject getIAttendListByUserid(Map<String,String> map){
		JSONObject json = new JSONObject();
		List<AttendForm2> list1 = new ArrayList<AttendForm2>();
		
		Connection conn = null;
		CallableStatement cstmt  = null;
		try {
			conn = SessionFactoryUtils.getDataSource(attendDao.getSession().getSessionFactory()).getConnection();
			cstmt  = conn.prepareCall("{Call p_attendstatistic_mx(?,?,?,?,?,?)}");  
			cstmt.setString(1, sysSettingsService.findUniqueBy("skey", "worktime").getValue());
			cstmt.setString(2, map.get("getuserid"));
			cstmt.setString(3, map.get("date"));
			cstmt.setString(4, map.get("badonly"));
			cstmt.setInt(5, Integer.parseInt(map.get("pagenum")));
			cstmt.setInt(6, Integer.parseInt(map.get("pagesize")));
			cstmt.execute();
			ResultSet rs = cstmt.getResultSet();
			
			while(rs.next()){
				AttendForm2 form2 = new AttendForm2(); 
				form2.setDate(rs.getString(1));
				form2.setWorktime(rs.getString(2));
				form2.setIsbad(rs.getString(3));
				list1.add(form2);
			}
			json.put("rows", list1);
			ResultSet rs1 = null;
			int total = 0;
			if(cstmt.getMoreResults()){
				rs1 = cstmt.getResultSet();
			}
			while(rs1.next()){
				total = rs1.getInt("total");
			}
			json.put("total", total);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(cstmt != null){
				try {
					cstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				cstmt = null;
			}
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				conn = null;
			}
		}
		return json;
	}
	
	public List<AttendForm3> getAttendCardListByUserid(String getuserid,String date){
		Parameter parameter = new Parameter();
		parameter.put("p1", date);
		//parameter.put("p2", getuserid);
		parameter.put("p3", date);
		//parameter.put("p4", getuserid);
		String sql = " select type,time,longitude,latitude,address from ("
				+" select id,1 type,ontime time,onlgt longitude,onlat latitude,onaddr address "
				+" from mlog_attend where date_format(lasttime,'%Y-%m-%d')=:p1 and userid in("+getuserid+")"
				+" union all "
				+" select id,2 type,offtime time,offlgt longitude,offlat latitude,offaddr address "
				+" from mlog_attend where date_format(lasttime,'%Y-%m-%d')=:p3 and userid in("+getuserid+")"
				+ ") a order by a.id,a.type";
		List<Object[]> list = attendDao.findBySql(sql, parameter);
		List<AttendForm3> list1 = new ArrayList<AttendForm3>();
		for(Object[] o : list){
			AttendForm3 form3 = new AttendForm3(); 
			form3.setType(String.valueOf(o[0]));
			form3.setTime(String.valueOf(o[1]));
			form3.setLongitude(String.valueOf(o[2]));
			form3.setLatitude(String.valueOf(o[3]));
			form3.setAddress(String.valueOf(o[4]));
			list1.add(form3);
		}
		return list1;
	}
	
	//根据姓名、单位和时间进行打卡查询
	/*public List<Map<String,Object>> getAttendCardListByUsername(Page<Object[]> p,Map<String,Object> parammap){
		
		Parameter parameter = new Parameter();
		parameter.put("p1", parammap.get("date"));
		//parameter.put("p2", parammap.get("userid"));
		parameter.put("p3", parammap.get("date"));
		//parameter.put("p4", parammap.get("userid"));
		String sql = " select id,type,time,longitude,latitude,address from ("
				+" select id,1 type,ontime time,onlgt longitude,onlat latitude,onaddr address "
				+" from mlog_attend where date_format(lasttime,'%Y-%m-%d')=:p1 and userid in ("+"select id from platform_t_user where name='"+parammap.get("username")+"' and divid='"+parammap.get("divid")+"')"
				+" union all "
				+" select id,2 type,offtime time,offlgt longitude,offlat latitude,offaddr address "
				+" from mlog_attend where date_format(lasttime,'%Y-%m-%d')=:p3 and userid in ("+"select id from platform_t_user where name='"+parammap.get("username")+"' and divid='"+parammap.get("divid")+"')"
				+ ") a order by a.id,a.type";
		System.out.println("getAttendCardListByUsername sql="+sql);
		List<Object[]> list = attendDao.findBySql(p,sql, parameter).getResult();
		System.out.println("getAttendCardListByUsername list="+list.size());
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf(o[0]));
				map.put("type", String.valueOf(o[1]));
				map.put("time", String.valueOf(o[2] == null ? "" : o[2]));
				map.put("longitude", String.valueOf(o[3] == null ? "0.0" : o[3]));
				map.put("latitude", String.valueOf(o[4] == null ? "0.0" : o[4]));
				map.put("address", String.valueOf(o[5] == null ? "#" : o[5]));
				list1.add(map);
			}
		}
		System.out.println("getAttendCardListByUsername list1="+list1.size());
		return list1;
	}*/
	
	
	public List<Map<String,Object>> getAttendCardListByUserid(Page<Object[]> p,Map<String,Object> parammap){
		Parameter parameter = new Parameter();
		parameter.put("p1", parammap.get("date"));
		//parameter.put("p2", parammap.get("userid"));
		parameter.put("p3", parammap.get("date"));
		//parameter.put("p4", parammap.get("userid"));
		String sql = " select id,type,time,longitude,latitude,address from ("
				+" select id,1 type,ontime time,onlgt longitude,onlat latitude,onaddr address "
				+" from mlog_attend where date_format(lasttime,'%Y-%m-%d')=:p1 and userid in("+parammap.get("userid")+")"
				+" union all "
				+" select id,2 type,offtime time,offlgt longitude,offlat latitude,offaddr address "
				+" from mlog_attend where date_format(lasttime,'%Y-%m-%d')=:p3 and userid in("+parammap.get("userid")+")"
				+ ") a order by a.id,a.type";
		List<Object[]> list = attendDao.findBySql(p,sql, parameter).getResult();
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf(o[0]));
				map.put("type", String.valueOf(o[1]));
				map.put("time", String.valueOf(o[2] == null ? "" : o[2]));
				map.put("longitude", String.valueOf(o[3] == null ? "0.0" : o[3]));
				map.put("latitude", String.valueOf(o[4] == null ? "0.0" : o[4]));
				map.put("address", String.valueOf(o[5] == null ? "#" : o[5]));
				list1.add(map);
			}
		}
		
		return list1;
	}
	
	
	public List<Map<String,Object>> getAttendAllList(Page<Object[]> page,Map<String,Object> parammap){
		AuthProfile curruser = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String userids = dataRulesService.getDataAuth(curruser.getUser().getId(),1);
		Parameter parameter = new Parameter();
		parameter.put("p1", sysSettingsService.findUniqueBy("skey", "worktime").getValue());
		//parameter.put("p2", userids);
		String whereSql = "";
		if(parammap.get("startDate") != null && !parammap.get("startDate").equals("")){
			whereSql += " and str_to_date(a.lasttime,'%Y-%m-%d')>=str_to_date(:p3,'%Y-%m-%d') ";
			parameter.put("p3",  parammap.get("startDate"));
		}
		
		if(parammap.get("endDate") != null && !parammap.get("endDate").equals("")){
			whereSql += " and str_to_date(a.lasttime,'%Y-%m-%d')<=str_to_date(:p4,'%Y-%m-%d') ";
			parameter.put("p4",  parammap.get("endDate"));
		}
		
		if(parammap.get("name") != null && !parammap.get("name").equals("")){
			whereSql += " and b.name like :p5 ";
			parameter.put("p5",  "%"+parammap.get("name")+"%");
		}
		String sql = "select c.* from (select a.id,a.userid,b.name,b.username,date_format(lasttime,'%Y-%m-%d') date,TIME_FORMAT(sec_to_time(sum(unix_timestamp(offtime)-unix_timestamp(ontime))),'%H:%i:%s') worktime, "
				+" case when sec_to_time(sum(unix_timestamp(offtime)-unix_timestamp(ontime)))>=time(:p1) then '正常' else '异常' end status "
				+" from mlog_attend a left join platform_t_user b on a.userid=b.id "
				+" where 1=1 and a.userid in ("+userids+") "
				+ whereSql
				+" group by userid,date) c "
				+" order by c.userid,c.date ";
		List<Object[]> list = attendDao.findBySql(page, sql, parameter).getResult();
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf(o[0]));
				map.put("userid", String.valueOf(o[1]));
				map.put("name", String.valueOf(o[2]));
				map.put("username", String.valueOf(o[3]));
				map.put("date", String.valueOf(o[4]));
				map.put("worktime", String.valueOf(o[5]));
				map.put("status", String.valueOf(o[6]));
				list1.add(map);
			}
		}
		return list1;
	}
	
	public List<Attend> getAttendByUserid(Integer userid){
		String hql = "from Attend a where a.userid=:p1 order by a.lasttime desc ";
		return attendDao.find(hql, new Parameter(userid));
	}
	
	public List<Map<String,Object>> getAttendStatisticList(Map<String,String> map) throws SQLException{
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		Connection conn = null;
		CallableStatement cstmt  = null;
		try {
			conn = SessionFactoryUtils.getDataSource(attendDao.getSession().getSessionFactory()).getConnection();
			cstmt  = conn.prepareCall("{Call p_attendstatistic_export(?,?,?,?,?,?,?)}"); 
			cstmt.setString(1, map.get("divid"));
			cstmt.setString(2, map.get("deptid") == null ? "":map.get("deptid"));
			cstmt.setString(3, map.get("date1"));
			cstmt.setString(4, map.get("date2"));
			cstmt.setInt(5, 0);//导出时必须传0
			cstmt.setInt(6, 0);
			cstmt.setInt(7, Integer.parseInt(map.get("userid")));
			cstmt.execute();
			ResultSet rs = cstmt.getResultSet();
			int nianxiu,shijia,bingjia,tiaoxiu;
			nianxiu=shijia=bingjia=tiaoxiu=0;
			
			while(rs.next()){
				Map<String,Object> m = new HashMap<String,Object>();
				m.put("name", rs.getString("name"));
				m.put("id", rs.getString("id"));
				m.put("isbads", rs.getString("isbads").split(","));
				m.put("worktimes", rs.getString("worktimes").split(","));
				m.put("dept",rs.getString("deptname"));
				//m.put("dates", rs.getString("dates"));
				m.put("nianxiu",rs.getInt("nianxiu"));
				m.put("shijia",rs.getInt("shijia"));
				m.put("bingjia",rs.getInt("bingjia"));
				m.put("tiaoxiu",rs.getInt("tiaoxiu"));
				list.add(m);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(cstmt != null){
				try {
					cstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				cstmt = null;
			}
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				conn = null;
			}
		}
		return list;
	}
	/**
	 * 
	 * @param map
	 * @return
	 * @throws SQLException
	 */
	public JSONObject getAttendStatisticQueryColumns(Map<String,String> map) throws SQLException{
		JSONObject json = new JSONObject();
		Date date1 = DateUtil.stringToDate(map.get("date1"), "yyyy-MM-dd");
		Date date2 = DateUtil.stringToDate(map.get("date2"), "yyyy-MM-dd");
		int days = (int)((date2.getTime()-date1.getTime())/(1000*60*60*24))+1;
		Calendar ca = Calendar.getInstance();
		ca.setTime(date1);
		
		int width = 0;
		List<Map<String,Object>> columnsList = new ArrayList<Map<String,Object>>();
		Map<String,Object> columnMap = new HashMap<String,Object>();
		StringBuffer columnsSbf = new StringBuffer(100);//拼接列
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "id");
		columnMap.put("field", "id");
		columnMap.put("hidden", "true");
		columnsSbf.append("{field:'id', title:'id', width:80,hidden: true},");
	//	width += 80;
		columnsList.add(columnMap);
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "部门");
		columnMap.put("field", "dept");
		columnMap.put("width", "80");
		width += 80;
		columnsList.add(columnMap);
		columnsSbf.append("{field:'dept', title:'部门', width:80},");
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "姓名");
		columnMap.put("field", "name");
		columnMap.put("width", "80");
		width += 80;
		columnsList.add(columnMap);
		columnsSbf.append("{field:'name', title:'姓名', width:80},");
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "正常天数");
		columnMap.put("field", "normaldays");
		columnMap.put("width", "80");
		width += 80;
		columnsList.add(columnMap);
		columnsSbf.append("{field:'normaldays', title:'正常天数', width:80},");
		
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "异常天数");
		columnMap.put("field", "baddays");
		columnMap.put("width", "80");
		width += 80;
		columnsList.add(columnMap);
		columnsSbf.append("{field:'baddays', title:'异常天数', width:80},");
		
		
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "请假天数");
		columnMap.put("field", "leavedays");
		columnMap.put("width", "80");
		width += 80;
		columnsList.add(columnMap);
		columnsSbf.append("{field:'leavedays', title:'请假天数', width:80,formatter:leave},");
		
		
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "加班时长");
		columnMap.put("field", "totledays");
		columnMap.put("width", "50");
		columnMap.put("sortable", true);
		width += 80;
		columnsList.add(columnMap);
		columnsSbf.append("{field:'totledays', title:'加班时长', width:80,sortable:true},");

		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "年休");
		columnMap.put("field", "nianxiu");
		columnMap.put("width", "50");
		width += 80;
		columnsList.add(columnMap);
		columnsSbf.append("{field:'nianxiu', title:'年休', width:80},");
		
		
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "事假");
		columnMap.put("field", "shijia");
		columnMap.put("width", "50");
		width += 80;
		columnsList.add(columnMap);
		columnsSbf.append("{field:'shijia', title:'事假', width:80},");
		
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "病假");
		columnMap.put("field", "bingjia");
		columnMap.put("width", "50");
		width += 80;
		columnsList.add(columnMap);
		columnsSbf.append("{field:'bingjia', title:'病假', width:80},");
		
		columnMap = new HashMap<String,Object>();
		columnMap.put("title", "调休");
		columnMap.put("field", "tiaoxiu");
		columnMap.put("width", "50");
		columnsSbf.append("{field:'tiaoxiu', title:'调休', width:80},");
		
		/*width += 80;
		columnsList.add(columnMap);
		columnMap = new HashMap<String,String>();
		columnMap.put("title", "工作时长");
		columnMap.put("field", "workdays");
		columnMap.put("width", "80");*/
		width += 80;
		width += 80;
		columnsList.add(columnMap);
		for(int i=0;i<days;i++){
			columnMap = new HashMap<String,Object>();
			columnMap.put("title", DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd"));
			columnMap.put("field", DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd"));
			columnMap.put("width", "100");
			width += 100;
			if(i==days-1){
				//columnsSbf.append("{field:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', title:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', width:80,styler:function(value,rowData,rowIndex){if (value <'09:00:00'){return 'background-color:#ffee00;color:red;';}}}");
				//columnsSbf.append("{field:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', title:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', width:80,formatter:function(value,row,index){if (value <'09:00:00'){return '<span style=\"color:#ffcc00;\"><a href=\"http://192.168.15.13:8080/Mlog1.0//attend/list.action\">'+value+'</a></span>';} else {return value;}}}");
				columnsSbf.append("{field:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', title:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', width:80,formatter:song}");
			}
			else{
				//columnsSbf.append("{field:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', title:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', width:80,styler:function(value,rowData,rowIndex){if (value <'09:00:00'){return 'background-color:#ffee00;color:red;';}}},");
				columnsSbf.append("{field:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', title:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', width:80,formatter:song},");
				//columnsSbf.append("{field:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', title:'"+DateUtil.dateToString(ca.getTime(),"yyyy-MM-dd")+"', width:80,formatter:function(value,rowData,rowIndex){if (value<'09:00:00'){ var item='"+"<span style='background-color:#ffee00;color:red';><a href='http://192.168.15.13:8080/Mlog1.0/attend/list.action'> value</a></span>'; return item;}}},");
			}
			ca.add(Calendar.DAY_OF_MONTH, 1);
			columnsList.add(columnMap);
			
			}
	
		/*columnMap = new HashMap<String,String>();
		columnMap.put("title", "请假");
		columnMap.put("field", "holiday");
		columnMap.put("width", "80");
		width += 80;
		columnsList.add(columnMap);*/
		
		json.put("columnsSbf", columnsSbf);
		json.put("columns",columnsList );
		json.put("width", width);
		return json;
	}
	
	public JSONObject getAttendStatisticListQuery(Map<String,String> map) {
		JSONObject json = new JSONObject();
		Connection conn = null;
		CallableStatement cstmt  = null;
		try {
			conn = SessionFactoryUtils.getDataSource(attendDao.getSession().getSessionFactory()).getConnection();
			cstmt  = conn.prepareCall("{Call p_attendstatistic_export(?,?,?,?,?,?,?)}");  
			cstmt.setString(1, map.get("divid"));
			cstmt.setString(2, map.get("deptid") == null ? "":map.get("deptid"));
			cstmt.setString(3, map.get("date1"));
			cstmt.setString(4, map.get("date2"));
			cstmt.setInt(5, Integer.parseInt(map.get("pageno")));
			cstmt.setInt(6, Integer.parseInt(map.get("pagesize")));
			cstmt.setInt(7, Integer.parseInt(map.get("userid")));
			cstmt.execute();
			JSONArray columnsarr = JSON.parseArray(map.get("columns"));
			List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
			ResultSet rs = cstmt.getResultSet();
			int total = 0;
			int fest=0;
			int nianxiu,shijia,bingjia,tiaoxiu;
			nianxiu=shijia=bingjia=tiaoxiu=0;
			fest=getFestCount(map.get("divid"),map.get("date1"),map.get("date2"));
			int leaveday=0;
			
			while(rs.next()){
				Map<String,Object> m = new HashMap<String,Object>();
				m.put("name", rs.getString("name"));
				m.put("id", rs.getString("id"));
				m.put("dept",rs.getString("deptname"));
			    leaveday=getLeaveCount(rs.getString("id"),map.get("date1"),map.get("date2"));//获取该用户的请假总天数
			   
			   
			    String[] worktimes = rs.getString("worktimes").split(",");
				String[] isbadsarr = rs.getString("isbads").split(",");
				int baddays = 0;
				int normaldays = 0;
			    double hh2=0.0;
              	double totledays = 0;
				for(int i=0;i<columnsarr.size()-11;i++){//循环次数为总列表减去姓名、部门、正常天数、异常天数、总时长和工作时长、请假天数
					String item = worktimes[i];
					m.put(((JSONObject)columnsarr.get(i+11)).getString("field"),item );//从第六列获得数据worktimes[i],
					
					
					if(isbadsarr[i].equals("true")){
						baddays++;
					}else {
						normaldays++;
					}
					
					int hh=Integer.parseInt(worktimes[i].substring(0,2));
					int mm=Integer.parseInt(worktimes[i].substring(3,5));
					int ss=Integer.parseInt(worktimes[i].substring(6,8));
					int time=hh*3600+mm*60+ss;
					totledays+=time;
					
				}
				double tmp=(baddays+normaldays-fest-leaveday)*9*3600;//所选 时间段内正常工作时长
				 totledays-=tmp;
				 hh2=(totledays/3600);
				DecimalFormat df2  = new DecimalFormat("###.00");//保留两位小数
				m.put("baddays",baddays-fest-leaveday);
				m.put("normaldays",normaldays);
				m.put("totledays", Double.parseDouble(df2.format(hh2)));
				m.put("workdays", null);
				m.put("leavedays", leaveday);
				m.put("nianxiu", rs.getInt("nianxiu"));
				m.put("shijia", rs.getInt("shijia"));
				m.put("bingjia", rs.getInt("bingjia"));
				m.put("tiaoxiu", rs.getInt("tiaoxiu"));
				//m.put("dates", rs.getString("dates"));
				list.add(m);
			}
			ResultSet rs1 = null;
			if(cstmt.getMoreResults()){
				rs1 = cstmt.getResultSet();
			}
			
			
			while(rs1.next()){
				total = rs1.getInt("total");
			}
			json.put("rows", list);
			json.put("total", total);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			if(cstmt != null){
				try {
					cstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				cstmt = null;
			}
			if(conn != null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				conn = null;
			}
		}
		return json;
	}
}
