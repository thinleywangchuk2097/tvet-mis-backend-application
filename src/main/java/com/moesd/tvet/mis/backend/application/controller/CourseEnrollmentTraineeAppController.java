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
import com.moesd.tvet.mis.backend.application.dto.CourseEnrollmentTraineeAppdto;
import com.moesd.tvet.mis.backend.application.dto.SelectedTraineedto;
import com.moesd.tvet.mis.backend.application.service.CourseEnrollmentTraineeAppService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/course-enrollment-trainee")
public class CourseEnrollmentTraineeAppController {

	private final CourseEnrollmentTraineeAppService courseEnrollmentTraineeAppService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitTrainee(@RequestBody CourseEnrollmentTraineeAppdto request) {
		return (courseEnrollmentTraineeAppService.submitTrainee(request));
	}

	@GetMapping("/get-applicant-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getCourseAppliedTraineesByApplicationNo(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> getTraineeDetails = courseEnrollmentTraineeAppService
				.getCourseAppliedTraineesByApplicationNo(applicationNo);
		return ResponseEntity.ok(getTraineeDetails);
	}

	@GetMapping("/get-reassessment-applicant-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getCourseAppliedTraineesReAssessmentByApplicationNo(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> getDetails = courseEnrollmentTraineeAppService
				.getCourseAppliedTraineesReAssessmentByApplicationNo(applicationNo);
		return ResponseEntity.ok(getDetails);
	}

	@PostMapping("/selected-trainees")
	public ResponseEntity<?> submitSelectedTrainee(@RequestBody SelectedTraineedto request) {
		return (courseEnrollmentTraineeAppService.submitSelectedTrainee(request));
	}

	@PostMapping("/selected-reassessment-trainees")
	public ResponseEntity<?> submitReassessmentTrainees(@RequestBody SelectedTraineedto request) {
		return (courseEnrollmentTraineeAppService.submitReassessmentTrainees(request));
	}

	@PostMapping("/update-trainees-application")
	public ResponseEntity<?> updateTraineeApplication(@RequestBody SelectedTraineedto request) {
		return (courseEnrollmentTraineeAppService.updateTraineeApplication(request));
	}

	@GetMapping("/get-trainee-details/{user_id}/{course_id}/{certification_level_id}")
	public ResponseEntity<List<ObjectNode>> getFailedTraineeDetails(@PathVariable("user_id") String userId,
			@PathVariable("course_id") String courseId,
			@PathVariable("certification_level_id") Integer certificationLevelId) {
		List<ObjectNode> details = courseEnrollmentTraineeAppService.getFailedTraineeDetails(userId, courseId,
				certificationLevelId);
		return ResponseEntity.ok(details);
	}

	@PostMapping("/select-unselect-trainees")
	public ResponseEntity<?> selectUnselectTrainee(@RequestBody SelectedTraineedto request) {
		return (courseEnrollmentTraineeAppService.selectUnselectTrainee(request));
	}

	@GetMapping("/get-assigned-assessors/{application_no}")
	public ResponseEntity<List<ObjectNode>> fetchAssignedAssessors(
			@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> detailsList = courseEnrollmentTraineeAppService.fetchAssignedAssessors(applicationNo);
		return ResponseEntity.ok(detailsList);
	}

	@PostMapping("/remove-selected-trainee")
	public ResponseEntity<?> removeTraineeFromSelectedProgramme(@RequestBody SelectedTraineedto request) {
		return (courseEnrollmentTraineeAppService.removeTraineeFromSelectedProgramme(request));
	}

}
