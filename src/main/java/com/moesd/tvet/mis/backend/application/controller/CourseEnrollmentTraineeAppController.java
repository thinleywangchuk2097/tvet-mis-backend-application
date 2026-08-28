package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
			@PathVariable String application_no) {
		List<ObjectNode> Details = courseEnrollmentTraineeAppService
				.getCourseAppliedTraineesByApplicationNo(application_no);
		return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-reassessment-applicant-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getCourseAppliedTraineesReAssessmentByApplicationNo(
			@PathVariable String application_no) {
		List<ObjectNode> Details = courseEnrollmentTraineeAppService
				.getCourseAppliedTraineesReAssessmentByApplicationNo(application_no);
		return ResponseEntity.ok(Details);
	}

	@PostMapping("/selected-trainees")
	public ResponseEntity<?> selectedTrainee(@RequestBody SelectedTraineedto request) {
		return (courseEnrollmentTraineeAppService.selectedTrainee(request));
	}
	
	@PostMapping("/selected-reassessment-trainees")
	public ResponseEntity<?> submitReassessmentTrainees(@RequestBody SelectedTraineedto request) {
		return (courseEnrollmentTraineeAppService.submitReassessmentTrainees(request));
	}

	@PostMapping("/update-trainees-application")
	public ResponseEntity<?> updateTraineeApplication(@RequestBody SelectedTraineedto request) {
		return (courseEnrollmentTraineeAppService.updateTraineeApplication(request));
	}

	@GetMapping("/get-trainee-details/{user_id}/{course_id}")
	public ResponseEntity<List<ObjectNode>> getFailedTraineeDetails(@PathVariable String user_id,
			@PathVariable String course_id) {
		List<ObjectNode> Details = courseEnrollmentTraineeAppService.getFailedTraineeDetails(user_id, course_id);
		return ResponseEntity.ok(Details);
	}

	@PostMapping("/select-unselect-trainees")
	public ResponseEntity<?> selectUnselectTrainee(@RequestBody SelectedTraineedto request) {
		return (courseEnrollmentTraineeAppService.selectUnselectTrainee(request));
	}
	
	
	@GetMapping("/get-assigned-assessors/{application_no}")
	public ResponseEntity<List<ObjectNode>> fetchAssignedAssessors(@PathVariable String application_no) {
		List<ObjectNode> Details = courseEnrollmentTraineeAppService.fetchAssignedAssessors(application_no);
		return ResponseEntity.ok(Details);
	}

}
