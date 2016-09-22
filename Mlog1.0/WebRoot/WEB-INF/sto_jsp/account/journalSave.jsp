<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<%@ include file="/WEB-INF/sto_jsp/include/css.jsp"%>
	 <style type="text/css">
	 	#content{
	 		position:absolute; 
	 		z-index:1;
	 		zoom:1;
	 	}
		#amcontent{
			padding:5px;
			 width:350px;
			 height:150px;
			 border:1px solid #CCC;
		}
		#date_left{
			float:left;
		}
		#date_right{
			float:left;
		}
		.week{
			font-size:14px;
			color:white;
			font-family: 'Microsoft YaHei','微软雅黑','SimSun','宋体';
			padding-left:5px;
			padding-top:5px;
		}
		.day{
			font-size:36px;
			color:white;
			font-family: 'Microsoft YaHei','微软雅黑','SimSun','宋体';
			padding-left:5px;
		}
		.month{
			font-size:14px;
			color:white;
			font-family: 'Microsoft YaHei','微软雅黑','SimSun','宋体';
			padding-left:5px;
		}
		.apm{
			font-size:14px;
			color:#0080ff;
			font-family: 'Microsoft YaHei','微软雅黑','SimSun','宋体';
			font-weight:solid;
			padding-left:90px;
		}
		.time{
			font-size:32px;
			color:#0080ff;
			font-family: 'Microsoft YaHei','微软雅黑','SimSun','宋体';
			font-weight:solid;
			padding-left:31px;
			padding-bottom:25px;
		}
		
    </style>
    <%@ include file="/WEB-INF/sto_jsp/include/js.jsp"%>
    <script src="${pageContext.request.contextPath}/js/ServerClock.js"></script>
<script type="text/javascript">
/****图片预览操作11****/
function onUploadImgChange(sender){
    if( !sender.value.match( /.jpg|.gif|.png|.bmp/i ) ){
        alert('图片格式无效！');
        sender.select();
		document.execCommand("delete");
        return false;
    }
}
function onPreviewLoad(sender){
    autoSizePreview( sender, sender.offsetWidth, sender.offsetHeight );
}
function autoSizePreview( objPre, originalWidth, originalHeight ){
    var zoomParam = clacImgZoomParam( 300, 300, originalWidth, originalHeight );
    objPre.style.width = zoomParam.width + 'px';
    objPre.style.height = zoomParam.height + 'px';
    objPre.style.marginTop = zoomParam.top + 'px';
    objPre.style.marginLeft = zoomParam.left + 'px';
}
function clacImgZoomParam( maxWidth, maxHeight, width, height ){
    var param = { width:width, height:height, top:0, left:0 };
    if( width>maxWidth || height>maxHeight ){
        rateWidth = width / maxWidth;
        rateHeight = height / maxHeight;
        if( rateWidth > rateHeight ){
            param.width = maxWidth;
            param.height = height / rateWidth;
        }else{
            param.width = width / rateHeight;
            param.height = maxHeight;
        }
    }
    param.left = (maxWidth - param.width) / 2;
    param.top = (maxHeight - param.height) / 2;
    return param;
}
function checkData() {
	if (String($("#amcontent").val()).replace('\s','') == '') {
		alert('请填写日志内容');
		return false;
	}
	return true;
}

