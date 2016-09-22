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
import sto.form.AttendForm2;
import sto.form.AttendForm3;
import sto.model.account.Fest;

/**
 * @ClassName: RoleService
 * @Description: service
 * @author zzh
 * @date 2014-11-5 11:07:12
 * 
 */
@Service
@SuppressWarnings("unchecked")
public class FestService extends BaseServiceImpl<Fest>{
	@Autowired
	private FestDao festDao;
	
	@Transactional(readOnly = false)
	public void save(Fest fest) {
		// 保存节日信息到fest表
	
	//	festDao.save(fest);
		// 保存角色信息 , 先删除原来的角色信息，再插入新的角色信息
	//	festDao.delete("delete from RoleUser where user.id=:p1", new Parameter(fest.getId()));
	//	for(int i=0;i<roleids.length;i++){
		System.out.print(fest.getDate());
		//System.out.println(fest.getUnit().getDivid());
		//festDao.updateBySql("insert into mlog_fest (ID, divid, date, remark) values(" +"'"+fest.getId()+ "','"+ fest.getUnit().getDivid()+ "','" + fest.getDate()+"','"+fest.getRemark() +"')", null);
		festDao.updateBySql("insert into mlog_fest (divid, date, remark) values('" + fest.getUnit().getDivid()+ "','" + fest.getDate()+"','"+fest.getRemark() +"')", null);
		festDao.flush();
		//	}
	//	festDao.flush();
	}
	

	public boolean checkDateIsExist(String divid,String date) {
		String sql = " select * from mlog_fest where ";
		Parameter parameter = new Parameter();
		parameter.put("p1", divid);
	        parameter.put("p2", date);
		String whereSql = " divid=:p1 and date=:p2 ";
		
		sql += whereSql;
			
		List<Fest> list = festDao.findBySql(sql, parameter, Fest.class);
		return list == null || list.size()<=0 ? false : true;
	}
}
