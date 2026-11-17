package com.moesd.tvet.mis.backend.application.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramAnnouncementDto;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramTrainerAppliedDto;
import com.moesd.tvet.mis.backend.application.dto.TOTCertificateRequestDto;
import com.moesd.tvet.mis.backend.application.dto.TOTCompleteApplicationDto;
import com.moesd.tvet.mis.backend.application.dto.TOTModuleMarksDto;
import com.moesd.tvet.mis.backend.application.dto.TOTTrainerSelectionDto;
import com.moesd.tvet.mis.backend.application.dto.TotCourseContentDto;
import com.moesd.tvet.mis.backend.application.dto.TotModuleDto;
import com.moesd.tvet.mis.backend.application.dto.TotProgramDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.TOTAnnouncement;
import com.moesd.tvet.mis.backend.application.model.TOTCourseContent;
import com.moesd.tvet.mis.backend.application.model.TOTModule;
import com.moesd.tvet.mis.backend.application.model.TOTProgram;
import com.moesd.tvet.mis.backend.application.model.TOTProgramTrainerApplied;
import com.moesd.tvet.mis.backend.application.model.TOTTrainerContentMarks;
import com.moesd.tvet.mis.backend.application.model.TOTTrainerModuleResult;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.model.WorkFlowListAudit;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import com.moesd.tvet.mis.backend.application.model.TaskFlowListAudit;
import com.moesd.tvet.mis.backend.application.repository.WorkFlowListRepository;
import com.moesd.tvet.mis.backend.application.repository.WorkFlowListAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListRepository;
import com.moesd.tvet.mis.backend.application.repository.TaskFlowListAuditRepository;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTAnnouncementRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTModuleRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTProgramRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTProgramTrainerAppliedRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTTrainerContentMarksRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTTrainerModuleResultRepository;
import com.moesd.tvet.mis.backend.application.service.TotService;
import com.moesd.tvet.mis.backend.application.service.WorkTaskFlowService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TotServiceImpl implements TotService {

	private final TOTProgramRepository totProgramRepository;
	private final TOTAnnouncementRepository totAnnouncementRepository;
	private final TOTModuleRepository totModuleRepository;
	private final TOTProgramTrainerAppliedRepository totProgramTrainerAppliedRepository;
	private final TOTTrainerContentMarksRepository totTrainerContentMarksRepository;
	private final TOTTrainerModuleResultRepository totTrainerModuleResultRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ObjectToJson objectTojson;
	private final WorkTaskFlowService workTaskFlowService;
	private final DropdownManagementRepository dropdownManagementRepository;
	private final WorkFlowListRepository workFlowListRepository;
	private final WorkFlowListAuditRepository workFlowListAuditRepository;
	private final TaskFlowListRepository taskFlowListRepository;
	private final TaskFlowListAuditRepository taskFlowListAuditRepository;

	/** Workflow status "Approved" and task status "Completed" (same ids used by other services). */
	private static final Integer WF_STATUS_APPROVED = 57;
	private static final Integer TASK_STATUS_COMPLETED_ID = 20;
	private static final int STATUS_ACTIVE = 122;
	private static final int STATUS_DELETED = 123;
	private static final BigDecimal HUNDRED = new BigDecimal("100");

	// response keys
	private static final String STATUS = "status";
	private static final String MESSAGE = "message";
	private static final String APPLICATION_NO = "applicationNo";
	private static final String TRAINER_ID = "trainerId";
	private static final String MODULE_ID = "moduleId";
	private static final String MODULES = "modules";
	private static final String TRAINERS = "trainers";
	private static final String CONTENTS = "contents";
	private static final String FULL_MARK = "fullMark";
	private static final String MODULE_TOTAL = "moduleTotal";
	private static final String MODULE_MAX = "moduleMax";
	private static final String WEIGHTAGE = "weightage";
	private static final String WEIGHTED_SCORE = "weightedScore";
	private static final String PERCENTAGE = "percentage";
	private static final String OVERALL_PERCENTAGE = "overallPercentage";
	private static final String GRADE = "grade";
	private static final String RESULT = "result";
	private static final String CERTIFICATE_NO = "certificateNo";
	private static final String GENERATED_AT = "generatedAt";

	/** Totals of one trainer's marks for one module. */
	private record ModuleScore(BigDecimal total, BigDecimal max, BigDecimal weightage, BigDecimal weightedScore) {
	}

	/** Overall percentage of one trainer plus the per-module percentage / grade rows. */
	private record TrainerScore(BigDecimal overall, List<Map<String, Object>> modules) {
	}

	// ======================= TOT programme =======================

	public ResponseEntity<?> submitTOTProgram(TotProgramDto request) {
		try {
			if (request.getId() != null && request.getId() > 0) {
				updateProgram(request);
				return respond(HttpStatus.OK, "TOT Program updated successfully.");
			}
			createProgram(request);
			return respond(HttpStatus.OK, "TOT Program added successfully.");

		} catch (RecordNotFoundException e) {
			return respond(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Failed to submit TOT Program", e);
			return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to submit TOT Program");
		}
	}

	private void updateProgram(TotProgramDto request) {
		TOTProgram program = totProgramRepository.findById(request.getId()).orElseThrow(
				() -> new RecordNotFoundException("TOT Program not found with id: " + request.getId()));

		program.setProgramName(request.getProgramName());
		program.setProgramCode(request.getProgramCode());
		program.setStatusId(request.getStatusId());
		program.setProgramTypeId(request.getProgramTypeId());
		program.setDescription(request.getDescription());
		program.setUpdatedBy(request.getCreatedBy());
		program.setUpdatedAt(new Date());

		totProgramRepository.save(program);

		if (request.getModules() == null || request.getModules().isEmpty())
			return;

		List<TOTModule> modules = new ArrayList<>();
		for (TotModuleDto moduleDto : request.getModules()) {
			modules.add(upsertModule(moduleDto, program));
		}
		totModuleRepository.saveAll(modules);
		program.setModules(modules);
	}

	/** Existing module (matched by module code) or a new one, with its course contents replaced. */
	private TOTModule upsertModule(TotModuleDto moduleDto, TOTProgram program) {
		TOTModule module = hasText(moduleDto.getModuleCode())
				? totModuleRepository.findByModuleCode(moduleDto.getModuleCode()).orElse(new TOTModule())
				: new TOTModule();

		module.setModuleCode(moduleDto.getModuleCode());
		module.setModuleName(moduleDto.getModuleName());
		module.setDescription(moduleDto.getDescription());
		module.setDuration(moduleDto.getDuration());
		module.setWeightage(moduleDto.getWeightage());
		module.setLearningOutcomes(moduleDto.getLearningOutcomes());
		module.setPrerequisites(moduleDto.getPrerequisites());
		module.setModuleOrder(moduleDto.getOrder());
		module.setTotProgram(program);

		// Replace course contents (orphanRemoval deletes old rows)
		List<TOTCourseContent> contents = module.getTotCourseContent();
		if (contents == null) {
			contents = new ArrayList<>();
			module.setTotCourseContent(contents);
		} else {
			contents.clear();
		}
		contents.addAll(buildCourseContents(moduleDto.getCourseContents(), module));
		return module;
	}

	private void createProgram(TotProgramDto request) {
		TOTProgram program = TOTProgram.builder()
				.programName(request.getProgramName())
				.programCode(request.getProgramCode())
				.statusId(request.getStatusId())
				.programTypeId(request.getProgramTypeId())
				.description(request.getDescription())
				.createdBy(request.getCreatedBy())
				.createdAt(new Date())
				.build();

		if (request.getModules() != null && !request.getModules().isEmpty()) {
			List<TOTModule> modules = new ArrayList<>();
			for (TotModuleDto moduleDto : request.getModules()) {
				TOTModule module = TOTModule.builder()
						.moduleCode(moduleDto.getModuleCode())
						.moduleName(moduleDto.getModuleName())
						.description(moduleDto.getDescription())
						.duration(moduleDto.getDuration())
						.weightage(moduleDto.getWeightage())
						.learningOutcomes(moduleDto.getLearningOutcomes())
						.prerequisites(moduleDto.getPrerequisites())
						.totProgram(program)
						.statusId(STATUS_ACTIVE)
						.moduleOrder(moduleDto.getOrder())
						.build();
				module.setTotCourseContent(buildCourseContents(moduleDto.getCourseContents(), module));
				modules.add(module);
			}
			program.setModules(modules);
		}

		// CascadeType.ALL persists TOTProgram -> TOTModule -> TOTCourseContent
		totProgramRepository.save(program);
	}

	/** Course content rows for a module; null rows and rows with neither content nor mark are skipped. */
	private static List<TOTCourseContent> buildCourseContents(List<TotCourseContentDto> dtos, TOTModule module) {
		List<TOTCourseContent> contents = new ArrayList<>();
		if (dtos == null)
			return contents;
		dtos.stream()
				.filter(cc -> cc != null && (hasText(cc.getCourseContent()) || hasText(cc.getFullMark())))
				.map(cc -> TOTCourseContent.builder()
						.courseContent(cc.getCourseContent())
						.fullMark(cc.getFullMark())
						.totModule(module)
						.build())
				.forEach(contents::add);
		return contents;
	}

	@Override
	public List<ObjectNode> getToTPrograms() {
		return objectTojson._toJson(totProgramRepository.getToTPrograms());
	}

	@Override
	public ResponseEntity<?> deleteToTPrograms(Long id) {
		TOTProgram program = totProgramRepository.findById(id)
				.orElseThrow(() -> new RecordNotFoundException("TOT Program not found with id: " + id));
		// Soft delete
		program.setStatusId(STATUS_DELETED);
		totProgramRepository.save(program);
		return ResponseEntity.ok("TOT Program deleted successfully.");
	}

	// ======================= Announcement =======================

	@Override
	public ResponseEntity<?> submitTOTProgramAnnouncement(TOTProgramAnnouncementDto request) {
		try {
			if (request.getId() != null && request.getId() > 0) {
				// Update existing announcement
				TOTAnnouncement totAnnouncement = totAnnouncementRepository.findById(request.getId()).orElseThrow(
						() -> new RecordNotFoundException("TOT Announcement not found with id: " + request.getId()));

				totAnnouncement.setApplicationStartDate(request.getApplicationStartDate());
				totAnnouncement.setApplicationEndDate(request.getApplicationEndDate());
				totAnnouncement.setProgramStartDate(request.getProgramStartDate());
				totAnnouncement.setProgramEndDate(request.getProgramEndDate());
				totAnnouncement.setMaxParticipants(request.getMaxParticipants());
				totAnnouncement.setVenue(request.getVenue());
				totAnnouncement.setEligibilityCriteria(request.getEligibilityCriteria());
				totAnnouncement.setRemarks(request.getRemarks());
				totAnnouncement.setProgramTypeId(request.getProgramTypeId());
				totAnnouncement.setShowId(request.getShowId());
				totAnnouncement.setUpdatedBy(request.getCreatedBy());
				totAnnouncement.setUpdatedAt(new Date());

				totAnnouncementRepository.save(totAnnouncement);
				return respond(HttpStatus.OK, "TOT Announcement updated successfully.");
			}

			// Create new announcement
			TOTProgram program = totProgramRepository.findById(request.getProgramId()).orElseThrow(
					() -> new RecordNotFoundException("TOT Program not found with id: " + request.getProgramId()));

			String applicationNo = generateApplicationNumber.generateApplicationNumber(24);

			TOTAnnouncement totAnnouncement = TOTAnnouncement.builder().applicationNo(applicationNo)
					.applicationStartDate(request.getApplicationStartDate())
					.applicationEndDate(request.getApplicationEndDate())
					.eligibilityCriteria(request.getEligibilityCriteria())
					.maxParticipants(request.getMaxParticipants()).venue(request.getVenue()).statusId(STATUS_ACTIVE)
					.totProgram(program).programTypeId(request.getProgramTypeId())
					.showId(request.getShowId())
					.programStartDate(request.getProgramStartDate()).programEndDate(request.getProgramEndDate())
					.remarks(request.getRemarks()).createdBy(request.getCreatedBy()).createdAt(new Date()).build();

			totAnnouncementRepository.save(totAnnouncement);
			return respond(HttpStatus.CREATED, "TOT Announcement created successfully.");

		} catch (RecordNotFoundException e) {
			return respond(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Failed to submit TOT Announcement", e);
			return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to submit TOT Announcement");
		}
	}

	@Override
	public List<ObjectNode> getToTProgramsAnnouncement() {
		return objectTojson._toJson(totProgramRepository.getToTProgramsAnnouncement());
	}

	@Override
	public ResponseEntity<?> deleteToTProgramsAnnouncement(Long id) {
		TOTAnnouncement totannouncement = totAnnouncementRepository.findById(id)
				.orElseThrow(() -> new RecordNotFoundException("TOT announcement program not found with id: " + id));
		// Soft delete
		totannouncement.setStatusId(STATUS_DELETED);
		totAnnouncementRepository.save(totannouncement);
		return ResponseEntity.ok("TOT Program announcement deleted successfully.");
	}

	// ======================= Trainer application =======================

	@Override
	public ResponseEntity<?> applyTrainerToTOTProgram(List<TOTProgramTrainerAppliedDto> requests) {
		try {
			if (requests == null || requests.isEmpty()) {
				throw new RecordNotFoundException("Trainer application list is required");
			}
			requests.forEach(TotServiceImpl::validateTrainerApplication);

			// All rows must belong to the same application & institute
			TOTProgramTrainerAppliedDto firstRequest = requests.get(0);
			boolean mixed = requests.stream().anyMatch(r ->
					!firstRequest.getApplicationNo().equals(r.getApplicationNo())
					|| !firstRequest.getInstituteId().equals(r.getInstituteId()));
			if (mixed) {
				throw new RecordNotFoundException("All trainers must be for the same application and institute");
			}

			// An institute can apply only once per announcement (application no)
			if (totProgramTrainerAppliedRepository.existsByApplicationNoAndInstituteId(
					firstRequest.getApplicationNo(), firstRequest.getInstituteId())) {
				return respond(HttpStatus.CONFLICT, "Your institute has already applied for this program.");
			}

			// Ignore duplicate trainer ids in the same request
			Set<Integer> seenTrainers = new HashSet<>();
			List<TOTProgramTrainerApplied> trainerList = requests.stream()
					.filter(r -> seenTrainers.add(r.getTrainerId()))
					.map(request -> TOTProgramTrainerApplied.builder()
							.applicationNo(request.getApplicationNo())
							.instituteId(request.getInstituteId())
							.trainerId(request.getTrainerId())
							.programAnnouncementId(request.getProgramAnnouncementId())
							.createdBy(request.getCreatedBy())
							.createdAt(new Date())
							.build())
					.toList();
			totProgramTrainerAppliedRepository.saveAll(trainerList);

			// Workflow + task (initiated)
			String applicationNo = firstRequest.getApplicationNo();
			Integer assignedRoleId = firstRequest.getAssignedRoleId();
			Integer taskStatusId = dropdownManagementRepository.findChildById(18)
					.orElseThrow(() -> new RecordNotFoundException("Initiated status not found"));

			WorkFlowList workflow = workTaskFlowService.createWorkflow(applicationNo, null,
					firstRequest.getServiceId(), firstRequest.getStatusId(), assignedRoleId, firstRequest.getRemarks());
			workTaskFlowService.createTaskFlow(applicationNo, taskStatusId, assignedRoleId, firstRequest.getUserId(),
					workflow, firstRequest.getRemarks(), 14);

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of(STATUS, 201, MESSAGE, "Trainers applied successfully.", "count", trainerList.size()));

		} catch (RecordNotFoundException e) {
			return respond(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Failed to apply trainers", e);
			return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to apply trainers.");
		}
	}

	private static void validateTrainerApplication(TOTProgramTrainerAppliedDto request) {
		if (request.getApplicationNo() == null || request.getApplicationNo().isEmpty())
			throw new RecordNotFoundException("applicationNo is required");
		if (request.getServiceId() == null)
			throw new RecordNotFoundException("serviceId is required");
		if (request.getInstituteId() == null)
			throw new RecordNotFoundException("instituteId is required");
		if (request.getTrainerId() == null)
			throw new RecordNotFoundException("trainerId is required");
		if (request.getProgramAnnouncementId() == null)
			throw new RecordNotFoundException("programAnnouncementId is required");
	}

	@Override
	public List<ObjectNode> getTrainerApplied(String applicationNo) {
		return objectTojson._toJson(totAnnouncementRepository.getTrainerApplied(applicationNo));
	}

	@Override
	@Transactional
	public ResponseEntity<?> saveTrainerSelection(TOTTrainerSelectionDto request) {
		try {
			if (request == null || !hasText(request.getApplicationNo())) {
				throw new RecordNotFoundException("applicationNo is required");
			}

			List<TOTProgramTrainerApplied> appliedList =
					totProgramTrainerAppliedRepository.findByApplicationNo(request.getApplicationNo());
			if (appliedList.isEmpty()) {
				throw new RecordNotFoundException(
						"No trainers applied for application no: " + request.getApplicationNo());
			}

			Set<Integer> selectedIds = request.getSelectedTrainerIds() == null
					? new HashSet<>()
					: new HashSet<>(request.getSelectedTrainerIds());
			if (selectedIds.isEmpty()) {
				throw new RecordNotFoundException("Please select at least one trainer");
			}

			Set<Integer> appliedIds = appliedList.stream()
					.map(TOTProgramTrainerApplied::getTrainerId)
					.collect(Collectors.toSet());
			if (!appliedIds.containsAll(selectedIds)) {
				throw new RecordNotFoundException("One or more selected trainers did not apply for this program");
			}

			Date now = new Date();
			for (TOTProgramTrainerApplied applied : appliedList) {
				applied.setIsSelected(selectedIds.contains(applied.getTrainerId()) ? 1 : 0);
				applied.setSelectedBy(request.getUpdatedBy());
				applied.setSelectedAt(now);
				applied.setUpdatedBy(request.getUpdatedBy());
				applied.setUpdatedAt(now);
			}
			totProgramTrainerAppliedRepository.saveAll(appliedList);

			return ResponseEntity.ok(Map.of(
					STATUS, 200,
					MESSAGE, "Trainer selection saved successfully.",
					"selectedCount", selectedIds.size()));

		} catch (RecordNotFoundException e) {
			return respond(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Failed to save trainer selection", e);
			return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save trainer selection.");
		}
	}

	/** Parses values like "30", "30%", " 25.5 " into a number; returns null if not numeric. */
	private BigDecimal parseNumber(String value) {
		if (value == null) return null;
		String cleaned = value.replace("%", "").trim();
		if (cleaned.isEmpty()) return null;
		try {
			return new BigDecimal(cleaned);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	// ======================= Module marks =======================

	@Override
	@Transactional
	public ResponseEntity<?> saveModuleMarks(TOTModuleMarksDto request) {
		try {
			validateMarksRequest(request);

			TOTProgramTrainerApplied applied = findSelectedTrainer(request.getApplicationNo(), request.getTrainerId());

			TOTModule module = totModuleRepository.findById(request.getModuleId())
					.orElseThrow(() -> new RecordNotFoundException("Module not found: " + request.getModuleId()));
			List<TOTCourseContent> moduleContents = module.getTotCourseContent() == null
					? new ArrayList<>() : module.getTotCourseContent();
			if (moduleContents.isEmpty()) {
				throw new RecordNotFoundException("Module has no course contents");
			}

			Map<Long, BigDecimal> submitted = submittedMarks(request);
			Date now = new Date();
			BigDecimal moduleTotal = BigDecimal.ZERO;
			BigDecimal moduleMax = BigDecimal.ZERO;
			List<TOTTrainerContentMarks> toSave = new ArrayList<>();

			// Full marks are taken from the DB (not trusted from the client)
			for (TOTCourseContent content : moduleContents) {
				BigDecimal fullMark = parseNumber(content.getFullMark());
				if (fullMark == null) fullMark = BigDecimal.ZERO;
				BigDecimal marks = validMarks(content, submitted.get(content.getId()), fullMark);

				toSave.add(contentMarksRow(request, applied.getId(), module.getId(), content, fullMark, marks, now));
				moduleTotal = moduleTotal.add(marks);
				moduleMax = moduleMax.add(fullMark);
			}
			totTrainerContentMarksRepository.saveAll(toSave);

			BigDecimal weightage = parseNumber(module.getWeightage());
			ModuleScore score = new ModuleScore(moduleTotal, moduleMax, weightage,
					weightedScore(moduleTotal, moduleMax, weightage));
			saveModuleResult(request, applied.getId(), module.getId(), score, now);

			Map<String, Object> body = new LinkedHashMap<>();
			body.put(STATUS, 200);
			body.put(MESSAGE, "Module marks saved successfully.");
			body.put(MODULE_TOTAL, score.total());
			body.put(MODULE_MAX, score.max());
			body.put(WEIGHTAGE, score.weightage());
			body.put(WEIGHTED_SCORE, score.weightedScore());
			return ResponseEntity.ok(body);

		} catch (RecordNotFoundException e) {
			return respond(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Failed to save module marks", e);
			return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save module marks.");
		}
	}

	private static void validateMarksRequest(TOTModuleMarksDto request) {
		if (request == null || !hasText(request.getApplicationNo()))
			throw new RecordNotFoundException("applicationNo is required");
		if (request.getTrainerId() == null)
			throw new RecordNotFoundException("trainerId is required");
		if (request.getModuleId() == null)
			throw new RecordNotFoundException("moduleId is required");
	}

	/** Trainer must be applied & selected for this application. */
	private TOTProgramTrainerApplied findSelectedTrainer(String applicationNo, Integer trainerId) {
		TOTProgramTrainerApplied applied = totProgramTrainerAppliedRepository.findByApplicationNo(applicationNo)
				.stream()
				.filter(a -> trainerId.equals(a.getTrainerId()))
				.findFirst()
				.orElseThrow(() -> new RecordNotFoundException("Trainer has not applied for this program"));
		if (!Integer.valueOf(1).equals(applied.getIsSelected())) {
			throw new RecordNotFoundException("Trainer is not selected for this program");
		}
		return applied;
	}

	/** contentId -> marks sent by the client. */
	private static Map<Long, BigDecimal> submittedMarks(TOTModuleMarksDto request) {
		Map<Long, BigDecimal> submitted = new HashMap<>();
		if (request.getContents() == null)
			return submitted;
		request.getContents().stream()
				.filter(cm -> cm != null && cm.getContentId() != null)
				.forEach(cm -> submitted.put(cm.getContentId(), cm.getMarks()));
		return submitted;
	}

	/** Marks must be present and between 0 and the full mark. */
	private static BigDecimal validMarks(TOTCourseContent content, BigDecimal marks, BigDecimal fullMark) {
		if (marks == null) {
			throw new RecordNotFoundException("Marks missing for content: " + content.getCourseContent());
		}
		if (marks.compareTo(BigDecimal.ZERO) < 0 || marks.compareTo(fullMark) > 0) {
			throw new RecordNotFoundException("Marks for '" + content.getCourseContent()
					+ "' must be between 0 and " + fullMark.stripTrailingZeros().toPlainString());
		}
		return marks;
	}

	/** Existing marks row for this trainer + content (updated) or a new one. */
	private TOTTrainerContentMarks contentMarksRow(TOTModuleMarksDto request, Long appliedId, Long moduleId,
			TOTCourseContent content, BigDecimal fullMark, BigDecimal marks, Date now) {
		TOTTrainerContentMarks row = totTrainerContentMarksRepository
				.findByApplicationNoAndTrainerIdAndCourseContentId(
						request.getApplicationNo(), request.getTrainerId(), content.getId())
				.orElse(null);
		if (row == null) {
			row = TOTTrainerContentMarks.builder()
					.applicationNo(request.getApplicationNo())
					.trainerId(request.getTrainerId())
					.courseContentId(content.getId())
					.createdBy(request.getCreatedBy())
					.createdAt(now)
					.build();
		} else {
			row.setUpdatedBy(request.getCreatedBy());
			row.setUpdatedAt(now);
		}
		row.setAppliedId(appliedId);
		row.setModuleId(moduleId);
		row.setFullMark(fullMark);
		row.setMarksObtained(marks);
		return row;
	}

	/** Weighted score = (moduleTotal / moduleMax) * weightage; null without weightage or marks. */
	private static BigDecimal weightedScore(BigDecimal moduleTotal, BigDecimal moduleMax, BigDecimal weightage) {
		if (weightage == null || moduleMax.compareTo(BigDecimal.ZERO) <= 0)
			return null;
		return moduleTotal.multiply(weightage).divide(moduleMax, 2, RoundingMode.HALF_UP);
	}

	private void saveModuleResult(TOTModuleMarksDto request, Long appliedId, Long moduleId, ModuleScore score,
			Date now) {
		TOTTrainerModuleResult result = totTrainerModuleResultRepository
				.findByApplicationNoAndTrainerIdAndModuleId(request.getApplicationNo(), request.getTrainerId(), moduleId)
				.orElse(null);
		if (result == null) {
			result = TOTTrainerModuleResult.builder()
					.applicationNo(request.getApplicationNo())
					.trainerId(request.getTrainerId())
					.moduleId(moduleId)
					.createdBy(request.getCreatedBy())
					.createdAt(now)
					.build();
		} else {
			result.setUpdatedBy(request.getCreatedBy());
			result.setUpdatedAt(now);
		}
		result.setAppliedId(appliedId);
		result.setModuleTotal(score.total());
		result.setModuleMax(score.max());
		result.setWeightage(score.weightage());
		result.setWeightedScore(score.weightedScore());
		totTrainerModuleResultRepository.save(result);
	}

	@Override
	public ResponseEntity<?> getTrainerMarks(String applicationNo) {
		List<Map<String, Object>> contents = totTrainerContentMarksRepository.findByApplicationNo(applicationNo)
				.stream()
				.map(m -> {
					Map<String, Object> row = new LinkedHashMap<>();
					row.put(TRAINER_ID, m.getTrainerId());
					row.put(MODULE_ID, m.getModuleId());
					row.put("contentId", m.getCourseContentId());
					row.put(FULL_MARK, m.getFullMark());
					row.put("marks", m.getMarksObtained());
					return row;
				})
				.toList();

		List<Map<String, Object>> modules = totTrainerModuleResultRepository.findByApplicationNo(applicationNo)
				.stream()
				.map(r -> {
					Map<String, Object> row = new LinkedHashMap<>();
					row.put(TRAINER_ID, r.getTrainerId());
					row.put(MODULE_ID, r.getModuleId());
					row.put(MODULE_TOTAL, r.getModuleTotal());
					row.put(MODULE_MAX, r.getModuleMax());
					row.put(WEIGHTAGE, r.getWeightage());
					row.put(WEIGHTED_SCORE, r.getWeightedScore());
					return row;
				})
				.toList();

		return ResponseEntity.ok(Map.of(CONTENTS, contents, MODULES, modules));
	}

	// ======================= Mark sheet / certificate =======================

	/** Grade scale (percentage). Adjust here if TTTRC uses a different scale. */
	private String gradeFor(BigDecimal percent) {
		if (percent == null) return null;
		double p = percent.doubleValue();
		if (p >= 90) return "A";
		if (p >= 80) return "B";
		if (p >= 70) return "C";
		if (p >= 60) return "D";
		return "E";
	}

	/** Minimum overall percentage to be declared "Competent". */
	private static final BigDecimal COMPETENT_PASS_PERCENT = new BigDecimal("60");

	private String resultFor(BigDecimal percent) {
		if (percent == null) return null;
		return percent.compareTo(COMPETENT_PASS_PERCENT) >= 0 ? "Competent" : "Not Yet Competent";
	}

	/** Bhutan financial year (July-June), e.g. 2025-26. */
	private String financialYear(LocalDate date) {
		LocalDate d = date == null ? LocalDate.now() : date;
		int start = d.getMonthValue() >= 7 ? d.getYear() : d.getYear() - 1;
		return start + "-" + String.format("%02d", (start + 1) % 100);
	}

	/** Module percentage = total * 100 / max; null when the result or its max is missing. */
	private static BigDecimal modulePercent(TOTTrainerModuleResult r) {
		if (r == null || r.getModuleMax() == null || r.getModuleMax().signum() == 0)
			return null;
		return r.getModuleTotal().multiply(HUNDRED).divide(r.getModuleMax(), 2, RoundingMode.HALF_UP);
	}

	private static String resultKey(Integer trainerId, Long moduleId) {
		return trainerId + "-" + moduleId;
	}

	private static boolean hasContents(TOTModule m) {
		return m.getTotCourseContent() != null && !m.getTotCourseContent().isEmpty();
	}

	private Map<String, Object> moduleGradeRow(Long moduleId, BigDecimal percent) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put(MODULE_ID, moduleId);
		row.put(PERCENTAGE, percent);
		row.put(GRADE, gradeFor(percent));
		return row;
	}

	@Override
	@Transactional
	public ResponseEntity<?> generateCertificates(TOTCertificateRequestDto request) {
		try {
			if (request == null || !hasText(request.getApplicationNo())) {
				throw new RecordNotFoundException("applicationNo is required");
			}
			String applicationNo = request.getApplicationNo();

			TOTAnnouncement announcement = totAnnouncementRepository.findFirstByApplicationNo(applicationNo)
					.orElseThrow(() -> new RecordNotFoundException("Announcement not found: " + applicationNo));
			TOTProgram program = announcement.getTotProgram();
			List<TOTModule> modules = certificateModules(program);

			List<TOTProgramTrainerApplied> appliedList = totProgramTrainerAppliedRepository
					.findByApplicationNo(applicationNo).stream()
					.sorted((a, b) -> Long.compare(a.getId(), b.getId()))
					.toList();
			List<TOTProgramTrainerApplied> targets = certificateTargets(appliedList, request.getTrainerIds());

			Map<String, TOTTrainerModuleResult> resultMap = new HashMap<>();
			for (TOTTrainerModuleResult r : totTrainerModuleResultRepository.findByApplicationNo(applicationNo)) {
				resultMap.put(resultKey(r.getTrainerId(), r.getModuleId()), r);
			}

			// Running serial for certificate numbers (keeps already issued numbers)
			int serial = (int) appliedList.stream().filter(a -> a.getCertificateNo() != null).count();
			String prefix = (program.getProgramCode() == null ? "TOT" : program.getProgramCode()) + "/"
					+ financialYear(announcement.getProgramStartDate()) + "/";

			Date now = new Date();
			List<Map<String, Object>> out = new ArrayList<>();
			for (TOTProgramTrainerApplied applied : targets) {
				TrainerScore score = scoreTrainer(applied, modules, resultMap);
				applied.setOverallPercentage(score.overall());
				applied.setGrade(gradeFor(score.overall()));
				applied.setResult(resultFor(score.overall()));
				if (applied.getCertificateNo() == null) {
					serial++;
					applied.setCertificateNo(prefix + serial);
				}
				applied.setCertificateGeneratedBy(request.getGeneratedBy());
				applied.setCertificateGeneratedAt(now);
				out.add(certificateRow(applied, score, now));
			}
			totProgramTrainerAppliedRepository.saveAll(targets);

			Map<String, Object> body = new LinkedHashMap<>();
			body.put(STATUS, 200);
			body.put(MESSAGE, "Certificates generated successfully.");
			body.put(TRAINERS, out);
			return ResponseEntity.ok(body);

		} catch (RecordNotFoundException e) {
			return respond(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Failed to generate certificates", e);
			return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate certificates.");
		}
	}

	/** Only modules that have course contents count towards the result. */
	private static List<TOTModule> certificateModules(TOTProgram program) {
		if (program == null || program.getModules() == null || program.getModules().isEmpty()) {
			throw new RecordNotFoundException("Program has no modules configured");
		}
		List<TOTModule> modules = program.getModules().stream().filter(TotServiceImpl::hasContents).toList();
		if (modules.isEmpty()) {
			throw new RecordNotFoundException("Program modules have no course contents");
		}
		return modules;
	}

	/** Selected trainers, limited to the requested trainer ids when any are sent. */
	private static List<TOTProgramTrainerApplied> certificateTargets(List<TOTProgramTrainerApplied> appliedList,
			List<Integer> trainerIds) {
		Set<Integer> requested = trainerIds == null ? new HashSet<>() : new HashSet<>(trainerIds);
		List<TOTProgramTrainerApplied> targets = appliedList.stream()
				.filter(a -> Integer.valueOf(1).equals(a.getIsSelected()))
				.filter(a -> requested.isEmpty() || requested.contains(a.getTrainerId()))
				.toList();
		if (targets.isEmpty()) {
			throw new RecordNotFoundException("No selected trainers to generate certificates for");
		}
		return targets;
	}

	/** Weighted overall when every module has weightage, otherwise the simple average of module percentages. */
	private TrainerScore scoreTrainer(TOTProgramTrainerApplied applied, List<TOTModule> modules,
			Map<String, TOTTrainerModuleResult> resultMap) {
		BigDecimal weightedSum = BigDecimal.ZERO;
		BigDecimal weightSum = BigDecimal.ZERO;
		BigDecimal percentSum = BigDecimal.ZERO;
		boolean allWeighted = true;
		List<Map<String, Object>> moduleOut = new ArrayList<>();

		for (TOTModule m : modules) {
			TOTTrainerModuleResult r = resultMap.get(resultKey(applied.getTrainerId(), m.getId()));
			BigDecimal modPercent = modulePercent(r);
			if (modPercent == null) {
				throw new RecordNotFoundException("Marks for module '" + m.getModuleName()
						+ "' are not saved for trainer id " + applied.getTrainerId());
			}
			percentSum = percentSum.add(modPercent);
			if (r.getWeightage() == null || r.getWeightedScore() == null) {
				allWeighted = false;
			} else {
				weightedSum = weightedSum.add(r.getWeightedScore());
				weightSum = weightSum.add(r.getWeightage());
			}
			moduleOut.add(moduleGradeRow(m.getId(), modPercent));
		}

		BigDecimal overall = (allWeighted && weightSum.signum() > 0)
				? weightedSum.multiply(HUNDRED).divide(weightSum, 2, RoundingMode.HALF_UP)
				: percentSum.divide(new BigDecimal(modules.size()), 2, RoundingMode.HALF_UP);
		return new TrainerScore(overall, moduleOut);
	}

	private static Map<String, Object> certificateRow(TOTProgramTrainerApplied applied, TrainerScore score,
			Date now) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put(TRAINER_ID, applied.getTrainerId());
		row.put(OVERALL_PERCENTAGE, score.overall());
		row.put(GRADE, applied.getGrade());
		row.put(RESULT, applied.getResult());
		row.put(CERTIFICATE_NO, applied.getCertificateNo());
		row.put(GENERATED_AT, now);
		row.put(MODULES, score.modules());
		return row;
	}

	// ======================= Complete application =======================

	@Override
	@Transactional
	public ResponseEntity<?> completeApplication(TOTCompleteApplicationDto request) {
		try {
			if (request == null || !hasText(request.getApplicationNo())) {
				throw new RecordNotFoundException("applicationNo is required");
			}
			String applicationNo = request.getApplicationNo();

			// Every selected trainer must have a generated mark sheet
			List<TOTProgramTrainerApplied> selected = totProgramTrainerAppliedRepository
					.findByApplicationNo(applicationNo).stream()
					.filter(a -> Integer.valueOf(1).equals(a.getIsSelected()))
					.toList();
			if (selected.isEmpty()) {
				throw new RecordNotFoundException("No trainers have been selected for this application");
			}
			long pending = selected.stream().filter(a -> a.getCertificateNo() == null).count();
			if (pending > 0) {
				throw new RecordNotFoundException(
						"Generate mark sheets for all selected trainers first (" + pending + " pending)");
			}

			Integer taskStatusId = dropdownManagementRepository.findChildById(TASK_STATUS_COMPLETED_ID)
					.orElseThrow(() -> new RecordNotFoundException("Task completed status not found"));

			List<WorkFlowList> workflows = workFlowListRepository.findAllByApplicationNo(applicationNo);
			List<TaskFlowList> tasks = taskFlowListRepository.findAllByApplicationNo(applicationNo);
			if (workflows.isEmpty() || tasks.isEmpty()) {
				throw new RecordNotFoundException("Workflow not found for application no: " + applicationNo);
			}

			String remarks = hasText(request.getRemarks())
					? request.getRemarks()
					: "TOT application completed. Mark sheets generated.";
			Date now = new Date();

			approveWorkflows(workflows, request, remarks, now);
			completeTasks(tasks, taskStatusId, remarks, now);

			return ResponseEntity.ok(Map.of(
					STATUS, 200,
					APPLICATION_NO, applicationNo,
					MESSAGE, "Application completed successfully."));

		} catch (RecordNotFoundException e) {
			return respond(HttpStatus.BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			log.error("Failed to complete TOT application", e);
			return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to complete application.");
		}
	}

	/** Audits each workflow row, then marks it approved. */
	private void approveWorkflows(List<WorkFlowList> workflows, TOTCompleteApplicationDto request, String remarks,
			Date now) {
		for (WorkFlowList wf : workflows) {
			workFlowListAuditRepository.save(WorkFlowListAudit.builder()
					.applicationNo(wf.getApplicationNo())
					.applicationName(wf.getApplicationName())
					.serviceId(wf.getServiceId())
					.statusId(wf.getStatusId())
					.roleId(wf.getRoleId())
					.actorId(wf.getActorId())
					.wfRemarks(wf.getWfRemarks())
					.createdAt(wf.getActionDate())
					.actionDate(now)
					.workFlow(wf)
					.build());

			wf.setStatusId(WF_STATUS_APPROVED);
			wf.setActorId(request.getUserId());
			wf.setWfRemarks(remarks);
			wf.setUpdatedBy(request.getUpdatedBy());
			wf.setUpdatedAt(now);
			wf.setActionDate(now);
		}
		workFlowListRepository.saveAll(workflows);
	}

	/** Audits each task row, then marks it completed. */
	private void completeTasks(List<TaskFlowList> tasks, Integer taskStatusId, String remarks, Date now) {
		for (TaskFlowList task : tasks) {
			taskFlowListAuditRepository.save(TaskFlowListAudit.builder()
					.applicationNo(task.getApplicationNo())
					.taskStatusId(task.getTaskStatusId())
					.assignedRoleId(task.getAssignedRoleId())
					.assignedUserId(task.getAssignedUserId())
					.workflowId(task.getWorkFlow() != null ? task.getWorkFlow().getId() : null)
					.saveRemarks(task.getSaveRemarks())
					.actionDate(task.getActionDate())
					.locationId(task.getLocationId())
					.taskFlow(task)
					.auditActionDate(now)
					.build());

			task.setTaskStatusId(taskStatusId);
			task.setSaveRemarks(remarks);
			task.setActionDate(now);
		}
		taskFlowListRepository.saveAll(tasks);
	}

	@Override
	public ResponseEntity<?> getInstituteApplied(Integer instituteId) {
		// One entry per application no this institute has applied to
		Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
		for (TOTProgramTrainerApplied a : totProgramTrainerAppliedRepository.findByInstituteId(instituteId)) {
			Map<String, Object> entry = grouped.computeIfAbsent(a.getApplicationNo(), k -> {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put(APPLICATION_NO, a.getApplicationNo());
				m.put("programAnnouncementId", a.getProgramAnnouncementId());
				m.put("appliedAt", a.getCreatedAt());
				m.put("trainerIds", new ArrayList<Integer>());
				return m;
			});
			@SuppressWarnings("unchecked")
			List<Integer> ids = (List<Integer>) entry.get("trainerIds");
			ids.add(a.getTrainerId());
		}
		return ResponseEntity.ok(new ArrayList<>(grouped.values()));
	}

	// ======================= Certificate lookup (read only) =======================

	@Override
	public List<ObjectNode> getCertifiedPrograms(Integer instituteId) {
		return objectTojson._toJson(totProgramTrainerAppliedRepository.getCertifiedPrograms(instituteId));
	}

	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCertificates(String applicationNo, Integer instituteId) {
		try {
			if (!hasText(applicationNo)) {
				throw new RecordNotFoundException("Application number is required");
			}
			String appNo = applicationNo.trim();

			TOTAnnouncement announcement = totAnnouncementRepository.findFirstByApplicationNo(appNo)
					.orElseThrow(() -> new RecordNotFoundException("No TOT programme found for application no " + appNo));
			TOTProgram program = announcement.getTotProgram();

			Map<Integer, Map<Long, BigDecimal>> marksByTrainer = marksByTrainer(appNo);
			Map<Integer, List<Map<String, Object>>> resultsByTrainer = resultsByTrainer(appNo);

			List<Map<String, Object>> trainers = new ArrayList<>();
			for (Tuple t : totProgramTrainerAppliedRepository.getCertifiedTrainers(appNo, instituteId)) {
				trainers.add(certifiedTrainerRow(t, marksByTrainer, resultsByTrainer));
			}

			Map<String, Object> body = new LinkedHashMap<>();
			body.put("program", programDetails(appNo, program, announcement));
			body.put(TRAINERS, trainers);
			return ResponseEntity.ok(body);

		} catch (RecordNotFoundException e) {
			return respond(HttpStatus.NOT_FOUND, e.getMessage());
		} catch (Exception e) {
			log.error("Failed to load TOT certificates", e);
			return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to load certificates.");
		}
	}

	/** Programme header plus its modules (only modules with course contents, in module order). */
	private static Map<String, Object> programDetails(String appNo, TOTProgram program, TOTAnnouncement announcement) {
		List<Map<String, Object>> modules = new ArrayList<>();
		if (program != null && program.getModules() != null) {
			program.getModules().stream()
					.filter(TotServiceImpl::hasContents)
					.sorted(TotServiceImpl::compareModuleOrder)
					.map(TotServiceImpl::moduleDetails)
					.forEach(modules::add);
		}

		Map<String, Object> programOut = new LinkedHashMap<>();
		programOut.put(APPLICATION_NO, appNo);
		programOut.put("programName", program != null ? program.getProgramName() : null);
		programOut.put("programCode", program != null ? program.getProgramCode() : null);
		programOut.put("venue", announcement.getVenue());
		programOut.put("programStartDate", announcement.getProgramStartDate());
		programOut.put("programEndDate", announcement.getProgramEndDate());
		programOut.put(MODULES, modules);
		return programOut;
	}

	/** Module order first (modules without an order go last), then id. */
	private static int compareModuleOrder(TOTModule a, TOTModule b) {
		int oa = a.getModuleOrder() == null ? Integer.MAX_VALUE : a.getModuleOrder();
		int ob = b.getModuleOrder() == null ? Integer.MAX_VALUE : b.getModuleOrder();
		return oa != ob ? Integer.compare(oa, ob) : Long.compare(a.getId(), b.getId());
	}

	private static Map<String, Object> moduleDetails(TOTModule m) {
		Map<String, Object> mm = new LinkedHashMap<>();
		mm.put("id", m.getId());
		mm.put("moduleCode", m.getModuleCode());
		mm.put("moduleName", m.getModuleName());
		mm.put("duration", m.getDuration());
		mm.put(WEIGHTAGE, m.getWeightage());
		List<Map<String, Object>> contents = new ArrayList<>();
		for (TOTCourseContent c : m.getTotCourseContent()) {
			Map<String, Object> cc = new LinkedHashMap<>();
			cc.put("id", c.getId());
			cc.put("courseContent", c.getCourseContent());
			cc.put(FULL_MARK, c.getFullMark());
			contents.add(cc);
		}
		mm.put(CONTENTS, contents);
		return mm;
	}

	/** trainerId -> (courseContentId -> marks obtained). */
	private Map<Integer, Map<Long, BigDecimal>> marksByTrainer(String appNo) {
		Map<Integer, Map<Long, BigDecimal>> marksByTrainer = new HashMap<>();
		for (TOTTrainerContentMarks cm : totTrainerContentMarksRepository.findByApplicationNo(appNo)) {
			marksByTrainer.computeIfAbsent(cm.getTrainerId(), k -> new HashMap<>())
					.put(cm.getCourseContentId(), cm.getMarksObtained());
		}
		return marksByTrainer;
	}

	/** trainerId -> module result rows (percentage, grade, weightage, weighted score). */
	private Map<Integer, List<Map<String, Object>>> resultsByTrainer(String appNo) {
		Map<Integer, List<Map<String, Object>>> resultsByTrainer = new HashMap<>();
		for (TOTTrainerModuleResult r : totTrainerModuleResultRepository.findByApplicationNo(appNo)) {
			Map<String, Object> mr = moduleGradeRow(r.getModuleId(), modulePercent(r));
			mr.put(WEIGHTAGE, r.getWeightage());
			mr.put(WEIGHTED_SCORE, r.getWeightedScore());
			resultsByTrainer.computeIfAbsent(r.getTrainerId(), k -> new ArrayList<>()).add(mr);
		}
		return resultsByTrainer;
	}

	private static Map<String, Object> certifiedTrainerRow(Tuple t, Map<Integer, Map<Long, BigDecimal>> marksByTrainer,
			Map<Integer, List<Map<String, Object>>> resultsByTrainer) {
		Integer trainerId = t.get("trainer_id") == null ? null : ((Number) t.get("trainer_id")).intValue();
		Object gen = t.get("certificate_generated_at");

		Map<String, Object> row = new LinkedHashMap<>();
		row.put(TRAINER_ID, trainerId);
		row.put("instituteId", t.get("institute_id"));
		row.put("instituteName", t.get("institute_name"));
		row.put("name", t.get("name"));
		row.put("cid", t.get("cid"));
		row.put(OVERALL_PERCENTAGE, t.get("overall_percentage"));
		row.put(GRADE, t.get(GRADE));
		row.put(RESULT, t.get(RESULT));
		row.put(CERTIFICATE_NO, t.get("certificate_no"));
		row.put(GENERATED_AT, gen == null ? null : gen.toString());
		Map<String, Object> marks = new LinkedHashMap<>();
		marksByTrainer.getOrDefault(trainerId, Map.of()).forEach((k, v) -> marks.put(String.valueOf(k), v));
		row.put("marks", marks);
		row.put(MODULES, resultsByTrainer.getOrDefault(trainerId, List.of()));
		return row;
	}

	// ======================= Helpers =======================

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	/** {status, message} body with the HTTP status code repeated as "status". */
	private static ResponseEntity<Map<String, Object>> respond(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of(STATUS, status.value(), MESSAGE, message));
	}
}
