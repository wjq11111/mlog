package sto.service.account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sto.common.service.BaseServiceImpl;
import sto.common.util.Parameter;
import sto.dao.account.DataRulesDao;
import sto.dao.account.DoorRulesDao;
import sto.model.account.DataRules;
import sto.model.account.User;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

@Service
public class DataRulesService extends BaseServiceImpl<DataRules> {

	@Autowired
	private DataRulesDao dataRulesDao;
	@Autowired
	private DoorRulesDao doorRulesDao;
	@Resource
	private UserService userService;
	
	/**
	 * 获取该用户可查看哪些用户某类数据的用户id
	 * @param userid
	 * @param type 权限类型  1：
	 * @return 可见的用户id
	 */
	public String getDataAuth(int userid,int type){
		String sql = "select userid from mlog_data_rules c where c.secutype=:p1 and c.managerid=:p2 ";
		Parameter para = new Parameter();
		para.put("p1", type);
		para.put("p2", userid);
		List<Integer> list = dataRulesDao.findBySql(sql,para);
		String userids = "";
		if(list != null){
			String[] str = new String[list.size()+1];
			for(int i=0;i<list.size();i++){
				str[i] = String.valueOf(list.get(i));
			}
			str[list.size()] = userid+"";
			userids = StringUtils.join(str, ",");
		}else {
			String[] str = new String[1];
			str[0] = userid+"";
			userids = StringUtils.join(str, ",");
		}
		return userids;
	}
	/**
	 * 获取该用户的某类数据的可被哪些人查看的用户id
	 * @param userid
	 * @param type 权限类型  1：
	 * @return 
	 */
	public String getUseridsViewed(int userid,int type){
		String sql = "select managerid from mlog_data_rules c where c.secutype=:p1 and c.userid=:p2 ";
		Parameter para = new Parameter();
		para.put("p1", type);
		para.put("p2", userid);
		List<Integer> list = dataRulesDao.findBySql(sql,para);
		String userids = "";
		if(list != null){
			String[] str = new String[list.size()+1];
			for(int i=0;i<list.size();i++){
				str[i] = String.valueOf(list.get(i));
			}
			str[list.size()] = userid+"";
			userids = StringUtils.join(str, ",");
		}else {
			String[] str = new String[1];
			str[0] = userid+"";
			userids = StringUtils.join(str, ",");
		}
		return userids;
	}
	
	
	/**
	 * 获取该用户的可被哪些人查看的用户id
	 * @param userid
	 * @return 
	 */
	public String getManageids(int userid){
		String sql = "select distinct managerid from mlog_data_rules c where  c.userid=:p1 ";
		Parameter para = new Parameter();
		para.put("p1", userid);
		List<Integer> list = dataRulesDao.findBySql(sql,para);
		String userids = "";
		if(list != null){
			String[] str = new String[list.size()];
			for(int i=0;i<list.size();i++){
				str[i] = String.valueOf(list.get(i));
			}
			userids = StringUtils.join(str, ",");
		}
		return userids;
	}
	
	/**
	 * 获取该用户的某类数据的可被哪些人查看的用户id
	 * @param userid
	 * @param type 权限类型  1：
	 * @return 
	 */
	public List<User> getUsersViewed(int userid,int type){
		String sql = "select a.* from platform_t_user a, mlog_data_rules b where a.id=b.managerid "
				+ " and  b.secutype=:p1 and b.userid=:p2 group by a.id ";
		Parameter para = new Parameter();
		para.put("p1", type);
		para.put("p2", userid);
		return dataRulesDao.findBySql(sql, para, User.class);
	}
	//得到用户（管理员）的可见数据权限
	public List<String> getDataAuthTreeids(int type,int userid){
		User user = userService.findUniqueBy("id", userid);
		String sql = "select cast(concat(d.deptid,'_',c.userid) as char(22)) from mlog_data_rules c left join platform_t_user d on c.userid=d.id where d.isdelete=0 and d.isenable=1 and c.secutype=:p1 and c.managerid=:p2 ";
		Parameter para = new Parameter();
		para.put("p1", type);
		para.put("p2", userid);
		List<Integer> list = dataRulesDao.findBySql(sql,para);
		List<String> strList = new ArrayList<String>();
		if(list != null){
			for(int i=0;i<list.size();i++){
				strList.add(String.valueOf(list.get(i)));
			}
			//strList.add(user.getDept().getId()+"_"+userid); //SHM 20150908 和数据库保持一致，有记录显示，无记录不显示
		}/*else {   //SHM 20150908 和数据库保持一致，有记录显示，无记录不显示
			strList.add(user.getDept().getId()+"_"+userid);
		}*/   
		return strList;
	}
	//得到新用户（被管理者）的被管理数据权限
	public List<String> getNewUserDataAuthTreeids(int type,int userid){
		User user = userService.findUniqueBy("id", userid);
		String sql = "select cast(concat(d.deptid,'_',c.managerid) as char(22)) from mlog_data_rules c left join platform_t_user d on c.managerid=d.id where d.isdelete=0 and d.isenable=1 and c.secutype=:p1 and c.userid=:p2 ";
		Parameter para = new Parameter();
		para.put("p1", type);
		para.put("p2", userid);
		List<Integer> list = dataRulesDao.findBySql(sql,para);
		List<String> strList = new ArrayList<String>();
		if(list != null){
			for(int i=0;i<list.size();i++){
				strList.add(String.valueOf(list.get(i)));
			}
			//strList.add(user.getDept().getId()+"_"+userid); //SHM 20150908 和数据库保持一致，有记录显示，无记录不显示
		}/*else {
			strList.add(user.getDept().getId()+"_"+userid);
		}*/
		return strList;
	}
	
