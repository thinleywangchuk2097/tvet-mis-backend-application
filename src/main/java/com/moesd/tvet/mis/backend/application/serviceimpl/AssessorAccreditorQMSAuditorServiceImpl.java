package com.moesd.tvet.mis.backend.application.serviceimpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AssessorAccreditorQMSAuditordto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.AssessorAccreditorQMSAuditor;
import com.moesd.tvet.mis.backend.application.model.AssessorAccreditorQMSAuditorAudit;
import com.moesd.tvet.mis.backend.application.model.AuditorWorkExperienceAudit;
import com.moesd.tvet.mis.backend.application.model.AuditorWorkExperience;
import com.moesd.tvet.mis.backend.application.model.Role;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.model.UserRole;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.AssessorAccreditorQMSAuditorAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.AssessorAccreditorQMSAuditorRepository;
import com.moesd.tvet.mis.backend.application.repository.AuditorWorkExperienceAuditRepository;
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
import com.moesd.tvet.mis.backend.application.utility.NotificationService;
import com.moesd.tvet.mis.backend.application.utility.NotificationService.Recipient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	private final NotificationService notificationService;
	private final AssessorAccreditorQMSAuditorAuditRepository assessorAccreditorQMSAuditorAuditRepository;
	private final AuditorWorkExperienceAuditRepository auditorWorkExperienceAuditRepository;

	/** workflow status "Rejected": a rejected applicant may apply again */
	private static final int STATUS_REJECTED = 58;
	/** workflow status "Approved" */
	private static final int STATUS_APPROVED = 57;

	@Override
	@Transactional
	public ResponseEntity<?> registerAssessorAccreditorQMSAuditor(AssessorAccreditorQMSAuditordto request) {
		try {
			// Validate required fields
			if (request.getServiceId() == null)
				throw new RecordNotFoundException("serviceId is required");

			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("AssignedRoleId is required");

			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");

			// one application per person per registration type (a rejected one may apply again)
			String duplicate = checkDuplicateApplication(request.getServiceId(), request.getCitizenId(),
					request.getReferenceNo(), Boolean.TRUE.equals(request.getRenewal()));
			if (duplicate != null)
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body(Map.of("message", duplicate, "timestamp", LocalDateTime.now()));

			String userId = request.getUserId();
			Integer serviceId = request.getServiceId();
			Integer assignedRoleId = request.getAssignedRoleId();
			Integer statusId = request.getStatusId();
			Integer locationId = 14; // default

			// Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);

			// Build AssessorAccreditorQMSAuditor entity
			AssessorAccreditorQMSAuditor registration = AssessorAccreditorQMSAuditor.builder()
					.applicationNo(applicationNo).serviceId(serviceId).referenceNo(request.getReferenceNo())
					.citizenId(request.getCitizenId()).dateOfBirth(request.getDateOfBirth())
					.fullName(request.getFullName()).genderId(request.getGenderId()).mobileNo(request.getMobileNo())
					.email(request.getEmail()).dzongkhagId(request.getDzongkhagId())
					.organizationName(request.getOrganizationName()).sectorId(request.getSectorId())
					.sectorName(request.getSectorName()).occupationId(request.getOccupationId())
					.occupationName(request.getOccupationName()).certificationLevelId(request.getCertificationLevelId())
					.certificationLevelName(request.getCertificationLevelName()).designation(request.getDesignation())
					.yearsOfExperience(request.getYearsOfExperience()).responsibility(request.getResponsibility())
					.qmsTraining(request.getQmsTraining()).academicBackground(request.getAcademicBackground())
					.statusId(statusId).createdAt(LocalDateTime.now()).createdBy(userId).updatedAt(LocalDateTime.now())
					.updatedBy(userId).remarks(request.getRemarks()).build();

			// Build work experiences and add to registration
			if (request.getWorkExperiences() != null && !request.getWorkExperiences().isEmpty()) {
				List<AuditorWorkExperience> workExperiences = request.getWorkExperiences().stream()
						.map(workExpDto -> AuditorWorkExperience.builder()
								.organizationName(workExpDto.getOrganizationName())
								.designation(workExpDto.getDesignation()).year(workExpDto.getYear())
								.responsibility(workExpDto.getResponsibility()).createdAt(LocalDateTime.now())
								.updatedAt(LocalDateTime.now()).assessorAccreditorQMSAuditor(registration) 
																											
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

			// Acknowledge the submission to the applicant by SMS and email
			notifySubmitted(savedRegistration, Boolean.TRUE.equals(request.getRenewal()));

			// Return response
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of("applicationNo", applicationNo, "id", savedRegistration.getId(), "status",
							HttpStatus.CREATED.value(), "message",
							"Assessor/Accreditor/QMS Auditor registration submitted successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			log.error("Failed to submit assessor/accreditor/qms auditor registration", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit assessor/accreditor/qms auditor registration", "timestamp", LocalDateTime.now()));
		}
	}

	/**
	 * Message when this CID (or reference no for applicants without CID) already has an application
	 * for the same service; null when there is none.
	 * New registration: any application that is not rejected blocks (in process or approved).
	 * Renewal: only an application still in process blocks (the approved registration is what is renewed).
	 */
	@Override
	public String checkDuplicateApplication(Integer serviceId, String rawCitizenId, String rawReferenceNo,
			boolean renewal) {
		String citizenId = trimToNull(rawCitizenId);
		String referenceNo = trimToNull(rawReferenceNo);
		if (citizenId == null && referenceNo == null)
			return null;

		String idLabel = citizenId != null ? "CID " + citizenId : "Reference No " + referenceNo;
		boolean exists = renewal ? hasApplicationInProcess(serviceId, citizenId, referenceNo)
				: hasActiveApplication(serviceId, citizenId, referenceNo);
		if (!exists)
			return null;
		return renewal
				? "An application for " + registrationTypeName(serviceId) + " with " + idLabel
						+ " is already in process. Please wait for it to be approved or rejected."
				: "An application for " + registrationTypeName(serviceId) + " with " + idLabel
						+ " is already submitted or approved. Duplicate applications are not allowed.";
	}

	// in process or approved (anything but rejected)
	private boolean hasActiveApplication(Integer serviceId, String citizenId, String referenceNo) {
		return citizenId != null
				? assessorAccreditorQMSAuditorRepository.existsByServiceIdAndCitizenIdAndStatusIdNot(serviceId,
						citizenId, STATUS_REJECTED)
				: assessorAccreditorQMSAuditorRepository.existsByServiceIdAndReferenceNoAndStatusIdNot(serviceId,
						referenceNo, STATUS_REJECTED);
	}

	// still in process (not approved, not rejected)
	private boolean hasApplicationInProcess(Integer serviceId, String citizenId, String referenceNo) {
		List<Integer> closed = List.of(STATUS_APPROVED, STATUS_REJECTED);
		return citizenId != null
				? assessorAccreditorQMSAuditorRepository.existsByServiceIdAndCitizenIdAndStatusIdNotIn(serviceId,
						citizenId, closed)
				: assessorAccreditorQMSAuditorRepository.existsByServiceIdAndReferenceNoAndStatusIdNotIn(serviceId,
						referenceNo, closed);
	}

	private static String trimToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	@Override
	public List<ObjectNode> getApplicationDetails(String applicationNo) {
		return objectTojson
				._toJson(assessorAccreditorQMSAuditorRepository.getApplicationDetailByApplicationNo(applicationNo));
	}

	@Override
	@Transactional
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
				// Renewal: the person's earlier approved registration for this service is copied to the
				// audit tables and removed; the renewed registration keeps its registration no (= login id)
				List<AssessorAccreditorQMSAuditor> previous = previousApprovedRegistrations(
						existingAssessorAccreditorQMSAuditor);
				String licenseNo = previous.stream().map(AssessorAccreditorQMSAuditor::getRegistrationNo)
						.filter(no -> no != null && !no.isBlank()).findFirst()
						.orElseGet(() -> generateLicenseNumber.generateLicenseNumber(serviceId));
				archivePreviousRegistrations(previous, existingAssessorAccreditorQMSAuditor);
				// Find existing registration by applicationNo
				AssessorAccreditorQMSAuditor editAssessorAccreditorQMSAuditor = assessorAccreditorQMSAuditorRepository
						.findByApplicationNo(request.getApplicationNo()) // Returns Optional
						.orElseThrow(() -> new RecordNotFoundException(
								"Institute registration not found with applicationNo: " + request.getApplicationNo()));
				// Update InstituteRegistration entity
				editAssessorAccreditorQMSAuditor.setRegistrationNo(licenseNo);
				// update
				assessorAccreditorQMSAuditorRepository.save(editAssessorAccreditorQMSAuditor);

				// Role for this registration type (32 Assessor, 5 Accreditor, 3 QMS Auditor)
				final Integer roleId = getRoleIdByServiceId(serviceId);
				if (roleId == null) {
					throw new RecordNotFoundException("Invalid serviceId: " + serviceId + " for role mapping");
				}
				Role role = roleRepository.findById(roleId)
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
								"Role with ID " + roleId + " does not exist"));

				User existingUser = findExistingUser(licenseNo, existingAssessorAccreditorQMSAuditor);
				if (existingUser != null) {
					// already has a login: no new user, only add this role if it is not there yet
					addRoleIfMissing(existingUser, role);
					notifyRoleAdded(existingAssessorAccreditorQMSAuditor, request, serviceId,
							existingUser.getUserId());
				} else {
					String plainPassword = initialPassword(
							request.getFullName() != null ? request.getFullName()
									: existingAssessorAccreditorQMSAuditor.getFullName());
					createUser(request, licenseNo, plainPassword, role);
					// approved: login credentials by SMS and email
					notifyDecision(existingAssessorAccreditorQMSAuditor, request, serviceId, true, licenseNo,
							plainPassword, null);
				}
			} else {
				taskStatusId = dropdownManagementRepository.findChildById(18) // task unclaimed Id
						.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));
			}
			// rejected: the reason (reviewer's remarks) by SMS and email
			if (statusId == 58) {
				notifyDecision(existingAssessorAccreditorQMSAuditor, request, serviceId, false, null, null,
						request.getRemarks());
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
			// undo everything (status, renewal archive, user / role) so nothing is half done
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("Failed to verify assessor/accreditor/qms auditor application {}", request.getApplicationNo(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to submit proposal", "error", String.valueOf(e.getMessage()), "timestamp",
					LocalDateTime.now()));
		}
	}

	/**
	 * Login that already exists for this applicant: the new registration no, the applicant's CID
	 * (e.g. an NDI login), or the registration no of an earlier approved registration of the same
	 * person (Assessor / Accreditor / QMS Auditor). Null when the person has no login yet.
	 */
	private User findExistingUser(String licenseNo, AssessorAccreditorQMSAuditor applicant) {
		String citizenId = trimToNull(applicant.getCitizenId());
		String referenceNo = trimToNull(applicant.getReferenceNo());

		List<String> candidates = new ArrayList<>();
		candidates.add(licenseNo);
		if (citizenId != null)
			candidates.add(citizenId);
		if (citizenId != null || referenceNo != null)
			candidates.addAll(
					assessorAccreditorQMSAuditorRepository.findApprovedRegistrationNos(citizenId, referenceNo));

		return candidates.stream().distinct().map(userRepository::findByUserId).flatMap(Optional::stream)
				.findFirst().orElse(null);
	}

	/** Adds the role to tbl_user_role only when the user does not have it yet. */
	private void addRoleIfMissing(User user, Role role) {
		if (userRoleRepository.existsByUserIdAndRoleId(user.getId(), role.getId())) {
			log.info("User {} already has role {}; nothing added", user.getUserId(), role.getId());
			return;
		}
		UserRole userRole = new UserRole();
		userRole.setUser(user);
		userRole.setRole(role);
		userRoleRepository.save(userRole);
		log.info("Role {} added to existing user {}", role.getId(), user.getUserId());
	}

	/** New login for the approved applicant, with this registration's role. */
	private void createUser(AssessorAccreditorQMSAuditordto request, String licenseNo, String plainPassword,
			Role role) {
		User user = new User();
		user.setUserId(licenseNo);
		user.setPassword(passwordEncoder.encode(plainPassword));
		user.setFirstName(request.getFullName());
		user.setMiddleName("");
		user.setLastName("");
		user.setLocationId(request.getDzongkhagId());
		user.setGenderId(request.getGenderId());
		user.setDoB(request.getDateOfBirth());
		user.setMobileNo(request.getMobileNo());
		user.setEmailId(request.getEmail());
		user.setCurrentRole(role.getId());
		user.setStatusId("1");
		user.setCreatedAt(new Date());
		user.setCreatedBy(null);
		user = userRepository.save(user);

		UserRole userRole = new UserRole();
		userRole.setUser(user);
		userRole.setRole(role);
		List<UserRole> userRoles = new ArrayList<>(List.of(userRole));
		userRoleRepository.saveAll(userRoles);
		user.setUserRoles(userRoles);
		userRepository.save(user);
	}

	/** Approved, but the person already had a login: tell them the role was added (no new password). */
	private void notifyRoleAdded(AssessorAccreditorQMSAuditor applicant, AssessorAccreditorQMSAuditordto request,
			Integer serviceId, String existingUserId) {
		String mobile = applicant.getMobileNo() != null ? applicant.getMobileNo() : request.getMobileNo();
		String email = applicant.getEmail() != null ? applicant.getEmail() : request.getEmail();
		String name = applicant.getFullName() != null ? applicant.getFullName() : request.getFullName();
		notificationService.sendStatus(new Recipient(mobile, email, name),
				registrationTypeName(serviceId) + " registration", applicant.getApplicationNo(), "Approved", null,
				"The " + registrationTypeName(serviceId) + " role has been added to your existing TVET-MIS account (User ID: "
						+ existingUserId + "). Please log in with your existing password.",
				"REGISTRATION_APPROVED");
	}

	/** The person's other approved registrations for the same service, newest first (by CID, or reference no). */
	private List<AssessorAccreditorQMSAuditor> previousApprovedRegistrations(AssessorAccreditorQMSAuditor current) {
		String citizenId = trimToNull(current.getCitizenId());
		String referenceNo = trimToNull(current.getReferenceNo());
		List<AssessorAccreditorQMSAuditor> previous;
		if (citizenId != null) {
			previous = assessorAccreditorQMSAuditorRepository.findByServiceIdAndCitizenIdAndStatusIdAndApplicationNoNot(
					current.getServiceId(), citizenId, STATUS_APPROVED, current.getApplicationNo());
		} else if (referenceNo != null) {
			previous = assessorAccreditorQMSAuditorRepository.findByServiceIdAndReferenceNoAndStatusIdAndApplicationNoNot(
					current.getServiceId(), referenceNo, STATUS_APPROVED, current.getApplicationNo());
		} else {
			return List.of();
		}
		return previous.stream()
				.sorted(Comparator.comparing(AssessorAccreditorQMSAuditor::getUpdatedAt,
						Comparator.nullsLast(Comparator.reverseOrder())))
				.toList();
	}

	/**
	 * Copies each old registration (and its work experiences) into the audit tables, linked to the
	 * renewed registration, then deletes the old row (its work experiences are removed with it).
	 * Flushed straight away so the renewed row can take over the unique registration no.
	 */
	private void archivePreviousRegistrations(List<AssessorAccreditorQMSAuditor> previous,
			AssessorAccreditorQMSAuditor renewed) {
		if (previous.isEmpty())
			return;
		for (AssessorAccreditorQMSAuditor old : previous) {
			AssessorAccreditorQMSAuditorAudit audit = AssessorAccreditorQMSAuditorAudit.builder()
					.applicationNo(old.getApplicationNo()).serviceId(old.getServiceId())
					.referenceNo(old.getReferenceNo()).citizenId(old.getCitizenId()).dateOfBirth(old.getDateOfBirth())
					.fullName(old.getFullName()).genderId(old.getGenderId()).mobileNo(old.getMobileNo())
					.email(old.getEmail()).dzongkhagId(old.getDzongkhagId()).organizationName(old.getOrganizationName())
					.sectorId(old.getSectorId()).sectorName(old.getSectorName()).occupationId(old.getOccupationId())
					.occupationName(old.getOccupationName()).certificationLevelId(old.getCertificationLevelId())
					.certificationLevelName(old.getCertificationLevelName()).designation(old.getDesignation())
					.yearsOfExperience(old.getYearsOfExperience()).responsibility(old.getResponsibility())
					.qmsTraining(old.getQmsTraining()).academicBackground(old.getAcademicBackground())
					.statusId(old.getStatusId()).createdAt(old.getCreatedAt()).createdBy(old.getCreatedBy())
					.updatedAt(old.getUpdatedAt()).updatedBy(old.getUpdatedBy()).remarks(old.getRemarks())
					.assessorAccreditorQMSAuditor(renewed).build();
			audit.setRegistrationNo(old.getRegistrationNo());
			assessorAccreditorQMSAuditorAuditRepository.save(audit);

			if (old.getWorkExperiences() != null && !old.getWorkExperiences().isEmpty()) {
				auditorWorkExperienceAuditRepository.saveAll(old.getWorkExperiences().stream()
						.map(we -> AuditorWorkExperienceAudit.builder().applicationNo(old.getApplicationNo())
								.organizationName(we.getOrganizationName()).designation(we.getDesignation())
								.year(we.getYear()).responsibility(we.getResponsibility())
								.createdAt(we.getCreatedAt()).updatedAt(we.getUpdatedAt()).build())
						.toList());
			}
			log.info("Registration {} ({}) archived on renewal {}", old.getApplicationNo(), old.getRegistrationNo(),
					renewed.getApplicationNo());
		}
		assessorAccreditorQMSAuditorRepository.deleteAll(previous);
		assessorAccreditorQMSAuditorRepository.flush();
	}

	/** Initial login password: full name without spaces + current year + "#" (same rule as institutes). */
	private String initialPassword(String fullName) {
		String name = fullName == null ? "" : fullName.replaceAll("\\s+", "");
		return name + java.time.Year.now().getValue() + "#";
	}

	private String registrationTypeName(Integer serviceId) {
		if (serviceId == null)
			return "registration";
		return switch (serviceId) {
		case 32 -> "Assessor";
		case 5 -> "Accreditor";
		case 3 -> "QMS Auditor";
		default -> "registration";
		};
	}

	/** Submission acknowledgement by SMS and email; a failure here never fails the submission. */
	private void notifySubmitted(AssessorAccreditorQMSAuditor registration, boolean renewal) {
		try {
			String title = registrationTypeName(registration.getServiceId()) + (renewal ? " renewal" : " registration");
			notificationService.sendStatus(
					new Recipient(registration.getMobileNo(), registration.getEmail(), registration.getFullName()),
					title, registration.getApplicationNo(), "Submitted", null,
					"It will now be reviewed by TVET Quality Assurance. You will be informed of the result by SMS and email.",
					renewal ? "RENEWAL_SUBMITTED" : "REGISTRATION_SUBMITTED");
		} catch (Exception e) {
			log.warn("Registration {} saved but the submission SMS/email failed: {}", registration.getApplicationNo(),
					e.getMessage());
		}
	}

	/** Result by SMS and email. Approved: User ID and password. Rejected: the reason. */
	private void notifyDecision(AssessorAccreditorQMSAuditor applicant, AssessorAccreditorQMSAuditordto request,
			Integer serviceId, boolean approved, String loginId, String password, String remarks) {
		String mobile = applicant.getMobileNo() != null ? applicant.getMobileNo() : request.getMobileNo();
		String email = applicant.getEmail() != null ? applicant.getEmail() : request.getEmail();
		String name = applicant.getFullName() != null ? applicant.getFullName() : request.getFullName();
		String title = registrationTypeName(serviceId) + " registration";

		if (approved) {
			notificationService.sendCredentials(mobile, email, name, title, applicant.getApplicationNo(), loginId,
					password);
		} else {
			notificationService.sendStatus(new Recipient(mobile, email, name), title, applicant.getApplicationNo(), "Rejected",
					remarks == null || remarks.isBlank() ? "Not specified" : remarks,
					"You may contact the TVET Quality Assurance office for more information.",
					"REGISTRATION_REJECTED");
		}
	}

	private Integer getRoleIdByServiceId(Integer serviceId) {
		if (serviceId == 32)
			return 30;
		if (serviceId == 5)
			return 29;
		if (serviceId == 3)
			return 10;
		return null;
	}

	@Override
	public List<ObjectNode> getApplicationByCitizenIdOrReferenceNo(String citizenId, String referenceNo,
			String serviceId) {
		return objectTojson._toJson(assessorAccreditorQMSAuditorRepository
				.getApplicationByCitizenIdOrReferenceNo(citizenId, referenceNo, serviceId));
	}

	@Override
	public List<ObjectNode> getRegisteredByServiceId(Integer serviceId) {
		// only the three registration services are listed publicly
		if (serviceId == null || !List.of(3, 5, 32).contains(serviceId)) {
			return List.of();
		}
		return objectTojson._toJson(assessorAccreditorQMSAuditorRepository.getRegisteredByServiceId(serviceId));
	}

}
