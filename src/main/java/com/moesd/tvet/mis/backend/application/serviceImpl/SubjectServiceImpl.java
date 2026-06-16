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
import com.moesd.tvet.mis.backend.application.dto.SubjectDto;
import com.moesd.tvet.mis.backend.application.model.Subject;
import com.moesd.tvet.mis.backend.application.repository.SubjectRepository;
import com.moesd.tvet.mis.backend.application.service.SubjectService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service 
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService{
	
	private final SubjectRepository subjectRepository;
	private final ObjectToJson objectTojson;
	
	@Override
	public ResponseEntity<?> submitSubject(SubjectDto request) {
		try {
			log.info("Submitting new subject: {}", request.getSubjectName());
			
			// Validate if subject name already exists
			Optional<Subject> existingSubjectByName = subjectRepository.findBySubjectName(request.getSubjectName());
			if (existingSubjectByName.isPresent()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Subject name already exists: " + request.getSubjectName());
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.CONFLICT)
					.body(errorResponse);
			}
			// Create new subject entity
			Subject subject = Subject.builder()
				.subjectCode(request.getSubjectCode())
				.subjectName(request.getSubjectName())
				.creditHours(request.getCreditHours())
				.theoryHours(request.getTheoryHours())
				.practicalHours(request.getPracticalHours())
				.instituteId(request.getInstituteId())
				.statusId(request.getStatusId() != null ? request.getStatusId() : 1)
				.description(request.getDescription())
				.createdBy(request.getCreatedBy())
				.createdAt(new Date())
				.build();
			
			// Save to database
			Subject savedSubject = subjectRepository.save(subject);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Subject submitted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", savedSubject);
			
			log.info("Subject submitted successfully with ID: {}", savedSubject.getId());
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error submitting subject: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to submit subject: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}
	
	@Override
	public List<ObjectNode> getAllActiveSubjects(Integer institute_id) {
		List<Tuple> resultList= subjectRepository.getAllActiveSubjects(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public ResponseEntity<?> updateSubject(SubjectDto request) {
		try {
			log.info("Updating subject with ID: {}", request.getId());
			
			// Check if subject exists
			Optional<Subject> existingSubjectOpt = subjectRepository.findById(request.getId());
			if (existingSubjectOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Subject not found with ID: " + request.getId());
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			Subject existingSubject = existingSubjectOpt.get();
			
			// Check if subject name is being changed and if new name already exists
			if (!existingSubject.getSubjectName().equals(request.getSubjectName())) {
				Optional<Subject> duplicateName = subjectRepository.findBySubjectName(request.getSubjectName());
				if (duplicateName.isPresent()) {
					// Prepare error response
					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("message", "Subject name already exists: " + request.getSubjectName());
					errorResponse.put("status", "ERROR");
					errorResponse.put("data", null);
					
					return ResponseEntity
						.status(HttpStatus.CONFLICT)
						.body(errorResponse);
				}
			}
			
			// Update subject entity
			existingSubject.setSubjectCode(request.getSubjectCode());
			existingSubject.setSubjectName(request.getSubjectName());
			existingSubject.setCreditHours(request.getCreditHours());
			existingSubject.setTheoryHours(request.getTheoryHours());
			existingSubject.setPracticalHours(request.getPracticalHours());
			existingSubject.setInstituteId(request.getInstituteId());
			existingSubject.setStatusId(request.getStatusId());
			existingSubject.setDescription(request.getDescription());
			existingSubject.setUpdatedBy(request.getUpdatedBy());
			existingSubject.setUpdatedAt(new Date());
			
			// Save to database
			Subject updatedSubject = subjectRepository.save(existingSubject);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Subject updated successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", updatedSubject);
			
			log.info("Subject updated successfully with ID: {}", updatedSubject.getId());
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error updating subject: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to update subject: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public ResponseEntity<?> softDeleteSubject(Integer subjectId) {
		try {
			log.info("Soft deleting subject with ID: {}", subjectId);
			
			// Check if subject exists
			Optional<Subject> existingSubjectOpt = subjectRepository.findById(Long.valueOf(subjectId));
			if (existingSubjectOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Subject not found with ID: " + subjectId);
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			// Soft delete by setting statusId to 0 (Inactive)
			Subject subject = existingSubjectOpt.get();
			subject.setStatusId(0);
			subject.setUpdatedAt(new Date());
			
			// Save to database
			Subject deletedSubject = subjectRepository.save(subject);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Subject deleted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", deletedSubject);
			
			log.info("Subject soft deleted successfully with ID: {}", subjectId);
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error deleting subject: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to delete subject: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

}
