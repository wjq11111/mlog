<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<%@ include file="/WEB-INF/sto_jsp/include/css.jsp"%>
	<%@ include file="/WEB-INF/sto_jsp/include/js.jsp"%>
	<OBJECT classid="CLSID:AE745FFD-E912-4411-AD53-91B19211B667"  id="doit" VIEWASTEXT width="1" height="1"></object>
</head>
<body class="easyui-layout" data-options="fit:true">

<div data-options="region:'north',border:false,title:'查询'"
		style="height: 60px; overflow: hidden;" align="left">
		<form id="searchForm">
			<table class="tableForm datagrid-toolbar"
				style="width: 100%; height: 100%;">
				<tr>
					<td style="font-size: 12px; width: 20%; align=left;">门禁名称:
					<input name="doorname" />
					</td>
					
					<td><input  type="text" style="display:none" /> 
					<a href="javascript:_search();" class="easyui-linkbutton">查询</a></td>
				</tr>
			</table>
		</form>
	</div>
	<div data-options="region:'center',border:false" style="overflow: hidden;">
		<table id="datagrid" style="overflow: auto;"></table>
	</div>
	   
<script type="text/javascript" >
	var datagrid;
	$(function() {
		$('input:text:first').focus(); //把焦点放在第一个文本框
		$('input').keypress(function (e) { 
		    if (e.which == 13) {//回车
		        _search();
		    }
		});
		
	
		datagrid = $('#datagrid').datagrid({
			url : '${pageContext.request.contextPath}/door/listJson.action',
			height:$(document.body).height()-70,
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
				checkbox : true
			},  {
				title : '门禁名称',
				field : 'doorname',
				width : 120,
				align:'center'
			}, {
				title : '控制器序列号',
				field : 'controllerid',
				width : 120,
				align:'center'
			},{
				title : '端口号',
				field : 'portid',
				width : 120,
				align:'center'
			}, {
				title : '门禁状态',
				field : 'doorstatus',
				width : 120,
				align:'center',
				formatter : function(v,row,index) {
					if ( row.doorstatus != 1) {
						return "已禁用";
					}else {
						return "启用";
					}
				}
			} ] ],
			toolbar : [ {
				text : '增加',
				iconCls : 'icon-add',
				handler : function() {
					save();
				}
			},/* '-', {
				text : '编辑',
				iconCls : 'icon-edit',
				handler : function() {
					update();
				}
			} , */  '-', {
				text : '启用门禁',
				iconCls : 'icon-edit',
				handler : function() {
					dooropen();
				} 
			} ,  '-', {
				text : '禁用门禁',
				iconCls : 'icon-edit',
				handler : function() {
					doorshut();
				} 
			} 
			],
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

	});

	function save() {
		var p = sy.dialog({
			title : '新增',
			href : '${pageContext.request.contextPath}/door/save.action',
			width : 380,
			height : 200,
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
				      url : "${pageContext.request.contextPath}/door/saveDo.action",
				      data : p.find('form').serialize(),
				      success : function(json){
						if (json.success) {
							datagrid.datagrid('reload');
							p.dialog('close');
							sy.messagerAlert('提示', '增加菜单项后需要重新登录该系统 ！');
						}
						sy.messagerShow({
							msg : json.msg,
							title : '提示'
						});
				      }
					 });
				}
			} ],
			onLoad : function() {
			}
		});
	}
	
	function update() {
		var rows = datagrid.datagrid('getSelections');
		if (rows.length == 1) {
			var p = sy.dialog({
				title : '修改',
				href : '${pageContext.request.contextPath}/door/update.action?id='+rows[0].id,
				width : 380,
				height : 300,
				buttons : [ {
					text : '保存',
					handler : function() {
						if (!p.find('form').form('validate')) {
							return false;
						}
						$.ajax({
					      type : "post",
					      url : "${pageContext.request.contextPath}/door/updateDo.action",
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
				} ],
				onLoad : function() {
				}
				
			});
		} else if (rows.length > 1) {
			sy.messagerAlert('提示', '同一时间只能编辑一条记录！', 'error');
		} else {
			sy.messagerAlert('提示', '请勾选要编辑的记录！', 'error');
		}
	}
	
	function dooropen() {
		var rows = datagrid.datagrid('getSelections');
		var ids = [];
		if (rows.length > 0) {
			sy.messagerConfirm('请确认', '确定要启动该门禁吗？', function(r) {
				if (r) {
					for ( var i = 0; i < rows.length; i++) {
						ids.push(rows[i].id);
					}
					$.ajax({
						url : '${pageContext.request.contextPath}/door/openStatusDo.action',
						data : {
							ids : ids.join(',')
						},
						dataType : 'json',
						success : function(d) {
							datagrid.datagrid('load');
							datagrid.datagrid('unselectAll');
							sy.messagerShow({
								title : '提示',
								msg : d.msg
							});
						}
					});
				}
			});
		} else {
			sy.messagerAlert('提示', '请勾选要编辑的记录！', 'error');
		}
	}
	function doorshut() {
		var rows = datagrid.datagrid('getSelections');
		var ids = [];
		if (rows.length >0) {
			sy.messagerConfirm('请确认', '确定要禁用该门禁吗？', function(r) {
				if (r) {
					for ( var i = 0; i < rows.length; i++) {
						ids.push(rows[i].id);
					}
					$.ajax({
						url : '${pageContext.request.contextPath}/door/shutStatusDo.action',
						data : {
							ids : ids.join(',')
						},
						dataType : 'json',
						success : function(d) {
							datagrid.datagrid('load');
							datagrid.datagrid('unselectAll');
							sy.messagerShow({
								title : '提示',
								msg : d.msg
							});
						}
					});
				}
			});
		}  else {
			sy.messagerAlert('提示', '请勾选要编辑的记录！', 'error');
		}
	}
	/* function remove() {
		var rows = datagrid.datagrid('getChecked');
		var ids = [];
		if (rows.length > 0) {
			sy.messagerConfirm('请确认', '您要删除选择门禁吗？', function(r) {
				if (r) {
					for ( var i = 0; i < rows.length; i++) {
						ids.push(rows[i].id);
					}
					$.ajax({
						url : '${pageContext.request.contextPath}/door/delete.action',
						data : {
							ids : ids.join(',')
						},
						dataType : 'json',
						success : function(d) {
							datagrid.datagrid('load');
							datagrid.datagrid('unselectAll');
							sy.messagerShow({
								title : '提示',
								msg : d.msg
							});
						}
					});
				}
			});
		} else {
			sy.messagerAlert('提示', '请选择要冻结的用户！', 'error');
		}
	}
	
	 */
	
	function _search() {
		
		
		datagrid.datagrid('uncheckAll');
		datagrid.datagrid('load', sy.serializeObject($('#searchForm')));
		
	}
	
</script>
</body>
</html>