function save(){
	$('#btn').linkbutton('disable');
	$("#form").form('submit',{
		onSubmit: function(param) {
			//去掉此onSubmit,提交不执行。可能easyui的Bug
			var ischeck = checkData();
			if(ischeck == false){
				$('#btn').linkbutton('enable');
			}
			return ischeck;
		},
    	success:function(json){
    		$('#btn').linkbutton('enable');
    		var objFile=$("#image");
    		var jsonObj = eval('(' + json + ')');
			if (jsonObj.success) {
				form.reset();
				$("#amcontent").val('');
				 objFile[0].outerHTML=objFile[0].outerHTML.replace(/(value=\").+\"/i,"$1\"");//"<INPUT name=image class=\"easyui-validatebox validatebox-text\" id=image onchange=onUploadImgChange(this); type=file size=30 accept=jpg/* jQuery18306152910368652736=\"10\">"

			};
			sy.messagerShow({
				msg : jsonObj.msg,
				title : '提示'
			});
	      }
	}); 
}
$(function(){
	//$("#amcontent").focus(); //把焦点放在第一个文本框
	$("#jou_bg").css("width",$(document.body).width()-50);
	$("#jou_bg").css("height",$(document.body).height()-40);
	
	$("#amcontent").css("width",$(document.body).width()-70);
	$("#amcontent").css("height",$(document.body).height()-110);
	$("#main").css("height",$(document.body).height()-110);
	
	$.post("${ctx}/common/getServerDateStr.action",{},
		function(jsonarr){
			var srvClock = new ServerClock(jsonarr[0],jsonarr[1],jsonarr[2],jsonarr[3],jsonarr[4],jsonarr[5]);
			/* 时间格式化字符串 */
			var fmtStr = "服务器的时间是：<br/>yyyy年MM月dd日 HH:mm:ss E<br/>是该年的第D天<br/>是该年的第w周<br/>";
			var fmtStr1 = "MM-dd-EEE-HH:mm-a";
			/* 
			由于网络延时无法估计的原因，会有一定的误差。用户可以通过 set_delay() 方法来减少误差。
			 默认为1000，表示 1 秒。 
			*/
			srvClock.set_delay(3000); /* 时钟向后延时 3 秒 */
			
			/* 0.5秒刷新一次时间 */
			window.setInterval(function(){
					var date = srvClock.get_ServerTime(fmtStr1).split("-");
					var dateHtml="<div id='date_left'><span class='week'>"+date[2]+"</span><br/><span class='day'>"+date[1]+"</span><span class='month'>"+date[0]+"</span></div>"
						+"<div id='date_right'><span class='apm'></span><br/><span class='time'>"+date[3]+"</span></div>";
			     	document.getElementById("date").innerHTML = dateHtml;
			 },500);	
		}
	);
	
});
</script>
</head>
<body class="easyui-layout" data-options="fit:true">
<form id="form" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/journal/saveDo.action">
<input id="userid" name="userid" type="hidden" value="${user.id}" >
<input id="name" name="name" type="hidden" value="${user.name}" >
<input id="createtime" name="createtime" type="hidden" value="${createtime}">
<div data-options="region:'center',border:false" style="overflow: hidden;">
	<div align="top" style="position:fixed;z-index:2;width:100%;height:64px;left:30px;top:10px;overflow: hidden;">
			<div id="date" style="float:left;width:236px;height:64px;background:url('${ctx}/_static/images/jou_date.png');background-repeat:no-repeat;">
			</div>
			<div style="float:left;padding-top:35px;">
				<div style="padding-left:20px;float:left;"><span style="font-size: 14px;padding-left:5px;font-family: 'Microsoft YaHei','微软雅黑','SimSun','宋体';font-weight:solid;">填写人：${user.name}</span><br/><span><img src="${ctx}/_static/images/jou_line.png" width="150" height="8"></span></div>
				<div style="padding-left:10px;float:left;">
				    <span style="font-size: 14px;padding-left:5px;font-family: 'Microsoft YaHei','微软雅黑','SimSun','宋体';font-weight:solid;">上传图片：</span>
				    <input size="30" id="image"  type="file" name="image" class="easyui-validatebox" accept="jpg/*" onchange="onUploadImgChange(this);" />
				    </div>
				<div style="padding-left:150px;float:right;"> 
					<a id="btn" href="javascript:save();" class="easyui-linkbutton">保存日志</a></div>
			</div>
	</div>
	<div style="position:fixed;z-index:1;width:100%;height:100%;left:10px;top:20px;">
		<img id="jou_bg" src="${ctx}/_static/images/jou_bg.png">
	</div>
	<%-- <div style="position:fixed;z-index:2;width:100%;height:100%;left:20px;top:80px;">
		<img id="jou_main" src="${ctx}/_static/images/jou_main.png">
	</div> --%>
	<div id="main" style="position:fixed;z-index:3;width:100%;left:20px;top:80px;overflow:hidden">
		<table border="0" cellspacing="0" cellpadding="0" >
			<tr>
			    <td align="left"><div id="content" ><textarea rows="25" cols="100" id="amcontent" name="amcontent"  onfocus="if(value=='请在这里写入当天日志。。。') {value=''}">请在这里写入当天日志。。。</textarea></div></td>
			</tr>
			</tr>
		</table>
	</div>
</div>
</form>
</body>