package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AddTrainerDto;
import com.moesd.tvet.mis.backend.application.model.AddTrainer;
import com.moesd.tvet.mis.backend.application.model.TrainerCourse;
import com.moesd.tvet.mis.backend.application.repository.AddTrainerRepository;
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
    
    private final AddTrainerRepository addTrainerRepository;
    private final TrainerCourseRepository trainerCourseRepository;
    private final ObjectToJson objectTojson;
    
    @Override
    @Transactional
    public ResponseEntity<?> submitTrainer(AddTrainerDto request) {
        try {
            log.info("Submitting new trainer: {}", request.getName());
            
            // Check if citizenId already exists
            if (request.getCitizenId() != null && !request.getCitizenId().isEmpty()) {
                Optional<AddTrainer> existingTrainer = addTrainerRepository.findAll()
                    .stream()
                    .filter(t -> request.getCitizenId().equals(t.getCitizenId()))
                    .findFirst();
                
                if (existingTrainer.isPresent()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Trainer with Citizen ID " + request.getCitizenId() + " already exists");
                    errorResponse.put("status", "ERROR");
                    errorResponse.put("data", null);
                    
                    return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(errorResponse);
                }
            }
            
            // Check if workPermitNo already exists
            if (request.getWorkPermitNo() != null && !request.getWorkPermitNo().isEmpty()) {
                Optional<AddTrainer> existingTrainer = addTrainerRepository.findAll()
                    .stream()
                    .filter(t -> request.getWorkPermitNo().equals(t.getWorkPermitNo()))
                    .findFirst();
                
                if (existingTrainer.isPresent()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("message", "Trainer with Work Permit No " + request.getWorkPermitNo() + " already exists");
                    errorResponse.put("status", "ERROR");
                    errorResponse.put("data", null);
                    
                    return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(errorResponse);
                }
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
                .description(request.getDescription())
                .createdBy(request.getCreatedBy())
                .createdAt(new Date())
                .build();
            
            // Save trainer to database
            AddTrainer savedTrainer = addTrainerRepository.save(trainer);
            log.info("Trainer saved with ID: {}", savedTrainer.getId());
            
            // Save courses if present
            if (request.getCourses() != null && !request.getCourses().isEmpty()) {
                List<TrainerCourse> trainerCourses = request.getCourses().stream()
                    .map(courseDto -> TrainerCourse.builder()
                        .courseId(courseDto.getCourseId())
                        .courseTypeId(courseDto.getCourseTypeId())
                        .trainer(savedTrainer)
                        .build())
                    .collect(Collectors.toList());
                
                trainerCourseRepository.saveAll(trainerCourses);
                log.info("Saved {} courses for trainer ID: {}", trainerCourses.size(), savedTrainer.getId());
            }
            
            // Prepare success response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Trainer submitted successfully");
            successResponse.put("status", "SUCCESS");
            successResponse.put("data", savedTrainer);
            
            log.info("Trainer submitted successfully with ID: {}", savedTrainer.getId());
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(successResponse);
            
        } catch (Exception e) {
            log.error("Error submitting trainer: {}", e.getMessage(), e);
            
            // Prepare error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to submit trainer: " + e.getMessage());
            errorResponse.put("status", "ERROR");
            errorResponse.put("data", null);
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
    
    @Override
    public List<ObjectNode> getAllTrainer(Integer instituteId) {
        try {
            log.info("Fetching all trainers for institute ID: {}", instituteId);
            
            // You need to create a custom query in AddTrainerRepository
            // For now, using findAll() and filtering
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
                // Prepare error response
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Trainer not found with ID: " + request.getId());
                errorResponse.put("status", "ERROR");
                errorResponse.put("data", null);
                
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(errorResponse);
            }
            
            AddTrainer existingTrainer = existingTrainerOpt.get();
            
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
            existingTrainer.setDescription(request.getDescription());
            existingTrainer.setUpdatedBy(request.getUpdatedBy());
            existingTrainer.setUpdatedAt(new Date());
            
            // Save updated trainer
            AddTrainer updatedTrainer = addTrainerRepository.save(existingTrainer);
            log.info("Trainer updated with ID: {}", updatedTrainer.getId());
            
            // Update courses - delete existing and add new ones
            if (request.getCourses() != null) {
                // Delete existing courses
                trainerCourseRepository.deleteByTrainerId(updatedTrainer.getId());
                
                // Save new courses
                if (!request.getCourses().isEmpty()) {
                    List<TrainerCourse> trainerCourses = request.getCourses().stream()
                        .map(courseDto -> TrainerCourse.builder()
                            .courseId(courseDto.getCourseId())
                            .courseTypeId(courseDto.getCourseTypeId())
                            .trainer(updatedTrainer)
                            .build())
                        .collect(Collectors.toList());
                    
                    trainerCourseRepository.saveAll(trainerCourses);
                    log.info("Updated {} courses for trainer ID: {}", trainerCourses.size(), updatedTrainer.getId());
                }
            }
            
            // Prepare success response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Trainer updated successfully");
            successResponse.put("status", "SUCCESS");
            successResponse.put("data", updatedTrainer);
            
            log.info("Trainer updated successfully with ID: {}", updatedTrainer.getId());
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(successResponse);
            
        } catch (Exception e) {
            log.error("Error updating trainer: {}", e.getMessage(), e);
            
            // Prepare error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to update trainer: " + e.getMessage());
            errorResponse.put("status", "ERROR");
            errorResponse.put("data", null);
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
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
                // Prepare error response
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Trainer not found with ID: " + trainerId);
                errorResponse.put("status", "ERROR");
                errorResponse.put("data", null);
                
                return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(errorResponse);
            }
            
            // Soft delete by setting statusId to 0 (Inactive)
            AddTrainer trainer = existingTrainerOpt.get();
            trainer.setStatusId(0); // 0 for inactive/deleted
            trainer.setUpdatedAt(new Date());
            
            // Save to database
            AddTrainer deletedTrainer = addTrainerRepository.save(trainer);
            
            // Prepare success response
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Trainer deleted successfully");
            successResponse.put("status", "SUCCESS");
            successResponse.put("data", deletedTrainer);
            
            log.info("Trainer soft deleted successfully with ID: {}", trainerId);
            return ResponseEntity
                .status(HttpStatus.OK)
                .body(successResponse);
            
        } catch (Exception e) {
            log.error("Error deleting trainer: {}", e.getMessage(), e);
            
            // Prepare error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete trainer: " + e.getMessage());
            errorResponse.put("status", "ERROR");
            errorResponse.put("data", null);
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
}