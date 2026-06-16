package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TutorDto;
import com.moesd.tvet.mis.backend.application.model.Tutor;
import com.moesd.tvet.mis.backend.application.repository.TutorRepository;
import com.moesd.tvet.mis.backend.application.service.TutorService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TutorServiceImpl implements TutorService{
	
	private final TutorRepository tutorRepository;
	private final ObjectToJson objectTojson;
	
	@Override
	public ResponseEntity<?> submitTutor(TutorDto request) {
		try {
			log.info("Submitting new tutor: {}", request.getFirstName() + " " + request.getLastName());
			
			// Create new tutor entity
			Tutor tutor = Tutor.builder()
				.citizenId(request.getCitizenId())
				.firstName(request.getFirstName())
				.middleName(request.getMiddleName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.mobileNo(request.getMobileNo())
				.qualificationId(request.getQualificationId())
				.instituteId(request.getInstituteId())
				.experienceYears(request.getExperienceYears())
				.hourlyRate(request.getHourlyRate())
				.statusId(request.getStatusId() != null ? request.getStatusId() : 1)
				.specialization(request.getSpecialization())
				.joiningDate(request.getJoiningDate())
				.description(request.getDescription())
				.createdBy(request.getCreatedBy())
				.createdAt(new Date())
				.build();
			
			// Save to database
			Tutor savedTutor = tutorRepository.save(tutor);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Tutor submitted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", savedTutor);
			
			log.info("Tutor submitted successfully with ID: {}", savedTutor.getId());
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error submitting tutor: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to submit tutor: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public List<ObjectNode> getAllActiveTutors(Integer institute_id) {
		List<Tuple> resultList = tutorRepository.getAllActiveTutors(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public ResponseEntity<?> updateTutor(TutorDto request) {
		try {
			log.info("Updating tutor with ID: {}", request.getId());
			
			// Check if tutor exists
			Optional<Tutor> existingTutorOpt = tutorRepository.findById(request.getId());
			if (existingTutorOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Tutor not found with ID: " + request.getId());
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			Tutor existingTutor = existingTutorOpt.get();
			
			// Update tutor entity
			existingTutor.setCitizenId(request.getCitizenId());
			existingTutor.setFirstName(request.getFirstName());
			existingTutor.setMiddleName(request.getMiddleName());
			existingTutor.setLastName(request.getLastName());
			existingTutor.setEmail(request.getEmail());
			existingTutor.setMobileNo(request.getMobileNo());
			existingTutor.setQualificationId(request.getQualificationId());
			existingTutor.setInstituteId(request.getInstituteId());
			existingTutor.setExperienceYears(request.getExperienceYears());
			existingTutor.setHourlyRate(request.getHourlyRate());
			existingTutor.setStatusId(request.getStatusId());
			existingTutor.setSpecialization(request.getSpecialization());
			existingTutor.setJoiningDate(request.getJoiningDate());
			existingTutor.setDescription(request.getDescription());
			existingTutor.setUpdatedBy(request.getUpdatedBy());
			existingTutor.setUpdatedAt(new Date());
			
			// Save to database
			Tutor updatedTutor = tutorRepository.save(existingTutor);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Tutor updated successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", updatedTutor);
			
			log.info("Tutor updated successfully with ID: {}", updatedTutor.getId());
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error updating tutor: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to update tutor: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public ResponseEntity<?> softDeleteTutor(Long tutorId) {
		try {
			log.info("Soft deleting tutor with ID: {}", tutorId);
			
			// Check if tutor exists
			Optional<Tutor> existingTutorOpt = tutorRepository.findById(tutorId);
			if (existingTutorOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Tutor not found with ID: " + tutorId);
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			// Soft delete by setting statusId to 2 (Inactive)
			Tutor tutor = existingTutorOpt.get();
			tutor.setStatusId(2);
			tutor.setUpdatedAt(new Date());
			
			// Save to database
			Tutor deletedTutor = tutorRepository.save(tutor);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Tutor deleted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", deletedTutor);
			
			log.info("Tutor soft deleted successfully with ID: {}", tutorId);
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error deleting tutor: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to delete tutor: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public List<ObjectNode> getTutorBySubjectId(Integer institute_id, Integer subject_id) {
		List<Tuple> resultList = tutorRepository.getTutorBySubjectId(institute_id, subject_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}
}