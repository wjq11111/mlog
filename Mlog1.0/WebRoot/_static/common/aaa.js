//定义变量
var qqcode_dg_cfg; //配置对象
var qqcode_root = qqcode_dg_cfg.root;//网址前缀


var tOut = -1;
var drag = false;
var g_safeNode = null;
lastScrollY = 0;
var kfguin=qqcode_dg_cfg.qq;

var companyname=unescape(qqcode_dg_cfg.cname);
var welcomeword=unescape(qqcode_dg_cfg.msg);
var wpadomain=qqcode_getdomain(qqcode_dg_cfg.root);

var qq_type=1;

var qqcode_dgtime=qqcode_dg_cfg.time;


var Browser = {
	ie:/msie/.test(window.navigator.userAgent.toLowerCase()),
	moz:/gecko/.test(window.navigator.userAgent.toLowerCase()),
	opera:/opera/.test(window.navigator.userAgent.toLowerCase()),
	safari:/safari/.test(window.navigator.userAgent.toLowerCase())
};


if(kfguin)
{
	
  if(qqcode_dg_cfg.side=='left')
  {
  	 _qqcode_rightDivHtml = '<div id="_qqcode_rightDiv" style="position:absolute; top:'+qqcode_dg_cfg.topfix+'px; left:10px;width:96px;overflow:hidden;z-index:99;clear:both;">';
	 	 
  }else{
  	_qqcode_rightDivHtml = '<div id="_qqcode_rightDiv" style="position:absolute; top:'+qqcode_dg_cfg.topfix+'px; right:1px;width:96px;overflow:hidden;z-index:99;clear:both;">';
  }

  _qqcode_rightDivHtml += kf_getPopup_qqcode_rightDivHtml(qqcode_dg_cfg,wpadomain);
 
  _qqcode_rightDivHtml += '</div>';
	

  document.write(_qqcode_rightDivHtml);

 
  if(qq_type==1 && qqcode_getCookie('isshowed')==0)
  {
  	  companyname = companyname.substr(0,15);	   	  
      welcomeword = kf_processWelcomeword(welcomeword);
  	  
  	  kfguin = kf_getSafeHTML(kfguin);
  	  companyname = kf_getSafeHTML(companyname);
  	  
  	  welcomeword = welcomeword.replace(/<br>/g,'\r\n');
  	  welcomeword = kf_getSafeHTML(welcomeword);
  	  welcomeword = welcomeword.replace(/\r/g, "").replace(/\n/g, "<BR>");
    
  	  if(parseInt(qqcode_dgtime)>0)
	  {
	  	window.setTimeout("kf_sleepShow()",parseInt(qqcode_dgtime));
	  }else
	  {
	  	kf_sleepShow();
	  }
      
  }
  window.setTimeout("kf_moveWithScroll()",1);
  
}


function kf_getSafeHTML(s)
{
	var html = "";
	var safeNode = g_safeNode;
	if(!safeNode){
		safeNode = document.createElement("TEXTAREA");
	}
	if(safeNode){
		if(Browser.moz){
			safeNode.textContent = s;
		}
		else{
			safeNode.innerText = s;
		}
		html = safeNode.innerHTML;
		if(Browser.moz){
			safeNode.textContent = "";
		}
		else{
			safeNode.innerText = "";
		}
		g_safeNode = safeNode;
	}
	return html;
}


function kf_moveWithScroll() 
{ 
	 if(typeof window.pageYOffset != 'undefined') { 
        nowY = window.pageYOffset; 
     } 
     else if(typeof document.compatMode != 'undefined' && document.compatMode != 'BackCompat') { 
        nowY = document.documentElement.scrollTop; 
     } 
     else if(typeof document.body != 'undefined') { 
        nowY = document.body.scrollTop; 
     }  

		percent = .1*(nowY - lastScrollY);
		if(percent > 0) 
		{
			percent=Math.ceil(percent);
		} 
		else
		{
			percent=Math.floor(percent);
		}

	 document.getElementById("_qqcode_rightDiv").style.top = parseInt(document.getElementById("_qqcode_rightDiv").style.top) + percent+"px";
	 if(document.getElementById("kfpopupDiv"))
	 {
	 	document.getElementById("kfpopupDiv").style.top = parseInt(document.getElementById("kfpopupDiv").style.top) + percent+"px";
	 }
	 lastScrollY = lastScrollY + percent;
	 tOut = window.setTimeout("kf_moveWithScroll()",1);
}

