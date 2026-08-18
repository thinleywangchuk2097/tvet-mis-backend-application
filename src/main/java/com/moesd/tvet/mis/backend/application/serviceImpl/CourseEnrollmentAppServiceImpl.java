package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CourseEnrollmentAppdto;
import com.moesd.tvet.mis.backend.application.model.CourseEnrollmentApp;
import com.moesd.tvet.mis.backend.application.repository.CourseEnrollmentAppRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.CourseEnrollmentAppService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class CourseEnrollmentAppServiceImpl implements CourseEnrollmentAppService {

	private final CourseEnrollmentAppRepository courseEnrollmentAppRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ServiceMasterRepository serviceMasterRepository;
	private final DocumentFileUploadService documentFileUploadService;
	private final ObjectToJson objectTojson;

	@Override
	@Transactional
	public ResponseEntity<?> submitCourseAnnouncement(CourseEnrollmentAppdto request) {

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
			CourseEnrollmentApp course = CourseEnrollmentApp.builder().applicationNo(applicationNo)
					.applicationStartDate(request.getApplicationStartDate())
					.applicationEndDate(request.getApplicationEndDate()).courseStartDate(request.getCourseStartDate())
					.courseEndDate(request.getCourseEndDate()).caEndDate(request.getCaEndDate())
					.caStartDate(request.getCaStartDate()).certificationLevelId(request.getCertificationLevelId())
					.courseDescription(request.getCourseDescription()).feesPerTrainee(request.getFeesPerTrainee())
					.courseId(request.getCourseId()).fundingSourceId(request.getFundingSourceId())
					.instituteId(request.getInstituteId()).remarks(request.getRemarks())
					.serviceId(request.getServiceId()).enrollmentCapacity(request.getEnrollmentCapacity())
					.courseDescription(request.getCourseDescription())
					.trainingLocationId(request.getTrainingLocationId()).statusId(request.getStatusId())
					.createdAt(new java.util.Date()).createdBy(request.getCreatedBy()).build();

			// Save entity
			courseEnrollmentAppRepository.save(course);

	
			// Documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo, "course_announcement",
						request.getServiceId(), request.getCreatedBy().toString(), null);
			}

			// Response
			return ResponseEntity.status(201).body(Map.of("applicationNo", applicationNo, "status", 201, "message",
					"Course announcement submitted successfully"));

		} catch (Exception e) {
		    log.error("Failed to submit course announcement", e);
		    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
		            .body(Map.of("message", "Failed to submit course announcement"));
		}
	}

	@Override
	public List<ObjectNode> getCourseDetailsAnnouncementByUserId(String user_id, String service_id) {
		List<Tuple> resultList = courseEnrollmentAppRepository.getCourseDetailsAnnouncementByUserId(user_id,service_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getReAssessmentServiceName() {
		List<Tuple> resultList = courseEnrollmentAppRepository.getReAssessmentServiceName();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	

}
