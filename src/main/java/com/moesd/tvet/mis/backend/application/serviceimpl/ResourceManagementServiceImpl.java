package com.moesd.tvet.mis.backend.application.serviceimpl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StaffManagementDto;
import com.moesd.tvet.mis.backend.application.dto.StaffTrainingHistoryDto;
import com.moesd.tvet.mis.backend.application.dto.StaffEmploymentHistoryDto;
import com.moesd.tvet.mis.backend.application.model.StaffEmploymentHistory;
import com.moesd.tvet.mis.backend.application.model.StaffManagement;
import com.moesd.tvet.mis.backend.application.model.StaffTrainingHistory;
import com.moesd.tvet.mis.backend.application.repository.StaffManagementRepository;
import com.moesd.tvet.mis.backend.application.service.ResourceManagementService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceManagementServiceImpl implements ResourceManagementService {
    
    private final StaffManagementRepository staffManagementRepository;
    private final ObjectToJson objectTojson;

    /** response body keys */
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_STATUS = "status";

    @Override
    @Transactional
    public ResponseEntity<?> submitStaff(StaffManagementDto request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(KEY_MESSAGE, "Request cannot be null", 
                                     KEY_STATUS, HttpStatus.BAD_REQUEST.value()));
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(KEY_MESSAGE, "Name is required", 
                                     KEY_STATUS, HttpStatus.BAD_REQUEST.value()));
            }

            // same person may not be added twice to the same institute
            String duplicate = checkStaffDuplicate(request.getInstituteId(), request.getCitizenId(),
                    request.getReferenceNo(), null);
            if (duplicate != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(KEY_MESSAGE, duplicate, KEY_STATUS, HttpStatus.CONFLICT.value()));
            }

            StaffManagement staff = StaffManagement.builder()
                    .hasCitizenId(request.getHasCitizenId())
                    .citizenId(request.getCitizenId())
                    .name(request.getName().trim())
                    .instituteId(request.getInstituteId())
                    .email(trimOrNull(request.getEmail()))
                    .mobileNo(trimOrNull(request.getMobileNo()))
                    .referenceNo(trimOrNull(request.getReferenceNo()))
                    .agencyName(blankToNull(request.getAgencyName()))
                    .fieldOfStudy(blankToNull(request.getFieldOfStudy()))
                    .positionLevel(blankToNull(request.getPositionLevel()))
                    .employeeId(blankToNull(request.getEmployeeId()))
                    .genderId(request.getGenderId())
                    .dob(request.getDob())
                    .createdBy(orZero(request.getCreatedBy()))
                    .updatedBy(orZero(request.getUpdatedBy()))
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
                                .Staff(staff)
                                .build())
                        .collect(Collectors.toCollection(ArrayList::new));
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
                                .fundingSourceId(trainDto.getFundingSourceId())
                                .trainingCost(trainDto.getTrainingCost())
                                .Staff(staff)
                                .build())
                        .collect(Collectors.toCollection(ArrayList::new));
                staff.setStafftraininghistory(trainingHistories);
            }

            StaffManagement savedStaff = staffManagementRepository.save(staff);

            log.info("Staff submitted successfully with ID: {}, Name: {}, CitizenId: {}", 
                     savedStaff.getId(), savedStaff.getName(), savedStaff.getCitizenId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            KEY_MESSAGE, "Staff submitted successfully",
                            KEY_STATUS, HttpStatus.CREATED.value()
                    ));

        } catch (Exception e) {
            log.error("Error submitting staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            KEY_MESSAGE, "Failed to submit staff: " + e.getMessage(),
                            KEY_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    private static String trimOrNull(String value) {
        return value != null ? value.trim() : null;
    }

    private static Integer orZero(Integer value) {
        return value != null ? value : 0;
    }

    /** Copies the editable staff fields from the request onto the stored staff. */
    private static void applyBasicFields(StaffManagement staff, StaffManagementDto request) {
        staff.setHasCitizenId(request.getHasCitizenId());
        staff.setCitizenId(request.getCitizenId());
        staff.setName(request.getName().trim());
        staff.setInstituteId(request.getInstituteId());
        staff.setEmail(trimOrNull(request.getEmail()));
        staff.setMobileNo(trimOrNull(request.getMobileNo()));
        staff.setReferenceNo(trimOrNull(request.getReferenceNo()));
        // civil servant details: only overwritten when the request carries them
        staff.setAgencyName(keepOrUpdate(staff.getAgencyName(), request.getAgencyName()));
        staff.setFieldOfStudy(keepOrUpdate(staff.getFieldOfStudy(), request.getFieldOfStudy()));
        staff.setPositionLevel(keepOrUpdate(staff.getPositionLevel(), request.getPositionLevel()));
        staff.setEmployeeId(keepOrUpdate(staff.getEmployeeId(), request.getEmployeeId()));
        staff.setGenderId(request.getGenderId());
        staff.setDob(request.getDob());
        staff.setUpdatedBy(orZero(request.getUpdatedBy()));
        staff.setUpdatedAt(new Date());
    }

    /** Replaces the employment history with the request's rows: known ids are updated, the rest added. */
    private static void mergeEmploymentHistory(StaffManagement staff, List<StaffEmploymentHistoryDto> rows) {
        List<StaffEmploymentHistory> current = staff.getStaffemploymenthistory();
        Map<Long, StaffEmploymentHistory> byId = current.stream()
                .filter(emp -> emp.getId() != null)
                .collect(Collectors.toMap(StaffEmploymentHistory::getId, emp -> emp));
        current.clear();
        for (StaffEmploymentHistoryDto row : rows) {
            current.add(toEmployment(staff, row, byId));
        }
    }

    private static StaffEmploymentHistory toEmployment(StaffManagement staff, StaffEmploymentHistoryDto empDto,
            Map<Long, StaffEmploymentHistory> byId) {
        StaffEmploymentHistory employment = empDto.getId() != null ? byId.get(empDto.getId()) : null;
        if (employment == null) {
            return StaffEmploymentHistory.builder()
                    .appointmentDate(empDto.getAppointmentDate())
                    .employmentTypeId(empDto.getEmploymentTypeId())
                    .qualificationId(empDto.getQualificationId())
                    .designation(empDto.getDesignation())
                    .Staff(staff)
                    .build();
        }
        employment.setAppointmentDate(empDto.getAppointmentDate());
        employment.setEmploymentTypeId(empDto.getEmploymentTypeId());
        employment.setQualificationId(empDto.getQualificationId());
        employment.setDesignation(empDto.getDesignation());
        employment.setStaff(staff);
        return employment;
    }

    /** Replaces the training history with the request's rows: known ids are updated, the rest added. */
    private static void mergeTrainingHistory(StaffManagement staff, List<StaffTrainingHistoryDto> rows) {
        List<StaffTrainingHistory> current = staff.getStafftraininghistory();
        Map<Long, StaffTrainingHistory> byId = current.stream()
                .filter(train -> train.getId() != null)
                .collect(Collectors.toMap(StaffTrainingHistory::getId, train -> train));
        current.clear();
        for (StaffTrainingHistoryDto row : rows) {
            current.add(toTraining(staff, row, byId));
        }
    }

    private static StaffTrainingHistory toTraining(StaffManagement staff, StaffTrainingHistoryDto trainDto,
            Map<Long, StaffTrainingHistory> byId) {
        StaffTrainingHistory training = trainDto.getId() != null ? byId.get(trainDto.getId()) : null;
        if (training == null) {
            return StaffTrainingHistory.builder()
                    .trainingName(trainDto.getTrainingName())
                    .trainingStart(trainDto.getTrainingStart())
                    .trainingEnd(trainDto.getTrainingEnd())
                    .providerName(trainDto.getProviderName())
                    .fundingSourceId(trainDto.getFundingSourceId())
                    .trainingCost(trainDto.getTrainingCost())
                    .Staff(staff)
                    .build();
        }
        training.setTrainingName(trainDto.getTrainingName());
        training.setTrainingStart(trainDto.getTrainingStart());
        training.setTrainingEnd(trainDto.getTrainingEnd());
        training.setProviderName(trainDto.getProviderName());
        training.setFundingSourceId(trainDto.getFundingSourceId());
        training.setTrainingCost(trainDto.getTrainingCost());
        training.setStaff(staff);
        return training;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /** The new value when one is sent, otherwise the stored one. */
    private static String keepOrUpdate(String current, String incoming) {
        String value = blankToNull(incoming);
        return value != null ? value : current;
    }

    @Override
    public String checkStaffDuplicate(Integer instituteId, String citizenId, String referenceNo, Long excludeId) {
        if (instituteId == null) {
            return null;
        }
        long exclude = excludeId != null ? excludeId : -1L;
        String cid = citizenId == null ? "" : citizenId.trim();
        if (!cid.isEmpty()) {
            return staffManagementRepository.existsCitizenInInstitute(instituteId, cid, exclude)
                    ? "Staff with Citizen ID " + cid + " is already registered in this institute."
                    : null;
        }
        String ref = referenceNo == null ? "" : referenceNo.trim();
        if (!ref.isEmpty() && staffManagementRepository.existsReferenceInInstitute(instituteId, ref, exclude)) {
            return "Staff with Reference No " + ref + " is already registered in this institute.";
        }
        return null;
    }

    @Override
    @Transactional
    public ResponseEntity<?> editStaff(StaffManagementDto request) {
        try {
            log.info("=== EDIT STAFF START ===");
            
            // ===== VALIDATION =====
            if (request == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(KEY_MESSAGE, "Request cannot be null", 
                                     KEY_STATUS, HttpStatus.BAD_REQUEST.value()));
            }

            if (request.getId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(KEY_MESSAGE, "Staff ID is required for update", 
                                     KEY_STATUS, HttpStatus.BAD_REQUEST.value()));
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(KEY_MESSAGE, "Name is required", 
                                     KEY_STATUS, HttpStatus.BAD_REQUEST.value()));
            }

            // ===== FIND EXISTING STAFF (only active ones) =====
            StaffManagement existingStaff = staffManagementRepository
                    .findByIdAndStatusIdNot(request.getId(), 0)
                    .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + request.getId()));

            // ===== CHECK FOR DUPLICATE IN THE SAME INSTITUTE (this staff excluded) =====
            Integer instituteId = request.getInstituteId() != null ? request.getInstituteId()
                    : existingStaff.getInstituteId();
            String duplicate = checkStaffDuplicate(instituteId, request.getCitizenId(), request.getReferenceNo(),
                    request.getId());
            if (duplicate != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of(KEY_MESSAGE, duplicate, KEY_STATUS, HttpStatus.CONFLICT.value()));
            }

            // ===== UPDATE FIELDS AND HISTORIES =====
            applyBasicFields(existingStaff, request);
            if (request.getStaffemploymenthistory() != null) {
                mergeEmploymentHistory(existingStaff, request.getStaffemploymenthistory());
            }
            if (request.getStafftraininghistory() != null) {
                mergeTrainingHistory(existingStaff, request.getStafftraininghistory());
            }

            // ===== SAVE =====
            StaffManagement updatedStaff = staffManagementRepository.save(existingStaff);
            
            log.info("Staff updated successfully with ID: {}, Name: {}, CitizenId: {}", 
                     updatedStaff.getId(), updatedStaff.getName(), updatedStaff.getCitizenId());

            return ResponseEntity.ok(Map.of(
                    KEY_MESSAGE, "Staff updated successfully",
                    KEY_STATUS, HttpStatus.OK.value()
            ));

        } catch (Exception e) {
            log.error("Error updating staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            KEY_MESSAGE, "Failed to update staff: " + e.getMessage(),
                            KEY_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteStaff(Long id) {
        try {
            if (id == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(KEY_MESSAGE, "Staff ID is required", 
                                     KEY_STATUS, HttpStatus.BAD_REQUEST.value()));
            }

            // ===== FIND EXISTING STAFF (including soft deleted) =====
            StaffManagement staff = staffManagementRepository
                    .findById(id) // Use standard findById to find even if statusId = 0
                    .orElseThrow(() -> new RuntimeException("Staff not found with ID: " + id));

            // Check if already deleted
            if (staff.getStatusId() != null && staff.getStatusId() == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                KEY_MESSAGE, "Staff is already deleted",
                                KEY_STATUS, HttpStatus.BAD_REQUEST.value()
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
                    KEY_MESSAGE, "Staff deleted successfully",
                    KEY_STATUS, HttpStatus.OK.value()
            ));

        } catch (Exception e) {
            log.error("Error deleting staff: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            KEY_MESSAGE, "Failed to delete staff: " + e.getMessage(),
                            KEY_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value()
                    ));
        }
    }

    @Override
    public List<ObjectNode> getInstituteStaff(String instituteId) {
        return objectTojson._toJson(staffManagementRepository.getInstituteStaff(instituteId));
    }
}