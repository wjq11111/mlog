<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<div align="center" style="padding: 5px; overflow: hidden;">
	<form method="post">
		<input type="hidden" name="id" value="${o.id}" />
		<table class="tableForm">


			<tr>
				<td class="label">门禁卡号</td>
				<td><input id="cardno" name="cardno" autocomplete='off'
					style="width: 250px;" readonly value="${o.cardno}"></td>
			</tr>
			<tr>
				<td class="label">旧令牌</td>
				<td><input id="oldrandomcode" name="oldrandomcode"
					autocomplete='off' readonly style="width: 250px;"></td>
			</tr>
			 <tr>
				<td class="label">新令牌</td>
				<td><input id="newrandomcode" name="newrandomcode"
					autocomplete='off' readonly style="width: 250px;"></td>
				<td><input id="generator" name="generator" type="button"
					value="生成器" onclick="generatorradom()"></td>
			</tr> 
           
		</table>
	</form>
</div>
<script type="text/javascript">
 var myObject;
 var con;
 $(function() {
	 //alert("1111");
	 var cardno = '<%=request.getAttribute("cardno")%>';
	 myObject = new ActiveXObject("myActiveX.PcscActiveX")
	 //alert(myObject);
	 if(myObject != null)
		{
			var s = myObject.EstablishContext();
			//alert("suc");
			if (myObject != null)
		   	{
		   		con = myObject.ConnectToReader("ACS ACR122 0");
		   		//alert("s="+s);
		   		var uid=myObject.Transmit("FF CA 00 00 00", 5, 10);
		     	/*  uid = trim(uid);   
		   		alert(uid); */
		   		var newuid=uid.substr(uid,12).replace(/\s/g, "");
		   	   //alert(newuid.replace(/\s/g, ""));		   		
		   	 if(newuid!=cardno){
		   	  var r=confirm("门禁卡UID与绑定卡号不一致，是否要替换？")
		   	  if (r==false)
		   	    {
		   		  document.getElementById("cardno").value =cardno;
		   		  document.getElementById("oldrandomcode").value ='<%=request.getAttribute("oldrandomcode")%>';
		   		   document.getElementById("newrandomcode").value ='<%=request.getAttribute("newrandomcode")%>';
		   		  document.getElementById("generator").disabled=true; 
		   	    }
		   	  else
		   	    {
		   		  document.getElementById("cardno").value=newuid;
		   		  document.getElementById("oldrandomcode").value='<%=request.getAttribute("oldrandomcode")%>';
		   	    }
		   	}else{
		   		  document.getElementById("cardno").value =cardno;
		   		  document.getElementById("oldrandomcode").value ='<%=request.getAttribute("oldrandomcode")%>';
		   		  document.getElementById("newrandomcode").value ='<%=request.getAttribute("newrandomcode")%>';
		   		  document.getElementById("generator").disabled=true; 
		   	} 
		   		
		  }else {
		   		alert("Failed to Connect To Reader");
		   	}
			
		}
		else
		{
			alert("Failed to Establish Context");
		}
	
 });

 <%--$(function(){
 	alert("1111");
	var myObject;
	myObject = new ActiveXObject("myActiveX.PcscActiveX")
	if(myObject != null)
	{
		var s = myObject.EstablishContext();
		alert("suc");
		connectToReader();
		
	}
	else
	{
		alert("Failed to Establish Context");
	}
	 var cardno = '<%=request.getAttribute("cardno")%>';
	 var tt = '<%=request.getAttribute("tt")%>';
	 
	 var uid = '<%=request.getAttribute("uid")%>';
	 
	alert(uid);
	// alert(cardno);
	var uid=myObject.Transmit("FF CA 00 00 00", 5, 10);
	alert(uid);
	var newuid=uid.substr(uid.trim(),8);
	 if(newudi==cardno){
	  var r=confirm("门禁卡UID与绑定卡号不一致，是否要替换？")
	  if (r==false)
	    {
		  document.getElementById("cardno").value =cardno;
		  document.getElementById("oldrandomcode").value ='<%=request.getAttribute("oldrandomcode")%>';
		  document.getElementById("newrandomcode").value ='<%=request.getAttribute("newrandomcode")%>';
		  document.getElementById("generator").disabled=true;
	    }
	  else
	    {
		  document.getElementById("cardno").value=uid;
		  document.getElementById("oldrandomcode").value='<%=request.getAttribute("data")%>';
	    }
	}else{
		  document.getElementById("cardno").value =cardno;
		  document.getElementById("oldrandomcode").value ='<%=request.getAttribute("oldrandomcode")%>';
		  document.getElementById("newrandomcode").value ='<%=request.getAttribute("newrandomcode")%>';
		  document.getElementById("generator").disabled=true;
	} 
	  

	   function connectToReader()
	   {
	   	if (myObject != null)
	   	{
	   		//var d = document.getElementById("reader");
	   		var s = myObject.ConnectToReader("ACS ACR122 0");
	   		//d.outerText = s;
	   	}
	   	else
	   	{
	   		alert("Failed to Connect To Reader");
	   	}
	   }
	   function transmit(apdu,sendLen,recLen);
	   {
	   	//var apdu = form.textApdu.value;
	   	//var sendLen = form.textLength.value;
	   	//var recvLen = form.textReceiveLength.value;

	   	if (myObject != null)
	   	{
	   		//var d = document.getElementById("result");
	   		var s = myObject.Transmit(apdu, sendLen, recvLen);
	   		//d.outerText = s;
	   	}
	   	else
	   	{
	   		alert("Failed to Transmit APDU");
	   	}
	   }
	
}); --%>