//关闭侧边的浮动界面,此函数的代码可移植到风格二里面
function kf_hide() 
{

	if(tOut!=-1)
	{
		clearTimeout(tOut); 
		tOut=-1;
	}
	if(document.getElementById("_qqcode_rightDiv")!=null)
	{
		document.getElementById("_qqcode_rightDiv").style.visibility = "hidden";
		document.getElementById("_qqcode_rightDiv").style.display = "none";
	}

}

function kf_hidekfpopup()
{
	if(tOut!=-1)
	{
		clearTimeout(tOut);   
		tOut=-1;
	}
	qqcode_setCookie('isshowed', 1, 's10' , wpadomain);
	document.getElementById("kfpopupDiv").style.visibility = "hidden";
	document.getElementById("kfpopupDiv").style.display = "none";
	tOut=window.setTimeout("kf_moveWithScroll()",1);
}

function kf_getPopupDivHtml(tempdata_cfg,companyname,welcomeword, wpadomain)
{
	var qqcode_root_img=tempdata_cfg.root+'images/';
	var temp = '';
	temp += '<div style="float:left; height:150px; width:8px; background-image:url('+qqcode_root_img+'bg_1.gif);"></div>';
    temp += '<div style="font-family:Tahoma; text-align:left; float:left; height:150px; width:324px; background-image:url('+qqcode_root_img+'bg_2.gif); background-repeat:repeat-x;">';
    temp += '<div><h1 style="float:left; width:250px; height:28px; line-height:28px; padding:2px 20px 0 2px; margin:0 0 9px 0; font-size:14px; color:#FFFFFF; display:block; background:none; border:none; position:static;">'+companyname+'</h1><a href="#" onclick="kf_hidekfpopup();return false;" style="background-image: url('+qqcode_root_img+'close.gif); float:right; height:19px; width:42px;" onmouseover="javascript:this.style.backgroundPosition=\'bottom\'" onmouseout="javascript:this.style.backgroundPosition=\'top\'"></a></div>';
    temp += '<div style="height:83px; padding:0 0 0 2px; clear:both;">';
    temp += '<div style="background-image:url('+qqcode_root_img+'face.jpg); height:70px; width:70px; float:left;"></div>';
    temp += '<p style="font-family:Tahoma; font-size:12px; line-height:24px; width: 240px; margin:0px; padding: 0 0 0 12px; display:block; float:left; margin-top:2px; word-wrap:break-word;">'+welcomeword+'</p></div>';
    temp += '<div style="clear:both;">';
    temp += '<a onclick="kf_hidekfpopup();return false;" href="#" style="float:right; background-image:url('+qqcode_root_img+'btn_1.gif); margin:0 0 0 5px; padding:0px; border:0px; height:21px; width:69px;"></a>';
    temp += '<a onclick="kf_hidekfpopup();" href="http://wpa.qq.com/msgrd?v=3&uin='+tempdata_cfg.qq+'&site=qq&menu=yes" style="float:right; background-image:url('+qqcode_root_img+'btn_2.gif); margin:0; padding:0px; border:0px; height:21px; width:69px;" target="_blank"></a></div>';
    temp += '</div><div style="float:left; height:150px; width:8px; background-image:url('+qqcode_root_img+'bg_1.gif); background-position:right;"></div>';
	
    return temp;
}



