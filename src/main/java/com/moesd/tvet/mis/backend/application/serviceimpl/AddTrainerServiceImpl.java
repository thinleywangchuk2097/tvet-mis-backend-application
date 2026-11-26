package com.moesd.tvet.mis.backend.application.serviceimpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AddTrainerDto;
import com.moesd.tvet.mis.backend.application.model.AddTrainer;
import com.moesd.tvet.mis.backend.application.model.TrainerCourse;
import com.moesd.tvet.mis.backend.application.repository.AddTrainerRepository;
import com.moesd.tvet.mis.backend.application.repository.InstituteRegistrationDetailsRepository;
import com.moesd.tvet.mis.backend.application.repository.TrainerCourseRepository;
import com.moesd.tvet.mis.backend.application.service.AddTrainerService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddTrainerServiceImpl implements AddTrainerService {

    // response body keys and status values
    private static final String MESSAGE = "message";
    private static final String STATUS = "status";
    private static final String DATA = "data";
    private static final String SUCCESS = "SUCCESS";
    private static final String ERROR = "ERROR";

    private final AddTrainerRepository addTrainerRepository;
    private final TrainerCourseRepository trainerCourseRepository;
    private final ObjectToJson objectTojson;
    private final InstituteRegistrationDetailsRepository instituteRegistrationDetailsRepository;

    private static final int STATUS_ACTIVE_RECORD = 1; // 0 = deleted

    @Override
    @Transactional
    public ResponseEntity<?> submitTrainer(AddTrainerDto request) {
        try {
            log.info("Submitting new trainer: {}", request.getName());

            // Already in this institute, or still active at another institute -> not allowed
            String conflict = checkTrainerConflict(request.getCitizenId(), request.getWorkPermitNo(),
                request.getInstituteId(), null);
            if (conflict != null) {
                log.warn("Trainer not added: {}", conflict);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(body(conflict, ERROR, null));
            }

            // Previously deleted (soft-deleted) record -> reactivate it with the new details
            Optional<AddTrainer> deleted = findMatchingTrainers(request).stream()
                .filter(t -> !Integer.valueOf(STATUS_ACTIVE_RECORD).equals(t.getStatusId()))
                .findFirst();
            if (deleted.isPresent()) {
                return reactivateTrainer(request, deleted.get());
            }

            // Create new trainer entity
            AddTrainer trainer = AddTrainer.builder()
                .citizenId(request.getCitizenId())
                .workPermitNo(request.getWorkPermitNo())
                .name(request.getName())
                .specialization(request.getSpecialization())
                .genderId(request.getGenderId())
                .qualificationId(request.getQualificationId())
                .workExperience(request.getWorkExperience())
                .employmentTypeId(request.getEmploymentTypeId())
                .email(request.getEmail())
                .mobileNo(request.getMobileNo())
                .instituteId(request.getInstituteId())
                .joiningDate(request.getJoiningDate())
                .statusId(request.getStatusId() != null ? request.getStatusId() : 1)
                .active(activeFlag(request.getActive(), "Y"))
                .description(request.getDescription())
                .createdBy(request.getCreatedBy())
                .createdAt(new Date())
                .build();

            // Save trainer to database
            AddTrainer savedTrainer = addTrainerRepository.save(trainer);
            log.info("Trainer saved with ID: {}", savedTrainer.getId());

            // Save courses if present
            if (request.getCourses() != null && !request.getCourses().isEmpty()) {
                List<TrainerCourse> trainerCourses = buildCourses(request, savedTrainer);
                trainerCourseRepository.saveAll(trainerCourses);
                log.info("Saved {} courses for trainer ID: {}", trainerCourses.size(), savedTrainer.getId());
            }

            log.info("Trainer submitted successfully with ID: {}", savedTrainer.getId());
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(body("Trainer submitted successfully", SUCCESS, savedTrainer));

        } catch (Exception e) {
            log.error("Error submitting trainer: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body("Failed to submit trainer: " + e.getMessage(), ERROR, null));
        }
    }

    /** Trainer records (any institute, active or soft-deleted) with the same CID or work permit no. */
    private List<AddTrainer> findMatchingTrainers(AddTrainerDto request) {
        return findMatchingTrainers(request.getCitizenId(), request.getWorkPermitNo());
    }

    private List<AddTrainer> findMatchingTrainers(String citizenId, String workPermitNo) {
        List<AddTrainer> matches = new ArrayList<>();
        if (hasText(citizenId)) {
            matches.addAll(addTrainerRepository.findAllByCitizenId(citizenId.trim()));
        }
        if (hasText(workPermitNo)) {
            addTrainerRepository.findAllByWorkPermitNo(workPermitNo.trim()).stream()
                .filter(t -> matches.stream().noneMatch(m -> m.getId().equals(t.getId())))
                .forEach(matches::add);
        }
        return matches;
    }

    /**
     * Not allowed when the same CID / work permit no is already registered (not deleted) in this institute,
     * or is active (is_active not 'N') at another institute. Inactive or deleted records elsewhere are fine.
     */
    @Override
    public Map<String, Object> findInstituteTrainer(String citizenId, Integer instituteId) {
        String cid = citizenId == null ? "" : citizenId.trim();
        List<AddTrainer> registered = addTrainerRepository.findAllByCitizenId(cid).stream()
            .filter(t -> Integer.valueOf(STATUS_ACTIVE_RECORD).equals(t.getStatusId()))
            .filter(t -> instituteId != null && instituteId.equals(t.getInstituteId()))
            .toList();
        Optional<AddTrainer> active = registered.stream().filter(t -> !"N".equals(t.getActive())).findFirst();

        Map<String, Object> result = new HashMap<>();
        result.put("found", active.isPresent());
        if (active.isPresent()) {
            AddTrainer t = active.get();
            Map<String, Object> data = new HashMap<>();
            data.put("name", t.getName());
            data.put("genderId", t.getGenderId());
            data.put("qualificationId", t.getQualificationId());
            data.put("workExperience", t.getWorkExperience());
            result.put(DATA, data);
        } else if (!registered.isEmpty()) {
            result.put(MESSAGE, "Trainer with CID " + cid + " is marked Inactive in your institute.");
        } else {
            result.put(MESSAGE, "CID " + cid + " is not registered as a trainer in your institute. Add the trainer first.");
        }
        return result;
    }

    @Override
    public String checkTrainerConflict(String citizenId, String workPermitNo, Integer instituteId, Long excludeId) {
        String idLabel = hasText(citizenId) ? "Citizen ID " + citizenId.trim() : "Work Permit No " + workPermitNo;
        List<AddTrainer> current = findMatchingTrainers(citizenId, workPermitNo).stream()
            .filter(t -> Integer.valueOf(STATUS_ACTIVE_RECORD).equals(t.getStatusId()))
            .filter(t -> excludeId == null || !excludeId.equals(t.getId()))
            .toList();

        if (current.stream().anyMatch(t -> instituteId != null && instituteId.equals(t.getInstituteId()))) {
            return "Trainer with " + idLabel + " is already registered in your institute.";
        }
        return current.stream()
            .filter(t -> !"N".equalsIgnoreCase(t.getActive()))
            .findFirst()
            .map(t -> "Trainer with " + idLabel + " is currently active at " + instituteName(t.getInstituteId())
                + ". The trainer must be made inactive there before being added to your institute.")
            .orElse(null);
    }

    private String instituteName(Integer instituteId) {
        if (instituteId == null) {
            return "another institute";
        }
        return instituteRegistrationDetailsRepository.findById(instituteId.longValue())
            .map(r -> r.getProposedInstituteName())
            .filter(AddTrainerServiceImpl::hasText)
            .orElse("another institute");
    }

    /** Restores a soft-deleted trainer with the newly submitted details. */
    private ResponseEntity<?> reactivateTrainer(AddTrainerDto request, AddTrainer deleted) {
        log.info("Reactivating soft-deleted trainer id {} for {}", deleted.getId(), request.getName());
        request.setId(deleted.getId());
        if (request.getUpdatedBy() == null) {
            request.setUpdatedBy(request.getCreatedBy());
        }
        ResponseEntity<?> reactivated = updateTrainer(request);
        if (!reactivated.getStatusCode().is2xxSuccessful()) {
            return reactivated;
        }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(body("Trainer added successfully (previously deleted record restored)", SUCCESS, null));
    }

    private static List<TrainerCourse> buildCourses(AddTrainerDto request, AddTrainer trainer) {
        return request.getCourses().stream()
            .map(courseDto -> TrainerCourse.builder()
                .courseId(courseDto.getCourseId())
                .trainer(trainer)
                .build())
            .toList();
    }

    /** "Y" or "N" from the request; anything else keeps the current value (new trainers default to Y). */
    private static String activeFlag(String requested, String current) {
        if ("Y".equalsIgnoreCase(requested) || "N".equalsIgnoreCase(requested)) {
            return requested.toUpperCase();
        }
        return current != null ? current : "Y";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isEmpty();
    }

    // HashMap (not Map.of) because data can be null
    private static Map<String, Object> body(String message, String status, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put(MESSAGE, message);
        response.put(STATUS, status);
        response.put(DATA, data);
        return response;
    }

    @Override
    public List<ObjectNode> getAllTrainer(Integer instituteId) {
        try {
            log.info("Fetching all trainers for institute ID: {}", instituteId);

            List<Tuple> resultList = addTrainerRepository.getAllActiveTrainers(instituteId);
            List<ObjectNode> trainerList = objectTojson._toJson(resultList);

            log.info("Found {} trainers for institute ID: {}", trainerList.size(), instituteId);
            return trainerList;

        } catch (Exception e) {
            log.error("Error fetching trainers: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateTrainer(AddTrainerDto request) {
        try {
            log.info("Updating trainer with ID: {}", request.getId());

            // Check if trainer exists
            Optional<AddTrainer> existingTrainerOpt = addTrainerRepository.findById(request.getId());
            if (existingTrainerOpt.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(body("Trainer not found with ID: " + request.getId(), ERROR, null));
            }

            AddTrainer existingTrainer = existingTrainerOpt.get();

            String conflict = checkTrainerConflict(request.getCitizenId(), request.getWorkPermitNo(),
                request.getInstituteId(), request.getId());
            if (conflict != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(body(conflict, ERROR, null));
            }

            // Update trainer entity
            existingTrainer.setCitizenId(request.getCitizenId());
            existingTrainer.setWorkPermitNo(request.getWorkPermitNo());
            existingTrainer.setSpecialization(request.getSpecialization());
            existingTrainer.setName(request.getName());
            existingTrainer.setGenderId(request.getGenderId());
            existingTrainer.setQualificationId(request.getQualificationId());
            existingTrainer.setWorkExperience(request.getWorkExperience());
            existingTrainer.setEmploymentTypeId(request.getEmploymentTypeId());
            existingTrainer.setEmail(request.getEmail());
            existingTrainer.setMobileNo(request.getMobileNo());
            existingTrainer.setInstituteId(request.getInstituteId());
            existingTrainer.setJoiningDate(request.getJoiningDate());
            existingTrainer.setStatusId(1);
            existingTrainer.setActive(activeFlag(request.getActive(), existingTrainer.getActive()));
            existingTrainer.setDescription(request.getDescription());
            existingTrainer.setUpdatedBy(request.getUpdatedBy());
            existingTrainer.setUpdatedAt(new Date());

            // Save updated trainer
            AddTrainer updatedTrainer = addTrainerRepository.save(existingTrainer);
            log.info("Trainer updated with ID: {}", updatedTrainer.getId());

            // Update courses - delete existing and add new ones
            if (request.getCourses() != null) {
                trainerCourseRepository.deleteByTrainerId(updatedTrainer.getId());

                if (!request.getCourses().isEmpty()) {
                    List<TrainerCourse> trainerCourses = buildCourses(request, updatedTrainer);
                    trainerCourseRepository.saveAll(trainerCourses);
                    log.info("Updated {} courses for trainer ID: {}", trainerCourses.size(), updatedTrainer.getId());
                }
            }

            log.info("Trainer updated successfully with ID: {}", updatedTrainer.getId());
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(body("Trainer updated successfully", SUCCESS, updatedTrainer));

        } catch (Exception e) {
            log.error("Error updating trainer: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body("Failed to update trainer: " + e.getMessage(), ERROR, null));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> softDeleteTrainer(Long trainerId) {
        try {
            log.info("Soft deleting trainer with ID: {}", trainerId);

            // Check if trainer exists
            Optional<AddTrainer> existingTrainerOpt = addTrainerRepository.findById(trainerId);
            if (existingTrainerOpt.isEmpty()) {
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(body("Trainer not found with ID: " + trainerId, ERROR, null));
            }

            // Soft delete by setting statusId to 0 (Inactive)
            AddTrainer trainer = existingTrainerOpt.get();
            trainer.setStatusId(0); // 0 for inactive/deleted
            trainer.setUpdatedAt(new Date());

            // Save to database
            AddTrainer deletedTrainer = addTrainerRepository.save(trainer);

            log.info("Trainer soft deleted successfully with ID: {}", trainerId);
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(body("Trainer deleted successfully", SUCCESS, deletedTrainer));

        } catch (Exception e) {
            log.error("Error deleting trainer: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body("Failed to delete trainer: " + e.getMessage(), ERROR, null));
        }
    }
}
