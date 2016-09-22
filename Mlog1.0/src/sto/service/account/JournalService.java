package sto.service.account;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

import other.AuthProfile;
import sto.common.service.BaseServiceImpl;
import sto.common.util.DateUtil;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.common.util.RoleType;
import sto.common.util.StringReg;
import sto.dao.account.DataRulesDao;
import sto.dao.account.JournalDao;
import sto.form.JournalForm;
import sto.form.JournalReplyForm;
import sto.form.MsgForm;
import sto.form.MsgReplyForm;
import sto.model.account.Journal;
import sto.model.account.SysSettings;

/**
 * @ClassName: RoleService
 * @Description: service
 * @author zzh
 * @date 2014-11-5 11:07:12
 * 
 */
@Service
@SuppressWarnings("unchecked")
public class JournalService extends BaseServiceImpl<Journal>{
	@Autowired
	private JournalDao journalDao;
	@Resource
	JournalReplyService journalReplyService;
	@Resource
	private DataRulesService dataRulesService;
	public List<JournalForm> getJournalList(Page<Object[]> page,Map<String,Object> parammap){
		int managerid = Integer.parseInt(String.valueOf(parammap.get("userid")));
		String getuserids = String.valueOf(parammap.get("getuserid"));
		String date = String.valueOf(parammap.get("date"));
		String whereSql = "";
		String whereSql1="";
		if(!StringUtils.isBlank(getuserids) && !getuserids.equals("null")){
			//String userids = dataRulesService.filterAuthUser(2,managerid,getuserids);//SHM 20150906 将IN 查询修改为关联方式
			//whereSql = " and a.writer in ("+userids+") ";
			whereSql1 = " (select userid from mlog_data_rules c where c.secutype=2 and c.managerid="+managerid+" and c.userid in ("+getuserids+")) uu ";
		}else {
			//String userids = dataRulesService.getDataAuth(managerid, 2);
			whereSql1 = " (select userid from mlog_data_rules c where c.secutype=2 and c.managerid="+managerid+") uu ";
		}
		
		if(!StringUtils.isBlank(date) && !date.equals("null")) {
			whereSql += " and str_to_date(a.createtime,'%Y-%m-%d') <= str_to_date('"+date+"','%Y-%m-%d') ";
		}
		String sql = "select a.id,a.writer userid,b.name username,a.content,a.image,a.createtime time,a.lgt longitude,a.lat latitude,a.addr address "
				+" from mlog_journal a,platform_t_user b, "
				+ whereSql1
				+" where a.writer=b.id and a.writer=uu.userid "
				+ whereSql
				+ " order by a.createtime desc ";
		
		List<Object[]> list = journalDao.findBySql(page, sql).getResult();
		List<JournalForm> formlist = new ArrayList<JournalForm>();
		if(list != null){
			for(Object[] o : list){
				JournalForm form = new JournalForm();
				form.setId(Integer.parseInt(String.valueOf(o[0])));
				form.setUserid(Integer.parseInt(String.valueOf(o[1])));
				form.setUsername(String.valueOf(o[2]));
				form.setContent(StringReg.replaceToHref(String.valueOf(o[3])));
				form.setImage(String.valueOf(o[4]));
				form.setTime(String.valueOf(o[5]));
				form.setLongitude(String.valueOf(o[6]));
				form.setLatitude(String.valueOf(o[7]));
				form.setAddress(String.valueOf(o[8]));
				List<JournalReplyForm> replyList = journalReplyService.getJournalReplyList(form.getId());
				form.setReplies(replyList);
				formlist.add(form);
			}
		}
		return formlist;
	}
	
