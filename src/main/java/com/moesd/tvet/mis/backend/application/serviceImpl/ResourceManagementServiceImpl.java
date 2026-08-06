package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StaffManagementDto;
import com.moesd.tvet.mis.backend.application.model.StaffEmploymentHistory;
import com.moesd.tvet.mis.backend.application.model.StaffManagement;
import com.moesd.tvet.mis.backend.application.model.StaffTrainingHistory;
import com.moesd.tvet.mis.backend.application.repository.StaffManagementRepository;
import com.moesd.tvet.mis.backend.application.service.ResourceManagementService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceManagementServiceImpl implements ResourceManagementService {
    
    private final StaffManagementRepository staffManagementRepository;
    private final ObjectToJson objectTojson;

    @Override
    @Transactional
    public ResponseEntity<?> submitStaff(StaffManagementDto request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Request cannot be null", 
                                     "status", HttpStatus.BAD_REQUEST.value()));
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Name is required", 
                                     "status", HttpStatus.BAD_REQUEST.value()));
            }

            if (request.getCitizenId() != null && !request.getCitizenId().trim().isEmpty()) {
                if (staffManagementRepository.existsByCitizenIdAndStatusIdNot(request.getCitizenId(), 0)) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Staff with Citizen ID '" + request.getCitizenId() + "' already exists", 
                                         "status", HttpStatus.BAD_REQUEST.value()));
                }
            }

            StaffManagement staff = StaffManagement.builder()
                    .hasCitizenId(request.getHasCitizenId())
                    .citizenId(request.getCitizenId())
                    .name(request.getName().trim())
                    .instituteId(request.getInstituteId())
                    .email(request.getEmail() != null ? request.getEmail().trim() : null)
                    .mobileNo(request.getMobileNo() != null ? request.getMobileNo().trim() : null)
                    .referenceNo(request.getReferenceNo() != null ? request.getReferenceNo().trim() : null)
                    .genderId(request.getGenderId())
                    .dob(request.getDob())
                    .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : 0)
                    .updatedBy(request.getUpdatedBy() != null ? request.getUpdatedBy() : 0)
                    .statusId(1) // Active by default
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            if (request.getStaffemploymenthistory() != null && !request.getStaffemploymenthistory().isEmpty()) {
                List<StaffEmploymentHistory> employmentHistories = request.getStaffemploymenthistory()
                        .stream()
                        .map(empDto -> StaffEmploymentHistory.builder()
                                .appointmentDate(empDto.getAppointmentDate())
                                .employmentTypeId(empDto.getEmploymentTypeId())
                                .qualificationId(empDto.getQualificationId())
                                .designation(empDto.getDesignation())
                                .resignationDate(empDto.getResignationDate())
                                .Staff(staff)
                                .build())
                        .collect(Collectors.toList());
                staff.setStaffemploymenthistory(employmentHistories);
            }

            if (request.getStafftraininghistory() != null && !request.getStafftraininghistory().isEmpty()) {
                List<StaffTrainingHistory> trainingHistories = request.getStafftraininghistory()
                        .stream()
                        .map(trainDto -> StaffTrainingHistory.builder()
                                .trainingName(trainDto.getTrainingName())
                                .trainingStart(trainDto.getTrainingStart())
                                .trainingEnd(trainDto.getTrainingEnd())
                                .providerName(trainDto.getProviderName())
                                .resignationDate(trainDto.getResignationDate())
                                .fundingSourceId(trainDto.getFundingSourceId())
                                .trainingCost(trainDto.getTrainingCost())
                                .Staff(staff)
                                .build())
                        .collect(Collectors.toList());
                staff.setStafftraininghistory(trainingHistories);
            }

            StaffManagement savedStaff = staffManagementRepository.save(staff);

            log.info("Staff submitted successfully with ID: {}, Name: {}, CitizenId: {}", 
                     savedStaff.getId(), savedStaff.getName(), savedStaff.getCitizenId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Staff submitted successfully",
                            "status", HttpStatus.CREATED.value()
                    ));

        } catch (Exception e) {
            log.error("Error submitting staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Failed to submit staff: " + e.getMessage(),
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> editStaff(StaffManagementDto request) {
        try {
            log.info("=== EDIT STAFF START ===");
            
            // ===== VALIDATION =====
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Request cannot be null", 
                                     "status", HttpStatus.BAD_REQUEST.value()));
            }

            if (request.getId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Staff ID is required for update", 
                                     "status", HttpStatus.BAD_REQUEST.value()));
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Name is required", 
                                     "status", HttpStatus.BAD_REQUEST.value()));
            }

            // ===== FIND EXISTING STAFF (only active ones) =====
            StaffManagement existingStaff = staffManagementRepository
                    .findByIdAndStatusIdNot(request.getId(), 0)
                    .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + request.getId()));

            // ===== CHECK FOR DUPLICATE CITIZEN ID =====
            if (request.getCitizenId() != null && !request.getCitizenId().trim().isEmpty()) {
                StaffManagement staffWithSameCitizenId = staffManagementRepository
                        .findByCitizenIdAndStatusIdNot(request.getCitizenId(), 0)
                        .orElse(null);
                
                if (staffWithSameCitizenId != null && !staffWithSameCitizenId.getId().equals(request.getId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("message", "Staff with Citizen ID '" + request.getCitizenId() + "' already exists", 
                                         "status", HttpStatus.CONFLICT.value()));
                }
            }

            // ===== UPDATE BASIC FIELDS =====
            existingStaff.setHasCitizenId(request.getHasCitizenId());
            existingStaff.setCitizenId(request.getCitizenId());
            existingStaff.setName(request.getName().trim());
            existingStaff.setInstituteId(request.getInstituteId());
            existingStaff.setEmail(request.getEmail() != null ? request.getEmail().trim() : null);
            existingStaff.setMobileNo(request.getMobileNo() != null ? request.getMobileNo().trim() : null);
            existingStaff.setReferenceNo(request.getReferenceNo() != null ? request.getReferenceNo().trim() : null);
            existingStaff.setGenderId(request.getGenderId());
            existingStaff.setDob(request.getDob());
            existingStaff.setUpdatedBy(request.getUpdatedBy() != null ? request.getUpdatedBy() : 0);
            existingStaff.setUpdatedAt(new Date());

            // ===== UPDATE EMPLOYMENT HISTORY =====
            List<StaffEmploymentHistory> currentEmployment = existingStaff.getStaffemploymenthistory();
            
            if (request.getStaffemploymenthistory() != null) {
                // Create a map of current employment by ID
                Map<Long, StaffEmploymentHistory> currentEmploymentMap = currentEmployment.stream()
                        .filter(emp -> emp.getId() != null)
                        .collect(Collectors.toMap(StaffEmploymentHistory::getId, emp -> emp));

                // Clear the current list
                currentEmployment.clear();
                
                // Process each employment from request
                for (var empDto : request.getStaffemploymenthistory()) {
                    StaffEmploymentHistory employment;
                    
                    if (empDto.getId() != null && currentEmploymentMap.containsKey(empDto.getId())) {
                        // Update existing
                        employment = currentEmploymentMap.get(empDto.getId());
                        employment.setAppointmentDate(empDto.getAppointmentDate());
                        employment.setEmploymentTypeId(empDto.getEmploymentTypeId());
                        employment.setQualificationId(empDto.getQualificationId());
                        employment.setDesignation(empDto.getDesignation());
                        employment.setResignationDate(empDto.getResignationDate());
                        employment.setStaff(existingStaff);
                    } else {
                        // Create new
                        employment = StaffEmploymentHistory.builder()
                                .appointmentDate(empDto.getAppointmentDate())
                                .employmentTypeId(empDto.getEmploymentTypeId())
                                .qualificationId(empDto.getQualificationId())
                                .designation(empDto.getDesignation())
                                .resignationDate(empDto.getResignationDate())
                                .Staff(existingStaff)
                                .build();
                    }
                    currentEmployment.add(employment);
                }
            }

            // ===== UPDATE TRAINING HISTORY =====
            List<StaffTrainingHistory> currentTraining = existingStaff.getStafftraininghistory();
            
            if (request.getStafftraininghistory() != null) {
                // Create a map of current training by ID
                Map<Long, StaffTrainingHistory> currentTrainingMap = currentTraining.stream()
                        .filter(train -> train.getId() != null)
                        .collect(Collectors.toMap(StaffTrainingHistory::getId, train -> train));

                // Clear the current list
                currentTraining.clear();
                
                // Process each training from request
                for (var trainDto : request.getStafftraininghistory()) {
                    StaffTrainingHistory training;
                    
                    if (trainDto.getId() != null && currentTrainingMap.containsKey(trainDto.getId())) {
                        // Update existing
                        training = currentTrainingMap.get(trainDto.getId());
                        training.setTrainingName(trainDto.getTrainingName());
                        training.setTrainingStart(trainDto.getTrainingStart());
                        training.setTrainingEnd(trainDto.getTrainingEnd());
                        training.setProviderName(trainDto.getProviderName());
                        training.setResignationDate(trainDto.getResignationDate());
                        training.setFundingSourceId(trainDto.getFundingSourceId());
                        training.setTrainingCost(trainDto.getTrainingCost());
                        training.setStaff(existingStaff);
                    } else {
                        // Create new
                        training = StaffTrainingHistory.builder()
                                .trainingName(trainDto.getTrainingName())
                                .trainingStart(trainDto.getTrainingStart())
                                .trainingEnd(trainDto.getTrainingEnd())
                                .providerName(trainDto.getProviderName())
                                .resignationDate(trainDto.getResignationDate())
                                .fundingSourceId(trainDto.getFundingSourceId())
                                .trainingCost(trainDto.getTrainingCost())
                                .Staff(existingStaff)
                                .build();
                    }
                    currentTraining.add(training);
                }
            }

            // ===== SAVE =====
            StaffManagement updatedStaff = staffManagementRepository.save(existingStaff);
            
            log.info("Staff updated successfully with ID: {}, Name: {}, CitizenId: {}", 
                     updatedStaff.getId(), updatedStaff.getName(), updatedStaff.getCitizenId());

            return ResponseEntity.ok(Map.of(
                    "message", "Staff updated successfully",
                    "status", HttpStatus.OK.value()
            ));

        } catch (Exception e) {
            log.error("Error updating staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Failed to update staff: " + e.getMessage(),
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteStaff(Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Staff ID is required", 
                                     "status", HttpStatus.BAD_REQUEST.value()));
            }

            // ===== FIND EXISTING STAFF (including soft deleted) =====
            StaffManagement staff = staffManagementRepository
                    .findById(id) // Use standard findById to find even if statusId = 0
                    .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));

            // Check if already deleted
            if (staff.getStatusId() != null && staff.getStatusId() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "message", "Staff is already deleted",
                                "status", HttpStatus.BAD_REQUEST.value()
                        ));
            }

            // ===== SOFT DELETE - Set statusId to 0 =====
            staff.setStatusId(0);
            staff.setUpdatedBy(0); // You can pass the actual user ID here
            staff.setUpdatedAt(new Date());
            
            StaffManagement deletedStaff = staffManagementRepository.save(staff);
            
            log.info("Staff soft deleted successfully with ID: {}, Name: {}", 
                     deletedStaff.getId(), deletedStaff.getName());
            
            return ResponseEntity.ok(Map.of(
                    "message", "Staff deleted successfully",
                    "status", HttpStatus.OK.value()
            ));

        } catch (Exception e) {
            log.error("Error deleting staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Failed to delete staff: " + e.getMessage(),
                            "status", HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Override
    public List<ObjectNode> getInstituteStaff(String instituteId) {
        List<Tuple> resultList = staffManagementRepository.getInstituteStaff(instituteId);
        List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
        return DtlsJson;
    }
}