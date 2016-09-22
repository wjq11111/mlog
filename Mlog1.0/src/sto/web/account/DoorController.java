
package sto.web.account;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
//import org.springside.modules.web.struts2.Struts2Utils;


import other.AuthProfile;
import sto.common.Md5Encrypt;
import sto.common.MlogPM;
import sto.common.sms.SmsController;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.common.util.RoleType;
import sto.common.web.BaseController;
import sto.model.account.Door;
import sto.model.account.Module;
import sto.model.account.Role;
import sto.model.account.RoleModule;
import sto.model.account.UnitSettings;
import sto.model.account.User;
import sto.service.account.DataRulesService;
import sto.service.account.DeptService;
import sto.service.account.DoorService;
import sto.service.account.ModuleService;
import sto.service.account.RoleModuleService;
import sto.service.account.RoleService;
import sto.service.account.UnitService;
import sto.service.account.UnitSettingsService;
import sto.service.account.UserService;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hebca.sms.ShortMessage;
import com.hebca.sms.SmsException;
import com.hebca.sms.SmsProvider;

/**
 * 门禁管理
 * 功能：列表、分配角色
 * 
 */
@Controller
@RequestMapping(value = "/door")
@SuppressWarnings("unchecked")
public class DoorController extends BaseController{
	private static Logger log = Logger.getLogger(DoorController.class);
	@Resource  
	protected DoorService doorService;
	@Resource  
	protected UserService userService;
	@Resource
	DeptService deptService;
	@Resource
	RoleService roleService;
	@Resource
	private DataRulesService dataRulesService;
	@Resource
	UnitService unitService;
	@Resource
	UnitSettingsService unitSettingsService;
	@Resource  
	protected ModuleService moduleService;
	@Resource  
	protected RoleModuleService roleModuleService;
	//private String testflag;
	/**
	 * 列表页面
	 */
	@RequestMapping("/list.action")
	public String list() {
		return "account/doorcontroldefine";
	}
	/**
	 * 列表返回json
	 */
	@RequestMapping("/listJson.action")
	@ResponseBody
	public Map listJson(Model model, HttpServletRequest request) {

		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String hql = " from Door where 1=1 ";

		String doorname = request.getParameter("doorname");
		
		if(!StringUtils.isBlank(doorname)){
			hql +=" and (doorname like '"+doorname+"%' ) ";
		}
		
		Map<String, Object> m = new HashMap<String, Object>();
		Page<Door> p = new Page<Door>(Integer.parseInt(request.getParameter("rows")==null?"10":request.getParameter("rows")));
		p.setPageNo(Integer.parseInt(request.getParameter("page")==null?"1":request.getParameter("page")));
		p.setOrder("id:asc");
		Page<Door> resultPage = doorService.find(p, hql, null);
		m.put("rows", resultPage.getResult()==null ? "":resultPage.getResult());
		m.put("total", resultPage.getTotalCount());
		return m;
	}
	
	/**
	 * 列表返回json,用于下拉列表显示
	 */
	/*@RequestMapping("/checklistJson.action")
	@ResponseBody
	public List<User> checklistJson(Model model, HttpServletRequest request) {

		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String hql = " select * from platform_t_user where 1=1 and isdelete=0 and isenable=1 ";

		String username = request.getParameter("username");
		String divid = request.getParameter("divid");
		String deptid = request.getParameter("deptid");
		if(RoleType.UNIT_USERGROUP.getName().contains(auth.getRole().getEnname())){
			divid = auth.getUser().getUnit().getDivid();
		}
		if(!StringUtils.isBlank(username)){
			hql +=" and (username like '"+username+"%' or name like '"+username+"%') ";
		}
		Parameter parameter = new Parameter();
		if(!StringUtils.isBlank(divid)){
			hql +=" and divid=:p1 ";
			parameter.put("p1", divid);
		}
		if(!StringUtils.isBlank(deptid)){
			hql +=" and deptid=:p2 ";
			parameter.put("p2", deptid);
		}

		List<User> m = userService.findBySql(hql, parameter, User.class);
		
		return m;
	}*/
	

