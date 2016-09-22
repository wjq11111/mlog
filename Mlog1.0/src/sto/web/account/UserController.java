
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

import other.AuthProfile;
import sto.common.Md5Encrypt;
import sto.common.MlogPM;
import sto.common.sms.SmsController;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.common.util.RoleType;
import sto.common.web.BaseController;
import sto.model.account.Role;
import sto.model.account.Unit;
import sto.model.account.UnitSettings;
import sto.model.account.User;
import sto.service.account.DataRulesService;
import sto.service.account.DeptService;
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
 * 用户管理
 * 功能：列表、分配角色
 * 
 */
@Controller
@RequestMapping(value = "/user")
@SuppressWarnings("unchecked")
public class UserController extends BaseController{
	private static Logger log = Logger.getLogger(UserController.class);
	
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
	
	/*private MifareClassic mifareClassic;
	private AcrDll acrdll;
	private PcscReader pcscReader;*/
	
	
		/**
	 * 列表页面
	 */
	@RequestMapping("/list.action")
	public String list() {
		return "account/userList";
	}
	/**
	 * 列表返回json
	 */
	@RequestMapping("/listJson.action")
	@ResponseBody
	public Map listJson(Model model, HttpServletRequest request) {

		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String hql = " from User where 1=1 ";

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

		Map<String, Object> m = new HashMap<String, Object>();
		Page<User> p = new Page<User>(Integer.parseInt(request.getParameter("rows")==null?"10":request.getParameter("rows")));
		p.setPageNo(Integer.parseInt(request.getParameter("page")==null?"1":request.getParameter("page")));
		p.setOrder("id:asc");
		Page<User> resultPage = userService.find(p, hql, parameter);
		m.put("rows", resultPage.getResult()==null ? "":resultPage.getResult());
		m.put("total", resultPage.getTotalCount());
		return m;
	}
	