	public List<Map<String,Object>> getJournalAllList(Page<Object[]> page,Map<String,Object> parammap){
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		//String userids = dataRulesService.getDataAuth(auth.getUser().getId(),2); //SHM 20150906 日志查询方式由IN方式修改为关联方式
		String userids="(select userid from mlog_data_rules c where c.secutype=2 and c.managerid="+auth.getUser().getId()+") uu";
		Parameter parameter = new Parameter();
		String whereSql = "";
		if(parammap.get("startDate") != null && !parammap.get("startDate").equals("")){
			whereSql += " and str_to_date(a.createtime,'%Y-%m-%d')>=str_to_date(:p1,'%Y-%m-%d') ";
			parameter.put("p1",  parammap.get("startDate"));
		}
		
		if(parammap.get("endDate") != null && !parammap.get("endDate").equals("")){
			whereSql += " and str_to_date(a.createtime,'%Y-%m-%d')<=str_to_date(:p2,'%Y-%m-%d') ";
			parameter.put("p2",  parammap.get("endDate"));
		}
		
		/*if(parammap.get("name") != null && !parammap.get("name").equals("")){
			whereSql += " and b.name like :p3 ";
			parameter.put("p3",  "%"+parammap.get("name")+"%");
		}*/
		if(parammap.get("divid") != null && !parammap.get("divid").equals("")){
		String divid = parammap.get("divid").toString();
		}else{
		if(RoleType.UNIT_USERGROUP.getName().contains(auth.getRole().getEnname())){
			whereSql +=" and b.divid=:p3 ";
			parameter.put("p3",  auth.getUser().getUnit().getDivid());
		  }
		}
		if(parammap.get("deptid") != null && !parammap.get("deptid").equals("")){
			whereSql += " and b.deptid=:p4 ";
			parameter.put("p4",  parammap.get("deptid"));
		}
		String sql = "select a.id,a.writer userid,b.username,c.deptname,b.name,a.content,replace(a.image,'\\\\','\\/') as image,date_format(a.createtime,'%Y-%m-%d') time,a.lgt longitude,a.lat latitude,a.addr address, "
				+" case when (select count(1) from mlog_journal_reply c where a.id=c.journalid)>0 then 1 else 0 end flag,a.iswarn "
				+" from mlog_journal a,platform_t_user b,mlog_dept c, "
				+  userids
				+" where a.writer=b.id and b.deptid=c.id and a.writer=uu.userid "
				+ whereSql
				+" and a.content is not null order by time desc,deptname asc ";
			//	+" and a.writer in ("+userids+") and a.content is not null order by time desc,deptname asc ";
				
		
		List<Object[]> list = journalDao.findBySql(page, sql, parameter).getResult();
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf(o[0]));
				map.put("userid", String.valueOf(o[1]));
				map.put("username", String.valueOf(o[2]));
				map.put("deptid", String.valueOf(o[3]));
				map.put("name", String.valueOf(o[4]));
				map.put("content",String.valueOf(o[5]));
				map.put("image", String.valueOf(o[6]));
				map.put("time", String.valueOf(o[7]));
				map.put("longitude", String.valueOf(o[8]));
				map.put("latitude", String.valueOf(o[9]));
				map.put("address", String.valueOf(o[10]));
				map.put("flag", String.valueOf(o[11]));
				map.put("iswarn", String.valueOf(o[12]));
				list1.add(map);
			}
		}
		return list1;
	}
	
	public List<Map<String,Object>> getJournalWordList(Map<String,Object> parammap){
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		//String userids = dataRulesService.getDataAuth(auth.getUser().getId(),2);//SHM 20150906 日志查询方式由IN方式修改为关联方式
		String userids="(select userid from mlog_data_rules c where c.secutype=2 and c.managerid="+auth.getUser().getId()+") uu";
		Parameter parameter = new Parameter();
		String whereSql = "";
		if(parammap.get("startDate") != null && !parammap.get("startDate").equals("")){
			whereSql += " and str_to_date(a.createtime,'%Y-%m-%d')>=str_to_date(:p1,'%Y-%m-%d') ";
			parameter.put("p1",  parammap.get("startDate"));
		}
		
		if(parammap.get("endDate") != null && !parammap.get("endDate").equals("")){
			whereSql += " and str_to_date(a.createtime,'%Y-%m-%d')<=str_to_date(:p2,'%Y-%m-%d') ";
			parameter.put("p2",  parammap.get("endDate"));
		}
		
		/*if(parammap.get("name") != null && !parammap.get("name").equals("")){
			whereSql += " and b.name like :p3 ";
			parameter.put("p3",  "%"+parammap.get("name")+"%");
		}*/
		if(parammap.get("divid") != null && !parammap.get("divid").equals("")){
		String divid = parammap.get("divid").toString();
		}else{
		if(RoleType.UNIT_USERGROUP.getName().contains(auth.getRole().getEnname())){
			whereSql +=" and b.divid=:p3 ";
			parameter.put("p3",  auth.getUser().getUnit().getDivid());
		  }
		}
		if(parammap.get("deptid") != null && !parammap.get("deptid").equals("")){
			whereSql += " and b.deptid=:p4 ";
			parameter.put("p4",  parammap.get("deptid"));
		}
		String sql = "select a.id,a.writer userid,b.username,c.deptname,b.name,a.content,replace(a.image,'\\\\','\\/') as image,date_format(a.createtime,'%Y-%m-%d') time,a.lgt longitude,a.lat latitude,a.addr address, "
				+" case when (select count(1) from mlog_journal_reply c where a.id=c.journalid)>0 then 1 else 0 end flag,a.iswarn "
				+" from mlog_journal a,platform_t_user b,mlog_dept c, "
				+  userids
				+" where a.writer=b.id and b.deptid=c.id and a.writer=uu.userid "
				+ whereSql
				+" and a.content is not null order by time desc,deptname asc ";
			//	+" and a.writer in ("+userids+") and a.content is not null order by time desc,deptname asc ";
		
		List<Object[]> list = journalDao.findBySql(sql, parameter);
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf(o[0]));
				map.put("userid", String.valueOf(o[1]));
				map.put("username", String.valueOf(o[2]));
				map.put("deptid", String.valueOf(o[3]));
				map.put("name", String.valueOf(o[4]));
				map.put("content",String.valueOf(o[5]));
				map.put("image", String.valueOf(o[6]));
				map.put("time", String.valueOf(o[7]));
				map.put("longitude", String.valueOf(o[8]));
				map.put("latitude", String.valueOf(o[9]));
				map.put("address", String.valueOf(o[10]));
				map.put("flag", String.valueOf(o[11]));
				map.put("iswarn", String.valueOf(o[12]));
				list1.add(map);
			}
		}
		return list1;
	}
	
	public List<Map<String,Object>> getJournalReplyByJournalId(Map<String,Object> parammap){
		String sql = "select a.id,a.journalid,a.replyer userid,b.name,b.username,a.recontent,a.reimage,date_format(a.redate,'%Y-%m-%d %H:%m:%s') redate "
				+" from mlog_journal_reply a,platform_t_user b "
				+" where a.replyer=b.id "
				+" and a.journalid=:p1 order by a.redate asc ";
		Parameter parameter = new Parameter();
		parameter.put("p1",  parammap.get("journalid"));
		List<Object[]> list = journalDao.findBySql(sql, parameter);
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();
		if(list != null){
			for(Object[] o : list){
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("id", String.valueOf(o[0]));
				map.put("journalid", String.valueOf(o[1]));
				map.put("userid", String.valueOf(o[2]));
				map.put("name", String.valueOf(o[3]));
				map.put("username", String.valueOf(o[4]));
				map.put("recontent", String.valueOf(o[5]));
				map.put("reimage", String.valueOf(o[6]));
				map.put("redate", String.valueOf(o[7]));
				list1.add(map);
			}
		}
		return list1;
	}

/**
 * 
 * @param map
 * @return
 * @throws SQLException
 */
public static JSONObject getJournalStatisticQueryColumns(Map<String,String> map) throws SQLException{
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
	columnMap.put("width", "100");
	width += 80;
	columnsList.add(columnMap);
	columnsSbf.append("{field:'dept', title:'部门', width:100},");
	columnMap = new HashMap<String,Object>();
	columnMap.put("title", "姓名");
	columnMap.put("field", "name");
	columnMap.put("width", "80");
	width += 80;
	columnsList.add(columnMap);
	columnsSbf.append("{field:'name', title:'姓名', width:80},");
	
	columnMap = new HashMap<String,Object>();
	columnMap.put("title", "应填天数");
	columnMap.put("field", "needdays");
	columnMap.put("width", "80");
	width += 80;
	columnsList.add(columnMap);
	columnsSbf.append("{field:'normaldays', title:'应填天数', width:80},");
	
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
}