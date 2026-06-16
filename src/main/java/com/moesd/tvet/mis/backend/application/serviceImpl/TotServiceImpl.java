package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Totdto;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentApp;
import com.moesd.tvet.mis.backend.application.model.TotApp;
import com.moesd.tvet.mis.backend.application.repository.CourseEnrollmentAppRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.repository.TotRepository;
import com.moesd.tvet.mis.backend.application.service.TotService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TotServiceImpl implements TotService{
	
	private final TotRepository totRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final ObjectToJson objectTojson;
	
	public ResponseEntity<?> submitCourseAnnouncement(Totdto request) {

		try {

			// Validation
			if (request.getServiceId() == null)
				throw new RuntimeException("serviceId is required");

			if (request.getStatusId() == null)
				throw new RuntimeException("statusId is required");

			// Validate service existence
			serviceMasterRepository.findById(request.getServiceId())
					.orElseThrow(() -> new RuntimeException("Service Id not found"));

			// Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(request.getServiceId());

			// Build entity
			TotApp course = TotApp.builder().applicationNo(applicationNo)
					.applicationStartDate(request.getApplicationStartDate())
					.applicationEndDate(request.getApplicationEndDate())
					.courseStartDate(request.getCourseStartDate())
					.courseEndDate(request.getCourseEndDate())
					.courseId(request.getCourseId())
					.courseDescription(request.getCourseDescription())
					.createdAt(new java.util.Date()).createdBy(request.getCreatedBy()).build();

			// Save entity
			totRepository.save(course);

			// Response
			return ResponseEntity.status(201).body(Map.of("applicationNo", applicationNo, "status", 201, "message",
					"Course announcement submitted successfully"));

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500)
					.body(Map.of("message", "Failed to submit course announcement", "error", e.getMessage()));
		}
	}
	
	@Override
	public List<ObjectNode> getCourseDetailsAnnouncementByUserId() {
		List<Tuple> resultList = totRepository.getCourseDetailsAnnouncementByUserId();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}
}