function generatorradom(){

	    var s = [];

	    var hexDigits = "0123456789abcdef";

	 /*    for (var i = 0; i < 32; i++) {
           if(i%2==0){
        	  s[i] = " "+hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
           }else{
        	   s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
           }
	    } */
	    for (var i = 0; i < 8; i++) {
	           
	       s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
	          
		 }
	    var uuid = s.join("");
       // alert(uuid.toUpperCase( ));
	    document.getElementById("newrandomcode").value=uuid.toUpperCase();
	    //加载密钥
	    var s1 = myObject.Transmit("FF 82 00 00 06 FF FF FF FF FF FF", 11, 20);//加载卡的初始密钥
	   // alert(s1.substr(s1,6).replace(/\s/g, ""));
	    if(s1.substr(s1,6).replace(/\s/g, "")=='6300'){//6300：失败 9000：成功
	    	alert("初始化失败,请重新初始化卡！");
	    	$("#save").linkbutton('disable');
	    	return;
	    }
	    var t1=myObject.Transmit("FF 86 00 00 05 01 00 01 60 00", 10, 20);//认证01数据块
	    if(t1.substr(t1,6).replace(/\s/g, "")=='6300'){
	    	alert("初始化失败,请重新初始化卡！");
	    	
	    	$("#save").linkbutton('disable');
	    	return;
	    } 
	    var u1=myObject.Transmit("FF D6 00 01 10"+"db0003e1000000000000000000000000", 21, 10);//向01块写入数据db0003e1
	    if(u1.substr(u1,6).replace(/\s/g, "")=='6300'){
	    	alert("初始化失败,请重新初始化卡！");
	    	
	    	$("#save").linkbutton('disable');
	    	return;
	    } 
	    var t=myObject.Transmit("FF 86 00 00 05 01 00 04 60 00", 10, 20);//认证04数据块
	    if(t.substr(t,6).replace(/\s/g, "")=='6300'){
	    	alert("初始化失败,请重新初始化卡！");
	    	
	    	$("#save").linkbutton('disable');
	    	return;
	    }  
	    var u=myObject.Transmit("FF D6 00 04 10"+"0304"+uuid.toUpperCase()+"FE000000000000000000", 21, 10);//向01块写入动态验证码
	    if(u.substr(u,6).replace(/\s/g, "")=='6300'){
	    	alert("初始化失败,请重新初始化卡！");
	    	
	    	$("#save").linkbutton('disable');
	    	return;
	    } 
	    var t11=myObject.Transmit("FF 86 00 00 05 01 00 07 60 00", 10, 20);//认证07数据块
	    if(t11.substr(t11,6).replace(/\s/g, "")=='6300'){
	    	alert("初始化失败,请重新初始化卡！");
	    	
	    	$("#save").linkbutton('disable');
	    	return;
	    } 
	     var u12=myObject.Transmit("FF D6 00 07 10"+"d3f7d3f7d3f77f078840ffffffffffff", 21, 10);//向07块写入数据
	     if(u12.substr(u12,6).replace(/\s/g, "")=='6300'){
		    	alert("初始化失败,请重新初始化卡！");
		    	
		    	$("#save").linkbutton('disable');
		    	return;
		    }  
	    var t111=myObject.Transmit("FF 86 00 00 05 01 00 03 60 00", 10, 20);//认证03数据块
	    if(t111.substr(t111,6).replace(/\s/g, "")=='6300'){
	    	alert("初始化失败,请重新初始化卡！");
	    	
	    	$("#save").linkbutton('disable');
	    	return;
	    } 
	    var u11=myObject.Transmit("FF D6 00 03 10"+"a0a1a2a3a4a5796788c1ffffffffffff", 21, 10);//向03块写入数据
	    if(u11.substr(u11,6).replace(/\s/g, "")=='6300'){
	    	alert("初始化失败,请重新初始化卡！");
	    	$("#save").linkbutton('disable');
	    	return;
	    } 
	   
	   
}

</script>
