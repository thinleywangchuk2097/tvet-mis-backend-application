package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AccreditedCoursedto;
import com.moesd.tvet.mis.backend.application.dto.AssignedRecsDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.AccreditedCourse;
import com.moesd.tvet.mis.backend.application.model.AccreditedCourseQualityStandardResponse;
import com.moesd.tvet.mis.backend.application.model.AccreditorTaskAssignment;
import com.moesd.tvet.mis.backend.application.model.RecMemberTaskAssignment;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.RecMemberTaskAssignmentRepository;
import com.moesd.tvet.mis.backend.application.repository.AccreditedCourseRepository;
import com.moesd.tvet.mis.backend.application.repository.AccreditorTaskAssignmentRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.AccreditedCourseService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccreditedCourseServiceImpl implements AccreditedCourseService {

	private final AccreditedCourseRepository accreditedCourseRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final DocumentFileUploadService documentFileUploadService;
	private final RoleServiceRepository roleServiceRepository;
	private final ObjectToJson objectTojson;
	private final RecMemberTaskAssignmentRepository recMemberTaskAssignmentRepository;
	private final AccreditorTaskAssignmentRepository accreditorTaskAssignmentRepository;
	
	
	@Override
	public ResponseEntity<?> registerAccreditedCourse(AccreditedCoursedto request) {
		try {

			// Validation
			if (request.getServiceId() == null)
				throw new RuntimeException("serviceId is required");

			if (request.getAssignedRoleId() == null)
				throw new RuntimeException("assignedRoleId is required");

			if (request.getStatusId() == null)
				throw new RuntimeException("statusId is required");

			Integer serviceId = request.getServiceId();
			Integer assignedRoleId = request.getAssignedRoleId();
			String userId = request.getUserId();
			Integer locationId = 14;

			// Validate service
			serviceMasterRepository.findById(serviceId).orElseThrow(() -> new RuntimeException("Service Id not found"));

			// Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);

			// Build main entity
			AccreditedCourse course = AccreditedCourse.builder().applicationNo(applicationNo)
					.instituteId(request.getInstituteId()).courseId(request.getCourseId())
					.curriculumId(request.getCurriculumId()).serviceId(serviceId)
					.feesPerTrainee(request.getFeesPerTrainee())
					.enrolmentCapacity(request.getEnrolmentCapacity())
					.sectorId(request.getSectorId()).is_active(request.getIs_active())
					.registration_date(request.getRegistration_date()).validity_date(request.getValidity_date())
					.statusId(request.getStatusId()).createdBy(request.getCreatedBy()).updatedBy(request.getUpdatedBy())
					.createdAt(new java.util.Date()).updatedAt(new java.util.Date()).build();
			// Build AccreditedCourseQualityStandardResponse that were added while course add 
			if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
				List<AccreditedCourseQualityStandardResponse> qualitystandards = request.getQualityStandards()
						.stream()
						.map(qualitystandardsDto -> AccreditedCourseQualityStandardResponse.builder()
								.standardId(qualitystandardsDto.getStandardId())
								.responseId(qualitystandardsDto.getResponseId())
								.accreditedCourse(course)
								.build())
						.collect(Collectors.toList());
				course.setQualityStandardResponses(qualitystandards);
			}

			// Save (cascade handles children)
			AccreditedCourse saved = accreditedCourseRepository.save(course);

			// Workflow
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RuntimeException("Unclaimed status not found"));

			var workflow = workTaskFlowService.createWorkflow(applicationNo, request.getApplicantName(), serviceId,
					request.getStatusId(), assignedRoleId, request.getRemarks());

			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, request.getAssignedUserId(),
					workflow, request.getRemarks(), locationId);

			// Documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo, "accredited_course",
						serviceId, userId, null);
			}

			// Response
			return ResponseEntity.status(201).body(java.util.Map.of("applicationNo", applicationNo, "id", saved.getId(),
					"status", 201, "message", "Accredited course submitted successfully"));

		}  catch (Exception e) {
	        log.error("Failed to submit accredited course", e);  // Log for debugging
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Failed to submit accredited course");  // User-friendly only
	    }
	}

	@Override
	public List<ObjectNode> getAccreditedCourseByApplicationNo(String application_no) {
		List<Tuple> resultList = accreditedCourseRepository
				.findByInstituteAccreditedCourseApplicationNo(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getAccreditedCourseDetailsByUserId(String user_id) {
		List<Tuple> resultList = accreditedCourseRepository.getAccreditedCourseDetailsByUserId(user_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public ResponseEntity<?> verifyAccreditedCourse(AccreditedCoursedto request) {
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
			// Integer locationId = 14;
			// Get task status
			Integer taskStatusId;
			if (statusId == 57 || statusId == 126) {
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
			AccreditedCourse existingAccreditedCourse = accreditedCourseRepository
					.findByApplicationNo(request.getApplicationNo()) // Returns Optional
					.orElseThrow(() -> new RecordNotFoundException(
							"Non Accredited Course not found with applicationNo: " + request.getApplicationNo()));

			// Update InstituteRegistration entity
			existingAccreditedCourse.setStatusId(request.getStatusId());
			existingAccreditedCourse.setUpdatedAt(new java.util.Date());
			existingAccreditedCourse.setUpdatedBy(request.getUpdatedBy());
			if(statusId == 126) {
				existingAccreditedCourse.setRenewalDate(LocalDateTime.now().plusYears(1));
			}
			//Update ONLY existing records
	        if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
	            
	            // Get existing quality standards for this registration
	            List<AccreditedCourseQualityStandardResponse> existingStandards = 
	            		existingAccreditedCourse.getQualityStandardResponses();
	           
	            if (existingStandards != null && !existingStandards.isEmpty()) {
	                Map<Long, AccreditedCourseQualityStandardResponse> existingMap = 
	                    existingStandards.stream()
	                        .collect(Collectors.toMap(
	                        		AccreditedCourseQualityStandardResponse::getStandardId,
	                            standard -> standard
	                        ));
	                
	                // Update only existing quality standards
	                for (var qualityDto : request.getQualityStandards()) {
	                    Long standardId = qualityDto.getStandardId();
	                    AccreditedCourseQualityStandardResponse existingStandard = existingMap.get(standardId);
	                    
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
			AccreditedCourse savedRegistration = accreditedCourseRepository.save(existingAccreditedCourse);

			//starts
			
			//save accreditors
			if (request.getAssignedAccreditors() != null && !request.getAssignedAccreditors().isEmpty()) {

			    List<AccreditorTaskAssignment> assignments = request.getAssignedAccreditors()
			            .stream()
			            .map(accreditor -> AccreditorTaskAssignment.builder()
			                    .userId(accreditor.getUserId())
			                    .ApplicationNo(request.getApplicationNo())
			                    .serviceId(serviceId)
			                    .build())
			            .toList();

			    accreditorTaskAssignmentRepository.saveAll(assignments);
			}
			//save REC members and its assignment to task
			if (request.getAssignedRecs() != null && !request.getAssignedRecs().isEmpty()) {
				List<RecMemberTaskAssignment> assignments = request.getAssignedRecs()
			            .stream()
			            .map(dto -> RecMemberTaskAssignment.builder()
			                    .userId(dto.getUserId())
			                    .ApplicationNo(request.getApplicationNo())
			                    .serviceId(serviceId)
			                    .build())
			            .toList();

			    recMemberTaskAssignmentRepository.saveAll(assignments);
				
				List<String> userIds = request.getAssignedRecs().stream()
				        .map(AssignedRecsDto::getUserId)
				        .filter(Objects::nonNull)
				        .filter(id -> !id.trim().isEmpty())
				        .collect(Collectors.toList());

				// Add current userId only if it is not null
				if (request.getUserId() != null) {
				    userIds.add(String.valueOf(request.getUserId()));
				}

				String assignedRecString = String.join(",", userIds);

				workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
						request.getUserId(), request.getRemarks(), serviceId, null);

				// update task flow
				workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId,
						roleService.getNextRoleId(),assignedRecString, request.getRemarks());
			} else {
				workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
						request.getUserId(), request.getRemarks(), serviceId, null);

				// update task flow
				workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId,
						roleService.getNextRoleId(), request.getUserId(), request.getRemarks());
			}

			// Save documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), request.getApplicationNo(),
						"NonAccreditedCourse", serviceId, actorId, null);
			}

			// Return response
			return ResponseEntity
					.ok(Map.of("applicationNo", request.getApplicationNo(), "id", savedRegistration.getId(), "status",
							HttpStatus.OK.value(), "message", "Accredited Course updated successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
	        log.error("Failed to update Accredited Course", e);  // Log for debugging
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("message", "Failed to update Accredited Course", 
	                        "timestamp", LocalDateTime.now()));
	    }
	}

	@Override
	public List<ObjectNode> getAccreditedApprovedCourseByUserId(String user_id) {
		List<Tuple> resultList = accreditedCourseRepository.getAccreditedApprovedCourseByUserId(user_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getAccreditedCourseByInstituteId(String institute_id) {
		List<Tuple> resultList = accreditedCourseRepository.getAccreditedCourseByInstituteId(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	

}
