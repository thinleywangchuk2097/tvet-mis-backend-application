package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramAnnouncementDto;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramTrainerAppliedDto;
import com.moesd.tvet.mis.backend.application.dto.TotModuleDto;
import com.moesd.tvet.mis.backend.application.dto.TotProgramDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.TOTAnnouncement;
import com.moesd.tvet.mis.backend.application.model.TOTModule;
import com.moesd.tvet.mis.backend.application.model.TOTProgram;
import com.moesd.tvet.mis.backend.application.model.TOTProgramTrainerApplied;
import com.moesd.tvet.mis.backend.application.model.WorkFlowList;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTAnnouncementRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTModuleRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTProgramRepository;
import com.moesd.tvet.mis.backend.application.repository.TOTProgramTrainerAppliedRepository;
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
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ObjectToJson objectTojson;
	private final WorkTaskFlowService workTaskFlowService;
	private final DropdownManagementRepository dropdownManagementRepository;

	public ResponseEntity<?> submitTOTProgram(TotProgramDto request) {
		try {
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

						TOTModule module = totModuleRepository.findByModuleCode(moduleDto.getModuleCode())
								.orElse(new TOTModule());

						// Set values (works for both update and new creation)
						module.setModuleCode(moduleDto.getModuleCode());
						module.setModuleName(moduleDto.getModuleName());
						module.setDescription(moduleDto.getDescription());
						module.setDuration(moduleDto.getDuration());
						module.setLearningOutcomes(moduleDto.getLearningOutcomes());
						module.setPrerequisites(moduleDto.getPrerequisites());
						module.setModuleOrder(moduleDto.getOrder());
						module.setTotProgram(program);

						modules.add(module);
					}

					totModuleRepository.saveAll(modules);
					program.setModules(modules);
				}

				return ResponseEntity.ok(Map.of("status", 200, "message", "TOT Program updated successfully."));

			} else {
				// Build entity
				TOTProgram program = TOTProgram.builder().programName(request.getProgramName())
						.programCode(request.getProgramCode()).statusId(request.getStatusId())
						.programTypeId(request.getProgramTypeId()).description(request.getDescription())
						.createdBy(request.getCreatedBy()).createdAt(new java.util.Date()).build();

				// Build program modules
				if (request.getModules() != null && !request.getModules().isEmpty()) {
					List<TOTModule> modules = request.getModules().stream()
							.map(moduleDto -> TOTModule.builder().moduleCode(moduleDto.getModuleCode())
									.moduleName(moduleDto.getModuleName()).description(moduleDto.getDescription())
									.duration(moduleDto.getDuration()).learningOutcomes(moduleDto.getLearningOutcomes())
									.prerequisites(moduleDto.getPrerequisites()).totProgram(program).statusId(122)
									.moduleOrder(moduleDto.getOrder()).build())
							.collect(Collectors.toList());
					program.setModules(modules);
				}

				// Save (cascade handles children)
				totProgramRepository.save(program);

				return ResponseEntity.ok(Map.of("status", 200, "message", "TOT Program added successfully."));
			}

		} catch (RecordNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("status", 404, "message", e.getMessage()));
		} catch (Exception e) {
		    log.error("Failed to submit TOT Program", e);
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(Map.of("status", 500, "message", "Failed to submit TOT Program"));
		}
	}

	@Override
	public List<ObjectNode> getToTPrograms() {
		List<Tuple> resultList = totProgramRepository.getToTPrograms();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
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
		List<Tuple> resultList = totProgramRepository.getToTProgramsAnnouncement();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
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

	        // Common workflow data
	        TOTProgramTrainerAppliedDto firstRequest = requests.get(0);

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

}
