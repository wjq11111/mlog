
function installSoftCert(p12Cert){
 	/*var cp=new ClientProvider();
	var p11=cp.GetClient()._GetP11Mgr();
	*/
	var _client = new HebcaClient();
	p11 = _client._GetClientCtrl();
	
	var tempLicence = p11.Licence;
	p11.Licence="amViY5ZoZWKcZmhlnXxhaGViY2GXGmNjaGgNBwECCUsBDAxoZXE07M9gt05BXmTmKGBQb4ryg2Hy";
	try{
		p11.Util.OnlineInstallCspCert(p12Cert);
	}catch(e){
		throw e;
	}
	finally{
    	p11.Licence=tempLicence;
	}
}




function readCertInfo(flag)
{
	try
	{	
		var cp=new ClientProvider();
		var client=cp.GetClient();
		client._GetMailCtrl().Logout();
		
		var sCert = client._GetP11Mgr().SelectSignCert();
		var info = createCertInfo(sCert);
		var cn=info.subject.cn;
		var g=info.subject.givenName;
		var newSignCert = sCert.GetCertB64();
	 	var eCert = sCert.Device.GetEncryptCert(0);
	 	var newCryptCert = eCert.GetCertB64();
	 	
		if (newSignCert == null || newSignCert == "" || newCryptCert == null || newCryptCert == ""){
			alert("读取证书信息错误，请重试！");
			return false;
		}
		document.all('signCert').value= newSignCert;
		document.all('encryptCert').value= newCryptCert;
		/*if(info.isOrgCert()){
			document.getElementById('companyName').value=cn.length>g.length?g:cn;
			document.getElementById('companyName').readOnly=true;
			document.getElementById('showCompanyName').innerHTML=cn.length>g.length?g:cn;
			
			
		}else if(cn.length>g.length?g:cn!=null){	
			document.getElementById('companyName').value=cn.length>g.length?g:cn;
			document.getElementById('companyName').readOnly=true;
			document.getElementById('showCompanyName').innerHTML=cn.length>g.length?g:cn;
		}*/
		
		if("qy"==flag){
			document.getElementById('companyName').value=cn.length>g.length?g:cn;
			document.getElementById('companyName').readOnly=true;
			document.getElementById('showCompanyName').innerHTML=cn.length>g.length?g:cn;
			
			
		}else{	
			document.getElementById('contact').value=cn.length>g.length?g:cn;
			//document.getElementById('contact').readOnly=true;
			document.getElementById('showCompanyName').innerHTML=cn.length>g.length?g:cn;
		}
		
		
		
		if(!client.ImportCert())
		{
			alert("无法导入证书到浏览器");
			return false;
		}
		
		client.ImportKeyEncryptParam();
		document.getElementById('certReg').checked=true;
		
	}
	catch(e)
   	{
		if(e.message=="找不到证书对应的私钥")
   		{
   			alert("证书信息与当前USBkey不匹配，请插入正确的USBkey!");
   		}
   		else
   		{
   			//alert(e.message);
   		}
   		return false;
   	}	
}

//创建证书对象，并初始化
function createCertInfo(sCert){
 	var info=new CertInfo();
   	try{info.subject.givenName=sCert.GetSubjectItem("G");}catch(e){
   		info.subject.givenName=sCert.GetSubjectItem("CN");
   	}
   	try{info.subject.org=sCert.GetSubjectItem("O");}catch(e){}
   	try{info.subject.orgUnit=sCert.GetSubjectItem("OU");}catch(e){}
   	try{info.subject.telphone=sCert.GetSubjectItem("Phone");}catch(e){}
   	try{info.subject.alias=sCert.GetSubjectItem("Alias");}catch(e){}
   	try{info.subject.email=sCert.GetSubjectItem("E");}catch(e){}
   	try{info.subject.cn=sCert.GetSubjectItem("CN");}catch(e){}
   	return info;	
}



function trim(str) {
	if (str == null) return null;
	return str.replace(/^\s*|\s*$/g,"");
}

function useCert(obj) {
	if (obj.checked){
		window.location.reload();
	}else{
		document.getElementById('showCompanyName').innerHTML="";
		document.getElementById('companyName').readOnly=false;
		document.getElementById('signCert').value="";
		document.getElementById('encryptCert').value="";
	}
}




//改变域名
function changeDomain() {  
	$('#domainShow').hide();
	$('#domain').show();
	 
}

function getVerifyCode(op) {  
	jQuery.ajax({
		type:"POST",
		url:"../registe/sendMoblieVerifyCode.action?op="+op,
		data:{mobile:$('#mobile').val()}, 
		success:function (msg) {
				if(msg.success){
					setTime();
					alert("验证码已发送到您的手机，请查收");
				}else{
					alert(msg.msg);
				}
		},error:function () {
			alert(errInfo2);
		}
	});
	 
}

var countdown=60; 
function setTime() { 
	if (countdown == 0) { 
		$('#verifyMiao').hide();
		$('#showVerifyCode').show();
		countdown = 60; 
	} else { 
		$('#showVerifyCode').hide();
		$('#verifyMiao').show();
		$('#verifyMiao').html("重新发送(" + countdown + ")");
		countdown--; 
		setTimeout(function() { 
			setTime();
		},1000);
	} 
	
} 

