<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<div align="center" style="padding: 5px; overflow: hidden;">
	<form method="post">
		<input type="hidden" name="id" value="${o.id}" />
		<table class="tableForm">
			<tr>
				<td class="label-required">
					门禁序列号：
				</td>
				<td>
					<input id="id" name="id" style="width: 250px;"
						autocomplete='off' class="easyui-validatebox"  value="${o.id}" readonly
						 />
				</td>
			</tr>
			<tr>
				<td class="label-required">
					控制器序列号：
				</td>
				<td>
					<input id="controlid" name="controlid" style="width: 250px;"
						autocomplete='off' class="easyui-validatebox"  value="${o.controllerid}" readonly
						data-options="required:true,missingMessage:'请填写控制器序列号'" />
				</td>
			</tr>
			<tr>
				<td class="label-required">
					端口号：
				</td>
				<td>
					<input id="portid" name="portid" style="width: 250px;" class="easyui-validatebox"
						data-options="required:true" autocomplete='off' readonly value="${o.portid}">
				</td>
			</tr>
			
			<tr>
				<td class="label-required">
					门禁名称：
				</td>
				<td>
					<input id="doorname" name="doorname" style="width: 250px;"
						autocomplete='off' class="easyui-validatebox" value="${o.doorname}"
						data-options="required:true,missingMessage:'请填写门禁标识'" />
				</td>
			</tr>
	       <tr>
				<td class="label">
					状态：
				</td>
				<td>
					<select name="isenable"  style="width: 255px;">
							<option value="1">启用</option>
							<option value="0">已禁用</option>
					</select>
				</td>
			</tr>
		</table>
	</form>
</div>
<script type="text/javascript">
/* Array.prototype.contains = function(b){
	boolean iscontain = false;
	for (var i = 0; i < this.length; i++){
		if(this[i] == b){
			iscontain = true;
			break;
		}
    }
	return iscontain;
}; */
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
	    	/* if(String($("#divid").combobox('getValue')).replace(/\s/,"") == ""){
	    		return false;
	    	}
	    	return true; */
	    }
	});

	var deptval = $("#deptid1").combotree('getValue');
	$("#deptid1").combotree('clear');
	$("#deptid1").combotree('reload','${pageContext.request.contextPath}/dept/listJson.action?divid='+$("#divid1").combobox('getValue'));  
	$("#deptid1").combotree('setValue',deptval);
	
	$("#roleid").combobox({
	    url:'${pageContext.request.contextPath}/role/listJsonNoPage.action',
	    valueField:'id',
	    textField: 'name',
	    panelHeight:'auto',
	    onSelect : function(r){
	    	$.post("${pageContext.request.contextPath}/role/isUnitgroup.action",{id:r.id},function(json){
	    		if(json.success){
		    		$("#unit_tr").css('display', 'block');
		    		$("#dept_tr").css('display', 'block');
		    		$("#divid1+.combo").show();
		    		$("#deptid1+.combo").show();
		    		$('#divid1').combobox({ required: true });
		    		$('#deptid1').combotree({ required: true });
		    	}else {
		    		$("#unit_tr").css('display', 'none');
		    		$("#dept_tr").css('display', 'none');
		    		$("#divid1+.combo").hide();
		    		$("#deptid1+.combo").hide();
		    		$('#divid1').combobox({ required: false });
		    		$('#deptid1').combotree({ required: false });
		    		$('#divid1').combobox('setValue','');
		    		$('#deptid1').combotree('setValue','');
		    	}
	    	});
	    	
	    },
	    onLoadSuccess:function(data){
	    	var id = $('#roleid').combobox('getValue');
	    	$.post("${pageContext.request.contextPath}/role/isUnitgroup.action",{id:id},function(json){
	    		if(json.success){
		    		$("#unit_tr").css('display', 'block');
		    		$("#dept_tr").css('display', 'block');
		    		$("#divid1+.combo").show();
		    		$("#deptid1+.combo").show();
		    		$('#divid1').combobox({ required: true });
		    		$('#deptid1').combotree({ required: true });
		    	}else {
		    		$("#unit_tr").css('display', 'none');
		    		$("#dept_tr").css('display', 'none');
		    		$("#divid1+.combo").hide();
		    		$("#deptid1+.combo").hide();
		    		$('#divid1').combobox({ required: false });
		    		$('#deptid1').combotree({ required: false });
		    		$('#divid1').combobox('setValue','');
		    		$('#deptid1').combotree('setValue','');
		    	}
	    	});
	    }
	});
});
</script>