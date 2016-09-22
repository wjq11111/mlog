var eoc = eoc || {};
eoc.cms = eoc.cms || {};
eoc.cms.main = function() {
	// init the main page
	$('#cms_content').tabs({
		select : '0'
	});
};

/**
 * tabs's width and height
 */
eoc.cms.main.tabsVolume = {};

/**
 * 显示的全部tab,用以控制是否显示
 */
eoc.cms.main.showTabs = {};
/**
 * 选中状态的tab
 */
eoc.cms.main.selectedTabs = {};
/**
 * 保存菜单树信息
 */
eoc.cms.main.menuTree = {};
/**
 * 上一次所点击的根节点id
 */
eoc.cms.main.lastMenuRootId = '';

var tabsMenu = $('#tabsMenu').menu({
	onClick : function(item) {
		var curTabTitle = $(this).data('tabTitle');
		var type = $(item.target).attr('type');

		if (type === 'refresh') {
			refreshTab(curTabTitle);
			return;
		}

		if (type === 'close') {
			var t = $('#cms_content').tabs('getTab', curTabTitle);
			if (t.panel('options').closable) {
				$('#cms_content').tabs('close', curTabTitle);
			}
			return;
		}

		var allTabs = $('#cms_content').tabs('tabs');
		var closeTabsTitle = [];

		$.each(allTabs, function() {
			var opt = $(this).panel('options');
			if (opt.closable && opt.title != curTabTitle && type === 'closeOther') {
				closeTabsTitle.push(opt.title);
			} else if (opt.closable && type === 'closeAll') {
				closeTabsTitle.push(opt.title);
			}
		});

		for ( var i = 0; i < closeTabsTitle.length; i++) {
			$('#cms_content').tabs('close', closeTabsTitle[i]);
		}
	}
});
/**
 * 打开主界面的tab标签页
 */
eoc.cms.main.openMainTab = function(node,superid) {
	var _cms_content = $('#cms_content');
	var _cms_left = $('#cms_left');
	
	eoc.cms.main.tabsVolume['width'] = _cms_content.width();
	eoc.cms.main.tabsVolume['height'] = _cms_left.height()-10;
	var flag = false;
	$(".tabs-inner").each(function(index, obj) {
		var text = $(obj).text();
		if (text == node.text) {
			$('#cms_content').tabs('select', node.text);
			flag = true;
			return;
		}
	})
	if (flag) {
		return;
	}
	//注册全局的ajax事件，来检查是否会话过期,过期就重新登录
	
	_cms_content.ajaxError(function(event, request, settings){
		if(request.responseText){
			if(request.responseText!=null && request.responseText.length>0){
				//绑定如果是跳转到login页面，来检查是否会话过期,过期就重新登录
				var _fm = $(request.responseText).find("input#isLogin");
				if(_fm!=null && _fm.length>0){
					$.messager.alert('确认','会话过期，请重新登录!','error',function(r){
						//window.location.href=window.location.href;
						window.location.reload();
					});
				}else{
					var d = $('<div id="errorExceptionDiv"></div>').appendTo('body');
					$('#errorExceptionDiv').dialog({
					    title: '异常',
					    width: 800,
					    height: 600,
					    closed: false,
					    cache: false,
					    content: request.responseText,
					    modal: true,
					    buttons:[{
							text:'确定',
							handler:function(){
								$('#errorExceptionDiv').dialog('close');
							}
						}]
					});
				}
			}
		}
    });
	//alert($(document).height()); //浏览器当前窗口文档的高度 
	var _height = $(window.document.body).height()-100;//浏览器当前窗口文档body的高度 
	//alert($(document.body).outerHeight(true));//浏览器当前窗口文档body的总高度 包括border padding margin 
	//alert($(window).width()); //浏览器当前窗口可视区域宽度 
	//alert($(document).width());//浏览器当前窗口文档对象宽度 
	//var width = $(document.body).width();//浏览器当前窗口文档body的高度 
	//alert($(document.body).outerWidth(true));//浏览器当前窗口文档body的总宽度 包括border padding margin
	if(node.attributes.enname=="cdgl" || node.attributes.enname=="dept"){
		_height = $(window.document.body).height()-50;
	}
	$('#cms_content').tabs('add', {
		id : superid,
		title : node.text,
		height:_height,
		content : '<iframe id="'+node.attributes.enname+'" src="'+ ctx + node.attributes.url  + '" style="border:0;background-color:#AAA;width:100%;" scrolling="auto" height="'+_height+'"></iframe>',
		closable : true,
		tools : [ {
			iconCls : 'icon-mini-refresh',
			handler : function() {
				refreshTab(node.text);
			}
		} ]
	});
	$('#cms_content').tabs({
		  onClose:function(title, index) {
			  delete eoc.cms.main.showTabs[title];
		  }
	});
	$('#cms_content').tabs('select', node.text);
	eoc.cms.main.showTabs[node.text] = {'id':superid,'text':node.text,'enname':node.attributes.enname};
	// resize the tabs layout.
	eoc.cms.main.resizeTabsLayout();
}

