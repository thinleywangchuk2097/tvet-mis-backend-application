package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TaskFlowListdto;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListRepository;
import com.moesd.tvet.mis.backend.application.service.TaskFlowListService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
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
	public List<ObjectNode> getGroupTaskListDtl(Integer taskStatusId, Integer currentRoleId,String locationId) {
		List<Tuple> resultList = taskFlowListRepository.getGroupTaskListDtl(taskStatusId,currentRoleId,locationId);
		List<ObjectNode> TaskDtlsJson = objectTojson._toJson(resultList);
		return TaskDtlsJson;
	}

	@Override
	public List<ObjectNode> getMyTaskListDtl(String userId, String current_roleId) {
		List<Tuple> resultList = taskFlowListRepository.getMyTaskListDtl(userId, current_roleId);
		List<ObjectNode> TaskDtlsJson = objectTojson._toJson(resultList);
		return TaskDtlsJson;
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

}
