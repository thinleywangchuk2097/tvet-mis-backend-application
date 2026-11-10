package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AccreditedCoursedto;
import com.moesd.tvet.mis.backend.application.dto.AssignedRecsDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.AccreditedCourse;
import com.moesd.tvet.mis.backend.application.model.AccreditedCourseAudit;
import com.moesd.tvet.mis.backend.application.model.AccreditedCourseQualityStandardResponseAudit;
import com.moesd.tvet.mis.backend.application.model.AccreditedCourseQualityStandardResponse;
import com.moesd.tvet.mis.backend.application.model.AccreditorTaskAssignment;
import com.moesd.tvet.mis.backend.application.model.RecMemberTaskAssignment;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.RecMemberTaskAssignmentRepository;
import com.moesd.tvet.mis.backend.application.repository.AccreditedCourseRepository;
import com.moesd.tvet.mis.backend.application.repository.AccreditedCourseAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.AccreditorTaskAssignmentRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.SelfAssessmentReportRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListRepository;
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
	private final TaskFlowListRepository taskFlowListRepository;
	private final SelfAssessmentReportRepository selfAssessmentReportRepository;
	private final AccreditedCourseAuditRepository accreditedCourseAuditRepository;

	private static final int ROLE_REC = 23;

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
					.instituteId(request.getInstituteId()).curriculumId(request.getCurriculumId()).serviceId(serviceId)
					.feesPerTrainee(request.getFeesPerTrainee()).enrolmentCapacity(request.getEnrolmentCapacity())
					.leadTrainerCidNo(request.getLeadTrainerCidNo()).leadTrainerName(request.getLeadTrainerName())
					.genderId(request.getGenderId()).professionalExperience(request.getProfessionalExperience())
					.qualificationId(request.getQualificationId()).registration_date(request.getRegistration_date())
					.validity_date(request.getValidity_date()).statusId(request.getStatusId()).ncsId(request.getNcsId())
					.createdBy(request.getCreatedBy()).updatedBy(request.getUpdatedBy()).createdAt(new java.util.Date())
					.updatedAt(new java.util.Date()).build();
			// Build AccreditedCourseQualityStandardResponse that were added while course
			// add
			if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
				List<AccreditedCourseQualityStandardResponse> qualitystandards = request.getQualityStandards().stream()
						.map(qualitystandardsDto -> AccreditedCourseQualityStandardResponse.builder()
								.standardId(qualitystandardsDto.getStandardId())
								.responseId(qualitystandardsDto.getResponseId()).accreditedCourse(course).build())
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

		} catch (Exception e) {
			log.error("Failed to submit accredited course", e); // Log for debugging
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit accredited course"); 
																														
		}
	}

	@Override
	public List<ObjectNode> getAccreditedCourseByApplicationNo(String applicationNo) {
	    return objectTojson._toJson(accreditedCourseRepository.findByInstituteAccreditedCourseApplicationNo(applicationNo));
	}

	@Override
	public List<ObjectNode> getAccreditedCourseDetailsByUserId(String userId) {
	    return objectTojson._toJson(accreditedCourseRepository.getAccreditedCourseDetailsByUserId(userId));
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
			Integer statusId = request.getStatusId();
			String actorId = String.valueOf(request.getUpdatedBy());
			// Get task status
			Integer taskStatusId;
			if (statusId == 57) {
				taskStatusId = dropdownManagementRepository.findChildById(20)// task completed Id
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			} else {
				taskStatusId = dropdownManagementRepository.findChildById(18) // initiated taskId
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}

			// Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// Fetch next role
			RoleService roleService = roleServiceRepository.getNextAssignedRole(assignedRoleId, serviceId, statusId)
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

			// REC stage with several REC members (same rule as InstituteRegistrationServiceImpl):
			// every assigned REC member must complete the task; only the last one forwards it to the next
			// role. A rejection (58 / 60) by any REC member still goes ahead straight away.
			if (Integer.valueOf(ROLE_REC).equals(assignedRoleId) && statusId != 58 && statusId != 60
					&& (request.getAssignedRecs() == null || request.getAssignedRecs().isEmpty())) {
				ResponseEntity<?> partial = recordRecCompletion(request);
				if (partial != null)
					return partial; // other REC members still pending -> stays with REC
			}

			// Find existing registration by applicationNo
			AccreditedCourse existingAccreditedCourse = accreditedCourseRepository
					.findByApplicationNo(request.getApplicationNo()) // Returns Optional
					.orElseThrow(() -> new RecordNotFoundException(
							"Non Accredited Course not found with applicationNo: " + request.getApplicationNo()));

			// Renewal (service 54) statuses: 115 Revision (submitted), 56 Verified, 58 Rejected, 57 Approved.
			// The renewal submission overwrites this row: keep the approved before-data in the audit table first
			boolean isRenewal = serviceId == RENEWAL_SERVICE_ID;
			if (isRenewal && statusId == 115) {
				saveAccreditedCourseAudit(existingAccreditedCourse);
			}

			// Update entity
			existingAccreditedCourse.setStatusId(request.getStatusId());
			existingAccreditedCourse.setUpdatedAt(new java.util.Date());
			existingAccreditedCourse.setUpdatedBy(request.getUpdatedBy());
			existingAccreditedCourse.setEnrolmentCapacity(request.getEnrolmentCapacity());		
			if (isRenewal && statusId == 57) {
				existingAccreditedCourse.setRenewalDate(LocalDateTime.now().plusYears(1));
			}
			// On approval: validity 5 years if the institute is accredited
			// (has a Self Assessment Report), otherwise 3 years
			if (statusId == 57) {
				existingAccreditedCourse.setValidity_date(LocalDate.now()
						.plusYears(validityYears(existingAccreditedCourse.getInstituteId())).toString());
			}
			// Update ONLY existing records
			if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {

				// Get existing quality standards for this registration
				List<AccreditedCourseQualityStandardResponse> existingStandards = existingAccreditedCourse
						.getQualityStandardResponses();

				if (existingStandards != null && !existingStandards.isEmpty()) {
					Map<Long, AccreditedCourseQualityStandardResponse> existingMap = existingStandards.stream()
							.collect(Collectors.toMap(AccreditedCourseQualityStandardResponse::getStandardId,
									standard -> standard));

					// Update only existing quality standards
					for (var qualityDto : request.getQualityStandards()) {
						Long standardId = qualityDto.getStandardId();
						AccreditedCourseQualityStandardResponse existingStandard = existingMap.get(standardId);

						if (existingStandard != null) {
							// Update only responseId and remarks
							existingStandard.setResponseId(qualityDto.getResponseId());
							existingStandard.setRemarks(qualityDto.getRemarks());

						} else {
							log.warn("Quality standard with standardId {} not found, skipping", standardId);
						}
					}

				}
			}
			// Save the updated registration
			AccreditedCourse savedRegistration = accreditedCourseRepository.save(existingAccreditedCourse);

			// Save accreditor
			if (request.getAssignedAccreditors() != null && !request.getAssignedAccreditors().isEmpty()) {
				List<AccreditorTaskAssignment> assignments = request.getAssignedAccreditors().stream()
						.map(accreditor -> AccreditorTaskAssignment.builder().userId(accreditor.getUserId())
								.ApplicationNo(request.getApplicationNo()).serviceId(serviceId).build())
						.toList();
				accreditorTaskAssignmentRepository.saveAll(assignments);
			}
			// save REC members and its assignment to task
			if (request.getAssignedRecs() != null && !request.getAssignedRecs().isEmpty()) {
				List<RecMemberTaskAssignment> assignments = request.getAssignedRecs().stream()
						.map(dto -> RecMemberTaskAssignment.builder().userId(dto.getUserId())
								.ApplicationNo(request.getApplicationNo()).serviceId(serviceId).build())
						.toList();

				recMemberTaskAssignmentRepository.saveAll(assignments);

				List<String> userIds = request.getAssignedRecs().stream().map(AssignedRecsDto::getUserId)
						.filter(Objects::nonNull).filter(id -> !id.trim().isEmpty()).collect(Collectors.toList());

				// Add current userId only if it is not null
				if (request.getUserId() != null) {
					userIds.add(String.valueOf(request.getUserId()));
				}

				String assignedRecString = String.join(",", userIds);

				workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
						request.getUserId(), request.getRemarks(), serviceId, null);

				// update task flow
				workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId,
						roleService.getNextRoleId(), assignedRecString, request.getRemarks());
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
			log.error("Failed to update Accredited Course", e); // Log for debugging
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to update Accredited Course", "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getAccreditedApprovedCourseByUserId(String userId) {
	    return objectTojson._toJson(accreditedCourseRepository.getAccreditedApprovedCourseByUserId(userId));
	}
	
	@Override
	public List<ObjectNode> getAccreditedApprovedCertificateCourseByUserId(String userId) {
		 return objectTojson._toJson(accreditedCourseRepository.getAccreditedApprovedCertificateCourseByUserId(userId));
	}
	
	@Override
	public List<ObjectNode> getAccreditedCourseByInstituteId(String instituteId) {
	    return objectTojson._toJson(accreditedCourseRepository.getAccreditedCourseByInstituteId(instituteId));
	}

	@Override
	public List<ObjectNode> curriculumExist(Long curriculumId, String registrationNo) {
	    return objectTojson._toJson(accreditedCourseRepository.curriculumExist(curriculumId, registrationNo));
	}

	@Override
	public List<ObjectNode> getListSelectedBQFTraineeForExcel(String applicationNo) {
	    return objectTojson._toJson(accreditedCourseRepository.getListSelectedBQFTraineeForExcel(applicationNo));
	}

	@Override
	public List<ObjectNode> getListPassTraineeForCertificatePrinting(String applicationNo, Integer instituteId,
			Integer serviceId, Integer certificationLevelId, Integer programmeId) {

		List<Tuple> passTrainee = accreditedCourseRepository.getListPassTraineeForCertificatePrinting(applicationNo,
				instituteId, serviceId, certificationLevelId, programmeId);

		return objectTojson._toJson(passTrainee);
	}

	@Override
	public List<ObjectNode> getServicesAssessementResult() {
	    return objectTojson._toJson(accreditedCourseRepository.getServicesAssessementResult());
	}

	@Override
	public List<ObjectNode> getProgrammesCertification(Integer instituteId, Integer serviceId,
			Integer certificationLevelId) {

		List<Tuple> listOfProgrammes;

		if (serviceId == 39 || serviceId == 41) {
			listOfProgrammes = accreditedCourseRepository.getRPLProgrammeForCertification(instituteId, serviceId,
					certificationLevelId);
		} else {
			listOfProgrammes = accreditedCourseRepository.getBQFProgrammeForCertification(instituteId, serviceId,
					certificationLevelId);
		}
		return objectTojson._toJson(listOfProgrammes);
	}

	/**
	 * Marks the acting REC member's row in tbl_rec_member_task_assignment as done (remarks NULL = pending).
	 * Returns a response while other REC members are still pending (this member is taken off
	 * tbl_task_dtls.assigned_user_id and the course stays with REC); returns null when this was the last
	 * pending member (or no individual REC assignment exists), so the normal flow forwards it.
	 */
	private ResponseEntity<?> recordRecCompletion(AccreditedCoursedto request) {
		String applicationNo = request.getApplicationNo();
		Long pending = recMemberTaskAssignmentRepository.getRECMemberCount(applicationNo);
		if (pending == null || pending == 0)
			return null;

		String recUserId = request.getRecMemberUserId() != null ? request.getRecMemberUserId().trim() : "";
		if (recUserId.isEmpty())
			throw new RecordNotFoundException("REC member user id is required");

		RecMemberTaskAssignment recMember = recMemberTaskAssignmentRepository
				.findRecMemberUser(recUserId, applicationNo)
				.orElseThrow(() -> new RecordNotFoundException(
						"You are not an assigned REC member for this application"));
		if (recMember.getRemarks() != null)
			throw new RecordNotFoundException("You have already completed this task");

		recMember.setRemarks(request.getRemarks() != null && !request.getRemarks().isBlank()
				? request.getRemarks().trim()
				: "Completed");
		recMemberTaskAssignmentRepository.save(recMember);

		if (pending <= 1)
			return null; // last REC member -> forward to the next role

		TaskFlowList taskFlow = taskFlowListRepository.findByApplicationNo(applicationNo);
		if (taskFlow != null && taskFlow.getAssignedUserId() != null && !taskFlow.getAssignedUserId().isBlank()) {
			String remaining = Arrays.stream(taskFlow.getAssignedUserId().split(",")).map(String::trim)
					.filter(id -> !id.isEmpty() && !id.equals(recUserId)).collect(Collectors.joining(","));
			taskFlow.setAssignedUserId(remaining.isEmpty() ? null : remaining);
			taskFlowListRepository.save(taskFlow);
		}

		long stillPending = pending - 1;
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("applicationNo", applicationNo);
		body.put("status", HttpStatus.OK.value());
		body.put("pendingRecMembers", stillPending);
		body.put("message", "Your action is recorded. Waiting for " + stillPending
				+ " more REC member(s) before the course moves to the next stage.");
		return ResponseEntity.ok(body);
	}

	

	/** Accredited course renewal service id */
	private static final int RENEWAL_SERVICE_ID = 54;

	/** Course validity: 5 years when the institute is accredited (has a Self Assessment Report), else 3 years. */
	private int validityYears(Integer instituteId) {
		if (instituteId == null)
			return 3;
		Long reports = selfAssessmentReportRepository.countByInstitute(instituteId);
		return reports != null && reports > 0 ? 5 : 3;
	}

	/** Saves a snapshot of the accredited course and its quality standard responses before they are changed. */
	private void saveAccreditedCourseAudit(AccreditedCourse course) {
		AccreditedCourseAudit courseAudit = AccreditedCourseAudit.builder()
				.accreditedCourseId(course.getId())
				.applicationNo(course.getApplicationNo())
				.instituteId(course.getInstituteId())
				.curriculumId(course.getCurriculumId())
				.ncsId(course.getNcsId())
				.feesPerTrainee(course.getFeesPerTrainee())
				.enrolmentCapacity(course.getEnrolmentCapacity())
				.registration_date(course.getRegistration_date())
				.validity_date(course.getValidity_date())
				.statusId(course.getStatusId())
				.serviceId(course.getServiceId())
				.leadTrainerCidNo(course.getLeadTrainerCidNo())
				.genderId(course.getGenderId())
				.leadTrainerName(course.getLeadTrainerName())
				.professionalExperience(course.getProfessionalExperience())
				.qualificationId(course.getQualificationId())
				.createdBy(course.getCreatedBy())
				.createdAt(course.getCreatedAt())
				.updatedBy(course.getUpdatedBy())
				.updatedAt(course.getUpdatedAt())
				.renewalDate(course.getRenewalDate())
				.build();

		// child rows point to the course audit (saved together through the cascade)
		if (course.getQualityStandardResponses() != null) {
			for (AccreditedCourseQualityStandardResponse r : course.getQualityStandardResponses()) {
				courseAudit.getQualityStandardResponses().add(AccreditedCourseQualityStandardResponseAudit.builder()
						.qualityStandardResponseId(r.getId())
						.standardId(r.getStandardId())
						.responseId(r.getResponseId())
						.remarks(r.getRemarks())
						.accreditedCourseAudit(courseAudit)
						.build());
			}
		}
		accreditedCourseAuditRepository.save(courseAudit);
	}
}