	/**
	 * 过滤getuserids中不具有权限的用户
	 * @param type
	 * @param userid
	 * @param getuserids
	 * @return
	 */
	public String filterAuthUser(int type,int userid,String getuserids){
		String sql = "select userid from mlog_data_rules c where c.secutype=:p1 and c.managerid=:p2 and c.userid in ("+getuserids+") ";
		Parameter para = new Parameter();
		para.put("p1", type);
		para.put("p2", userid);
		//para.put("p3", getuserids);
		List<Integer> list = dataRulesDao.findBySql(sql,para);
		String userids = "";
		boolean isContainSelf = false;//权限中是否包含自己
		if(list != null){
			userids = StringUtils.join(list, ",");
			for(int i=0;i<list.size();i++){
				if(String.valueOf(list.get(i)).equals(String.valueOf(userid))){
					isContainSelf = true;
				}
			}
		}
		//getuserids中是否包含自己
		String[] arr = getuserids.split(",");
		List idlist = Arrays.asList(arr);
		boolean paraIsContainSelf = idlist.contains(String.valueOf(userid));
		if(paraIsContainSelf && !isContainSelf){//传参包含,权限不包含
			if(userids.isEmpty()){
				userids = ""+userid;
			}else {
				userids +=","+userid;
			}
		}
		return userids;
	}
	/**
	 * 获取userid用户的可见权限用户
	 * @param userid
	 * @param type
	 * @return 返回userid字符串，以逗号分隔
	 */
	public String getAuthContacts(int userid,int type){
		String sql = " select group_concat(userid) userids from mlog_data_rules where managerid="+userid+" and secutype="+type;
		List<Object> list = dataRulesDao.findBySql(sql);
		if(list.size()>0){
			return (String)list.get(0);
		}
		return null;
	}
	
	public String[] getAuthContactsArrays(int userid,int type){
		String sql = " select userid from mlog_data_rules where managerid="+userid+" and secutype="+type;
		List<Integer> list = dataRulesDao.findBySql(sql);
		String[] str = null;
		if(list.size()>0){
			str = new String[list.size()];
			for(int i=0;i<list.size();i++){
				str[i] = String.valueOf(list.get(i));
			}
		}
		return str;
	}
	@Transactional(readOnly = false)
	public void saveDataAuth(int managerid,String types,String userids){
		String[] typearr = types.split(",");
		String[] useridarr = userids.split(",");
		for(String type : typearr){
			dataRulesDao.delete("delete from DataRules where managerid="+managerid+" and secutype="+Integer.parseInt(type),null);
			for(String userid : useridarr){
				if(userid!="" && userid!=null){
				dataRulesDao.updateBySql("insert into mlog_data_rules(userid,managerid,secutype) values("+Integer.parseInt(userid)+","+managerid+","+Integer.parseInt(type)+")", null);
				}
			}
		}
	}
	
	@Transactional(readOnly = false)
	public void saveNewUserDataAuth(int userid,String types,String managerids){
		String[] typearr = types.split(",");
		String[] manageridarr = managerids.split(",");
		for(String type : typearr){
			dataRulesDao.delete("delete from DataRules where userid="+userid+" and secutype="+Integer.parseInt(type),null);
			for(String managerid : manageridarr){
				if(managerid!="" && managerid!=null){
				dataRulesDao.updateBySql("insert into mlog_data_rules(userid,managerid,secutype) values("+userid+","+Integer.parseInt(managerid)+","+Integer.parseInt(type)+")", null);
			   }
			}
		}
	}
	
