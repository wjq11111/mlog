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
		<input id="columns" name="columns" type="hidden" /> <input id="width"
				name="width" type="hidden" />
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
					<shiro:hasAnyRoles name="sysadmin,unitmanager">
					<td style="font-size: 12px; width: 20%; align=left;">部门:
						<input id="deptid" name="deptid" />
					</td>
					</shiro:hasAnyRoles>
					 <td style="font-size: 12px;width:8%; align=left;">开始日期：</td>
					<td style="font-size: 12px;width:10%"><input id="startDate" name="startDate"  class="Wdate" style="width: 155px;"
						onFocus="WdatePicker({onpicked:function(dp){selectDate();},isShowClear:false,readOnly:true,maxDate:'#F{$dp.$D(\'endDate\');}'})"
						value="${startDate}"></td>
					<td style="font-size: 12px;width: 8%; align=left;">结束日期：</td>
					<td style="font-size: 12px;width:10%"><input id="endDate" name="endDate"   class="Wdate"	style="width: 155px;"			
						onFocus="WdatePicker({onpicked:function(dp){selectDate();},isShowClear:false,readOnly:true})"
						value="${endDate}"></td>
					<td><input  type="text" style="display:none" />  
					<a href="javascript:_search();" class="easyui-linkbutton">查询</a>
					</td>
					<td style="font-size: 12px;"><a href="javascript:void(0)"
						class="easyui-linkbutton" data-options="iconCls:'icon-search'"
						onclick="exportToExcel();">导出到excel</a></td>
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
			$("#deptid").combotree({
			    url: '${ctx}/dept/listJson.action',
			    editable : true,
			    onBeforeSelect : function(node){
			    	/* var parent = $("#deptid").combotree('tree').tree('getParent',node.target);
			    	if(parent == null){
			    		alert("请选择具体部门");
			    		return false;
			    	} */
			    	return true;
			    },
			    onBeforeLoad : function(param){
			    },
			    onChange : function(n,o){
					if(n=='' || n == 'undefined'){
						$("#deptid").combotree('reset');
					}		    	
			    }
			});
			
		}catch(e){
			e.message;
		}
	
		datagrid = $('#datagrid').datagrid({
			url : '${pageContext.request.contextPath}/leave/listJson.action',
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
				width : 100,
				align:'center',
				sortable : true,
				checkbox : true
			}, {
				title : '单位',
				field : 'divname',
				width : 80,
				align:'center'
			},{
				title : '部门',
				field : 'deptname',
				width : 100,
				align:'center'
			},{
				title : '姓名',
				field : 'name',
				width : 60,
				align:'center'
			}, {
				title : '时间',
				field : 'leavedate',
				width : 100,
				align:'center'
			},{
				title : '请假类型',
				field : 'leavetype',
				width : 100,
				align:'center',
				formatter:function(v,row,i){
						if(v=='0'){
							return "年休";
						}
						if(v=='1'){
							return "事假";
						}
						if(v=='2'){
							return "病假";
						}
						if(v=='3'){
							return "调休";
						}
						return v;
					}
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
			href : '${pageContext.request.contextPath}/leave/save.action',
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
				      url : "${pageContext.request.contextPath}/leave/saveDo.action",
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
						url : '${pageContext.request.contextPath}/leave/delete.action',
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
	function exportToExcel() {
		try {
			if ($("#divid").combobox('getValue') == '') {
				sy.messagerShow({
					msg : "请选择要导出的单位",
					title : '提示'
				});
				return false;
			}
		} catch (e) {
			e.message;
		}
		try{
			if($("#deptid").combobox('getText') == ''){
				$("#deptid").combobox('setValue','');
			}
		}catch(e){
			e.message;
		} 
		if ($("#startDate").val() == '') {
			sy.messagerShow({
				msg : "请选择开始日期",
				title : '提示'
			});
			return false;
		}
		if ($("#endDate").val() == '') {
			sy.messagerShow({
				msg : "请选择结束日期",
				title : '提示'
			});
			return false;
		}
		var opts = $('#datagrid').datagrid('getColumnFields');
		//alert(opts);
		var divid=$("#divid").combobox('getValue');
		$('#columns').attr('value',opts);
		$('#searchForm')
				.attr('action',
						'${pageContext.request.contextPath}/leave/exportToExcel.action');
		$('#searchForm').submit();
	}
	
</script>
</body>
</html>
