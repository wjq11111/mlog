<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<style type="text/css">
.green {
	float:left;
	text-align:center;
	vertical-align: middle;
    padding:10px; 
    width:150px; 
    height:15px;
    background:#80ff00;
    margin-left:-37px;
    border: 0;
    z-index:1;
}
.yellow {
	float:left;
	text-align:center;
	vertical-align: middle; 
    padding:10px; 
    width:150px; 
    height:15px;
    background:#ffff00;
    border: 0;
    margin-left:-37px;
    z-index:1;
}
.grey {
	float:left;
	text-align:center;
	vertical-align: middle;
    padding:10px; 
    width:150px; 
    height:15px;
    background:#c0c0c0;
    margin-left:-37px;
    z-index:1;
}
div{
	align:center;
}
 
.yellow1 {
  border: 17px solid transparent;
  border-left: 20px solid #ffff00;
  width: 0;
  height: 0px;
  float:left;
  z-index:2;
  position:relative;
}
.green1 {
  border: 17px solid transparent;
  border-left: 20px solid #80ff00;
  width: 0;
  height: 0px;
  float:left;
  padding:1px;
  z-index:2;
  position:relative;
}
.grey1 {
  border: 17px solid transparent;
  border-left: 20px solid #c0c0c0;
  width: 0;
  height: 0px;
  float:left;
  position:relative;
  z-index:2;
}
</style>

</style>

<div style="width:600px;overflow:hidden;">
	<div style="width:550px;height:50px;margin-top:10px;margin-left:50px;">
		<c:choose><c:when test="${step==0}"><div id="step0" class="yellow">单位设置</div><div id="step00" class="yellow1"></div></c:when><c:when test="${step>0}"><div id="step0" class="green">单位设置</div><div id="step00" class="green1"></div></c:when><c:otherwise><div id="step0" class="grey">单位设置</div><div id="step00" class="grey1"></div></c:otherwise></c:choose>
		<c:choose><c:when test="${step==1}"><div id="step1" class="yellow">部门设置</div><div id="step11" class="yellow1"></div></c:when><c:when test="${step>1}"><div id="step1" class="green">部门设置</div><div id="step11" class="yellow1"></div></c:when><c:otherwise><div id="step1" class="grey">部门设置</div><div id="step11" class="grey1"></div></c:otherwise></c:choose>
		<c:choose><c:when test="${step==2}"><div id="step2" class="yellow">增加员工</div><div id="step22" class="yellow1"></div></c:when><c:when test="${step>2}"><div id="step2" class="green">增加员工</div><div id="step22" class="green1"></div></c:when><c:otherwise><div id="step2" class="grey">增加员工</div><div id="step22" class="grey1"></div></c:otherwise></c:choose>
	</div>
	<div id="content" style="width:600px;height:300px;display:none">
		<div id="datagrid" style="width:600px;height:500px;"></div>
	</div>
	<div id="content1" style="width:600px;height:300px;display:none">
		<div id="datagrid1" style="width:600px;height:500px;"></div>
	</div>
	<div id="content2" style="width:600px;height:300px;display:none">
		<div id="datagrid2" style="width:600px;height:500px;"></div>
	</div>
	<div id="button" style="float:right;padding-top:2px;margin-right:10px;width:100px;height:40px;margin-top:75px">
	<a id="next" href="javascript:next();" class="easyui-linkbutton">下一步</a>
	</div>
</div>
<script type="text/javascript">
var step = 0;
var curstep = 0;
<%
String step = String.valueOf(request.getAttribute("step"));
String divid1 = String.valueOf(request.getAttribute("divid"));
%> 

var divid=${divid};

