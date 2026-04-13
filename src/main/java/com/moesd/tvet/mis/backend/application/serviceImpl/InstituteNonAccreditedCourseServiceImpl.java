package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteNonAccreditedCoursedto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.InstituteNonAccreditedCourse;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.InstituteNonAccreditedCourseRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.InstituteNonAccreditedCourseService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class InstituteNonAccreditedCourseServiceImpl implements InstituteNonAccreditedCourseService {

	private final GenerateApplicationNumber generateApplicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final DocumentFileUploadService documentFileUploadService;
	private final InstituteNonAccreditedCourseRepository instituteNonAccreditedCourseRepository;
	private final ObjectToJson objectTojson;
	private final RoleServiceRepository roleServiceRepository;
	
	
	@Override
	public ResponseEntity<?> submitNonAccreditedCourse(InstituteNonAccreditedCoursedto request) {
		try {
			// 1. Validate required fields
			if (request.getServiceId() == null)
				throw new RecordNotFoundException("serviceId is required");

			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assigned RoleId is required");

			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");


			Integer serviceId = request.getServiceId();
			Integer assignedRoleId = request.getAssignedRoleId();
			String userId = request.getUserId();
			Integer locationId = 14;
			String applicantName = request.getCourseTitle(); // Using course title as applicant name

			// 2. Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// 3. Get unclaimed statusId (you may need different ID for non-accredited
			// course)
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RecordNotFoundException("Unclaimed status not found"));

			// 4. Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);

			// 5. Build entity
			InstituteNonAccreditedCourse course = InstituteNonAccreditedCourse.builder().applicationNo(applicationNo)
					.instituteId(request.getInstituteId()).courseTitle(request.getCourseTitle()).theoryHour(request.getTheoryHour())
					.practicalHour(request.getPracticalHour()).ojtHour(request.getOjtHour())
					.feesPerTrainee(request.getFeesPerTrainee()).enrolmentCapacity(request.getEnrolmentCapacity())
					.certificateLevelId(request.getCertificateLevelId()).curriculumTypeId(request.getCurriculumTypeId())
					.statusId(request.getStatusId()).registrationDate(new Date()).createdBy(request.getCreatedBy())
					.createdAt(LocalDateTime.now()).build();

			// 6. Save proposal
			instituteNonAccreditedCourseRepository.save(course);

			// 7. Create workflow
			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, applicantName, serviceId,
					request.getStatusId(), assignedRoleId, request.getRemarks());

			// 8. Create task flow
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, request.getAssignedUserId(),
					workflow, request.getRemarks(), locationId);

			// 9. Save documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo,
						"institute_non_accredited_course", // module name
						serviceId, userId, null);
			}

			// 10. Return response
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("applicationNo", applicationNo, "status", HttpStatus.CREATED.value()));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to submit non-accredited course", "error", e.getMessage(),
							"timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getNonAccreditedCourseByApplicationNo(String application_no) {
		List<Tuple> resultList = instituteNonAccreditedCourseRepository.getNonAccreditedCourseByApplicationNo(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public ResponseEntity<?> verifyNonAccreditedCourse(InstituteNonAccreditedCoursedto request) {
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
			InstituteNonAccreditedCourse existingNonAccreditedCourse = instituteNonAccreditedCourseRepository
					.findByApplicationNo(request.getApplicationNo()) // Returns Optional
					.orElseThrow(() -> new RecordNotFoundException(
							"Non Accredited Course not found with applicationNo: " + request.getApplicationNo()));
			
			// Update InstituteRegistration entity
			existingNonAccreditedCourse.setStatusId(request.getStatusId());
			existingNonAccreditedCourse.setUpdatedAt(LocalDateTime.now());
			existingNonAccreditedCourse.setUpdatedBy(request.getUpdatedBy());

			// Save the updated registration
			InstituteNonAccreditedCourse savedRegistration = instituteNonAccreditedCourseRepository.save(existingNonAccreditedCourse);
			
			
			workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
					request.getUserId(), request.getRemarks(), serviceId, null);

			// update task flow
			workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, roleService.getNextRoleId(),
					request.getUserId(), request.getRemarks());

			// Save documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), request.getApplicationNo(),
						"NonAccreditedCourse", serviceId, actorId, null);
			}

			// Return response
			return ResponseEntity
					.ok(Map.of("applicationNo", request.getApplicationNo(), "id", savedRegistration.getId(), "status",
							HttpStatus.OK.value(), "message", "Non Accredited Course registered successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to update Non Accredited Course registration", "error", e.getMessage(),
							"timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getNonAccreditedCourseDetailsByUserId(String user_id) {
		List<Tuple> resultList = instituteNonAccreditedCourseRepository.getNonAccreditedCourseDetailsByUserId(user_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

}
