package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SelfAssessmentReportdto;
import com.moesd.tvet.mis.backend.application.service.SelfAssessmentReportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/self-assessment-report")
public class SelfAssessmentReportController {

	private final SelfAssessmentReportService selfAssessmentReportService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitSelfAssessmentReport(@RequestBody SelfAssessmentReportdto request) {
		return selfAssessmentReportService.submitSelfAssessmentReport(request);
	}

	@PostMapping("/save-draft")
	public ResponseEntity<?> saveDraftSelfAssessmentReport(@RequestBody SelfAssessmentReportdto request) {
		return selfAssessmentReportService.saveDraftSelfAssessmentReport(request);
	}

	@PostMapping("/verify-self-assessment-report")
	public ResponseEntity<?> verifySelfAssessmentReport(@RequestBody SelfAssessmentReportdto request) {
		return selfAssessmentReportService.verifySelfAssessmentReport(request);
	}

	@GetMapping("/get-report-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getSelfAssessmentReportByApplicationNo(
			@PathVariable("application_no") String applicationNo) {
		return ResponseEntity.ok(selfAssessmentReportService.getSelfAssessmentReportByApplicationNo(applicationNo));
	}

	@GetMapping("/get-application-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getSelfAssessmentReportsByUserId(@PathVariable("user_id") String userId) {
		return ResponseEntity.ok(selfAssessmentReportService.getSelfAssessmentReportsByUserId(userId));
	}

	@GetMapping("/get-basic-information/{user_id}")
	public ResponseEntity<Map<String, Object>> getBasicInformationByUserId(@PathVariable("user_id") String userId) {
		return ResponseEntity.ok(selfAssessmentReportService.getBasicInformationByUserId(userId));
	}

	@GetMapping("/get-basic-information-by-institute/{institute_id}")
	public ResponseEntity<Map<String, Object>> getBasicInformationByInstituteId(
			@PathVariable("institute_id") Integer instituteId) {
		return ResponseEntity.ok(selfAssessmentReportService.getBasicInformationByInstituteId(instituteId));
	}

	@GetMapping("/get-active-report/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getActiveSelfAssessmentReport(
			@PathVariable("institute_id") Integer instituteId) {
		return ResponseEntity.ok(selfAssessmentReportService.getActiveSelfAssessmentReport(instituteId));
	}
}
