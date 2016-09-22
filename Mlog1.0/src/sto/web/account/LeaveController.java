
package sto.web.account;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.PreparedStatement;

import other.AuthProfile;
import sto.common.util.DateUtil;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.common.util.RoleType;
import sto.common.web.BaseController;
import sto.model.account.Fest;
import sto.model.account.Leave;
import sto.model.account.Unit;
import sto.model.account.User;
import sto.service.account.DataRulesService;
import sto.service.account.DeptService;
import sto.service.account.FestService;
import sto.service.account.LeaveService;
import sto.service.account.RoleService;
import sto.service.account.UnitService;
import sto.service.account.UserService;

/**
 *节日管理
 * 功能：列表、分配角色
 * 
 */
@Controller
@RequestMapping(value = "/leave")
@SuppressWarnings("unchecked")
public class LeaveController extends BaseController{
	private static Logger log = Logger.getLogger(FestController.class);
	@Resource  
    protected UserService userService;
	@Resource
	protected LeaveService leaveService;
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
		return "account/leaveList";
	}
	/**
	 * 列表返回json
	 */
	@RequestMapping("/listJson.action")
	@ResponseBody
	public Map listJson(Model model, HttpServletRequest request) {

		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String hql = " from Leave where 1=1 ";
		
		String id = request.getParameter("id");
		String divid = request.getParameter("divid");
		String deptid = request.getParameter("deptid");
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
		if(!StringUtils.isBlank(deptid)){
			hql +=" and deptid=:p2 ";
			parameter.put("p2", deptid);
		}
		if(!StringUtils.isBlank(date1) && !StringUtils.isBlank(date2)){
			hql +=" and (leavedate>=str_to_date(:p3,'%Y-%m-%d') and leavedate<=str_to_date(:p4,'%Y-%m-%d')) ";
			parameter.put("p3", date1);
			parameter.put("p4", date2);
		}
		

		
		Map<String, Object> m = new HashMap<String, Object>();
		Page<Fest> p = new Page<Fest>(Integer.parseInt(request.getParameter("rows")==null?"10":request.getParameter("rows")));
		p.setPageNo(Integer.parseInt(request.getParameter("page")==null?"1":request.getParameter("page")));
		p.setOrder("leavedate:desc");
		Page<Fest> resultPage =leaveService.find(p, hql, parameter);
		m.put("rows", resultPage.getResult()==null ? "":resultPage.getResult());
		m.put("total", resultPage.getTotalCount());
		return m;
	}
	
	/**
	 * 新增
	 */
	@RequestMapping(value="/save.action")
	public String save() {
		return "account/leaveSave";
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
		String date = request.getParameter("leavedate");
		String divid=request.getParameter("divid1");
		String deptid=request.getParameter("deptid1");
		String userid=request.getParameter("userid1");
		String leavetype=request.getParameter("leavetype");
		//Role role = roleService.get(Integer.parseInt(roleids[0]));
		//校验不可重复用户是否重复

		
     	boolean count = leaveService.checkDateIsExist(userid, date);
		if(count){
			return err("请假记录已存在");
		}
        Leave leave = new Leave();
		leave.setLeavetype(leavetype);
		User user = userService.findUniqueBy("id", Integer.parseInt(userid));
		leave.setUser(user);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); 
		java.util.Date card_indate = null;
		try {
			card_indate = sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        leave.setLeavedate(new java.sql.Date(card_indate.getTime()));

		if(!StringUtils.isBlank(divid)){
			Unit unit = unitService.findUniqueBy("divid", divid);
			leave.setUnit(unit);
			Parameter parameter = new Parameter();
			parameter.put("p1", divid);
			parameter.put("p2", Integer.parseInt(deptid));
			leave.setDept(deptService.find(" from Dept where divid=:p1 and id=:p2", parameter).get(0));
		}else {
			leave.setUnit(null);
			leave.setDept(null);
		}
		leaveService.save(leave);
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
			
			//festService.update("update Fest set isdelete=1,isenable=0  where id in('"+ids+"')", null);
		}
		leaveService.delete("delete from Leave where id in('"+ids+"')", null);
		return suc();
	}
    /**
    *  导出入EXCEL
    *
    *
    */
	@RequestMapping("/exportToExcel.action")
	public void exportToExcel(HttpServletRequest request,HttpServletResponse response){
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		//创建一个新的excel 
		HSSFWorkbook wb = new HSSFWorkbook(); 
		//创建sheet页 
		HSSFSheet sheet = wb.createSheet();
		//设置默认样式
		HSSFCellStyle defaultstyle=wb.createCellStyle();
		defaultstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);    //设置底线和颜色
		defaultstyle.setBottomBorderColor(HSSFColor.BLACK.index);
		defaultstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);   //设置左边线和颜色
		defaultstyle.setLeftBorderColor(HSSFColor.BLACK.index);
		defaultstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);   //设置右边线和颜色
		defaultstyle.setRightBorderColor(HSSFColor.BLACK.index);
		defaultstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);   //设置上面线和颜色
		defaultstyle.setTopBorderColor(HSSFColor.BLACK.index);
		//设置默认样式
		HSSFCellStyle numbericstyle=wb.createCellStyle();
		numbericstyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);    //设置底线和颜色
		numbericstyle.setBottomBorderColor(HSSFColor.BLACK.index);
		numbericstyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);   //设置左边线和颜色
		numbericstyle.setLeftBorderColor(HSSFColor.BLACK.index);
		numbericstyle.setBorderRight(HSSFCellStyle.BORDER_THIN);   //设置右边线和颜色
		numbericstyle.setRightBorderColor(HSSFColor.BLACK.index);
		numbericstyle.setBorderTop(HSSFCellStyle.BORDER_THIN);   //设置上面线和颜色
		numbericstyle.setTopBorderColor(HSSFColor.BLACK.index);
		numbericstyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		//设置标题样式
		HSSFCellStyle titlestyle=wb.createCellStyle();
		titlestyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);    //设置底线和颜色
		titlestyle.setBottomBorderColor(HSSFColor.BLACK.index);
		titlestyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);   //设置左边线和颜色
		titlestyle.setLeftBorderColor(HSSFColor.BLACK.index);
		titlestyle.setBorderRight(HSSFCellStyle.BORDER_THIN);   //设置右边线和颜色
		titlestyle.setRightBorderColor(HSSFColor.BLACK.index);
		titlestyle.setBorderTop(HSSFCellStyle.BORDER_THIN);   //设置上面线和颜色
		titlestyle.setTopBorderColor(HSSFColor.BLACK.index);
		titlestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		titlestyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		//自定义RGB颜色
		/*HSSFPalette palette2 = wb.getCustomPalette();  
		palette2.setColorAtIndex(HSSFColor.LIME.index, (byte) 140, (byte) 216, (byte) 226);*/
		//设置 前景色           
		//titlestyle.setFillForegroundColor(HSSFColor.BLUE.index);//添加前景色,内容看的清楚
		//titlestyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		HSSFFont font1 = wb.createFont();
		font1.setFontHeightInPoints((short)10);   //字体大小
		font1.setFontName("宋体");   //什么字体
		font1.setItalic(false);//是不倾斜
		font1.setStrikeout(false); //是不是划掉
		font1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);   //字体加粗
		titlestyle.setFont(font1);
		
		//设置考勤异常日期样式
		HSSFCellStyle style2=wb.createCellStyle();
		style2.setBorderBottom(HSSFCellStyle.BORDER_THIN);    //设置底线和颜色
		style2.setBottomBorderColor(HSSFColor.BLACK.index);
		style2.setBorderLeft(HSSFCellStyle.BORDER_THIN);   //设置左边线和颜色
		style2.setLeftBorderColor(HSSFColor.BLACK.index);
		style2.setBorderRight(HSSFCellStyle.BORDER_THIN);   //设置右边线和颜色
		style2.setRightBorderColor(HSSFColor.BLACK.index);
		style2.setBorderTop(HSSFCellStyle.BORDER_THIN);   //设置上面线和颜色
		style2.setTopBorderColor(HSSFColor.BLACK.index);
		//自定义RGB颜色
		/*HSSFPalette palette2 = wb.getCustomPalette();  
		palette2.setColorAtIndex(HSSFColor.LIME.index, (byte) 140, (byte) 216, (byte) 226);*/
		//设置 前景色           
		style2.setFillForegroundColor(HSSFColor.YELLOW.index);//添加前景色,内容看的清楚
		style2.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		
		//创建header页 
		HSSFHeader header = sheet.getHeader(); 
		//设置标题居中 
		header.setCenter("标题");
		
		String divid = request.getParameter("divid");
		if(divid == null){
			if(RoleType.UNIT_USERGROUP.getName().contains(auth.getRole().getEnname())){
				divid = auth.getUser().getUnit().getDivid();
			}
		}
		String deptid = request.getParameter("deptid");
		String date1 = request.getParameter("startDate");
		String date2 = request.getParameter("endDate");
		String columns = request.getParameter("columns");
		String[] cloumlist=columns.split(",");
		Map<String,String> m = new HashMap<String,String>();
		m.put("divid", divid);
		m.put("deptid", deptid);
		m.put("date1", date1);
		m.put("date2", date2);
	//	m.put("userid", auth.getUser().getId().toString());
		List<Map<String, Object>> list =leaveService.getLeaveList(m);
		HSSFRow row = null;
		HSSFCell cell = null;
		Map<String,Object> map = null;
		if(list != null && list.size()>0){
			row = sheet.createRow(0);//创建标题行
			row.setHeight((short)400);
			
		//	JSONArray columnsarr = JSON.parseArray(columns);
			for(int j=1;j<cloumlist.length;j++){
				String title = "";
				if(cloumlist[j].equalsIgnoreCase("divname")){
				   title="单位";
				}else if(cloumlist[j]=="deptname" || cloumlist[j].equalsIgnoreCase("deptname")){
				   title="部门";
				}else if(cloumlist[j]=="name" || cloumlist[j].equalsIgnoreCase("name")){
				   title="姓名";
				}else if(cloumlist[j]=="leavedate" || cloumlist[j].equalsIgnoreCase("leavedate")){
				   title="日期";
				}else if(cloumlist[j]=="leavetype" || cloumlist[j].equalsIgnoreCase("leavetype")){
				   title="请假类型";
				} 
                cell = row.createCell(j-1);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(new HSSFRichTextString(title));
				cell.setCellStyle(titlestyle);
				sheet.setColumnWidth(j, 3500);
			}
			//添加请假列标题
			/*cell = row.createCell(columnsarr.size());
			cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			cell.setCellValue(new HSSFRichTextString("请假天数"));
			cell.setCellStyle(titlestyle);
			sheet.setColumnWidth(columnsarr.size(), 2500);*/
			
		 try{  	for(int i=0;i<list.size();i++){
				map = list.get(i);
				row = sheet.createRow(i+1);//创建内容行
				//创建单位列
				cell = row.createCell(0);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(new HSSFRichTextString(String.valueOf(map.get("div"))));
				cell.setCellStyle(defaultstyle);
				sheet.setColumnWidth(0, 3000);
				
				//创建部门列
				cell = row.createCell(1);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(new HSSFRichTextString(String.valueOf(map.get("dept"))));
				cell.setCellStyle(defaultstyle);
				sheet.setColumnWidth(1, 3000);
				
				
				
				
				//添加姓名列内容
				//cell = row.createCell(columnsarr.size()-2);
				cell = row.createCell(2);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue(new HSSFRichTextString(String.valueOf(map.get("name"))));
				cell.setCellStyle(numbericstyle);
				sheet.setColumnWidth(2, 2500);
				//sheet.setColumnWidth(columnsarr.size()-2, 2500);
				//添加请假日期列内容
				//cell = row.createCell(columnsarr.size()-1);
				cell = row.createCell(3);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellValue(new HSSFRichTextString(String.valueOf(map.get("leavedate"))));
				cell.setCellStyle(numbericstyle);
				//sheet.setColumnWidth(columnsarr.size()-1, 2500);
				sheet.setColumnWidth(3, 2500);
				//添加请假列内容
				cell = row.createCell(4);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				String leavetype=String.valueOf(map.get("leavetype"));
				String type="";
				if(leavetype.equalsIgnoreCase("0")){
					type="年休";
				}else if(leavetype.equalsIgnoreCase("1")){
				    type="事假";
				}else if(leavetype.equalsIgnoreCase("2")){
				    type="病假";
				}else if(leavetype.equalsIgnoreCase("2")){
					type="调休";
				}
				cell.setCellValue(new HSSFRichTextString(type));
				cell.setCellStyle(numbericstyle);
				sheet.setColumnWidth(4, 2500);
				
			}
		 }catch(StringIndexOutOfBoundsException e)
		 {
			 e.printStackTrace();
		 }
		}
		//设置默认列宽
		sheet.setDefaultRowHeight((short)400);
		//设置footer 
		sheet.setGridsPrinted(true); 
		HSSFFooter footer = sheet.getFooter(); 
		footer.setRight("page   " + HeaderFooter.page() + "of" + HeaderFooter.numPages());
		
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/ms-excel");
		//response.setContentLength((int)file.length());
		response.setHeader("Result-Code", "0");
		try {
			response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode("请假统计.xls","UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			/*OutputStream os = new FileOutputStream(new File("d:\\test.xls"));
			wb.write(os);
			os.flush();
			os.close();*/
			response.setBufferSize(1024);
			/*OutputStream os = response.getOutputStream();
			os.write(wb.getBytes());
			os.flush();
			os.close();*/
			
			wb.write(response.getOutputStream());
			response.flushBuffer();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			//
		}
	}
	@RequestMapping("/leavemxList.action")
	public String attendcardmxList(Model model, HttpServletRequest request) {
		model.addAttribute("startDate", DateUtil.dateToString(DateUtil.getFirstDayOfMonth(),"yyyy-MM-dd"));
		model.addAttribute("endDate", DateUtil.dateToString(new Date(), "yyyy-MM-dd"));
		return "account/leavemxList";
	}
	@RequestMapping("/getLeaveListByUserid.action")
	@ResponseBody
	public Map getLeaveListByUserid(Model model, HttpServletRequest request) {
		Map<String, Object> m = new HashMap<String, Object>();
		
		Page<Object[]> p = new Page<Object[]>(Integer.parseInt(request.getParameter("rows")==null?"10":request.getParameter("rows")));
		p.setPageNo(Integer.parseInt(request.getParameter("page")));
		Map<String, Object> paramap = new HashMap<String, Object>();
		request.getParameterNames();
		paramap.put("stardate", request.getParameter("stardate"));
		paramap.put("enddate", request.getParameter("enddate"));
		paramap.put("userid", request.getParameter("userid"));
		List<Map<String,Object>>  list = leaveService.getLeaveListByUserid(p,paramap);
		m.put("rows", list);
		m.put("total", p.getTotalCount());
		return m;
	}
}