	/**
	 * 新增
	 */
	@RequestMapping(value="/save.action")
	public String save() {
		return "account/doorSave";
	}
	/**
	 * 新增保存
	 */
	@RequestMapping(value="/saveDo.action",method=RequestMethod.POST)
	@ResponseBody
	public Map saveDo(Door r,HttpServletRequest request) {
		
		String controlid = request.getParameter("controlid");
		String portid = request.getParameter("portid");
		String doorname = request.getParameter("doorname");
		String doorstatus =request.getParameter("isenable");
		
		if(!StringUtils.isBlank(controlid) && !StringUtils.isBlank(portid)){
			Parameter param = new Parameter();
			String sql = " select count(1) from platform_t_accesscontrol a where a.controllerid=:p1 and a.portid=:p2 ";
			param.put("p1", controlid);
			param.put("p2", portid);
			int count = Integer.parseInt(String.valueOf(doorService.findBySql(sql, param, null).get(0)));
			if(count > 0){
				return err("门禁系统已存在");
			}
		}
		if(!StringUtils.isBlank(controlid)){
			Parameter param = new Parameter();
			String sql = " select count(1) from platform_t_accesscontrol a where a.doorname=:p1 ";
			param.put("p1",doorname);
			int count = Integer.parseInt(String.valueOf(doorService.findBySql(sql, param, null).get(0)));
			if(count > 0){
				return err("门禁名称已存在，请更换新名称！");
			}
		}

		Door door = new Door();
		BeanUtils.copyProperties(r,door);

		
		door.setControllerid(controlid);
		door.setDoorname(doorname);
		door.setPortid(portid);
		door.setDoorstatus(doorstatus);
		door.setRegularflag(0);
		doorService.save(door);
		//door.getId();
		//增加门禁菜单,插入至数据库platform_t_module
		Module module=new Module();
		module.setCurl("/door/doorlist.action?doorflag='"+door.getId()+"'");
		module.setStatus(1);
		module.setOrderid(door.getId()+1);
		module.setName(doorname);
		module.setSuperid(moduleService.findUniqueBy("name", "门禁管理").getId());
		moduleService.save(module);
		
		roleService.updateBySql("insert into platform_t_role_module(roleid,moduleid) values(1," + module.getId()+")", null);
		
		return suc();
	}

	/**
	 * 修改页面
	 */
	@RequestMapping(value="/update.action")
	public String update(HttpServletRequest request,Model model) {
		model.addAttribute("o",doorService.get(Integer.parseInt(request.getParameter("id"))));
		return "account/doorUpdate";
	}

