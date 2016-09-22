<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<div align="center" style="padding: 5px; overflow: hidden;">
	<form method="post">
		<input type="hidden" name="id" value="${door.id}" />
		<table class="tableForm">
			<tr>
				
				<td class="label" colspan="2" ><input type="checkbox"
					id="closeflag" name="closeflag">锁定时间：（若取消勾选则默认全天开启）</td>

			</tr>
			<tr>
				<td class="label" width="80" >从：</td>
				<td align="left"><input id="starttime" name="starttime" 
					class="easyui-timespinner" style="width: 80px;" required="required"
					value="${door.regularstarttime}" data-options="showSeconds:true" /></td>
			</tr>
			<tr>
				<td class="label">到：</td>
				<td align="left"><input id="endtime" name="endtime"
					class="easyui-timespinner" style="width: 80px;" required="required" 
					value="${door.regularendtime}" data-options="showSeconds:true" /></td>
			</tr>

			<!-- <tr>
						<td class="label">重复</td>
						<td><select id="cc" class="easyui-combobox" name="cc" 
							style="width: 150px;">
								<option value="0">只一次</option>
								<option value="1">周一至周五</option>
								<option value="2">每天</option>
								
						</select></td>
					</tr> -->
			<tr>
				<td class="label" colspan="2" align="left" ><font color="red">注：以上时间之外默认为开启时间！</font></td>
			</tr>

		</table>
	</form>
</div>
<script type="text/javascript">
$(function() {
	//alert(111);
	
	 var checkvalue=${door.regularflag};
	
	//alert(checkvalue);
	
	if(checkvalue=='1'){
		document.getElementById("closeflag").checked = true;
	     //var ccvalue=${door.regularid};
	    /* if(ccvalue!=null && ccvalue!=""){
	    	document.getElementById("cc").options[ccvalue].selected = true;
	    	}  */
	} 
	  
});

	
</script>