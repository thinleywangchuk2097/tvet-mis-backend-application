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
import com.moesd.tvet.mis.backend.application.dto.MonitoringAssessmentDto;
import com.moesd.tvet.mis.backend.application.service.MonitoringAssessmentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/monitoring")
public class MonitoringAssessmentController {

	private final MonitoringAssessmentService monitoringAssessmentService;

	@GetMapping("/get-institute-type")
	public ResponseEntity<List<ObjectNode>> getInstituteTypeDropdown() {
		List<ObjectNode> instituteDetails = monitoringAssessmentService.getInstituteTypeDropdown();
		return ResponseEntity.ok(instituteDetails);
	}

	@GetMapping("/get-institutes-dropdown/{service_id}")
	public ResponseEntity<List<ObjectNode>> getInstituteDropdown(@PathVariable("service_id") String serviceId) {
		List<ObjectNode> getInstituteDropdown = monitoringAssessmentService.getInstituteDropdown(serviceId);
		return ResponseEntity.ok(getInstituteDropdown);
	}

	@PostMapping("/submit")
	public ResponseEntity<?> submitMonitoringAssessment(@RequestBody MonitoringAssessmentDto request) {
		return (monitoringAssessmentService.submitMonitoringAssessment(request));
	}

	@GetMapping("/get-monitoring-assessment/{user_id}")
	public ResponseEntity<List<ObjectNode>> getMonitoringAssessment(@PathVariable("user_id") String userId) {
		List<ObjectNode> getMonitoringAssessmentDetails = monitoringAssessmentService.getMonitoringAssessment(userId);
		return ResponseEntity.ok(getMonitoringAssessmentDetails);
	}

	@PostMapping("/verify")
	public ResponseEntity<?> verifyMonitoringAssessment(@RequestBody MonitoringAssessmentDto request) {
		return (monitoringAssessmentService.verifyMonitoringAssessment(request));
	}

	@GetMapping("/get-monitoring-assessment-details/{applicationNo}")
	public ResponseEntity<List<ObjectNode>> getMonitoringAssessmentByApplicationNo(@PathVariable String applicationNo) {
		List<ObjectNode> getMonitoringDetails = monitoringAssessmentService.getMonitoringAssessmentByApplicationNo(applicationNo);
		return ResponseEntity.ok(getMonitoringDetails);
	}

	@GetMapping("/get-institutes-renewal-status/{registrationNo}")
	public ResponseEntity<List<ObjectNode>> getInstitutesRenewalStatus(@PathVariable String registrationNo) {
		List<ObjectNode> instituteDetails = monitoringAssessmentService.getInstitutesRenewalStatus(registrationNo);
		return ResponseEntity.ok(instituteDetails);
	}

}
