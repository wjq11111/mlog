<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<script src="${ctx}/js/My97DatePicker/WdatePicker.js"
	type="text/javascript"></script>
<head/>
<%@ include file="/WEB-INF/sto_jsp/include/css.jsp"%>
<link rel="stylesheet" type="text/css"
	href="${ctx}/_static/js/ztree/zTreeStyle/zTreeStyle.css">
<style>
div.content_wrap {
	width: 600px;
	height: 380px;
}

div.content_wrap div.left {
	float: left;
	width: 250px;
}

div.content_wrap div.right {
	float: right;
	width: 340px;
}

div.zTreeDemoBackground {
	width: 400px;
	text-align: left;
	border: 1px solid #8080ff;
	overflow-y: auto
}

.search {
	background: transparent url('${ctx}/_static/images/auth_query.png')
		no-repeat 0 -27px;
	border: 0 none;
	height: 28px;
	width: 142px;
	padding: 0 12px 0 40px;
	outline: none;
	font-size: 12px;
	line-height: 28px;
}
</style>
<%@ include file="/WEB-INF/sto_jsp/include/js.jsp"%>
<script type="text/javascript"
	src="${ctx}/_static/js/ztree/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript"
	src="${ctx}/_static/js/ztree/jquery.ztree.all-3.5.min.js"></script>
<script type="text/javascript"
	src="${ctx}/_static/js/ztree/jquery.ztree.exhide-3.5.min.js"></script>
