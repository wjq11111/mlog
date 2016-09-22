<%@ page language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/sto_jsp/include/taglib.jsp"%>
<div align="center" style="padding: 5px; overflow: hidden;">
	<form method="post">
		<input type="hidden" name="ids" id="ids" value="${ids}" />
		<table class="tableForm">
			<tr>
						<td class="label" >手机开门状态：</td>
						<td><select name="mobileopenflag"  style="width: 150px;">
							<option value="1"  <c:if test='${user.mobileopenflag==1}'>selected</c:if> >允许</option>
							<option value="0"  <c:if test='${user.mobileopenflag==0}'>selected</c:if> >禁用</option>
					</select></td>
						
					</tr>
					<tr>
						<td class="label">开始时间:</td>
						<td><input id="starttime" name="starttime" class="easyui-timespinner"
							style="width: 150px;" readonly value="${user.openstarttime}"
							data-options="showSeconds:true" /></td>
					</tr>
					<tr>
						<td class="label">结束时间:</td>
						<td><input id="endtime" name="endtime" class="easyui-timespinner"
							style="width: 150px;"  value="${user.openendtime}" 
							data-options="showSeconds:true" /></td>
					</tr>
					
				
					
		</table>
	</form>
</div>
<script type="text/javascript">
$(function() {
	
});

	
</script>