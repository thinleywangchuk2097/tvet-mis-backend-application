package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.moesd.tvet.mis.backend.application.dto.CMFGMonitoringDto;
import com.moesd.tvet.mis.backend.application.dto.CMFGVerifyDto;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.CMFGMonitoringAuditRepository;
import com.moesd.tvet.mis.backend.application.model.CMFGMonitoringAudit;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import com.moesd.tvet.mis.backend.application.dto.QualityStandardsdto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.CMFGMonitoring;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.CMFGMonitoringRepository;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.CMFGMonitoringService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.node.ObjectNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class CMFGMonitoringServiceImpl implements CMFGMonitoringService {

	/** Checklist (quality standards) and application numbers use this service. */
	private static final int CMFG_SERVICE_ID = 55;
	/** Task status "unclaimed / initiated" (same id other services use). */
	private static final int TASK_STATUS_INITIATED = 18;
	private static final int LOCATION_ID = 14;

	private final CMFGMonitoringRepository cmfgMonitoringRepository;
	private final ServiceMasterRepository serviceMasterRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final DocumentFileUploadService documentFileUploadService;
	private final ObjectToJson objectTojson;
	private final RoleServiceRepository roleServiceRepository;
	private final CMFGMonitoringAuditRepository cmfgMonitoringAuditRepository;

	/** Task status "completed" (same id other services use). */
	private static final int TASK_STATUS_COMPLETED = 20;

	@Override
	@Transactional
	public ResponseEntity<?> submitMonitoring(CMFGMonitoringDto request) {
		try {
			if (request.getInstituteId() == null || request.getInstituteId().isBlank())
				throw new RecordNotFoundException("instituteId is required");
			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assignedRoleId is required");
			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");

			List<QualityStandardsdto> answers = request.getQualityStandards() == null ? List.of()
					: request.getQualityStandards().stream()
							.filter(q -> q != null && q.getStandardId() != null && q.getResponseId() != null
									&& !q.getResponseId().isBlank())
							.collect(Collectors.toList());
			if (answers.isEmpty()) {
				throw new RecordNotFoundException("Please answer the monitoring checklist");
			}

			Integer serviceId = request.getServiceId() != null ? request.getServiceId() : CMFG_SERVICE_ID;
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);
			LocalDateTime now = LocalDateTime.now();

			List<CMFGMonitoring> rows = answers.stream()
					.map(q -> CMFGMonitoring.builder()
							.applicationNo(applicationNo)
							.standardId(q.getStandardId())
							.responseId(q.getResponseId())
							.remarks(q.getRemarks())
							.instituteId(request.getInstituteId())
							.createdBy(request.getCreatedBy())
							.createdAt(now)
							.build())
					.collect(Collectors.toList());
			cmfgMonitoringRepository.saveAll(rows);

			// Workflow + task (goes to the reviewer's task list)
			Integer taskStatusId = dropdownManagementRepository.findChildById(TASK_STATUS_INITIATED)
					.orElseThrow(() -> new RecordNotFoundException("Initiated task status not found"));
			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, request.getApplicantName(),
					serviceId, request.getStatusId(), request.getAssignedRoleId(), request.getRemarks());
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, request.getAssignedRoleId(),
					request.getAssignedUserId(), workflow, request.getRemarks(), LOCATION_ID);

			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo, "cmfg_monitoring",
						serviceId, request.getUserId(), null);
			}

			Map<String, Object> body = new LinkedHashMap<>();
			body.put("status", 201);
			body.put("applicationNo", applicationNo);
			body.put("message", "CMFG monitoring submitted successfully");
			return ResponseEntity.status(HttpStatus.CREATED).body(body);

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", 400, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to submit CMFG monitoring", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to submit CMFG monitoring"));
		}
	}

	@Override
	public List<ObjectNode> getMonitoringByUserId(String userId) {
		return objectTojson._toJson(cmfgMonitoringRepository.getMonitoringByUserId(userId));
	}

	@Override
	public ResponseEntity<?> getMonitoringByApplicationNo(String applicationNo) {
		List<ObjectNode> summary = objectTojson._toJson(cmfgMonitoringRepository.getMonitoringSummary(applicationNo));
		if (summary.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("status", 404, "message", "Monitoring application not found: " + applicationNo));
		}
		List<Map<String, Object>> responses = cmfgMonitoringRepository
				.findByApplicationNoOrderByIdAsc(applicationNo).stream()
				.map(r -> {
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("standardId", r.getStandardId());
					m.put("responseId", r.getResponseId());
					m.put("remarks", r.getRemarks());
					return m;
				})
				.collect(Collectors.toList());

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("summary", summary.get(0));
		body.put("qualityStandards", responses);
		return ResponseEntity.ok(body);
	}

	/**
	 * Reviewer action (e.g. verify = status 56). The next role comes from tbl_role_service
	 * (assigned role + service + status). If no next role is configured, this is the final
	 * step and the task is marked completed.
	 */
	@Override
	@Transactional
	public ResponseEntity<?> verifyMonitoring(CMFGVerifyDto request) {
		try {
			if (request.getApplicationNo() == null || request.getApplicationNo().isBlank())
				throw new RecordNotFoundException("applicationNo is required");
			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");
			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assignedRoleId is required");

			String applicationNo = request.getApplicationNo();
			Integer serviceId = request.getServiceId() != null ? request.getServiceId() : CMFG_SERVICE_ID;

			List<CMFGMonitoring> rows = cmfgMonitoringRepository.findByApplicationNoOrderByIdAsc(applicationNo);
			if (rows.isEmpty()) {
				throw new RecordNotFoundException("Monitoring application not found: " + applicationNo);
			}

			Optional<RoleService> next = roleServiceRepository.getNextAssignedRole(request.getAssignedRoleId(),
					serviceId, request.getStatusId());
			boolean finalStep = next.isEmpty() || next.get().getNextRoleId() == null;
			Integer nextRoleId = finalStep ? request.getAssignedRoleId() : next.get().getNextRoleId();

			Integer taskStatusId = dropdownManagementRepository
					.findChildById(finalStep ? TASK_STATUS_COMPLETED : TASK_STATUS_INITIATED)
					.orElseThrow(() -> new RecordNotFoundException("Task status not found"));

			String remarks = request.getRemarks() != null && !request.getRemarks().isBlank()
					? request.getRemarks()
					: "CMFG monitoring verified";

			// ---- reviewer edits: audit the old values first, then update ----
			LocalDateTime now = LocalDateTime.now();
			Map<Long, QualityStandardsdto> edits = new HashMap<>();
			if (request.getQualityStandards() != null) {
				for (QualityStandardsdto q : request.getQualityStandards()) {
					if (q != null && q.getStandardId() != null) edits.put(q.getStandardId(), q);
				}
			}
			List<CMFGMonitoringAudit> audits = new java.util.ArrayList<>();
			int changed = 0;
			for (CMFGMonitoring row : rows) {
				QualityStandardsdto q = edits.get(row.getStandardId());
				if (q != null) {
					String newResponse = q.getResponseId() == null || q.getResponseId().isBlank()
							? row.getResponseId() : q.getResponseId();
					String newRemarks = q.getRemarks() == null || q.getRemarks().isBlank() ? null : q.getRemarks().trim();
					boolean differs = !Objects.equals(row.getResponseId(), newResponse)
							|| !Objects.equals(blankToNull(row.getRemarks()), newRemarks);
					if (differs) {
						// snapshot of the data BEFORE the change
						audits.add(CMFGMonitoringAudit.builder()
								.applicationNo(row.getApplicationNo())
								.standardId(row.getStandardId())
								.responseId(row.getResponseId())
								.remarks(row.getRemarks())
								.instituteId(row.getInstituteId())
								.createdBy(row.getCreatedBy())
								.createdAt(row.getCreatedAt())
								.updatedBy(row.getUpdatedBy())
								.updatedAt(row.getUpdatedAt())
								.cmfgMonitoring(row)
								.build());
						row.setResponseId(newResponse);
						row.setRemarks(newRemarks);
						changed++;
					}
				}
				row.setUpdatedBy(request.getUpdatedBy());
				row.setUpdatedAt(now);
			}
			if (!audits.isEmpty()) {
				cmfgMonitoringAuditRepository.saveAll(audits); // old values first
			}
			cmfgMonitoringRepository.saveAll(rows);

			workTaskFlowService.updateWorkflow(applicationNo, request.getStatusId(), request.getAssignedRoleId(),
					request.getUserId(), remarks, serviceId, request.getUpdatedBy());
			// next role picks it up from its group task list (no user assigned yet)
			workTaskFlowService.updateTaskFlow(applicationNo, taskStatusId, nextRoleId,
					finalStep ? request.getUserId() : null, remarks);

			Map<String, Object> body = new LinkedHashMap<>();
			body.put("status", 200);
			body.put("applicationNo", applicationNo);
			body.put("completed", finalStep);
			body.put("nextRoleId", finalStep ? null : nextRoleId);
			body.put("changedItems", changed);
			body.put("message", finalStep ? "CMFG monitoring verified and completed"
					: "CMFG monitoring verified and forwarded");
			return ResponseEntity.ok(body);

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", 400, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to verify CMFG monitoring", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to verify CMFG monitoring"));
		}
	}


	private static String blankToNull(String v) {
		return v == null || v.isBlank() ? null : v.trim();
	}
}