<OBJECT classid="CLSID:AE745FFD-E912-4411-AD53-91B19211B667" id="doit"
	VIEWASTEXT width="1" height="1"></object> 
	
	
	<script type="text/javascript">
	$('input:text:first').focus(); //把焦点放在第一个文本框
	$('input').keypress(function (e) { 
	    if (e.which == 13) {//回车
	        _search();
	    }
	});
	var datagrid;
	var divid;
	$(function() {
		var doorid=<%=request.getAttribute("doorid")%>;
		//alert(tt);
		datagrid = $('#datagrid').datagrid({
			url : '${pageContext.request.contextPath}/door/doorlistJson.action?id='+doorid,
			height:$(document.body).height()-120,
			iconCls : 'icon-save',
			pagination : true,
			pageSize : 10,
			pageList : [5,10,20,30,50],
			pageNumber:1,
			fitColumns : true,
			nowrap : true,
			border : false,
			idField : 'id',
			sortName : 'id',
			sortOrder : 'desc',
			checkOnSelect : true,
			selectOnCheck : true,
			columns : [ [ {
				title : '编号',
				field : 'id',
				width : 150,
				sortable : true,
				hidden:true
			},  {
				title : '门禁序列号',
				field : 'accesscontrolid',
				width : 120,
				align:'center'
			}, {
				title : '姓名',
				field : 'name',
				width : 120,
				align:'center'
			},{
				title : '时间',
				field : 'recordtime',
				width : 120,
				align:'center'
			},{
				title : '类型',
				field : 'type',
				width : 120,
				align:'center' ,
				formatter : function(v,row,index) {
					if ( row.type == 0) {
						return "超级卡";
					}
					if ( row.type == 1) {
						return "IC卡";
					}
					if ( row.type == 2) {
						return "手机";
					}
				} 
			},{
				title : '出入状态',
				field : 'status',
				width : 120,
				align:'center' ,
				formatter : function(v,row,index) {
					if ( row.status == 1) {
						return "开门";
					}
				} 
			} ] ],
			toolbar : [ {
				text : '策略设置',
				iconCls : 'icon-add',
				handler : function() {
					addcelv();
				}
			}/* ,'-' ,{
				text : '紧急锁定',
				iconCls: 'icon-help',
				handler : function() {
					lock();
				} 
			}*/],
			
			onRowContextMenu : function(e, rowIndex, rowData) {
				e.preventDefault();
				$(this).datagrid('unselectAll');
				$(this).datagrid('selectRow', rowIndex);
				$('#menu').menu('show', {
					left : e.pageX,
					top : e.pageY
				});
			},
			onLoadSuccess : function(data){
				datagrid.datagrid('uncheckAll');
			}
		});
		$("#l_div").css('height',$(document.body).height()-150);
		$("#r_div").css('height',$(document.body).height()-150);
		$('#divid').combobox({
		    url : '${ctx}/unit/listJsonNoPage.action',    
		    valueField : 'divid',    
		    textField : 'divname',
		    editable : false,
		    onSelect : function(r){
		    	initLeftTree(r.divid);
		    	initRightTree(r.divid);
		    	divid=r.divid;
		    }
		});
		
		
	});
	/* function selectType(){
		
		var checkedItems = new Array(); 
		
		$("input[name='type']:checked").each(function() {checkedItems.push($(this).val());});
		var ltree = $.fn.zTree.getZTreeObj("left");
		var rtree = $.fn.zTree.getZTreeObj("right");
		
		var node = ltree.getSelectedNodes();
		if(node && node.length>0){
			if(checkedItems.length==1){
				var id = node[0].id;
				var los = id.indexOf("_");
				var userid = id.substring(los+1,id.length);
				//alert("selecttype="+$("#managementtype").val().replace('\s',''));
				if ($("#managementtype").val().replace('\s','') == '0'){
				$.ajax({
				   type: "POST",
				   url: '${pageContext.request.contextPath}/user/getDataAuthTreeids.action',
				   data:{type:checkedItems[0],userid:userid},
				   dataType : 'json', 
				   success: function(data){
					   rtree.checkAllNodes(false);
					   for(var i=0;i<data.length;i++){
						   var node = rtree.getNodeByParam("id", data[i], null);
						   //treeObj.selectNode(node,true);
						   rtree.checkNode(node, true, true);
					   }
				   }
				});
			  }else if ($("#managementtype").val().replace('\s','') == '1'){
				  $.ajax({
					   type: "POST",
					   url: '${pageContext.request.contextPath}/user/getNewUserDataAuthTreeids.action',
					   data:{type:checkedItems[0],userid:userid},
					   dataType : 'json', 
					   success: function(data){
						   rtree.checkAllNodes(false);
						   for(var i=0;i<data.length;i++){
							   var node = rtree.getNodeByParam("id", data[i], null);
							   //treeObj.selectNode(node,true);
							   rtree.checkNode(node, true, true);
						   }
					   }
					   
					});
			  }
			}else {
				rtree.checkAllNodes(false);
			}
		}
		
		
	}

	function zTreeBeforeClick(treeId, treeNode, clickFlag) {
		var checkedItems = new Array(); 
		$("input[name='type']:checked").each(function() {checkedItems.push($(this).val());});
		
		if(checkedItems.length<=0){
			sy.messagerShow({
				msg : '请选择权限类型',
				title : '提示'
			});
			return false;
		}
		
		if(treeNode.children){
			return false;
		}
	};

	function zTreeOnClick(event, treeId, treeNode) {
		var checkedItems = new Array(); 
		$("input[name='type']:checked").each(function() {checkedItems.push($(this).val());});
		if(checkedItems.length>1){
			return false;
		}
		if(checkedItems.length<=0){
			sy.messagerShow({
				msg : '请选择权限类型',
				title : '提示'
			});
			return false;
		}
		
		var treeObj = $.fn.zTree.getZTreeObj("right");
		if(checkedItems.length==1){
			var id = treeNode.id;
			var los = id.indexOf("_");
			var userid = id.substring(los+1,id.length);
		//	alert("zTreeOnClick="+$("#managementtype").val().replace('\s',''));
			if ($("#managementtype").val().replace('\s','') == '0'){
			$.ajax({
			   type: "POST",
			   url: '${pageContext.request.contextPath}/user/getDataAuthTreeids.action',
			   data:{type:checkedItems[0],userid:userid},
			   dataType : 'json', 
			   success: function(data){
				   treeObj.checkAllNodes(false);
				   for(var i=0;i<data.length;i++){
					   var node = treeObj.getNodeByParam("id", data[i], null);
					   //treeObj.selectNode(node,true);
					   treeObj.checkNode(node, true, true);
				   }
			   }
			});
		   }else if ($("#managementtype").val().replace('\s','') == '1'){
			  $.ajax({
				   type: "POST",
				   url: '${pageContext.request.contextPath}/user/getNewUserDataAuthTreeids.action',
				   data:{type:checkedItems[0],userid:userid},
				   dataType : 'json', 
				   success: function(data){
					  // alert("data"+data.length);
					   treeObj.checkAllNodes(false);
					   for(var i=0;i<data.length;i++){
						  // alert("data[i]="+data[i])
						    var node = treeObj.getNodeByParam("id", data[i], null);
						   //treeObj.selectNode(node,true);
						   treeObj.checkNode(node, true, true);
					   }
				   }
				 
				});
		  }
		}else {
			treeObj.checkAllNodes(false);
		}
	    //alert(treeNode.id + ", " + treeNode.deptname);
	}; */
	
	
	var setting1;
	var alllNodes = [];
	function initLeftTree(divid){
		var doorid=<%=request.getAttribute("doorid")%>;
		var left = $("#left");
		//生成菜单tree
		setting1 = {
			check: {
					enable: true
			},
			edit: {
				enable: true,
				showRemoveBtn: false,
				showRenameBtn: false
			},
			view: {
				showIcon: true,
				fontCss: getFontCss
			},
			 /* callback: {
				onLoadSuccess:expand_ztree
				//beforeClick : expand_ztree
				//onClick : zTreeOnClick
				
			}, */ 
			data: {
				simpleData: {
					enable: true
				}
			}
		};
		
		
		$.ajax({
		   type: "POST",
		   url: '${pageContext.request.contextPath}/auth/doorAuthlist.action',
		   data : {divid:divid,doorid:doorid},
		   dataType : 'json', 
		   success: function(data){
			   alllNodes = data;
			  
			   $.fn.zTree.init(left, setting1, data);
			/*    $.fn.zTree.init(right, setting2, data); */
			   
		   }
		});
      }
	function initRightTree(divid){
		
		var doorid=<%=request.getAttribute("doorid")%>;
		var right = $("#right");
		//生成菜单tree
		var setting2 = {
			check: {
				enable: true
			},
			view: {
				showIcon: true
			},
			data: {
				simpleData: {
					enable: true
				}
			}
		};
		$.ajax({
		   type: "POST",
		   url: '${pageContext.request.contextPath}/auth/doorUnauthlist.action',
		   data : {divid:divid,doorid:doorid},
		   dataType : 'json', 
		   success: function(data){
			   alllNodes = data;
			  
			  /*  $.fn.zTree.init(left, setting1, data); */
			   $.fn.zTree.init(right, setting2, data);
			   
		   }
		});
      }
	initLeftTree(null);
	initRightTree(null);
	var nodeList = [];
	var nodeList1 = [];
	/* $('#username').bind("blur",function(event) {
		search_ztree('left','username');
	}); */
	$('#username').bind("keyup",function(event) {/* 增加回车提交功能 */
		if (event.keyCode == 13) {
			search_ztree('left','username');
		}
	});
	/**
     * 展开树
     * @param treeId  
     */
    function expand_ztree(treeId){
        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        treeObj.expandAll(true);
    }
	
     
    /**
     * 收起树：只展开根节点下的一级节点
     * @param treeId
     */
    function close_ztree(treeId){
        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        var nodes = treeObj.transformToArray(treeObj.getNodes());
        var nodeLength = nodes.length;
        for (var i = 0; i < nodeLength; i++) {
            if (nodes[i].id == '0') {
                //根节点：展开
                treeObj.expandNode(nodes[i], true, true, false);
            } else {
                //非根节点：收起
                treeObj.expandNode(nodes[i], false, true, false);
            }
        }
    }
     
    /**
     * 搜索树，高亮显示并展示【模糊匹配搜索条件的节点s】
     * @param treeId
     * @param searchConditionId 文本框的id
     */
    function search_ztree(treeId, searchConditionId){
        searchByFlag_ztree(treeId, searchConditionId, "");
    }
     
    /**
     * 搜索树，高亮显示并展示【模糊匹配搜索条件的节点s】
     * @param treeId
     * @param searchConditionId     搜索条件Id
     * @param flag                  需要高亮显示的节点标识
     */
    function searchByFlag_ztree(treeId, searchConditionId, flag){
        //<1>.搜索条件
        var searchCondition = $('#' + searchConditionId).val();
        //<2>.得到模糊匹配搜索条件的节点数组集合
        var highlightNodes = new Array();
        if (searchCondition != "") {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            highlightNodes = treeObj.getNodesByParamFuzzy("name", searchCondition, null);
        }
        //<3>.高亮显示并展示【指定节点s】
        highlightAndExpand_ztree(treeId, highlightNodes, flag);
    }
     
    /**
     * 高亮显示并展示【指定节点s】
     * @param treeId
     * @param highlightNodes 需要高亮显示的节点数组
     * @param flag           需要高亮显示的节点标识
     */
    function highlightAndExpand_ztree(treeId, highlightNodes, flag){
        var treeObj = $.fn.zTree.getZTreeObj(treeId);
        //<1>. 先把全部节点更新为普通样式
        var treeNodes = treeObj.transformToArray(treeObj.getNodes());
        for (var i = 0; i < treeNodes.length; i++) {
            treeNodes[i].highlight = false;
            treeObj.updateNode(treeNodes[i]);
        }
        //<2>.收起树, 只展开根节点下的一级节点
        close_ztree(treeId);
        //<3>.把指定节点的样式更新为高亮显示，并展开
        if (highlightNodes != null) {
            for (var i = 0; i < highlightNodes.length; i++) {
                if (flag != null && flag != "") {
                    if (highlightNodes[i].flag == flag) {
                        //高亮显示节点，并展开
                        highlightNodes[i].highlight = true;
                        treeObj.updateNode(highlightNodes[i]);
                        //高亮显示节点的父节点的父节点....直到根节点，并展示
                        var parentNode = highlightNodes[i].getParentNode();
                        var parentNodes = getParentNodes_ztree(treeId, parentNode);
                        treeObj.expandNode(parentNodes, true, false, true);
                        treeObj.expandNode(parentNode, true, false, true);
                    }
                } else {
                    //高亮显示节点，并展开
                    highlightNodes[i].highlight = true;
                    treeObj.updateNode(highlightNodes[i]);
                    //高亮显示节点的父节点的父节点....直到根节点，并展示
                    var parentNode = highlightNodes[i].getParentNode();
                    var parentNodes = getParentNodes_ztree(treeId, parentNode);
                    treeObj.expandNode(parentNodes, true, false, true);
                    treeObj.expandNode(parentNode, true, false, true);
                }
            }
        }
    }
     
    /**
     * 递归得到指定节点的父节点的父节点....直到根节点
     */
    function getParentNodes_ztree(treeId, node){
        if (node != null) {
            var treeObj = $.fn.zTree.getZTreeObj(treeId);
            var parentNode = node.getParentNode();
            return getParentNodes_ztree(treeId, parentNode);
        } else {
            return node;
        }
    }
     
    /**
     * 设置树节点字体样式
     */
    function getFontCss(treeId, treeNode) {
        if (treeNode.id == 0) {
            //根节点
            return {color:"#333", "font-weight":"bold"};
        } else if (treeNode.isParent == false){
            //叶子节点
            return treeNode.highlight ? {color:"#ff0000", "font-weight":"bold"} : {color:"#333", "font-weight":"normal"};
        } else {
            //父节点
            return treeNode.highlight ? {color:"#ff0000", "font-weight":"bold"} : {color:"#333", "font-weight":"normal"};
        }
    }
	
	
	//保存用户门禁权限
	function authsave(){
		
	var id=<%=request.getAttribute("doorid")%>;
	//alert(id);
	var rtree = $.fn.zTree.getZTreeObj("right");
	var checkedItems = new Array();
	var nodes = rtree.getCheckedNodes(true);
	//alert(nodes);
	if(nodes==""){
		sy.messagerShow({
			msg : '请选择授权用户',
			title : '提示'
		});
		return false;
	}
	var userids="";
	for(var i=0;i<nodes.length;i++){
		//alert(JSON.stringify(nodes[i]));
		//alert(nodes[i].id);
		var rid = String(nodes[i].id);
		
		var los = rid.indexOf("_");
		if(los != -1){
			userids += rid.substring(los+1,rid.length)+",";
		} 
		
	}
	$.ajax({
		   type: "POST",
		   url: '${pageContext.request.contextPath}/door/saveDoorAuth.action',
		   data:{doorid:id,userids:userids},
		   dataType : 'json', 
		   success: function(json){
			   	if (json.success) {
			   		initLeftTree(divid);
			   		initRightTree(divid);
			   		expand_ztree("left");
			   		//expand_ztree("right");
				}
				sy.messagerShow({
					msg : json.msg,
					title : '提示'
				});
		   }
		});
	expand_ztree("left");
	expand_ztree("right");
	//alert("divid"+divid);
	//initLeftTree(divid);
	//initRightTree(divid);
}
	//删除用户门禁权限
	function authdelete(){
	var id=<%=request.getAttribute("doorid")%>;
	var ltree = $.fn.zTree.getZTreeObj("left");
	//alert("ltree"+ltree);
	var checkedItems = new Array();
	var nodes = ltree.getCheckedNodes(true);
	//alert(nodes=="");
	if(nodes==""){
		sy.messagerShow({
			msg : '请选择要取消授权的用户',
			title : '提示'
		});
		return false;
	}
	var userids="";
	for(var i=0;i<nodes.length;i++){
		//alert(JSON.stringify(nodes[i]));
		//alert(nodes[i].id);
		var rid = String(nodes[i].id);
		
		var los = rid.indexOf("_");
		if(los != -1){
			userids += rid.substring(los+1,rid.length)+",";
		} 
		
	}
	$.ajax({
		   type: "POST",
		   url: '${pageContext.request.contextPath}/door/deleteDoorAuth.action',
		   data:{doorid:id,userids:userids},
		   dataType : 'json', 
		   success: function(json){
			   	if (json.success) {
			   		initLeftTree(divid);
			   		initRightTree(divid);
			   		//$("#left").tree('reload');
			   		//$("#right").tree('reload');
			   		$("left").tree("expand");
			   		$("right").tree('expandAll');
			   		//expand_ztree("left");
			   		//expand_ztree("right");
				}
				 sy.messagerShow({
					msg : json.msg,
					title : '提示'
				}); 
				
		   }
		});
	//alert("divid"+divid);
	
}
	
	function addcelv() {
		var id=<%=request.getAttribute("doorid")%>;
		//alert(id);
		var p = sy.dialog({
			
			title : '策略设置',
			href : '${pageContext.request.contextPath}/door/addcelv.action?id='+id,
			width : 380,
			height : 280,
			buttons : [ {
				text : '保存',
				handler : function() {
					 if (!p.find('form').form('validate')) {
						return false;
					} 
					 var des = p.find('#logonid').val();
					if(des==""){
						sy.messagerAlert('提示', '名称不能空', 'error');
						return;
					} 
					$.ajax({
				      type : "post",
				      url : "${pageContext.request.contextPath}/door/saveCelv.action?id="+id,
				      data : p.find('form').serialize(),
				      success : function(json){
						if (json.success) {
							datagrid.datagrid('reload');
							p.dialog('close');
						}
						sy.messagerShow({
							msg : json.msg,
							title : '提示'
						});
				      }
					 });
				}
			} ,{
				text : '取消',
				handler : function(){
					p.dialog('close');
				}
			}],
			onLoad : function() {
			}
		});
	}
