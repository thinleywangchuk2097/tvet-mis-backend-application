package com.moesd.tvet.mis.backend.application.service;

import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;

public interface WorkTaskFlowService {
	
	WorkFlowList createWorkflow(String applicationNo, String appName, Integer serviceId, Integer statusId,
			Integer roleId, Integer actorId, String remarks);

	TaskFlowList createTaskFlow(String applicationNo, Integer taskStatusId, String assignedRoleId,
			WorkFlowList workflow, String remarks, Integer locationId);

	WorkFlowList updateWorkflow(String applicationNo, Integer statusId, Integer roleId, Integer actorId,
			String remarks, Integer serviceId, Integer updatedBy);

	TaskFlowList updateTaskFlow(String applicationNo, Integer taskStatusId, String assignedRoleId,String assignedUserId, String remarks);

}
