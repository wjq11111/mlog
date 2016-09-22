<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<%@ include file="/WEB-INF/sto_jsp/include/css.jsp"%>
	<%@ include file="/WEB-INF/sto_jsp/include/js.jsp"%>
	<script src="${ctx}/js/My97DatePicker/WdatePicker.js"
	type="text/javascript"></script>
	<OBJECT classid="CLSID:AE745FFD-E912-4411-AD53-91B19211B667"  id="doit" VIEWASTEXT width="1" height="1"></object>
</head>
<body class="easyui-layout" data-options="fit:true">

<div data-options="region:'north',border:false,title:'查询'"
		style="height: 60px; overflow: hidden;" align="left">
		<form id="searchForm">
			<table class="tableForm datagrid-toolbar"
				style="width: 100%; height: 100%;">
				<tr>
				<!--	<td style="font-size: 12px; width: 20%; align=left;">账号:
					<input name="username" />
					</td> -->
					<shiro:hasRole name="sysadmin">
					<td style="font-size: 12px; width: 20%; align=left;">单位:
						<input id="divid" name="divid" />
					</td>
					</shiro:hasRole>
					 <td style="font-size: 12px;width:7%; align=left;">开始日期：</td>
					<td style="font-size: 12px;width:15%"><input id="startDate" name="startDate"  class="Wdate" style="width: 155px;"
						onFocus="WdatePicker({onpicked:function(dp){selectDate();},isShowClear:false,readOnly:true,maxDate:'#F{$dp.$D(\'endDate\');}'})"
						value="${startDate}"></td>
					<td style="font-size: 12px;width: 7%; align=left;">结束日期：</td>
					<td style="font-size: 12px;width:15%"><input id="endDate" name="endDate"   class="Wdate"	style="width: 155px;"			
						onFocus="WdatePicker({onpicked:function(dp){selectDate();},isShowClear:false,readOnly:true})"
						value="${endDate}"></td>
					<td><input  type="text" style="display:none" />  
					<a href="javascript:_search();" class="easyui-linkbutton">查询</a>
					</td>
					
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
		$('input:text:first').focus(); //把焦点放在第一个文本框,
		try{
			$("#divid").combobox({   
			    url:'${ctx}/unit/listJsonNoPage.action',  //获取单位信息，以中文名称显示出来
			    valueField:'divid',
			    textField: 'divname',
			    panelHeight:'250',
			    onSelect : function(r){
			    	$("#deptid").combotree('clear');
			    	$("#deptid").combotree('reload','${ctx}/dept/listJson.action?divid='+r.divid);  
			    },
			    onChange : function(n,o){
					if(n=='' || n == 'undefined'){
						$("#divid").combobox('reset');
					}		    	
			    }
			});
			
		}catch(e){
			e.message;
		}
	
		datagrid = $('#datagrid').datagrid({
			url : '${pageContext.request.contextPath}/fest/listJson.action',
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
				field : 'ID',
				width : 150,
				align:'center',
				sortable : true,
				checkbox : true
			}, {
				title : '单位',
				field : 'divname',
				width : 120,
				align:'center'
			}, {
				title : '时间',
				field : 'date',
				width : 120,
				align:'center'
			},{
				title : '备注',
				field : 'remark',
				width : 120,
				align:'center'
			} ] ] ,
			toolbar : [ {
				text : '增加',
				iconCls : 'icon-add',
				handler : function() {
					save();
				}
			},'-', {
				text : '删除',
				iconCls : 'icon-remove',
				handler : function() {
					remove();
				}
			} ]
		});

	});

	function save() {
		var p = sy.dialog({
			title : '新增',
			href : '${pageContext.request.contextPath}/fest/save.action',
			width : 370,
			height : 270,
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
				      url : "${pageContext.request.contextPath}/fest/saveDo.action",
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
	}
	
	function update() {
		var rows = datagrid.datagrid('getSelections');
		if (rows.length == 1) {
			var p = sy.dialog({
				title : '修改',
				href : '${pageContext.request.contextPath}/fest/update.action?id='+rows[0].id,
				width : 470,
				height : 400,
				buttons : [ {
					text : '保存',
					handler : function() {
						if (!p.find('form').form('validate')) {
							return false;
						}
						$.ajax({
					      type : "post",
					      url : "${pageContext.request.contextPath}/fest/updateDo.action",
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
	
		
	function remove() {
		var rows = datagrid.datagrid('getChecked');
		var ids = [];
		if (rows.length > 0) {
			sy.messagerConfirm('请确认', '您要删除选择用户吗？', function(r) {
				if (r) {
					for ( var i = 0; i < rows.length; i++) {
						ids.push(rows[i].id);
					}
					$.ajax({
						url : '${pageContext.request.contextPath}/fest/delete.action',
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
			sy.messagerAlert('提示', '请选择要删除的用户！', 'error');
		}
	}
	
	function _search() {
		try{
		if ($("#startDate").val() == '') {
			$("#startDate").combobox('setValue','');
		}
		if ($("#endDate").val() == '') {
			$("#endDate").combobox('setValue','');
			}
		}catch(e)
		{
			e.message;
		}
		try{
			if($("#divid").combobox('getText') == ''){
				$("#divid").combobox('setValue','');
			}
		}catch(e){
			e.message;
		} 
		
	
		
			
		datagrid.datagrid('uncheckAll');
		datagrid.datagrid('load', sy.serializeObject($('#searchForm')));
		
	}
	
</script>
</body>
</html>