eoc.cms.main.openMainTabZtree = function(node,superid) {
	var _cms_content = $('#cms_content');
	var _cms_left = $('#cms_left');
	
	eoc.cms.main.tabsVolume['width'] = _cms_content.width();
	eoc.cms.main.tabsVolume['height'] = _cms_left.height()-10;
	var flag = false;
	$(".tabs-inner").each(function(index, obj) {
		var text = $(obj).text();
		if (text == node.name) {
			$('#cms_content').tabs('select', node.name);
			flag = true;
			return;
		}
	})
	if (flag) {
		return;
	}
	//注册全局的ajax事件，来检查是否会话过期,过期就重新登录
	
	_cms_content.ajaxError(function(event, request, settings){
		if(request.responseText){
			if(request.responseText!=null && request.responseText.length>0){
				//绑定如果是跳转到login页面，来检查是否会话过期,过期就重新登录
				var _fm = $(request.responseText).find("input#isLogin");
				if(_fm!=null && _fm.length>0){
					$.messager.alert('确认','会话过期，请重新登录!','error',function(r){
						//window.location.href=window.location.href;
						window.location.reload();
					});
				}else{
					var d = $('<div id="errorExceptionDiv"></div>').appendTo('body');
					$('#errorExceptionDiv').dialog({
					    title: '异常',
					    width: 800,
					    height: 600,
					    closed: false,
					    cache: false,
					    content: request.responseText,
					    modal: true,
					    buttons:[{
							text:'确定',
							handler:function(){
								$('#errorExceptionDiv').dialog('close');
							}
						}]
					});
				}
			}
		}
    });
	//alert($(document).height()); //浏览器当前窗口文档的高度 
	var _height = $(window.document.body).height()-100;//浏览器当前窗口文档body的高度 
	//alert($(document.body).outerHeight(true));//浏览器当前窗口文档body的总高度 包括border padding margin 
	//alert($(window).width()); //浏览器当前窗口可视区域宽度 
	//alert($(document).width());//浏览器当前窗口文档对象宽度 
	//var width = $(document.body).width();//浏览器当前窗口文档body的高度 
	//alert($(document.body).outerWidth(true));//浏览器当前窗口文档body的总宽度 包括border padding margin
	if(node.enname=="cdgl" || node.enname=="dept"){
		_height = $(window.document.body).height()-105;
	}
	$('#cms_content').tabs('add', {
		id : superid,
		title : node.name,
		height:_height,
		content : '<iframe id="'+node.enname+'" src="'+ ctx + node.curl  + '" style="border:0;background-color:#AAA;width:100%;" scrolling="auto" height="'+_height+'"></iframe>',
		closable : true,
		tools : [ {
			iconCls : 'icon-mini-refresh',
			handler : function() {
				refreshTab(node.name);
			}
		} ]
	});
	$('#cms_content').tabs({
		  onClose:function(title, index) {
			  delete eoc.cms.main.showTabs[title];
		  },
		  onContextMenu : function(e, title) {
			  e.preventDefault();
			  tabsMenu.menu('show', {
				  left : e.pageX,
					top : e.pageY
			  }).data('tabTitle', title);
		}
	});
	$('#cms_content').tabs('select', node.name);
	eoc.cms.main.showTabs[node.name] = {'id':superid,'text':node.name,'enname':node.enname};
	// resize the tabs layout.
	eoc.cms.main.resizeTabsLayout();
}

