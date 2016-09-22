package sto.service.account;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.SessionFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import other.AuthProfile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import sto.common.service.BaseServiceImpl;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.common.util.RoleType;
import sto.common.util.StringUtils;
import sto.dao.account.FestDao;
import sto.dao.account.LeaveDao;
import sto.dao.account.UserDao;
import sto.form.AttendForm2;
import sto.form.AttendForm3;
import sto.form.MsgReplyForm;
import sto.model.account.Leave;
import sto.model.account.User;

/**
 * @ClassName: RoleService
 * @Description: service
 * @author zzh
 * @date 2014-11-5 11:07:12
 * 
 */
@Service
@SuppressWarnings("unchecked")
public class LeaveService extends BaseServiceImpl<Leave>{
	@Autowired
	private LeaveDao leaveDao;
	@Autowired
	private UserDao userDao;
	@Transactional(readOnly = false)
	public void save(Leave leave) {
		
		leaveDao.updateBySql("insert into mlog_leave (divid,deptid,userid,leavedate,leavetype) values('" + leave.getUnit().getDivid()+ "'," +leave.getDept().getId()+","+leave.getUser().getId()+",'"+ leave.getLeavedate()+"','"+leave.getLeavetype() +"')", null);
		leaveDao.flush();
		//	}
	//	festDao.flush();
	}
	

	public boolean checkDateIsExist(String userid,String date) {
		String sql = " select * from mlog_leave where ";
		Parameter parameter = new Parameter();
		parameter.put("p1", userid);
	        parameter.put("p2", date);
		String whereSql = " userid=:p1 and leavedate=:p2 ";
		
		sql += whereSql;
			
		List<Leave> list = leaveDao.findBySql(sql, parameter, Leave.class);
		return list == null || list.size()<=0 ? false : true;
	}
	
	


	public List<Map<String, Object>> getLeaveList(Map<String, String> m) {
		// TODO Auto-generated method stub
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String hql = " from Leave where 1=1 ";
		
		String divid = m.get("divid");
		String deptid = m.get("deptid");
		String date1=m.get("date1");
		String date2=m.get("date2");
		
		
		if(RoleType.UNIT_USERGROUP.getName().contains(auth.getRole().getEnname())){
			 divid = auth.getUser().getUnit().getDivid();
		}
		System.out.print(divid);

		Parameter parameter = new Parameter();
		if(!StringUtils.isBlank(divid)){
			hql +=" and divid=:p1 ";
			parameter.put("p1", divid);
		}
		if(!StringUtils.isBlank(deptid)){
			hql +=" and deptid=:p2 ";
			parameter.put("p2", deptid);
		}
		if(!StringUtils.isBlank(date1) && !StringUtils.isBlank(date2)){
			hql +=" and (leavedate>=str_to_date(:p3,'%Y-%m-%d') and leavedate<=str_to_date(:p4,'%Y-%m-%d')) ";
			parameter.put("p3", date1);
			parameter.put("p4", date2);
		}
		
		List<Leave> leave = leaveDao.find(hql, parameter);
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		for(Leave l: leave ){
			Map<String,Object> m1 = new HashMap<String,Object>();
			m1.put("name", l.getUser().getName());
			m1.put("div", l.getDivname());
			m1.put("leavedate", l.getLeavedate());
			m1.put("leavetype", l.getLeavetype());
			m1.put("dept",l.getDeptname());
			
			//m.put("dates", rs.getString("dates"));
			list.add(m1);
		}
		
		return list;
	}

	public List<Map<String, Object>> getLeaveListByUserid(Page<Object[]> p,Map<String, Object> parammap) {
		// TODO Auto-generated method stub
		Parameter parameter = new Parameter();
		parameter.put("p1", parammap.get("stardate"));
		parameter.put("p2", parammap.get("enddate"));
		//parameter.put("p2", parammap.get("userid"));
		parameter.put("p3", parammap.get("userid"));
		//parameter.put("p4", parammap.get("userid"));
		String sql = "select * from mlog_leave where (leavedate>=str_to_date(:p1,'%Y-%m-%d') and leavedate<=str_to_date(:p2,'%Y-%m-%d')) and userid=:p3";
		
		List<Object[]> list = leaveDao.findBySql(p,sql, parameter).getResult();
		
		
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(Object[] o : list){
				String sql1=" from User  WHERE id="+Integer.parseInt(String.valueOf(o[3]));
				List<User> newlist=userDao.find(sql1);
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("userid", newlist.get(0).getName());
				map.put("id", String.valueOf(o[0]));
				map.put("divid", (Object)newlist.get(0).getDivname());
				map.put("deptid", (Object)newlist.get(0).getDeptname());
				
				map.put("leavedate", String.valueOf(o[4]));
				map.put("leavetype", String.valueOf(o[5]));
				
				list1.add(map);
			}
		}
		
		return list1;
	}
}