function kf_getPopup_qqcode_rightDivHtml(tempdata_cfg,wpadomain)
{	

	var tempdata_root=tempdata_cfg.root;//网址前缀
	
	var tempdata_site_url=qqcode_getsiteurl(tempdata_cfg.root);
	var temp = "";

	
	temp +='<div id="_qqcode_rightDiv_head" style="padding:0;padding-right:7px;background:url('+tempdata_root+'images/qqcode_title.png) no-repeat right -30px;">';
	
	temp +='<div class="_qqcode_rightDiv_head_inner" style="padding:0;background:url('+tempdata_root+'images/qqcode_title.png)  no-repeat left top;height:30px;line-height:30px;padding-left:7px;font-size:14px;overflow:hidden;"></div>';
	temp +='</div>';
	
	temp +='<div id="_qqcode_rightDiv_content" style="zoom:1;padding:0;padding-right:2px;background:url('+tempdata_root+'images/qqcode_line.png) repeat-y right top;">';
	
	temp +='<div class="_qqcode_rightDiv_content_inner" style="padding:0;padding-left:2px;background:url('+tempdata_root+'images/qqcode_line.png) repeat-y left top;zoom:1;">';
	
	temp +='<div id="_qqcode_rightDiv_content_body" style="padding:10px;background:url('+tempdata_root+'images/qqcode_bg.png) repeat-x #fff left 1px;text-align:center;font-size:12px;">';
	

	temp +='<div style="width:70px;height:70px;display:block;margin-bottom:10px;background:url('+tempdata_root+'images/face.jpg) no-repeat;"></div>';
	temp += '<div style="display:block;zoom:1;"><a onclick="kf_hidekfpopup();" href="http://wpa.qq.com/msgrd?v=3&uin='+tempdata_cfg.qq+'&site=qq&menu=yes" style="background-image:url('+tempdata_root+'images/btn_2.gif); margin:0; padding:0px; border:0px; height:21px; width:69px;display:inline-block;" target="_blank"></a></div>';
	
	
	temp +='</div></div></div>';

	temp +='<div id="_qqcode_rightDiv_foot" style="padding:0;padding-left:7px;background:url('+tempdata_root+'images/qqcode_title.png) no-repeat left bottom">';
	temp +='<div class="_qqcode_rightDiv_foot_inner" style="padding:0;background:url('+tempdata_root+'images/qqcode_title.png)  no-repeat right -84px;height:33px;line-height:33px;padding-right:7px;font-size:12px;overflow:hidden;display:block;"><a href="'+tempdata_site_url+'" style="display:block;width:100%;height:20px;margin-top:10px;" target="_blank"></a></div>';
	temp +='</div>';

	
	return temp;
}



function kf_validateWelcomeword(word)
{
	var count = 0;
	
	for(var i=0;i<word.length;i++)
	{
		if(word.charAt(i)=='\n')
		{
			count++;
		}
		if(count>2)
		{
				return 2;
		}
	}
	if(word.length > 57+2*count)
	{
		return 1;
	}
	
	count = 0;
  var temp = word.indexOf('\n');
  while(temp!=-1)
  {
  	word = word.substr(temp+1); 
  	if(temp-1<=19) 
  	{
  		count += 19;
  	}
  	else if(temp-1<=38)
  	{
  		count += 38;
  	}
  	else if(temp-1<=57)
  	{
  		count += 57;
  	}
  	
  	temp = word.indexOf('\n');
  }
  count+=word.length;	
  if(count>57)
  {
  	return 3;
  }
  
  return 0;
}


function kf_processWelcomeword(word)
{
	word = word.substr(0,57+10);
	var result = '';
	var count = 0;	
	var temp = word.indexOf('<br>');
	
  while(count<57 && temp!=-1)
  {

  	if(temp<=19) 
  	{
  		count += 19;
  		if(count<=57)
  		{
  		   result += word.substr(0,temp+5);
  	  }
  	  else
  	  {
  	  	 result += word.substr(0,57-count>word.length?word.length:57-count);
  	  }
  	}
  	else if(temp<=38)
  	{
  		count += 38;
  		if(count<=57)
  		{
  		   result += word.substr(0,temp+5);
  	  }
  	  else
  	  {
  	  	 result += word.substr(0,57-count>word.length?word.length:57-count);
  	  }
  	}
  	else if(temp<=57)
  	{
  		count += 57;
  		if(count<=57)
  		{
  		   result += word.substr(0,temp+5);
  	  }
  	  else
  	  {
  	  	 result += word.substr(0,57-count>word.length?word.length:57-count);
  	  }
  	}
  	
  	word = word.substr(temp+5); 
  	temp = word.indexOf('<br>');
  }
  
  if(count<57)
  {
      result += word.substr(0,57-count>word.length?word.length:57-count);
  }
  
  return result;
}


