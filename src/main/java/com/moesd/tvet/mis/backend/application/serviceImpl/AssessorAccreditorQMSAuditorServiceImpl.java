package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.moesd.tvet.mis.backend.application.dto.AssessorAccreditorQMSAuditordto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.AssessorAccreditorQMSAuditor;
import com.moesd.tvet.mis.backend.application.model.AuditorWorkExperience;
import com.moesd.tvet.mis.backend.application.model.Role;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.model.UserRole;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.AssessorAccreditorQMSAuditorRepository;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRoleRepository;
import com.moesd.tvet.mis.backend.application.service.AssessorAccreditorQMSAuditorService;
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
public class AssessorAccreditorQMSAuditorServiceImpl implements AssessorAccreditorQMSAuditorService {

	private final AssessorAccreditorQMSAuditorRepository assessorAccreditorQMSAuditorRepository;
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

	@Override
	@Transactional
	public ResponseEntity<?> registerAssessorAccreditorQMSAuditor(AssessorAccreditorQMSAuditordto request) {
		try {
			System.out.println("request" + request);// Validate required fields
			if (request.getServiceId() == null)
				throw new RecordNotFoundException("serviceId is required");

			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("AssignedRoleId is required");

			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");

			String userId = request.getUserId();
			Integer serviceId = request.getServiceId();
			Integer assignedRoleId = request.getAssignedRoleId();
			Integer statusId = request.getStatusId();
			Integer locationId = 14; // default

			// Fetch next role
			// RoleService roleService =
			// roleServiceRepository.getNextAssignedRole(assignedRoleId, serviceId,
			// statusId)
			// .orElseThrow(() -> new RecordNotFoundException("Next assigned role not
			// found"));

			// Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);

			// Build AssessorAccreditorQMSAuditor entity
			AssessorAccreditorQMSAuditor registration = AssessorAccreditorQMSAuditor.builder()
					.applicationNo(applicationNo).serviceId(serviceId).referenceNo(request.getReferenceNo())
					.citizenId(request.getCitizenId()).dateOfBirth(request.getDateOfBirth())
					.fullName(request.getFullName()).genderId(request.getGenderId())
					.mobileNo(request.getMobileNo()).email(request.getEmail()).dzongkhagId(request.getDzongkhagId())
					.organizationName(request.getOrganizationName())
					.sectorId(request.getSectorId()).sectorName(request.getSectorName())
					.occupationId(request.getOccupationId()).occupationName(request.getOccupationName())
					.certificationLevelId(request.getCertificationLevelId())
					.certificationLevelName(request.getCertificationLevelName()).designation(request.getDesignation())
					.yearsOfExperience(request.getYearsOfExperience()).responsibility(request.getResponsibility())
					.qmsTraining(request.getQmsTraining()).academicBackground(request.getAcademicBackground())
					.statusId(statusId).createdAt(LocalDateTime.now()).createdBy(userId).updatedAt(LocalDateTime.now())
					.updatedBy(userId).remarks(request.getRemarks()).build();

			// Build work experiences and add to registration
			if (request.getWorkExperiences() != null && !request.getWorkExperiences().isEmpty()) {
				List<AuditorWorkExperience> workExperiences = request.getWorkExperiences().stream()
						.map(workExpDto -> AuditorWorkExperience.builder().organizationName(workExpDto.getOrganizationName())
								.designation(workExpDto.getDesignation()).year(workExpDto.getYear())
								.responsibility(workExpDto.getResponsibility()).createdAt(LocalDateTime.now())
								.updatedAt(LocalDateTime.now()).assessorAccreditorQMSAuditor(registration) // Set the
																											// parent
								.build())
						.collect(Collectors.toList());

				registration.setWorkExperiences(workExperiences);
			}

			// Save everything - cascade will automatically save work experiences
			AssessorAccreditorQMSAuditor savedRegistration = assessorAccreditorQMSAuditorRepository.save(registration);

			// Get initiated status (Unclaimed status)
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RecordNotFoundException("Unclaimed status not found"));

			// Create workflow
			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, request.getFullName(), serviceId,
					statusId, assignedRoleId, request.getRemarks());

