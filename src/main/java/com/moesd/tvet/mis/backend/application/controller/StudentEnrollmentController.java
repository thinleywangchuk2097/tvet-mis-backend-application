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
import com.moesd.tvet.mis.backend.application.dto.StudentEnrollmentDto;
import com.moesd.tvet.mis.backend.application.service.StudentEnrollmentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/student-enrollment")
public class StudentEnrollmentController {

	private final StudentEnrollmentService studentEnrollmentService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitEnrollment(@RequestBody StudentEnrollmentDto request) {
		return studentEnrollmentService.submitEnrollment(request);
	}

	@GetMapping("/get-all-enrollments/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getAllActiveEnrollments(@PathVariable("institute_id") Integer instituteId) {
		return ResponseEntity.ok(studentEnrollmentService.getAllActiveEnrollments(instituteId));
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateEnrollment(@RequestBody StudentEnrollmentDto request) {
		return studentEnrollmentService.updateEnrollment(request);
	}

	@PostMapping("/delete/{id}")
	public ResponseEntity<?> softDeleteEnrollment(@PathVariable Long id) {
		return studentEnrollmentService.softDeleteEnrollment(id);
	}
}
