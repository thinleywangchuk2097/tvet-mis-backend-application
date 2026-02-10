package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TaskFlowListdto;

public interface TaskFlowListService {
	
	List<ObjectNode> getGroupTaskListDtl(Integer taskStatusId, Integer currentRoleId,String locationId);
	
	List<ObjectNode> getMyTaskListDtl(String userId, String current_roleId);
	
	ObjectNode claimTask(TaskFlowListdto request);
	
	ObjectNode unclaimTask(TaskFlowListdto request);
}
