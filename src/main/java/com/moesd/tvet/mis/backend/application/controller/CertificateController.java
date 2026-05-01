package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.service.CertificateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/certificate")
public class CertificateController {
	private final CertificateService certificateService;
	
	
	@GetMapping("/get-assessment-institutes")
	public ResponseEntity<?> getAssessmentInstitute() {
		List<ObjectNode> assessmentInstitutesLists = certificateService.getAssessmentInstitute();
		return ResponseEntity.ok(assessmentInstitutesLists);
	}
	
	@GetMapping("/get-assessement-courses/{instituteId}")
	public ResponseEntity<?> getAssessmentCourse(@PathVariable Integer instituteId) {
		List<ObjectNode> assessmentCourseLists = certificateService.getAssessmentCourse(instituteId);
		return ResponseEntity.ok(assessmentCourseLists);
	}
}
