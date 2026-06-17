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
import com.moesd.tvet.mis.backend.application.dto.SubjectDto;
import com.moesd.tvet.mis.backend.application.service.SubjectService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/subject")
public class SubjectController {
	
	private final SubjectService subjectService;
	
	@PostMapping("/submit")
	public ResponseEntity<?> submitSubject(@RequestBody SubjectDto request) {
		return (subjectService.submitSubject(request));
	}
	
	@GetMapping("/get-all-subjects/{institute_id}")
	public ResponseEntity<?> getAllActiveSubjects(@PathVariable Integer institute_id) {
		List<ObjectNode> activeSubjects = subjectService.getAllActiveSubjects(institute_id);
		return ResponseEntity.ok(activeSubjects);
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateSubject(@RequestBody SubjectDto request) {
		return subjectService.updateSubject(request);
	}

	@PostMapping("/delete/{subjectId}")
	public ResponseEntity<?> softDeleteSubject(@PathVariable Integer subjectId) {
		return subjectService.softDeleteSubject(subjectId);
	}
}
