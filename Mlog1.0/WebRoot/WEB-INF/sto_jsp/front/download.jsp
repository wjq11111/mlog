<%@ page contentType="text/html; charset=UTF-8" session="false" %>
<%@ page import="org.apache.shiro.*,sto.common.exception.*,org.apache.shiro.authc.AuthenticationException,java.lang.*"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>快办365</title>
<%@ include file="/WEB-INF/sto_jsp/include/css.jsp"%>
<link href="${ctx}/_static/css/front/main.css" rel="stylesheet" type="text/css" />
<link href="${ctx}/_static/css/focus/lrtk.css" rel="stylesheet" type="text/css" />
<style type="text/css">
#item{width:200px;height:4350px;padding-top:10px;background-color:#eeeeee;)}
#item ul li{width:200px;font-size:18px;font-weight:bold;float:left; text-align:center;}
#item ul li a{font-family: "Microsoft YaHei","微软雅黑","SimSun","宋体";display:block; text-decoration:none; width:200px;height:30px;color:#3577b3;}
#item ul li a:hover{color:#FFFFFF;background-color:#dd0000;}
</style>
<link rel="shortcut icon" href="${ctx}/_static/images/front/favicon.ico" type="image/x-icon" />
<%@ include file="/WEB-INF/sto_jsp/include/js.jsp"%>
<script type="text/javascript" src="${ctx}/_static/js/focus/lrtk.js"></script>
<script type="text/javascript" src="${ctx}/js/login1.js?version=20130427&r=<%=Math.random()%>"></script>
<script type="text/javascript">
function help(flag){
	if(flag == 1){
		$("#pc").css('display','block');
		$("#mobile").css('display','none');
	}
	if(flag == 2){
		$("#pc").css('display','none');
		$("#mobile").css('display','block');
	}
	
}
</script>
</head>
<body>

<table width="1015" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr>
    <td><img src="${ctx}/includes/images/download_1.jpg" width="1015" height="201" alt=""></td>
  </tr>
</table>
<table width="1015" border="0" align="center" cellpadding="0" cellspacing="0">
  <tr >
     <td bordercolor="#ffffff" height="10"></td>
  </tr>
  <tr>
      <td align="right"><a href='"${ctx}/front/help.action"'>常见问题</a></td>
  </tr>
  <tr>
    <td ><img src="${ctx}/includes/images/downloadbut_1.jpg" width="150"  heigth="80"></td>
  </tr>
 </table>
<table width="1015" align="center" bordercolor="#C1C1C1" border="0" style="border-top-style: ">
	<tr>
		<td align="left" width="50%">
		    &nbsp;&nbsp;<strong>软件名称：</strong>
		    <font color="#215292" > <strong>软证书数字证书助手</strong></a></font>
		</td>
		<td  align="right" width="50%" colsan="6">
			<a href='http://www.kuaiban/download/hebca.exe'><img src="${ctx}/includes/images/download_2.jpg" width="100"  heigth="50"></a>
			<%-- <a href='http://192.168.15.13:8080/Mlog1.0/download/hebca.exe'><img src="${ctx}/includes/images/download_2.jpg" width="100"  heigth="50"></a> --%>
		</td>
	</tr>
	<tr>
		<td colspan="5">
			<table width="100%" border="0" bordercolor="#C1C1C1"
			cellpadding="0" cellspacing="0" style="border-top-style: ">
				<tr>
					<td bordercolor="#ffffff" height="5"></td>
				</tr>
				<tr>
					<td  height="40"valign="top">
						&nbsp;
						<font color="#215292"> 软件说明：</font>
						&nbsp;&nbsp;用于支持软证书使用的助手 
					</td>
					<td  align="right"    width="50%">
						上传日期：2015-06-26
					</td>
				</tr>
			</table>
		</td>
									
									
	</tr>
	<tr>
	    <td colspan="7" height="30" background="${ctx}/includes/images/downloadline_2.jpg"></td>
	</tr>
</table>

<table width="1015" align="center" bordercolor="#C1C1C1" border="0" style="border-top-style: ">
	<tr>
		<td align="left" width="50%">
		    &nbsp;&nbsp;<strong>软件名称：</strong>
		    <font color="#215292"  ><strong>数字证书助手</strong></a></font>
		</td>
		<td  align="right" width="50%" colsan="6">
			<a href='http://www.hebca.com/UserFiles/HebcaHelper5.0.8.exe'><img src="${ctx}/includes/images/download_2.jpg" width="100"  heigth="50"></a>
		</td>
	</tr>
	<tr>
		<td colspan="5">
			<table width="100%" border="0" bordercolor="#C1C1C1"
			cellpadding="0" cellspacing="0" style="border-top-style: ">
				<tr>
					<td bordercolor="#ffffff" height="5"></td>
				</tr>
				<tr>
					<td  height="40"valign="top">
						&nbsp;
						<font color="#215292"> 软件说明：</font>
						&nbsp;&nbsp;用于支持软证书、USB证书使用的数字证书助手 
					</td>
					<td  align="right"    width="50%">
						上传日期：2015-06-26
					</td>
				</tr>
			</table>
		</td>
									
									
	</tr>
	<tr>
	    <td colspan="7" height="30" background="${ctx}/includes/images/downloadline_2.jpg"></td>
	</tr>
</table>



</body>
</html>
