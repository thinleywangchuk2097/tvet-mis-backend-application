package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SubjectDto;
import com.moesd.tvet.mis.backend.application.dto.SubjectVerifyDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.Subject;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.SubjectRepository;
import com.moesd.tvet.mis.backend.application.service.SubjectService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import jakarta.transaction.Transactional;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service 
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService{
	
	// Add Subject workflow: submitted (55) -> role 7 verify (56) / reject (58) -> role 22 approve (57)
	private static final int SUBJECT_SERVICE_ID = 56;
	private static final int STATUS_SUBMITTED = 55;
	private static final int FIRST_ROLE_ID = 7;
	private static final int TASK_STATUS_INITIATED = 18;
	private static final int TASK_STATUS_COMPLETED = 20;
	private static final int LOCATION_ID = 14;

	private final SubjectRepository subjectRepository;
	private final ObjectToJson objectTojson;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final WorkTaskFlowService workTaskFlowService;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final RoleServiceRepository roleServiceRepository;
	
	@Override
	@Transactional
	public ResponseEntity<?> submitSubject(SubjectDto request) {
		try {
			log.info("Submitting new subject: {}", request.getSubjectName());
			
			// Validate if subject name already exists
			Optional<Subject> existingSubjectByName = subjectRepository.findBySubjectName(request.getSubjectName());
			if (existingSubjectByName.isPresent()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Subject name already exists: " + request.getSubjectName());
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(errorResponse);
			}
			// Create new subject entity (goes to role 7 for verification)
			String applicationNo = generateApplicationNumber.generateApplicationNumber(SUBJECT_SERVICE_ID);
			Subject subject = Subject.builder()
				.applicationNo(applicationNo)
				.applicationStatusId(STATUS_SUBMITTED)
				.subjectName(request.getSubjectName())
				.classLevelId(request.getClassLevelId())
				.totalDurationHours(request.getTotalDurationHours())
				.instituteId(request.getInstituteId())
				.statusId(request.getStatusId() != null ? request.getStatusId() : 1)
				.description(request.getDescription())
				.createdBy(request.getCreatedBy())
				.createdAt(new Date())
				.build();
			
			// Save to database
			Subject savedSubject = subjectRepository.save(subject);

			// Workflow + task (role 7 task list)
			Integer taskStatusId = dropdownManagementRepository.findChildById(TASK_STATUS_INITIATED)
					.orElseThrow(() -> new RecordNotFoundException("Initiated task status not found"));
			String remarks = "Subject submitted for verification";
			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, request.getSubjectName(),
					SUBJECT_SERVICE_ID, STATUS_SUBMITTED, FIRST_ROLE_ID, remarks);
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, FIRST_ROLE_ID, null, workflow, remarks,
					LOCATION_ID);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Subject submitted for verification. Application No: " + applicationNo);
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", savedSubject);
			
			log.info("Subject submitted successfully with ID: {}", savedSubject.getId());
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error submitting subject: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to submit subject: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}
	
	@Override
	public List<ObjectNode> getAllActiveSubjects(String instituteId) {
	    return objectTojson._toJson(subjectRepository.getAllActiveSubjects(instituteId));
	}

	@Override
	public ResponseEntity<?> updateSubject(SubjectDto request) {
		try {
			log.info("Updating subject with ID: {}", request.getId());
			
			// Check if subject exists
			Optional<Subject> existingSubjectOpt = subjectRepository.findById(request.getId());
			if (existingSubjectOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Subject not found with ID: " + request.getId());
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			Subject existingSubject = existingSubjectOpt.get();
			
			// Check if subject name is being changed and if new name already exists
			if (!existingSubject.getSubjectName().equals(request.getSubjectName())) {
				Optional<Subject> duplicateName = subjectRepository.findBySubjectName(request.getSubjectName());
				if (duplicateName.isPresent()) {
					// Prepare error response
					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("message", "Subject name already exists: " + request.getSubjectName());
					errorResponse.put("status", "ERROR");
					errorResponse.put("data", null);
					
					return ResponseEntity
						.status(HttpStatus.CONFLICT)
						.body(errorResponse);
				}
			}
			
			// Update subject entity
			existingSubject.setSubjectName(request.getSubjectName());
			existingSubject.setClassLevelId(request.getClassLevelId());
			existingSubject.setTotalDurationHours(request.getTotalDurationHours());
			existingSubject.setInstituteId(request.getInstituteId());
			if (request.getStatusId() != null)
				existingSubject.setStatusId(request.getStatusId());
			existingSubject.setDescription(request.getDescription());
			existingSubject.setUpdatedBy(request.getUpdatedBy());
			existingSubject.setUpdatedAt(new Date());
			
			// Save to database
			Subject updatedSubject = subjectRepository.save(existingSubject);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Subject updated successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", updatedSubject);
			
			log.info("Subject updated successfully with ID: {}", updatedSubject.getId());
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error updating subject: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to update subject: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public ResponseEntity<?> softDeleteSubject(Integer subjectId) {
		try {
			log.info("Soft deleting subject with ID: {}", subjectId);
			
			// Check if subject exists
			Optional<Subject> existingSubjectOpt = subjectRepository.findById(Long.valueOf(subjectId));
			if (existingSubjectOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Subject not found with ID: " + subjectId);
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			// Soft delete by setting statusId to 0 (Inactive)
			Subject subject = existingSubjectOpt.get();
			subject.setStatusId(0);
			subject.setUpdatedAt(new Date());
			
			// Save to database
			Subject deletedSubject = subjectRepository.save(subject);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Subject deleted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", deletedSubject);
			
			log.info("Subject soft deleted successfully with ID: {}", subjectId);
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error deleting subject: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to delete subject: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public List<ObjectNode> getSubjectByApplicationNo(String applicationNo) {
		return objectTojson._toJson(subjectRepository.getSubjectByApplicationNo(applicationNo));
	}

	/** Role 7: verify (56) / reject (58); role 22: approve (57). Next role comes from tbl_role_service. */
	@Override
	@Transactional
	public ResponseEntity<?> verifySubject(SubjectVerifyDto request) {
		try {
			if (request.getApplicationNo() == null || request.getApplicationNo().isBlank())
				throw new RecordNotFoundException("applicationNo is required");
			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");
			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assignedRoleId is required");

			Subject subject = subjectRepository.findByApplicationNo(request.getApplicationNo())
					.orElseThrow(() -> new RecordNotFoundException(
							"Subject application not found: " + request.getApplicationNo()));

			Optional<RoleService> next = roleServiceRepository.getNextAssignedRole(request.getAssignedRoleId(),
					SUBJECT_SERVICE_ID, request.getStatusId());
			boolean finalStep = next.isEmpty() || next.get().getNextRoleId() == null;
			Integer nextRoleId = finalStep ? request.getAssignedRoleId() : next.get().getNextRoleId();

			Integer taskStatusId = dropdownManagementRepository
					.findChildById(finalStep ? TASK_STATUS_COMPLETED : TASK_STATUS_INITIATED)
					.orElseThrow(() -> new RecordNotFoundException("Task status not found"));

			String remarks = request.getRemarks() != null && !request.getRemarks().isBlank()
					? request.getRemarks().trim()
					: "Subject application processed";

			subject.setApplicationStatusId(request.getStatusId());
			subject.setUpdatedBy(request.getUpdatedBy());
			subject.setUpdatedAt(new Date());
			subjectRepository.save(subject);

			workTaskFlowService.updateWorkflow(request.getApplicationNo(), request.getStatusId(),
					request.getAssignedRoleId(), request.getUserId(), remarks, SUBJECT_SERVICE_ID,
					request.getUpdatedBy());
			workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, nextRoleId,
					request.getUserId(), remarks);

			Map<String, Object> body = new HashMap<>();
			body.put("status", HttpStatus.OK.value());
			body.put("applicationNo", request.getApplicationNo());
			body.put("message", "Subject application updated successfully");
			return ResponseEntity.ok(body);

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			log.error("Failed to verify subject application", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to process subject application", "timestamp",
							LocalDateTime.now()));
		}
	}

}
