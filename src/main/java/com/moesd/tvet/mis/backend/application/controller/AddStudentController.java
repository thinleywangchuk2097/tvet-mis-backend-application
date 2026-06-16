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
import com.moesd.tvet.mis.backend.application.dto.StudentAddDto;
import com.moesd.tvet.mis.backend.application.service.AddStudentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/student")
public class AddStudentController {

	private final AddStudentService addStudentService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitStudent(@RequestBody StudentAddDto request) {
		return (addStudentService.submitStudent(request));
	}

	@GetMapping("/get-all-students/{student_id}")
	public ResponseEntity<?> getAllActiveStudents(@PathVariable Integer student_id) {
		List<ObjectNode> activeSubjects = addStudentService.getAllActiveStudents(student_id);
		return ResponseEntity.ok(activeSubjects);
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateStudent(@RequestBody StudentAddDto request) {
		return addStudentService.updateStudent(request);
	}

	@PostMapping("/delete/{studentId}")
	public ResponseEntity<?> softDeleteStudent(@PathVariable Long studentId) {
		return addStudentService.softDeleteStudent(studentId);
	}
}
