<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<%@ page import="sto.common.util.RoleType"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/_static/css/common.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/_static/css/list.css">
	<%@ include file="/WEB-INF/sto_jsp/include/css.jsp"%>
	<%@ include file="/WEB-INF/sto_jsp/include/js.jsp"%>
</head>
<body class="easyui-layout" data-options="fit:true">

<div data-options="region:'north',border:false,title:'查询条件'"
		style="height: 60px; overflow: hidden;">
		<form id="searchForm">
			<table class="tableForm datagrid-toolbar"
				style="width：100%;height: 100%;">
				<tr>
				<shiro:hasAnyRoles name="<%=RoleType.UNUNIT_USERGROUP.getName()%>">
						<td style="font-size: 12px; width: 16%;">单位: <input
							id="divid" name="divid" />
						</td>
					</shiro:hasAnyRoles>
					<td style="font-size: 12px; width: 16%;">部门: <input
						id="deptid" name="deptid" />
					</td>
					<!-- <td style="font-size: 12px;style="width: 20%;">姓名:</td>
					<td><input id="name" name="name" /></td> -->
					<td style="font-size: 12px;">开始日期：</td>
					<td><input id="startDate" name="startDate" class="easyui-datebox" data-options="editable:false" value="${startDate}" "></td>
					<td style="font-size: 12px;">结束日期：</td>
					<td><input id="endDate" name="endDate" class="easyui-datebox" data-options="editable:false" value="${endDate}"></td>
					<td style="font-size: 12px;">
						<input name="text" style="display:none">
						<a href="javascript:void(0)" class="easyui-linkbutton" data-options="iconCls:'icon-search'" onclick="_search();">查询</a>
					</td>
					<td style="font-size: 12px;"><a href="javascript:void(0)"
						class="easyui-linkbutton" data-options="iconCls:'icon-search'"
						onclick="exportToWord();">导出到word</a></td>
				</tr>
			</table>
		</form>
	</div>
	<div data-options="region:'center',border:false"  style="overflow: auto;display: block">
		<table id="datagrid" style="overflow: hidden;"></table>
	</div>
