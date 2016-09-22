package sto.web.interfaces;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import other.CertParse;
import sto.common.Constant_sto;
import sto.common.HttpOnline;
import sto.common.HttpOnline.OnlineIType;
import sto.common.Md5Encrypt;
import sto.common.MlogPM;
import sto.common.SimpleUpload;
import sto.common.sms.SmsController;
import sto.common.util.DateUtil;
import sto.common.util.Page;
import sto.common.util.Parameter;
import sto.common.util.PinyinUtil;
import sto.common.util.StringUtils;
import sto.common.util.VerifyCodeUtil2;
import sto.common.web.BaseController;
import sto.form.AttendForm1;
import sto.form.AttendForm2;
import sto.form.AttendForm3;
import sto.form.JournalForm;
import sto.form.MsgForm;
import sto.form.PositionForm;
import sto.form.RegUnitForm;
import sto.model.account.App;
import sto.model.account.Attend;
import sto.model.account.BugReport;
import sto.model.account.Dept;
import sto.model.account.Door;
import sto.model.account.Journal;
import sto.model.account.JournalReply;
import sto.model.account.Msg;
import sto.model.account.MsgReceiver;
import sto.model.account.MsgReply;
import sto.model.account.Position;
import sto.model.account.Role;
import sto.model.account.SysSettings;
import sto.model.account.Unit;
import sto.model.account.UnitSettings;
import sto.model.account.User;
import sto.model.account.UserSettings;
import sto.model.account.Verifycode;
import sto.service.account.AppService;
import sto.service.account.AttendService;
import sto.service.account.BugReportService;
import sto.service.account.DataRulesService;
import sto.service.account.DeptService;
import sto.service.account.DoorService;
import sto.service.account.JournalReplyService;
import sto.service.account.JournalService;
import sto.service.account.MsgReceiverService;
import sto.service.account.MsgReplyService;
import sto.service.account.MsgService;
import sto.service.account.PositionService;
import sto.service.account.SysSettingsService;
import sto.service.account.UnitService;
import sto.service.account.UnitSettingsService;
import sto.service.account.UserService;
import sto.service.account.UserSettingsService;
import sto.service.account.VerifycodeService;
import sto.utils.IdGen;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.hebca.pki.Cert;
import com.hebca.sms.ShortMessage;
import com.hebca.sms.SmsException;
import com.hebca.sms.SmsProvider;
import com.koal.svs.client.st.THostInfoSt;

@Controller
@RequestMapping(value = "/interface")
public class MobileController extends BaseController {
	private static Logger log = Logger.getLogger(MobileController.class);

	private static ContentType contentType = ContentType.JSON;
	private static Map<String, Date> timeMap = new HashMap();
	@Resource(name = "hostInfo")
	THostInfoSt hostInfo;
	@Resource
	UserService userService;
	@Resource
	AttendService attendService;
	@Resource
	MsgService msgService;
	@Resource
	MsgReceiverService msgReceiverService;
	@Resource
	MsgReplyService msgReplyService;
	@Resource
	JournalService journalService;
	@Resource
	JournalReplyService journalReplyService;
	@Resource
	PositionService positionService;
	@Resource
	DeptService deptService;
	@Resource
	SysSettingsService sysSettingsService;
	@Resource
	UnitSettingsService unitSettingsService;
	@Resource
	DataRulesService dataRulesService;
	@Resource
	UnitService unitService;
	@Resource
	AppService appService;
	@Resource
	BugReportService bugReportService;
	@Resource
	UserSettingsService userSettingsService;
	@Resource
	VerifycodeService verifycodeService;
	@Resource
	DoorService doorService;

