package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.ProgramMonitoringDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.ProgramMonitoring;
import com.moesd.tvet.mis.backend.application.model.ProgramMonitoringCheckList;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.ProgramMonitoringRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.ProgramMonitoringService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgramMonitoringServiceImpl implements ProgramMonitoringService {

	private final ProgramMonitoringRepository programMonitoringRepository;
	private final ObjectToJson objectTojson;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final RoleServiceRepository roleServiceRepository;
	private final DocumentFileUploadService documentFileUploadService;
	
	@Override
	public ResponseEntity<?> submitProgramMonitoring(ProgramMonitoringDto request) {
		try {

			// Validation
			if (request.getServiceId() == null)
				throw new RuntimeException("serviceId is required");

			if (request.getStatusId() == null)
				throw new RuntimeException("statusId is required");

			Integer serviceId = request.getServiceId();
			// Validate service
			serviceMasterRepository.findById(serviceId).orElseThrow(() -> new RuntimeException("Service Id not found"));

			// Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(51);

			// Build main entity
			ProgramMonitoring assessment = ProgramMonitoring.builder().applicationNo(applicationNo)
					.instituteId(request.getInstituteId())
					.dzongkhagId(request.getDzongkhagId())
					.exactLocation(request.getExactLocation())
					.instituteName(request.getInstituteName())
					.monitoringDate(request.getMonitoringDate())
					.registrationNo(request.getRegistrationNo())
					.courseId(request.getCourseId())
					.courseTypeId(request.getCourseTypeId())
					.serviceId(request.getServiceId())
					.statusId(request.getStatusId()).createdBy(request.getCreatedBy())
					.createdAt(new java.util.Date()).build();
			// Build MonitoringAssessmentCheckList
			if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
				List<ProgramMonitoringCheckList> qualitystandards = request.getQualityStandards().stream()
						.map(qualitystandardsDto -> ProgramMonitoringCheckList.builder()
								.standardId(qualitystandardsDto.getStandardId())
								.responseId(qualitystandardsDto.getResponseId())
								.remarks(qualitystandardsDto.getRemarks()).programMonitoring(assessment).build())
						.collect(Collectors.toList());
				assessment.setChecklists(qualitystandards);
			}

			// Save (cascade handles children)
			ProgramMonitoring saved = programMonitoringRepository.save(assessment);

			// Response
			return ResponseEntity.status(201).body(java.util.Map.of("applicationNo", applicationNo, "id", saved.getId(),
					"status", 201, "message", "Accredited course submitted successfully"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500)
					.body(java.util.Map.of("message", "Failed to submit accredited course", "error", e.getMessage()));
		}
	}

	@Override
	public List<ObjectNode> getProgramMonitoring(String user_id) {
		List<Tuple> resultList = programMonitoringRepository.getProgramMonitoring(user_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public ResponseEntity<?> verifyProgramMonitoring(ProgramMonitoringDto request) {
		try {
			System.out.println("dato" + request);
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
			Integer actorId = request.getActionId();
			Integer locationId = 14;
			//String userId = request.getUserId();
			// Get task status
			Integer taskStatusId;
			if (statusId == 57) {
				taskStatusId = dropdownManagementRepository.findChildById(20)// task completed Id
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}else {
				taskStatusId = dropdownManagementRepository.findChildById(18) // initiated taskId
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}

			

			// Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// Fetch next role
			RoleService roleService = roleServiceRepository.getNextAssignedRole(assignedRoleId, serviceId, statusId)
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

			// Find existing registration by applicationNo
			ProgramMonitoring programMonitoring = programMonitoringRepository
					.findByApplicationNo(request.getApplicationNo()) // Returns Optional
					.orElseThrow(() -> new RecordNotFoundException(
							"Monitoring Assessment not found with applicationNo: " + request.getApplicationNo()));

			// Update InstituteRegistration entity
			programMonitoring.setStatusId(request.getStatusId());
			programMonitoring.setDescription(request.getDescription());
			programMonitoring.setUpdatedAt(new java.util.Date());
			programMonitoring.setUpdatedBy(actorId);
			//Update ONLY existing records
	        if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
	            // Get existing quality standards for this registration
	            List<ProgramMonitoringCheckList> existingStandards = 
	            		programMonitoring.getChecklists();
	            if (existingStandards != null && !existingStandards.isEmpty()) {
	                Map<Long, ProgramMonitoringCheckList> existingMap = 
	                    existingStandards.stream()
	                        .collect(Collectors.toMap(
	                        		ProgramMonitoringCheckList::getStandardId,
	                            standard -> standard
	                        ));
	                
	                // Update only existing quality standards
	                for (var qualityDto : request.getQualityStandards()) {
	                    Long standardId = qualityDto.getStandardId();
	                    ProgramMonitoringCheckList existingStandard = existingMap.get(standardId);
	                    
	                    if (existingStandard != null) {
	                        // Update only responseId and remarks
	                        existingStandard.setResponseId(qualityDto.getResponseId());
	                        existingStandard.setRemarks(qualityDto.getRemarks());
	                       
	                    } else {
	                        System.out.println("Quality standard with standardId " + standardId + " not found, skipping");
	                    }
	                }
	               
	            }
	        }
			// Save the updated registration
	        ProgramMonitoring savedMonitoringAssessment = programMonitoringRepository.save(programMonitoring);
            if(statusId == 57 || statusId == 104) {
            	workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
    					request.getUserId(), request.getRemarks(), serviceId, actorId);

    			// update task flow
    			workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, roleService.getNextRoleId(),
    					request.getUserId(), request.getRemarks());
            }else {
            	var workflow = workTaskFlowService.createWorkflow(request.getApplicationNo(), request.getInstituteName(), serviceId,
    					request.getStatusId(), assignedRoleId, request.getRemarks());

    			workTaskFlowService.createTaskFlow(request.getApplicationNo(), taskStatusId, roleService.getNextRoleId(), request.getAssignedUserId(),
    					workflow, request.getRemarks(), locationId);
            }
            // Save documents
         	if (request.getDocuments() != null && request.getDocuments().length > 0) {
         		documentFileUploadService.saveDocument(request.getDocuments(), request.getApplicationNo(), "program_monitoring",
         						serviceId, null, null);
         	}
			// Return response
			return ResponseEntity
					.ok(Map.of("applicationNo", request.getApplicationNo(), "id", savedMonitoringAssessment.getId(), "status",
							HttpStatus.OK.value(), "message", "Monitoring Assessment updated successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to update  Monitoring Assessment", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getProgramMonitoringByApplicationNo(String applicationNo) {
		List<Tuple> resultList = programMonitoringRepository.getProgramMonitoringByApplicationNo(applicationNo);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getCourseService() {
		List<Tuple> resultList = programMonitoringRepository.getCourseService();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getCourseByInstituteId(Integer institute_id, Integer course_type_id) {
		if(course_type_id == 26) {
			List<Tuple> resultList = programMonitoringRepository.getAccreditedCourse(institute_id);
			List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
			return DtlsJson;
		}else {
			List<Tuple> resultList = programMonitoringRepository.getNonAccreditedCourse(institute_id);
			List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
			return DtlsJson;
		}
		
	}

	

}
