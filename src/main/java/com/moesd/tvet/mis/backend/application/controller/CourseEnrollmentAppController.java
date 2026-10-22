package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CourseEnrollmentAppdto;
import com.moesd.tvet.mis.backend.application.service.CourseEnrollmentAppService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/course-announcement")
public class CourseEnrollmentAppController {

	private final CourseEnrollmentAppService courseEnrollmentAppService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitCourseAnnouncement(@RequestBody CourseEnrollmentAppdto request) {
		return (courseEnrollmentAppService.submitCourseAnnouncement(request));
	}

	@GetMapping("/get-application-details/{user_id}/{service_id}")
	public ResponseEntity<List<ObjectNode>> getCourseDetailsAnnouncementByUserId(@PathVariable("user_id") String userId,
			@PathVariable("service_id") String serviceId) {
		List<ObjectNode> getCourseDetailsAnnouncementDetails = courseEnrollmentAppService.getCourseDetailsAnnouncementByUserId(userId, serviceId);
		return ResponseEntity.ok(getCourseDetailsAnnouncementDetails);
	}

	@GetMapping("/get-reassessment-service-name")
	public ResponseEntity<List<ObjectNode>> getReAssessmentServiceName() {
		List<ObjectNode> getReAssessmentServiceNameDetails = courseEnrollmentAppService.getReAssessmentServiceName();
		return ResponseEntity.ok(getReAssessmentServiceNameDetails);
	}

	@GetMapping("/get-trainee-verification-details/{traineeId}")
	public ResponseEntity<List<ObjectNode>> getTraineeDetailsById(@PathVariable String traineeId) {
		List<ObjectNode> getTraineeDetails = courseEnrollmentAppService.getTraineeDetailsById(traineeId);
		return ResponseEntity.ok(getTraineeDetails);
	}

}
