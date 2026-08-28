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
import com.moesd.tvet.mis.backend.application.model.CurriculumDevelopmentAudit;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.CurriculumDevelopmentAuditRepository;
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
	private final CurriculumDevelopmentAuditRepository curriculumDevelopmentAuditRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final DocumentFileUploadService documentFileUploadService;
	private final ObjectToJson objectTojson;
	private final ServiceMasterRepository serviceMasterRepository;
	private final RoleServiceRepository roleServiceRepository;

	@Override
	public ResponseEntity<?> submitCurriculum(CurriculumDevelopmentdto request) {
		try {
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
			Integer statusId = request.getStatusId();// workflow statusId
			Integer serviceId = request.getServiceId();
			Integer assignedRoleId = request.getAssignedRoleId();
			String userId = request.getUserId();
			String applicantName = request.getCurriculumTitle();
			Integer locationId = 14;
			
			//Get initiated statusId
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));
			
			//newly added
			if (request.getServiceId() == 48 || request.getServiceId() == 49) {
		
				CurriculumDevelopment curriculum = curriculumDevelopmentRepository.findByApplicationNo(request.getApplicationNo());
				//update audit 
				saveCurriculumAudit(curriculum);
				
				curriculum.setApplicationNo(request.getApplicationNo());
				curriculum.setCurriculumTitle(request.getCurriculumTitle());
				curriculum.setCurriculumTypeId(request.getCurriculumTypeId());
				curriculum.setDescription(request.getDescription());
				curriculum.setInstituteId(request.getInstituteId());
				curriculum.setProgrammeTypeId(request.getProgrammeTypeId());
				curriculum.setProgrammeId(request.getProgrammeId());;
				curriculum.setCertificateLevelId(request.getCertificateLevelId());
				curriculum.setEntryRequirement(request.getEntryRequirement());
				curriculum.setTotalTheoryDuration(request.getTotalTheoryDuration());
				curriculum.setTotalPracticalDuration(request.getTotalPracticalDuration());
				curriculum.setTotalOjtDuration(request.getTotalOjtDuration());
				curriculum.setTotalProgramDuration(request.getTotalProgramDuration());
				curriculum.setStatusId(request.getStatusId());
				curriculum.setCreatedBy(request.getCreatedBy());
				curriculum.setCreatedAt(LocalDateTime.now());
				curriculum.setUpdatedAt(LocalDateTime.now());
				curriculum.setUpdatedBy(request.getUpdatedBy());
				
				// update curriculum
				curriculumDevelopmentRepository.save(curriculum);
				
				
				workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
						request.getUserId(), request.getRemarks(), serviceId, request.getUpdatedBy());

				// update task flow
				workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, assignedRoleId,
						request.getUserId(), request.getRemarks());
				
				//Save documents
				if (request.getDocuments() != null && request.getDocuments().length > 0) {
					documentFileUploadService.saveDocument(request.getDocuments(), request.getApplicationNo(), "curriculum",
							serviceId, userId, null);
				}

				//Return response
				return ResponseEntity.status(HttpStatus.CREATED)
						.body(Map.of("applicationNo", request.getApplicationNo(), "status", HttpStatus.CREATED.value()));
				  
			}else {
				
				//Generate application number
				String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);

				//Build entity
				CurriculumDevelopment curriculumDevelopment = CurriculumDevelopment.builder().applicationNo(applicationNo)
						.curriculumTitle(request.getCurriculumTitle()).curriculumTypeId(request.getCurriculumTypeId())
						.description(request.getDescription()).instituteId(request.getInstituteId())
						.programmeTypeId(request.getProgrammeTypeId())
						.programmeId(request.getProgrammeId())
						.certificateLevelId(request.getCertificateLevelId())
						.entryRequirement(request.getEntryRequirement()).totalTheoryDuration(request.getTotalTheoryDuration())
						.totalPracticalDuration(request.getTotalPracticalDuration()).totalOjtDuration(request.getTotalOjtDuration())
						.totalProgramDuration(request.getTotalProgramDuration())
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
					documentFileUploadService.saveDocument(request.getDocuments(), applicationNo, "curriculum",
							serviceId, userId, null);
				}

				//Return response
				return ResponseEntity.status(HttpStatus.CREATED)
						.body(Map.of("applicationNo", applicationNo, "status", HttpStatus.CREATED.value()));
			}
			

		} catch (Exception e) {
			log.error("Error submitting curriculum development: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to submit curriculum development", "error", e.getMessage(),
							"timestamp", LocalDateTime.now()));
		}
	}
	
	private void saveCurriculumAudit(CurriculumDevelopment curriculum) {
		CurriculumDevelopmentAudit curriculumAudit = CurriculumDevelopmentAudit.builder()
	                .applicationNo(curriculum.getApplicationNo())
	                .curriculumTitle(curriculum.getCurriculumTitle())
	                .curriculumTypeId(curriculum.getCurriculumTypeId())
					.description(curriculum.getDescription())
					.instituteId(curriculum.getInstituteId())
					.programmeTypeId(curriculum.getProgrammeTypeId())
					.programmeId(curriculum.getProgrammeId())
					.certificateLevelId(curriculum.getCertificateLevelId())
					.entryRequirement(curriculum.getEntryRequirement())
					.totalTheoryDuration(curriculum.getTotalTheoryDuration())
					.totalPracticalDuration(curriculum.getTotalPracticalDuration())
					.totalOjtDuration(curriculum.getTotalOjtDuration())
					.totalProgramDuration(curriculum.getTotalProgramDuration())
					.curriculum(curriculum)
					.statusId(curriculum.getStatusId())
					.createdBy(curriculum.getCreatedBy())
					.createdAt(curriculum.getCreatedAt())
	                .build();

		curriculumDevelopmentAuditRepository.save(curriculumAudit);
		
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
			Integer taskStatusId;
			if(request.getStatusId() == 57) {
				taskStatusId = dropdownManagementRepository.findChildById(20) // task completedId
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}else {
				taskStatusId = dropdownManagementRepository.findChildById(18) // initiated 
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}
			
			
			// Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// Fetch next role
			RoleService roleService = roleServiceRepository.getNextAssignedRole(assignedRoleId, serviceId, statusId)
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

			// Find existing registration by applicationNo
			CurriculumDevelopment existingCurriculumDevelopment =
			        curriculumDevelopmentRepository.findByApplicationNo(request.getApplicationNo());

			if (existingCurriculumDevelopment == null) {
			    throw new RecordNotFoundException(
			            "Curriculum not found with applicationNo: " + request.getApplicationNo());
			}
			
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
		    log.error("Failed to update institute registration", e);
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(Map.of("message", "Failed to update institute registration", 
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

	@Override
	public CurriculumDevelopment getCurriculumById(Long id) {
	    return curriculumDevelopmentRepository.findById(id)
	            .orElseThrow(() -> new RecordNotFoundException(
	                    "Curriculum not found with id: " + id));
	}

}