/*function kf_setCookie(name, value, exp, path, domain)
{
	var nv = name + "=" + escape(value) + ";";

	var d = null;
	if(typeof(exp) == "object")
	{
		d = exp;
	}
	else if(typeof(exp) == "number")
	{
		d = new Date();
		d = new Date(d.getFullYear(), d.getMonth(), d.getDate(), d.getHours(), d.getMinutes() + exp, d.getSeconds(), d.getMilliseconds());
	}
	
	if(d)
	{
		nv += "expires=" + d.toGMTString() + ";";
	}
	
	if(!path)
	{
		nv += "path=/;";
	}
	else if(typeof(path) == "string" && path != "")
	{
		nv += "path=" + path + ";";
	}

	if(!domain && typeof(VS_COOKIEDM) != "undefined")
	{
		domain = VS_COOKIEDM;
	}
	
	if(typeof(domain) == "string" && domain != "")
	{
		nv += "domain=" + domain + ";";
	}

	document.cookie = nv;
}

function kf_getCookie(name)
{
	
	var value = "";
	var cookies = document.cookie.split("; ");
	var nv;
	var i;

	for(i = 0; i < cookies.length; i++)
	{
		nv = cookies[i].split("=");

		if(nv && nv.length >= 2 && name == kf_rTrim(kf_lTrim(nv[0])))
		{
			value = unescape(nv[1]);
		}
	}

	return value;
} 
*/

function kf_sleepShow()   
{   
	//kf_setCookie('isshowed', 1, 10, '/', wpadomain); 
	 
	
	var position = parseInt(document.getElementById("_qqcode_rightDiv").style.top) + 100;
	
	var xx_w,xx_h;
	if(document.body.scrollWidth){
		xx_w = document.body.scrollWidth;
		xx_h = document.body.scrollHeight;
	}
	else{
		xx_w = document.documentElement.scrollWidth;
		xx_h = document.documentElement.scrollHeight;
	}
	
	var ew = 340;
    var ww = xx_w;

    var lt = (ww/2 - ew/2);
	var wh = xx_h;
	var xy = kf_getcenterXY();

	
//	var tp = (wh*0.15 + xy.y) + 'px';
	

	var qq_left_pos=lt;

	
	popupDivHtml = '<div id="kfpopupDiv" onmousedown="MyMove.Move(\'kfpopupDiv\',event,1);"  style="z-index:10000;filter:alpha(opacity=90);position: absolute; top: '+position+'px; left: '+qq_left_pos+'px;color:#000;font-size: 12px;cursor:move;height: 150px;width: 340px;">';
  popupDivHtml += kf_getPopupDivHtml(qqcode_dg_cfg,companyname,welcomeword, wpadomain);
  popupDivHtml += '</div>';
  document.body.insertAdjacentHTML("beforeEnd",popupDivHtml);
} 

function kf_getcenterXY(){
	var x,y;
	if(document.body.scrollTop){
		x = document.body.scrollLeft;
		y = document.body.scrollTop;
	}
	else{
		x = document.documentElement.scrollLeft;
		y = document.documentElement.scrollTop;
	}
	return {x:x,y:y};
};


function kf_dealErrors() 
{
	//kf_hide();
	return true;
}

function kf_lTrim(str)
{
  while (str.charAt(0) == " ")
  {
    str = str.slice(1);
  }
  return str;
}

function kf_rTrim(str)
{
  var iLength = str.length;
  while (str.charAt(iLength - 1) == " ")
  {
    str = str.slice(0, iLength - 1);
	iLength--;
  }
  return str;
}

window.onerror = kf_dealErrors;
var MyMove = new Tong_MoveDiv();   