eoc.cms.main.resizeTabsLayout = function(){
	var _cms_content = $('#cms_content');
	var _cms_left = $('#cms_left');
	_cms_content.height(_cms_left.height()+21);/* +21 */
}

/**
 * 初始化分页信息
 */
eoc.cms.main.createPagination = function(pageInfo,tableJqObj){
	var _callBack = pageInfo.callBack || function(){}; 
	if(eoc.cms.main.isEmpty(pageInfo) | eoc.cms.main.isEmpty(tableJqObj)){
		return ;
	}
	var separateKeyValue = function(str){
		if(eoc.cms.main.isEmpty(pageInfo)){
			return ;
		}
		if(str.indexOf('=')==-1){
			return ;
		}
		return str.split('=');
	};
	try{
		var p = tableJqObj.datagrid('getPager');  
	    $(p).pagination({  
	        pageSize : eoc.cms.main.isEmpty(pageInfo.pageSize) == true ? 20 : pageInfo.pageSize,// 每页显示的记录条数，默认为10 
																								// 10
	        pageList : eoc.cms.main.isEmpty(pageInfo.pageList) == true ? [5,10,15,20,50] : pageInfo.pageList,// 可以设置每页记录条数的列表 
																												// [5,10,15]
	        beforePageText : '第',// 页数文本框前显示的汉字 
	        afterPageText : '页   共 {pages} 页',  
	        displayMsg : '当前显示 {from} - {to} 条记录  共 {total} 条记录',
			onSelectPage : function(pPageIndex, pPageSize){
		        var queryParams = tableJqObj.datagrid('options').queryParams;  
		        queryParams.pageIndex = pPageIndex;  
		        queryParams.pageSize = pPageSize;
		        if(!eoc.cms.main.isEmpty(pageInfo.params)){
		        	var _params = pageInfo.params;
		        	if(_params.indexOf('&')!=-1){
		        		var _paramsArr = _params.split('&');
		        		for(var i = 0; i < _paramsArr.length; i ++){
		        			var _kv = separateKeyValue(_paramsArr[i]);
		        			queryParams[_kv[0]] = _kv[1];
			        	}
		        	}else{
		        		var _kv = separateKeyValue(_params);
		        		queryParams[_kv[0]] = _kv[1];
		        	}
		        }
		        jQuery.isFunction(_callBack) && _callBack();
		        // 重新加载datagrid的数据
		        tableJqObj.datagrid('reload');
			}  
	    });
	}catch(err){
		throw err;
	}
}

/**
 * 判断是否为空值
 */
eoc.cms.main.isEmpty = function(val){
	return val == null || val == "" || val == undefined;
}

/**
 * 判断是否为数字
 */
function isNum(num) {
	  if (isNull(num)) {
		return false;
	   }
	   var reg=/^[0-9]*$/;
	  if (reg.test(num)) {
		   return true;
	   }
	  return false;
}

/**
 * 判断是否为空值
 */
function isNull(exp){
	if (exp==null|| typeof(exp)!="undefined" && exp=="")
	{
	    return true;;
	}
	return false;
}

/**
 * 判断是否为小数
 */
function isFloat(float) {
	  if (isNull(float)) {
		return false;
	   }
	   var reg=/^[0-9]*\.?[0-9]*$/;
	  if (reg.test(float)) {
		    return true;
	   }
	  return false;
}

eoc.cms.main.test = function(node) {
	// alert('main_test');
};

function refreshTab(title) {
	var tab = $('#cms_content').tabs('getTab', title);
	$('#cms_content').tabs('update', {
		tab : tab,
		options : tab.panel('options')
	});
}