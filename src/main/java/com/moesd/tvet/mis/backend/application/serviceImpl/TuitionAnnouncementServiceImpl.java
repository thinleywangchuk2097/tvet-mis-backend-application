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
import com.moesd.tvet.mis.backend.application.dto.TuitionAnnouncementDto;
import com.moesd.tvet.mis.backend.application.model.TuitionAnnouncement;
import com.moesd.tvet.mis.backend.application.repository.TuitionAnnouncementRepository;
import com.moesd.tvet.mis.backend.application.service.TuitionAnnouncementService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TuitionAnnouncementServiceImpl implements TuitionAnnouncementService{
	
	private final TuitionAnnouncementRepository tuitionAnnouncementRepository;
	private final ObjectToJson objectTojson;
	
	@Override
	public ResponseEntity<?> submitTuitionAnnouncement(TuitionAnnouncementDto request) {
		try {
			log.info("Submitting new tuition announcement: {}", request.getTitle());
			
			// Create new tuition announcement entity
			TuitionAnnouncement announcement = TuitionAnnouncement.builder()
				.title(request.getTitle())
				.subjectId(request.getSubjectId())
				.tutorId(request.getTutorId())
				.startDate(request.getStartDate())
				.endDate(request.getEndDate())
				.description(request.getDescription())
				.instituteId(request.getInstituteId())
				.startTime(request.getStartTime())
				.endTime(request.getEndTime())
				.venue(request.getVenue())
				.maxStudents(request.getMaxStudents())
				.fee(request.getFee())
				.materials(request.getMaterials())
				.requirements(request.getRequirements())
				.contactPerson(request.getContactPerson())
				.contactPhone(request.getContactPhone())
				.statusId(request.getStatusId() != null ? request.getStatusId() : "1")
				.createdBy(request.getCreatedBy())
				.createdAt(new Date())
				.build();
			
			// Save to database
			TuitionAnnouncement savedAnnouncement = tuitionAnnouncementRepository.save(announcement);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Tuition announcement submitted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", savedAnnouncement);
			
			log.info("Tuition announcement submitted successfully with ID: {}", savedAnnouncement.getId());
			return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error submitting tuition announcement: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to submit tuition announcement: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public List<ObjectNode> getAllTuitionAnnouncement(Integer institute_id) {
		List<Tuple> resultList = tuitionAnnouncementRepository.getAllTuitionAnnouncements(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public ResponseEntity<?> updateTuitionAnnouncement(TuitionAnnouncementDto request) {
		try {
			log.info("Updating tuition announcement with ID: {}", request.getId());
			
			// Check if announcement exists
			Optional<TuitionAnnouncement> existingAnnouncementOpt = tuitionAnnouncementRepository.findById(request.getId());
			if (existingAnnouncementOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Tuition announcement not found with ID: " + request.getId());
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			TuitionAnnouncement existingAnnouncement = existingAnnouncementOpt.get();
			
			// Update announcement entity
			existingAnnouncement.setTitle(request.getTitle());
			existingAnnouncement.setSubjectId(request.getSubjectId());
			existingAnnouncement.setTutorId(request.getTutorId());
			existingAnnouncement.setStartDate(request.getStartDate());
			existingAnnouncement.setEndDate(request.getEndDate());
			existingAnnouncement.setDescription(request.getDescription());
			existingAnnouncement.setInstituteId(request.getInstituteId());
			existingAnnouncement.setStartTime(request.getStartTime());
			existingAnnouncement.setEndTime(request.getEndTime());
			existingAnnouncement.setVenue(request.getVenue());
			existingAnnouncement.setMaxStudents(request.getMaxStudents());
			existingAnnouncement.setFee(request.getFee());
			existingAnnouncement.setMaterials(request.getMaterials());
			existingAnnouncement.setRequirements(request.getRequirements());
			existingAnnouncement.setContactPerson(request.getContactPerson());
			existingAnnouncement.setContactPhone(request.getContactPhone());
			existingAnnouncement.setStatusId(request.getStatusId());
			existingAnnouncement.setUpdatedBy(request.getUpdatedBy());
			existingAnnouncement.setUpdatedAt(new Date());
			
			// Save to database
			TuitionAnnouncement updatedAnnouncement = tuitionAnnouncementRepository.save(existingAnnouncement);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Tuition announcement updated successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", updatedAnnouncement);
			
			log.info("Tuition announcement updated successfully with ID: {}", updatedAnnouncement.getId());
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error updating tuition announcement: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to update tuition announcement: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}

	@Override
	public ResponseEntity<?> softDeleteTuitionAnnouncement(Long tuitionId) {
		try {
			log.info("Soft deleting tuition announcement with ID: {}", tuitionId);
			
			// Check if announcement exists
			Optional<TuitionAnnouncement> existingAnnouncementOpt = tuitionAnnouncementRepository.findById(tuitionId);
			if (existingAnnouncementOpt.isEmpty()) {
				// Prepare error response
				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("message", "Tuition announcement not found with ID: " + tuitionId);
				errorResponse.put("status", "ERROR");
				errorResponse.put("data", null);
				
				return ResponseEntity
					.status(HttpStatus.NOT_FOUND)
					.body(errorResponse);
			}
			
			// Soft delete by setting statusId to "3" (Closed) or "4" (Cancelled)
			// Using "3" for Closed as soft delete
			TuitionAnnouncement announcement = existingAnnouncementOpt.get();
			announcement.setStatusId("3"); // Closed status
			announcement.setUpdatedAt(new Date());
			
			// Save to database
			TuitionAnnouncement deletedAnnouncement = tuitionAnnouncementRepository.save(announcement);
			
			// Prepare success response
			Map<String, Object> successResponse = new HashMap<>();
			successResponse.put("message", "Tuition announcement deleted successfully");
			successResponse.put("status", "SUCCESS");
			successResponse.put("data", deletedAnnouncement);
			
			log.info("Tuition announcement soft deleted successfully with ID: {}", tuitionId);
			return ResponseEntity
				.status(HttpStatus.OK)
				.body(successResponse);
			
		} catch (Exception e) {
			log.error("Error deleting tuition announcement: {}", e.getMessage(), e);
			
			// Prepare error response
			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("message", "Failed to delete tuition announcement: " + e.getMessage());
			errorResponse.put("status", "ERROR");
			errorResponse.put("data", null);
			
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse);
		}
	}
}