package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TaskFlowListdto;
import com.moesd.tvet.mis.backend.application.service.TaskFlowListService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/tasklist")
public class TaskFlowListController {
	
	private final TaskFlowListService taskFlowListService;

	@GetMapping("/get-group-tasklist-dtl/{taskStatusId}/{currentRoleId}/{locationId}")
	public ResponseEntity<List<ObjectNode>> getGroupTaskListDtl(@PathVariable Integer taskStatusId,
			@PathVariable Integer currentRoleId, @PathVariable(required = false) String locationId) {
		return new ResponseEntity<List<ObjectNode>>(
				taskFlowListService.getGroupTaskListDtl(taskStatusId, currentRoleId, locationId), HttpStatus.OK);
	}

	@GetMapping("/get-my-tasklist-dtl/{userId}/{current_roleId}")
	public ResponseEntity<List<ObjectNode>> getMyTaskListDtl(@PathVariable String userId, @PathVariable String current_roleId) {
		return new ResponseEntity<List<ObjectNode>>(taskFlowListService.getMyTaskListDtl(userId, current_roleId), HttpStatus.OK);
	}

	
	@PostMapping("/claim-task")
	public ResponseEntity<ObjectNode> claimTask(@RequestBody TaskFlowListdto request) {
	    ObjectNode result = taskFlowListService.claimTask(request);
	    return ResponseEntity.status(result.get("status").asInt()).body(result);
	}
	
	
	@PostMapping("/unclaim-task")
	public ResponseEntity<ObjectNode> unclaimTask(@RequestBody TaskFlowListdto request) {
	    ObjectNode result = taskFlowListService.unclaimTask(request);
	    return ResponseEntity.status(result.get("status").asInt()).body(result);
	}
}
