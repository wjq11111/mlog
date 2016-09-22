<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<%@ page import="sto.common.util.RoleType"%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%@ include file="/WEB-INF/sto_jsp/include/css.jsp"%>
<%@ include file="/WEB-INF/sto_jsp/include/js.jsp"%>
<script src="${ctx}/js/My97DatePicker/WdatePicker.js"
	type="text/javascript"></script>
</head>
<body class="easyui-layout" data-options="fit:true">

	<div data-options="region:'north',border:false,title:'查询条件'"
		style="height: 60px; overflow: hidden;" align="left">
		<form id="searchForm" method="post">
			<input id="columns" name="columns" type="hidden" /> <input id="width"
				name="width" type="hidden" />
			<table class="tableForm datagrid-toolbar" style="height: 100%;">
				<tr>
					<shiro:hasAnyRoles name="<%=RoleType.UNUNIT_USERGROUP.getName()%>">
						<td style="font-size: 12px; width: 20%;">单位: <input
							id="divid" name="divid" />
						</td>
					</shiro:hasAnyRoles>
					<td style="font-size: 12px; width: 20%;">部门: <input
						id="deptid" name="deptid" />
					</td>
					<%-- <td style="font-size: 12px;">开始日期：</td>
					<td><input id="startDate" name="startDate" class="easyui-datebox" data-options="editable:false" value="${startDate}" ></td>
					<td style="font-size: 12px;">结束日期：</td>
					<td><input id="endDate" name="endDate" class="easyui-datebox" data-options="editable:false" value="${endDate}"></td>
					 --%>
					<td style="font-size: 12px;">开始日期：</td>
					<td><input id="startDate" name="startDate"
						style="width: 155px;" class="Wdate"
						onFocus="WdatePicker({onpicked:function(dp){selectDate();},isShowClear:false,readOnly:true,maxDate:'#F{$dp.$D(\'endDate\');}'})"
						value="${startDate}"></td>
					<td style="font-size: 12px;">结束日期：</td>
					<td><input id="endDate" name="endDate" style="width: 155px;"
						class="Wdate"
						onFocus="WdatePicker({onpicked:function(dp){selectDate();},isShowClear:false,readOnly:true,minDate:'#F{$dp.$D(\'startDate\');}'})"
						value="${endDate}"></td>
					<td style="font-size: 12px;"><a href="javascript:void(0)"
						class="easyui-linkbutton" data-options="iconCls:'icon-search'"
						onclick="_search();">查询</a></td>
					<td style="font-size: 12px;"><a href="javascript:void(0)"
						class="easyui-linkbutton" data-options="iconCls:'icon-search'"
						onclick="exportToExcel();">导出到excel</a></td>
				</tr>
			</table>
		</form>
	</div>
	<div data-options="region:'center',border:false"
		style="overflow-y: hidden;">
		<div id="datagrid" style="overflow: auto; height: 450px;"></div>
	</div>

	<script type="text/javascript">
	function attendcardmx(date,userid) {
		        //var divid=($("#divid").combobox('getValue'));
	       		var p = sy.dialog({
				title : '打卡记录',
        	    href : '${pageContext.request.contextPath}/attend/attendcardmxList.action',
				width : 870,
				height : 400,
				buttons : [],
				onLoad : function() {
					var f = p.find('form');
					/* f.form('load', {
						date:date,
						username:username,
						divid:$("#divid").combobox('getValue'),
					}); */
					var mxdatagrid = f.find("table[id='mxdatagrid']").datagrid({
						url : '${pageContext.request.contextPath}/attend/getAttendCardListByUserid.action?date='+date+"&userid="+userid,
						/* queryParams: {
							date: rows[0].date,    
						    userid: rows[0].userid
						}, */
						height:350,
						pagination : true,
						pageSize : 10,
						pageList : [5,10,20,30,50],
						pageNumber:1,
						pagePosition : 'bottom',
						fitColumns:true,
						nowrap : true,
						border : false,
						idField : 'orderid',
						sortName : 'orderid',
						sortOrder : 'desc',
						checkOnSelect : true,
						singleSelect : true,
						columns : [ [ {
							title : 'id',
							field : 'id',
							width : 150,
							hidden : true
						},{
							title : '序号',
							field : 'orderid',
							width : 30,
							align:'center',
							formatter:function(value, row, index){
								return index+1;
							}
						}, {
							title : '打卡类型',
							field : 'type',
							width : 50,
							align:'center',
							formatter:function(v){
								if(v==1){
									return "上班";
								}
								if(v==2){
									return "下班";
								}
								return "打卡异常";
							}
							
						}, {
							title : '打卡时间',
							field : 'time',
							width : 80,
							align:'center'
						}, {
							title : '经度',
							field : 'longitude',
							width : 100,
							align:'center'
						}, {
							title : '纬度',
							field : 'latitude',
							width : 100,
							align:'center'
						}, {
							title : '打卡地址',
							field : 'address',
							width : 200,
							align:'center'
						}] ]
					});
					
			}
				
			});
		}
	
	//请假记录
	 function leavemx(stardate,enddate,userid) {
        //var divid=($("#divid").combobox('getValue'));
        
   		var p = sy.dialog({
		title : '请假记录',
	    href : '${pageContext.request.contextPath}/leave/leavemxList.action',
		width : 870,
		height : 400,
		buttons : [],
		onLoad : function() {
			var f = p.find('form');
			/* f.form('load', {
				date:date,
				username:username,
				divid:$("#divid").combobox('getValue'),
			}); */
			var mxdatagrid1 = f.find("table[id='mxdatagrid1']").datagrid({
				url : '${pageContext.request.contextPath}/leave/getLeaveListByUserid.action?stardate='+stardate+"&enddate="+enddate+"&userid="+userid,
				/* queryParams: {
					date: rows[0].date,    
				    userid: rows[0].userid
				}, */
				height:350,
				pagination : true,
				pageSize : 10,
				pageList : [5,10,20,30,50],
				pageNumber:1,
				pagePosition : 'bottom',
				fitColumns:true,
				nowrap : true,
				border : false,
				checkOnSelect : true,
				singleSelect : true,
				columns : [ [ {
					title : 'id',
					field : 'id',
					width : 150,
					hidden : true
				},{
					title : '单位',
					field : 'divid',
					width : 80,
					align:'center'
					
				}, {
					title : '部门',
					field : 'deptid',
					width : 80,
					align:'center'
										
				}, {
					title : '姓名 ',
					field : 'userid',
					width : 80,
					align:'center'
				}, {
					title : '请假时间',
					field : 'leavedate',
					width : 100,
					align:'center'
				}, {
					title : '请假类型',
					field : 'leavetype',
					width : 60,
					align:'center',
					formatter:function(v){
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
				}] ]
			});
			
	}
		
	});
} 
	
	
	function leave(value,row,index){//请假列属性方法
		var col=$("#datagrid").datagrid('getColumnFields');
	    //alert($("#startDate").val());
		if(value>0){return '<a href=\"javascript:leavemx(\''+$("#startDate").val()+'\',\''+$("#endDate").val()+'\',\''+row[col[0]]+'\')\">'+value+'</a></span>';}
		else {return value;}
		
	}
		function song(value,row,index){// 日期列属性方法
			var colnum = $("#datagrid").datagrid('getColumnFields').length;
			var col=$("#datagrid").datagrid('getColumnFields');
			
		//	debugger;
			if(value=='00:00:00'){return '<span style=\"background-color:#0fffd0;\"><a href=\"javascript:attendcardmx(\''+this.field+'\',\''+row[col[0]]+'\')\">'+value+'</a></span>';}
			else if (value <'09:00:00'){return '<span style=\"background-color:#ffff00;\"><a href=\"javascript:attendcardmx(\''+this.field+'\',\''+row[col[0]]+'\')\">'+value+'</a></span>';} 
			else { 	return '<a href=\"javascript:attendcardmx(\''+this.field+'\',\''+row[col[0]]+'\')\">'+value+'</a>' ;}
			
		}
		
		
		
		var datagrid;
		$(function() {
			$('input:text:first').focus(); //把焦点放在第一个文本框
			$('input').keypress(function(e) {
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
			$.ajax({
						type : "post",
						url : "${pageContext.request.contextPath}/attend/statisticQueryColumns.action",
						data : sy.serializeObject($('#searchForm')),
						success : function(json) {
							if (json.success) {
								$("#columns").val(JSON.stringify(json.columns));
								//$("#columns").val("[{field:name, title:姓名, width:80}, {field:normaldays, title:正常天数, width:80}, {field:baddays, title:异常天数, width:80}, {field:totledays, title:加班时长, width:80,styler:function(value,row,index){if (value < 5){return 'background-color:#ffee00;color:red;';} else {return 'background-color:#11ee00;color:blue;';}}}, {field:2015-06-01, title:2015-06-01, width:100}, {field:2015-06-02, title:2015-06-02, width:100}, {field:2015-06-03, title:2015-06-03, width:100}, {field:2015-06-04, title:2015-06-04, width:100}, {field:2015-06-05, title:2015-06-05, width:100}]");
								//$("#width").val(json.width);
								//loadDatagrid(eval("([{field:'name', title:'姓名', width:80}, {field:'normaldays', title:'正常天数', width:80}, {field:'baddays', title:'异常天数', width:80}, {field:'totledays', title:'加班时长', width:80,styler:function(value,row,index){if (value < 5){return 'background-color:#ffee00;color:red;';} else {return 'background-color:#11ee00;color:blue;';}}}, {field:'2015-06-01', title:'2015-06-01', width:100}, {field:'2015-06-02', title:'2015-06-02', width:100}, {field:'2015-06-03', title:'2015-06-03', width:100}, {field:'2015-06-04', title:'2015-06-04', width:100}, {field:'2015-06-05', title:'2015-06-05', width:100}])"), parseInt(json.width));
								//alert(JSON.stringify(json.columnsSbf));
								loadDatagrid(eval("(["+json.columnsSbf+"])"), parseInt(json.width));
							} else {
								sy.messagerShow({
									msg : json.msg,
									title : '提示'
								});
							}
						}
					});
		});
		function selectDate() {
			$.ajax({
						type : "post",
						url : "${pageContext.request.contextPath}/attend/statisticQueryColumns.action",
						data : sy.serializeObject($('#searchForm')),
						success : function(json) {
							if (json.success) {
								$("#columns").val(JSON.stringify(json.columns));
								//$("#width").val(json.width);
								//alert(JSON.stringify(json.columnsSbf));
								//loadDatagrid(eval("([{field:'name', title:'姓名', width:80}, {field:'normaldays', title:'正常天数', width:80}, {field:'baddays', title:'异常天数', width:80}, {field:'totledays', title:'加班时长', width:80,styler:function(value,row,index){if (value == '-45:0:0'){return 'background-color:#ffee00;color:red;';} else {return 'background-color:#11ee00;color:blue;';}}}, {field:'2015-06-01', title:'2015-06-01', width:100}, {field:'2015-06-02', title:'2015-06-02', width:100}, {field:'2015-06-03', title:'2015-06-03', width:100}, {field:'2015-06-04', title:'2015-06-04', width:100}, {field:'2015-06-05', title:'2015-06-05', width:100}])"), parseInt(json.width));
								loadDatagrid(eval("(["+json.columnsSbf+"])"), parseInt(json.width));
								//$("#datagrid").datagrid('options').url = "${pageContext.request.contextPath}/attend/statisticQuery.action";
								$("#datagrid").datagrid('load',
										sy.serializeObject($('#searchForm')));
									
							} else {
								sy.messagerShow({
									msg : json.msg,
									title : '提示'
								});
							}
						}
					});
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
			$('#searchForm')
					.attr('action',
							"${pageContext.request.contextPath}/attend/exportToExcel.action");
			$('#searchForm').submit();
		}
		function loadDatagrid(columns, width) {
			var datagrid = $('#datagrid')
					.datagrid(
							{
								//width:width,
								height : $(document.body).height() - 70,
								pagination : true,
								//url:"${pageContext.request.contextPath}/attend/statisticQuery.action",
								pageSize : 10,
								pageList : [ 5, 10, 20, 30, 50 ],
								pageNumber : 1,
								rownumbers : true,
								nowrap : true,
								border : false,
								idField : 'dept',
								sortName : 'dept',
								sortOrder : 'asc',
								checkOnSelect : true,
								singleSelect : true,
								remoteSort:false,
								columns : [ columns ],

								/* toolbar : [ {
								text : '导出到excel',
								iconCls : 'icon-search',
								handler : function() {
								exportToExcel();
								}
								}], */

								/* rowStyler : function(index, row) {
									var colnum = $("#datagrid").datagrid(
											'getColumnFields').length;
									//alert("colmun"+colnum);
									var rownum = $("#datagrid").datagrid(
											'getRows').length;
									//debugger;

									for (var j = 0; j < colnum; j++) {
										//alert("j="+j);
										var colName = $("#datagrid").datagrid(
												'getColumnFields')[j];
										if (row[colName] == "00:00:00") {
											return 'background-color:#55ee00;color:red;';
										}
										else
											{
											return 'background-color:#55ee00;color:blue;';
											}
									}
								}, */
															
								 onRowContextMenu : function(e, rowIndex,
										rowData) {

									e.preventDefault();
									$(this).datagrid('unselectAll');
									$(this).datagrid('selectRow', rowIndex);
									$('#menu').menu('show', {
										left : e.pageX,
										top : e.pageY
									});
								} 
							});
			scrollShow($("#datagrid"), width);
		}
		function _search() {
			try {
				if ($("#divid").combobox('getValue') == '') {
					sy.messagerShow({
						msg : "请选择要查询的单位",
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
			//数据库中字段最大允许1024列，时间字段占8位，逗号1位，所以设定最大时间不超过35天，超过会溢出
			var start=$("#startDate").val();
		    var end=$("#endDate").val();
			var starta=start.split("-");
			var enda=end.split("-");
			var start1=new Date(starta[0],starta[1]-1,starta[2]);
			var end1=new Date(enda[0],enda[1]-1,enda[2]);
		    var idays=parseInt(Math.abs(end1-start1)/1000/60/60/24)+1;
			
			if(idays>=35)
				{
				sy.messagerShow({
					msg : "日期选择超出范围",
					title : '提示'
				});
				return false;
				} 
			$("#datagrid").datagrid('options').url = "${pageContext.request.contextPath}/attend/statisticQuery.action";
			$("#datagrid").datagrid('load',
					sy.serializeObject($('#searchForm')));
			
		}
		function scrollShow(datagrid, width) {
			//datagrid.prev(".datagrid-view2").children(".datagrid-body").html("<div style='width:" + datagrid.prev(".datagrid-view2").find(".datagrid-header-row").width() + "px;border:solid 0px;height:1px;'></div>");
			datagrid
					.prev(".datagrid-view2")
					.children(".datagrid-body")
					.html(
							"<div style='width:" + width + "px;border:solid 0px;height:1px;'></div>");
		}
	</script>
</body>
</html>