<script type="text/javascript" >
	var datagrid;
	$(function() {
		/* $(window).resize(function () {
            $('#datagrid').datagrid('resize', {
                width: $(document.body).width() - 10,
                height: $(document.body).height() - 200
            }).datagrid('resize', {
                width: $(document.body).width() - 10,
                height: $(document.body).height() - 200
            });
	    });  */
	   // alert($(document.body).width());
	  /*   $(window).resize(function(){
	    	 $('#datagrid').datagrid('resize', {
	    		width:function(){return $(document.body).width();},
	    	 	height:function(){return $(document.body).height()-200;}
	    	 });
	    });
	    $("#datagrid").show();*/
		$('input:text:first').focus(); //把焦点放在第一个文本框
		$('input').keypress(function (e) { 
		    if (e.which == 13) {//回车
		        _search();
		    }
		}); 
	
		
		try {
			$("#divid").combobox(
					{
						url : '${ctx}/unit/listJsonNoPage.action',
						valueField : 'divid',
						textField : 'divname',
						panelHeight : '250',
						onSelect : function(r) {
							$("#deptid").combotree('clear');
							$("#deptid").combotree(
									'reload',
									'${ctx}/dept/listJson.action?divid='
											+ r.divid);
						},
						onChange : function(n, o) {
							if (n == '' || n == 'undefined') {
								$("#divid").combobox('reset');
							}
						}
					});
		} catch (e) {
			e.message;
		}
		try {
			$("#deptid").combotree({
				url : '${ctx}/dept/listJson.action',
				editable : true,
				onBeforeSelect : function(node) {
					/* var parent = $("#deptid").combotree('tree').tree('getParent',node.target);
					if(parent == null){
						alert("请选择具体部门");
						return false;
					} */
					
					return true;
				},
				onBeforeLoad : function(param) {
					$("#deptid").combotree('clear');
				},
				onChange : function(n, o) {
					if (n == '' || n == 'undefined') {
						$("#deptid").combotree('reset');
						
					}
				}
			});
		} catch (e) {
			e.message;
		}
	
		datagrid = $('#datagrid').datagrid({
			url : '${pageContext.request.contextPath}/journal/getJournalAllList.action',
			queryParams: {
				//name: $('#name').val(),    
			    startDate: $('#startDate').datebox('getValue'),
			    endDate:$('#endDate').datebox('getValue'),
			   
			},
			height:$(document.body).height()-70,
			resize : true,
			pagination : true,
			pageSize : 10,
			pageList : [5,10,20,30,50],
			pageNumber:1,
			nowrap : true,
			border : false,
			idField : 'id',
			sortName : 'id',
			//sortOrder : 'desc',
			checkOnSelect : true,
			singleSelect : true,
			columns : [ [ {
				title : '编号',
				field : 'id',
				hidden : true
			},{
				field : 'userid',
				hidden : true
			}, {
				title : '序号',
				field : 'orderid',
				width : 30,
				align:'center',
				formatter:function(value, row, index){
					return index+1;
				}
			}, {
				title : '用户名',
				field : 'username',
				width : 120,
				align:'center',
				hidden :true
			}, {
				title : '部门',
				field : 'deptid',
				width : 80,
				align:'center'
									
			}, {
				title : '姓名',
				field : 'name',
				width : 120,
				align:'center'
			}, {
				title : '日志内容',
				field : 'content',
				width : 620,
				align:'left',
				formatter:function(v,row,i){
					var value = v;
					if(v.length>40){
						value = v.substring(0,40)+"......";
					}
					if(row.iswarn == 1){
						value = "<font style='color:red;'>"+value+"<img src='${ctx}/_static/images/warn.gif' style='width:20px;height:20px' /></font>";
					}else if(row.flag == 1){
						value = "<font style='color:red;'>"+value+"</font>";
					}
					return '<a href="javascript:void(0);" onclick="queryReply(\''+row.id+'\');">'+value+'</a>';
				}
			}, {
				title : '日志图片',
				field : 'image',
				width : 120,
				align:'center',
				formatter : function(v, rowData,r) {
					var path = 'http://'+window.location.host+'${pageContext.request.contextPath}/'+'logimages/';	
					if (rowData.image == 'null' || rowData.image=='' || rowData.image.size<=0)
						return "无";
					else{
						return '<a href="javascript:void(0);" onclick="window.open (\''+path+rowData.image+'\',\'_blank\',\'height=500,width=500,top=100,left=200,toolbar=no,menubar=no,scrollbars=yes, resizable=yes,location=no, status=no\') ;">查看</a>';
					}						
				}
			}, {
				title : '填写日期',
				field : 'time',
				width : 120,
				align:'center'
			}] ],
			onRowContextMenu : function(e, rowIndex, rowData) {
				e.preventDefault();
				$(this).datagrid('unselectAll');
				$(this).datagrid('selectRow', rowIndex);
				$('#menu').menu('show', {
					left : e.pageX,
					top : e.pageY
				});
			},
			onBeforeLoad : function(param){
				window.parent.checkWarn();
			}
		});
		
	

	});
	function queryReply(id) {
		/* var p = sy.dialog({
			title : '查看回复',
			href : '${pageContext.request.contextPath}/journal/journalReplyList.action?journalid='+id,
			width : 870,
			height : 500,
			closed: false,
			buttons : [],
			onLoad : function() {		
			}
			
		}); */
		$.ajax({
    		type : 'post',
    		url : '${pageContext.request.contextPath}/journal/resetWarnStatus.action?journalid='+id,
    	
    		success : function(json){
    			datagrid.datagrid('reload');
    		}
    	});
		$('#reply').dialog({    
		    title: '查看回复',    
		    width: 600,    
		    height: 350,    
		    closed: false,    
		    cache: false,
		    modal: true,
		    href: '${pageContext.request.contextPath}/journal/journalReplyList.action?journalid='+id
		});    
	}
	
	function _search() {
	
		$("#datagrid").datagrid('options').url = "${pageContext.request.contextPath}/journal/getJournalAllList.action";
		datagrid.datagrid('load', sy.serializeObject($('#searchForm')));
	}
	function exportToWord() {
		
		$('#searchForm')
				.attr('action',
						"${pageContext.request.contextPath}/journal/exportToWord.action");
		$('#searchForm').submit();
	}
</script>
<div id="reply"></div>  
</body>
</html>
