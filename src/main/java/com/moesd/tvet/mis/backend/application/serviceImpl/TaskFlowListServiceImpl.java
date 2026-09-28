package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TaskFlowListdto;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListRepository;
import com.moesd.tvet.mis.backend.application.service.TaskFlowListService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
public class TaskFlowListServiceImpl implements TaskFlowListService{
	
	private final TaskFlowListRepository taskFlowListRepository;
    private final TaskFlowListAuditRepository taskFlowListAuditRepository;
    private final ObjectToJson objectTojson;
	private final WorkTaskFlowService workTaskFlowService;
	
	@Autowired
	private ObjectMapper objectMapper;
    
	@Override
	public List<ObjectNode> getGroupTaskListDtl(Integer taskStatusId, Integer currentRoleId, String locationId) {
	    return objectTojson._toJson(
	            taskFlowListRepository.getGroupTaskListDtl(taskStatusId, currentRoleId, locationId));
	}

	@Override
	public List<ObjectNode> getMyTaskListDtl(String userId, String current_roleId) {
	    return objectTojson._toJson(taskFlowListRepository.getMyTaskListDtl(userId, current_roleId));
	}
	
	@Override
	public List<ObjectNode> getApplicationStatusAuditCurrentTaskDtl(String applicationNo) {
	    return objectTojson._toJson(
	            taskFlowListRepository.getApplicationStatusAuditCurrentTaskDtl(applicationNo));
	}

	@Override
	public ObjectNode claimTask(TaskFlowListdto request) {
	    ObjectNode response = JsonNodeFactory.instance.objectNode();

	    try {
	        workTaskFlowService.updateTaskFlow(
	            request.getApplicationNo(),
	            request.getTaskStatusId(),
	            request.getAssignedRoleId(),
	            request.getAssignedUserId(),
	            request.getRemarks()
	        );

	        response.put("status", 200);
	        response.put("message", "Task successfully claimed");

	    } catch (Exception e) {
	        response.put("status", 500);
	        response.put("message", "Failed to claim task: " + e.getMessage());
	    }

	    return response;
	}



	@Override
	public ObjectNode unclaimTask(TaskFlowListdto request) {
	    ObjectNode response = objectMapper.createObjectNode();

	    TaskFlowList taskFlow = taskFlowListAuditRepository.getInitialTask(request.getApplicationNo());

	    if (taskFlow == null) {
	        response.put("status", HttpStatus.NOT_FOUND.value());
	        response.put("message", "No initial task found for application number " + request.getApplicationNo());
	        return response;
	    }

	    workTaskFlowService.updateTaskFlow(
	            request.getApplicationNo(),
	            taskFlow.getTaskStatusId(),
	            taskFlow.getAssignedRoleId(),
	            taskFlow.getAssignedUserId(),
	            taskFlow.getSaveRemarks()
	        );

	    response.put("status", HttpStatus.OK.value());
	    response.put("message", "Task successfully unclaimed");
	    return response;
	}

	@Override
	public List<ObjectNode> getApplicationStatusDtl(String applicationNo, String applicantName,
	        String applicationDate) {
	    return objectTojson._toJson(
	            taskFlowListRepository.getApplicationStatusDtl(applicationNo, applicantName, applicationDate));
	}

}
