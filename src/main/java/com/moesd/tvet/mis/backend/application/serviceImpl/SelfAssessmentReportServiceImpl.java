package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SelfAssessmentMemberdto;
import com.moesd.tvet.mis.backend.application.dto.SelfAssessmentQualityStandarddto;
import com.moesd.tvet.mis.backend.application.dto.SelfAssessmentReportdto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationQualityStandard;
import com.moesd.tvet.mis.backend.application.model.RoleService;
import com.moesd.tvet.mis.backend.application.model.SelfAssessmentReport;
import com.moesd.tvet.mis.backend.application.model.SelfAssessmentReportMember;
import com.moesd.tvet.mis.backend.application.model.SelfAssessmentReportQualityResponse;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.QualityStandardRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleServiceRepository;
import com.moesd.tvet.mis.backend.application.repository.SelfAssessmentReportRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.SelfAssessmentReportService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelfAssessmentReportServiceImpl implements SelfAssessmentReportService {

	// Service / status constants (tbl_service_master, tbl_dropdown_child_master)
	private static final int SAR_SERVICE_ID = 19;
	private static final int STATUS_SUBMITTED = 55;
	private static final int STATUS_VERIFIED = 56;
	private static final int STATUS_APPROVED = 57;
	private static final int STATUS_REJECTED = 58;
	private static final int TASK_INITIATED = 18;
	private static final int TASK_COMPLETED = 20;
	private static final int DEFAULT_LOCATION_ID = 14;
	private static final int DEFAULT_ALLOCATED_POINT = 4;

	private static final String DOC_DIRECTORY = "self_assessment_report";
	private static final String ATTACHMENT_BUSINESS_LICENSE = "BUSINESS_LICENSE";
	private static final String ATTACHMENT_TRAINER_CV = "TRAINER_CV";
	private static final String ATTACHMENT_VERIFICATION = "VERIFICATION";

	private final SelfAssessmentReportRepository selfAssessmentReportRepository;
	private final QualityStandardRepository qualityStandardRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final WorkTaskFlowService workTaskFlowService;
	private final DocumentFileUploadService documentFileUploadService;
	private final RoleServiceRepository roleServiceRepository;
	private final ObjectToJson objectTojson;

	// =====================================================================
	// Submit (Institute)
	// =====================================================================
	@Override
	@Transactional
	public ResponseEntity<?> submitSelfAssessmentReport(SelfAssessmentReportdto request) {
		try {
			if (request.getInstituteId() == null)
				throw new RecordNotFoundException("instituteId is required");

			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assignedRoleId is required");

			if (request.getQualityStandards() == null || request.getQualityStandards().isEmpty())
				throw new RecordNotFoundException("Quality standards are required");

			Integer serviceId = request.getServiceId() != null ? request.getServiceId() : SAR_SERVICE_ID;
			Integer statusId = request.getStatusId() != null ? request.getStatusId() : STATUS_SUBMITTED;
			Integer assignedRoleId = request.getAssignedRoleId();
			String userId = request.getUserId();

			// Only one report in process per institute
			if (!selfAssessmentReportRepository.getActiveSelfAssessmentReport(request.getInstituteId()).isEmpty())
				throw new RecordNotFoundException(
						"A Self Assessment Report is already in process for this institute");

			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			// Validate points against the configured quality standards
			Map<Long, Integer> allocatedPoints = getAllocatedPoints(serviceId);
			for (SelfAssessmentQualityStandarddto dto : request.getQualityStandards()) {
				validatePoint(dto.getStandardId(), dto.getSelfPoint(), allocatedPoints);
			}
			java.util.Set<Long> answered = request.getQualityStandards().stream()
					.map(SelfAssessmentQualityStandarddto::getStandardId).collect(Collectors.toSet());
			if (!answered.containsAll(allocatedPoints.keySet()))
				throw new RecordNotFoundException("Please enter points for all quality indicators");

			String applicationNo = generateApplicationNumber.generateApplicationNumber(serviceId);

			SelfAssessmentReport report = SelfAssessmentReport.builder().applicationNo(applicationNo)
					.instituteId(request.getInstituteId()).serviceId(serviceId).statusId(statusId)
					.submittedDate(new java.util.Date()).createdBy(request.getCreatedBy())
					.updatedBy(request.getCreatedBy()).createdAt(new java.util.Date()).updatedAt(new java.util.Date())
					.build();

			// Institute self-assessed points (earned point starts equal to self point)
			List<SelfAssessmentReportQualityResponse> responses = request.getQualityStandards().stream()
					.map(dto -> SelfAssessmentReportQualityResponse.builder().standardId(dto.getStandardId())
							.selfPoint(dto.getSelfPoint()).earnedPoint(dto.getSelfPoint())
							.selfAssessmentReport(report).build())
					.collect(Collectors.toList());
			report.setQualityStandardResponses(responses);

			applyScore(report, serviceId);

			SelfAssessmentReport saved = selfAssessmentReportRepository.save(report);

			// Workflow
			Integer taskStatusId = dropdownManagementRepository.findChildById(TASK_INITIATED)
					.orElseThrow(() -> new RecordNotFoundException("Unclaimed status not found"));

			var workflow = workTaskFlowService.createWorkflow(applicationNo, request.getApplicantName(), serviceId,
					statusId, assignedRoleId, request.getRemarks());

			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, request.getAssignedUserId(),
					workflow, request.getRemarks(), DEFAULT_LOCATION_ID);

			// Documents
			saveDocuments(request.getBusinessLicenseDocuments(), applicationNo, serviceId, userId,
					ATTACHMENT_BUSINESS_LICENSE);
			saveDocuments(request.getTrainerCvDocuments(), applicationNo, serviceId, userId, ATTACHMENT_TRAINER_CV);
			saveDocuments(request.getDocuments(), applicationNo, serviceId, userId, null);

			return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(saved, HttpStatus.CREATED,
					"Self Assessment Report submitted successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			log.error("Failed to submit Self Assessment Report", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					Map.of("message", "Failed to submit Self Assessment Report", "timestamp", LocalDateTime.now()));
		}
	}

	// =====================================================================
	// Save draft (verification page - earned points, justification, members)
	// No workflow movement; allows the entry to be completed over several days
	// =====================================================================
	@Override
	@Transactional
	public ResponseEntity<?> saveDraftSelfAssessmentReport(SelfAssessmentReportdto request) {
		try {
			if (request.getApplicationNo() == null || request.getApplicationNo().isEmpty())
				throw new RecordNotFoundException("applicationNo is required");

			SelfAssessmentReport report = findReport(request.getApplicationNo());

			if (report.getStatusId() != null
					&& (report.getStatusId() == STATUS_APPROVED || report.getStatusId() == STATUS_REJECTED))
				throw new RecordNotFoundException("Report is already closed and cannot be modified");

			updateVerificationData(report, request);
			report.setUpdatedAt(new java.util.Date());
			report.setUpdatedBy(request.getUpdatedBy());
			SelfAssessmentReport saved = selfAssessmentReportRepository.save(report);

			return ResponseEntity.ok(buildResponse(saved, HttpStatus.OK, "Draft saved successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			log.error("Failed to save Self Assessment Report draft", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("message", "Failed to save draft", "timestamp", LocalDateTime.now()));
		}
	}

	// =====================================================================
	// Verify / Endorse / Approve / Reject (workflow)
	// =====================================================================
	@Override
	@Transactional
	public ResponseEntity<?> verifySelfAssessmentReport(SelfAssessmentReportdto request) {
		try {
			if (request.getApplicationNo() == null || request.getApplicationNo().isEmpty())
				throw new RecordNotFoundException("applicationNo is required");

			if (request.getAssignedRoleId() == null)
				throw new RecordNotFoundException("assigned RoleId is required");

			if (request.getStatusId() == null)
				throw new RecordNotFoundException("statusId is required");

			Integer serviceId = request.getServiceId() != null ? request.getServiceId() : SAR_SERVICE_ID;
			Integer assignedRoleId = request.getAssignedRoleId();
			Integer statusId = request.getStatusId();
			String actorId = String.valueOf(request.getUpdatedBy());

			boolean isClosing = statusId == STATUS_APPROVED || statusId == STATUS_REJECTED;
			Integer taskStatusId = dropdownManagementRepository
					.findChildById(isClosing ? TASK_COMPLETED : TASK_INITIATED)
					.orElseThrow(() -> new RecordNotFoundException("Task Status Id not found"));

			serviceMasterRepository.findById(serviceId)
					.orElseThrow(() -> new RecordNotFoundException("Service Id not found"));

			RoleService roleService = roleServiceRepository.getNextAssignedRole(assignedRoleId, serviceId, statusId)
					.orElseThrow(() -> new RecordNotFoundException("Next assigned role not found"));

			SelfAssessmentReport report = findReport(request.getApplicationNo());

			updateVerificationData(report, request);

			// Verification requires at least one accreditor
			if (statusId == STATUS_VERIFIED && report.getMembers().stream()
					.noneMatch(m -> SelfAssessmentReportMember.TYPE_ACCREDITOR.equals(m.getMemberType())))
				throw new RecordNotFoundException("Please add at least one Accreditor before verification");

			report.setStatusId(statusId);
			report.setUpdatedAt(new java.util.Date());
			report.setUpdatedBy(request.getUpdatedBy());
			SelfAssessmentReport saved = selfAssessmentReportRepository.save(report);

			// actor = logged in user; next task goes to the next role group (or a specific user if sent)
			String workflowActor = request.getUserId() != null ? request.getUserId() : actorId;
			workTaskFlowService.updateWorkflow(request.getApplicationNo(), statusId, assignedRoleId, workflowActor,
					request.getRemarks(), serviceId, request.getUpdatedBy());

			workTaskFlowService.updateTaskFlow(request.getApplicationNo(), taskStatusId, roleService.getNextRoleId(),
					request.getAssignedUserId(), request.getRemarks());

			saveDocuments(request.getDocuments(), request.getApplicationNo(), serviceId, actorId,
					ATTACHMENT_VERIFICATION);

			return ResponseEntity
					.ok(buildResponse(saved, HttpStatus.OK, "Self Assessment Report updated successfully"));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("message", e.getMessage(), "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			log.error("Failed to update Self Assessment Report", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
					Map.of("message", "Failed to update Self Assessment Report", "timestamp", LocalDateTime.now()));
		}
	}

	// =====================================================================
	// Read
	// =====================================================================
	@Override
	public List<ObjectNode> getSelfAssessmentReportByApplicationNo(String applicationNo) {
		return objectTojson._toJson(selfAssessmentReportRepository.getSelfAssessmentReportByApplicationNo(applicationNo));
	}

	@Override
	public List<ObjectNode> getSelfAssessmentReportsByUserId(String userId) {
		return objectTojson._toJson(selfAssessmentReportRepository.getSelfAssessmentReportsByUserId(userId));
	}

	@Override
	public Map<String, Object> getBasicInformationByUserId(String userId) {
		List<ObjectNode> institute = objectTojson._toJson(selfAssessmentReportRepository.getInstituteDetailsByUserId(userId));
		return buildBasicInformation(institute);
	}

	@Override
	public Map<String, Object> getBasicInformationByInstituteId(Integer instituteId) {
		List<ObjectNode> institute = objectTojson
				._toJson(selfAssessmentReportRepository.getInstituteDetailsByInstituteId(instituteId));
		return buildBasicInformation(institute);
	}

	@Override
	public List<ObjectNode> getActiveSelfAssessmentReport(Integer instituteId) {
		return objectTojson._toJson(selfAssessmentReportRepository.getActiveSelfAssessmentReport(instituteId));
	}

	// =====================================================================
	// Helpers
	// =====================================================================
	private Map<String, Object> buildResponse(SelfAssessmentReport saved, HttpStatus status, String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("applicationNo", saved.getApplicationNo());
		body.put("id", saved.getId());
		body.put("status", status.value());
		body.put("totalScore", saved.getTotalScore());
		body.put("grade", saved.getGrade());
		body.put("message", message);
		return body;
	}

	private Map<String, Object> buildBasicInformation(List<ObjectNode> institute) {
		Map<String, Object> result = new LinkedHashMap<>();
		ObjectNode instituteNode = institute.isEmpty() ? null : institute.get(0);
		result.put("institute", instituteNode);

		Integer instituteId = null;
		if (instituteNode != null && instituteNode.hasNonNull("institute_id")) {
			try {
				instituteId = Integer.valueOf(instituteNode.get("institute_id").asString());
			} catch (NumberFormatException e) {
				log.warn("Invalid institute id {}", instituteNode.get("institute_id"));
			}
		}

		if (instituteId != null) {
			result.put("trainers", objectTojson._toJson(selfAssessmentReportRepository.getTrainersByInstituteId(instituteId)));
			result.put("courses", objectTojson._toJson(selfAssessmentReportRepository.getCoursesByInstituteId(instituteId)));
		} else {
			result.put("trainers", new ArrayList<>());
			result.put("courses", new ArrayList<>());
		}
		return result;
	}

	private SelfAssessmentReport findReport(String applicationNo) {
		return selfAssessmentReportRepository.findByApplicationNo(applicationNo).orElseThrow(
				() -> new RecordNotFoundException("Self Assessment Report not found with applicationNo: " + applicationNo));
	}

	/**
	 * Updates earned points / justification and the ACCREDITATORS section, then
	 * recalculates the score.
	 */
	private void updateVerificationData(SelfAssessmentReport report, SelfAssessmentReportdto request) {
		Integer serviceId = report.getServiceId() != null ? report.getServiceId() : SAR_SERVICE_ID;

		if (request.getQualityStandards() != null && !request.getQualityStandards().isEmpty()) {
			Map<Long, Integer> allocatedPoints = getAllocatedPoints(serviceId);
			Map<Long, SelfAssessmentReportQualityResponse> existingMap = report.getQualityStandardResponses().stream()
					.collect(Collectors.toMap(SelfAssessmentReportQualityResponse::getStandardId, Function.identity(),
							(a, b) -> a));

			for (SelfAssessmentQualityStandarddto dto : request.getQualityStandards()) {
				if (dto.getStandardId() == null)
					continue;
				if (dto.getEarnedPoint() != null)
					validatePoint(dto.getStandardId(), dto.getEarnedPoint(), allocatedPoints);

				SelfAssessmentReportQualityResponse existing = existingMap.get(dto.getStandardId());
				if (existing != null) {
					existing.setEarnedPoint(dto.getEarnedPoint());
					existing.setJustification(dto.getJustification());
				} else {
					report.getQualityStandardResponses()
							.add(SelfAssessmentReportQualityResponse.builder().standardId(dto.getStandardId())
									.earnedPoint(dto.getEarnedPoint()).justification(dto.getJustification())
									.selfAssessmentReport(report).build());
				}
			}
		}

		// Members are replaced only when the section is sent
		if (request.getAccreditors() != null || request.getCoordinators() != null) {
			report.getMembers().clear();
			addMembers(report, request.getAccreditors(), SelfAssessmentReportMember.TYPE_ACCREDITOR);
			addMembers(report, request.getCoordinators(), SelfAssessmentReportMember.TYPE_COORDINATOR);
		}

		applyScore(report, serviceId);
	}

	private void addMembers(SelfAssessmentReport report, List<SelfAssessmentMemberdto> members, String type) {
		if (members == null)
			return;
		members.stream().filter(Objects::nonNull)
				.filter(m -> m.getName() != null && !m.getName().trim().isEmpty())
				.forEach(m -> report.getMembers()
						.add(SelfAssessmentReportMember.builder().memberType(type).userId(m.getUserId())
								.name(m.getName().trim()).designation(m.getDesignation())
								.selfAssessmentReport(report).build()));
	}

	private void saveDocuments(com.moesd.tvet.mis.backend.application.dto.Documentdto[] documents, String applicationNo,
			Integer serviceId, String userId, String attachmentType) {
		if (documents != null && documents.length > 0) {
			documentFileUploadService.saveDocument(documents, applicationNo, DOC_DIRECTORY, serviceId, userId,
					attachmentType);
		}
	}

	private void validatePoint(Long standardId, Integer point, Map<Long, Integer> allocatedPoints) {
		if (standardId == null)
			throw new RecordNotFoundException("standardId is required");
		Integer max = allocatedPoints.get(standardId);
		if (max == null)
			throw new RecordNotFoundException("Invalid quality standard: " + standardId);
		if (point == null || point < 0 || point > max)
			throw new RecordNotFoundException("Points for standard " + standardId + " must be between 0 and " + max);
	}

	/**
	 * Allocated points of every sub-indicator (leaf) for the service. A leaf is a
	 * standard that is not the parent of any other standard.
	 */
	private Map<Long, Integer> getAllocatedPoints(Integer serviceId) {
		List<InstituteRegistrationQualityStandard> standards = getActiveStandards(serviceId);
		java.util.Set<Integer> parentIds = standards.stream().map(InstituteRegistrationQualityStandard::getParentId)
				.filter(Objects::nonNull).collect(Collectors.toSet());

		Map<Long, Integer> result = new HashMap<>();
		for (InstituteRegistrationQualityStandard s : standards) {
			if (s.getParentId() != null && s.getParentId() != 0 && !parentIds.contains(s.getId())) {
				result.put(s.getId().longValue(), parsePoint(s.getDescription()));
			}
		}
		return result;
	}

	/**
	 * Score per criteria = earned / allocated * criteria weight. Criteria weight is
	 * stored in description as "Level|Score" (e.g. "Critical|160").
	 */
	private void applyScore(SelfAssessmentReport report, Integer serviceId) {
		List<InstituteRegistrationQualityStandard> standards = getActiveStandards(serviceId);
		if (standards.isEmpty())
			return;

		Map<Integer, InstituteRegistrationQualityStandard> byId = standards.stream()
				.collect(Collectors.toMap(InstituteRegistrationQualityStandard::getId, Function.identity()));
		java.util.Set<Integer> parentIds = standards.stream().map(InstituteRegistrationQualityStandard::getParentId)
				.filter(Objects::nonNull).collect(Collectors.toSet());

		Map<Long, Integer> pointsByStandard = new HashMap<>();
		for (SelfAssessmentReportQualityResponse r : report.getQualityStandardResponses()) {
			Integer point = r.getEarnedPoint() != null ? r.getEarnedPoint() : r.getSelfPoint();
			pointsByStandard.put(r.getStandardId(), point == null ? 0 : point);
		}

		Map<Integer, int[]> criteriaTotals = new HashMap<>(); // criteriaId -> [allocated, earned]
		for (InstituteRegistrationQualityStandard s : standards) {
			boolean isLeaf = s.getParentId() != null && s.getParentId() != 0 && !parentIds.contains(s.getId());
			if (!isLeaf)
				continue;
			Integer criteriaId = findRootId(s, byId);
			int[] totals = criteriaTotals.computeIfAbsent(criteriaId, k -> new int[2]);
			totals[0] += parsePoint(s.getDescription());
			totals[1] += pointsByStandard.getOrDefault(s.getId().longValue(), 0);
		}

		double total = 0d;
		for (Map.Entry<Integer, int[]> entry : criteriaTotals.entrySet()) {
			int allocated = entry.getValue()[0];
			int earned = entry.getValue()[1];
			if (allocated == 0)
				continue;
			InstituteRegistrationQualityStandard criteria = byId.get(entry.getKey());
			double weight = parseWeight(criteria != null ? criteria.getDescription() : null, allocated);
			total += (earned * weight) / allocated;
		}

		double rounded = BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP).doubleValue();
		report.setTotalScore(rounded);
		report.setGrade(resolveGrade(rounded));
	}

	private List<InstituteRegistrationQualityStandard> getActiveStandards(Integer serviceId) {
		return qualityStandardRepository.findByServiceId(serviceId).stream()
				.filter(s -> s.getIsActive() == null || s.getIsActive() == 1).collect(Collectors.toList());
	}

	private Integer findRootId(InstituteRegistrationQualityStandard s,
			Map<Integer, InstituteRegistrationQualityStandard> byId) {
		InstituteRegistrationQualityStandard current = s;
		int guard = 0;
		while (current.getParentId() != null && current.getParentId() != 0 && guard++ < 10) {
			InstituteRegistrationQualityStandard parent = byId.get(current.getParentId());
			if (parent == null)
				break;
			current = parent;
		}
		return current.getId();
	}

	private int parsePoint(String description) {
		try {
			return description != null && description.trim().matches("\\d+") ? Integer.parseInt(description.trim())
					: DEFAULT_ALLOCATED_POINT;
		} catch (NumberFormatException e) {
			return DEFAULT_ALLOCATED_POINT;
		}
	}

	private double parseWeight(String description, int fallback) {
		if (description != null && description.contains("|")) {
			String value = description.substring(description.lastIndexOf('|') + 1).trim();
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException e) {
				log.warn("Invalid criteria weight '{}'", description);
			}
		}
		return fallback;
	}

	/** A+ (901-1000), A (801-900), B (701-800), C (601-700), Not Accredited (<= 600) */
	private String resolveGrade(double score) {
		if (score > 900)
			return "A+";
		if (score > 800)
			return "A";
		if (score > 700)
			return "B";
		if (score > 600)
			return "C";
		return "Not Accredited";
	}
}