	@RequestMapping("/getUser.action")
	public void getUser(HttpServletRequest request, HttpServletResponse response) {
		log.debug("getUser.action");
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		int resultCode = 0;
		String username = request.getParameter("username");
		String password = Md5Encrypt.md5(StringUtils.trimToEmpty(request
				.getParameter("password")));
		String divid = request.getParameter("divid");
		log.debug(username + ":" + divid);
		try {
			List<Unit> list = unitService.find(
					"from Unit where 1=1 and isregistered=1 and divid=:p1",
					new Parameter(divid));
			if (list != null && list.size() > 0) {
				json1.put("projectid", list.get(0).getProjectid());
				json1.put("divname", list.get(0).getDivname());
				User user = new User();
				user.setUsername(username);
				user.setPassword(password);
				Unit unit = new Unit();
				unit.setDivid(divid);
				user.setUnit(unit);
				// flag 1成功 2用户名不存在 3密码不正确 4用户名不能为空 5 密码不能为空
				int flag = userService.checkUserNameAndPassWordByDivid(user);
				if (flag == 1) {
					User u = userService.getUserByNameAndPassWordByDivid(user);
					json1.put("userid", u.getId());
					json1.put("name", StringUtils.trimToEmpty(u.getName()));
					json1.put("mobilephone",
							StringUtils.trimToEmpty(u.getMobilephone()));
					json1.put("identitycard",
							StringUtils.trimToEmpty(u.getIdentitycard()));
					json1.put("certcn", StringUtils.trimToEmpty(u.getScertcn()));

					json.put("success", true);
					json.put("data", json1);
					resultCode = 0;
				} else if (flag == 2) {
					json.put("success", false);
					json.put("msg", "该单位无此用户");
					resultCode = 0;
				} else if (flag == 3) {
					json.put("success", false);
					json.put("msg", "账户已冻结");
					resultCode = 0;
				} else if (flag == 4) {
					json.put("success", false);
					json.put("msg", "密码错误");
					resultCode = 0;
				}
			} else {
				json.put("success", false);
				json.put("msg", "该单位不存在或单位已冻结");
				resultCode = 0;
			}

			super.writeJson(request, response, contentType, resultCode, json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping("/login.action")
	public void login(HttpServletRequest request, HttpServletResponse response) {
		log.debug("login.action");
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		int resultCode = 0;
		try {
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String divid = request.getParameter("divid");
			String imei = request.getParameter("imei");
			String deviceid = request.getParameter("deviceid");
			String packageversion = request.getParameter("packageversion");
			log.debug(username + ":" + divid + ":imei:" + imei);
			System.out.println(username + ":" + divid + ":imei:" + imei
					+ ";deviceid:" + deviceid);
			// 进行证书验证
			String error = "";
			String cert = request.getParameter("cert");
			String random = request.getParameter("random");
			String randomsign = request.getParameter("randomsign");

			if (cert == null) {
				cert = "";
			}
			if (random == null) {
				random = "";
			}
			if (randomsign == null) {
				randomsign = "";
			}
			if (cert != null && random != null && randomsign != null) {
				int result = -1;
				String uniqueFlag = ""; // 证书唯一标示
				try {
					System.out.println("解析证书" + cert);
					byte[] arrayOriginData = random.getBytes();
					int nOriginDataLen = arrayOriginData.length;
					// SvsClientHelper svsclient =
					// SvsClientHelper.getInstance();
					// 与svs服务器建立连接
					// svsclient.initialize(hostInfo.getSvrIP(),
					// hostInfo.getPort(), 1000, false, 5000);
					// result = svsclient.verifyCertSign(-1, 0, arrayOriginData,
					// nOriginDataLen, cert, randomsign, 1, hostInfo);
					// 进行证书验证，包含三方面验证：（1）用户身份验证 （2）证书状态验证 （3）证书有效期验证
					System.out.println("result证书监测" + result);
					result = 0;
					if (result == 0) {
						CertParse cp = new CertParse(new Cert(cert)); // 解析证书

						String CN = cp.getSubject(CertParse.DN_CN); // 获取证书CN号
						String gName = cp.getSubject(CertParse.DN_GIVENNAME); // 获取证书名称
						if (CN.length() > gName.length()) { // 通过判断这两个字段的长度来确定哪个是证书唯一标示。
							uniqueFlag = CN;
						} else {
							uniqueFlag = gName;
						}
						System.out.println("获取cn" + uniqueFlag);
						if (!StringUtils.isBlank(uniqueFlag)) {
							// flag 1成功 2证书未绑定 3证书不存在
							Map<String, String> map = new HashMap<String, String>();
							map.put("scertcn", uniqueFlag);
							map.put("imei", imei);
							map.put("deviceid", deviceid);
							int flag = userService.checkScertcn(map);
							if (flag == 1 || flag == 4) {
								User u = userService
										.getUserByScertcn(uniqueFlag);
								if (u.getUnit() == null
										|| u.getUnit().getDivid() == null
										|| u.getUnit().getDivid().trim()
												.equals("")) {
									json.put("success", false);
									json.put("msg", "非单位用户");
									resultCode = 0;
								} else {
									json1.put("userid", u.getId());
									json1.put("name", StringUtils.trimToEmpty(u
											.getName()));
									json1.put("deptid", u.getDept().getDeptid());
									json1.put("deptname", StringUtils
											.trimToEmpty(u.getDept()
													.getDeptname()));
									json1.put("clientrole", StringUtils
											.trimToEmpty(String.valueOf(u
													.getClientrole())));
									if (flag == 4) {// 未与任何设备绑定，则绑定该设备
										u.setImei(imei);
										u.setDeviceid(deviceid);
										// userService.save(u);
									}
									u.setPackageversion(packageversion);
									userService.save(u);
									json.put("success", true);
									json.put("data", json1);
									resultCode = 0;
								}
							} else if (flag == 2) {
								if (null != username
										&& !username.trim().equals("")
										&& null != password
										&& !password.trim().equals("")) {
									User user = new User();
									user.setUsername(username);
									user.setPassword(Md5Encrypt.md5(password));
									Unit unit = new Unit();
									unit.setDivid(divid);
									user.setUnit(unit);
									// flag 1成功 2用户名不存在
									int flag1 = userService
											.checkUserNameAndPassWordByDivid(user);
									if (flag1 == 1) {
										User u = userService
												.getUserByNameAndPassWordByDivid(user);
										String certcn = StringUtils
												.trimToEmpty(u.getScertcn());
										if ("".equals(certcn)) {// 用户未绑定软证书
											u.setScertcn(uniqueFlag);// 绑定证书
											u.setImei(imei);
											u.setPackageversion(packageversion);// 记录用户使用版本
											userService.save(u);
											json1.put("userid", u.getId());
											json1.put("name", StringUtils
													.trimToEmpty(u.getName()));
											json1.put("deptid", u.getDept()
													.getDeptid());
											json1.put("deptname", StringUtils
													.trimToEmpty(u.getDept()
															.getDeptname()));
											json1.put(
													"clientrole",
													StringUtils.trimToEmpty(String.valueOf(u
															.getClientrole())));

											json.put("success", true);
											json.put("data", json1);
											resultCode = 0;
										} else {// 用户已绑定软证书
											json.put("success", false);
											json.put("msg",
													"该用户已绑定其他证书，请尝试其他用户名，或联系管理员解除绑定");
											resultCode = 0;
										}
									} else if (flag1 == 2) {
										json.put("success", false);
										json.put("msg", "该单位无此用户");
										resultCode = 0;
									} else if (flag1 == 3) {
										json.put("success", false);
										json.put("msg", "账户已冻结");
										resultCode = 0;
									} else if (flag1 == 4) {
										json.put("success", false);
										json.put("msg", "密码错误");
										resultCode = 1;
									}
								} else {
									json1.put("userid", -1);// 证书未绑定用户,userid返回-1
									json1.put("name", "");
									json1.put("deptid", -1);
									json1.put("deptname", "");
									json1.put("clientrole", "");

									json.put("success", true);// 证书未绑定用户
									json.put("data", json1);
									resultCode = 0;
								}
							} else if (flag == 3) {
								json.put("success", false);
								json.put("msg", "证书不存在");
							} else if (flag == 5) {
								json.put("success", false);
								json.put("msg", "该证书已绑定其他设备");
							} else if (flag == 6) {
								json.put("success", false);
								json.put("msg", "该账户已冻结");
							} else {
								json.put("success", false);
								json.put("msg", "未知错误");
							}
						}
					} else {
						// 验证失败
						switch (result) {
						case -1:
							error = "(无法连接svs服务器)";
							break;
						case 2:
							error = "(证书已经过期，需要延期后才能使用)";
							break;
						case -6805:
							error = "(无效的证书文件)";
							break;
						case -6406:
							error = "(签名验证失败)";
							break;
						default:
							error = "(errorcode:" + result + ")";
						}
						System.out.println("errorcode验签异常" + result);
						json.put("success", false);
						json.put("msg", "验签错误码：" + result + ";错误信息：" + error);
					}
				} catch (Exception e) {
					e.printStackTrace();
					json.put("success", false);
					json.put("msg", "验签异常：" + e.getMessage());
				}

			} else {
				json.put("success", false);
				json.put("msg", "获取证书异常");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/attend.action")
	/*
	 * public void attend(HttpServletRequest request, HttpServletResponse
	 * response) { log.debug("attend.action"); JSONObject json = new
	 * JSONObject(); JSONObject json1 = new JSONObject(); int resultCode = 0;
	 * int userid = Integer.parseInt(request.getParameter("userid")); String lgt
	 * = request.getParameter("longitude"); String lat =
	 * request.getParameter("latitude"); String address =
	 * request.getParameter("address"); int type =
	 * Integer.parseInt(request.getParameter("type")); log.debug(userid + ":" +
	 * lgt + ":" + lat); Attend ad = null; Date date = new Date();
	 * 
	 * ad = attendService.getLastAttend(userid); if(ad!=null){ Date ondate =
	 * DateUtil.stringToDate(ad.getOntime(), "yyyy-MM-dd");// 上班卡日期 Date newdate
	 * = DateUtil.dateToDate(date, "yyyy-MM-dd");// 当前日期 if
	 * (ondate.before(newdate)) { if ((ad.getOfftime() == null ||
	 * "".equals(ad.getOfftime()))) { // 隔天打卡 ad.setOfflat(lat);
	 * ad.setOffaddr(address); ad.setOfftime(DateUtil.dateToString(ondate,
	 * "yyyy-MM-dd") + " 23:59:59"); ad.setStatus(1);
	 * ad.setLasttime(DateUtil.dateToString(ondate, "yyyy-MM-dd") +
	 * " 23:59:59"); attendService.save(ad); // 隔天打下班卡情况，添加打下班卡当天的上下班卡记录 Attend
	 * extra_ad = new Attend(); extra_ad.setUserid(userid);
	 * extra_ad.setOnlgt(lgt); extra_ad.setOnlat(lat);
	 * extra_ad.setOnaddr(address);
	 * extra_ad.setOntime(DateUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss"));
	 * extra_ad.setStatus(0);
	 * 
	 * extra_ad.setLasttime(DateUtil.dateToString(date, "yyyy-MM-dd HH:mm:ss"));
	 * attendService.save(extra_ad); } else { Attend adnew = new Attend();
	 * adnew.setUserid(userid); adnew.setOnlgt(lgt); adnew.setOnlat(lat);
	 * adnew.setOnaddr(address); adnew.setOntime(new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .format(new Date()));
	 * adnew.setStatus(0); adnew.setLasttime(new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .format(new Date())); User user =
	 * userService.get(userid); if (user.getUnit() == null) {
	 * json1.put("frequency", 10 * 60 * 1000); } else { List<UnitSettings> list1
	 * = unitSettingsService
	 * .find(" from UnitSettings where skey='frequency' and divid=:p1", new
	 * Parameter(user.getUnit().getDivid())); if (list1 != null && list1.size()
	 * > 0) { UnitSettings unitSettings = list1.get(0); json1.put( "frequency",
	 * Integer.parseInt(unitSettings.getValue()) * 60 * 1000); } else {
	 * json1.put("frequency", 10 * 60 * 1000); } } attendService.save(adnew); }
	 * 
	 * } else {
	 * 
	 * if (ad.getOfftime() != null) {// 插入一整条记录，上班时间为上次打卡时间，下班时间为这次打卡时间 Attend
	 * adnew = new Attend(); adnew.setUserid(userid);
	 * adnew.setOnlgt(ad.getOfflgt()); adnew.setOnlat(ad.getOfflat());
	 * adnew.setOnaddr(ad.getOffaddr()); adnew.setOntime(ad.getOfftime());
	 * adnew.setOfflgt(lgt); adnew.setOfflat(lat); adnew.setOffaddr(address);
	 * adnew.setOfftime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .format(new
	 * Date())); adnew.setStatus(1); adnew.setLasttime(new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .format(new Date())); User user =
	 * userService.get(userid); if (user.getUnit() == null) {
	 * json1.put("frequency", 10 * 60 * 1000); } else { List<UnitSettings> list1
	 * = unitSettingsService
	 * .find(" from UnitSettings where skey='frequency' and divid=:p1", new
	 * Parameter(user.getUnit().getDivid())); if (list1 != null && list1.size()
	 * > 0) { UnitSettings unitSettings = list1.get(0); json1.put( "frequency",
	 * Integer.parseInt(unitSettings.getValue()) * 60 * 1000); } else {
	 * json1.put("frequency", 10 * 60 * 1000); } } attendService.save(adnew); }
	 * else {// 第二次打卡状态记录
	 * 
	 * ad.setOfflgt(lgt); ad.setOfflat(lat); ad.setOffaddr(address);
	 * ad.setOfftime(DateUtil .dateToString(date, "yyyy-MM-dd HH:mm:ss"));
	 * ad.setStatus(1); ad.setLasttime(DateUtil.dateToString(date,
	 * "yyyy-MM-dd HH:mm:ss")); attendService.save(ad); }
	 * 
	 * } //attendService.save(ad); }else{ Attend adnew = new Attend();
	 * adnew.setUserid(userid); adnew.setOnlgt(lgt); adnew.setOnlat(lat);
	 * adnew.setOnaddr(address); adnew.setOntime(new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .format(new Date()));
	 * adnew.setStatus(0); adnew.setLasttime(new
	 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss") .format(new Date())); User user =
	 * userService.get(userid); if (user.getUnit() == null) {
	 * json1.put("frequency", 10 * 60 * 1000); } else { List<UnitSettings> list1
	 * = unitSettingsService
	 * .find(" from UnitSettings where skey='frequency' and divid=:p1", new
	 * Parameter(user.getUnit().getDivid())); if (list1 != null && list1.size()
	 * > 0) { UnitSettings unitSettings = list1.get(0); json1.put( "frequency",
	 * Integer.parseInt(unitSettings.getValue()) * 60 * 1000); } else {
	 * json1.put("frequency", 10 * 60 * 1000); } } attendService.save(adnew); }
	 * 
	 * 
	 * 
	 * 
	 * json.put("success", true); json.put("data", json1);// 考勤打卡结束
	 * super.writeJson(request, response, contentType, resultCode, json); }
	 */
	public void attend(HttpServletRequest request, HttpServletResponse response) {
		log.debug("attend.action");
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String lgt = request.getParameter("longitude");
		String lat = request.getParameter("latitude");
		String address = request.getParameter("address");
		int type = Integer.parseInt(request.getParameter("type"));
		log.debug(userid + ":" + lgt + ":" + lat);
		Attend ad = null;
		if (type == 1) {// 上班卡
			ad = new Attend();
			ad.setUserid(userid);
			ad.setOnlgt(lgt);
			ad.setOnlat(lat);
			ad.setOnaddr(address);
			ad.setOntime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date()));
			ad.setStatus(0);
			ad.setLasttime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new Date()));
			User user = userService.get(userid);
			if (user.getUnit() == null) {
				json1.put("frequency", 10 * 60 * 1000);
			} else {
				List<UnitSettings> list1 = unitSettingsService
						.find(" from UnitSettings where skey='frequency' and divid=:p1",
								new Parameter(user.getUnit().getDivid()));
				if (list1 != null && list1.size() > 0) {
					UnitSettings unitSettings = list1.get(0);
					json1.put(
							"frequency",
							Integer.parseInt(unitSettings.getValue()) * 60 * 1000);
				} else {
					json1.put("frequency", 10 * 60 * 1000);
				}
			}

		} else {// 下班卡
			ad = attendService.getLastAttend(userid);
			Date date = new Date();
			Date ondate = DateUtil.stringToDate(ad.getOntime(), "yyyy-MM-dd");// 上班卡日期
			Date offdate = DateUtil.dateToDate(date, "yyyy-MM-dd");// 下班卡日期
			if (ondate.before(offdate)) {// 隔天打卡
				// 隔天打下班卡情况，添加最后一次打上班卡当天的下班卡记录
				ad.setOfflgt(lgt);
				ad.setOfflat(lat);
				ad.setOffaddr(address);
				ad.setOfftime(DateUtil.dateToString(ondate, "yyyy-MM-dd")
						+ " 23:59:59");
				ad.setStatus(1);
				ad.setLasttime(DateUtil.dateToString(ondate, "yyyy-MM-dd")
						+ " 23:59:59");

				// 隔天打下班卡情况，添加打下班卡当天的上下班卡记录
				Attend extra_ad = new Attend();
				extra_ad.setUserid(userid);
				extra_ad.setOnlgt(lgt);
				extra_ad.setOnlat(lat);
				extra_ad.setOnaddr(address);
				extra_ad.setOntime(DateUtil.dateToString(offdate, "yyyy-MM-dd")
						+ " 00:00:00");
				// extra_ad.setStatus(0);
				extra_ad.setOfflgt(lgt);
				extra_ad.setOfflat(lat);
				extra_ad.setOffaddr(address);
				extra_ad.setOfftime(DateUtil.dateToString(date,
						"yyyy-MM-dd HH:mm:ss"));
				extra_ad.setStatus(1);

				extra_ad.setLasttime(DateUtil.dateToString(date,
						"yyyy-MM-dd HH:mm:ss"));
				attendService.save(extra_ad);
			} else {
				ad.setOfflgt(lgt);
				ad.setOfflat(lat);
				ad.setOffaddr(address);
				ad.setOfftime(DateUtil
						.dateToString(date, "yyyy-MM-dd HH:mm:ss"));
				ad.setStatus(1);
				ad.setLasttime(DateUtil.dateToString(date,
						"yyyy-MM-dd HH:mm:ss"));
			}
		}

		attendService.save(ad);

		json.put("success", true);
		json.put("data", json1);

		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getLastAttend.action")
	public void getLastAttend(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		int resultCode = 0;
		String userid = request.getParameter("userid");
		Attend ad = attendService.getLastAttend(Integer.parseInt(userid));
		if (ad != null) {
			if (ad.getStatus() == 0) {
				json1.put("time", StringUtils.trimToEmpty(ad.getOntime()));
				json1.put("longitude", StringUtils.trimToEmpty(ad.getOnlgt()));
				json1.put("latitude", StringUtils.trimToEmpty(ad.getOnlat()));
				json1.put("address", StringUtils.trimToEmpty(ad.getOnaddr()));
				json1.put("type", 1);
			} else {
				json1.put("time", StringUtils.trimToEmpty(ad.getOfftime()));
				json1.put("longitude", StringUtils.trimToEmpty(ad.getOfflgt()));
				json1.put("latitude", StringUtils.trimToEmpty(ad.getOfflat()));
				json1.put("address", StringUtils.trimToEmpty(ad.getOffaddr()));
				json1.put("type", 2);
			}
		}
		json.put("success", true);
		json.put("data", json1);

		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getAttendStatisticsList.action")
	public void getAttendStatisticsList(HttpServletRequest request,
			HttpServletResponse response) {
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String getuserid = request.getParameter("getuserid");// 接口要求只能传一个人
		String date = request.getParameter("date");
		String badonly = request.getParameter("badonly");
		String pagenum = request.getParameter("pagenum");
		String pagesize = request.getParameter("pagesize");

		JSONObject json1 = new JSONObject();
		getuserid = dataRulesService.filterAuthUser(1, userid, getuserid);
		AttendForm1 form1 = attendService.getBadAttendMonthStatistics1(userid,
				getuserid, date);
		json1.put("userid", form1.getUserid());
		json1.put("username", form1.getUsername());
		json1.put("baddays", form1.getBaddays());

		JSONArray arr1 = new JSONArray();
		Map<String, String> map = new HashMap<String, String>();
		map.put("getuserid", getuserid);
		map.put("date", date);
		map.put("badonly", badonly);
		map.put("pagenum", pagenum);
		map.put("pagesize", pagesize);
		// 获取用户当月考勤异常天数
		JSONObject result = attendService.getIAttendListByUserid(map);
		List<AttendForm2> list = (List<AttendForm2>) result.get("rows");
		json1.put("total", result.getIntValue("total"));
		for (AttendForm2 form2 : list) {
			JSONObject json2 = new JSONObject();
			json2.put("date", form2.getDate());
			json2.put("worktime", form2.getWorktime());
			json2.put("isbad", form2.getIsbad());

			JSONArray arr2 = new JSONArray();
			// 获取用户某天的打卡记录
			List<AttendForm3> list1 = attendService.getAttendCardListByUserid(
					getuserid, form2.getDate());
			for (AttendForm3 form3 : list1) {
				JSONObject json3 = new JSONObject();
				json3.put("type", form3.getType());
				json3.put("time", form3.getTime());
				json3.put("longitude", form3.getLongitude());
				json3.put("latitude", form3.getLatitude());
				json3.put("address", form3.getAddress());
				arr2.add(json3);
			}
			json2.put("attendlist", arr2);
			arr1.add(json2);
		}
		json1.put("records", arr1);
		JSONObject json = new JSONObject();
		json.put("data", json1);
		json.put("success", true);
		log.info("super类" + super.hashCode() + "response::"
				+ response.hashCode() + "考勤记录：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/sendMsg.action")
	public void sendMsg(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		// boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		CommonsMultipartResolver resolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		resolver.setResolveLazily(true);
		MultipartHttpServletRequest multipartRequest = resolver
				.resolveMultipart(request);
		MultipartFile multipartFile = multipartRequest.getFile("image");
		System.out.println("=========================="
				+ multipartRequest.getContentType());
		String image = "";
		if (multipartFile != null) {
			SysSettings sysSettings = sysSettingsService.findUniqueBy("skey",
					"msgimages");
			String filename = multipartFile.getOriginalFilename();
			int los = filename.lastIndexOf(".");
			multipartFile.getName();
			String uploadFileNamePre = filename.substring(0, los);
			String uploadFileNameSuf = filename.substring(los,
					filename.length());
			// String basepath = sysSettings.getValue();
			String basepath1 = request.getSession().getServletContext()
					.getRealPath("/");
			String basepath = basepath1 + "msgimages" + File.separator;
			String uploadpath = "M"
					+ new SimpleDateFormat("yyyyMMdd").format(new Date())
					+ File.separator;
			String tempfilename = uploadFileNamePre + "_" + IdGen.uuid()
					+ uploadFileNameSuf;
			image = uploadpath + tempfilename;
			try {
				boolean isSuccess = SimpleUpload.uploadByteFile(
						multipartFile.getInputStream(), basepath + uploadpath,
						tempfilename);
				if (isSuccess) {
					System.out.println("msg图片上传成功");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String userid = multipartRequest.getParameter("userid");
		String content = multipartRequest.getParameter("content");
		String receivers = multipartRequest.getParameter("receivers");
		String lgt = multipartRequest.getParameter("longitude");
		String lat = multipartRequest.getParameter("latitude");
		String addr = multipartRequest.getParameter("address");

		request.setAttribute("receivers", receivers);// 用于给拦截器传递参数

		Msg msg = new Msg();
		msg.setPublisher(Integer.parseInt(userid));
		msg.setContent(content);
		msg.setImage(image);
		msg.setLgt(lgt);
		msg.setLat(lat);
		msg.setAddr(addr);
		msg.setCreatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		msg.setTypes("0");// 0代表消息，1代表会议
		msgService.save(msg);

		MsgReceiver msgReceiver = null;
		String[] userids = receivers.split(",");
		if (!Arrays.asList(userids).contains(userid)) {
			msgReceiver = new MsgReceiver();
			msgReceiver.setMsg(msg);
			msgReceiver.setReceiver(Integer.parseInt(userid));
			msgReceiverService.save(msgReceiver);
		}
		for (String receiver : userids) {
			msgReceiver = new MsgReceiver();
			msgReceiver.setMsg(msg);
			msgReceiver.setReceiver(Integer.parseInt(receiver));
			msgReceiverService.save(msgReceiver);
		}
		JSONObject json1 = new JSONObject();
		json1.put("image", image);
		json.put("success", true);
		json.put("data", json1);
		log.info("发送消息：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getMsgList.action")
	public void getMsgList(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		JSONArray arr = new JSONArray();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String getuserids = request.getParameter("getuserid");
		int pagenum = Integer.parseInt(request.getParameter("pagenum"));
		int pagesize = Integer.parseInt(request.getParameter("pagesize"));
		Page<Object[]> page = new Page<Object[]>(pagesize);
		page.setPageNo(pagenum);
		List<MsgForm> list = msgService.getMsgList(userid, getuserids, page);
		if (list.size() > 0) {
			arr.addAll(list);
			json1.put("total", page.getTotalCount());
			json1.put("records", arr);

		}
		json.put("data", json1);
		json.put("success", true);
		log.info("获取消息：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/replyMsg.action")
	public void replyMsg(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		int msgid = Integer.parseInt(request.getParameter("replyid"));
		String replytostr = request.getParameter("replyto");
		int replyto = Integer.parseInt(StringUtils.isBlank(replytostr) ? "0"
				: replytostr);
		String content = request.getParameter("content");

		MsgReply msgReply = new MsgReply();
		msgReply.setMsg(msgService.get(msgid));
		msgReply.setRecontent(content);
		msgReply.setRedate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		msgReply.setReplyer(userid);
		msgReply.setReplyto(replyto);
		msgReplyService.save(msgReply);

		json.put("success", true);
		json.put("data", new JSONObject());

		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/sendMeeting.action")
	// 发送会议消息
	public void sendMeeting(HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		// boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		CommonsMultipartResolver resolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		resolver.setResolveLazily(true);
		MultipartHttpServletRequest multipartRequest = resolver
				.resolveMultipart(request);
		MultipartFile multipartFile = multipartRequest.getFile("image");
		System.out.println("=========================="
				+ multipartRequest.getContentType());
		String image = "";
		if (multipartFile != null) {
			SysSettings sysSettings = sysSettingsService.findUniqueBy("skey",
					"msgimages");
			String filename = multipartFile.getOriginalFilename();
			int los = filename.lastIndexOf(".");
			multipartFile.getName();
			String uploadFileNamePre = filename.substring(0, los);
			String uploadFileNameSuf = filename.substring(los,
					filename.length());
			// String basepath = sysSettings.getValue();
			String basepath1 = request.getSession().getServletContext()
					.getRealPath("/");
			String basepath = basepath1 + "msgimages" + File.separator;
			String uploadpath = "M"
					+ new SimpleDateFormat("yyyyMMdd").format(new Date())
					+ File.separator;
			String tempfilename = uploadFileNamePre + "_" + IdGen.uuid()
					+ uploadFileNameSuf;
			image = uploadpath + tempfilename;
			try {
				boolean isSuccess = SimpleUpload.uploadByteFile(
						multipartFile.getInputStream(), basepath + uploadpath,
						tempfilename);
				if (isSuccess) {
					System.out.println("msg图片上传成功");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String userid = multipartRequest.getParameter("userid");
		String content = multipartRequest.getParameter("content");
		String receivers = multipartRequest.getParameter("receivers");
		String lgt = multipartRequest.getParameter("longitude");
		String lat = multipartRequest.getParameter("latitude");
		String addr = multipartRequest.getParameter("address");

		request.setAttribute("receivers", receivers);// 用于给拦截器传递参数

		Msg msg = new Msg();
		msg.setPublisher(Integer.parseInt(userid));
		msg.setContent(content);
		msg.setImage(image);
		msg.setLgt(lgt);
		msg.setLat(lat);
		msg.setAddr(addr);
		msg.setCreatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		msg.setTypes("1");// 0代表消息，1代表会议
		msgService.save(msg);

		MsgReceiver msgReceiver = null;
		String[] userids = receivers.split(",");
		if (!Arrays.asList(userids).contains(userid)) {
			msgReceiver = new MsgReceiver();
			msgReceiver.setMsg(msg);
			msgReceiver.setReceiver(Integer.parseInt(userid));
			msgReceiverService.save(msgReceiver);
		}
		for (String receiver : userids) {
			msgReceiver = new MsgReceiver();
			msgReceiver.setMsg(msg);
			msgReceiver.setReceiver(Integer.parseInt(receiver));
			msgReceiverService.save(msgReceiver);
		}
		JSONObject json1 = new JSONObject();
		json1.put("image", image);
		json.put("success", true);
		json.put("data", json1);
		log.info("发送消息：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getMeetingList.action")
	// 获取会议列表
	public void getMeetingList(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		JSONArray arr = new JSONArray();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String getuserids = request.getParameter("getuserid");
		int pagenum = Integer.parseInt(request.getParameter("pagenum"));
		int pagesize = Integer.parseInt(request.getParameter("pagesize"));
		Page<Object[]> page = new Page<Object[]>(pagesize);
		page.setPageNo(pagenum);
		List<MsgForm> list = msgService
				.getMeetingList(userid, getuserids, page);
		if (list.size() > 0) {
			arr.addAll(list);
			json1.put("total", page.getTotalCount());
			json1.put("records", arr);

		}
		json.put("data", json1);
		json.put("success", true);
		log.info("获取消息：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/replyMeeting.action")
	// 回复会议
	public void replyMeeting(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		int msgid = Integer.parseInt(request.getParameter("replyid"));
		String replytostr = request.getParameter("replyto");
		int replyto = Integer.parseInt(StringUtils.isBlank(replytostr) ? "0"
				: replytostr);
		String content = request.getParameter("content");

		MsgReply msgReply = new MsgReply();
		msgReply.setMsg(msgService.get(msgid));
		msgReply.setRecontent(content);
		msgReply.setRedate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		msgReply.setReplyer(userid);
		msgReply.setReplyto(replyto);
		msgReplyService.save(msgReply);

		json.put("success", true);
		json.put("data", new JSONObject());

		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/writeJournal.action")
	public void writeJournal(HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		MultipartResolver resolver = new CommonsMultipartResolver(request
				.getSession().getServletContext());
		MultipartHttpServletRequest multipartRequest = resolver
				.resolveMultipart(request);
		MultipartFile multipartFile = multipartRequest.getFile("image");
		String image = "";
		if (multipartFile != null) {
			SysSettings sysSettings = sysSettingsService.findUniqueBy("skey",
					"logimages");
			String filename = multipartFile.getOriginalFilename();
			int los = filename.lastIndexOf(".");
			String uploadFileNamePre = filename.substring(0, los);
			String uploadFileNameSuf = filename.substring(los,
					filename.length());
			// String basepath = sysSettings.getValue();
			String basepath1 = request.getSession().getServletContext()
					.getRealPath("/");
			String basepath = basepath1 + "logimages" + File.separator;
			/*
			 * if(!basepath.endsWith("\\")){ basepath += File.separator; }
			 */
			String uploadpath = "L"
					+ new SimpleDateFormat("yyyyMMdd").format(new Date())
					+ File.separator;
			String tempfilename = uploadFileNamePre + "_" + IdGen.uuid()
					+ uploadFileNameSuf;
			image = uploadpath + tempfilename;
			try {
				boolean isSuccess = SimpleUpload.uploadByteFile(
						multipartFile.getInputStream(), basepath + uploadpath,
						tempfilename);
				if (isSuccess) {
					System.out.println("日志图片上传成功");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int userid = Integer.parseInt(multipartRequest.getParameter("userid"));
		String content = multipartRequest.getParameter("content");
		// String image = multipartRequest.getParameter("image");
		String lgt = multipartRequest.getParameter("longitude");
		String lat = multipartRequest.getParameter("latitude");
		String addr = multipartRequest.getParameter("address");
		// String costtime = request.getParameter("costtime");

		request.setAttribute("userid", userid);// 用于给拦截器传递参数

		Journal journal = new Journal();
		journal.setWriter(userid);
		journal.setContent(content);
		journal.setImage(image);
		journal.setLgt(lgt);
		journal.setLat(lat);
		journal.setAddr(addr);
		// journal.setCosttime(costtime);
		journal.setCreatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		journalService.save(journal);

		JSONObject json1 = new JSONObject();
		json1.put("image", image);

		json.put("success", true);
		json.put("data", json1);

		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getJournalList.action")
	public void getJournalList(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		JSONArray arr = new JSONArray();
		int resultCode = 0;
		int pagenum = Integer.parseInt(request.getParameter("pagenum"));
		int pagesize = Integer.parseInt(request.getParameter("pagesize"));
		Page<Object[]> page = new Page<Object[]>(pagesize);
		page.setPageNo(pagenum);
		Map<String, Object> parammap = new HashMap<String, Object>();
		parammap.put("userid", request.getParameter("userid"));
		parammap.put("getuserid", request.getParameter("getuserid"));
		parammap.put("date", request.getParameter("date"));
		List<JournalForm> list = journalService.getJournalList(page, parammap);
		if (list.size() > 0) {
			arr.addAll(list);
			json1.put("total", page.getTotalCount());
			json1.put("records", arr);
		}
		json.put("data", json1);
		json.put("success", true);
		log.info("获取日志：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/replyJournal.action")
	public void replyJournal(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		int journalid = Integer.parseInt(request.getParameter("replyid"));
		String content = request.getParameter("content");
		String replytostr = request.getParameter("replyto");
		int replyto = Integer.parseInt(StringUtils.isBlank(replytostr) ? "0"
				: replytostr);

		JournalReply journalReply = new JournalReply();
		journalReply.setJournal(journalService.get(journalid));
		journalReply.setRecontent(content);
		journalReply.setRedate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		journalReply.setReplyer(userid);
		journalReply.setReplyto(replyto);
		journalReplyService.save(journalReply);

		json.put("success", true);
		json.put("data", new JSONObject());

		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/sendPosition.action")
	public void sendPosition(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String longitude = request.getParameter("longitude");
		String latitude = request.getParameter("latitude");
		String address = request.getParameter("address");
		String forcesend = request.getParameter("forcesend");
		Position p = new Position();
		p.setSender(userid);
		p.setLgt(longitude);
		p.setLat(latitude);
		p.setAddress(address);
		p.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		if (forcesend == null || forcesend.equals("0")) {// 自动上传
			Attend ad = attendService.getLastAttend(userid);
			if (ad != null && ad.getStatus() == 0) {// 自动上传，仅仅上班时才保存定位信息
				json1.put("type", 1);
				positionService.save(p);
			} else {
				json1.put("type", 2);
			}
		} else {
			positionService.save(p);
		}
		json.put("success", true);
		json.put("data", json1);

		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getLastPosition.action")
	public void getLastPosition(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		JSONArray arr = new JSONArray();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String getuserids = request.getParameter("getuserid");
		List<PositionForm> list = positionService.getLastPosition(userid,
				getuserids);
		if (list.size() > 0) {
			arr.addAll(list);
			json1.put("records", arr);
		}
		json.put("data", json1);
		json.put("success", true);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getTrack.action")
	public void getTrack(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		JSONArray arr = new JSONArray();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String getuserids = request.getParameter("getuserid");
		String date = request.getParameter("date");
		List<PositionForm> list = positionService.getPositions(userid, date,
				getuserids);
		if (list.size() > 0) {
			arr.addAll(list);
			json1.put("records", arr);
		}
		json.put("data", json1);
		json.put("success", true);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getContacts.action")
	public void getContacts(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String signcert = request.getParameter("signcert");
		String signdata = request.getParameter("signdata");
		String lastupdated = request.getParameter("lastupdated");

		User user = userService.get(userid);
		Parameter parameter = new Parameter();
		parameter.put("p1", user.getUnit().getDivid());
		List<Dept> list = deptService
				.findBySql(
						"select * from mlog_dept a where a.parentid is null and a.divid=:p1",
						parameter, Dept.class);
		PropertyFilter filter = new PropertyFilter() {
			public boolean apply(Object source, String name, Object value) {
				if (source instanceof Dept) {
					if ("deptid".equals(name)) {
						return true;
					}
					if ("deptname".equals(name)) {
						return true;
					}
					if ("depts".equals(name)) {
						return true;
					}
					if ("contacts".equals(name)) {
						return true;
					}
				} else if (source instanceof User) {
					if ("userid".equals(name)) {
						return true;
					}
					if ("name".equals(name)) {
						return true;
					}
					if ("mobilephone".equals(name)) {
						return true;
					}
				}

				return false;
			}
		};
		User user1 = userService.get(userid);
		UnitSettings unitSettings = (UnitSettings) unitSettingsService.find(
				" from UnitSettings where skey='lastupdated' and divid=:p1",
				new Parameter(user1.getUnit().getDivid())).get(0);
		String value = unitSettings.getValue();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd+HH:mm:ss");// get方式请求，url进行了utf-8编码空格转换为+
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date lastUpdateTime = null;
		Date sysLastUpdateTime = null;
		boolean isUpdated = true;
		try {
			if (lastupdated == null) {
				isUpdated = true;
			} else {
				lastUpdateTime = sdf.parse(lastupdated);
				sysLastUpdateTime = sdf1.parse(value);
				isUpdated = lastUpdateTime.before(sysLastUpdateTime);
			}
		} catch (ParseException e) {
			json.put("success", false);
			json.put("msg", "获取通讯录最后更新时间异常");
			e.printStackTrace();
		}
		json.put("success", true);
		JSONObject json1 = new JSONObject();

		json1.put("modified", isUpdated);
		if (isUpdated) {
			json1.put("updatedTime", value);
			json1.put("depts", JSON.parseArray(JSON.toJSONString(list, filter,
					SerializerFeature.DisableCircularReferenceDetect)));
		}
		json.put("data", json1);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getAuthContacts.action")
	public void getAuthContacts(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		int type = Integer.parseInt(request.getParameter("type"));
		String userids = dataRulesService.getDataAuth(userid, type);
		if (userids != null) {
			json1.put("userids", userids);
		}
		json.put("data", json1);
		json.put("success", true);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/download.action")
	public void download(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		String filename = request.getParameter("filename");
		String basepath = "";
		if (filename.startsWith("M")) {// 消息
			SysSettings sysSettings = sysSettingsService.findUniqueBy("skey",
					"msgimages");
			// basepath = sysSettings.getValue();
			String basepath1 = request.getSession().getServletContext()
					.getRealPath("/");// 获取系统应用路径
			basepath = basepath1 + "msgimages" + File.separator;
		} else if (filename.startsWith("L")) {// 日志
			SysSettings sysSettings = sysSettingsService.findUniqueBy("skey",
					"logimages");
			// basepath = sysSettings.getValue();
			String basepath1 = request.getSession().getServletContext()
					.getRealPath("/");// 获取系统应用路径
			basepath = basepath1 + "logimages" + File.separator;
		} else if (filename.startsWith("U")) {//
			basepath = request.getRealPath("/usersettings") + File.separator;
		}
		/*
		 * if(!basepath.endsWith("\\")){ basepath += File.separator; }
		 */
		String filepath = basepath + filename;
		File file = new File(filepath);
		response.setCharacterEncoding("utf-8");
		response.setContentType(ContentType.FILE.getName());
		// response.setContentLength((int)file.length());
		response.setHeader("Result-Code", String.valueOf(resultCode));
		try {
			response.setHeader("Content-Disposition", "attachment;filename="
					+ URLEncoder.encode(filename, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			bos = new BufferedOutputStream(response.getOutputStream());
			byte[] bt = new byte[1024 * 8];
			int bytesRead = 0;
			while (-1 != (bytesRead = bis.read(bt, 0, bt.length))) {
				bos.write(bt);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
					bos = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bis != null) {
				try {
					bis.close();
					bis = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@RequestMapping("/checkUpdate.action")
	public void checkUpdate(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		JSONObject json1 = new JSONObject();
		int resultCode = 0;
		// int userid = Integer.parseInt(request.getParameter("userid"));
		String apkversion = request.getParameter("apkversion");
		String os = request.getParameter("OS");// 获取客户端操作系统
		App app = new App();
		if ("Android".equals(os) || "".equals(os) || os == null) {// 客户端为安卓
			app = appService.getLastVersion(0);
		} else if (os.equals("iOS"))// 客户端为苹果
		{
			app = appService.getLastVersion(1);
		}
		int result = 0;
		if (app != null) {
			String basepath = request.getScheme() + "://"
					+ request.getServerName() + ":" + request.getServerPort()
					+ request.getContextPath();
			String[] ver_a = apkversion.split("\\.");// 客户端版本
			String[] ver_b = app.getApkversion().split("\\.");
			for (int i = 0; i < ver_a.length; i++) {
				int a = Integer.parseInt(ver_a[i]);
				int b = Integer.parseInt(ver_b[i]);
				if (a > b) {// 客户端版本高于服务端发布最新版本
					result = 1;
					break;
				}
				if (a < b) {// 客户端版本低于服务端发布最新版本
					result = -1;
					break;
				}
				result = 0;
			}
			if (result == 0) {// 客户端版本与服务端发布版本相同
				if (app.getStatus() == 0) {// 正常
					json1.put("isupdate", 0);
				} else if (app.getStatus() == 1) {// 停用
					json1.put("isupdate", 0);
				} else if (app.getStatus() == 2) {// 回滚
					json1.put("isupdate", 1);
					json1.put("isforceupdate", 1);
					if ("Android".equals(os) || "".equals(os) || os == null) {
						json1.put(
								"downloadurl",
								basepath
										+ app.getDownloadurl().replace(
												File.separator, "/"));
					} else if ("iOS".equals(os)) {
						json1.put(
								"downloadurl",
								"itms-services://?action=download-manifest&amp;url=https://dn-hebca-kuaiban.qbox.me/kuaiban.plist");
					}
					json1.put("description", app.getDescription());
				}
			} else if (result == -1) {// 客户端版本低于服务端发布最新版本
				if (app.getStatus() == 0) {// 正常
					json1.put("isupdate", 1);
					json1.put("isforceupdate", app.getIsforceupdate());
					if ("Android".equals(os) || "".equals(os) || os == null) {
						json1.put(
								"downloadurl",
								basepath
										+ app.getDownloadurl().replace(
												File.separator, "/"));
					} else if ("iOS".equals(os)) {
						json1.put(
								"downloadurl",
								"itms-services://?action=download-manifest&amp;url=https://dn-hebca-kuaiban.qbox.me/kuaiban.plist");
					}
					json1.put("description", app.getDescription());
				} else if (app.getStatus() == 1) {// 停用
					json1.put("isupdate", 0);
				} else if (app.getStatus() == 2) {// 回滚
					json1.put("isupdate", 1);
					json1.put("isforceupdate", 1);
					app = appService.getLastNormalVersion();// 获取发布最新的正常版本进行回滚
					if ("Android".equals(os) || "".equals(os) || os == null) {
						json1.put(
								"downloadurl",
								basepath
										+ app.getDownloadurl().replace(
												File.separator, "/"));
					} else if ("iOS".equals(os)) {
						json1.put(
								"downloadurl",
								"itms-services://?action=download-manifest&amp;url=https://dn-hebca-kuaiban.qbox.me/kuaiban.plist");
					}
					json1.put("description", app.getDescription());
				}
			} else {
				json1.put("isupdate", 0);
			}
		} else {
			json1.put("isupdate", 0);
		}

		json.put("data", json1);
		json.put("success", true);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/bindpush.action")
	public void bindpush(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		String certcn = request.getParameter("certcn");
		String pushchannelid = request.getParameter("channelid");// baidu返回值
		String pushuserid = request.getParameter("userid");// baidu返回值
		System.out.println("调用bindpush" + ":" + certcn + ":" + pushchannelid
				+ ":" + pushuserid);
		User user = userService.getUserByScertcn(certcn);
		/*
		 * if(StringUtils.isBlank(user.getPushchannelid()) ||
		 * StringUtils.isBlank(user.getPushuserid())){
		 * user.setPushchannelid(pushchannelid); user.setPushuserid(pushuserid);
		 * json.put("success", true); json.put("data", new JSONObject()); }else
		 * { String str = user.getPushchannelid()+user.getPushuserid(); String
		 * str1 = pushchannelid+pushuserid; if(str.equals(str1)){
		 * user.setPushchannelid(pushchannelid); user.setPushuserid(pushuserid);
		 * json.put("success", true); json.put("data", new JSONObject()); }else
		 * { json.put("success", false); json.put("msg", "该证书已绑定其他设备"); } }
		 */
		user.setPushchannelid(pushchannelid);
		user.setPushuserid(pushuserid);
		userService.save(user);
		json.put("success", true);
		json.put("data", new JSONObject());
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/bugreport.action")
	public void bugreport(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		String devicetype = request.getParameter("devicetype");
		String platform = request.getParameter("platform");
		String phoneid = request.getParameter("phoneid");
		String packagename = request.getParameter("packagename");
		String packageversion = request.getParameter("packageversion");
		String exceptiontime = request.getParameter("exceptiontime");
		String stacktrace = request.getParameter("stacktrace");

		BugReport bug = new BugReport();
		bug.setDevicetype(devicetype);
		bug.setPlatform(platform);
		bug.setPhoneid(phoneid);
		bug.setPackagename(packagename);
		bug.setPackageversion(packageversion);
		bug.setExceptiontime(exceptiontime);
		bug.setStacktrace(stacktrace);
		bug.setUploadtime(DateUtil.dateToString(new Date(),
				"yyyy-MM-dd HH:mm:ss"));
		bugReportService.save(bug);

		json.put("success", true);
		json.put("data", new JSONObject());

		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/userconfig.action")
	public void userconfig(HttpServletRequest request,
			HttpServletResponse response) throws UnsupportedEncodingException {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		// boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		CommonsMultipartResolver resolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		resolver.setResolveLazily(true);
		MultipartHttpServletRequest multipartRequest = resolver
				.resolveMultipart(request);
		int userid = Integer.parseInt(multipartRequest.getParameter("userid"));
		String skey = multipartRequest.getParameter("key");
		String value = multipartRequest.getParameter("value");
		String description = multipartRequest.getParameter("description");

		MultipartFile multipartFile = multipartRequest.getFile("attachment");
		String attachment = "";
		if (multipartFile != null) {
			String filename = multipartFile.getOriginalFilename();
			int los = filename.lastIndexOf(".");
			multipartFile.getName();
			String uploadFileNamePre = filename.substring(0, los);
			String uploadFileNameSuf = filename.substring(los,
					filename.length());
			String basepath = multipartRequest.getRealPath("/usersettings")
					+ File.separator;
			String uploadpath = "U" + userid + File.separator;
			String tempfilename = uploadFileNamePre + "_" + IdGen.uuid()
					+ uploadFileNameSuf;
			attachment = uploadpath + tempfilename;
			try {
				boolean isSuccess = SimpleUpload.uploadByteFile(
						multipartFile.getInputStream(), basepath + uploadpath,
						tempfilename);
				if (isSuccess) {
					System.out.println("用户自定义文件上传成功");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Parameter parameter = new Parameter();
		parameter.put("p1", userid);
		parameter.put("p2", skey);
		List<UserSettings> list = userSettingsService
				.findBySql(
						"select * from mlog_user_settings a where a.userid=:p1 and a.skey=:p2",
						parameter, UserSettings.class);
		UserSettings us = null;
		if (list == null || list.size() <= 0) {
			us = new UserSettings();
			us.setUserid(userid);
			us.setSkey(skey);
			if (StringUtils.isBlank(attachment)) {
				us.setValue(value);
			} else {
				us.setValue(attachment);
			}
			us.setDescription(description);
			us.setCreatetime(DateUtil.dateToString(new Date(),
					"yyyy-MM-dd HH:mm:ss"));
		} else {
			us = list.get(0);
			if (StringUtils.isBlank(attachment)) {
				us.setValue(value);
			} else {
				us.setValue(attachment);
			}
			us.setDescription(description);
			us.setCreatetime(DateUtil.dateToString(new Date(),
					"yyyy-MM-dd HH:mm:ss"));
		}
		userSettingsService.save(us);
		JSONObject json1 = new JSONObject();
		json.put("success", true);
		json.put("data", json1);
		log.info("用户自定义：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	@RequestMapping("/getuserconfig.action")
	public void getuserconfig(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String skey = request.getParameter("key");
		Parameter parameter = new Parameter();
		parameter.put("p1", userid);
		parameter.put("p2", skey);
		List<UserSettings> list = userSettingsService
				.findBySql(
						"select * from mlog_user_settings a where a.userid=:p1 and a.skey=:p2",
						parameter, UserSettings.class);
		if (list.size() > 0) {
			UserSettings us = list.get(0);
			JSONObject json1 = new JSONObject();
			json1.put("value", us.getValue());
			json1.put("description", us.getDescription());
			json.put("data", json1);
			json.put("success", true);
		} else {
			json.put("msg", "用户无此配置");
			json.put("success", false);
		}
		log.info("获取用户配置：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	/**
	 * 注册管理员
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/regUnit.action")
	public void regUnit(HttpServletRequest request, HttpServletResponse response) {
		boolean isVerified = false;
		JSONObject json = new JSONObject();
		int resultCode = 0;
		String username = request.getParameter("username"); // 用户名
		String password = request.getParameter("password"); // 密码
		String divname = request.getParameter("divname"); // 企业名称
		String name = request.getParameter("name"); // 姓名
		String mobilephone = request.getParameter("mobilephone"); // 手机号
		String identitycard = request.getParameter("identitycard"); // 身份证号
		String scertcn = request.getParameter("scertcn"); // 已经存在的证书CN号
		String code = request.getParameter("code"); // 验证码
		// 先检测验证码是否正确，如果不正确则返回错误码，还没有进行检测
		if (StringUtils.isNotBlank(code) && StringUtils.isNotEmpty(code)) {
			// 系统当前时间
			Date currentDatetime = new Date();

			Verifycode verifycode = new Verifycode();
			verifycode.setMobile(mobilephone);
			verifycode.setVerifycode(code);
			List<Verifycode> VerifycodeList = verifycodeService
					.getVerifycodeList(verifycode);
			if (VerifycodeList != null && VerifycodeList.size() >= 1) {
				for (int i = 0; i < VerifycodeList.size(); i++) {
					Date checkDate = VerifycodeList.get(i).getCreatedate();
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(checkDate);
					// 验证码的有效时间 为3小时
					calendar.add(Calendar.HOUR, 3);
					if (calendar.getTime().after(currentDatetime)) {
						isVerified = true;
						break;
					}
				}
			}
			if (!isVerified) {
				isVerified = isEqualDateVerifyCode(code);
			}
			if (isVerified) { // 验证码验证通过,并且此用户名和单位不存在
				if (isUserExist(username)) {
					json.put("msg", "用户已经存在");
					json.put("success", false);
				} else if (isUnitExist(divname)) {
					json.put("msg", "单位已经存在");
					json.put("success", false);
				} else {
					RegUnitForm form = new RegUnitForm();
					form.setDivname(divname);
					form.setAddr(null);
					form.setLinkman(null);
					form.setCorporation(divname);
					form.setName(name);
					form.setMobilephone(mobilephone);
					form.setIdentitycard(identitycard);
					form.setUsername(username);
					form.setPassword(password);
					// 注册单位
					try {
						JSONObject json1 = unitService.initUnit1(form);
						if (!json1.getBooleanValue("success")) {
							json.put("msg", "注册单位失败");
							json.put("success", false);
						} else {
							Unit _unit = (Unit) unitService.find(
									"from Unit where divname=:p1",
									new Parameter(divname)).get(0);
							User _user = (User) userService
									.find("from User where username=:p1 and divid=:p2",
											new Parameter(username, _unit
													.getDivid())).get(0);
							if (scertcn == null || scertcn == "") {
								if (_unit.getAcceptnom() == null
										|| _unit.getAcceptnom().equals("")) {// 判断是否已注册证书，未注册则通过在线为单位管理员注册个人软证书
									Map<String, String> map = new HashMap<String, String>();
									map.put("projectid",
											MlogPM.get("online.projectid"));
									map.put("businesstype", "04");
									map.put("username", name);
									map.put("operatorname", name);
									map.put("operatorphone", mobilephone);
									map.put("divid", "个人");
									map.put("serialnumber", "");
									map.put("appopenmoney", "0");
									JSONObject json2 = HttpOnline.invoke(
											OnlineIType.CERTAPPLY, map);
									if (json2.getBooleanValue("success") == false) {
										json.put("msg", "注册个人证书失败");
										json.put("success", false);
									} else {
										_unit.setAcceptnom(json2.getJSONObject(
												"data").getString("acceptNo"));
										unitService.save(_unit);
									}
								}
								Map<String, String> map1 = new HashMap<String, String>();
								map1.put("acceptno", _unit.getAcceptnom());
								map1.put("columnnames", "certcn");
								JSONObject json3 = HttpOnline.invoke(
										OnlineIType.GETAPPLYDATA, map1);
								if (json3.getBooleanValue("success") == false) {
									json.put("msg", "获取个人证书注册信息失败");
									json.put("success", false);
								} else {
									scertcn = ((JSONObject) json3
											.getJSONObject("data")
											.getJSONArray("applyData").get(0))
											.getString("columnData");
								}
							}
							// 调用在线的接口，保存验证码信息
							saveCheckcodeLog("regUnit", "MobileController",
									_unit.getAcceptnom(), code,
									getRemoteIp(request));
							_user.setScertcn(scertcn);
							userService.save(_user);
							_unit.setAdminuser(scertcn);
							unitService.save(_unit);
							// 为单位管理员设置数据权限
							String sql = "INSERT into mlog_data_rules(userid,secutype,managerid) SELECT "
									+ _user.getId()
									+ ",1,"
									+ _user.getId()
									+ " from DUAL UNION SELECT "
									+ _user.getId()
									+ ",2,"
									+ _user.getId()
									+ " from DUAL UNION SELECT "
									+ _user.getId()
									+ ",3,"
									+ _user.getId()
									+ " from DUAL";
							dataRulesService.updateBySql(sql, null);

							if (_unit.getIsregistered() == null
									|| _unit.getIsregistered() == 0) {// 判断单位是否已注册，未注册则通过在线注册projectid项目下单位
								Map<String, String> map2 = new HashMap<String, String>();
								map2.put("projectid", _unit.getProjectid());
								map2.put("parentid", _unit.getParentid());
								map2.put("divid", _unit.getDivid());
								map2.put("divname", _unit.getDivname());
								map2.put("adminuser", scertcn);// name
																// 20115-05-15Horse_Little
								map2.put("ischeck", "0");
								// map2.put("adminuser", _unit.getAdminuser());
								// JSONObject json4 =
								// HttpOnline.invoke(OnlineIType.INSERTPROJECT,
								// map2);//需要审核
								JSONObject json4 = HttpOnline.invoke(
										OnlineIType.INITUNIT, map2);// 不需要审核
								if (json4.getBooleanValue("success") == false) {
									_unit.setIsregistered(0);
									unitService.save(_unit);
								} else {
									_unit.setIsregistered(1);
									_unit.setInitstep(0);
									unitService.save(_unit);
									// 单位注册时初始化单位设置参数
									List<UnitSettings> uslist = unitSettingsService
											.find("from UnitSettings where divid=:p1 ",
													new Parameter(_unit
															.getDivid()));
									List<SysSettings> list = sysSettingsService
											.find("from SysSettings where iscommon=0 ",
													null);
									if (uslist == null || uslist.size() <= 0) {
										for (SysSettings ss : list) {
											UnitSettings us = new UnitSettings();
											us.setDivid(_unit.getDivid());
											us.setName(ss.getName());
											us.setSkey(ss.getSkey());
											us.setValue(ss.getValue());
											unitSettingsService.save(us);
										}
									}

									String basepath = request.getScheme()
											+ "://" + request.getServerName()
											+ ":" + request.getServerPort()
											+ request.getContextPath();
									String onlineurl = MlogPM.get("online.url")
											+ OnlineIType.CERTINSTALLSOFT
													.getName();
								}
							} else {
								String basepath = request.getScheme() + "://"
										+ request.getServerName() + ":"
										+ request.getServerPort()
										+ request.getContextPath();
								String onlineurl = MlogPM.get("online.url")
										+ OnlineIType.CERTINSTALLSOFT.getName();
							}
							JSONObject json5 = new JSONObject();
							json5.put("divid", _unit.getDivid());
							json5.put("acceptno", _unit.getAcceptnom());
							json.put("success", true);
							json.put("data", json5);
						}
					} catch (SQLException e) {
						e.printStackTrace();
						json.put("msg", "注册单位失败");
						json.put("success", false);
					}
				}
			} else { // 验证码验证未通过
				json.put("msg", "验证码失效或输入错误");
				json.put("success", false);
			}
		} else {
			json.put("msg", "验证码失效");
			json.put("success", false);
		}

		log.info("获取用户配置：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	/**
	 * 获取验证码
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/getVerifyCode.action")
	public void getVerifyCode(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		String mobilephone = request.getParameter("mobilephone");// 手机号
		String imei = request.getParameter("imei"); // 设备串号
		String randomCode = VerifyCodeUtil2.getRandomVerifyCode();
		if (randomCode != null && randomCode.trim().length() >= 1) {
			// randomCode不为空的时候
			Verifycode verifycode = new Verifycode();
			verifycode.setMobile(mobilephone);
			verifycode.setVerifycode(randomCode);
			verifycode.setCreatedate(new Date());
			try {
				verifycodeService.save(verifycode);
			} catch (Exception e) {
				log.debug("：verifycodeService：保存失败11111111111");
				e.printStackTrace();
			}

			// 组合消息并发送
			String content = MlogPM.get("sms.verifycode");
			SmsProvider smsProvider = SmsController.getSmsProvider();
			log.debug(smsProvider + "：smsProvider的值11111111111");
			ShortMessage msg = new ShortMessage();
			msg.setSenderName("河北腾翔");
			msg.setContent(content.replace("[XXX]", "[" + randomCode + "]"));
			msg.setReceiverNumber(mobilephone);
			try {
				smsProvider.sendMessageAsync(msg);
			} catch (SmsException e) {
				System.out.println(mobilephone + "：发送失败");
				log.debug(mobilephone + "：发送失败");
				e.printStackTrace();
			} catch (Exception e) {
				log.debug(mobilephone + "：发送失败11");
				e.printStackTrace();
			}
			// if(!smsProvider.sendMessageAsync(msg)){
			// System.out.println(mobilephone+"：发送失败");
			// log.info(mobilephone+"：发送失败");
			// };
			suc();
		} else {
			log.debug("：randomCode获取失败1111111111");
		}
	}

	/**
	 * 邀请用户,已经完成
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/inviteUser.action")
	public void inviteUser(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		CommonsMultipartResolver resolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		resolver.setResolveLazily(true);
		MultipartHttpServletRequest multipartRequest = resolver
				.resolveMultipart(request);
		// 获取人员信息
		String userid = request.getParameter("userid"); // 整型，邀请人自己的用户ID
		String manageid = dataRulesService.getManageids(Integer
				.parseInt(userid));// 获取邀请人的上级管理员ID
		String names = request.getParameter("names"); // 中文名称，多个以英文逗号区分
		String mobilephones = request.getParameter("mobilephones"); // 手机号，多个以英文逗号区分
		// 对每个人员进行相应的处理
		String[] manageidList = manageid.split(",");
		String[] nameList = names.split(",");
		String usernameList = "";// 用于存放姓名全拼
		String[] mobilephonesList = mobilephones.split(",");
		List<Integer> useridList = new ArrayList<Integer>();
		List<User> list = userService.findBySql(
				"select * from platform_t_user a where a.id=" + userid, null,
				User.class);
		// 从用户表中检测受邀用户（账号）姓名全拼是否已存在
		for (int i = 0; i < nameList.length; i++) {
			usernameList = usernameList + "'"
					+ PinyinUtil.getPinYin(nameList[i]) + "'";
			if (i + 1 != nameList.length) {
				usernameList = usernameList + ",";
			}
		}
		List<User> userlist = userService.findBySql(
				"select * from platform_t_user a where a.username in ("
						+ usernameList + ") and a.divid='"
						+ list.get(0).getUnit().getDivid() + "'", null,
				User.class);

		Map usrMap = new HashMap();
		if (userlist != null && list.size() >= 1 && list.get(0) != null) {
			for (int j = 0; j < userlist.size(); j++) {
				usrMap.put(userlist.get(j).getUsername(), 1);
			}
		}
		String deptid = list.get(0).getDept().getDeptid();// 获取邀请人的部门ID
		Integer roleid = list.get(0).getRole().getId();// 获取邀请人的角色ID
		for (int i = 0; i < mobilephonesList.length; i++) {
			String nameTemp = nameList[i];
			String mobilephoneTemp = mobilephonesList[i];
			String nametemp = nameList[i];
			List<User> userExist = userService.findBySql(
					"select * from platform_t_user a where a.name='" + nametemp
							+ "' and a.mobilephone=" + mobilephoneTemp
							+ "  and isdelete=0 and isenable=1 and a.divid='"
							+ list.get(0).getUnit().getDivid() + "'", null,
					User.class);
			if (userExist != null && userExist.size() >= 1
					&& userExist.get(0) != null
					&& userExist.get(0).getUnit() != null
					&& userExist.get(0).getUnit().getDivid() != null) {
				continue;
			} else {
				String password = Md5Encrypt.md5(Constant_sto.PASSWORD_DEFAULT);

				// 获取单位名称,为此用户开通快办
				if (list != null && list.size() >= 1 && list.get(0) != null
						&& list.get(0).getUnit() != null
						&& list.get(0).getUnit().getDivid() != null) {
					String divid = list.get(0).getUnit().getDivid();

					User user = new User();
					Unit unit = new Unit();
					unit.setDivid(divid);
					user.setDept(list.get(0).getDept());
					// user.setDeptid(deptid);
					user.setUnit(unit);
					user.setIsdelete(0);
					user.setIsenable(1);
					String usertempname = null;// 如果检测到重名用户，临时存放用户的账号（拼音+4位随机码）
					if (usrMap.get(PinyinUtil.getPinYin(nameList[i])) == null) {
						user.setUsername(PinyinUtil.getPinYin(nameTemp));// +VerifyCodeUtil2.getRandomVerifyCodeBySize(4)
					} else {
						usertempname = PinyinUtil.getPinYin(nameTemp)
								+ VerifyCodeUtil2.getRandomVerifyCodeBySize(4);
						user.setUsername(usertempname);//
					}
					user.setName(nameTemp);
					user.setPassword(password);
					user.setClientrole(0);
					user.setIdentitycard("130000000000000000");
					Role role = new Role();
					role.setId(5);
					user.setRole(role);
					user.setMobilephone(mobilephoneTemp);
					userService.save(user);
					if (user.getId() != null && user.getId() >= 1) {
						useridList.add(user.getId());
					}
					// end 获取单位名称,为此用户开通快办
					// 组合消息并发送
					String content = MlogPM.get("sms.content");
					// List<User> list =
					// userService.findBySql("select * from platform_t_user a where a.isdelete=0 and a.isenable=1 and (a.divid is not null or trim(a.divid)<>'') and id in ("+ids+") ",
					// null, User.class);
					SmsProvider smsProvider = SmsController.getSmsProvider();
					ShortMessage msg = new ShortMessage();
					msg.setSenderName("河北腾翔");
					if (usrMap.get(PinyinUtil.getPinYin(nameList[i])) == null) {
						msg.setContent(content
								.replace("账户名：xxx",
										"账户名：" + PinyinUtil.getPinYin(nameTemp))
								.replace("密码：******", "密码：123456")
								.replace(
										"单位ID：XXX",
										"单位ID："
												+ list.get(0).getUnit()
														.getDivid()));
					} else {
						msg.setContent(content
								.replace("账户名：xxx", "账户名：" + usertempname)
								.replace("密码：******", "密码：123456")
								.replace(
										"单位ID：XXX",
										"单位ID："
												+ list.get(0).getUnit()
														.getDivid()));
					}
					msg.setReceiverNumber(mobilephoneTemp);
					try {
						smsProvider.sendMessageAsync(msg);
					} catch (SmsException e) {
						System.out.println(mobilephoneTemp + "：发送失败");
						log.info(mobilephoneTemp + "：发送失败");
						e.printStackTrace();
					}
					// if(!smsProvider.sendMessage(msg)){
					// System.out.println(mobilephoneTemp+"：发送失败");
					// log.info(mobilephoneTemp+"：发送失败");
					// };
					suc();
				}
			}
		}
		// 为新加的这些用户分配权限，让单位管理员、部门主管可以查看这些新邀请的用户信息
		if (useridList != null && useridList.size() >= 1) {
			for (Integer val : useridList) {
				// 对自己进行授权
				String sql1 = " UNION SELECT " + val + ",1," + val
						+ " from DUAL UNION SELECT " + val + ",2," + val
						+ " from DUAL UNION SELECT " + val + ",3," + val
						+ " from DUAL";
				// 邀请人为管理员用户或单位主管的情况下，将继承该人所有权限
				if (roleid == 4 || roleid == 6) {
					String sql = "INSERT into mlog_data_rules(userid,secutype,managerid) select "
							+ val
							+ ",a.secutype,a.managerid from mlog_data_rules a where a.userid = "
							+ userid;
					dataRulesService.updateBySql(sql + sql1, null);
				} else {
					String sql = "INSERT into mlog_data_rules(userid,secutype,managerid) select "
							+ val
							+ ",a.secutype,a.managerid from mlog_data_rules a where a.userid = "
							+ userid + " and a.userid <> a.managerid";
					dataRulesService.updateBySql(sql + sql1, null);
				}

			}
		}
		// 修改通讯录更新时间
		unitSettingsService.lastupdatedSettings(list.get(0).getUnit()
				.getDivid());
	}

	/**
	 * 验证证书是否已经绑定
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/isCertBinded.action")
	public void isCertBinded(HttpServletRequest request,
			HttpServletResponse response) {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		// 获取人员信息
		String certcn = request.getParameter("certcn"); // 软证书CN号
		// 获取此软证书对应的记录
		List<Object> _unitList = unitService.find(
				"from Unit where scertcn=:p1", new Parameter(certcn));
		JSONObject json2 = new JSONObject();
		if (_unitList != null && _unitList.size() >= 1
				&& _unitList.get(0) != null) {
			// 如果此软证书对应的记录不为空，则表示已经绑定
			json2.put("iscertbinded", true);
		} else {
			json2.put("iscertbinded", false);
		}
		json.put("success", true);
		json.put("data", json2);
		log.info("获取用户配置：：" + json);
		super.writeJson(request, response, contentType, resultCode, json);
	}

	/**
	 * 根据用户的手机类型来获得安装包
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/getApp.action")
	public void getApp(HttpServletRequest request, HttpServletResponse response) {
		String userAgent = request.getHeader("user-agent");
		String mobileType = request.getParameter("mobileType");
		System.out
				.println("MlogPM.get(server.url)" + MlogPM.get("android.url"));
		String androidurl = MlogPM.get("android.url");
		String iosurl = MlogPM.get("ios.url");
		if (mobileType == null)
			mobileType = "";
		String ret = "";
		if (userAgent.contains("iPhone OS") || userAgent.contains("Mac OS")
				|| mobileType.contains("iOS")) { // 下载苹果
			/*
			 * ret =
			 * "<script script language='javascript' type='text/javascript'> window.location.href='itms-services://?action=download-manifest&amp;url=https://dn-hebca-kuaiban.qbox.me/kuaiban.plist'</script>"
			 * ;
			 */
			ret = "<script script language='javascript' type='text/javascript'> window.location.href='"
					+ iosurl + "'</script>";
		} else if (userAgent.contains("Android")
				|| mobileType.contains("Android")) { // 下载android
			App app = appService.getLastVersion(0);
			String downloadurl = app.getDownloadurl().replace("\\", "/");
			ret = "<script script language='javascript' type='text/javascript'> window.location.href='"
					+ androidurl + downloadurl + "'</script>";
			/*
			 * ret =
			 * "<script script language='javascript' type='text/javascript'> window.location.href='http://www.kuaiban365.com"
			 * +app.getDownloadurl()+"'</script>";
			 */
		}
		try {
			outputString(response, ret);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据用户的手机类型来获得安装包,动态生成二维码
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping("/getAppCode.action")
	public void getAppCode(HttpServletRequest request,
			HttpServletResponse response) {
		String userAgent = request.getHeader("user-agent");
		String mobileType = request.getParameter("type");
		String str = "";
		if (userAgent.contains("iPhone OS") || userAgent.contains("Mac OS")
				|| mobileType.contains("iOS")) { // 下载苹果
			str = MlogPM.get("ios.url");
		} else if (userAgent.contains("Android")
				|| mobileType.contains("Android")) { // 下载android
			App app = appService.getLastVersion(0);
			String downloadurl = app.getDownloadurl().replace("\\", "/");
			str = MlogPM.get("android.url") + downloadurl;
		}
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);

		response.setContentType("image/png");
		ServletOutputStream out = null;
		try {
			out = response.getOutputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Map<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
			hints.put(EncodeHintType.MARGIN, 0);
			BitMatrix matrix = new MultiFormatWriter().encode(str,
					BarcodeFormat.QR_CODE, 140, 140, hints);
			int width = matrix.getWidth();
			int height = matrix.getHeight();
			BufferedImage buffImg = new BufferedImage(width, height,
					BufferedImage.TYPE_BYTE_BINARY);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					buffImg.setRGB(x, y, matrix.get(x, y) ? 0xFF000000
							: 0xFFFFFFFF);
				}
			}
			try {
				ImageIO.write(buffImg, "png", out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		JSONObject json = new JSONObject();
		json.put("data", "aaa");
		json.put("success", true);
		Map<String, String> parammap = new HashMap<String, String>();
		parammap.put("aa", null);
		String aa = null;
		System.out.println(String.valueOf(parammap.get("aa")));
		try {
			System.out.println(URLDecoder.decode("\\U9648\\U51a0\\U5e0c",
					"utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	/************************** private function *************************/
	/**
	 * 检测用户是否已经存在
	 * 
	 * @param username
	 * @return
	 */
	private boolean isUserExist(String username) {
		boolean isUserExist = false;
		List<Object> _userList = userService.find(
				"from User where username=:p1 and role=6", new Parameter(
						username));
		if (_userList != null && _userList.size() >= 1) {
			isUserExist = true;
		}
		return isUserExist;
	}

	/**
	 * 检测单位是否已经存在
	 * 
	 * @param unitname
	 * @return
	 */
	private boolean isUnitExist(String divname) {
		boolean isUnitExist = false;
		List<Object> _unitList = unitService.find(
				"from Unit where divname=:p1", new Parameter(divname));
		if (_unitList != null && _unitList.size() >= 1) {
			isUnitExist = true;
		}
		return isUnitExist;
	}

	/**
	 * 检测验证码是否是当前日期的反向6位输出，如2015年6月16日，则今天的日期验证码是616051
	 * 
	 * @param verifycode
	 *            待检测的验证码
	 * @return
	 */
	private boolean isEqualDateVerifyCode(String verifycode) {
		boolean isEqualDateVerifyCode = false;
		String currentDateVerifyCode = "";
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
		String str = sdf.format(date);
		String[] list = str.split("-");
		for (int i = (list.length - 1); i >= 0; i--) {
			currentDateVerifyCode += list[i];
		}
		if (currentDateVerifyCode.equalsIgnoreCase(verifycode)) {
			isEqualDateVerifyCode = true;
		}
		return isEqualDateVerifyCode;
	}

	private void saveCheckcodeLog(String methodName, String actionName,
			String acceptno, String checkcode, String invokeip) {
		Map<String, String> map1 = new HashMap<String, String>();
		map1.put("acceptno", acceptno);
		map1.put("actionmethod", methodName);
		map1.put("actionname", actionName);
		map1.put("invokeip", invokeip);
		map1.put("checkcode", checkcode);
		map1.put("status", "1");
		HttpOnline.invoke(OnlineIType.SETCHECKCODE, map1);
	}

	private String getRemoteIp(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}

	private void outputString(HttpServletResponse response, String retInfo)
			throws IOException {
		response.setCharacterEncoding("UTF-8");
		PrintWriter printWriter = response.getWriter();
		printWriter.print(retInfo);
		printWriter.flush();
		printWriter.close();
	}

	@RequestMapping("/moveCar.action")
	public void moveCar(HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		JSONObject json = new JSONObject();
		int resultCode = 0;
		MultipartResolver resolver = new CommonsMultipartResolver(request
				.getSession().getServletContext());
		MultipartHttpServletRequest multipartRequest = resolver
				.resolveMultipart(request);
		MultipartFile multipartFile = multipartRequest.getFile("image");
		String image = "";
		if (multipartFile != null) {
			SysSettings sysSettings = sysSettingsService.findUniqueBy("skey",
					"logimages");
			String filename = multipartFile.getOriginalFilename();
			int los = filename.lastIndexOf(".");
			String uploadFileNamePre = filename.substring(0, los);
			String uploadFileNameSuf = filename.substring(los,
					filename.length());
			String basepath1 = request.getSession().getServletContext()
					.getRealPath("/");
			String basepath = basepath1 + "logimages" + File.separator;
			String uploadpath = "L"
					+ new SimpleDateFormat("yyyyMMdd").format(new Date())
					+ File.separator;
			String tempfilename = uploadFileNamePre + "_" + IdGen.uuid()
					+ uploadFileNameSuf;
			image = uploadpath + tempfilename;
			try {
				boolean isSuccess = SimpleUpload.uploadByteFile(
						multipartFile.getInputStream(), basepath + uploadpath,
						tempfilename);
				if (isSuccess) {
					System.out.println("日志图片上传成功");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		int userid = Integer.parseInt(multipartRequest.getParameter("userid"));
		String content = multipartRequest.getParameter("content");
		String lgt = multipartRequest.getParameter("longitude");
		String lat = multipartRequest.getParameter("latitude");
		String addr = multipartRequest.getParameter("address");
		String ownerCarNo = multipartRequest.getParameter("ownercarno");
		request.setAttribute("userid", userid);// 用于给拦截器传递参数

		Journal journal = new Journal();
		journal.setWriter(userid);
		journal.setContent(content);
		journal.setImage(image);
		journal.setLgt(lgt);
		journal.setLat(lat);
		journal.setAddr(addr);
		journal.setOwnercarno(ownerCarNo);
		// journal.setCosttime(costtime);
		journal.setCreatetime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		journalService.save(journal);
		List<User> list = userService.findBySql(
				"select * from platform_t_user a where a.ownercarno = '"
						+ content + "' or a.id=" + userid, null, User.class);
		User ownerUser = null;
		User moveUser = null;
		if (list.size() == 1) {
			ownerUser = list.get(0);
			if (ownerUser.getOwnercarno() == null
					|| ownerUser.getOwnercarno().equals(""))
				ownerUser.setOwnercarno(ownerCarNo);
			userService.save(ownerUser);
			// 组合消息并发送
			content = MlogPM.get("sms.move");
			SmsProvider smsProvider = SmsController.getSmsProvider();
			ShortMessage msg = new ShortMessage();
			msg.setSenderName("河北腾翔");
			msg.setContent(content.replace("爱车******",
					"爱车" + ownerUser.getOwnercarno()));
			msg.setReceiverNumber(ownerUser.getMobilephone());
			try {
				smsProvider.sendMessageAsync(msg);
			} catch (SmsException e) {
				System.out.println(ownerUser.getMobilephone() + "：发送失败");
				log.info(ownerUser.getMobilephone() + "：发送失败");
				e.printStackTrace();
			}
			suc();
			JSONObject json1 = new JSONObject();
			json1.put("image", image);

			json.put("success", true);
			json.put("data", json1);

			super.writeJson(request, response, contentType, resultCode, json);
		}
		if (list.size() == 2) {

			for (User user : list)
				if (user.getUserid() == userid)
					ownerUser = user;
				else
					moveUser = user;
			if (ownerUser.getOwnercarno() == null
					|| ownerUser.getOwnercarno().equals(""))
				ownerUser.setOwnercarno(ownerCarNo);
			userService.save(ownerUser);
			// 组合消息并发送
			content = MlogPM.get("sms.move");
			SmsProvider smsProvider = SmsController.getSmsProvider();
			ShortMessage msg = new ShortMessage();
			msg.setSenderName("河北腾翔");
			msg.setContent(content.replace("爱车******",
					"爱车" + moveUser.getOwnercarno()));
			msg.setReceiverNumber(moveUser.getMobilephone());
			try {
				smsProvider.sendMessageAsync(msg);
			} catch (SmsException e) {
				System.out.println(moveUser.getMobilephone() + "：发送失败");
				log.info(moveUser.getMobilephone() + "：发送失败");
				e.printStackTrace();
			}
			suc();
			JSONObject json1 = new JSONObject();
			json1.put("image", image);

			json.put("success", true);
			json.put("data", json1);

			super.writeJson(request, response, contentType, resultCode, json);
		} else {
			System.out.println("号为：" + content + " 或userid= " + userid
					+ " 用户信息错误！找到记录数大于于2。");
			JSONObject json1 = new JSONObject();
			json.put("success", false);
			super.writeJson(request, response, contentType, resultCode, json);
			return;
		}
	}

	// 获取门禁列表
	@RequestMapping("/getDoorsList.action")
	public void getDoorsList(HttpServletRequest request,
			HttpServletResponse response) {
		log.debug("getDoorsList.action");
		JSONObject json = new JSONObject();
		JSONObject json2 = new JSONObject();
		JSONArray arr2 = new JSONArray();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		log.debug("userid" + ":" + userid);
		// 判断用户是否有手机开门权限 flag:true代表能开门，false代表不能开无权限
		boolean openflag = isMobileOpen(userid);
		if (openflag) {
			Date nowDate = new Date();
			DateFormat format = new SimpleDateFormat("HH:mm:ss");
			String time = format.format(nowDate);
			try {
				String sql = "select a.* from platform_t_accesscontrol a,mlog_door_rules b where a.doorstatus=1 and (a.regularflag=0 or (a.regularflag=1 and ('"
						+ time
						+ "' not BETWEEN a.regularstarttime and a.regularendtime))) and a.id=b.accesscontrolid and b.userid="
						+ userid;
				List<Door> list = doorService.findBySql(sql, null, Door.class);
				for (Door door : list) {
					JSONObject json3 = new JSONObject();
					json3.put("CID", door.getControllerid());
					json3.put("IID", door.getPortid());
					json3.put("Iname", door.getDoorname());
					json3.put("IState", door.getDoorstatus());
					arr2.add(json3);
				}
				json2.put("records", arr2);
				json.put("success", true);
				json.put("data", json2);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			json.put("success", false);
			//json.put("data", json2);
			
		}
		super.writeJson(request, response, contentType, resultCode,
				json);
	}

	@RequestMapping("/sendDoorOpen.action")
	public void sendDoorOpen(HttpServletRequest request,
			HttpServletResponse response) {
		log.debug("sendDoorOpen.action");
		// 考勤打卡
		JSONObject attendjson = new JSONObject();//
		JSONObject json1 = new JSONObject();
		int resultCode = 0;
		int userid = Integer.parseInt(request.getParameter("userid"));
		String lgt = request.getParameter("longitude");
		String lat = request.getParameter("latitude");
		String address = request.getParameter("address");
		// int type = Integer.parseInt(request.getParameter("type"));
		log.debug(userid + ":" + lgt + ":" + lat);
		Attend ad = null;
		Date date = new Date();

		ad = attendService.getLastAttend(userid);
		if (ad != null) {
			Date ondate = DateUtil.stringToDate(ad.getOntime(), "yyyy-MM-dd");// 上班卡日期
			Date newdate = DateUtil.dateToDate(date, "yyyy-MM-dd");// 当前日期
			if (ondate.before(newdate)) {
				if ((ad.getOfftime() == null || "".equals(ad.getOfftime()))) { // 隔天打卡
					ad.setOfflgt(lgt);
					ad.setOfflat(lat);
					ad.setOffaddr(address);

					User user = userService.get(userid);
					List<UnitSettings> list1 = unitSettingsService
							.find(" from UnitSettings where skey='closetime' and divid=:p1",
									new Parameter(user.getUnit().getDivid()));
					if (list1 != null && list1.size() > 0) {
						UnitSettings unitSettings = list1.get(0);
						ad.setOfftime(DateUtil.dateToString(ondate,
								"yyyy-MM-dd") + " " + unitSettings.getValue());
						ad.setLasttime(DateUtil.dateToString(ondate,
								"yyyy-MM-dd") + " " + unitSettings.getValue());
					} else {
						ad.setOfftime(DateUtil.dateToString(ondate,
								"yyyy-MM-dd") + " 23:59:59");
						ad.setLasttime(DateUtil.dateToString(ondate,
								"yyyy-MM-dd") + " 23:59:59");
					}
					ad.setStatus(1);

					attendService.save(ad);
					// 隔天打下班卡情况，添加打下班卡当天的上下班卡记录
					Attend extra_ad = new Attend();
					extra_ad.setUserid(userid);
					extra_ad.setOnlgt(lgt);
					extra_ad.setOnlat(lat);
					extra_ad.setOnaddr(address);
					extra_ad.setOntime(DateUtil.dateToString(date,
							"yyyy-MM-dd HH:mm:ss"));
					extra_ad.setStatus(0);

					extra_ad.setLasttime(DateUtil.dateToString(date,
							"yyyy-MM-dd HH:mm:ss"));
					attendService.save(extra_ad);
				} else {
					Attend adnew = new Attend();
					adnew.setUserid(userid);
					adnew.setOnlgt(lgt);
					adnew.setOnlat(lat);
					adnew.setOnaddr(address);
					adnew.setOntime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(date));
					adnew.setStatus(0);
					adnew.setLasttime(new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(date));
					User user = userService.get(userid);
					if (user.getUnit() == null) {
						json1.put("frequency", 10 * 60 * 1000);
					} else {
						List<UnitSettings> list1 = unitSettingsService
								.find(" from UnitSettings where skey='frequency' and divid=:p1",
										new Parameter(user.getUnit().getDivid()));
						if (list1 != null && list1.size() > 0) {
							UnitSettings unitSettings = list1.get(0);
							json1.put(
									"frequency",
									Integer.parseInt(unitSettings.getValue()) * 60 * 1000);
						} else {
							json1.put("frequency", 10 * 60 * 1000);
						}
					}
					attendService.save(adnew);
				}

			} else {

				if (ad.getOfftime() != null) {// 插入一整条记录，上班时间为上次打卡时间，下班时间为这次打卡时间
					Attend adnew = new Attend();
					adnew.setUserid(userid);
					adnew.setOnlgt(ad.getOfflgt());
					adnew.setOnlat(ad.getOfflat());
					adnew.setOnaddr(ad.getOffaddr());
					adnew.setOntime(ad.getOfftime());
					adnew.setOfflgt(lgt);
					adnew.setOfflat(lat);
					adnew.setOffaddr(address);
					adnew.setOfftime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(date));
					adnew.setStatus(1);
					adnew.setLasttime(new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss").format(date));
					User user = userService.get(userid);
					if (user.getUnit() == null) {
						json1.put("frequency", 10 * 60 * 1000);
					} else {
						List<UnitSettings> list1 = unitSettingsService
								.find(" from UnitSettings where skey='frequency' and divid=:p1",
										new Parameter(user.getUnit().getDivid()));
						if (list1 != null && list1.size() > 0) {
							UnitSettings unitSettings = list1.get(0);
							json1.put(
									"frequency",
									Integer.parseInt(unitSettings.getValue()) * 60 * 1000);
						} else {
							json1.put("frequency", 10 * 60 * 1000);
						}
					}
					attendService.save(adnew);
				} else {// 第二次打卡状态记录

					ad.setOfflgt(lgt);
					ad.setOfflat(lat);
					ad.setOffaddr(address);
					ad.setOfftime(DateUtil.dateToString(date,
							"yyyy-MM-dd HH:mm:ss"));
					ad.setStatus(1);
					ad.setLasttime(DateUtil.dateToString(date,
							"yyyy-MM-dd HH:mm:ss"));
					attendService.save(ad);
				}

			}
			// attendService.save(ad);
		} else {
			Attend adnew = new Attend();
			adnew.setUserid(userid);
			adnew.setOnlgt(lgt);
			adnew.setOnlat(lat);
			adnew.setOnaddr(address);
			adnew.setOntime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(date));
			adnew.setStatus(0);
			adnew.setLasttime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(date));
			User user = userService.get(userid);
			if (user.getUnit() == null) {
				json1.put("frequency", 10 * 60 * 1000);
			} else {
				List<UnitSettings> list1 = unitSettingsService
						.find(" from UnitSettings where skey='frequency' and divid=:p1",
								new Parameter(user.getUnit().getDivid()));
				if (list1 != null && list1.size() > 0) {
					UnitSettings unitSettings = list1.get(0);
					json1.put(
							"frequency",
							Integer.parseInt(unitSettings.getValue()) * 60 * 1000);
				} else {
					json1.put("frequency", 10 * 60 * 1000);
				}
			}
			attendService.save(adnew);
		}

		attendjson.put("success", true);
		attendjson.put("data", json1);// 考勤打卡结束

		// String userid = request.getParameter("userid");
		String cid = request.getParameter("CID");
		String iid = request.getParameter("IID");
		log.debug("userid" + ":" + userid);
		Date nowdate = new Date();
		// SimpleDateFormat dfs = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		// String nowdate1=dfs.format(nowdate);
		long between = 0;

		if (timeMap.get(cid + iid) == null) {
			boolean openflag = isMobileOpen(userid);
			if (openflag) {
				// 判断用户是否有开该门的权限
				boolean dooropen = isDoorOpen(userid, cid, iid);
				if (dooropen) {

					creatSocket(userid, cid, iid);
					// 接收到成功开门信息后，将开门流水插入数据库中
					String sql = "insert into platform_t_accessrecord(accesscontrolid,userid,recordtime,type,status) values("
							+ getDoorId(cid, iid)
							+ ","
							+ userid
							+ ",'"
							+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
									.format(nowdate) + "',2," + "1)";
					doorService.updateBySql(sql, null);
					timeMap.put(cid + iid, nowdate);
				}
			
			}
			
		} else {
			Date ondate = DateUtil.dateToDate((Date)timeMap.get(cid + iid)
					, "yyyy-MM-dd");// 上班卡日期
			Date newdate = DateUtil.dateToDate(nowdate, "yyyy-MM-dd");// 当前日期
	
			if (ondate.before(newdate)) {
				boolean openflag = isMobileOpen(userid);
				if (openflag) {
					// 判断用户是否有开该门的权限
					boolean dooropen = isDoorOpen(userid, cid, iid);
					if (dooropen) {

						creatSocket(userid, cid, iid);
						// 接收到成功开门信息后，将开门流水插入数据库中
						String sql = "insert into platform_t_accessrecord(accesscontrolid,userid,recordtime,type,status) values("
								+ getDoorId(cid, iid)
								+ ","
								+ userid
								+ ",'"
								+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
										.format(nowdate) + "',2," + "1)";
						doorService.updateBySql(sql, null);
						//timeMap.remove(cid+iid);
						timeMap.put(cid + iid, nowdate);
					}
				}
			} else {
				between = (nowdate.getTime() - timeMap.get(cid + iid).getTime()) / 1000;//判断时间差如果小于5秒，则不发送开门请求
				System.out.println("between"+between);
				if (between > 5) {
					//timeMap.put(cid + iid, new Date());
					// 判断用户是否有手机开门权限
					boolean openflag = isMobileOpen(userid);
					if (openflag) {
						// 判断用户是否有开该门的权限
						boolean dooropen = isDoorOpen(userid, cid, iid);
						if (dooropen) {

							creatSocket(userid, cid, iid);
							// 接收到成功开门信息后，将开门流水插入数据库中
							String sql = "insert into platform_t_accessrecord(accesscontrolid,userid,recordtime,type,status) values("
									+ getDoorId(cid, iid)
									+ ","
									+ userid
									+ ",'"
									+ new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss")
											.format(nowdate) + "',2," + "1)";
							doorService.updateBySql(sql, null);
							timeMap.put(cid + iid, nowdate);
						}
					}

				}
			}

		}
		super.writeJson(request, response, contentType, resultCode, attendjson);

	}

	/**
	 * 检测用户是否有手机开门权限，
	 * 
	 * @param userid
	 * @return
	 */
	private boolean isMobileOpen(int userid) {
		boolean i = false;
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String str = sdf.format(date);
		String sql = "select * from platform_t_user a WHERE a.isdelete=0 and a.mobileopenflag=1 and (('"
				+ str
				+ "' BETWEEN a.openstarttime and a.openendtime) or (a.openstarttime is null or a.openstarttime='' or a.openendtime is null or a.openendtime='' )) and id="
				+ userid;
		List<User> list = userService.findBySql(sql, null, User.class);
		if (list != null && list.size() == 1 && list.get(0) != null) {
			i = true;
		}
		return i;
	}

	/**
	 * 检测用户是否有开门权限，
	 * 
	 * @param userid
	 *            ，CID,IID
	 * @return
	 */
	private boolean isDoorOpen(int userid, String cid, String iid) {
		boolean i = false;
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String str = sdf.format(date);

		String sql = "select a.* from platform_t_accesscontrol a,mlog_door_rules b where a.doorstatus=1 and (a.regularflag=0 or (a.regularflag=1 and ('"
				+ str
				+ "' not BETWEEN a.regularstarttime and a.regularendtime))) and a.id=b.accesscontrolid and b.userid="
				+ userid
				+ "  and a.controllerid='"
				+ cid
				+ "' and a.portid='"
				+ iid + "'";
		List<Door> list = doorService.findBySql(sql, null, Door.class);
		if (list != null && list.size() == 1 && list.get(0) != null) {
			i = true;
		}
		return i;
	}

	/**
	 * 根据控制器ID和端口号得到门禁ID，
	 * 
	 * @param userid
	 *            ，CID,IID
	 * @return
	 */
	private int getDoorId(String cid, String iid) {
		int i = 0;

		String sql = "select * from platform_t_accesscontrol a where  a.controllerid='"
				+ cid + "' and a.portid='" + iid + "'";
		List<Door> list = userService.findBySql(sql, null, Door.class);
		if (list != null && list.size() == 1 && list.get(0) != null) {
			i = list.get(0).getId();
		}
		return i;
	}

	/**
	 * 创建socket通信，将开门流水保存至数据库
	 * 
	 * @param userid
	 *            ，CID,IID
	 * @return
	 */
	private void creatSocket(int userid, String cid, String iid) {
		// 创建socket 发送开门指令
		Socket socket = null;
		BufferedReader reader = null;
		PrintWriter wtr = null;
		try {
			// 1、创建客户端Socket，指定服务器端口号和地址
			socket = new Socket("localhost", 18888);
			System.out.println("服务端接口开门socket连接成功;");
			String opendoor = "{\"Version\":\"01\",\"Action\":\"0002\",\"Msg\":{\"CID\":\""
					+ cid + "\",\"IID\":\"" + iid + "\",\"Do\":1}}";
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			wtr = new PrintWriter(socket.getOutputStream());
			if (opendoor != null & opendoor.length() > 0) {
				wtr.println(opendoor);
				wtr.flush();

				System.out.println(opendoor + "发送完毕");
			}

			//wtr.close();
			//socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();

		}finally {
			wtr.close();
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
