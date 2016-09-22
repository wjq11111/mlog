
package sto.web.account;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;














import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import other.AuthProfile;
import sto.common.MlogPM;
import sto.common.SimpleUpload;
import sto.common.util.DateUtil;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.common.util.RoleType;
import sto.common.util.StringReg;
import sto.common.web.BaseController;
import sto.model.account.Journal;
import sto.model.account.JournalReply;
import sto.model.account.SysSettings;
import sto.service.account.JournalReplyService;
import sto.service.account.JournalService;
import sto.service.account.SysSettingsService;
import sto.utils.IdGen;

import com.alibaba.fastjson.JSONObject;

/**
 * 用户管理
 * 功能：列表、分配角色
 * 
 */
@Controller
@RequestMapping(value = "/journal")
@SuppressWarnings("unchecked")
public class JournalController extends BaseController{
	@Resource
	JournalService journalService;
	@Resource
	SysSettingsService sysSettingsService;
	@Resource
	JournalReplyService journalReplyService;
	
	@RequestMapping("/list.action")
	public String list(Model model, HttpServletRequest request) {
		model.addAttribute("startDate", DateUtil.dateToString(DateUtil.getFirstDayOfMonth(),"yyyy-MM-dd"));
		model.addAttribute("endDate", DateUtil.dateToString(new Date(), "yyyy-MM-dd"));
		return "account/journalList";
	}
	@RequestMapping("/add.action")
	public String add(Model model, HttpServletRequest request) {
		AuthProfile curruser = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("createtime", DateUtil.dateToString(new Date(),"yyyy-MM-dd"));
		model.addAttribute("user", curruser.getUser());
		return "account/journalSave";
	}
	@RequestMapping("/staticlist.action")
	public String statisticList(Model model, HttpServletRequest request) {
		model.addAttribute("startDate", DateUtil.dateToString(DateUtil.getFirstDayOfMonth(),"yyyy-MM-dd"));
		model.addAttribute("endDate", DateUtil.dateToString(new Date(), "yyyy-MM-dd"));
		return "account/journalstatisticList";
	}
	@RequestMapping("/statisticQueryColumns.action")
	@ResponseBody
	public JSONObject statisticQueryColumns(HttpServletRequest request){
		String date1 = request.getParameter("startDate");
		String date2 = request.getParameter("endDate");
		int days = (int)((DateUtil.stringToDate(date2, "yyyy-MM-dd").getTime()-DateUtil.stringToDate(date1, "yyyy-MM-dd").getTime())/(1000*60*60*24))+1;
		Map<String,String> m = new HashMap<String,String>();
		m.put("date1", date1);
		m.put("date2", date2);
		JSONObject json = new JSONObject();
		if(days>=35){//数据库中字段最大允许1024列，时间字段占8位，逗号1位，所以设定最大时间不超过35天，超过会溢出
			json.put("msg", "日期选择超出范围");
			json.put("success",false);
			return json;
		}
		try {
			json = JournalService.getJournalStatisticQueryColumns(m);
			if(!json.isEmpty()){
				json.put("success", true);
			}else {
				json.put("success", false);
				json.put("msg", "查询失败");
			}
		} catch (Exception e1) {
			json.put("success", false);
			json.put("msg", e1.getMessage());
			e1.printStackTrace();
		}
		return json;
	}


