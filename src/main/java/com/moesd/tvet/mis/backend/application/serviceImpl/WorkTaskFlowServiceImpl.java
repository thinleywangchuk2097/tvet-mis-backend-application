package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import com.moesd.tvet.mis.backend.application.model.TaskFlowListAudit;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.model.WorkFlowListAudit;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListRepository;
import com.moesd.tvet.mis.backend.application.repository.WorkFlowListAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.WorkFlowListRepository;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkTaskFlowServiceImpl implements WorkTaskFlowService{
	
	@Autowired
	private WorkFlowListAuditRepository workFlowListAuditRepository;
	@Autowired
	private TaskFlowListAuditRepository taskFlowListAuditRepository;
	
	private final WorkFlowListRepository workFlowListRepository;
	
	private final TaskFlowListRepository taskFlowListRepository;

	@Override
	public WorkFlowList createWorkflow(String applicationNo, String appName, Integer serviceId, Integer statusId,
			Integer roleId, Integer actorId, String remarks) {
		WorkFlowList workflow = WorkFlowList.builder()
                .applicationNo(applicationNo)
                .applicationName(appName)
                .serviceId(serviceId)
                .statusId(statusId)
                .roleId(roleId)
                .actorId(actorId)
                .wfRemarks(remarks)
                .actionDate(new Date())
                .build();

        return workFlowListRepository.save(workflow);
	}

	@Override
	public TaskFlowList createTaskFlow(String applicationNo, Integer taskStatusId, String assignedRoleId,
			WorkFlowList workflow, String remarks, Integer locationId) {
		TaskFlowList taskFlow = TaskFlowList.builder()
                .applicationNo(applicationNo)
                .taskStatusId(taskStatusId)
                .assignedRoleId(assignedRoleId)
                .workFlow(workflow)
                .saveRemarks(remarks)
                .locationId(locationId)
                .actionDate(new Date())
                .build();

        return taskFlowListRepository.save(taskFlow);
	}

	@Override
	public WorkFlowList updateWorkflow(String applicationNo, Integer statusId, Integer roleId, Integer actorId,
			String remarks, Integer serviceId, Integer updatedBy) {
		
			WorkFlowList workflow = workFlowListRepository.findByApplicationNo(applicationNo);
		    saveWorkflowAudit(workflow);

	        workflow.setStatusId(statusId);
	        workflow.setRoleId(roleId);
	        workflow.setActorId(actorId);
	        workflow.setWfRemarks(remarks);
	        workflow.setServiceId(serviceId);
	        workflow.setUpdateBy(updatedBy);
	        workflow.setActionDate(new Date());

	        return workFlowListRepository.save(workflow);
	}

	@Override
	public TaskFlowList updateTaskFlow(String applicationNo, Integer taskStatusId, String assignedRoleId, String assignedUserId,String remarks) {
		//audit save
		TaskFlowList taskFlow = taskFlowListRepository.findByApplicationNo(applicationNo);
		saveTaskflowAudit(taskFlow);
		
		WorkFlowList workflow = workFlowListRepository.findByApplicationNo(applicationNo);
		
		taskFlow.setAssignedUserId(assignedUserId);
        taskFlow.setTaskStatusId(taskStatusId);
        taskFlow.setAssignedRoleId(assignedRoleId);
        taskFlow.setSaveRemarks(remarks);
        taskFlow.setWorkFlow(workflow);
        taskFlow.setActionDate(new Date());

        return taskFlowListRepository.save(taskFlow);
	}

	private void saveWorkflowAudit(WorkFlowList workflow) {
		WorkFlowListAudit workflowAudit = WorkFlowListAudit.builder()
	                .applicationNo(workflow.getApplicationNo())
	                .applicationName(workflow.getApplicationName())
	                .serviceId(workflow.getServiceId())
	                .statusId(workflow.getStatusId())
	                .roleId(workflow.getRoleId())
	                .actorId(workflow.getActorId())
	                .wfRemarks(workflow.getWfRemarks())
	                .createdAt(workflow.getActionDate())
	                .actionDate(new Date())
	                .workFlow(workflow)
	                .build();

		workFlowListAuditRepository.save(workflowAudit);
		
	}
	
	private void saveTaskflowAudit(TaskFlowList taskflow) {
		TaskFlowListAudit taskFlowAudit = TaskFlowListAudit.builder()
                .applicationNo(taskflow.getApplicationNo())
                .taskStatusId(taskflow.getTaskStatusId())
                .assignedRoleId(taskflow.getAssignedRoleId())
                .assignedUserId(taskflow.getAssignedUserId())
                .workflowId(taskflow.getWorkFlow().getId())
                .saveRemarks(taskflow.getSaveRemarks())
                .actionDate(taskflow.getActionDate())
                .locationId(taskflow.getLocationId())
                .taskFlow(taskflow)
                .auditActionDate(new Date())
                .build();

		taskFlowListAuditRepository.save(taskFlowAudit);
    }

}