	/**
	 * 修改保存
	 */
	@RequestMapping(value="/updateDo.action")
	@ResponseBody
	public Map updateDo(Door o,HttpServletRequest request) {
		
		String doorname = request.getParameter("doorname");
		String isenable = request.getParameter("isenable");
		Door door = doorService.get(Integer.parseInt(request.getParameter("id")));
		
		//检验是否更改门禁状态，如果更改需要发信息给控制器
		if(!door.getDoorstatus().equals(isenable)){
			//给控制器发送门禁状态信息
			
			door.setDoorstatus(isenable);
		}
        
        door.setDoorname(doorname);
		
		
		doorService.save(door);
		
		return suc();
	}
	/**
	 * 开启门禁
	 */
	@RequestMapping(value="/openStatusDo.action")
	@ResponseBody
	public Map openStatusDo(String ids) {
		
		if(StringUtils.isNotBlank(ids)) {
			ids = ids.replaceAll(",", "','");
			doorService.update("update Door set doorstatus=1  where id in('"+ids+"')", null);
		}
		
		
		return suc();
	}
	/**
	 * 关闭门禁
	 */
	@RequestMapping(value="/shutStatusDo.action")
	@ResponseBody
	public Map shutStatusDo(String ids) {
		
		if(StringUtils.isNotBlank(ids)) {
			ids = ids.replaceAll(",", "','");
			doorService.update("update Door set doorstatus=0  where id in('"+ids+"')", null);
		}
		
		
		return suc();
	}
	/**
	 * 删除
	 */
/*	@RequestMapping(value="/delete.action")
	@ResponseBody
	public Map<String, Object> delete(String ids) {
		if(StringUtils.isNotBlank(ids)) {
			ids = ids.replaceAll(",", "','");
			userService.update("update User set isdelete=1,isenable=0  where id in('"+ids+"')", null);
			//修改通讯录更新时间
			String sql="select * from platform_t_user where id in('"+ids+"')";
			List<User> user = userService.findBySql(sql, null, User.class);
			for(User t : user){
				unitSettingsService.lastupdatedSettings(t.getUnit().getDivid());
			}
		}
		//userService.delete("delete from User where id in('"+ids+"')", null);
		
		return suc();
	}*/

/*	@RequestMapping(value="/unfreeze.action")
	@ResponseBody
	public Map<String, Object> unfreeze(String ids) {
		if(StringUtils.isNotBlank(ids)) {
			ids = ids.replaceAll(",", "','");
			userService.update("update User set isdelete=0,isenable=1  where id in('"+ids+"')", null);
			//修改通讯录更新时间
			String sql="select * from platform_t_user where id in('"+ids+"')";
			List<User> user = userService.findBySql(sql, null, User.class);
			for(User t : user){
				unitSettingsService.lastupdatedSettings(t.getUnit().getDivid());
			}
		}
		return suc();
	}
	@RequestMapping(value="/bindCert.action")
	@ResponseBody
	public JSONObject bindCert(HttpServletRequest request) {
		JSONObject json = new JSONObject();
		String certcn = request.getParameter("certcn");
		String userid = request.getParameter("ids");
		String flag = request.getParameter("flag");//0硬证书 1软证书
		User user = userService.get(Integer.parseInt(userid));
		if(null != certcn && !certcn.equals("")){
			if(flag.equals("1")){
				int isBind = userService.checkScertcn(certcn);
				if(isBind == 1){
					json.put("success", false);
					json.put("msg", "该证书已经绑定到其他用户！");
				}else {
					user.setScertcn(certcn);
					userService.save(user);	 
					json.put("success", true);
					json.put("msg", "绑定成功！");
				}
			}else if(flag.equals("0")){
				int isBind = userService.checkHcertcn(certcn);
				if(isBind == 1){
					json.put("success", false);
					json.put("msg", "该证书已经绑定到其他用户！");
				}else{
					user.setHcertcn(certcn);
					this.userService.save(user);			 
					json.put("success", true);
					json.put("msg", "绑定成功！");
				}
			}else{
				int isBind=userService.checkCardno(certcn);
				if(isBind == 1){
					json.put("success", false);
					json.put("msg", "该卡号已经绑定到其他用户！");
				}else{
					user.setCardno(certcn);
					this.userService.save(user);			 
					json.put("success", true);
					json.put("msg", "绑定成功！");
				}
			}
		}else {
			json.put("success", false);
			json.put("msg", "证书数据为空！");
		}
		return json;
	}
	@RequestMapping(value="/unbindCert.action")
	@ResponseBody
	public JSONObject unbindCert(HttpServletRequest request) {
		JSONObject json = new JSONObject();
		String id = request.getParameter("ids");
		String flag = request.getParameter("flag");
		if (id != null && !id.trim().equals("")) {
			User user = userService.get(Integer.parseInt(id));
			if(flag.equals("1")){
				user.setScertcn("");
				user.setImei("");
				user.setDeviceid("");
			}else if(flag.equals("0")){
				   user.setHcertcn("");
			   }else{
				  user.setCardno("");
			   }
			userService.save(user);
			json.put("success", true);
			json.put("msg", "解绑成功！");
		} else {
			json.put("success", false);
			json.put("msg", "解绑成功！");
		}
		return json;
	}*/

