package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
	public ResponseEntity<List<ObjectNode>> getInstituteDropdown(@PathVariable String service_id){
	    List<ObjectNode> instituteDetails = monitoringAssessmentService.getInstituteDropdown(service_id);
	    return ResponseEntity.ok(instituteDetails);
	}
}
