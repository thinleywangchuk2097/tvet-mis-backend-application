package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CourseEnrollmentTraineeAppdto;
import com.moesd.tvet.mis.backend.application.dto.SelectedTraineedto;
import com.moesd.tvet.mis.backend.application.dto.TraineeInternaldto;
import com.moesd.tvet.mis.backend.application.dto.TraineeMarksdto;
import com.moesd.tvet.mis.backend.application.dto.TraineeStatusdto;
import com.moesd.tvet.mis.backend.application.dto.TraineeVivadto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentApp;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentTraineeApp;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.CourseEnrollmentAppRepository;
import com.moesd.tvet.mis.backend.application.repository.CourseEnrollmentTraineeAppRepository;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.CourseEnrollmentTraineeAppService;
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
public class CourseEnrollmentTraineeAppServiceImpl implements CourseEnrollmentTraineeAppService {

	private final CourseEnrollmentTraineeAppRepository courseEnrollmentTraineeAppRepository;
	private final CourseEnrollmentAppRepository courseEnrollmentAppRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final RoleServiceRepository roleServiceRepository;
	private final DocumentFileUploadService documentFileUploadService;
	private final ObjectToJson objectTojson;
	private final WorkTaskFlowService workTaskFlowService;
	private final DropdownManagementRepository dropdownManagementRepository;

	@Override
	@Transactional
	public ResponseEntity<?> submitTrainee(CourseEnrollmentTraineeAppdto request) {
		try {

			// Validation
			if (request.getServiceId() == null)
				throw new RuntimeException("serviceId is required");

			if (request.getStatusId() == null)
				throw new RuntimeException("statusId is required");

			// Validate service existence
			serviceMasterRepository.findById(request.getServiceId())
					.orElseThrow(() -> new RuntimeException("Service Id not found"));

			// Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(request.getServiceId());
			CourseEnrollmentApp course = courseEnrollmentAppRepository.findByApplicationNo(request.getApplicationNo())
					.orElseThrow(() -> new RuntimeException("Course not found"));
			// Build entity
			CourseEnrollmentTraineeApp trainee = CourseEnrollmentTraineeApp.builder().applicationNo(applicationNo)
					.applicantName(request.getName()).emailId(request.getEmail()).mobileNo(request.getMobileNo())
					.course(course).academicQualificationId(request.getAcademicQualificationId())
					.cidNo(request.getCidNo()).referenceNo(request.getReferenceNo()).dob(request.getDob())
					.genderId(request.getGenderId()).traineeTypeId(request.getTraineeTypeId())
					.employmentStatusId(request.getEmploymentStatusId()).remarks(request.getRemarks())
					.presentDzongkhagId(request.getPresentDzongkhagId()).presentGewogId(request.getPresentGewogId())
					.parentOccupationId(request.getParentOccupationId())
					.parentMaritalStatusId(request.getParentMaritalStatusId()).statusId(request.getStatusId())
					.createdAt(new java.util.Date()).build();

			// Save entity
			courseEnrollmentTraineeAppRepository.save(trainee);

			// Documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo, "trainee_course_apply",
						request.getServiceId(), null, null);
			}