	/**
	 * 列表返回json,用于下拉列表显示
	 */
	@RequestMapping("/checklistJson.action")
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
	}
	

	/**
	 * 新增
	 */
	@RequestMapping(value="/save.action")
	public String save() {
		return "account/userSave";
	}
	/**
	 * 新增保存
	 */
	@RequestMapping(value="/saveDo.action",method=RequestMethod.POST)
	@ResponseBody
	public Map saveDo(User r,HttpServletRequest request) {
		String[] roleids = request.getParameterValues("roleid");
		String deptid = request.getParameter("deptid1");
		String divid = request.getParameter("divid1");
		Role role = roleService.get(Integer.parseInt(roleids[0]));
		//校验不可重复用户是否重复
		if(RoleType.NOREPEAT_USERGROUP.getName().contains(role.getEnname())){
			String sql = " select count(1) from platform_t_user a,platform_t_role b where a.roleid=b.id "
					+" and b.enname in ('"+RoleType.NOREPEAT_USERGROUP.getName().replace(",", "','")+"') "
					+" and a.username=:p1 ";
			Parameter param = new Parameter();
			param.put("p1", r.getUsername());
			int count = Integer.parseInt(String.valueOf(userService.findBySql(sql, param, null).get(0)));
			if(count > 0){
				return err("与系统用户或其他单位管理员重复");
			}
		}
		//校验同一单位内用户不可重复
		if(!StringUtils.isBlank(divid)){
			Parameter param = new Parameter();
			String sql = " select count(1) from platform_t_user a where a.divid=:p1 and a.username=:p2 ";
			param.put("p1", divid);
			param.put("p2", r.getUsername());
			int count = Integer.parseInt(String.valueOf(userService.findBySql(sql, param, null).get(0)));
			if(count > 0){
				return err("单位已存在此用户");
			}
		}

		User user = new User();
		BeanUtils.copyProperties(r,user);

		user.setRole(role);
		user.setClientrole(0);
		user.setIsdelete(0);
		user.setIsenable(1);
		user.setPassword(Md5Encrypt.md5("123456"));
		if(!StringUtils.isBlank(divid)){
			Unit unit = unitService.findUniqueBy("divid", divid);
			user.setUnit(unit);
			Parameter parameter = new Parameter();
			parameter.put("p1", divid);
			parameter.put("p2", Integer.parseInt(deptid));
			user.setDept(deptService.find(" from Dept where divid=:p1 and id=:p2", parameter).get(0));
		}else {
			user.setUnit(null);
			user.setDept(null);
		}
		userService.save(user, roleids);
		//给新用户分配数据权限
		dataRulesService.saveDataAuth(user.getId(), "1,2,3",user.getId().toString());
		//修改通讯录更新时间
		unitSettingsService.lastupdatedSettings(divid);
		return suc();
	}

	/**
	 * 修改页面
	 */
	@RequestMapping(value="/update.action")
	public String update(HttpServletRequest request,Model model) {
		model.addAttribute("o",userService.get(Integer.parseInt(request.getParameter("id"))));
		return "account/userUpdate";
	}

	/**
	 * 修改保存
	 */
	@RequestMapping(value="/updateDo.action")
	@ResponseBody
	public Map updateDo(User o,HttpServletRequest request) {
		String[] roleids = request.getParameterValues("roleid");
		String deptid = request.getParameter("deptid1");
		String divid = request.getParameter("divid1");
		User user = userService.get(Integer.parseInt(request.getParameter("id")));
		Role role = roleService.get(Integer.parseInt(roleids[0]));
		//校验不可重复用户是否重复
		if(RoleType.NOREPEAT_USERGROUP.getName().contains(role.getEnname())){
			String sql = " select count(1) from platform_t_user a,platform_t_role b where a.roleid=b.id "
					+" and b.enname in ('"+RoleType.NOREPEAT_USERGROUP.getName().replace(",", "','")+"') "
					+" and a.username=:p1 and b.enname<>:p2";
			Parameter param = new Parameter();
			param.put("p1", o.getUsername());
			param.put("p2", user.getRole().getEnname());
			int count = Integer.parseInt(String.valueOf(userService.findBySql(sql, param, null).get(0)));
			if(count > 0){
				return err("与系统用户或其他单位管理员重复");
			}
		}
		//校验同一单位内用户不可重复
		if(!StringUtils.isBlank(divid)){
			if(user.getUnit() != null){
				if(!divid.equals(user.getUnit().getDivid())){
					Parameter param = new Parameter();
					String sql = " select count(1) from platform_t_user a where a.divid=:p1 and a.username=:p2 ";
					param.put("p1", divid);
					param.put("p2", o.getUsername());
					int count = Integer.parseInt(String.valueOf(userService.findBySql(sql, param, null).get(0)));
					if(count > 0){
						return err("单位已存在此用户");
					}
				}
			}
		}

		if(!StringUtils.isBlank(divid)){
			Unit unit = unitService.findUniqueBy("divid", divid);
			user.setUnit(unit);
			user.setDept(deptService.get(Integer.parseInt(deptid)));
		}else {
			user.setUnit(null);
			user.setDept(null);
		}
		user.setRole(role);
		//user.setClientrole(0);
		user.setIsdelete(0);
		user.setIdentitycard(o.getIdentitycard());
		user.setMobilephone(o.getMobilephone());
		user.setTelephone(o.getTelephone());
		user.setExtension(o.getExtension());
		user.setIsenable(o.getIsenable());
		user.setMobileopenflag(o.getMobileopenflag());
		if(o.getMobileopenflag()==1){
			user.setOpenstarttime(o.getOpenstarttime());
			user.setOpenendtime(o.getOpenendtime());
		}else{
			user.setOpenstarttime("");
			user.setOpenendtime("");
		}
		
		userService.save(user, roleids);
		//修改通讯录更新时间
		unitSettingsService.lastupdatedSettings(divid);
		return suc();
	}

	/**
	 * 删除
	 */
	@RequestMapping(value="/delete.action")
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
	}

	@RequestMapping(value="/unfreeze.action")
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
			}else{
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
			}else{
				user.setHcertcn("");
			}
			userService.save(user);
			json.put("success", true);
			json.put("msg", "解绑成功！");
		} else {
			json.put("success", false);
			json.put("msg", "解绑成功！");
		}
		return json;
	}

	@RequestMapping(value="/auth.action")
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

	@RequestMapping("/saveDataAuth.action")
	@ResponseBody
	public Map saveDataAuth(Model model,HttpServletRequest request,HttpServletResponse response) {
		int managerid = Integer.parseInt(request.getParameter("managerid"));
		String types = request.getParameter("types");
		String userids = request.getParameter("userids");
		dataRulesService.saveDataAuth(managerid, types,userids);
		//修改通讯录更新时间
		String sql="select * from platform_t_user where id='"+managerid+"'";
	    List<User> user = userService.findBySql(sql, null, User.class);
		unitSettingsService.lastupdatedSettings(user.get(0).getUnit().getDivid());
		
		return suc();
	}
	
	@RequestMapping("/saveNewUserDataAuth.action")
	@ResponseBody
	public Map saveNewUserDataAuth(Model model,HttpServletRequest request,HttpServletResponse response) {
		int userid = Integer.parseInt(request.getParameter("userid"));
		String types = request.getParameter("types");
		String managerids = request.getParameter("managerids");
		dataRulesService.saveNewUserDataAuth(userid,types,managerids);
		//修改通讯录更新时间
		String sql="select * from platform_t_user where id='"+userid+"'";
	    List<User> user = userService.findBySql(sql, null, User.class);
		unitSettingsService.lastupdatedSettings(user.get(0).getUnit().getDivid());
		
		return suc();
	}

	@RequestMapping(value="/resetPassword.action",method=RequestMethod.POST)
	@ResponseBody
	public Map resetPassword(User r,HttpServletRequest request) {
		Parameter parameter = new Parameter();
		parameter.put("p1", Md5Encrypt.md5("123456"));
		parameter.put("p2", Integer.parseInt(request.getParameter("id")));
		int result = userService.updateBySql("update platform_t_user set password=:p1 where id=:p2", parameter);
		if(result == 1){
			return suc();
		}else{
			return err("重置失败！");
		}
	}
	@RequestMapping(value="/send.action")
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
	}
	
	/**
	 * 制作门禁卡页面
	 */
	@RequestMapping(value="/cardBind.action")
	public String cardBind(HttpServletRequest request,Model model) {
		User user=userService.get(Integer.parseInt(request.getParameter("id")));
		model.addAttribute("o",user);
		model.addAttribute("cardno",user.getCardno());
		model.addAttribute("oldrandomcode",user.getOldrandomcode());
		model.addAttribute("newrandomcode",user.getNewrandomcode());
		//通过读卡器读取
		/*pcscReader = new PcscReader();
		pcscReader.setEventHandler(new ReaderEvents());

		// Register the event handler implementation of this class
		//pcscReader.getEventHandler().addEventListener(this);
		acrdll = new AcrDll(pcscReader);
		String[] readerList = null;
		String uid=null;
		String blockdata=null;
		try {
			readerList = acrdll.getPcscConnection().listTerminals();
			if (readerList.length == 0) {
				request.setAttribute("message","No PC/SC reader detected");
	    	}
		} catch (Exception ex) {
			ex.getMessage().toString();
		}
		
		try {
			if (acrdll.getPcscConnection().isConnectionActive())
				acrdll.getPcscConnection().disconnect();

			String rdrcon = readerList[0];

			acrdll.getPcscConnection().connect(rdrcon, "*");
			mifareClassic = new MifareClassic(
					acrdll.getPcscConnection());

		} catch (Exception ex) {
			ex.getMessage().toString();
		}
		
		try {

          uid=mifareClassic.apdu(codeToByte("FF CA 00 00 00"));//SHM 得到UID命令结果
          String renzheng=loadAuthKey("FF 82 00 00 06 FF FF FF FF FF FF","FF 86 00 00 05 01 00 02 60 00");
          if(!renzheng.equals("认证成功")){
        	  request.setAttribute("message",renzheng);
          }
          blockdata=mifareClassic.apdu(codeToByte("FF B0 00 02 10"));
          
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    
		
		model.addAttribute("uid",uid);//读卡器的UID读取至前台页面
		model.addAttribute("data",blockdata);//读卡器块中的随机数
	    String cardFlag="0";
	    if(!(uid.replace(" ", "")).equals(user.getCardno())){
	    	cardFlag="1";
	    }
	    model.addAttribute("tt",cardFlag);//判断标记，1：代表读卡器内容和数据库不一致，0：代表一致
*/		return "account/cardBind";
	}

	/**
	 * 门禁卡保存
	 */
	@RequestMapping(value="/cardBindDo.action")
	@ResponseBody
	public JSONObject  cardBindDo(User o,HttpServletRequest request) {
		JSONObject json = new JSONObject();
		String cardno = request.getParameter("cardno");
		String oldrandomcode = request.getParameter("oldrandomcode");
		String newrandomcode = request.getParameter("newrandomcode");
		User user = userService.get(Integer.parseInt(request.getParameter("id")));
		
		
		
		if(null != cardno && !cardno.equals("")){
			
				int isBind = userService.checkCardno(cardno.replace(" ", ""));
				if(isBind == 1){
					json.put("success", false);
					json.put("msg", "该卡号已经绑定到其他用户！");
				}else {
					/*try {
				          String renzheng=loadAuthKey("FF 82 00 00 06 FF FF FF FF FF FF","FF 86 00 00 05 01 00 02 60 00");
				          if(!renzheng.equals("认证成功")){
				        	  request.setAttribute("message",renzheng);
				          }
				         String write=mifareClassic.apdu(codeToByte("FF D6 00 01 10"+newrandomcode));//将10换成04，意思为将16位改成4位
				         if(!write.equals("9000")){
				        	 
				         }
				          
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}*/
					user.setCardno(cardno.replace(" ", ""));
					user.setOldrandomcode(newrandomcode.replace(" ", ""));
					user.setNewrandomcode(newrandomcode.replace(" ", ""));
					userService.save(user);	 
					json.put("success", true);
					json.put("msg", "绑定成功！");
				}
			
		}else {
			json.put("success", false);
			json.put("msg", "卡号数据为空！");
		}
		
	
		return json;
	}
 /*     public String loadAuthKey(String load,String auth){
    		
    		 String result1 = null;
			try {
				result1 = mifareClassic.apdu(codeToByte(load));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
             if(!result1.equalsIgnoreCase("9000")){
           	  return "加载密钥失败";
             }
             String result2 = null;
			try {
				result2 = mifareClassic.apdu(codeToByte(auth));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
             if(!result2.equalsIgnoreCase("9000")){
            	 return "认证密钥失败";
             }
    	  return "认证成功";
   }*/
      public byte[] codeToByte(String code){
  	    String read=code.trim().replaceAll(" ", "");//认证密钥指令
		byte[] buff = new byte[read.length() / 2];
		int b = 0;
		for (int i = 0; i < read.length(); i = i + 2) {
			buff[b] = (byte) ((Integer) Integer.parseInt(
					read.substring(i, i + 2), 16)).byteValue();
			b++;
		}
		return buff;
      }
      /**
  	 * 开启手机开门功能
  	 */
  	@RequestMapping(value="/openDoorDo.action")
  	@ResponseBody
  	public Map openDoorDo(String ids,String starttime,String endtime) {
  		
  		if(StringUtils.isNotBlank(ids)) {
  			ids = ids.replaceAll(",", "','");
  			userService.update("update User set mobileopenflag=1  where id in('"+ids+"')", null);
  		}
  		
  		
  		return suc();
  	}  
    /**
 	 * 禁用手机开门功能
 	 */
 	@RequestMapping(value="/closeDoorDo.action")
 	@ResponseBody
 	public Map closeDoorDo(String ids) {
 		
 		if(StringUtils.isNotBlank(ids)) {
 			ids = ids.replaceAll(",", "','");
 			userService.update("update User set mobileopenflag=0,openstarttime='',openendtime=''  where id in('"+ids+"')", null);
 		}
 		
 		
 		return suc();
 	} 
 	//手机开门策略
 		@RequestMapping("/mobiledoorcelv.action")
 		public String mobiledoorcelv(HttpServletRequest request,Model model) {
 			String ids = request.getParameter("ids");
 			String tmp[]=null;
 			if(StringUtils.isNotBlank(ids)) {
		 		ids = ids.replaceAll(",", "','");
		 		tmp=ids.split(",");
		 	}
 			if(tmp.length==1)
 			{
 				User user = userService.get(Integer.parseInt(tmp[0]));
 				model.addAttribute("user", user);
 			} 			
 			request.setAttribute("ids", ids);
 			return "account/opendoorSet";
 		}
 		/**
 		 * 列表返回json
 		 */
 		@RequestMapping("/saveCelv.action")
 		@ResponseBody
 		public Map saveCelv(Model model, HttpServletRequest request) {
 		
 				//Door door = doorService.get(Integer.parseInt(request.getParameter("id")));
 				String ids=request.getParameter("ids");
 				String mobileopenflag=request.getParameter("mobileopenflag");
 				String starttime=request.getParameter("starttime");
 				String endtime=request.getParameter("endtime");
 				if(StringUtils.isNotBlank(ids)) {
 		 			ids = ids.replaceAll(",", "','");
 		 			userService.update("update User set mobileopenflag="+mobileopenflag+",openstarttime='"+starttime+"',openendtime='"+endtime+"'  where id in('"+ids+"')", null);
 		 		}
 							
 								
 				
 				return suc();
 			}
}
