package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TaskFlowListdto;
import com.moesd.tvet.mis.backend.application.model.Role;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleRepository;
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
	private final RoleRepository roleRepository;
	
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
	
	private static final int ROLE_REC = 23;

	@Override
	public List<ObjectNode> getApplicationStatusAuditCurrentTaskDtl(String applicationNo) {
	    List<ObjectNode> rows = objectTojson._toJson(
	            taskFlowListRepository.getApplicationStatusAuditCurrentTaskDtl(applicationNo));
	    if (rows.isEmpty())
	        return rows;

	    // current task (tbl_task_dtls): who has the application now
	    TaskFlowList task = taskFlowListRepository.findByApplicationNo(applicationNo);
	    List<String> assignedIds = task != null ? splitIds(task.getAssignedUserId()) : List.of();
	    Integer assignedRoleId = task != null ? task.getAssignedRoleId() : null;
	    boolean withRec = Integer.valueOf(ROLE_REC).equals(assignedRoleId);
	    List<String> recDoneIds = withRec
	            ? taskFlowListRepository.findCompletedRecUserIds(applicationNo).stream().filter(Objects::nonNull)
	                    .map(String::trim).filter(id -> !id.isEmpty()).distinct().collect(Collectors.toList())
	            : List.of();

	    // one lookup for every user id that needs a name (actors + current holders + REC done)
	    Set<String> allIds = new LinkedHashSet<>();
	    rows.forEach(r -> allIds.addAll(splitIds(text(r, "actor_id"))));
	    allIds.addAll(assignedIds);
	    allIds.addAll(recDoneIds);
	    Map<String, String> names = userNames(new ArrayList<>(allIds));

	    String assignedNames = assignedIds.stream().map(id -> names.getOrDefault(id, id))
	            .collect(Collectors.joining(", "));
	    String assignedRoleName = assignedRoleId == null ? ""
	            : roleRepository.findById(assignedRoleId).map(Role::getRoleName).orElse("");

	    for (ObjectNode row : rows) {
	        row.put("actor_name", names.getOrDefault(text(row, "actor_id"), ""));

	        if ("CURRENT".equals(text(row, "record_type"))) {
	            row.put("assigned_role_id", assignedRoleId == null ? "" : String.valueOf(assignedRoleId));
	            row.put("assigned_role_name", assignedRoleName);
	            row.put("assigned_user_id",
	                    task != null && task.getAssignedUserId() != null ? task.getAssignedUserId() : "");
	            row.put("assigned_user_names", assignedNames);
	            // with REC: still pending = current holders; completed = REC rows with remarks
	            row.put("rec_pending_users", withRec ? assignedNames : "");
	            row.put("rec_completed_users", recDoneIds.stream().map(id -> names.getOrDefault(id, id))
	                    .collect(Collectors.joining(", ")));
	        }
	    }
	    return rows;
	}

	private static String text(ObjectNode row, String field) {
	    return row.hasNonNull(field) ? row.get(field).asString() : "";
	}

	private static List<String> splitIds(String csv) {
	    if (csv == null || csv.isBlank())
	        return List.of();
	    return Arrays.stream(csv.split(",")).map(String::trim).filter(id -> !id.isEmpty()).distinct()
	            .collect(Collectors.toList());
	}

	private Map<String, String> userNames(List<String> userIds) {
	    Map<String, String> names = new HashMap<>();
	    if (userIds.isEmpty())
	        return names;
	    for (Tuple t : taskFlowListRepository.findUserNamesByUserIds(userIds)) {
	        Object id = t.get("user_id");
	        Object name = t.get("name");
	        if (id != null && name != null && !String.valueOf(name).isBlank())
	            names.putIfAbsent(String.valueOf(id), String.valueOf(name));
	    }
	    return names;
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
