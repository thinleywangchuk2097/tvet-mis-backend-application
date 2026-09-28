package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CourseEnrollmentTraineeAppdto;
import com.moesd.tvet.mis.backend.application.dto.SelectedTraineedto;
import com.moesd.tvet.mis.backend.application.dto.TraineeInternaldto;
import com.moesd.tvet.mis.backend.application.dto.TraineeMarksdto;
import com.moesd.tvet.mis.backend.application.dto.TraineeStatusdto;
import com.moesd.tvet.mis.backend.application.dto.TraineeVivadto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.AssessorTaskAssignment;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentApp;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentTraineeApp;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentTraineeAppSubjectMarks;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.AssessorTaskAssignmentRepository;
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
    private final AssessorTaskAssignmentRepository assessorTaskAssignmentRepository;
    
    
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
					.employmentStatusId(request.getEmploymentStatusId()).examYear(request.getExamYear())
					.schoolName(request.getSchoolName()).stream(request.getStream())
					.presentDzongkhagId(request.getPresentDzongkhagId()).presentGewogId(request.getPresentGewogId())
					.guardianName(request.getGuardianName()).guardianMobileNo(request.getGuardianMobileNo())
					.guardianOccupationId(request.getGuardianOccupationId())
					.guardianMaritalStatusId(request.getGuardianMaritalStatusId()).statusId(request.getStatusId())
					.createdAt(new java.util.Date()).build();

			// Process trainee marks - set the relationship
			if (request.getTraineeMarks() != null && !request.getTraineeMarks().isEmpty()) {
				List<CourseEnrollmentTraineeAppSubjectMarks> studentresults = request.getTraineeMarks().stream()
						.map(studentresultsDto -> {
							CourseEnrollmentTraineeAppSubjectMarks mark = CourseEnrollmentTraineeAppSubjectMarks
									.builder().subject(studentresultsDto.getSubject())
									.markScore(studentresultsDto.getTotal()).build();
							// IMPORTANT: Set the courseTrainee relationship
							mark.setCourseTrainee(trainee);
							return mark;
						}).collect(Collectors.toList());
				trainee.setTraineeMarks(studentresults);
			}

			// Save entity (cascade will save the marks too)
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
		    log.error("Failed to submit Trainee", e);
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(Map.of("message", "Failed to submit Trainee"));
		}
	}

	@Override
	public List<ObjectNode> getCourseAppliedTraineesByApplicationNo(String applicationNo) {
	    return objectTojson._toJson(
	            courseEnrollmentTraineeAppRepository.getCourseAppliedTraineesByApplicationNo(applicationNo));
	}

	@Override
	@Transactional
	public ResponseEntity<?> submitSelectedTrainee(SelectedTraineedto request) {
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
			
			//new added
			CourseEnrollmentApp course = courseEnrollmentAppRepository
						.findByApplicationNo(request.getApplicationNo())
						.orElseThrow(() -> new RuntimeException("Course not found"));
			//this status is being used while trainee selection
			course.setApplicationStatusId(request.getStatusId());
			//ended new added line
						
			List<CourseEnrollmentTraineeApp> trainees = courseEnrollmentTraineeAppRepository
					.findByApplicationNo(request.getApplicationNo());

			if (trainees.isEmpty()) {
				throw new RecordNotFoundException("No trainees found for applicationNo: " + request.getApplicationNo());
			}

			// If traineeIds provided → filter
			if (request.getTraineeIds() != null && !request.getTraineeIds().isEmpty()) {
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
			Integer taskStatusId;
			if(request.getStatusId() == 57) {
				taskStatusId = dropdownManagementRepository.findChildById(18)// task Initiated Id
					.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}else {
				taskStatusId = dropdownManagementRepository.findChildById(20)// task completed Id
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}
			
			Integer resultId;
			// Validate required fields
			CourseEnrollmentApp course = courseEnrollmentAppRepository
					.findByApplicationNo(request.getApplicationNo())
					.orElseThrow(() -> new RuntimeException("Programme not found"));
			//this status is being used while trainee selection
			course.setApplicationStatusId(request.getStatusId());
			//set CA date
			if (request.getCaStartDate() != null && request.getCaEndDate() != null) {
			    course.setCaStartDate(request.getCaStartDate());
			    course.setCaEndDate(request.getCaEndDate());
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
						trainee.setRemarks(String.valueOf(dto.getRemarks()));
						if (request.getCertificationLevelId() == 111 || request.getCertificationLevelId() == 112) {
							if (dto.getTheoryAssessment() >= 10 && dto.getPracticalAssessment() >= 36 && dto.getInternalAssessment() >= 10) {
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
						if (request.getCertificationLevelId() == 111 || request.getCertificationLevelId() == 112) {
							if (dto.getVivaAssessment() >= 10 && dto.getPracticalAssessment() >= 36 && dto.getInternalAssessment() >= 10) {
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
					
				}
				// save changes
				courseEnrollmentTraineeAppRepository.saveAll(trainees);
			}
			//save assessor
			if (request.getAssignedAssessors() != null && !request.getAssignedAssessors().isEmpty()) {
			    List<AssessorTaskAssignment> assignments = request.getAssignedAssessors()
			            .stream()
			            .map(assessor -> AssessorTaskAssignment.builder()
			                    .userId(assessor.getUserId())
			                    .ApplicationNo(request.getApplicationNo())
			                    .serviceId(request.getServiceId())
			                    .build())
			            .toList();

			    assessorTaskAssignmentRepository.saveAll(assignments);
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
	public List<ObjectNode> getFailedTraineeDetails(String userId, String courseId, Integer certificationLevelId) {
	    return objectTojson._toJson(
	            courseEnrollmentTraineeAppRepository.getFailedTraineeDetails(userId, courseId, certificationLevelId));
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
	        validateRequest(request);

	        List<CourseEnrollmentTraineeApp> existingTrainees = fetchFailedTrainees(request);
	        markAsReassessment(existingTrainees);
	        updateCourseStatus(request);
	        ensureTraineesExist(existingTrainees, request);

	        createReassessmentApplications(request, existingTrainees);
	        updateInternalAssessments(request, existingTrainees);

	        return ResponseEntity.status(HttpStatus.CREATED)
	                .body(Map.of("status", HttpStatus.CREATED.value()));

	    } catch (RecordNotFoundException e) {
	        log.error("Record not found: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
	    } catch (Exception e) {
	        log.error("Error submitting trainees: {}", e.getMessage(), e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("message", "Failed to submit trainees course",
	                        "error", e.getMessage(), "timestamp", LocalDateTime.now()));
	    }
	}

	private void validateRequest(SelectedTraineedto request) {
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
	}

	private List<CourseEnrollmentTraineeApp> fetchFailedTrainees(SelectedTraineedto request) {
	    return courseEnrollmentTraineeAppRepository.getFailedTraineeReassessment(
	            request.getUserId(), request.getProgrammeId(), request.getCertificationLevelId());
	}

	private void markAsReassessment(List<CourseEnrollmentTraineeApp> trainees) {
	    if (trainees == null || trainees.isEmpty()) {
	        return;
	    }
	    Integer reassessmentStatusId = 141;
	    trainees.forEach(t -> t.setResultStatusId(reassessmentStatusId));
	    courseEnrollmentTraineeAppRepository.saveAll(trainees);
	}

	private void updateCourseStatus(SelectedTraineedto request) {
	    CourseEnrollmentApp course = courseEnrollmentAppRepository
	            .findByApplicationNo(request.getApplicationNo())
	            .orElseThrow(() -> new RuntimeException("Programme not found"));
	    course.setApplicationStatusId(request.getStatusId());
	    courseEnrollmentAppRepository.save(course);
	}

	private void ensureTraineesExist(List<CourseEnrollmentTraineeApp> trainees, SelectedTraineedto request) {
	    if (trainees.isEmpty()) {
	        throw new RecordNotFoundException("No trainees found for applicationNo: " + request.getApplicationNo());
	    }
	}

	private void createReassessmentApplications(SelectedTraineedto request,
	        List<CourseEnrollmentTraineeApp> existingTrainees) {
	    if (request.getTraineeIds() == null || request.getTraineeIds().isEmpty()) {
	        return;
	    }

	    Integer taskStatusId = dropdownManagementRepository.findChildById(18)
	            .orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));

	    CourseEnrollmentApp course = courseEnrollmentAppRepository
	            .findByApplicationNo(request.getApplicationNo())
	            .orElseThrow(() -> new RuntimeException("Programme not found"));

	    List<CourseEnrollmentTraineeApp> newTrainees = request.getTraineeIds().stream()
	            .map(dto -> buildReassessmentTrainee(dto, existingTrainees, course))
	            .toList();

	    courseEnrollmentTraineeAppRepository.saveAll(newTrainees);

	    WorkFlowList workflow = workTaskFlowService.createWorkflow(
	            request.getApplicationNo(), request.getCourseName(), request.getServiceId(),
	            request.getStatusId(), request.getAssignedRoleId(), request.getRemarks());

	    workTaskFlowService.createTaskFlow(request.getApplicationNo(), taskStatusId,
	            request.getAssignedRoleId(), request.getAssignedUserId(), workflow,
	            request.getRemarks(), 14);
	}

	private CourseEnrollmentTraineeApp buildReassessmentTrainee(TraineeStatusdto dto,
	        List<CourseEnrollmentTraineeApp> existingTrainees, CourseEnrollmentApp course) {
	    CourseEnrollmentTraineeApp existing = findTrainee(existingTrainees, dto.getTraineeId());

	    return CourseEnrollmentTraineeApp.builder()
	            .applicationNo(generateApplicationNumber.generateApplicationNumber(43))
	            .applicantName(existing.getApplicantName())
	            .emailId(existing.getEmailId())
	            .mobileNo(existing.getMobileNo())
	            .statusId(dto.getStatusId())
	            .course(course)
	            .parentFailedId(dto.getTraineeId())
	            .internalAssessment(existing.getInternalAssessment())
	            .practicalAssessment(existing.getPracticalAssessment())
	            .vivaAssessment(existing.getVivaAssessment())
	            .theoryAssessment(existing.getTheoryAssessment())
	            .reAssessmentNo(existing.getReAssessmentNo() != null
	                    ? existing.getReAssessmentNo() + 1 : 1)
	            .academicQualificationId(existing.getAcademicQualificationId())
	            .cidNo(existing.getCidNo())
	            .referenceNo(existing.getReferenceNo())
	            .dob(existing.getDob())
	            .genderId(existing.getGenderId())
	            .traineeTypeId(existing.getTraineeTypeId())
	            .employmentStatusId(existing.getEmploymentStatusId())
	            .presentDzongkhagId(existing.getPresentDzongkhagId())
	            .presentGewogId(existing.getPresentGewogId())
	            .createdAt(new java.util.Date())
	            .build();
	}

	private void updateInternalAssessments(SelectedTraineedto request,
	        List<CourseEnrollmentTraineeApp> existingTrainees) {
	    if (request.getTraineeInternalAssessments() == null
	            || request.getTraineeInternalAssessments().isEmpty()) {
	        return;
	    }

	    log.info("Updating internal assessments for existing trainees");

	    Integer taskStatusId = dropdownManagementRepository.findChildById(18)
	            .orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));

	    request.getTraineeInternalAssessments().forEach(dto -> {
	        CourseEnrollmentTraineeApp trainee = findTrainee(existingTrainees, dto.getTraineeId());
	        trainee.setInternalAssessment(String.valueOf(dto.getInternalAssessment()));
	    });

	    courseEnrollmentTraineeAppRepository.saveAll(existingTrainees);

	    RoleService roleService = roleServiceRepository
	            .getNextAssignedRole(request.getAssignedRoleId(), request.getServiceId(), request.getStatusId())
	            .orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

	    workTaskFlowService.updateWorkflow(request.getApplicationNo(), request.getStatusId(),
	            request.getAssignedRoleId(), null, request.getRemarks(), request.getServiceId(), null);

	    workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId,
	            roleService.getNextRoleId(), null, request.getRemarks());
	}

	private CourseEnrollmentTraineeApp findTrainee(List<CourseEnrollmentTraineeApp> trainees, Long traineeId) {
	    return trainees.stream()
	            .filter(t -> t.getId().equals(traineeId))
	            .findFirst()
	            .orElseThrow(() -> new RuntimeException("Trainee not found with ID: " + traineeId));
	}
	

	@Override
	public List<ObjectNode> getCourseAppliedTraineesReAssessmentByApplicationNo(String applicationNo) {
	    return objectTojson._toJson(courseEnrollmentTraineeAppRepository
	            .getCourseAppliedTraineesReAssessmentByApplicationNo(applicationNo));
	}

	@Override
	public List<ObjectNode> fetchAssignedAssessors(String applicationNo) {
	    return objectTojson._toJson(
	            courseEnrollmentTraineeAppRepository.fetchAssignedAssessors(applicationNo));
	}

	@Override
	public ResponseEntity<?> removeTraineeFromSelectedProgramme(SelectedTraineedto request) {

	    CourseEnrollmentTraineeApp trainee = courseEnrollmentTraineeAppRepository
	            .removeTraineeFromSelectedProgramme(request.getTraineeId())
	            .orElseThrow(() -> new RuntimeException("Course not found"));
	    // This status is being used while trainee selection
	    trainee.setStatusId(request.getStatusId());
	    trainee.setUpdatedBy(request.getUpdatedBy());
	    trainee.setRemarks(request.getRemarks());
	    
	    courseEnrollmentTraineeAppRepository.save(trainee);
	    
	    return ResponseEntity.ok(
	           Map.of(
	                 "message", "Trainee removed from selected programme successfully",
	                 "traineeId", request.getTraineeId()
	          )
	    );
	}

	

	

}
