<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<script type="text/javascript">
$(function(){
	$("#divid1").combobox({
	    url:'${pageContext.request.contextPath}/unit/listJsonNoPage.action',
	    valueField:'divid',
	    textField: 'divname',
	    panelHeight:'250',
	    onSelect : function(r){
	    	$("#deptid1").combotree('clear');
	    	$("#deptid1").combotree('reload','${pageContext.request.contextPath}/dept/listJson.action?divid='+r.divid);  
	    }
	});
	$("#deptid1").combotree({
	    url: '${pageContext.request.contextPath}/dept/listJson.action',
	    valueField:'divid',
	    required: true,
	    onBeforeSelect : function(node){
	    	/* var parent = $("#deptid").combotree('tree').tree('getParent',node.target);
	    	if(parent == null){
	    		alert("请选择具体部门");
	    		return false;
	    	} */
	    	return true;
	    },
	    onBeforeLoad : function(param){
	    	if(String($("#divid1").combobox('getValue')).replace(/\s/,"") == ""){
	    		return false;
	    	}
	    	return true;
	    },
	    onSelect : function(r){
	    	
	    	$("#userid1").combobox('clear');
	    	//debugger;
	    	$("#userid1").combobox('reload','${pageContext.request.contextPath}/user/checklistJson.action?deptid='+r.id);  
	    }
	});
	 $("#userid1").combobox({
	    //url: '${pageContext.request.contextPath}/user/checklistJson.action',   
	    valueField: 'id',    
        textField: 'name' ,
        panelHeight:'250'
	});  

});
$("#unit_tr").css('display', 'none');
$("#dept_tr").css('display', 'none');
//$("#divid1+.combo").hide();
//$("#deptid1+.combo").hide();
$("#deptid").combotree('reload','${pageContext.request.contextPath}/dept/listJson.action');

</script>
<div align="center" style="padding: 5px; overflow: hidden;">
	<form method="post">
		<table class="tableForm">
			 <!--   <tr>
				<td class="label-required" >
					编号：
				</td>
				<td>
					<input id="festid" name="festid" style="width: 250px;"
						autocomplete='off' class="easyui-validatebox"
						data-options="required:true,missingMessage:'请填写编码'" />
				</td>
			</tr> -->
			<tr > 
				<td  style="font-size: 12px;float:right;">
					所属单位
				</td>
				<td>
					<input id="divid1" name="divid1" style="width: 255px;" autocomplete='off'>
				</td>
			</tr>
			<tr> 
				<td  style="font-size: 12px;float:right;">
					部门
				</td>
				<td>
					<input id="deptid1" name="deptid1" style="width: 255px;" autocomplete='off'>
				</td>
			</tr>
			<tr> 
				<td  style="font-size: 12px;float:right;">
				  人员
				</td>
				<td>
					<input id="userid1" name="userid1" style="width: 255px;" autocomplete='off'>
				</td>
			</tr>
			<tr>
			<td style="font-size: 12px;float:right;">日期</td>
					<td><input id="leavedate" name="leavedate" style="width: 255px;" class="easyui-datebox" data-options="editable:false" value="${startDate}" ></td>
			</tr>
			<tr>
				<td style="font-size: 12px;float:right;">
					请假类型	
				</td>
				<td>
					<select id="leavetype" class="easyui-combobox" name="leavetype" style="width:255px;">   
                           <option value="0">年休</option>   
                           <option value="1">事假</option>   
                           <option value="2">病假</option>   
                           <option value="3">调休</option>   
                             
                   </select>  

				</td>
			</tr>
		</table>
	</form>
</div>