<%-- 	function lock(){
		var id=<%=request.getAttribute("doorid")%>;
		 sy.messagerConfirm('请确认', '您要删除选择门禁吗？', function(r) {
			if (r) {
			
			   $.ajax({
				url : '${pageContext.request.contextPath}/door/lock.action',
				data : {
				id : id
					   },
				dataType : 'json',
				success : function(d) {
					sy.messagerShow({
					title : '提示',
					msg : d.msg
					});
					alert(d.islock);
					if(d.islock == 1){
					   $("#warn").css('display','block');
					}else{
						 $("#warn").css('display','none');
					  }
				}
			});
		    }
			});
		
	} --%>
	function _search() {
		
		
		datagrid.datagrid('uncheckAll');
		datagrid.datagrid('load', sy.serializeObject($('#searchForm')));
		
	}
</script>
	
</head>

<body  data-options="fit:true">
<%--    <div id="warn">
			<c:if test="${islock==1}">
				<span><font id="blink" color="red">您的日志有新回复!</font></span>
			</c:if>
		</div> --%>
	<div id="doorid" class="easyui-tabs" style="width:1130px;height:540px;">
		<div title="门禁出入记录" style="padding: 20px;height:100%;">
		
		<form id="searchForm">
			<table class="tableForm datagrid-toolbar"
				style="width: 100%; ">
				<tr>
					<!-- <td style="font-size: 12px; width: 20%; align=left;">姓名:
					<input name="name" />
					</td> -->
					<td style="font-size: 12px;width: 6%;">开始日期：</td>
					<td style="width: 10%;align:left;"><input id="startDate" name="startDate"
						style="width: 155px;" class="Wdate"
						onFocus="WdatePicker({onpicked:function(dp){selectDate();},isShowClear:false,readOnly:true,maxDate:'#F{$dp.$D(\'endDate\');}'})"
						value="${startDate}"></td>
					<td style="font-size: 12px;width: 6%;">结束日期：</td>
					<td style="width: 10%;align:left;"><input id="endDate" name="endDate" style="width: 155px;"
						class="Wdate"
						onFocus="WdatePicker({onpicked:function(dp){selectDate();},isShowClear:false,readOnly:true,minDate:'#F{$dp.$D(\'startDate\');}'})"
						value="${endDate}"></td>
					<td style="align:left;"><input  type="text" style="display:none" /> 
					<a href="javascript:_search();" class="easyui-linkbutton">查询</a></td>
				</tr>
			</table>
		</form>
	
		<table id="datagrid" style="overflow: auto;">
		</table>
		</div>
		
		<div title="用户管理" 	style="padding: 20px; height:100%;"  >
		<form id="searchForm1">
			<table class="tableForm datagrid-toolbar"
				style="width: 100%; ">
				<tr>
					<td style="font-size: 12px; width: 15%; align=left;">单位:
					<input id="divid" name="divid" />
					</td>
					
				</tr>
			</table>
		</form>
		
		<table>			
			<!-- <tr>
			<td align="right" colspan="2"><input  type="text" style="display:none" /> 
					<a href="javascript:void(0)" class="easyui-linkbutton" onclick="save()">保存</a>
			</td>
			</tr> -->
			<tr>
				<td colspan="2"><div id="div_l"><img src="${ctx}/_static/images/auth_user1.png" style="margin-bottom:-8px;width:20px;height:20px;">已授权用户
				</td>
				<td><div id="div_r"><img src="${ctx}/_static/images/auth_user2.png" style="margin-bottom:-8px;width:20px;height:20px;">未授权用户</div>
				</td>
			</tr>
			<tr>
				<td>
					<div id="l_div" class="zTreeDemoBackground left">
						<ul id='left' name='left' class="ztree"></ul>
					</div>
				</td>
				<td align="right" style="width:30"><input  type="button" onclick="authsave()"  value="<<<授权"/> <br/><br/><br/>
			     <input  type="button"  value="取消>>>" onclick="authdelete()"/> 
				 </td>
				<td>
					<div id="r_div" class="zTreeDemoBackground left">
						<ul id='right' name='right' class="ztree"></ul>
					</div>
				</td>
			</tr>
		</table>
        </div>
	</div>
</body>
</html>