			// Response
			return ResponseEntity.status(201).body(
					Map.of("applicationNo", applicationNo, "status", 201, "message", "Trainee submitted successfully"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500)
					.body(Map.of("message", "Failed to submit Trainee", "error", e.getMessage()));
		}
	}

	@Override
	public List<ObjectNode> getCourseAppliedTraineesByApplicationNo(String application_no) {
		List<Tuple> resultList = courseEnrollmentTraineeAppRepository
				.getCourseAppliedTraineesByApplicationNo(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public ResponseEntity<?> selectedTrainee(SelectedTraineedto request) {
		try {
			// Validate required fields
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

			Integer locationId = 14;

			List<CourseEnrollmentTraineeApp> trainees = courseEnrollmentTraineeAppRepository
					.findByApplicationNo(request.getApplicationNo());

			if (trainees.isEmpty()) {
				throw new RecordNotFoundException("No trainees found for applicationNo: " + request.getApplicationNo());
			}

			// If traineeIds provided → filter
			if (request.getTraineeIds() != null && !request.getTraineeIds().isEmpty()) {
				System.out.println("inside getTraineeIds");
				// Get initiated statusId
				Integer taskStatusId = dropdownManagementRepository.findChildById(18)
						.orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));
				// Loop through each DTO
				for (TraineeStatusdto dto : request.getTraineeIds()) {
					CourseEnrollmentTraineeApp trainee = trainees.stream()
							.filter(t -> t.getId().equals(dto.getTraineeId())).findFirst().orElseThrow(
									() -> new RuntimeException("Trainee not found with ID: " + dto.getTraineeId()));
					// Update internal assessment
					trainee.setStatusId(dto.getStatusId());
				}
				// Save all updated trainees
				courseEnrollmentTraineeAppRepository.saveAll(trainees);
				// Create workflow
				WorkFlowList workflow = workTaskFlowService.createWorkflow(request.getApplicationNo(),
						request.getCourseName(), request.getServiceId(), request.getStatusId(),
						request.getAssignedRoleId(), request.getRemarks());

				// Create task flow
				workTaskFlowService.createTaskFlow(request.getApplicationNo(), taskStatusId,
						request.getAssignedRoleId(), request.getAssignedUserId(), workflow, request.getRemarks(),
						locationId);
			}

			// newly added
			if (request.getTraineeInternalAssessments() != null && !request.getTraineeInternalAssessments().isEmpty()) {
				// Get initiated statusId
				Integer taskStatusId = dropdownManagementRepository.findChildById(18)
						.orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));
				// Loop through each DTO
				for (TraineeInternaldto dto : request.getTraineeInternalAssessments()) {
					CourseEnrollmentTraineeApp trainee = trainees.stream()
							.filter(t -> t.getId().equals(dto.getTraineeId())).findFirst().orElseThrow(
									() -> new RuntimeException("Trainee not found with ID: " + dto.getTraineeId()));
					// Update internal assessment
					trainee.setInternalAssessment(String.valueOf(dto.getInternalAssessment()));
				}
				// Save all updated trainees
				courseEnrollmentTraineeAppRepository.saveAll(trainees);
				// Fetch next role
				RoleService roleService = roleServiceRepository
						.getNextAssignedRole(request.getAssignedRoleId(), request.getServiceId(), request.getStatusId())
						.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

				workTaskFlowService.updateWorkflow(request.getApplicationNo(), request.getStatusId(),
						request.getAssignedRoleId(), request.getUserId(), request.getRemarks(), request.getServiceId(),
						null);
				// update task flow
				workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId,
						roleService.getNextRoleId(), request.getUserId(), request.getRemarks());
			}
			// end newly added
			// Return response
			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", HttpStatus.CREATED.value()));

		} catch (Exception e) {
			log.error("Error submitting trainees : {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit trainees course", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> updateTraineeApplication(SelectedTraineedto request) {
		try {
			// Get task statusId
			Integer taskStatusId = dropdownManagementRepository.findChildById(20)// task completed Id
					.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			
			Integer resultId;
			// Validate required fields
			if (request.getCaStartDate() != null && request.getCaEndDate() != null) {
				CourseEnrollmentApp course = courseEnrollmentAppRepository
						.findByApplicationNo(request.getApplicationNo())
						.orElseThrow(() -> new RuntimeException("Course not found"));
				course.setCaStartDate(request.getCaStartDate());
				course.setCaEndDate(request.getCaEndDate());
				// save
				courseEnrollmentAppRepository.save(course);
			}

			List<CourseEnrollmentTraineeApp> trainees = courseEnrollmentTraineeAppRepository
					.findByApplicationNo(request.getApplicationNo());

			if (request.getTraineeMarks() != null && !request.getTraineeMarks().isEmpty()) {
				// Convert trainees → Map (id → entity)
				Map<Long, CourseEnrollmentTraineeApp> traineeMap = trainees.stream()
						.collect(Collectors.toMap(CourseEnrollmentTraineeApp::getId, t -> t));
				// Loop through incoming marks
				for (TraineeMarksdto dto : request.getTraineeMarks()) {
					CourseEnrollmentTraineeApp trainee = traineeMap.get(dto.getTraineeId());
					if (trainee == null) {
						throw new RuntimeException("Trainee not found with ID: " + dto.getTraineeId());
					}
					// Update fields
					if (dto.getTheoryAssessment() != null && dto.getPracticalAssessment() != null) {
						trainee.setTheoryAssessment(String.valueOf(dto.getTheoryAssessment()));
						trainee.setPracticalAssessment(String.valueOf(dto.getPracticalAssessment()));
						if (request.getCertificationlevelId() == 36) {
							if (dto.getTheoryAssessment() >= 40 && dto.getPracticalAssessment() >= 40) {
								resultId = 94;
								trainee.setResultStatusId(resultId);
							} else {
								resultId = 95;
								trainee.setResultStatusId(resultId);
							}
						} else {
							if (dto.getTheoryAssessment() == 91 && dto.getPracticalAssessment() == 91) {
								resultId = 94;
								trainee.setResultStatusId(resultId);
							} else {
								resultId = 95;
								trainee.setResultStatusId(resultId);
							}
						}

					}
					// if (dto.getPracticalAssessment() != null) {
					// trainee.setPracticalAssessment(String.valueOf(dto.getPracticalAssessment()));
					// }
				}
				// Save all updates
				courseEnrollmentTraineeAppRepository.saveAll(trainees);
			}
			if (request.getTraineeVivaAssessments() != null && !request.getTraineeVivaAssessments().isEmpty()) {
				// Build a quick lookup: traineeId -> entity
				Map<Long, CourseEnrollmentTraineeApp> traineeMap = trainees.stream()
						.collect(Collectors.toMap(CourseEnrollmentTraineeApp::getId, t -> t));
				// Apply updates
				for (TraineeVivadto dto : request.getTraineeVivaAssessments()) {
					CourseEnrollmentTraineeApp trainee = traineeMap.get(dto.getTraineeId());
					if (trainee == null) {
						throw new RuntimeException("Trainee not found with ID: " + dto.getTraineeId());
					}

					if (dto.getVivaAssessment() != null && dto.getPracticalAssessment() != null) {
						trainee.setVivaAssessment(String.valueOf(dto.getVivaAssessment()));
						trainee.setPracticalAssessment(String.valueOf(dto.getPracticalAssessment()));
						if (request.getCertificationlevelId() == 36) {
							if (dto.getVivaAssessment() >= 40 && dto.getPracticalAssessment() >= 40) {
								resultId = 94;
								trainee.setResultStatusId(resultId);
							} else {
								resultId = 95;
								trainee.setResultStatusId(resultId);
							}
						} else {
							if (dto.getVivaAssessment() == 91 && dto.getPracticalAssessment() == 91) {
								resultId = 94;
								trainee.setResultStatusId(resultId);
							} else {
								resultId = 95;
								trainee.setResultStatusId(resultId);
							}
						}
					}
					// if (dto.getPracticalAssessment() != null) {
					// trainee.setPracticalAssessment(String.valueOf(dto.getPracticalAssessment()));
					// }
				}
				// save changes
				courseEnrollmentTraineeAppRepository.saveAll(trainees);
			}
			// Fetch next role
			RoleService roleService = roleServiceRepository
					.getNextAssignedRole(request.getAssignedRoleId(), request.getServiceId(), request.getStatusId())
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

			workTaskFlowService.updateWorkflow(request.getApplicationNo(), request.getStatusId(),
					request.getAssignedRoleId(), request.getUserId(), request.getRemarks(), request.getServiceId(),
					null);
			// update task flow
			workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, roleService.getNextRoleId(),
					request.getUserId(), request.getRemarks());
			// Return response
			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", HttpStatus.CREATED.value()));

		} catch (Exception e) {
			log.error("Error submitting trainees : {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit trainees course", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getFailedTraineeDetails(String user_id, String course_id) {
		List<Tuple> resultList = courseEnrollmentTraineeAppRepository.getFailedTraineeDetails(user_id, course_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public ResponseEntity<?> selectUnselectTrainee(SelectedTraineedto request) {
		try {
			List<CourseEnrollmentTraineeApp> trainees = courseEnrollmentTraineeAppRepository
					.findByApplicationNo(request.getApplicationNo());

			if (trainees.isEmpty()) {
				throw new RecordNotFoundException("No trainees found for applicationNo: " + request.getApplicationNo());
			}
			// If traineeIds provided → filter
			if (request.getTraineeIds() != null && !request.getTraineeIds().isEmpty()) {
				// Loop through each DTO
				for (TraineeStatusdto dto : request.getTraineeIds()) {
					CourseEnrollmentTraineeApp trainee = trainees.stream()
							.filter(t -> t.getId().equals(dto.getTraineeId())).findFirst().orElseThrow(
									() -> new RuntimeException("Trainee not found with ID: " + dto.getTraineeId()));
					// Update internal assessment
					trainee.setStatusId(dto.getStatusId());
				}
				// Save all updated trainees
				courseEnrollmentTraineeAppRepository.saveAll(trainees);
			}
			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", HttpStatus.CREATED.value()));

		} catch (Exception e) {
			log.error("Error submitting trainees : {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit trainees course", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public ResponseEntity<?> submitReassessmentTrainees(SelectedTraineedto request) {
		try {
			// Validate required fields
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

			Integer locationId = 14;

			// Fetch existing failed trainees
			List<CourseEnrollmentTraineeApp> existingTrainees = courseEnrollmentTraineeAppRepository
					.getFailedTraineeReassessment(request.getUserId(), request.getCourseId());

			CourseEnrollmentApp course = courseEnrollmentAppRepository.findByApplicationNo(request.getApplicationNo())
					.orElseThrow(() -> new RuntimeException("Course not found"));

			if (existingTrainees.isEmpty()) {
				throw new RecordNotFoundException("No trainees found for applicationNo: " + request.getApplicationNo());
			}

			// Handle new reassessment trainees (creating new applications)
			if (request.getTraineeIds() != null && !request.getTraineeIds().isEmpty()) {
				log.info("Processing new reassessment trainees");
				Integer taskStatusId = dropdownManagementRepository.findChildById(18)
						.orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));

				List<CourseEnrollmentTraineeApp> newTrainees = new ArrayList<>();

				for (TraineeStatusdto dto : request.getTraineeIds()) {
					// Verify trainee exists in failed list
					CourseEnrollmentTraineeApp existingTrainee = existingTrainees.stream()
							.filter(t -> t.getId().equals(dto.getTraineeId())).findFirst().orElseThrow(
									() -> new RuntimeException("Trainee not found with ID: " + dto.getTraineeId()));

					// Generate application number
					String applicationNo = generateApplicationNumber.generateApplicationNumber(43);

					// Create new reassessment application using data from existing trainee
					CourseEnrollmentTraineeApp newTrainee = CourseEnrollmentTraineeApp.builder()
							.applicationNo(applicationNo).applicantName(existingTrainee.getApplicantName())
							.emailId(existingTrainee.getEmailId()).mobileNo(existingTrainee.getMobileNo())
							.statusId(dto.getStatusId()).course(course)
							.reAssessmentNo(existingTrainee.getReAssessmentNo() != null
									? existingTrainee.getReAssessmentNo() + 1
									: 1)
							.academicQualificationId(existingTrainee.getAcademicQualificationId())
							.cidNo(existingTrainee.getCidNo()).referenceNo(existingTrainee.getReferenceNo())
							.dob(existingTrainee.getDob()).genderId(existingTrainee.getGenderId())
							.traineeTypeId(existingTrainee.getTraineeTypeId())
							.employmentStatusId(existingTrainee.getEmploymentStatusId()).remarks(request.getRemarks())
							.presentDzongkhagId(existingTrainee.getPresentDzongkhagId())
							.presentGewogId(existingTrainee.getPresentGewogId())
							.parentOccupationId(existingTrainee.getParentOccupationId())
							.parentMaritalStatusId(existingTrainee.getParentMaritalStatusId())
							.createdAt(new java.util.Date()).build();

					newTrainees.add(newTrainee);
				}

				// Save all new trainees
				courseEnrollmentTraineeAppRepository.saveAll(newTrainees);

				// Create workflow
				WorkFlowList workflow = workTaskFlowService.createWorkflow(request.getApplicationNo(),
						request.getCourseName(), request.getServiceId(), request.getStatusId(),
						request.getAssignedRoleId(), request.getRemarks());

				// Create task flow
				workTaskFlowService.createTaskFlow(request.getApplicationNo(), taskStatusId,
						request.getAssignedRoleId(), request.getAssignedUserId(), workflow, request.getRemarks(),
						locationId);
			}

			// Handle internal assessment updates for existing trainees
			if (request.getTraineeInternalAssessments() != null && !request.getTraineeInternalAssessments().isEmpty()) {
				log.info("Updating internal assessments for existing trainees");
				Integer taskStatusId = dropdownManagementRepository.findChildById(18)
						.orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));

				for (TraineeInternaldto dto : request.getTraineeInternalAssessments()) {
					CourseEnrollmentTraineeApp trainee = existingTrainees.stream()
							.filter(t -> t.getId().equals(dto.getTraineeId())).findFirst().orElseThrow(
									() -> new RuntimeException("Trainee not found with ID: " + dto.getTraineeId()));

					trainee.setInternalAssessment(String.valueOf(dto.getInternalAssessment()));
				}

				// Save all updated trainees
				courseEnrollmentTraineeAppRepository.saveAll(existingTrainees);

				// Fetch next role
				RoleService roleService = roleServiceRepository
						.getNextAssignedRole(request.getAssignedRoleId(), request.getServiceId(), request.getStatusId())
						.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

				workTaskFlowService.updateWorkflow(request.getApplicationNo(), request.getStatusId(),
						request.getAssignedRoleId(), null, request.getRemarks(), request.getServiceId(), null);

				workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId,
						roleService.getNextRoleId(), null, request.getRemarks());
			}

			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("status", HttpStatus.CREATED.value()));

		} catch (RecordNotFoundException e) {
			log.error("Record not found: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			log.error("Error submitting trainees: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit trainees course", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getCourseAppliedTraineesReAssessmentByApplicationNo(String application_no) {
		List<Tuple> resultList = courseEnrollmentTraineeAppRepository
				.getCourseAppliedTraineesReAssessmentByApplicationNo(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

}
