
function ClientProvider(){
	
}

ClientProvider.prototype={
	//获取Client
	GetClient : null 
};

ClientProvider.SealDataName="seal";

ClientProvider.Alg3DES="3DES";
ClientProvider.AlgRC4="RC4";
ClientProvider.AlgSCB2="SCB2";
ClientProvider.AlgSSF33="SSF33";

ClientProvider.AlgOidSSF33="1.2.840.113549.3.255";
ClientProvider.AlgOidSCB2="1.2.840.113549.3.254";

function EncryptAlg()
{
	this.encryptAlgs=[
		{
			name:ClientProvider.Alg3DES,
			alg:6,
			isKeyEncrypt: false
		},
		{
			name:ClientProvider.AlgRC4,
			alg:7,
			isKeyEncrypt: false
		},
		{
			name:ClientProvider.AlgSCB2,
			alg:11,
			isKeyEncrypt: true
		},
		{
			name:ClientProvider.AlgSSF33,
			alg:10,
			isKeyEncrypt: true
		}
	]
}

EncryptAlg.prototype={
	GetAlg:function(name){
		for(var i=0;i<this.encryptAlgs.length;i++)
		{
			if(this.encryptAlgs[i].name==name)
				return this.encryptAlgs[i];
		}
		return null;
	}
};

function CspConfig(){
	this.cspConfig=
		[
			{ 
				name: "HaiTai Cryptographic Service Provider",
				p11DeviceName:"河北CA数字证书设备-HT",
				keyEncryptAlgs:[
					{
						name: ClientProvider.AlgSCB2,
						oid: ClientProvider.AlgOidSCB2,
						id: 0x66FE
					},
					{
						name: ClientProvider.AlgSSF33,
						oid: ClientProvider.AlgOidSSF33,
						id: 0x66FF
					}
				]
			}
			,
			{
				name: "Watchdata HBCA CSP v3.3",
				p11DeviceName:"握奇Key",
				keyEncryptAlgs:[
					{
						name: ClientProvider.AlgSCB2,
						oid: ClientProvider.AlgOidSCB2,
						id: 0x6622
					},
					{
						name: ClientProvider.AlgSSF33,
						oid: ClientProvider.AlgOidSSF33,
						id: 0x6621
					}
				]
			}
			,
			{
				name: "EnterSafe ePass3000GM CSP v1.0",
				p11DeviceName:"飞天SM2Key",
				keyEncryptAlgs:[]
			}
		];
}

CspConfig.prototype={
	GetCspNames: function(){
		var ret="";
		for(var i=0;i<this.cspConfig.length;i++)
		{
			ret+=this.cspConfig[i].name;
			if(i!=this.cspConfig.length)
				ret+=";";
		}
		return ret;
	},
	
	GetKeyEncryptAlg:function(cspName,algName){
		for(var i=0;i<this.cspConfig.length;i++)
		{
			var csp=this.cspConfig[i];
			
			if(csp.name==cspName)
			{
				for(var j=0;j<csp.keyEncryptAlgs.length;j++)
				{
					if(csp.keyEncryptAlgs[j].name==algName)
					{
						return csp.keyEncryptAlgs[j];
					}
				}
			}
			
		}
		return null;
	},
	
	GetCspConfigByCspName:function(cspName){
		for(var i=0;i<this.cspConfig.length;i++)
		{
			var csp=this.cspConfig[i];
			
			if(csp.name==cspName)
			{
				return csp;
			}
			
		}
		return null;
	},
	
	GetCspConfigByP11DeviceName:function(p11DeviceName){
		for(var i=0;i<this.cspConfig.length;i++)
		{
			var csp=this.cspConfig[i];
			
			if(csp.p11DeviceName==p11DeviceName)
			{
				return csp;
			}
			
		}
		return null;
	}
	
	
};

function IClient(){
	
}

IClient.prototype={
	
	GetName: function(){},
	
	//检查安装
	//返回值：bool，true代表检查通过，false代表检查不通过
	CheckInstall : function(){},
	
	//签名
	//参数source：签名原文
	//返回值：签名结果
	Sign:	function(source){},
	
	//获取签名证书
	//返回值：签名证书字符串
	GetSignCert : function(){},
	
	//获取加密证书
	//返回值：加密证书字符串
	GetCryptCert : function(){},
	
	//导入证书
	//返回值：bool, 是否导入成功
	ImportCert: function(){},
	
	//获取证书信息
	//参数isSignCert：true：签名证书，false：加密证书
	GetCertInfo : function(isSignCert){},
	
	
	//获取支持的国密加密算法类型
	GetCspConfig:function(){},
	
	//-----------------------------------以下为国密算法相关接口--------------------------
	
	//导入国密算法参数
	ImportKeyEncryptParam:function(){},
	
	
	
	//-----------------------------------以下为CA相关接口--------------------------
	//获取签章
	GetSeal : function(){}
};

function DN(){
	this.cn="";
	this.givenName="";
	this.org="";
	this.orgUnit="";
	this.telphone="";
	this.alias="";
	this.email="";
}

function CertInfo(){
	this.subject=new DN();
}

CertInfo.prototype.isPersonCert=function(){
	return this.subject.alias.length==15 || this.subject.alias.length==18;
}

CertInfo.prototype.isOrgCert=function(){
	return this.subject.alias.length==9 ;
}


var ClientUtil=new Object();

ClientUtil.ShowCheckInstallPanel=function(message){
	/*var tableobj=document.getElementById('ccr');
	var Row1=tableobj.insertRow(tableobj.rows.length);
	var Rows=tableobj.rows;
	var Cell1=Rows(Row1.rowIndex).insertCell();
	Cell1.align="left"; 
	
	var warningHTML="&nbsp;&nbsp;<img src='../images/netdisk-images/warn.gif' >&nbsp;<span class=\"errorMessage\">";
	warningHTML+="<font color='#ff0000'>"+message+"</a>";
	warningHTML+="</span>";
	Cell1.innerHTML=warningHTML;*/
	
	var warningHTML="<tr><td align='left'>&nbsp;&nbsp;<img src='../images/netdisk-images/warn.gif' >&nbsp;<span class=\"errorMessage\">";
	warningHTML+="<font color='#ff0000'>"+message+"</a>";
	warningHTML+="</span></td></tr>";
	
	$("#ccr").html(warningHTML);
	
		
}

ClientUtil.CreateActiveXObject=function(name,errorMessage){
	var obj;
	try
	{	
		obj=new ActiveXObject(name);
	}
	catch(e)
	{
		throw new Error(errorMessage);	
	}
	
	if(obj==undefined || obj==null)
		throw new Error(errorMessage);
	
	return obj;
}