	public List<Map<String,Object>> getAuthContactsTree(Map<String,Object> parammap){
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		String sql = "select cast(id as char(22)) id,pId,name,isParent,divid from ( "
				+" select id,parentid pId,deptname name,'true' isParent,a.divid divid "
				+" from mlog_dept a "
				+" union all "
				+" select cast(concat(b.id,'_',a.id) as char(22)) id,b.id pId,a.name name,'false' isParent,a.divid divid "
				+" from platform_t_user a,mlog_dept b where a.deptid=b.id and a.divid=b.divid "
				+" and a.deptid is not null "
				+" ) c where 1=1 ";
		Parameter parameter = new Parameter();
		parameter.put("p1",  parammap.get("divid"));
		sql += " and c.divid=:p1 ";
		
		List<Object[]> list = doorRulesDao.findBySql(sql, parameter);
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf((o[0])));
				map.put("pId", String.valueOf(o[1]));
				map.put("name", String.valueOf(o[2]));
				map.put("isParent", String.valueOf(o[3]));
				map.put("divid", String.valueOf(o[4]));
				JSONObject json = new JSONObject();
				json.putAll(map);
				System.out.println(JSON.toJSONString(json));
				list1.add(map);
			}
		}
		
		return list1;
	}
	//得到门禁系统授权用户树
	public List<Map<String,Object>> getDoorAuthContactsTree(Map<String,Object> parammap){
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		String sql = "select cast(id as char(22)) id,pId,name,isParent,divid from ( "
				+" select id,parentid pId,deptname name,'true' isParent,a.divid divid "
				+" from mlog_dept a "
				+" union all "
				+" select cast(concat(b.id,'_',a.id) as char(22)) id,b.id pId,a.name name,'false' isParent,a.divid divid "
				+" from platform_t_user a,mlog_dept b,mlog_door_rules d where a.deptid=b.id and a.divid=b.divid and d.userid=a.id and d.accesscontrolid=:p2 "
				+" and a.deptid is not null and a.isdelete=0"
				+" ) c where 1=1 ";
		Parameter parameter = new Parameter();
		parameter.put("p1",  parammap.get("divid"));
		parameter.put("p2", parammap.get("doorid"));
		sql += " and c.divid=:p1 ";
		
		List<Object[]> list = doorRulesDao.findBySql(sql, parameter);
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf((o[0])));
				map.put("pId", String.valueOf(o[1]));
				map.put("name", String.valueOf(o[2]));
				map.put("isParent", String.valueOf(o[3]));
				map.put("divid", String.valueOf(o[4]));
				JSONObject json = new JSONObject();
				json.putAll(map);
				System.out.println(JSON.toJSONString(json));
				list1.add(map);
			}
		}
		
		return list1;
	}
	
	//得到门禁系统未授权用户树
	public List<Map<String,Object>> getDoorUnauthContactsTree(Map<String,Object> parammap){
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		String sql = "select cast(id as char(22)) id,pId,name,isParent,divid from ( "
				+" select id,parentid pId,deptname name,'true' isParent,a.divid divid "
				+" from mlog_dept a "
				+" union all "
				+" select cast(concat(b.id,'_',a.id) as char(22)) id,b.id pId,a.name name,'false' isParent,a.divid divid "
				+" from platform_t_user a,mlog_dept b where a.deptid=b.id and a.divid=b.divid and a.id not in(SELECT userid from mlog_door_rules WHERE accesscontrolid=:p2)  "
				+" and a.deptid is not null and a.isdelete=0 "
				+" ) c where 1=1 ";
		Parameter parameter = new Parameter();
		parameter.put("p1",  parammap.get("divid"));
		parameter.put("p2", parammap.get("doorid"));
		sql += " and c.divid=:p1 ";
		
		List<Object[]> list = doorRulesDao.findBySql(sql, parameter);
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf((o[0])));
				map.put("pId", String.valueOf(o[1]));
				map.put("name", String.valueOf(o[2]));
				map.put("isParent", String.valueOf(o[3]));
				map.put("divid", String.valueOf(o[4]));
				JSONObject json = new JSONObject();
				json.putAll(map);
				System.out.println(JSON.toJSONString(json));
				list1.add(map);
			}
		}
		
		return list1;
	}
	//保存门禁权限信息
	@Transactional(readOnly = false)
	public void saveDoorAuth(String userids,int doorid){
		String[] useridarr = userids.split(",");
				for(String userid : useridarr){
				if(userid!="" && userid!=null){
					doorRulesDao.updateBySql("insert into mlog_door_rules(userid,accesscontrolid) values("+Integer.parseInt(userid)+","+doorid+")", null);
				}
			}
	}
	
	//删除门禁权限信息
		@Transactional(readOnly = false)
		public void deleteDoorAuth(String userids,int doorid){
			String[] useridarr = userids.split(",");
					for(String userid : useridarr){
					if(userid!="" && userid!=null){
						doorRulesDao.updateBySql("delete from mlog_door_rules where userid="+userid+" and accesscontrolid="+doorid,null);
					}
				}
		}

}
