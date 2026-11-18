package com.moesd.tvet.mis.backend.application.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
			validateVerifyRequest(request);
			String applicationNo = request.getApplicationNo();
			Integer serviceId = request.getServiceId() != null ? request.getServiceId() : CMFG_SERVICE_ID;

			List<CMFGMonitoring> rows = cmfgMonitoringRepository.findByApplicationNoOrderByIdAsc(applicationNo);
			if (rows.isEmpty()) {
				throw new RecordNotFoundException("Monitoring application not found: " + applicationNo);
			}

			NextStep step = resolveNextStep(request, serviceId);
			Integer taskStatusId = dropdownManagementRepository
					.findChildById(step.finalStep() ? TASK_STATUS_COMPLETED : TASK_STATUS_INITIATED)
					.orElseThrow(() -> new RecordNotFoundException("Task status not found"));

			String remarks = request.getRemarks() != null && !request.getRemarks().isBlank()
					? request.getRemarks()
					: "CMFG monitoring verified";

			int changed = applyReviewerEdits(rows, request);

			workTaskFlowService.updateWorkflow(applicationNo, request.getStatusId(), request.getAssignedRoleId(),
					request.getUserId(), remarks, serviceId, request.getUpdatedBy());
			// next role picks it up from its group task list (no user assigned yet)
			workTaskFlowService.updateTaskFlow(applicationNo, taskStatusId, step.nextRoleId(),
					step.finalStep() ? request.getUserId() : null, remarks);

			return ResponseEntity.ok(verifyResponse(applicationNo, step, changed));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", 400, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to verify CMFG monitoring", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to verify CMFG monitoring"));
		}
	}

	/** Where the application goes next; no next role configured = final step (task completed). */
	private record NextStep(boolean finalStep, Integer nextRoleId) {
	}

	private static void validateVerifyRequest(CMFGVerifyDto request) {
		if (request.getApplicationNo() == null || request.getApplicationNo().isBlank())
			throw new RecordNotFoundException("applicationNo is required");
		if (request.getStatusId() == null)
			throw new RecordNotFoundException("statusId is required");
		if (request.getAssignedRoleId() == null)
			throw new RecordNotFoundException("assignedRoleId is required");
	}

	private NextStep resolveNextStep(CMFGVerifyDto request, Integer serviceId) {
		Optional<RoleService> next = roleServiceRepository.getNextAssignedRole(request.getAssignedRoleId(),
				serviceId, request.getStatusId());
		boolean finalStep = next.isEmpty() || next.get().getNextRoleId() == null;
		return new NextStep(finalStep, finalStep ? request.getAssignedRoleId() : next.get().getNextRoleId());
	}

	/**
	 * Reviewer edits: for every changed answer the old values are audited first, then updated.
	 * All rows get the reviewer as updatedBy. Returns the number of changed answers.
	 */
	private int applyReviewerEdits(List<CMFGMonitoring> rows, CMFGVerifyDto request) {
		Map<Long, QualityStandardsdto> edits = editsByStandard(request.getQualityStandards());
		LocalDateTime now = LocalDateTime.now();
		List<CMFGMonitoringAudit> audits = new ArrayList<>();
		for (CMFGMonitoring row : rows) {
			QualityStandardsdto q = edits.get(row.getStandardId());
			CMFGMonitoringAudit before = q == null ? null : applyEdit(row, q);
			if (before != null) {
				audits.add(before);
			}
			row.setUpdatedBy(request.getUpdatedBy());
			row.setUpdatedAt(now);
		}
		if (!audits.isEmpty()) {
			cmfgMonitoringAuditRepository.saveAll(audits); // old values first
		}
		cmfgMonitoringRepository.saveAll(rows);
		return audits.size();
	}

	private static Map<Long, QualityStandardsdto> editsByStandard(List<QualityStandardsdto> standards) {
		Map<Long, QualityStandardsdto> edits = new HashMap<>();
		if (standards != null) {
			standards.stream()
					.filter(q -> q != null && q.getStandardId() != null)
					.forEach(q -> edits.put(q.getStandardId(), q));
		}
		return edits;
	}

	/** Applies the reviewer's answer / remarks; returns the old values (for the audit) when they differ, else null. */
	private static CMFGMonitoringAudit applyEdit(CMFGMonitoring row, QualityStandardsdto q) {
		String newResponse = q.getResponseId() == null || q.getResponseId().isBlank()
				? row.getResponseId() : q.getResponseId();
		String newRemarks = blankToNull(q.getRemarks());
		boolean differs = !Objects.equals(row.getResponseId(), newResponse)
				|| !Objects.equals(blankToNull(row.getRemarks()), newRemarks);
		if (!differs) {
			return null;
		}
		CMFGMonitoringAudit before = auditOf(row);
		row.setResponseId(newResponse);
		row.setRemarks(newRemarks);
		return before;
	}

	/** Snapshot of a monitoring row BEFORE it is changed */
	private static CMFGMonitoringAudit auditOf(CMFGMonitoring row) {
		return CMFGMonitoringAudit.builder()
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
				.build();
	}

	private static Map<String, Object> verifyResponse(String applicationNo, NextStep step, int changed) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", 200);
		body.put("applicationNo", applicationNo);
		body.put("completed", step.finalStep());
		body.put("nextRoleId", step.finalStep() ? null : step.nextRoleId());
		body.put("changedItems", changed);
		body.put("message", step.finalStep() ? "CMFG monitoring verified and completed"
				: "CMFG monitoring verified and forwarded");
		return body;
	}

	private static String blankToNull(String v) {
		return v == null || v.isBlank() ? null : v.trim();
	}
}
