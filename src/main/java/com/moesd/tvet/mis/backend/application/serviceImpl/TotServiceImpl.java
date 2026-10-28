package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

	public ResponseEntity<?> submitTOTProgram(TotProgramDto request) {
	    try {

	        // ================= UPDATE =================
	        if (request.getId() != null && request.getId() > 0) {

	            TOTProgram program = totProgramRepository.findById(request.getId()).orElseThrow(
	                    () -> new RecordNotFoundException("TOT Program not found with id: " + request.getId()));

	            program.setProgramName(request.getProgramName());
	            program.setProgramCode(request.getProgramCode());
	            program.setStatusId(request.getStatusId());
	            program.setProgramTypeId(request.getProgramTypeId());
	            program.setDescription(request.getDescription());
	            program.setUpdatedBy(request.getCreatedBy());
	            program.setUpdatedAt(new java.util.Date());

	            totProgramRepository.save(program);

	            if (request.getModules() != null && !request.getModules().isEmpty()) {

	                List<TOTModule> modules = new ArrayList<>();

	                for (TotModuleDto moduleDto : request.getModules()) {

	                    // Find existing module by moduleCode (or create new)
	                    TOTModule module;
	                    if (moduleDto.getModuleCode() != null && !moduleDto.getModuleCode().isBlank()) {
	                        module = totModuleRepository.findByModuleCode(moduleDto.getModuleCode())
	                                .orElse(new TOTModule());
	                    } else {
	                        module = new TOTModule();
	                    }

	                    module.setModuleCode(moduleDto.getModuleCode());
	                    module.setModuleName(moduleDto.getModuleName());
	                    module.setDescription(moduleDto.getDescription());
	                    module.setDuration(moduleDto.getDuration());
	                    module.setWeightage(moduleDto.getWeightage());	       
	                    module.setLearningOutcomes(moduleDto.getLearningOutcomes());
	                    module.setPrerequisites(moduleDto.getPrerequisites());
	                    module.setModuleOrder(moduleDto.getOrder());
	                    module.setTotProgram(program);

	                    // ---- Replace course contents (orphanRemoval deletes old rows) ----
	                    List<TOTCourseContent> contents = module.getTotCourseContent();
	                    if (contents == null) {
	                        contents = new ArrayList<>();
	                        module.setTotCourseContent(contents);
	                    } else {
	                        contents.clear();
	                    }

	                    if (moduleDto.getCourseContents() != null) {
	                        for (TotCourseContentDto ccDto : moduleDto.getCourseContents()) {
	                            if (ccDto == null) continue;

	                            boolean contentBlank = ccDto.getCourseContent() == null
	                                    || ccDto.getCourseContent().isBlank();
	                            boolean markBlank = ccDto.getFullMark() == null
	                                    || ccDto.getFullMark().isBlank();
	                            if (contentBlank && markBlank) continue; // skip empty row

	                            TOTCourseContent cc = TOTCourseContent.builder()
	                                    .courseContent(ccDto.getCourseContent())
	                                    .fullMark(ccDto.getFullMark())
	                                    .totModule(module)
	                                    .build();
	                            contents.add(cc);
	                        }
	                    }

	                    modules.add(module);
	                }

	                totModuleRepository.saveAll(modules);
	                program.setModules(modules);
	            }

	            return ResponseEntity.ok(Map.of("status", 200, "message", "TOT Program updated successfully."));
	        }

	        // ================= CREATE =================
	        TOTProgram program = TOTProgram.builder()
	                .programName(request.getProgramName())
	                .programCode(request.getProgramCode())
	                .statusId(request.getStatusId())
	                .programTypeId(request.getProgramTypeId())
	                .description(request.getDescription())
	                .createdBy(request.getCreatedBy())
	                .createdAt(new java.util.Date())
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
	                        .statusId(122)
	                        .moduleOrder(moduleDto.getOrder())
	                        .build();

	                // Build course contents
	                List<TOTCourseContent> contents = new ArrayList<>();
	                if (moduleDto.getCourseContents() != null) {
	                    for (TotCourseContentDto ccDto : moduleDto.getCourseContents()) {
	                        if (ccDto == null) continue;

	                        boolean contentBlank = ccDto.getCourseContent() == null
	                                || ccDto.getCourseContent().isBlank();
	                        boolean markBlank = ccDto.getFullMark() == null
	                                || ccDto.getFullMark().isBlank();
	                        if (contentBlank && markBlank) continue; // skip empty row

	                        TOTCourseContent cc = TOTCourseContent.builder()
	                                .courseContent(ccDto.getCourseContent())
	                                .fullMark(ccDto.getFullMark())
	                                .totModule(module)
	                                .build();
	                        contents.add(cc);
	                    }
	                }
	                module.setTotCourseContent(contents);

	                modules.add(module);
	            }

	            program.setModules(modules);
	        }

	        // CascadeType.ALL persists TOTProgram -> TOTModule -> TOTCourseContent
	        totProgramRepository.save(program);

	        return ResponseEntity.ok(Map.of("status", 200, "message", "TOT Program added successfully."));

	    } catch (RecordNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of("status", 404, "message", e.getMessage()));
	    } catch (Exception e) {
	        log.error("Failed to submit TOT Program", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("status", 500, "message", "Failed to submit TOT Program"));
	    }
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
		program.setStatusId(123);
		// Save changes
		totProgramRepository.save(program);
		return ResponseEntity.ok("TOT Program deleted successfully.");
	}

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
				// Audit fields (if available)
				totAnnouncement.setUpdatedBy(request.getCreatedBy());
				totAnnouncement.setUpdatedAt(new Date());

				totAnnouncementRepository.save(totAnnouncement);

				return ResponseEntity.ok(Map.of("status", 200, "message", "TOT Announcement updated successfully."));

			} else {

				// Create new announcement
				TOTProgram program = totProgramRepository.findById(request.getProgramId()).orElseThrow(
						() -> new RecordNotFoundException("TOT Program not found with id: " + request.getProgramId()));

				// Generate application number
				String applicationNo = generateApplicationNumber.generateApplicationNumber(24);

				TOTAnnouncement totAnnouncement = TOTAnnouncement.builder().applicationNo(applicationNo)
						.applicationStartDate(request.getApplicationStartDate())
						.applicationEndDate(request.getApplicationEndDate())
						.eligibilityCriteria(request.getEligibilityCriteria())
						.maxParticipants(request.getMaxParticipants()).venue(request.getVenue()).statusId(122)
						.totProgram(program).programTypeId(request.getProgramTypeId())
						.showId(request.getShowId())
						.programStartDate(request.getProgramStartDate()).programEndDate(request.getProgramEndDate())
						.remarks(request.getRemarks()).createdBy(request.getCreatedBy()).createdAt(new Date()).build();

				totAnnouncementRepository.save(totAnnouncement);

				return ResponseEntity.status(HttpStatus.CREATED)
						.body(Map.of("status", 201, "message", "TOT Announcement created successfully."));
			}

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", 404, "message", e.getMessage()));
		} catch (Exception e) {
		    log.error("Failed to submit TOT Announcement", e);
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(Map.of("status", 500, "message", "Failed to submit TOT Announcement"));
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
		totannouncement.setStatusId(123);
		// Save changes
		totAnnouncementRepository.save(totannouncement);

		return ResponseEntity.ok("TOT Program announcement deleted successfully.");
	}

	@Override
	public ResponseEntity<?> applyTrainerToTOTProgram(List<TOTProgramTrainerAppliedDto> requests) {
	    try {

	        if (requests == null || requests.isEmpty()) {
	            throw new RecordNotFoundException("Trainer application list is required");
	        }

	        // Validate request data
	        for (TOTProgramTrainerAppliedDto request : requests) {

	            if (request.getApplicationNo() == null || request.getApplicationNo().isEmpty()) {
	                throw new RecordNotFoundException("applicationNo is required");
	            }

	            if (request.getServiceId() == null) {
	                throw new RecordNotFoundException("serviceId is required");
	            }

	            if (request.getInstituteId() == null) {
	                throw new RecordNotFoundException("instituteId is required");
	            }

	            if (request.getTrainerId() == null) {
	                throw new RecordNotFoundException("trainerId is required");
	            }

	            if (request.getProgramAnnouncementId() == null) {
	                throw new RecordNotFoundException("programAnnouncementId is required");
	            }
	        }

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
	            return ResponseEntity.status(HttpStatus.CONFLICT)
	                    .body(Map.of(
	                            "status", 409,
	                            "message", "Your institute has already applied for this program."));
	        }

	        // Ignore duplicate trainer ids in the same request
	        Set<Integer> seenTrainers = new HashSet<>();
	        requests = requests.stream()
	                .filter(r -> seenTrainers.add(r.getTrainerId()))
	                .collect(Collectors.toList());

	        // Common workflow data

	        String applicationNo = firstRequest.getApplicationNo();
	        Integer serviceId = firstRequest.getServiceId();
	        Integer assignedRoleId = firstRequest.getAssignedRoleId();
	        String userId = firstRequest.getUserId();
	        Integer statusId = firstRequest.getStatusId();
	        String remarks = firstRequest.getRemarks();

	        Integer locationId = 14;


	        // Save trainer applications
	        List<TOTProgramTrainerApplied> trainerList = requests.stream()
	                .map(request -> TOTProgramTrainerApplied.builder()
	                        .applicationNo(request.getApplicationNo())
	                        .instituteId(request.getInstituteId())
	                        .trainerId(request.getTrainerId())
	                        .programAnnouncementId(request.getProgramAnnouncementId())
	                        .createdBy(request.getCreatedBy())
	                        .createdAt(new Date())
	                        .build())
	                .collect(Collectors.toList());

	        totProgramTrainerAppliedRepository.saveAll(trainerList);


	        // Get initiated task status
	        Integer taskStatusId = dropdownManagementRepository.findChildById(18)
	                .orElseThrow(() -> new RecordNotFoundException(
	                        "Initiated status not found"));


	        // Create workflow
	        WorkFlowList workflow = workTaskFlowService.createWorkflow(
	                applicationNo,
	                null, // institute name if required
	                serviceId,
	                statusId,
	                assignedRoleId,
	                remarks
	        );


	        // Create task flow
	        workTaskFlowService.createTaskFlow(
	                applicationNo,
	                taskStatusId,
	                assignedRoleId,
	                userId,
	                workflow,
	                remarks,
	                locationId
	        );


	        return ResponseEntity.status(HttpStatus.CREATED)
	                .body(Map.of(
	                        "status", 201,
	                        "message", "Trainers applied successfully.",
	                        "count", trainerList.size()
	                ));


	    } catch (RecordNotFoundException e) {

	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(Map.of(
	                        "status", 404,
	                        "message", e.getMessage()
	                ));

	    } catch (Exception e) {
	        log.error("Failed to apply trainers", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of(
	                        "status", 500,
	                        "message", "Failed to apply trainers."
	                ));
	    }
	}

	@Override
	public List<ObjectNode> getTrainerApplied(String applicationNo) {
		return objectTojson._toJson(totAnnouncementRepository.getTrainerApplied(applicationNo));
	}

	@Override
	@Transactional
	public ResponseEntity<?> saveTrainerSelection(TOTTrainerSelectionDto request) {
		try {
			if (request == null || request.getApplicationNo() == null || request.getApplicationNo().isBlank()) {
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
					"status", 200,
					"message", "Trainer selection saved successfully.",
					"selectedCount", selectedIds.size()));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", 400, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to save trainer selection", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to save trainer selection."));
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

	@Override
	@Transactional
	public ResponseEntity<?> saveModuleMarks(TOTModuleMarksDto request) {
		try {
			if (request == null || request.getApplicationNo() == null || request.getApplicationNo().isBlank()) {
				throw new RecordNotFoundException("applicationNo is required");
			}
			if (request.getTrainerId() == null) {
				throw new RecordNotFoundException("trainerId is required");
			}
			if (request.getModuleId() == null) {
				throw new RecordNotFoundException("moduleId is required");
			}

			// Trainer must be applied & selected for this application
			TOTProgramTrainerApplied applied = totProgramTrainerAppliedRepository
					.findByApplicationNo(request.getApplicationNo()).stream()
					.filter(a -> request.getTrainerId().equals(a.getTrainerId()))
					.findFirst()
					.orElseThrow(() -> new RecordNotFoundException("Trainer has not applied for this program"));
			if (!Integer.valueOf(1).equals(applied.getIsSelected())) {
				throw new RecordNotFoundException("Trainer is not selected for this program");
			}

			TOTModule module = totModuleRepository.findById(request.getModuleId())
					.orElseThrow(() -> new RecordNotFoundException("Module not found: " + request.getModuleId()));

			List<TOTCourseContent> moduleContents = module.getTotCourseContent() == null
					? new ArrayList<>() : module.getTotCourseContent();
			if (moduleContents.isEmpty()) {
				throw new RecordNotFoundException("Module has no course contents");
			}

			Map<Long, BigDecimal> submitted = new HashMap<>();
			if (request.getContents() != null) {
				for (TOTModuleMarksDto.ContentMark cm : request.getContents()) {
					if (cm != null && cm.getContentId() != null) {
						submitted.put(cm.getContentId(), cm.getMarks());
					}
				}
			}

			Date now = new Date();
			BigDecimal moduleTotal = BigDecimal.ZERO;
			BigDecimal moduleMax = BigDecimal.ZERO;
			List<TOTTrainerContentMarks> toSave = new ArrayList<>();

			// Full marks are taken from the DB (not trusted from the client)
			for (TOTCourseContent content : moduleContents) {
				BigDecimal fullMark = parseNumber(content.getFullMark());
				if (fullMark == null) fullMark = BigDecimal.ZERO;
				BigDecimal marks = submitted.get(content.getId());

				if (marks == null) {
					throw new RecordNotFoundException("Marks missing for content: " + content.getCourseContent());
				}
				if (marks.compareTo(BigDecimal.ZERO) < 0 || marks.compareTo(fullMark) > 0) {
					throw new RecordNotFoundException("Marks for '" + content.getCourseContent()
							+ "' must be between 0 and " + fullMark.stripTrailingZeros().toPlainString());
				}

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
				row.setAppliedId(applied.getId());
				row.setModuleId(module.getId());
				row.setFullMark(fullMark);
				row.setMarksObtained(marks);
				toSave.add(row);

				moduleTotal = moduleTotal.add(marks);
				moduleMax = moduleMax.add(fullMark);
			}
			totTrainerContentMarksRepository.saveAll(toSave);

			// Weighted score = (moduleTotal / moduleMax) * weightage
			BigDecimal weightage = parseNumber(module.getWeightage());
			BigDecimal weightedScore = null;
			if (weightage != null && moduleMax.compareTo(BigDecimal.ZERO) > 0) {
				weightedScore = moduleTotal.multiply(weightage)
						.divide(moduleMax, 2, RoundingMode.HALF_UP);
			}

			TOTTrainerModuleResult result = totTrainerModuleResultRepository
					.findByApplicationNoAndTrainerIdAndModuleId(
							request.getApplicationNo(), request.getTrainerId(), module.getId())
					.orElse(null);
			if (result == null) {
				result = TOTTrainerModuleResult.builder()
						.applicationNo(request.getApplicationNo())
						.trainerId(request.getTrainerId())
						.moduleId(module.getId())
						.createdBy(request.getCreatedBy())
						.createdAt(now)
						.build();
			} else {
				result.setUpdatedBy(request.getCreatedBy());
				result.setUpdatedAt(now);
			}
			result.setAppliedId(applied.getId());
			result.setModuleTotal(moduleTotal);
			result.setModuleMax(moduleMax);
			result.setWeightage(weightage);
			result.setWeightedScore(weightedScore);
			totTrainerModuleResultRepository.save(result);

			Map<String, Object> body = new LinkedHashMap<>();
			body.put("status", 200);
			body.put("message", "Module marks saved successfully.");
			body.put("moduleTotal", moduleTotal);
			body.put("moduleMax", moduleMax);
			body.put("weightage", weightage);
			body.put("weightedScore", weightedScore);
			return ResponseEntity.ok(body);

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", 400, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to save module marks", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to save module marks."));
		}
	}

	@Override
	public ResponseEntity<?> getTrainerMarks(String applicationNo) {
		List<Map<String, Object>> contents = totTrainerContentMarksRepository.findByApplicationNo(applicationNo)
				.stream()
				.map(m -> {
					Map<String, Object> row = new LinkedHashMap<>();
					row.put("trainerId", m.getTrainerId());
					row.put("moduleId", m.getModuleId());
					row.put("contentId", m.getCourseContentId());
					row.put("fullMark", m.getFullMark());
					row.put("marks", m.getMarksObtained());
					return row;
				})
				.collect(Collectors.toList());

		List<Map<String, Object>> modules = totTrainerModuleResultRepository.findByApplicationNo(applicationNo)
				.stream()
				.map(r -> {
					Map<String, Object> row = new LinkedHashMap<>();
					row.put("trainerId", r.getTrainerId());
					row.put("moduleId", r.getModuleId());
					row.put("moduleTotal", r.getModuleTotal());
					row.put("moduleMax", r.getModuleMax());
					row.put("weightage", r.getWeightage());
					row.put("weightedScore", r.getWeightedScore());
					return row;
				})
				.collect(Collectors.toList());

		return ResponseEntity.ok(Map.of("contents", contents, "modules", modules));
	}


	// ---------------- Mark sheet / certificate ----------------

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
	private String financialYear(java.time.LocalDate date) {
		java.time.LocalDate d = date == null ? java.time.LocalDate.now() : date;
		int start = d.getMonthValue() >= 7 ? d.getYear() : d.getYear() - 1;
		return start + "-" + String.format("%02d", (start + 1) % 100);
	}

	@Override
	@Transactional
	public ResponseEntity<?> generateCertificates(TOTCertificateRequestDto request) {
		try {
			if (request == null || request.getApplicationNo() == null || request.getApplicationNo().isBlank()) {
				throw new RecordNotFoundException("applicationNo is required");
			}
			String applicationNo = request.getApplicationNo();

			TOTAnnouncement announcement = totAnnouncementRepository.findFirstByApplicationNo(applicationNo)
					.orElseThrow(() -> new RecordNotFoundException("Announcement not found: " + applicationNo));
			TOTProgram program = announcement.getTotProgram();
			if (program == null || program.getModules() == null || program.getModules().isEmpty()) {
				throw new RecordNotFoundException("Program has no modules configured");
			}

			// Only modules that have course contents count towards the result
			List<TOTModule> modules = program.getModules().stream()
					.filter(m -> m.getTotCourseContent() != null && !m.getTotCourseContent().isEmpty())
					.collect(Collectors.toList());
			if (modules.isEmpty()) {
				throw new RecordNotFoundException("Program modules have no course contents");
			}

			List<TOTProgramTrainerApplied> appliedList = totProgramTrainerAppliedRepository
					.findByApplicationNo(applicationNo).stream()
					.sorted((a, b) -> Long.compare(a.getId(), b.getId()))
					.collect(Collectors.toList());

			Set<Integer> requested = request.getTrainerIds() == null ? new HashSet<>()
					: new HashSet<>(request.getTrainerIds());

			List<TOTProgramTrainerApplied> targets = appliedList.stream()
					.filter(a -> Integer.valueOf(1).equals(a.getIsSelected()))
					.filter(a -> requested.isEmpty() || requested.contains(a.getTrainerId()))
					.collect(Collectors.toList());
			if (targets.isEmpty()) {
				throw new RecordNotFoundException("No selected trainers to generate certificates for");
			}

			Map<String, TOTTrainerModuleResult> resultMap = new HashMap<>();
			for (TOTTrainerModuleResult r : totTrainerModuleResultRepository.findByApplicationNo(applicationNo)) {
				resultMap.put(r.getTrainerId() + "-" + r.getModuleId(), r);
			}

			// Running serial for certificate numbers (keeps already issued numbers)
			int serial = (int) appliedList.stream().filter(a -> a.getCertificateNo() != null).count();
			String prefix = (program.getProgramCode() == null ? "TOT" : program.getProgramCode()) + "/"
					+ financialYear(announcement.getProgramStartDate())
					+ "/";

			Date now = new Date();
			List<Map<String, Object>> out = new ArrayList<>();

			for (TOTProgramTrainerApplied applied : targets) {
				BigDecimal weightedSum = BigDecimal.ZERO;
				BigDecimal weightSum = BigDecimal.ZERO;
				BigDecimal percentSum = BigDecimal.ZERO;
				boolean allWeighted = true;
				List<Map<String, Object>> moduleOut = new ArrayList<>();

				for (TOTModule m : modules) {
					TOTTrainerModuleResult r = resultMap.get(applied.getTrainerId() + "-" + m.getId());
					if (r == null || r.getModuleMax() == null || r.getModuleMax().signum() == 0) {
						throw new RecordNotFoundException("Marks for module '" + m.getModuleName()
								+ "' are not saved for trainer id " + applied.getTrainerId());
					}
					BigDecimal modPercent = r.getModuleTotal().multiply(new BigDecimal("100"))
							.divide(r.getModuleMax(), 2, RoundingMode.HALF_UP);
					percentSum = percentSum.add(modPercent);
					if (r.getWeightage() == null || r.getWeightedScore() == null) {
						allWeighted = false;
					} else {
						weightedSum = weightedSum.add(r.getWeightedScore());
						weightSum = weightSum.add(r.getWeightage());
					}
					Map<String, Object> mo = new LinkedHashMap<>();
					mo.put("moduleId", m.getId());
					mo.put("percentage", modPercent);
					mo.put("grade", gradeFor(modPercent));
					moduleOut.add(mo);
				}

				// Weighted overall when every module has weightage, otherwise simple average
				BigDecimal overall = (allWeighted && weightSum.signum() > 0)
						? weightedSum.multiply(new BigDecimal("100")).divide(weightSum, 2, RoundingMode.HALF_UP)
						: percentSum.divide(new BigDecimal(modules.size()), 2, RoundingMode.HALF_UP);

				applied.setOverallPercentage(overall);
				applied.setGrade(gradeFor(overall));
				applied.setResult(resultFor(overall));
				if (applied.getCertificateNo() == null) {
					serial++;
					applied.setCertificateNo(prefix + serial);
				}
				applied.setCertificateGeneratedBy(request.getGeneratedBy());
				applied.setCertificateGeneratedAt(now);

				Map<String, Object> row = new LinkedHashMap<>();
				row.put("trainerId", applied.getTrainerId());
				row.put("overallPercentage", overall);
				row.put("grade", applied.getGrade());
				row.put("result", applied.getResult());
				row.put("certificateNo", applied.getCertificateNo());
				row.put("generatedAt", now);
				row.put("modules", moduleOut);
				out.add(row);
			}
			totProgramTrainerAppliedRepository.saveAll(targets);

			Map<String, Object> body = new LinkedHashMap<>();
			body.put("status", 200);
			body.put("message", "Certificates generated successfully.");
			body.put("trainers", out);
			return ResponseEntity.ok(body);

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", 400, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to generate certificates", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to generate certificates."));
		}
	}


	@Override
	@Transactional
	public ResponseEntity<?> completeApplication(TOTCompleteApplicationDto request) {
		try {
			if (request == null || request.getApplicationNo() == null || request.getApplicationNo().isBlank()) {
				throw new RecordNotFoundException("applicationNo is required");
			}
			String applicationNo = request.getApplicationNo();

			// Every selected trainer must have a generated mark sheet
			List<TOTProgramTrainerApplied> selected = totProgramTrainerAppliedRepository
					.findByApplicationNo(applicationNo).stream()
					.filter(a -> Integer.valueOf(1).equals(a.getIsSelected()))
					.collect(Collectors.toList());
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

			String remarks = request.getRemarks() != null && !request.getRemarks().isBlank()
					? request.getRemarks()
					: "TOT application completed. Mark sheets generated.";
			Date now = new Date();

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

			return ResponseEntity.ok(Map.of(
					"status", 200,
					"applicationNo", applicationNo,
					"message", "Application completed successfully."));

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(Map.of("status", 400, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to complete TOT application", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to complete application."));
		}
	}


	@Override
	public ResponseEntity<?> getInstituteApplied(Integer instituteId) {
		// One entry per application no this institute has applied to
		Map<String, Map<String, Object>> grouped = new LinkedHashMap<>();
		for (TOTProgramTrainerApplied a : totProgramTrainerAppliedRepository.findByInstituteId(instituteId)) {
			Map<String, Object> entry = grouped.computeIfAbsent(a.getApplicationNo(), k -> {
				Map<String, Object> m = new LinkedHashMap<>();
				m.put("applicationNo", a.getApplicationNo());
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


	// ---------------- Certificate lookup (read only) ----------------

	@Override
	public List<ObjectNode> getCertifiedPrograms(Integer instituteId) {
		return objectTojson._toJson(totProgramTrainerAppliedRepository.getCertifiedPrograms(instituteId));
	}

	@Override
	@Transactional(readOnly = true)
	public ResponseEntity<?> getCertificates(String applicationNo, Integer instituteId) {
		try {
			if (applicationNo == null || applicationNo.isBlank()) {
				throw new RecordNotFoundException("Application number is required");
			}
			String appNo = applicationNo.trim();

			TOTAnnouncement announcement = totAnnouncementRepository.findFirstByApplicationNo(appNo)
					.orElseThrow(() -> new RecordNotFoundException("No TOT programme found for application no " + appNo));
			TOTProgram program = announcement.getTotProgram();

			// ---- programme + modules (only modules with course contents) ----
			List<Map<String, Object>> modules = new ArrayList<>();
			if (program != null && program.getModules() != null) {
				program.getModules().stream()
						.filter(m -> m.getTotCourseContent() != null && !m.getTotCourseContent().isEmpty())
						.sorted((a, b) -> {
							int oa = a.getModuleOrder() == null ? Integer.MAX_VALUE : a.getModuleOrder();
							int ob = b.getModuleOrder() == null ? Integer.MAX_VALUE : b.getModuleOrder();
							return oa != ob ? Integer.compare(oa, ob) : Long.compare(a.getId(), b.getId());
						})
						.forEach(m -> {
							Map<String, Object> mm = new LinkedHashMap<>();
							mm.put("id", m.getId());
							mm.put("moduleCode", m.getModuleCode());
							mm.put("moduleName", m.getModuleName());
							mm.put("duration", m.getDuration());
							mm.put("weightage", m.getWeightage());
							List<Map<String, Object>> contents = new ArrayList<>();
							for (TOTCourseContent c : m.getTotCourseContent()) {
								Map<String, Object> cc = new LinkedHashMap<>();
								cc.put("id", c.getId());
								cc.put("courseContent", c.getCourseContent());
								cc.put("fullMark", c.getFullMark());
								contents.add(cc);
							}
							mm.put("contents", contents);
							modules.add(mm);
						});
			}

			Map<String, Object> programOut = new LinkedHashMap<>();
			programOut.put("applicationNo", appNo);
			programOut.put("programName", program != null ? program.getProgramName() : null);
			programOut.put("programCode", program != null ? program.getProgramCode() : null);
			programOut.put("venue", announcement.getVenue());
			programOut.put("programStartDate", announcement.getProgramStartDate());
			programOut.put("programEndDate", announcement.getProgramEndDate());
			programOut.put("modules", modules);

			// ---- marks and module results for this application ----
			Map<Integer, Map<Long, BigDecimal>> marksByTrainer = new HashMap<>();
			for (TOTTrainerContentMarks cm : totTrainerContentMarksRepository.findByApplicationNo(appNo)) {
				marksByTrainer.computeIfAbsent(cm.getTrainerId(), k -> new HashMap<>())
						.put(cm.getCourseContentId(), cm.getMarksObtained());
			}
			Map<Integer, List<Map<String, Object>>> resultsByTrainer = new HashMap<>();
			for (TOTTrainerModuleResult r : totTrainerModuleResultRepository.findByApplicationNo(appNo)) {
				BigDecimal pct = (r.getModuleMax() == null || r.getModuleMax().signum() == 0) ? null
						: r.getModuleTotal().multiply(new BigDecimal("100"))
								.divide(r.getModuleMax(), 2, RoundingMode.HALF_UP);
				Map<String, Object> mr = new LinkedHashMap<>();
				mr.put("moduleId", r.getModuleId());
				mr.put("percentage", pct);
				mr.put("grade", gradeFor(pct));
				mr.put("weightage", r.getWeightage());
				mr.put("weightedScore", r.getWeightedScore());
				resultsByTrainer.computeIfAbsent(r.getTrainerId(), k -> new ArrayList<>()).add(mr);
			}

			// ---- certified trainers ----
			List<Map<String, Object>> trainers = new ArrayList<>();
			for (jakarta.persistence.Tuple t : totProgramTrainerAppliedRepository.getCertifiedTrainers(appNo,
					instituteId)) {
				Integer trainerId = t.get("trainer_id") == null ? null
						: ((Number) t.get("trainer_id")).intValue();
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("trainerId", trainerId);
				row.put("instituteId", t.get("institute_id"));
				row.put("instituteName", t.get("institute_name"));
				row.put("name", t.get("name"));
				row.put("cid", t.get("cid"));
				row.put("overallPercentage", t.get("overall_percentage"));
				row.put("grade", t.get("grade"));
				row.put("result", t.get("result"));
				row.put("certificateNo", t.get("certificate_no"));
				Object gen = t.get("certificate_generated_at");
				row.put("generatedAt", gen == null ? null : gen.toString());
				Map<String, Object> marks = new LinkedHashMap<>();
				marksByTrainer.getOrDefault(trainerId, Map.of()).forEach((k, v) -> marks.put(String.valueOf(k), v));
				row.put("marks", marks);
				row.put("modules", resultsByTrainer.getOrDefault(trainerId, List.of()));
				trainers.add(row);
			}

			Map<String, Object> body = new LinkedHashMap<>();
			body.put("program", programOut);
			body.put("trainers", trainers);
			return ResponseEntity.ok(body);

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("status", 404, "message", e.getMessage()));
		} catch (Exception e) {
			log.error("Failed to load TOT certificates", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", 500, "message", "Failed to load certificates."));
		}
	}

}
