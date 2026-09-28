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
import com.moesd.tvet.mis.backend.application.dto.NonAccreditedCoursedto;
import com.moesd.tvet.mis.backend.application.service.NonAccreditedCourseService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/non-accredited-course")
public class NonAccreditedCourseController {

	private final NonAccreditedCourseService instituteNonAccreditedCourseService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitNonAccreditedCourse(@RequestBody NonAccreditedCoursedto request) {
		return (instituteNonAccreditedCourseService.submitNonAccreditedCourse(request));
	}

	@GetMapping("/get-course-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getNonAccreditedCourseByApplicationNo(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> data = instituteNonAccreditedCourseService
				.getNonAccreditedCourseByApplicationNo(applicationNo);
		return ResponseEntity.ok(data);
	}

	@PostMapping("/verify-non-accredited-course")
	public ResponseEntity<?> verifyNonAccreditedCourse(@RequestBody NonAccreditedCoursedto request) {
		return (instituteNonAccreditedCourseService.verifyNonAccreditedCourse(request));
	}

	@GetMapping("/get-application-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getNonAccreditedCourseDetailsByUserId(
			@PathVariable("user_id") String userId) {
		List<ObjectNode> getNonAccreditedCourseDetails = instituteNonAccreditedCourseService.getNonAccreditedCourseDetailsByUserId(userId);
		return ResponseEntity.ok(getNonAccreditedCourseDetails);
	}

	@GetMapping("/get-non-accredited-approved-course-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getNonAccreditedApprovedCourseByUserId(
			@PathVariable("user_id") String userId) {
		List<ObjectNode> getNonAccreditedApprovedCourseDetails = instituteNonAccreditedCourseService.getNonAccreditedApprovedCourseByUserId(userId);
		return ResponseEntity.ok(getNonAccreditedApprovedCourseDetails);
	}

	@GetMapping("/curriculum-already-exist/{curriculumId}/{registration_no}")
	public ResponseEntity<List<ObjectNode>> curriculumAlreadyExist(@PathVariable Long curriculumId,
			@PathVariable("registration_no") String registrationNo) {
		List<ObjectNode> curriculumAlreadyExistDetails = instituteNonAccreditedCourseService.curriculumAlreadyExist(curriculumId,
				registrationNo);
		return ResponseEntity.ok(curriculumAlreadyExistDetails);
	}
}
