package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteRegistrationdto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationApp;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationAppCourse;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationAppQualityStandardResponse;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationAppTrainer;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationAppTuitionDetails;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationDetails;
import com.moesd.tvet.mis.backend.application.model.Role;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.model.UserRole;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.InstituteRegistrationDetailsRepository;
import com.moesd.tvet.mis.backend.application.repository.InstituteRegistrationRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
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
	private final DocumentFileUploadService documentFileUploadService;
	private final ObjectToJson objectTojson;
	private final GenerateLicenseNumber generateLicenseNumber;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final UserRoleRepository userRoleRepository;
	private final PasswordEncoder passwordEncoder;
	private final InstituteRegistrationDetailsRepository instituteRegistrationDetailsRepository;

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

			if (request.getApplicationNo() == null || request.getApplicationNo().isEmpty())
				throw new RecordNotFoundException("applicationNo is required");

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
			InstituteRegistrationApp registration = InstituteRegistrationApp.builder().applicationNo(applicationNo)
					.proposedInstituteName(request.getInstituteName()).dzongkhagId(request.getDzongkhagId())
					.emailId(request.getEmailId()).proposalApplicationNo(request.getApplicationNo())
					.exactLocation(request.getExactLocation()).mobileNo(request.getMobileNo())
					.telephoneNo(request.getTelephoneNo()).website(request.getWebsite())
					.ownershipTypeId(request.getOwnershipTypeId()).bhutaneseEmployees(request.getBhutaneseEmployees())
					.nonBhutaneseEmployees(request.getNonBhutaneseEmployees())
					.businessLicenseNo(request.getBusinessLicenseNo()).keyContactName(request.getKeyContactName())
					.keyContactDesignation(request.getKeyContactDesignation())
					.keyContactMobileNo(request.getKeyContactMobileNo()).statusId(request.getStatusId())
					.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).serviceId(serviceId)
					.createdBy(request.getCreatedBy()).updatedBy(userId).build();

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
								.courseLevelId(courseDto.getCourseLevel()).instituteRegistration(registration) 																	
								.build())
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

			// Get unclaimed statusId
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RecordNotFoundException("Unclaimed status not found"));

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
            //System.out.println("roleService" + roleService);
            // Find existing registration by applicationNo
			InstituteRegistrationApp existingRegistration = instituteRegistrationRepository
					.findByApplicationNo(request.getApplicationNo()) // Returns Optional
					.orElseThrow(() -> new RecordNotFoundException(
							"Institute registration not found with applicationNo: " + request.getApplicationNo()));
			// Update InstituteRegistration entity
			existingRegistration.setUpdatedBy(request.getUpdatedBy());
			existingRegistration.setStatusId(request.getStatusId());
			existingRegistration.setUpdatedAt(LocalDateTime.now());
			
			  //Update ONLY existing records
	        if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
	            
	            // Get existing quality standards for this registration
	            List<InstituteRegistrationAppQualityStandardResponse> existingStandards = 
	                existingRegistration.getQualityStandardResponses();
	           
	            if (existingStandards != null && !existingStandards.isEmpty()) {
	                Map<Long, InstituteRegistrationAppQualityStandardResponse> existingMap = 
	                    existingStandards.stream()
	                        .collect(Collectors.toMap(
	                            InstituteRegistrationAppQualityStandardResponse::getStandardId,
	                            standard -> standard
	                        ));
	                
	                // Update only existing quality standards
	                for (var qualityDto : request.getQualityStandards()) {
	                    Long standardId = qualityDto.getStandardId();
	                    InstituteRegistrationAppQualityStandardResponse existingStandard = existingMap.get(standardId);
	                    
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
			InstituteRegistrationApp savedRegistration = instituteRegistrationRepository.save(existingRegistration);

			// Get task status
			Integer taskStatusId;
			if (statusId == 57) {
				taskStatusId = dropdownManagementRepository.findChildById(20)// task completed Id
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
				// Generate application number
				String licenseNo = generateLicenseNumber.generateLicenseNumber(serviceId);
				// Build Institute Registration Details entity
				InstituteRegistrationDetails registrationDetails = InstituteRegistrationDetails.builder()
						.applicationNo(request.getApplicationNo()).RegistrationNo(licenseNo)
						.proposedInstituteName(request.getInstituteName()).dzongkhagId(request.getDzongkhagId())
						.emailId(request.getEmailId())
						.exactLocation(request.getExactLocation()).mobileNo(request.getMobileNo())
						.telephoneNo(request.getTelephoneNo()).website(request.getWebsite())
						.ownershipTypeId(request.getOwnershipTypeId())
						.bhutaneseEmployees(request.getBhutaneseEmployees())
						.nonBhutaneseEmployees(request.getNonBhutaneseEmployees())
						.businessLicenseNo(request.getBusinessLicenseNo()).keyContactName(request.getKeyContactName())
						.keyContactDesignation(request.getKeyContactDesignation()).keyContactMobileNo(request.getKeyContactMobileNo())
						.statusId(request.getStatusId()).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
						.serviceId(serviceId).createdBy(userId).updatedBy(userId).build();
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

				// Create a request object with role IDs
				List<Integer> RoleIds = Arrays.asList(11); // role IDs

				// Assign Roles
				List<UserRole> userRoles = new ArrayList<>();
				for (Integer roleId : RoleIds) {
					Role role = roleRepository.findById(roleId)
							.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
									"Role with ID " + roleId + " does not exist"));
					UserRole userRole = new UserRole();
					userRole.setUser(user);
					userRole.setRole(role);
					userRoles.add(userRole);
				}
				user.setUserId(licenseNo);
				user.setPassword(passwordEncoder.encode("password"));
				user.setFirstName(request.getInstituteName());
				user.setMiddleName("");
				user.setLastName("");
				user.setGenderId("");
				user.setMobileNo(request.getMobileNo());
				user.setEmailId(request.getEmailId());
				user.setCurrentRole(11);
				user.setStatusId("1");
				user.setLocationId(request.getDzongkhagId());
				user.setCreatedAt(new Date());
				user.setCreatedBy(null);
				user = userRepository.save(user);

				userRoleRepository.saveAll(userRoles);
				user.setUserRoles(userRoles);
				userRepository.save(user);
			} else {
				taskStatusId = dropdownManagementRepository.findChildById(18) // task unclaimed Id
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}

			workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
					request.getUserId(), request.getRemarks(), serviceId, null);

			// update task flow
			workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, roleService.getNextRoleId(),
					request.getUserId(), request.getRemarks());

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

	@Override
	public List<ObjectNode> getInstituteDetails(String registration_no) {
		List<Tuple> resultList = instituteRegistrationDetailsRepository.getInstituteDetails(registration_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

}