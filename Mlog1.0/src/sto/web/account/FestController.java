
package sto.web.account;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.PreparedStatement;

import other.AuthProfile;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.common.util.RoleType;
import sto.common.web.BaseController;
import sto.model.account.Fest;
import sto.model.account.Unit;
import sto.service.account.DataRulesService;
import sto.service.account.DeptService;
import sto.service.account.FestService;
import sto.service.account.RoleService;
import sto.service.account.UnitService;
import sto.service.account.UserService;

/**
 *节日管理
 * 功能：列表、分配角色
 * 
 */
@Controller
@RequestMapping(value = "/fest")
@SuppressWarnings("unchecked")
public class FestController extends BaseController{
	private static Logger log = Logger.getLogger(FestController.class);
	@Resource  
    protected UserService userService;
	@Resource
	protected FestService festService;
	@Resource
	DeptService deptService;
	@Resource
	RoleService roleService;
	@Resource
	private DataRulesService dataRulesService;
	@Resource
	UnitService unitService;
	/**
	 * 列表页面
	 */
	@RequestMapping("/list.action")
	public String list() {
		return "account/festList";
	}
	/**
	 * 列表返回json
	 */
	@RequestMapping("/listJson.action")
	@ResponseBody
	public Map listJson(Model model, HttpServletRequest request) {

		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String hql = " from Fest where 1=1 ";
		
		String id = request.getParameter("id");
		String divid = request.getParameter("divid");
		String date1=request.getParameter("startDate");
		String date2=request.getParameter("endDate");
		
		
		if(RoleType.UNIT_USERGROUP.getName().contains(auth.getRole().getEnname())){
			 divid = auth.getUser().getUnit().getDivid();
		}
		System.out.print(divid);

		Parameter parameter = new Parameter();
		if(!StringUtils.isBlank(divid)){
			hql +=" and divid=:p1 ";
			parameter.put("p1", divid);
		}
		if(!StringUtils.isBlank(date1) && !StringUtils.isBlank(date2)){
			hql +=" and (date>=str_to_date(:p2,'%Y-%m-%d') and date<=str_to_date(:p3,'%Y-%m-%d')) ";
			parameter.put("p2", date1);
			parameter.put("p3", date2);
		}
		

		
		Map<String, Object> m = new HashMap<String, Object>();
		Page<Fest> p = new Page<Fest>(Integer.parseInt(request.getParameter("rows")==null?"10":request.getParameter("rows")));
		p.setPageNo(Integer.parseInt(request.getParameter("page")==null?"1":request.getParameter("page")));
		p.setOrder("date:desc");
		Page<Fest> resultPage =festService.find(p, hql, parameter);
		m.put("rows", resultPage.getResult()==null ? "":resultPage.getResult());
		m.put("total", resultPage.getTotalCount());
		return m;
	}
	
