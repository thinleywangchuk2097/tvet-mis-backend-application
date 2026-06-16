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
import com.moesd.tvet.mis.backend.application.dto.TutorDto;
import com.moesd.tvet.mis.backend.application.service.TutorService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/tutor")
public class TutorController {
	
	private final TutorService tutorService;
	
	@PostMapping("/submit")
	public ResponseEntity<?> submitTutor(@RequestBody TutorDto request) {
		return (tutorService.submitTutor(request));
	}
	
	@GetMapping("/get-all-tutors/{institute_id}")
	public ResponseEntity<?> getAllActiveTutors(@PathVariable Integer institute_id) {
		List<ObjectNode> activeSubjects = tutorService.getAllActiveTutors(institute_id);
		return ResponseEntity.ok(activeSubjects);
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateTutor(@RequestBody TutorDto request) {
		return tutorService.updateTutor(request);
	}

	@PostMapping("/delete/{tutorId}")
	public ResponseEntity<?> softDeleteTutor(@PathVariable Long tutorId) {
		return tutorService.softDeleteTutor(tutorId);
	}
	@GetMapping("/get-tutor/{institute_id}/{subject_id}")
	public ResponseEntity<?> getTutorBySubjectId(@PathVariable Integer institute_id, @PathVariable Integer subject_id) {
		List<ObjectNode> tutor = tutorService.getTutorBySubjectId(institute_id, subject_id);
		return ResponseEntity.ok(tutor);
	}
}
