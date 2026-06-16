package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.MonitoringAssessmentDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.MonitoringAssessment;
import com.moesd.tvet.mis.backend.application.model.MonitoringAssessmentCheckList;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.MonitoringAssessmentRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.MonitoringAssessmentService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonitoringAssessmentServiceImpl implements MonitoringAssessmentService{
	
	private final MonitoringAssessmentRepository monitoringAssessmentRepository;
	private final ObjectToJson objectTojson;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final RoleServiceRepository roleServiceRepository;
	
	@Override
	public List<ObjectNode> getInstituteTypeDropdown() {
		List<Tuple> resultList = monitoringAssessmentRepository.getInstituteTypeDropdown();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getInstituteDropdown(String service_id) {
		List<Tuple> resultList = monitoringAssessmentRepository.getInstituteDropdown(service_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public ResponseEntity<?> submitMonitoringAssessment(MonitoringAssessmentDto request) {
		try {

			// Validation
			if (request.getServiceId() == null)
				throw new RuntimeException("serviceId is required");

			if (request.getStatusId() == null)
				throw new RuntimeException("statusId is required");

			Integer serviceId = request.getServiceId();
//			String assignedUserId = request.getAssignedUserId();
//			String userId = request.getUserId();
//			Integer locationId = 14;

			// Validate service
			serviceMasterRepository.findById(serviceId).orElseThrow(() -> new RuntimeException("Service Id not found"));

			// Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(47);

			// Build main entity
			MonitoringAssessment assessment = MonitoringAssessment.builder().applicationNo(applicationNo)
					.instituteId(request.getInstituteId()).dzongkhagId(request.getDzongkhagId())
					.exactLocation(request.getExactLocation()).instituteName(request.getInstituteName())
					.monitoringDate(request.getMonitoringDate()).registrationNo(request.getRegistrationNo())
					.serviceId(request.getServiceId())
					.statusId(request.getStatusId()).createdBy(request.getCreatedBy())
					.createdAt(new java.util.Date()).build();
			// Build MonitoringAssessmentCheckList 
			if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
				List<MonitoringAssessmentCheckList> qualitystandards = request.getQualityStandards()
						.stream()
						.map(qualitystandardsDto -> MonitoringAssessmentCheckList.builder()
								.standardId(qualitystandardsDto.getStandardId())
								.responseId(qualitystandardsDto.getResponseId())
								.remarks(qualitystandardsDto.getRemarks())
								.monitoringAssessment(assessment)
								.build())
						.collect(Collectors.toList());
				assessment.setChecklists(qualitystandards);
			}

			// Save (cascade handles children)
			MonitoringAssessment saved = monitoringAssessmentRepository.save(assessment);

			// Workflow
//			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
//					.orElseThrow(() -> new RuntimeException("Unclaimed status not found"));
//
//			var workflow = workTaskFlowService.createWorkflow(applicationNo, request.getInstituteName(), serviceId,
//					request.getStatusId(), assignedRoleId, request.getRemarks());
//
//			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, request.getAssignedUserId(),
//					workflow, request.getRemarks(), locationId);

			

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
	public List<ObjectNode> getMonitoringAssessment(String user_id) {
		List<Tuple> resultList = monitoringAssessmentRepository.getMonitoringAssessment(user_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public ResponseEntity<?> verifyMonitoringAssessment(MonitoringAssessmentDto request) {
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
			Integer actorId = request.getUpdatedBy();
			Integer locationId = 14;
			// Get task status
			Integer taskStatusId;
			if (statusId == 57 || statusId == 104 ) {
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
			MonitoringAssessment monitoringAssessment = monitoringAssessmentRepository
					.findByApplicationNo(request.getApplicationNo()) // Returns Optional
					.orElseThrow(() -> new RecordNotFoundException(
							"Monitoring Assessment not found with applicationNo: " + request.getApplicationNo()));

			// Update InstituteRegistration entity
			monitoringAssessment.setStatusId(request.getStatusId());
			monitoringAssessment.setUpdatedAt(new java.util.Date());
			monitoringAssessment.setUpdatedBy(request.getUpdatedBy());
			//Update ONLY existing records
	        if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
	            
	            // Get existing quality standards for this registration
	            List<MonitoringAssessmentCheckList> existingStandards = 
	            		monitoringAssessment.getChecklists();
	           
	            if (existingStandards != null && !existingStandards.isEmpty()) {
	                Map<Long, MonitoringAssessmentCheckList> existingMap = 
	                    existingStandards.stream()
	                        .collect(Collectors.toMap(
	                        		MonitoringAssessmentCheckList::getStandardId,
	                            standard -> standard
	                        ));
	                
	                // Update only existing quality standards
	                for (var qualityDto : request.getQualityStandards()) {
	                    Long standardId = qualityDto.getStandardId();
	                    MonitoringAssessmentCheckList existingStandard = existingMap.get(standardId);
	                    
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
	        MonitoringAssessment savedMonitoringAssessment = monitoringAssessmentRepository.save(monitoringAssessment);
            if(statusId == 57 || statusId == 104 ) {
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
	public List<ObjectNode> getMonitoringAssessmentByApplicationNo(String applicationNo) {
		List<Tuple> resultList = monitoringAssessmentRepository.getMonitoringAssessmentByApplicationNo(applicationNo);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

}