			// Create task flow
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, request.getAssignedUserId(),
					workflow, request.getRemarks(), locationId);

			// Save documents if any
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo,
						"assessor_accreditor_qmsauditor", // document type
						serviceId, userId, null);
			}

			// Return response
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("applicationNo", applicationNo, "id", savedRegistration.getId(), "status",
							HttpStatus.CREATED.value(), "message",
							"Assessor/Accreditor/QMS Auditor registration submitted successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to submit assessor/accreditor/qms auditor registration", "error",
							e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Override
	public List<ObjectNode> getApplicationDetails(String application_no) {
		List<Tuple> resultList = assessorAccreditorQMSAuditorRepository
				.getApplicationDetailByApplicationNo(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public ResponseEntity<?> verifyAssessorAccreditorQMSAuditor(AssessorAccreditorQMSAuditordto request) {
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
			Integer statusId = request.getStatusId();// work flow status Id
			String userId = request.getUserId();

			// 3. Fetch next role
			RoleService roleService = roleServiceRepository.getNextAssignedRole(assignedRoleId, serviceId, statusId)
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

			// Find existing registration by applicationNo
			AssessorAccreditorQMSAuditor existingAssessorAccreditorQMSAuditor = assessorAccreditorQMSAuditorRepository
					.findByApplicationNo(request.getApplicationNo()) // Returns Optional
					.orElseThrow(() -> new RecordNotFoundException(
							"Institute registration not found with applicationNo: " + request.getApplicationNo()));
			// Update InstituteRegistration entity
			existingAssessorAccreditorQMSAuditor.setStatusId(request.getStatusId());
			existingAssessorAccreditorQMSAuditor.setUpdatedAt(LocalDateTime.now());
			existingAssessorAccreditorQMSAuditor.setUpdatedBy(userId);

			// Save
			assessorAccreditorQMSAuditorRepository.save(existingAssessorAccreditorQMSAuditor);

			// Get task status
			Integer taskStatusId;
			if (statusId == 57) {
				taskStatusId = dropdownManagementRepository.findChildById(20)// task completed Id
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
				// Generate application number
				String licenseNo = generateLicenseNumber.generateLicenseNumber(serviceId);
				// Find existing registration by applicationNo
				AssessorAccreditorQMSAuditor editAssessorAccreditorQMSAuditor = assessorAccreditorQMSAuditorRepository
						.findByApplicationNo(request.getApplicationNo()) // Returns Optional
						.orElseThrow(() -> new RecordNotFoundException(
								"Institute registration not found with applicationNo: " + request.getApplicationNo()));
				// Update InstituteRegistration entity
				editAssessorAccreditorQMSAuditor.setRegistrationNo(licenseNo);
				// update
				assessorAccreditorQMSAuditorRepository.save(editAssessorAccreditorQMSAuditor);
				
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
				user.setFirstName(request.getFullName());
				user.setMiddleName("");
				user.setLastName("");
				user.setLocationId(request.getDzongkhagId());
				user.setGenderId(request.getGenderId());
				user.setDoB(request.getDateOfBirth());
				user.setMobileNo(request.getMobileNo());
				user.setEmailId(request.getEmail());
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
				
			} else {
				taskStatusId = dropdownManagementRepository.findChildById(18) // task unclaimed Id
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}
			// 5. update workflow
			workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId,
					request.getUserId(), request.getRemarks(), serviceId, null);

			// 6. update task flow
			workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, roleService.getNextRoleId(),
					request.getUserId(), request.getRemarks());

			// 7. Save documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), request.getApplicationNo(),
						"institute_proposal", serviceId, userId, null);
			}

			// For update operations - return 200 OK with updated data
			return ResponseEntity.ok().body(Map.of("applicationNo", request.getApplicationNo(), "status",
					HttpStatus.OK.value(), "message", "Application verified successfully"));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit proposal", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}
	
	private Integer getRoleIdByServiceId(Integer serviceId) {
	    if (serviceId == 32) return 30;
	    if (serviceId == 5) return 29;
	    if (serviceId == 3) return 10;
	    return null;
	}

	@Override
	public List<ObjectNode> getApplicationByCitizenIdOrReferenceNo(String citizenId, String referenceNo,String serviceId) {
		List<Tuple> resultList = assessorAccreditorQMSAuditorRepository
				.getApplicationByCitizenIdOrReferenceNo(citizenId, referenceNo, serviceId);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

}