	/**
	 * 新增保存
	 */
	@RequestMapping(value="/saveDo.action",method=RequestMethod.POST)
	@ResponseBody
	public void saveDo(Journal r,HttpServletRequest request,HttpServletResponse response) {
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		
		MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		MultipartHttpServletRequest multipartRequest = resolver.resolveMultipart(request);

		
		MultipartFile multipartFile = multipartRequest.getFile("image");//获取上传图片
		//String content = multipartRequest.getParameter("amcontent")+"#||#"+multipartRequest.getParameter("pmcontent");
		String amcontent = multipartRequest.getParameter("amcontent");
		//String image = multipartRequest.getParameter("image");
		String lgt = multipartRequest.getParameter("longitude");
		String lat = multipartRequest.getParameter("latitude");
		String addr = multipartRequest.getParameter("address");
		//String costtime = request.getParameter("costtime");
        String image="";
		if(multipartFile != null&&multipartFile.getSize() >0 && !multipartFile.isEmpty()){
			SysSettings sysSettings = sysSettingsService.findUniqueBy("skey", "logimages");
			String filename = multipartFile.getOriginalFilename();
			int los = filename.lastIndexOf(".");
			String uploadFileNamePre = filename.substring(0,los);
			String uploadFileNameSuf = filename.substring(los,filename.length());
			//String basepath = sysSettings.getValue();
			String basepath1=request.getSession().getServletContext().getRealPath("/");
			String basepath=basepath1+"logimages"+File.separator;
			/*if(!basepath.endsWith("\\")){
				basepath += File.separator;
			}*/
			String uploadpath = "L"+new SimpleDateFormat("yyyyMMdd").format(new Date())+File.separator;
			String tempfilename =uploadFileNamePre+"_"+IdGen.uuid()+uploadFileNameSuf;
			image = uploadpath+tempfilename;
			try {
				boolean isSuccess = SimpleUpload.uploadByteFile(multipartFile.getInputStream(), basepath+uploadpath,tempfilename);
				if(isSuccess){
					System.out.println("日志图片上传成功");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Journal journal = new Journal();
		journal.setWriter(auth.getUser().getId());
		journal.setLgt(lgt);
		journal.setLat(lat);
		journal.setAddr(addr);
		journal.setContent(amcontent);
		journal.setImage(image);
		//journal.setCosttime(costtime);
		journal.setCreatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		journalService.save(journal);
		response.setContentType("text/html;charset=utf-8"); 
		try {
			response.getWriter().write("{'success':'true','msg':'保存成功'}");
			response.getWriter().flush();
			response.getWriter().close();

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	@RequestMapping("/getJournalAllList.action")
	@ResponseBody
	public Map getJournalAllList(Model model, HttpServletRequest request) {
		Map<String, Object> m = new HashMap<String, Object>();
		Page<Object[]> p = new Page<Object[]>(Integer.parseInt(request.getParameter("rows")==null?"10":request.getParameter("rows")));
		p.setPageNo(Integer.parseInt(request.getParameter("page")==null?"1":request.getParameter("page")));
		Map<String, Object> paramap = new HashMap<String, Object>();
		paramap.put("startDate", request.getParameter("startDate"));
		paramap.put("endDate", request.getParameter("endDate"));
		//paramap.put("name", request.getParameter("name"));
		paramap.put("divid", request.getParameter("divid"));
		paramap.put("deptid", request.getParameter("deptid"));
		List<Map<String,Object>>  list = journalService.getJournalAllList(p,paramap);
		m.put("rows", list);
		m.put("total", p.getTotalCount());
		return m;
	}
	
	@RequestMapping("/journalReplyList.action")
	public String journalReplyList(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
		AuthProfile curruser = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		String journalid = request.getParameter("journalid");
		model.addAttribute("replyer", curruser.getUser().getId());
		Map<String, Object> paramap = new HashMap<String, Object>();
		paramap.put("journalid", journalid);
		List<Map<String,Object>>  list = journalService.getJournalReplyByJournalId(paramap);
		model.addAttribute("replyList", list);
		model.addAttribute("journalid", request.getParameter("journalid"));
		String content = journalService.get(Integer.parseInt(journalid)).getContent().replace("\n", "<br/>");
		model.addAttribute("content", StringReg.replaceToHref(content));
		return "account/journalReplyList";
	}
	
	@RequestMapping("/replyJournal.action")
	@ResponseBody
	public Map replyJournal(JournalReply jr, HttpServletRequest request) {
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		int journalid = Integer.parseInt(request.getParameter("journalid"));
		String content = request.getParameter("content");
		
		JournalReply journalReply = new JournalReply();
		journalReply.setJournal(journalService.get(journalid));
		journalReply.setRecontent(content);
		journalReply.setRedate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		journalReply.setReplyer(auth.getUser().getId());
		journalReplyService.save(journalReply);
		return suc();
	}
	
	@RequestMapping("/resetWarnStatus.action")
	@ResponseBody
	public Map resetWarnStatus(HttpServletRequest request) {
		Session session = SecurityUtils.getSubject().getSession();
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		int id = Integer.parseInt(request.getParameter("journalid"));
		Journal journal = journalService.get(id);
		if(auth.getUser().getId().intValue() == journal.getWriter().intValue()){
			journalService.updateBySql("update mlog_journal set iswarn=0 where id=:p1", new Parameter(id));
		}
		return suc();
	}
	
	@RequestMapping("/getWarnStatus.action")
	@ResponseBody
	public JSONObject getWarnStatus(){
		JSONObject json = new JSONObject();
		AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
		json.put("iswarn", 0);
		if(auth.getUser().getId()!=null){
			List<Journal> list = journalService.findBySql("select * from mlog_journal a where a.writer=:p1 and a.iswarn=1", new Parameter(auth.getUser().getId()), Journal.class);
			if(list != null && list.size()>0){
				json.put("iswarn", 1);
			}
		}
		return json;
	}
	
	 /**
	    *  导出入Word
	    *
	    *
	    */
		@RequestMapping("/exportToWord.action")
		public void exportToWord(HttpServletRequest request,HttpServletResponse response){
			AuthProfile auth = (AuthProfile) SecurityUtils.getSubject().getPrincipal();
			//创建一个新的word
			HWPFDocument doc = null;
			String basepath1=request.getSession().getServletContext().getRealPath("/");//获取系统应用路径
			String filepath=basepath1+"download"+File.separator+"templet.doc";
			try {
				doc = new HWPFDocument(new FileInputStream(filepath));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			 Map<String, Object> paramap = new HashMap<String, Object>();
			 paramap.put("startDate", request.getParameter("startDate"));
		     paramap.put("endDate", request.getParameter("endDate"));
			 paramap.put("divid", request.getParameter("divid"));
			 paramap.put("deptid", request.getParameter("deptid"));

			List<Map<String,Object>>  list = journalService.getJournalWordList(paramap);
			/*String name=(String) list.get(list.size()-1).get("name");
			String time=(String) list.get(list.size()-1).get("time");
			String deptid=(String) list.get(list.size()-1).get("deptid");*/
			Range range=null;
			//for(Map<String, Object> o : list){
			for(int i=list.size()-1;i>=0;i--){
			 range = doc.getRange();  
			/*range.replaceText("${reportDate}",(String) o.get("time"));  
			range.replaceText("${reportDept}",(String) o.get("deptid"));  
			range.replaceText("${reportName}", (String) o.get("name"));  
			range.replaceText("${reportContent}", (String) o.get("content"));
			i=i+1;*/
			
			range.insertBefore(String.valueOf('\r'));
			range.insertBefore(String.valueOf('\n'));
			range.insertBefore((String) list.get(i).get("content"));
			range.insertBefore(String.valueOf('\r'));
			range.insertBefore(String.valueOf('\n'));
			range.insertBefore("/****************************************************************************/");
			range.insertBefore(String.valueOf('\r'));
			range.insertBefore(String.valueOf('\n'));
			range.insertBefore("日志内容：");
			range.insertBefore(String.valueOf('\r'));
			range.insertBefore(String.valueOf('\n'));
			range.insertBefore((String) list.get(i).get("name"));
			range.insertBefore("姓名：");	
			range.insertBefore(String.valueOf('\r'));
			range.insertBefore(String.valueOf('\n'));
			range.insertBefore((String) list.get(i).get("deptid"));
			range.insertBefore("部门：");	
			//range.insertBefore(String.valueOf('\n'));
			range.insertBefore(String.valueOf('\r'));
			range.insertBefore(String.valueOf('\n'));
			range.insertBefore((String) list.get(i).get("time"));
			range.insertBefore("时间：");	
			range.insertBefore(String.valueOf('\r'));
			range.insertBefore(String.valueOf('\n'));
			range.insertBefore("/****************************************************************************/");
			  }
			
           
		        response.setCharacterEncoding("utf-8");
				response.setContentType("application/ms-word");
				//response.setContentLength((int)file.length());
				response.setHeader("Result-Code", "0");
				try {
					response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode("日志统计.doc","UTF-8"));
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
					
					doc.write(response.getOutputStream());
					response.flushBuffer();
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					//
				}
			    System.out.println("success");
		   }

		
	
	
	public static void main(String[] args){
		String protocol = "(?:(mailto|ssh|ftp|https?)://)?"; 
		String hostname = "(?:[a-z0-9](?:[-a-z0-9]*[a-z0-9])?\\.)+(?:com|net|edu|biz|gov|org|in(?:t|fo)|(?-i:[a-z][a-z]))"; 
		String ip = "(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.(?:[01]?\\d\\d?|2[0-4]\\d|25[0-5])"; 
		String port= "(?::(\\d{1,5}))?"; 
		String path = "(/.*)?"; 
		String url = "("+protocol + "((?:" + hostname + "|" + ip + "))" + port + path+")";
		Pattern p = Pattern.compile(url);
		Matcher m = p.matcher("测试url http://www.baidu.comdadfsdf");
		while (m.find()) {  
			System.out.println(m.group(1));  
		}
	}
}
