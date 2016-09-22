package sto.service.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sto.common.service.BaseServiceImpl;
import sto.common.util.Parameter;
import sto.dao.account.DeptDao;
import sto.form.DeptForm;
import sto.model.account.Dept;
import sto.model.account.User;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@Service
public class DeptService extends BaseServiceImpl<Dept> {

	@Autowired
	private DeptDao deptDao;

	public Dept get(String id) {
		return deptDao.get(id);
	}
	
	public List<Dept> find(String qlString, Parameter parameter) {
		return deptDao.find(qlString, parameter);
	}
	
	public List<DeptForm> getAllDeptForms(){
		String sql = "select parentid,deptid,deptname from mlog_dept order by level ";
		List<Object[]> list = deptDao.findBySql(sql);
		List<DeptForm> formList = new ArrayList<DeptForm>();
		for(Object[] o:list){
			DeptForm form = new DeptForm();
			form.setDeptid((String)o[0]);
			form.setDeptname((String)o[1]);
			formList.add(form);
		}
		return formList;
	}
	
	public boolean isDeptHasUsers(Dept dept){
		boolean isHas = true;
		if(dept.getContacts().size()>0){
			isHas = true;
		}else {
			if(dept.getChildren().size()>0){
				for(Dept d : dept.getChildren()){
					isHas =	isDeptHasUsers(d);
				}				
			}else {
				isHas =  false;
			}
		}
		return isHas;
	}
	public Dept getDeptNameByid(String deptid) {
		Parameter parameter = new Parameter();
		parameter.put("p2", deptid);
		String sql = " select * from mlog_dept  where id=:p2 ";
		List<Dept> list = deptDao.findBySql(sql, parameter, Dept.class);
		return list == null || list.size()<=0 ? null : list.get(0);
	}

}
