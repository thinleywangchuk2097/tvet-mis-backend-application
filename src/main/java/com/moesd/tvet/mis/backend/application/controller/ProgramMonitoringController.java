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
import com.moesd.tvet.mis.backend.application.dto.ProgramMonitoringDto;
import com.moesd.tvet.mis.backend.application.service.ProgramMonitoringService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/program-monitoring")
public class ProgramMonitoringController {

	private final ProgramMonitoringService programMonitoringService;
	
	@GetMapping("/get-service")
	public ResponseEntity<List<ObjectNode>> getCourseService() {
		List<ObjectNode> seviceCourse = programMonitoringService.getCourseService();
		return ResponseEntity.ok(seviceCourse);
	}
	
	@PostMapping("/submit")
	public ResponseEntity<?> submitProgramMonitoring(@RequestBody ProgramMonitoringDto request) {
		return (programMonitoringService.submitProgramMonitoring(request));
	}
	
	@GetMapping("/get-program-monitoring/{user_id}")
	public ResponseEntity<List<ObjectNode>> getProgramMonitoring(@PathVariable String user_id){
	    List<ObjectNode> Details = programMonitoringService.getProgramMonitoring(user_id);
	    return ResponseEntity.ok(Details);
	}
	
	@PostMapping("/verify")
	public ResponseEntity<?> verifyProgramMonitoring(@RequestBody ProgramMonitoringDto request) {
		return (programMonitoringService.verifyProgramMonitoring(request));
	}
	
	@GetMapping("/get-program-monitoring-details/{applicationNo}")
	public ResponseEntity<List<ObjectNode>> getProgramMonitoringByApplicationNo(@PathVariable String applicationNo){
	    List<ObjectNode> Details = programMonitoringService.getProgramMonitoringByApplicationNo(applicationNo);
	    return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-courses/{institute_id}/{course_type_id}")
	public ResponseEntity<List<ObjectNode>> getCourseByInstituteId(@PathVariable Integer institute_id, @PathVariable Integer course_type_id){
	    List<ObjectNode> Details = programMonitoringService.getCourseByInstituteId(institute_id, course_type_id);
	    return ResponseEntity.ok(Details);
	}
}