function Tong_MoveDiv()
{ 
 	  this.Move=function(Id,Evt,T) 
 	  {    
 	  	if(Id == "") 
		{
			return;
		} 
 	  	var o = document.getElementById(Id);    
 	  	if(!o) 
		{
			return;
		}    
 	    evt = Evt ? Evt : window.event;    
 	    o.style.position = "absolute";    
 	    o.style.zIndex = 9999;    
 	    var obj = evt.srcElement ? evt.srcElement : evt.target;   
 	    var w = o.offsetWidth;      
 	    var h = o.offsetHeight;      
 	    var l = o.offsetLeft;      
 	    var t = o.offsetTop;  
 	    var div = document.createElement("DIV");  
 	    document.body.appendChild(div);   
 	    div.style.cssText = "filter:alpha(Opacity=10,style=0);opacity:0.2;width:"+w+"px;height:"+h+"px;top:"+t+"px;left:"+l+"px;position:absolute;background:#000";      
 	    div.setAttribute("id", Id +"temp");    
 	    this.Move_OnlyMove(Id,evt,T); 
 	}  
 	
 	this.Move_OnlyMove = function(Id,Evt,T) 
 	{    
 		  var o = document.getElementById(Id+"temp");    
 		  if(!o)
		  {
			return;
		  }   
 		  evt = Evt?Evt:window.event; 
 		  var relLeft = evt.clientX - o.offsetLeft;
 		  var relTop = evt.clientY - o.offsetTop;    
 		  if(!window.captureEvents)    
 		  {      
 		  	 o.setCapture();           
 		  }   
 		  else   
 		  {     
 		  	 window.captureEvents(Event.MOUSEMOVE|Event.MOUSEUP);      
 		  }       
 		  			  
	      document.onmousemove = function(e)
	      {
	            if(!o)
	            {
	                return;
	            }
	            e = e ? e : window.event;
	
	        	var bh = Math.max(document.body.scrollHeight,document.body.clientHeight,document.body.offsetHeight,
	        						document.documentElement.scrollHeight,document.documentElement.clientHeight,document.documentElement.offsetHeight);
	        	var bw = Math.max(document.body.scrollWidth,document.body.clientWidth,document.body.offsetWidth,
	        						document.documentElement.scrollWidth,document.documentElement.clientWidth,document.documentElement.offsetWidth);
	        	var sbw = 0;
	        	if(document.body.scrollWidth < bw)
	        		sbw = document.body.scrollWidth;
	        	if(document.body.clientWidth < bw && sbw < document.body.clientWidth)
	        		sbw = document.body.clientWidth;
	        	if(document.body.offsetWidth < bw && sbw < document.body.offsetWidth)
	        		sbw = document.body.offsetWidth;
	        	if(document.documentElement.scrollWidth < bw && sbw < document.documentElement.scrollWidth)
	        		sbw = document.documentElement.scrollWidth;
	        	if(document.documentElement.clientWidth < bw && sbw < document.documentElement.clientWidth)
	        		sbw = document.documentElement.clientWidth;
	        	if(document.documentElement.offsetWidth < bw && sbw < document.documentElement.offsetWidth)
	        		sbw = document.documentElement.offsetWidth;
	             
	            if(e.clientX - relLeft <= 0)
	            {
	                o.style.left = 0 +"px";
	            }
	            else if(e.clientX - relLeft >= bw - o.offsetWidth - 2)
	            {
	                o.style.left = (sbw - o.offsetWidth - 2) +"px";
	            }
	            else
	            {
	                o.style.left = e.clientX - relLeft +"px";
	            }
	            if(e.clientY - relTop <= 1)
	            {
	                o.style.top = 1 +"px";
	            }
	            else if(e.clientY - relTop >= bh - o.offsetHeight - 30)
	            {
	                o.style.top = (bh - o.offsetHeight) +"px";
	            }
	            else
	            {
	                o.style.top = e.clientY - relTop +"px";
	            }
	      }
 		   
 		  document.onmouseup = function()      
 		  {       
 		   	   if(!o) return;       
 		   	   	
 		   	   if(!window.captureEvents) 
			   {
			   	  o.releaseCapture();  
			   }         		   	   	      
 		   	   else  
			   {
			   	  window.releaseEvents(Event.MOUSEMOVE|Event.MOUSEUP); 
			   }     
 		   	   	        
 		   	   var o1 = document.getElementById(Id);       
 		   	   if(!o1) 
			   {
			      return; 
			   }        		   	   	
 		   	   var l0 = o.offsetLeft;       
 		   	   var t0 = o.offsetTop;       
 		   	   var l = o1.offsetLeft;       
 		   	   var t = o1.offsetTop;   
 		   	   
 		   	   //alert(l0 + " " +  t0 +" "+ l +" "+t);     
 		   	   
 		   	   MyMove.Move_e(Id, l0 , t0, l, t,T);       
 		   	   document.body.removeChild(o);       
 		   	   o = null;      
 		}  
 	}  
 	
 	
 	this.Move_e = function(Id, l0 , t0, l, t,T)     
 	{      
 		    if(typeof(window["ct"+ Id]) != "undefined") 
			{
				  clearTimeout(window["ct"+ Id]);   
			}
 		    
 		    var o = document.getElementById(Id);      
 		    if(!o) return;      
 		    var sl = st = 8;      
 		    var s_l = Math.abs(l0 - l);      
 		    var s_t = Math.abs(t0 - t);      
 		    if(s_l - s_t > 0)  
			{
				if(s_t) 
				{
					sl = Math.round(s_l / s_t) > 8 ? 8 : Math.round(s_l / s_t) * 6; 
				}       
 		    		      
 		        else
				{
					sl = 0; 
				}            		      
			}        		    	   
 		    else
			{
				if(s_l)
				{
					st = Math.round(s_t / s_l) > 8 ? 8 : Math.round(s_t / s_l) * 6;   
				}          		    		    
 		        else  
			    {
			  	    st = 0;
			    }       		      	  
			}       
 		    	       		      	
 		    if(l0 - l < 0) 
			{
				sl *= -1; 
			}  		    	     
 		    if(t0 - t < 0) 
			{
				st *= -1; 
			}   		    	     
 		    if(Math.abs(l + sl - l0) < 52 && sl) 
			{
 		    	sl = sl > 0 ? 2 : -2; 					
			}    
 		    if(Math.abs(t + st - t0) < 52 && st) 
			{
	        	st = st > 0 ? 2 : -2;  					
			}      
 		    if(Math.abs(l + sl - l0) < 16 && sl) 
			{
 		    	sl = sl > 0 ? 1 : -1;  					
			}   
 		    if(Math.abs(t + st - t0) < 16 && st) 
			{
 		    	st = st > 0 ? 1 : -1;    					
			} 
 		    if(s_l == 0 && s_t == 0)
			{
     		    return;   				
			} 
 		    if(T)      
 		    {    
 		    	o.style.left = l0 +"px";    
 		    	o.style.top = t0 +"px";    
 		    	return;      
 		    }      
 		    else      
 		    {    
 		    	if(Math.abs(l + sl - l0) < 2) 
				{
					o.style.left = l0 +"px";  
				}       		    		 
 		    	else     
				{
					o.style.left = l + sl +"px";   
				}   		    	 
 		    	if(Math.abs(t + st - t0) < 2) 
				{
					o.style.top = t0 +"px";   
				}        		    		 
 		    	else    
				{
					o.style.top = t + st +"px";   
				}
 		    		         		    	
 		    	window["ct"+ Id] = window.setTimeout("MyMove.Move_e('"+ Id +"', "+ l0 +" , "+ t0 +", "+ (l + sl) +", "+ (t + st) +","+T+")", 1);      
 		    }     
 		}   
} 
	