	/**
	 * 新增
	 */
	@RequestMapping(value="/save.action")
	public String save() {
		return "account/festSave";
	}
	/**
	 * 新增保存
	 */
	@RequestMapping(value="/saveDo.action",method=RequestMethod.POST)
	@ResponseBody
	public Map saveDo(HttpServletRequest request) {
		//String[] roleids = request.getParameterValues("roleid");
	//	String id=request.getParameter("festid");
	//	System.out.println(id);
		String date = request.getParameter("date");
		String divid=request.getParameter("divid1");
		
		
		String remark=request.getParameter("remark");
		//Role role = roleService.get(Integer.parseInt(roleids[0]));
		//校验不可重复用户是否重复
//		if(RoleType.NOREPEAT_USERGROUP.getName().contains(role.getEnname())){
//			String sql = " select count(1) from platform_t_user a,platform_t_role b where a.roleid=b.id "
//				+" and b.enname in ('"+RoleType.NOREPEAT_USERGROUP.getName().replace(",", "','")+"') "
//				+" and a.username=:p1 ";
//			Parameter param = new Parameter();
//			param.put("p1", r.getUsername());
//			int count = Integer.parseInt(String.valueOf(userService.findBySql(sql, param, null).get(0)));
//			if(count > 0){
//				return err("与系统用户或其他单位管理员重复");
//			}
//		}
//		//校验同一单位内用户不可重复
//		if(!StringUtils.isBlank(divid)){
//			Parameter param = new Parameter();
//			String sql = " select count(1) from platform_t_user a where a.divid=:p1 and a.username=:p2 ";
//			param.put("p1", divid);
//			param.put("p2", r.getUsername());
//			int count = Integer.parseInt(String.valueOf(userService.findBySql(sql, param, null).get(0)));
//			if(count > 0){
//				return err("单位已存在此用户");
//			}
//		}
		
		Parameter param = new Parameter();
//		String sql = " select count(1) from mlog_fest a where  id=:p1 ";
//		param.put("p1",id);
		boolean count = festService.checkDateIsExist(divid, date);
		if(count){
			return err("日期已存在");
		}
        Fest fest = new Fest();
	//	BeanUtils.copyProperties(r,fest);
		
//		fest.setId(Integer.parseInt(id));
	   // fest.setDivname(divname);
		fest.setRemark(remark);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
		java.util.Date card_indate = null;
		try {
			card_indate = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        fest.setDate(new java.sql.Date(card_indate.getTime()));
//		user.setClientrole(0);
//		user.setIsdelete(0);
//		user.setIsenable(1);
//		user.setPassword(Md5Encrypt.md5("123456"));
		if(!StringUtils.isBlank(divid)){
			Unit unit = unitService.findUniqueBy("divid", divid);
			fest.setUnit(unit);
//			Parameter parameter = new Parameter();
//			parameter.put("p1", divid);
//			parameter.put("p2", Integer.parseInt(deptid));
//			user.setDept(deptService.find(" from Dept where divid=:p1 and id=:p2", parameter).get(0));
		}else {
			fest.setUnit(null);
//			user.setDept(null);
		}
		festService.save(fest);
		return suc();
	}
//	
	/**
	 * 修改页面
	 */
	@RequestMapping(value="/update.action")
	public String update(HttpServletRequest request,Model model) {
		model.addAttribute("o",festService.get(Integer.parseInt(request.getParameter("id"))));
		return "account/festUpdate";
	}
//	
	/**
//	 * 修改保存
//	 */
	@RequestMapping(value="/updateDo.action")
	@ResponseBody
	public Map updateDo(Fest o,HttpServletRequest request) {
		//String[] roleids = request.getParameterValues("roleid");
		//String deptid = request.getParameter("deptid1");
		//String divid = request.getParameter("divid1");
		Fest fest = festService.get(Integer.parseInt(request.getParameter("id")));
//		Role role = roleService.get(Integer.parseInt(roleids[0]));
//		//校验不可重复用户是否重复
//		if(RoleType.NOREPEAT_USERGROUP.getName().contains(role.getEnname())){
//			String sql = " select count(1) from platform_t_user a,platform_t_role b where a.roleid=b.id "
//				+" and b.enname in ('"+RoleType.NOREPEAT_USERGROUP.getName().replace(",", "','")+"') "
//				+" and a.username=:p1 and b.enname<>:p2";
//			Parameter param = new Parameter();
//			param.put("p1", o.getUsername());
//			param.put("p2", user.getRole().getEnname());
//			int count = Integer.parseInt(String.valueOf(userService.findBySql(sql, param, null).get(0)));
//			if(count > 0){
//				return err("与系统用户或其他单位管理员重复");
//			}
//		}
//		//校验同一单位内用户不可重复
//		if(!StringUtils.isBlank(divid)){
//			if(user.getUnit() != null){
//				if(!divid.equals(user.getUnit().getDivid())){
//					Parameter param = new Parameter();
//					String sql = " select count(1) from platform_t_user a where a.divid=:p1 and a.username=:p2 ";
//					param.put("p1", divid);
//					param.put("p2", o.getUsername());
//					int count = Integer.parseInt(String.valueOf(userService.findBySql(sql, param, null).get(0)));
//					if(count > 0){
//						return err("单位已存在此用户");
//					}
//				}
//			}
//		}
//		
//		if(!StringUtils.isBlank(divid)){
//			Unit unit = unitService.findUniqueBy("divid", divid);
//			user.setUnit(unit);
//			user.setDept(deptService.get(Integer.parseInt(deptid)));
//		}else {
//			user.setUnit(null);
//			user.setDept(null);
//		}
//		user.setRole(role);
//		//user.setClientrole(0);
//		user.setIsdelete(0);
//		user.setIdentitycard(o.getIdentitycard());
//		user.setMobilephone(o.getMobilephone());
//		user.setTelephone(o.getTelephone());
//		user.setExtension(o.getExtension());
//		user.setIsenable(o.getIsenable());
		fest.setDate(o.getDate());
	//	fest.setDivid(o.getDivid());
		fest.setDivname(o.getDivname());
		fest.setRemark(o.getRemark());
		festService.save(fest);
		return suc();
	}
//	
	/**
	 * 删除
	 */
	@RequestMapping(value="/delete.action")
	@ResponseBody
	public Map<String, Object> delete(String ids) {
		if(StringUtils.isNotBlank(ids)) {
			ids = ids.replaceAll(",", "','");
			
			//festService.update("update Fest set isdelete=1,isenable=0  where id in('"+ids+"')", null);
		}
		festService.delete("delete from Fest where id in('"+ids+"')", null);
		return suc();
	}

}