	/*@RequestMapping(value="/auth.action")
	public String auth() {
		return "account/authList";
	}

	@RequestMapping("/listJsonForListExistDept.action")
	@ResponseBody
	public List<User> listJsonForListExistDept(Model model,HttpServletRequest request,HttpServletResponse response) {
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String sql = "";
		Parameter parameter = new Parameter();
		if(RoleType.UNIT_USERGROUP.getName().contains(auth.getRole().getEnname())){
			parameter.put("p1", auth.getUser().getUnit().getDivid());
			return userService.findBySql("select * from platform_t_user a where exists (select * from mlog_dept b where a.deptid=b.id and a.divid=b.divid) and a.divid=:p1 order by deptid asc", parameter, User.class);
		}else {
			return userService.findBySql("select * from platform_t_user a where exists (select * from mlog_dept b where a.deptid=b.id and a.divid=b.divid) order by deptid asc", null, User.class);
		}
	}

	@RequestMapping("/getDataAuthTreeids.action")
	@ResponseBody
	public List<String> getDataAuthTreeids(Model model,HttpServletRequest request,HttpServletResponse response) {
		int type = Integer.parseInt(request.getParameter("type"));
		int userid = Integer.parseInt(request.getParameter("userid"));
		return dataRulesService.getDataAuthTreeids(type, userid);
	}

	@RequestMapping("/getNewUserDataAuthTreeids.action")
	@ResponseBody
	public List<String> getNewUserDataAuthTreeids(Model model,HttpServletRequest request,HttpServletResponse response) {
		int type = Integer.parseInt(request.getParameter("type"));
		int userid = Integer.parseInt(request.getParameter("userid"));
		return dataRulesService.getNewUserDataAuthTreeids(type, userid);
	}
*/
	//保存门禁权限
	@RequestMapping("/saveDoorAuth.action")
	@ResponseBody
	public Map saveDataAuth(Model model,HttpServletRequest request,HttpServletResponse response) {
		
		String userids = request.getParameter("userids");
		String doorid=request.getParameter("doorid");
		dataRulesService.saveDoorAuth(userids,Integer.parseInt(doorid));
		return suc();
	}
	//删除门禁权限
	@RequestMapping("/deleteDoorAuth.action")
	@ResponseBody
	public Map deleteDoorAuth(Model model,HttpServletRequest request,HttpServletResponse response) {
		
		String userids = request.getParameter("userids");
		String doorid=request.getParameter("doorid");
		dataRulesService.deleteDoorAuth(userids,Integer.parseInt(doorid));
        return suc();
	}
	
	
	/*@RequestMapping(value="/send.action")
	public String send(HttpServletRequest request,Model model) {
		model.addAttribute("content",MlogPM.get("sms.content"));
		return "account/sendSms";
	}
	@RequestMapping("/sendSms.action")
	@ResponseBody
	public Map sendSms(Model model,HttpServletRequest request,HttpServletResponse response) {
		String ids = request.getParameter("ids");
		String content = request.getParameter("content");
		String isUseTemplate = request.getParameter("isusetemplate");
		if(isUseTemplate.equals("1")){
			content = MlogPM.get("sms.content");
		}
		List<User> list = userService.findBySql("select * from platform_t_user a where a.isdelete=0 and a.isenable=1 and (a.divid is not null or trim(a.divid)<>'') and id in ("+ids+") ", null, User.class);
		SmsProvider smsProvider = SmsController.getSmsProvider();
		for(User user : list){
			ShortMessage msg = new ShortMessage();
			msg.setSenderName("河北腾翔");
			if(isUseTemplate.equals("1")){
				msg.setContent(content.replace("账户名：xxx", "账户名："+user.getUsername()).replace("密码：******", "密码：123456").replace("单位ID：XXX", "单位ID："+user.getUnit().getDivid()));
			}else {
				msg.setContent(content);
			}
			msg.setReceiverNumber(user.getMobilephone());
			try {
				smsProvider.sendMessageAsync(msg);
			}
			catch(Exception ex){
				System.out.println(user.getMobilephone()+"：发送失败");
				log.info(user.getMobilephone()+"：发送失败");
			};	
		}
		return suc();
	}
	@RequestMapping("/sendSmsAll.action")
	@ResponseBody
	public Map sendSmsAll(Model model,HttpServletRequest request,HttpServletResponse response) {
		String content = request.getParameter("content");
		String isUseTemplate = request.getParameter("isusetemplate");
		String divid = request.getParameter("divid");
		if(isUseTemplate.equals("1")){
			content = MlogPM.get("sms.content");
		}
		content +="【河北腾翔】";
		String sql = " select * from platform_t_user a where (a.divid is not null or trim(a.divid)<>'') and a.isdelete=0 and a.isenable=1 ";
		Parameter parameter = new Parameter();
		if(!StringUtils.isBlank(divid)){
			sql += " and divid=:p1";
			parameter.put("p1", divid);
		}
		List<User> list = userService.findBySql(sql, parameter, User.class);
		SmsProvider smsProvider = SmsController.getSmsProvider();
		for(User user : list){
			ShortMessage msg = new ShortMessage();
			msg.setSenderName("河北腾翔");
			if(isUseTemplate.equals("1")){
				msg.setContent(content.replace("账户名：xxx", "账户名："+user.getUsername()).replace("密码：******", "密码：123456").replace("单位ID：XXX", "单位ID："+user.getUnit().getDivid()));
			}else {
				msg.setContent(content);
			}
			msg.setReceiverNumber(user.getMobilephone());
			try {
				smsProvider.sendMessageAsync(msg);
			} catch (SmsException e) {
				System.out.println(user.getMobilephone()+"：发送失败");
				log.info(user.getMobilephone()+"：发送失败");
				e.printStackTrace();
			}
			//			if(!smsProvider.sendMessage(msg)){
			//				System.out.println(user.getMobilephone()+"：发送失败");
			//				log.info(user.getMobilephone()+"：发送失败");
			//			};	
		}
		return suc();
	}

	@RequestMapping(value="/modifyPasswd.action")
	public String modifyPasswd(HttpServletRequest request,Model model) {
		model.addAttribute("o",userService.get(Integer.parseInt(request.getParameter("id"))));
		return "account/modifyPasswd";
	}

	@RequestMapping(value="/modifyPasswdDo.action")
	@ResponseBody
	public Map modifyPasswdDo(HttpServletRequest request,Model model) {
		User user = userService.get(Integer.parseInt(request.getParameter("id")));
		String pwd = request.getParameter("pwd");
		String rpwd = request.getParameter("rpwd");
		if(pwd.equals(rpwd)){
			user.setPassword(Md5Encrypt.md5(rpwd));
		}
		userService.save(user);
		return suc();
	}

	@RequestMapping(value="/batchSaveDo.action")
	@ResponseBody
	public Map batchSaveDo(UnitSettings o,HttpServletRequest request) {
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String params = request.getParameter("params");
		JSONArray arr = JSON.parseArray(params);
		for(Object obj :arr){
			String username = ((JSONObject)obj).getString("username");
			String name = ((JSONObject)obj).getString("name");
			String identitycard = ((JSONObject)obj).getString("identitycard");
			String mobilephone = ((JSONObject)obj).getString("mobilephone");
			String deptid = ((JSONObject)obj).getString("deptid");
			User user = new User();
			user.setUsername(username);
			user.setName(name);
			user.setIdentitycard(identitycard);
			user.setMobilephone(mobilephone);
			user.setUnit(auth.getUser().getUnit());
			user.setDept(deptService.get(Integer.parseInt(deptid)));
			user.setRole(roleService.findUniqueBy("enname", RoleType.NORMAL.getName()));
			user.setClientrole(0);
			user.setIsdelete(0);
			user.setIsenable(1);
			user.setPassword(Md5Encrypt.md5("123456"));
			userService.save(user);
		}
		return suc();
	}*/
	//门禁管理列表
	@RequestMapping("/doorlist.action")
	public String doorlist(HttpServletRequest request,Model model) {
		String doorflag=request.getParameter("doorflag");
		System.out.println("testflag="+doorflag); 
		model.addAttribute("doorid", doorflag);
		return "account/door";
	}
	/**
	 * 列表返回json
	 */
	@RequestMapping("/doorlistJson.action")
	@ResponseBody
	public Map doorlistJson(Model model, HttpServletRequest request) {

		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String hql = " from Accessrecord where 1=1 ";

		String id = request.getParameter("id");
		//String name=request.getParameter("name");
		String startDate=request.getParameter("startDate");
		String endDate=request.getParameter("endDate");
		if(!StringUtils.isBlank(id)){
			hql +=" and  accesscontrolid="+id;
		}
		if(!StringUtils.isBlank(startDate) ){
			hql +=" and   str_to_date(recordtime,'%Y-%m-%d')>=str_to_date('"+startDate+"','%Y-%m-%d')";
		}
		if(!StringUtils.isBlank(startDate) && !StringUtils.isBlank(endDate)){
			hql +=" and   str_to_date(recordtime,'%Y-%m-%d')<=str_to_date('"+endDate+"','%Y-%m-%d')";
		}
		
		Map<String, Object> m = new HashMap<String, Object>();
		Page<Door> p = new Page<Door>(Integer.parseInt(request.getParameter("rows")==null?"10":request.getParameter("rows")));
		p.setPageNo(Integer.parseInt(request.getParameter("page")==null?"1":request.getParameter("page")));
		p.setOrder("recordtime:desc");
		Page<Door> resultPage = doorService.find(p, hql, null);
		m.put("rows", resultPage.getResult()==null ? "":resultPage.getResult());
		m.put("total", resultPage.getTotalCount());
		return m;
	}
	