/*补充助手函数*/
function qqcode_getsiteurl(qqcode_root){
	
	var exp_result = qqcode_root.match(/^(\w+:\/\/[^\/]{1,}\/)*\/*/);
	if(exp_result==null)
	{
		return '';
	}
	
	return exp_result[0];
	
}

function qqcode_getdomain(qqcode_root){
	
	var exp_result = qqcode_root.match(/^\w+:\/\/([^\/]{1,})((:?\d*))\/*/);
	if(exp_result==null)
	{
		return '';
	}
	
	return exp_result[1];
	
}



//读取cookies
function qqcode_getCookie(name)
{
	
	var value = "";
	var cookies = document.cookie.split("; ");
	var nv;
	var i;

	for(i = 0; i < cookies.length; i++)
	{
		nv = cookies[i].split("=");

		if(nv && nv.length >= 2 && name == kf_rTrim(kf_lTrim(nv[0])))
		{
			value = unescape(nv[1]);
		}
	}

	return value;
} 

//删除cookies
function qqcode_delCookie(name)
{
    var exp = new Date();
    exp.setTime(exp.getTime() - 1);
    var cval=getCookie(name);
    if(cval!=null)
        document.cookie= name + "="+cval+";expires="+exp.toGMTString();
}
function qqcode_setCookie(name,value,time,domain)
{
    var strsec = qqcode_getsec(time);
    var exp = new Date();
    exp.setTime(exp.getTime() + strsec*1);
	var nv='';
	if(typeof(domain) == "string" && domain != "")
	{
		nv = "domain=" + domain + ";";
	}
	var path='/';

	document.cookie = name + "=" + escape (value) + ((exp) ? ";expires=" + exp.toGMTString() : "") + ((path) ? ";path=" + path : "; path=/");
	//document.cookie = name + "=" + escape (value) + ((exp) ? ";expires=" + exp.toGMTString() : "") + ((path) ? ";path=" + path : "; path=/") + ((domain) ? ";domain=" + domain : "");

}

function qqcode_getsec(str)
{
  
   var str1=str.substring(1,str.length)*1;
   var str2=str.substring(0,1);
   if (str2=="s")
   {
        return str1*1000;
   }
   else if (str2=="h")
   {
       return str1*60*60*1000;
   }
   else if (str2=="d")
   {
       return str1*24*60*60*1000;
   }
}