$(function(){
	//alert("step"+step);
	step = parseInt('<%=step%>');
	changeClass(step);
	init(step);
	curstep = step;
});
function next(){
	
	//alert(curstep);
	if(save(curstep)){//保存上一步
		changeClass(curstep);
		curstep++;
		if(curstep>=3){
			//alert("curstep"+curstep);
			curstep=3;	
			$("#init").dialog('close');
		}else {
			//alert("curstep="+curstep);
			init(curstep);
			changeClass(curstep);
		}
	};
	
}
function changeClass(step){
	$('#step0').removeClass();
	$('#step1').removeClass();
	$('#step2').removeClass();
	$('#step00').removeClass();
	$('#step11').removeClass();
	$('#step22').removeClass();
	if(step == 0){
		$('#step0').addClass('yellow');
		$('#step1').addClass('grey');
		$('#step2').addClass('grey');
		$('#step00').addClass('yellow1');
		$('#step11').addClass('grey1');
		$('#step22').addClass('grey1');
		$('#content').css('display','block');
		$('#content1').css('display','none');
		$('#content2').css('display','none');
	}else if(step == 1){
		$('#step0').addClass('green');
		$('#step1').addClass('yellow');
		$('#step2').addClass('grey');
		$('#step00').addClass('green1');
		$('#step11').addClass('yellow1');
		$('#step22').addClass('grey1');
		$('#content').css('display','none');
		$('#content1').css('display','block');
		$('#content2').css('display','none');
	}else if(step == 2){
		document.getElementById("next").innerHTML=" 完成";
		$('#step0').addClass('green');
		$('#step1').addClass('green');
		$('#step2').addClass('yellow');
		$('#step00').addClass('green1');
		$('#step11').addClass('green1');
		$('#step22').addClass('yellow1');
		$('#content').css('display','none');
		$('#content1').css('display','none');
		$('#content2').css('display','block');
	}
	
}
function save(step){
	//alert(step);
	if(step == 0){
		//alert("0-单位 "+step);
		var rows = $("#datagrid").datagrid('getRows');
		var params=[];
		for(var i=0;i<rows.length;i++){
			/* alert(rows[i].id);
			alert(rows[i].skey);
			alert(rows[i].value);
			alert(rows[i].divid); */
			param={};
			param.id=rows[i].id;
			param.value=rows[i].value;
			
			param.divid=divid;
			params.push(param);
		}
		$.ajax({
		      type : "post",
		      url : "${pageContext.request.contextPath}/unitsettings/updateDo.action",
		      data : {params:JSON.stringify(params)},
		      success : function(json){
		    	  $.post("${pageContext.request.contextPath}/unit/updateInitStatus.action",{step:step+1});
		    	  
		      }
		});
		return true;
	}else if(step == 1){
		//alert("1-部门 "+step);
		$("#datagrid1").datagrid('acceptChanges');
		var rows = $("#datagrid1").datagrid('getRows');
		//alert("部门rows"+rows);
		var params=[];
		var deptids = "";
		for(var i=0;i<rows.length;i++){
			//alert("部门id rows"+rows[i].deptid);
			//alert("部门名称 rows"+rows[i].deptname);
			param={};
			if(String(rows[i].deptid).replace(/(^\s*)|(\s*$)/g,'') != '' && String(rows[i].deptname).replace(/(^\s*)|(\s*$)/g,'') != '' && String(rows[i].orderid).replace(/(^\s*)|(\s*$)/g,'') != ''){
				/* if(deptids.indexOf(rows[i].deptid)){
					alert("部门编号重复");
					return false;
				} */
				deptids += rows[i].deptid+",";
				param.deptid=rows[i].deptid;
				param.deptname=rows[i].deptname;
				param.orderid=rows[i].orderid;
				params.push(param);
				/* alert("param.deptid "+param.deptid);
				alert("param.deptname"+param.deptname);
				alert("param.orderid"+param.orderid); */
			}else
				{
				alert("请补充完整部门信息");
				return false;
				}
		}
		$.ajax({
		      type : "post",
		      url : "${pageContext.request.contextPath}/dept/batchSaveDo.action",
		      data : {params:JSON.stringify(params)},
		      success : function(json){
		    	  $.post("${pageContext.request.contextPath}/unit/updateInitStatus.action",{step:step+1});
		    	  
		      }
		});
		return true;
	}else if(step==2){
		//alert("2-用户 "+step);
		$("#datagrid2").datagrid('acceptChanges');
		var rows = $("#datagrid2").datagrid('getRows');
		var params=[];
		for(var i=0;i<rows.length;i++){
			param={};
			if(String(rows[i].username).replace(/(^\s*)|(\s*$)/g,'') != '' 
					&& String(rows[i].name).replace(/(^\s*)|(\s*$)/g,'') != ''
					&& String(rows[i].identitycard).replace(/(^\s*)|(\s*$)/g,'') != ''
					&& String(rows[i].mobilephone).replace(/(^\s*)|(\s*$)/g,'') != ''
					&& String(rows[i].deptid).replace(/(^\s*)|(\s*$)/g,'') != ''){
				param.username=rows[i].username;
				param.name=rows[i].name;
				param.identitycard=rows[i].identitycard;
				param.mobilephone=rows[i].mobilephone;
				param.deptid=rows[i].deptid;
			}else
				{
				alert("请补充完整用户信息");
				return false;
				}
			
			params.push(param);
		}
		$.ajax({
		      type : "post",
		      url : "${pageContext.request.contextPath}/user/batchSaveDo.action",
		      data : {params:JSON.stringify(params)},
		      success : function(json){
		    	  $.post("${pageContext.request.contextPath}/unit/updateInitStatus.action",{step:step+1});
		    	 
		      }
		});
		return true;
	}else {
		$.post("${pageContext.request.contextPath}/unit/updateInitStatus.action",{step:step});
		$("#init").dialog('close');
	}
	
}
function init(step){
	//alert(divid);
	if(0 == step){
			var datagrid = $('#datagrid').datagrid({
			url : '${pageContext.request.contextPath}/unitsettings/listJson.action',
			queryParams: {
				//divid: '', 
				divid:divid,
			    flag: 0
			},
			pagination : true,
			pageSize : 10,
			height:370,
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
			onClickCell : onClickCell,
			columns : [ [ {
				title : '唯一项',
				field : 'id',
				width : 20,
				sortable : true,
				hidden : true
			}, {
				title : '序号',
				field : 'orderid',
				width : 30,
				halign:'center',
				align:'right',
				formatter:function(value, row, index){
					return index+1;
				}
			},{
				title : '设置项',
				field : 'name',
				width : 200,
				halign:'center',
				align:'left'
			}, {
				title : '唯一标识',
				field : 'skey',
				width : 50,
				halign:'center',
				align:'left'
			},{
				title : '设置值',
				field : 'value',
				width : 100,
				halign:'center',
				align:'left',
				editor:{
					type:'text'
				}
			}] ]
		});
	
		$.extend($.fn.datagrid.defaults.editors, {
			text: {
				init: function(container, options){
		            var input = $('<input type="text" class="datagrid-editable-input">').appendTo(container);
		            return input;
		        },
		        destroy: function(target){
		            $(target).remove();
		        },
		        getValue: function(target){
		           return $(target).val();
		        },
		       setValue: function(target, value){
		            $(target).val(value);
		        },
		       resize: function(target, width){
		           $(target)._outerWidth(width);
		       }
		    }
		});
		 $.extend($.fn.datagrid.methods, {
		     editCell: function(jq,param){
		         return jq.each(function(){
		             var opts = $(this).datagrid('options');
		             var fields = $(this).datagrid('getColumnFields',true).concat($(this).datagrid('getColumnFields'));
		             for(var i=0; i<fields.length; i++){
		                 var col = $(this).datagrid('getColumnOption', fields[i]);
		                 col.editor1 = col.editor;
		                 if (fields[i] != param.field){
		                     col.editor = null;
		                }
		             }
		            $(this).datagrid('beginEdit', param.index);
		             for(var i=0; i<fields.length; i++){
		                 var col = $(this).datagrid('getColumnOption', fields[i]);
		             col.editor = col.editor1;
		         }
		     });
		 }
		});
	
		var editIndex = undefined;
		function endEditing(){
		    if (editIndex == undefined){return true}
		    if (datagrid.datagrid('validateRow', editIndex)){
		    	datagrid.datagrid('endEdit', editIndex);
		        editIndex = undefined;
		        return true;
		    } else {
		        return false;
		    }
		}
	
		function onClickCell(index, field){
			if (endEditing()){
				datagrid.datagrid('selectRow', index).datagrid('editCell', {index:index,field:field});
				editIndex = index;
			}
		}
	}else if(1 == step){
		var datagrid1 =  $('#datagrid1').datagrid({
			fit:true,
			height:450,
			fitColumns:true,
			rownumbers:true,
			pagination:false,
			method:'post',
		    columns:[[    
		        {field:'deptid',title:'部门编码',width:20,editor:{type:'text'}},    
		        {field:'deptname',title:'部门名称',width:30,editor:{type:'text'}},    
		        {field:'orderid',title:'顺序',width:30,editor:{type:'text'}} 
		    ]],
		    onClickRow:datagrid_onClickRow1,
		    toolbar: [{
				iconCls: 'icon-add',
				handler: function(){
					datagrid1.datagrid('appendRow', {
						deptid : '',
						deptname : '',
						orderid : ""
					});

					var rows = datagrid1.datagrid('getRows'); 
					datagrid1.datagrid('beginEdit', rows.length - 1); 

				}
			}]
	
		});
		function datagrid_onClickRow1(rowIndex) {
			for (var i = 0; i < rowIndex; i++) {
				datagrid1.datagrid('unselectRow', i);
			}
			datagrid1.datagrid('selectRow', rowIndex);
			for (var i = rowIndex + 1; i < datagrid1.datagrid(
					'getData').total; i++) {
				datagrid1.datagrid('unselectRow', i);
			}
			datagrid1.datagrid('beginEdit', rowIndex);
		}
	}else if(2 == step){
		var datagrid2 =  $('#datagrid2').datagrid({
			fit:true,
			height:350,
			fitColumns:true,
			rownumbers:true,
			pagination:false,
			method:'post',
		    columns:[[    
		        {field:'username',title:'用户名',width:10,editor:{type:'text'}},    
		        {field:'name',title:'用户中文名',width:20,editor:{type:'text'}},    
		        {field:'identitycard',title:'身份证号码',width:20,editor:{type:'text'}
                },
		        {field:'mobilephone',title:'手机号',width:20,editor:{type:'validatebox'  
                  }},
		        {field:'deptid',title:'所属部门',width:20,
		        	editor:{
		        		type:'combotree',
		        		options:{
		        			editable:false,
		        		
		        			url:'${pageContext.request.contextPath}/dept/listJson.action?divid='+divid
		        		}
		       		}
		        } 
		    ]],
		    onClickRow:datagrid_onClickRow2,
		    toolbar: [{
				iconCls: 'icon-add',
				handler: function(){
					datagrid2.datagrid('appendRow', {
						username : '',
						name : '',
						identitycard : "",
						mobilephone : "",
						deptid:""
					});
				}
			}]
	
		});
	
		function datagrid_onClickRow2(rowIndex) {
			for (var i = 0; i < rowIndex; i++) {
				datagrid2.datagrid('unselectRow', i);
			}
			datagrid2.datagrid('selectRow', rowIndex);
			for (var i = rowIndex + 1; i < datagrid2.datagrid(
					'getData').total; i++) {
				datagrid2.datagrid('unselectRow', i);
			}
			datagrid2.datagrid('beginEdit', rowIndex);
		}
	}
}
</script>
