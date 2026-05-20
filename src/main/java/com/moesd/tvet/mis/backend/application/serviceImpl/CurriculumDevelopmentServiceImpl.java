package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CurriculumDevelopmentdto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.CurriculumDevelopment;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.CurriculumDevelopmentRepository;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.CurriculumDevelopmentService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumDevelopmentServiceImpl implements CurriculumDevelopmentService {

	private final CurriculumDevelopmentRepository curriculumDevelopmentRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final DocumentFileUploadService documentFileUploadService;
	private final ObjectToJson objectTojson;
	private final ServiceMasterRepository serviceMasterRepository;
	private final RoleServiceRepository roleServiceRepository;

	@Override
	public ResponseEntity<?> submitCurriculumDevelopment(CurriculumDevelopmentdto request) {
		try {
			System.out.println("request" + request);
			//Validate required fields
			if (request.getServiceId() == null) {
				log.error("Validation failed: serviceId is required");
				throw new RecordNotFoundException("serviceId is required");
			}

			if (request.getAssignedRoleId() == null) {
				log.error("Validation failed: assignedRoleId is required");
				throw new RecordNotFoundException("assigned RoleId is required");
			}

			if (request.getStatusId() == null) {
				log.error("Validation failed: statusId is required");
				throw new RecordNotFoundException("statusId is required");
			}

			Integer serviceId = request.getServiceId();
			Integer assignedRoleId = request.getAssignedRoleId();
			String userId = request.getUserId();
			String applicantName = request.getCurriculumName();
			Integer locationId = 14;
			//Get initiated statusId
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));

			//Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);

			//Build entity
			CurriculumDevelopment curriculumDevelopment = CurriculumDevelopment.builder().applicationNo(applicationNo)
					.curriculumName(request.getCurriculumName()).curriculumTypeId(request.getCurriculumTypeId())
					.description(request.getDescription()).instituteId(request.getInstituteId())
					.statusId(request.getStatusId()).createdBy(request.getCreatedBy()).createdAt(LocalDateTime.now()).build();

			// Save curriculum development
			curriculumDevelopmentRepository.save(curriculumDevelopment);
			log.info("Curriculum development saved with ID: {} and Application No: {}", curriculumDevelopment.getId(),
					curriculumDevelopment.getApplicationNo());

			//Create workflow
			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, applicantName, serviceId,
					request.getStatusId(), assignedRoleId, request.getRemarks());

			//Create task flow
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, request.getAssignedUserId(),
					workflow, request.getRemarks(), locationId);

			//Save documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo, "curriculum_development",
						serviceId, userId, null);
			}

			//Return response
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("applicationNo", applicationNo, "status", HttpStatus.CREATED.value()));

		} catch (Exception e) {
			log.error("Error submitting curriculum development: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to submit curriculum development", "error", e.getMessage(),
							"timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getCurriculumDetails(String application_no) {
		List<Tuple> resultList = curriculumDevelopmentRepository.getCurriculumDetails(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public ResponseEntity<?> verifyCurriculumDevelopment(CurriculumDevelopmentdto request) {
		try {
			// Validate required fields for editing
			if (request.getApplicationNo() == null || request.getApplicationNo().isEmpty())
				throw new RecordNotFoundException("applicationNo is required");

			if (request.getServiceId() == null)
				throw new RecordNotFoundException("serviceId is required");

			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assigned RoleId is required");

			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");

			Integer serviceId = request.getServiceId();
			Integer assignedRoleId = request.getAssignedRoleId();
			Integer statusId = request.getStatusId();// workflow statusId
			String actorId = String.valueOf(request.getUpdatedBy());
			//Integer locationId = 14;
			
			
			Integer taskStatusId = dropdownManagementRepository.findChildById(20) // task completedId
					.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			
			// Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// Fetch next role
			RoleService roleService = roleServiceRepository.getNextAssignedRole(assignedRoleId, serviceId, statusId)
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

			// Find existing registration by applicationNo
			CurriculumDevelopment existingCurriculumDevelopment = curriculumDevelopmentRepository
					.findByApplicationNo(request.getApplicationNo()) // Returns Optional
					.orElseThrow(() -> new RecordNotFoundException(
							"Institute registration not found with applicationNo: " + request.getApplicationNo()));
			
			// Update InstituteRegistration entity
			existingCurriculumDevelopment.setStatusId(request.getStatusId());
			existingCurriculumDevelopment.setUpdatedAt(LocalDateTime.now());
			existingCurriculumDevelopment.setUpdatedBy(request.getUpdatedBy());

			// Save the updated registration
			CurriculumDevelopment savedRegistration = curriculumDevelopmentRepository.save(existingCurriculumDevelopment);
			
			
			workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
					request.getUserId(), request.getRemarks(), serviceId, null);

			// update task flow
			workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, roleService.getNextRoleId(),
					request.getUserId(), request.getRemarks());

			// Save documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), request.getApplicationNo(),
						"institute_registration", serviceId, actorId, null);
			}

			// Return response
			return ResponseEntity
					.ok(Map.of("applicationNo", request.getApplicationNo(), "id", savedRegistration.getId(), "status",
							HttpStatus.OK.value(), "message", "Institute registered successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to update institute registration", "error", e.getMessage(),
							"timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getCurriculumDetailsByUserId(String user_id) {
		List<Tuple> resultList = curriculumDevelopmentRepository.getCurriculumDetailsByUserId(user_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getApprovedCurriculumDataByUserId(String user_id, String curriculum_type) {
		List<Tuple> resultList = curriculumDevelopmentRepository.getApprovedCurriculumDataByUserId(user_id, curriculum_type);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

}