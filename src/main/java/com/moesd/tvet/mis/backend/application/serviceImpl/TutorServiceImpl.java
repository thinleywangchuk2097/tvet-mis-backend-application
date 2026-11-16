package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TutorDto;
import com.moesd.tvet.mis.backend.application.dto.TutorVerifyDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.Tutor;
import com.moesd.tvet.mis.backend.application.model.TutorSubject;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.TutorRepository;
import com.moesd.tvet.mis.backend.application.service.TutorService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import jakarta.transaction.Transactional;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService{
	
	// Add Tutor workflow: submitted (55) -> role 7 verify (56) / reject (58) -> role 22 approve (57)
	private static final int TUTOR_SERVICE_ID = 57;
	private static final int STATUS_SUBMITTED = 55;
	private static final int FIRST_ROLE_ID = 7;
	private static final int TASK_STATUS_INITIATED = 18;
	private static final int TASK_STATUS_COMPLETED = 20;
	private static final int LOCATION_ID = 14;

	private final TutorRepository tutorRepository;
	private final ObjectToJson objectTojson;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final WorkTaskFlowService workTaskFlowService;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final RoleServiceRepository roleServiceRepository;
	
	@Override
	@Transactional
	public ResponseEntity<?> submitTutor(TutorDto request) {
		try {
			log.info("Submitting new tutor: {}", request.getName());
			
			// Create new tutor entity (goes to role 7 for verification)
			String applicationNo = generateApplicationNumber.generateApplicationNumber(TUTOR_SERVICE_ID);
			Tutor tutor = Tutor.builder()
				.applicationNo(applicationNo)
				.applicationStatusId(STATUS_SUBMITTED)
				.citizenId(request.getCitizenId())
				.name(request.getName())
				.genderId(request.getGenderId())
				.qualificationId(request.getQualificationId())
				.email(request.getEmail())
				.mobileNo(request.getMobileNo())
				.instituteId(request.getInstituteId())
				.statusId(request.getStatusId() != null ? request.getStatusId() : 1)
				.createdBy(request.getCreatedBy())
				.createdAt(new Date())
				.build();
			setSubjects(tutor, request.getSubjectIds());
			
			// Save to database
			Tutor savedTutor = tutorRepository.save(tutor);

			// Workflow + task (role 7 task list)
			Integer taskStatusId = dropdownManagementRepository.findChildById(TASK_STATUS_INITIATED)
					.orElseThrow(() -> new RecordNotFoundException("Initiated task status not found"));
			String remarks = "Tutor submitted for verification";
			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, request.getName(),
					TUTOR_SERVICE_ID, STATUS_SUBMITTED, FIRST_ROLE_ID, remarks);
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, FIRST_ROLE_ID, null, workflow, remarks,
					LOCATION_ID);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Tutor submitted for verification. Application No: " + applicationNo);
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", savedTutor);
			
			log.info("Tutor submitted successfully with ID: {}", savedTutor.getId());
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error submitting tutor: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to submit tutor: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public List<ObjectNode> getAllActiveTutors(String instituteId) {
	    return objectTojson._toJson(tutorRepository.getAllActiveTutors(instituteId));
	}
	
	@Override
	public ResponseEntity<?> updateTutor(TutorDto request) {
		try {
			log.info("Updating tutor with ID: {}", request.getId());
			
			// Check if tutor exists
			Optional<Tutor> existingTutorOpt = tutorRepository.findById(request.getId());
			if (existingTutorOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Tutor not found with ID: " + request.getId());
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			Tutor existingTutor = existingTutorOpt.get();
			
			// Update tutor entity
			existingTutor.setCitizenId(request.getCitizenId());
			existingTutor.setName(request.getName());
			existingTutor.setGenderId(request.getGenderId());
			existingTutor.setQualificationId(request.getQualificationId());
			existingTutor.setEmail(request.getEmail());
			existingTutor.setMobileNo(request.getMobileNo());
			existingTutor.setInstituteId(request.getInstituteId());
			if (request.getStatusId() != null)
				existingTutor.setStatusId(request.getStatusId());
			setSubjects(existingTutor, request.getSubjectIds());
			existingTutor.setUpdatedBy(request.getUpdatedBy());
			existingTutor.setUpdatedAt(new Date());
			
			// Save to database
			Tutor updatedTutor = tutorRepository.save(existingTutor);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Tutor updated successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", updatedTutor);
			
			log.info("Tutor updated successfully with ID: {}", updatedTutor.getId());
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error updating tutor: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to update tutor: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public ResponseEntity<?> softDeleteTutor(Long tutorId) {
		try {
			log.info("Soft deleting tutor with ID: {}", tutorId);
			
			// Check if tutor exists
			Optional<Tutor> existingTutorOpt = tutorRepository.findById(tutorId);
			if (existingTutorOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Tutor not found with ID: " + tutorId);
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			// Soft delete by setting statusId to 2 (Inactive)
			Tutor tutor = existingTutorOpt.get();
			tutor.setStatusId(2);
			tutor.setUpdatedAt(new Date());
			
			// Save to database
			Tutor deletedTutor = tutorRepository.save(tutor);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Tutor deleted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", deletedTutor);
			
			log.info("Tutor soft deleted successfully with ID: {}", tutorId);
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error deleting tutor: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to delete tutor: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	

	@Override
	public List<ObjectNode> getTutorByApplicationNo(String applicationNo) {
		return objectTojson._toJson(tutorRepository.getTutorByApplicationNo(applicationNo));
	}

	/** Role 7: verify (56) / reject (58); role 22: approve (57). Next role comes from tbl_role_service. */
	@Override
	@Transactional
	public ResponseEntity<?> verifyTutor(TutorVerifyDto request) {
		try {
			if (request.getApplicationNo() == null || request.getApplicationNo().isBlank())
				throw new RecordNotFoundException("applicationNo is required");
			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");
			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assignedRoleId is required");

			Tutor tutor = tutorRepository.findByApplicationNo(request.getApplicationNo())
					.orElseThrow(() -> new RecordNotFoundException(
							"Tutor application not found: " + request.getApplicationNo()));

			Optional<RoleService> next = roleServiceRepository.getNextAssignedRole(request.getAssignedRoleId(),
					TUTOR_SERVICE_ID, request.getStatusId());
			boolean finalStep = next.isEmpty() || next.get().getNextRoleId() == null;
			Integer nextRoleId = finalStep ? request.getAssignedRoleId() : next.get().getNextRoleId();

			Integer taskStatusId = dropdownManagementRepository
					.findChildById(finalStep ? TASK_STATUS_COMPLETED : TASK_STATUS_INITIATED)
					.orElseThrow(() -> new RecordNotFoundException("Task status not found"));

			String remarks = request.getRemarks() != null && !request.getRemarks().isBlank()
					? request.getRemarks().trim()
					: "Tutor application processed";

			tutor.setApplicationStatusId(request.getStatusId());
			tutor.setUpdatedBy(request.getUpdatedBy());
			tutor.setUpdatedAt(new Date());
			tutorRepository.save(tutor);

			workTaskFlowService.updateWorkflow(request.getApplicationNo(), request.getStatusId(),
					request.getAssignedRoleId(), request.getUserId(), remarks, TUTOR_SERVICE_ID,
					request.getUpdatedBy());
			workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, nextRoleId,
					request.getUserId(), remarks);

			Map<String, Object> body = new HashMap<>();
			body.put("status", HttpStatus.OK.value());
			body.put("applicationNo", request.getApplicationNo());
			body.put("message", "Tutor application updated successfully");
			return ResponseEntity.ok(body);

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			log.error("Failed to verify tutor application", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to process tutor application", "timestamp",
							LocalDateTime.now()));
		}
	}

	/** Replaces the tutor's subjects (saved with the tutor through the cascade). */
	private void setSubjects(Tutor tutor, List<Long> subjectIds) {
		tutor.getSubjects().clear();
		if (subjectIds == null)
			return;
		subjectIds.stream().filter(Objects::nonNull).distinct()
				.forEach(subjectId -> tutor.getSubjects()
						.add(TutorSubject.builder().subjectId(subjectId).tutor(tutor).build()));
	}
}