	//门禁策略
	@RequestMapping("/addcelv.action")
	public String addcelv(HttpServletRequest request,Model model) {
		String id = request.getParameter("id");
		Door door = doorService.get(Integer.parseInt(id));
		model.addAttribute("door", door);
		return "account/doorCelv";
	}
	/**
	 * 列表返回json
	 */
	@RequestMapping("/saveCelv.action")
	@ResponseBody
	public Map saveCelv(Door r, HttpServletRequest request) {
	
			Door door = doorService.get(Integer.parseInt(request.getParameter("id")));
			String regularflag=request.getParameter("closeflag");
			String starttime=request.getParameter("starttime");
			String endtime=request.getParameter("endtime");
			String cc=request.getParameter("cc");
			if(!StringUtils.isBlank(regularflag) && "on".equals(regularflag)){
				door.setRegularflag(1);
				door.setRegularstarttime(starttime);;
				door.setRegularendtime(endtime);
				door.setRegularid(cc);
			}else{
				door.setRegularflag(0);
				door.setRegularstarttime("");;
				door.setRegularendtime("");
				door.setRegularid("");
			}
			
			
			doorService.save(door);
			
			
			return suc();
		}
	
	
	
	//紧急锁定
	/*@RequestMapping(value="/lock.action")
	@ResponseBody
	public JSONObject lock(String ids) {
		JSONObject json = new JSONObject();
		
		//json.put("islock", 0);
		// 调SOCKET发送锁定命令，如果成功iswarn为1
		
		json.put("islock", 1);

		return json;
	}*/
}
