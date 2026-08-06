package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AssignedRecsDto;
import com.moesd.tvet.mis.backend.application.dto.InstituteChangeRequestDto;
import com.moesd.tvet.mis.backend.application.dto.InstituteRegistrationdto;
import com.moesd.tvet.mis.backend.application.dto.PartnerDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.AccreditorTaskAssignment;
import com.moesd.tvet.mis.backend.application.model.InstituteChangeDetails;
import com.moesd.tvet.mis.backend.application.model.InstituteChangePartnership;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationApp;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationAppCourse;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationAppQualityStandardResponse;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationAppTrainer;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationAppTuitionDetails;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationDetails;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationDetailsAudit;
import com.moesd.tvet.mis.backend.application.model.RecMemberTaskAssignment;
import com.moesd.tvet.mis.backend.application.model.Role;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.model.UserRole;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.AccreditorTaskAssignmentRepository;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.InstituteChangeRepository;
import com.moesd.tvet.mis.backend.application.repository.InstituteRegistrationDetailsAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.InstituteRegistrationDetailsRepository;
import com.moesd.tvet.mis.backend.application.repository.InstituteRegistrationRepository;
import com.moesd.tvet.mis.backend.application.repository.RecMemberTaskAssignmentRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRoleRepository;
import com.moesd.tvet.mis.backend.application.service.InstituteRegistrationService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.GenerateLicenseNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstituteRegistrationServiceImpl implements InstituteRegistrationService {

	private final InstituteRegistrationRepository instituteRegistrationRepository;
	private final ServiceMasterRepository serviceMasterRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final RoleServiceRepository roleServiceRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final TaskFlowListRepository taskFlowListRepository;
	private final DocumentFileUploadService documentFileUploadService;
	private final ObjectToJson objectTojson;
	private final GenerateLicenseNumber generateLicenseNumber;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;
	private final PasswordEncoder passwordEncoder;
	private final InstituteRegistrationDetailsRepository instituteRegistrationDetailsRepository;
    private final RecMemberTaskAssignmentRepository recMemberTaskAssignmentRepository;
    private final AccreditorTaskAssignmentRepository accreditorTaskAssignmentRepository;
    private final InstituteRegistrationDetailsAuditRepository instituteRegistrationDetailsAuditRepository;
    private final InstituteChangeRepository instituteChangeRepository;
    
    
	@Override
	@Transactional
	public ResponseEntity<?> registerInstitute(InstituteRegistrationdto request) {
		try {
			// Validate required fields
			if (request.getServiceId() == null)
				throw new RecordNotFoundException("serviceId is required");

			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assigned RoleId is required");

			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");

//			if (request.getApplicationNo() == null || request.getApplicationNo().isEmpty())
//				throw new RecordNotFoundException("applicationNo is required");

			Integer serviceId = request.getServiceId();
			Integer assignedRoleId = request.getAssignedRoleId();
			String userId = request.getUserId();
			Integer locationId = 14;

			// Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);
			// Build InstituteRegistration entity
			InstituteRegistrationApp registration = InstituteRegistrationApp.builder()
					.applicationNo(applicationNo)
					.proposedInstituteName(request.getInstituteName()).dzongkhagId(request.getDzongkhagId())
					.emailId(request.getEmailId()).proposalApplicationNo(request.getApplicationNo())
					.exactLocation(request.getExactLocation()).mobileNo(request.getMobileNo())
					.telephoneNo(request.getTelephoneNo()).website(request.getWebsite())
					.renewalRegistrationNo(request.getRegistrationNo())
					.ownershipTypeId(request.getOwnershipTypeId()).bhutaneseEmployees(request.getBhutaneseEmployees())
					.nonBhutaneseEmployees(request.getNonBhutaneseEmployees())
					.businessLicenseNo(request.getBusinessLicenseNo()).keyContactName(request.getKeyContactName())
					.keyContactDesignation(request.getKeyContactDesignation())
					.keyContactMobileNo(request.getKeyContactMobileNo())
					.serviceId(serviceId).statusId(request.getStatusId())
					.createdAt(LocalDateTime.now())
					.createdBy(request.getCreatedBy())
					.build();

			// Build trainers that were added while institute registration
			if (request.getTrainers() != null && !request.getTrainers().isEmpty()) {
				List<InstituteRegistrationAppTrainer> trainers = request.getTrainers().stream()
						.map(trainerDto -> InstituteRegistrationAppTrainer.builder()
								.nationalityId(trainerDto.getNationalityId()).cid(trainerDto.getCid())
								.workPermit(trainerDto.getWorkPermit()).name(trainerDto.getName())
								.genderId(trainerDto.getGenderId()).qualification(trainerDto.getQualification())
								.experience(trainerDto.getExperience()).typeId(trainerDto.getTypeId())
								.instituteRegistration(registration) // Set the parent
								.build())
						.collect(Collectors.toList());

				registration.setTrainers(trainers);
			}

			// Build courses that were added while institute registration
			if (request.getCourses() != null && !request.getCourses().isEmpty()) {
				List<InstituteRegistrationAppCourse> courses = request.getCourses().stream()
						.map(courseDto -> InstituteRegistrationAppCourse.builder().sectorId(courseDto.getSectorId())
								.courseId(courseDto.getCourseId()).theoryHours(courseDto.getTheoryHours())
								.practicalHours(courseDto.getPracticalHours()).ojtHours(courseDto.getOjtHours())
								.feesPerTrainee(courseDto.getFeesPerTrainee())
								.enrollmentCapacity(courseDto.getEnrollmentCapacity())
								.courseLevelId(courseDto.getCourseLevelId()).instituteRegistration(registration).build())
						.collect(Collectors.toList());

				registration.setCourses(courses);
			}

			if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
				List<InstituteRegistrationAppQualityStandardResponse> qualitystandards = request.getQualityStandards()
						.stream()
						.map(qualitystandardsDto -> InstituteRegistrationAppQualityStandardResponse.builder()
								.standardId(qualitystandardsDto.getStandardId())
								.responseId(qualitystandardsDto.getResponseId()).instituteRegistration(registration)
								.build())
						.collect(Collectors.toList());
				registration.setQualityStandardResponses(qualitystandards);
			}

			if (request.getTuitionDetails() != null && !request.getTuitionDetails().isEmpty()) {
				List<InstituteRegistrationAppTuitionDetails> tuitiondetails = request.getTuitionDetails().stream()
						.map(tuitiondetailsDto -> InstituteRegistrationAppTuitionDetails.builder()
								.classLevel(tuitiondetailsDto.getClassLevel()).duration(tuitiondetailsDto.getDuration())
								.fees(tuitiondetailsDto.getFees()).subject(tuitiondetailsDto.getSubjects())
								.tutorCid(tuitiondetailsDto.getTutorCid()).tutorName(tuitiondetailsDto.getTutorName())
								.tutorQualification(tuitiondetailsDto.getTutorQualification())
								.instituteRegistration(registration) // Set the parent
								.build())
						.collect(Collectors.toList());
				registration.setTuitionDetails(tuitiondetails);
			}

			// Save everything - cascade will automatically save trainers and quality
			InstituteRegistrationApp savedRegistration = instituteRegistrationRepository.save(registration);

			// Get initiated statusId
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));

			// Create workflow
			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, request.getInstituteName(),
					serviceId, request.getStatusId(), assignedRoleId, request.getRemarks());

			// Create task flow
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, request.getAssignedUserId(),
					workflow, request.getRemarks(), locationId);

			// Save documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo, "institute_registration",
						serviceId, userId, null);
			}

			// Return response
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("applicationNo", applicationNo, "id", savedRegistration.getId(), "status",
							HttpStatus.CREATED.value(), "message", "Institute registration submitted successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to submit institute registration", "error", e.getMessage(),
							"timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<Tuple> applicationExistOrNot(String application_no, String service_id) {
		List<Tuple> resultList = instituteRegistrationRepository.findByProposalApplicationNo(application_no,
				service_id);
		return resultList;
	}

	@Override
	public List<ObjectNode> getInstituteRegistrationDetails(String application_no) {
		List<Tuple> resultList = instituteRegistrationRepository.getInstituteRegistrationDetails(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	@Transactional
	public ResponseEntity<?> verifyInstituteRegistration(InstituteRegistrationdto request) {
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
			String userId = request.getUserId();

			// Integer locationId = 14;
			// Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// Fetch next role
			RoleService roleService = roleServiceRepository.getNextAssignedRole(assignedRoleId, serviceId, statusId)
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));
			// System.out.println("roleService" + roleService);
			// Find existing registration by applicationNo
			InstituteRegistrationApp existingRegistration = instituteRegistrationRepository
					.findByApplicationNo(request.getApplicationNo()) // Returns Optional
					.orElseThrow(() -> new RecordNotFoundException(
							"Institute registration not found with applicationNo: " + request.getApplicationNo()));
			// Update InstituteRegistration entity
			existingRegistration.setUpdatedBy(request.getUpdatedBy());
			existingRegistration.setStatusId(request.getStatusId());
			existingRegistration.setUpdatedAt(LocalDateTime.now());

			// Update ONLY existing records
			if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {

				// Get existing quality standards for this registration
				List<InstituteRegistrationAppQualityStandardResponse> existingStandards = existingRegistration
						.getQualityStandardResponses();

				if (existingStandards != null && !existingStandards.isEmpty()) {
					Map<Long, InstituteRegistrationAppQualityStandardResponse> existingMap = existingStandards.stream()
							.collect(Collectors.toMap(InstituteRegistrationAppQualityStandardResponse::getStandardId,
									standard -> standard));

					// Update only existing quality standards
					for (var qualityDto : request.getQualityStandards()) {
						Long standardId = qualityDto.getStandardId();
						InstituteRegistrationAppQualityStandardResponse existingStandard = existingMap.get(standardId);

						if (existingStandard != null) {
							// Update only responseId and remarks
							existingStandard.setResponseId(qualityDto.getResponseId());
							existingStandard.setRemarks(qualityDto.getRemarks());

						} else {
							System.out
									.println("Quality standard with standardId " + standardId + " not found, skipping");
						}
					}

				}
			}

			// Save the updated registration
			InstituteRegistrationApp savedRegistration = instituteRegistrationRepository.save(existingRegistration);

			// Get task status
			Integer taskStatusId;
			if (statusId == 57) {
				taskStatusId = dropdownManagementRepository.findChildById(20)// task completed Id
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
				if(serviceId == 8 || serviceId == 52 || serviceId == 53) {
					
					InstituteRegistrationDetails instituteRegistration = instituteRegistrationDetailsRepository
				                .findByRegistrationNo(request.getRegistrationNo())
				                .orElseThrow(() -> new RecordNotFoundException("Institute registration not found with registrationNo: " + request.getRegistrationNo()));
					//save audit
					saveInstituteRegistrationAudit(instituteRegistration);
				    // Update basic fields
					instituteRegistration.setApplicationNo(request.getApplicationNo());
					instituteRegistration.setProposedInstituteName(request.getInstituteName());
					instituteRegistration.setDzongkhagId(request.getDzongkhagId());
					instituteRegistration.setEmailId(request.getEmailId());
					instituteRegistration.setExactLocation(request.getExactLocation());
					instituteRegistration.setMobileNo(request.getMobileNo());
					instituteRegistration.setTelephoneNo(request.getTelephoneNo());
					instituteRegistration.setWebsite(request.getWebsite());
					instituteRegistration.setOwnershipTypeId(request.getOwnershipTypeId());
					instituteRegistration.setBhutaneseEmployees(request.getBhutaneseEmployees());
					instituteRegistration.setNonBhutaneseEmployees(request.getNonBhutaneseEmployees());
					instituteRegistration.setBusinessLicenseNo(request.getBusinessLicenseNo());
					instituteRegistration.setKeyContactName(request.getKeyContactName());
					instituteRegistration.setKeyContactDesignation(request.getKeyContactDesignation());
					instituteRegistration.setKeyContactMobileNo(request.getKeyContactMobileNo());
					instituteRegistration.setStatusId(request.getStatusId());
					instituteRegistration.setInstituteRenewalDate(
						    instituteRegistration.getInstituteRenewalDate() != null
						        ? instituteRegistration.getInstituteRenewalDate().plusYears(1)
						        : LocalDateTime.now().plusYears(1)
						);
					instituteRegistration.setServiceId(serviceId);
					instituteRegistration.setUpdatedAt(LocalDateTime.now());
					instituteRegistration.setUpdatedBy(request.getUpdatedBy());
					
					instituteRegistrationDetailsRepository.save(instituteRegistration);
					
				}else {
					// Generate application number
					String licenseNo = generateLicenseNumber.generateLicenseNumber(serviceId);
					// Build Institute Registration Details entity
					InstituteRegistrationDetails registrationDetails = InstituteRegistrationDetails.builder()
							.applicationNo(request.getApplicationNo()).registrationNo(licenseNo)
							.proposedInstituteName(request.getInstituteName()).dzongkhagId(request.getDzongkhagId())
							.emailId(request.getEmailId()).exactLocation(request.getExactLocation())
							.mobileNo(request.getMobileNo()).telephoneNo(request.getTelephoneNo())
							.website(request.getWebsite()).ownershipTypeId(request.getOwnershipTypeId())
							.bhutaneseEmployees(request.getBhutaneseEmployees())
							.nonBhutaneseEmployees(request.getNonBhutaneseEmployees())
							.businessLicenseNo(request.getBusinessLicenseNo()).keyContactName(request.getKeyContactName())
							.keyContactDesignation(request.getKeyContactDesignation())
							.instituteRenewalDate(LocalDateTime.now())
							.keyContactMobileNo(request.getKeyContactMobileNo()).statusId(request.getStatusId())
							.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).serviceId(serviceId)
							.createdBy(userId).build();
					// save registration details
					instituteRegistrationDetailsRepository.save(registrationDetails);
					// create user
					// Check if user ID already exists
					if (userRepository.findByUserId(licenseNo).isPresent()) {
						return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", HttpStatus.CONFLICT.value(),
								"error", "Conflict", "message", "User ID " + request.getUserId() + " already exists"));
					}

					// Create new User
					User user = new User();
					// Get roleId from method
					final Integer roleId = getRoleIdByServiceId(serviceId);
					// Validate role mapping
					if (roleId == null) {
						throw new RecordNotFoundException("Invalid serviceId: " + serviceId + " for role mapping");
					}
					// Fetch the role
					Role role = roleRepository.findById(roleId)
							.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
									"Role with ID " + roleId + " does not exist"));
					// Assign the single role
					List<UserRole> userRoles = new ArrayList<>();
					UserRole userRole = new UserRole();
					userRole.setUser(user);
					userRole.setRole(role);
					userRoles.add(userRole);
					// Set user properties
					user.setUserId(licenseNo);
					user.setPassword(passwordEncoder.encode("password"));
					user.setFirstName(request.getInstituteName());
					user.setMiddleName("");
					user.setLastName("");
					user.setGenderId("");
					user.setMobileNo(request.getMobileNo());
					user.setEmailId(request.getEmailId());
					user.setCurrentRole(roleId);
					user.setStatusId("1");
					user.setLocationId(request.getDzongkhagId());
					user.setCreatedAt(new Date());
					user.setCreatedBy(null);
					user = userRepository.save(user);
					// Save user role
					userRoleRepository.saveAll(userRoles);
					user.setUserRoles(userRoles);
					userRepository.save(user);
				}
				
				
			} else {
				taskStatusId = dropdownManagementRepository.findChildById(18) // task initiated
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}
			//save accreditor
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
				//new starts
				Long recCount = recMemberTaskAssignmentRepository
				        .getRECMemberCount(request.getApplicationNo());
				if (request.getCurrentRoleId() == 23 && recCount > 1) {
                    
				    RecMemberTaskAssignment recMember = recMemberTaskAssignmentRepository
				            .findRecMemberUser(request.getRecMemberUserId(), request.getApplicationNo())
				            .orElseThrow(() -> new RecordNotFoundException(
				                    "Rec Member not found with User Id: " + request.getRecMemberUserId()));

				    recMember.setRemarks(request.getOverallRemarks());
				    recMemberTaskAssignmentRepository.save(recMember);

				    String removeUserId = request.getRecMemberUserId();

				    TaskFlowList taskFlowList = taskFlowListRepository
				            .findByApplicationNo(request.getApplicationNo());

				    if (taskFlowList != null) {

				        String assignedUsers = taskFlowList.getAssignedUserId();

				        if (assignedUsers != null && !assignedUsers.isBlank()) {

				            String updatedAssignedUsers = Arrays.stream(assignedUsers.split(","))
				                    .map(String::trim)
				                    .filter(id -> !id.equals(removeUserId))
				                    .collect(Collectors.joining(","));

				            taskFlowList.setAssignedUserId(
				                    updatedAssignedUsers.isEmpty() ? null : updatedAssignedUsers
				            );

				            taskFlowListRepository.save(taskFlowList);
				        }
				    }
				}else {
					
					//new ends
					workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
							request.getUserId(), request.getRemarks(), serviceId, null);

					// update task flow
					workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId,
							roleService.getNextRoleId(), request.getUserId(), request.getRemarks());
				}
				
				
				
			
			}

			// Save documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), request.getApplicationNo(),
						"institute_registration", serviceId, userId, null);
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

	private Integer getRoleIdByServiceId(Integer serviceId) {
		if (serviceId == 7)
			return 11;
		if (serviceId == 36)
			return 28;
		if (serviceId == 4)
			return 12;
		return null;
	}
	
	private void saveInstituteRegistrationAudit(InstituteRegistrationDetails registration) {
		InstituteRegistrationDetailsAudit registrationAudit = InstituteRegistrationDetailsAudit.builder()
				.applicationNo(registration.getApplicationNo())
				.registrationNo(registration.getRegistrationNo())
				.proposedInstituteName(registration.getProposedInstituteName())
				.dzongkhagId(registration.getDzongkhagId())
				.emailId(registration.getEmailId()).exactLocation(registration.getExactLocation())
				.mobileNo(registration.getMobileNo()).telephoneNo(registration.getTelephoneNo())
				.website(registration.getWebsite()).ownershipTypeId(registration.getOwnershipTypeId())
				.bhutaneseEmployees(registration.getBhutaneseEmployees())
				.nonBhutaneseEmployees(registration.getNonBhutaneseEmployees())
				.businessLicenseNo(registration.getBusinessLicenseNo()).keyContactName(registration.getKeyContactName())
				.keyContactDesignation(registration.getKeyContactDesignation())
				.instituteRenewalDate(registration.getInstituteRenewalDate())
				.keyContactMobileNo(registration.getKeyContactMobileNo()).statusId(registration.getStatusId())
				.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).serviceId(registration.getServiceId())
				.instituteRegistrationDetails(registration)
				.createdBy(registration.getCreatedBy()).build();

		instituteRegistrationDetailsAuditRepository.save(registrationAudit);
		
	}
	
	@Override
	public List<ObjectNode> getInstituteDetails(String registration_no) {
		List<Tuple> resultList = instituteRegistrationDetailsRepository.getInstituteDetails(registration_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getInstituteRenewalDetails(String registration_no) {
		List<Tuple> resultList = instituteRegistrationDetailsRepository.getInstituteRenewalDetails(registration_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getInstituteChangeDetails(String registration_no) {
		List<Tuple> resultList = instituteRegistrationDetailsRepository.getInstituteChangeDetails(registration_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public ResponseEntity<?> instituteChange(InstituteChangeRequestDto request) {
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
			Integer locationId = 14;
			String applicantName = request.getInstituteName();
			// 2. Validate service
			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// 3. Get unclaimed statusId
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RecordNotFoundException("Unclaimed status not found"));

			// 4. Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);

			// 5. Build entity
			InstituteChangeDetails instituteChange = InstituteChangeDetails.builder().applicationNo(applicationNo)
					.instituteId(request.getInstituteId()).reasonForChange(request.getReasonForChange())
					.ownershipTypeId(request.getOwnershipTypeId()).otherOwnershipTypeId(request.getOtherOwnershipTypeId())
					.registrationNo(request.getRegistrationNo()).companyName(request.getCompanyName())
					.otherName(request.getOtherName()).otherAddress(request.getOtherAddress())
					.instituteName(request.getInstituteName()).changeType(request.getChangeType())
					.dzongkhagId(request.getDzongkhagId()).exactLocation(request.getExactLocation())
					.promoterCitizenId(request.getPromoterCitizenId()).promoterName(request.getPromoterName())
					.createdBy(request.getCreatedBy())
					.statusId(request.getStatusId()).createdAt(LocalDateTime.now()).build();

			// 6. Handle partners safely
			// handle partners safely
			PartnerDto[] partners = request.getPartners();
			if (partners != null && partners.length > 0) {
				List<InstituteChangePartnership> partnerEntities = new ArrayList<>();
				for (PartnerDto partnerDto : partners) {
					InstituteChangePartnership partner = InstituteChangePartnership.builder()
							.typeOfOwnerId(partnerDto.getTypeOfOwner()).partnerCidNo(partnerDto.getCitizenId())
							.partnerName(partnerDto.getPartnerName())
							.partnerCompanyRegistrationNo(partnerDto.getRegistrationNo())
							.partnerCompanyName(partnerDto.getCompanyName()).parent(instituteChange).build();

					partnerEntities.add(partner);
				}
				instituteChange.setInstituteChangePartnership(partnerEntities);
			} else {
				instituteChange.setInstituteChangePartnership(new ArrayList<>()); // safe empty list
			}

			// 7. Save proposal
			instituteChangeRepository.save(instituteChange);

			// 8. Create workflow
			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, applicantName, serviceId,
					request.getStatusId(), assignedRoleId, request.getRemarks());

			// 9. Create task flow
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, request.getAssignedUserId(),
					workflow, request.getRemarks(), locationId);

			// 10. Save documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo, "institute_change",
						serviceId, null, null);
			}

			// 11. Return response
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("applicationNo", applicationNo, "status", HttpStatus.CREATED.value()));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit proposal", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getInstituteChangeByApplicationNo(String application_no) {
		List<Tuple> resultList = instituteChangeRepository.getInstituteChangeByApplicationNo(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	
	

}