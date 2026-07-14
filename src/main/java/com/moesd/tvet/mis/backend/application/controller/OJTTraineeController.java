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
import com.moesd.tvet.mis.backend.application.dto.OJTAgrementDto;
import com.moesd.tvet.mis.backend.application.dto.OJTCompanyDto;
import com.moesd.tvet.mis.backend.application.dto.OJTTraineeDto;
import com.moesd.tvet.mis.backend.application.service.OJTTraineeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/ojt")
public class OJTTraineeController {
	
	private final OJTTraineeService oJTTraineeService;
	
	@PostMapping("/submit-company")
	public ResponseEntity<?> submitOJTCompany(@RequestBody OJTCompanyDto request) {
		return (oJTTraineeService.submitOJTCompany(request));
	}
	
	@GetMapping("/get-company/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getCompanyByInstituteId(@PathVariable String institute_id){
	    List<ObjectNode> Details = oJTTraineeService.getCompanyByInstituteId(institute_id);
	    return ResponseEntity.ok(Details);
	}
	
	
	@PostMapping("/submit-agreement")
	public ResponseEntity<?> submitOJTAgrement(@RequestBody OJTAgrementDto request) {
		return (oJTTraineeService.submitOJTAgrement(request));
	}
	
	@GetMapping("/get-agreement/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getAgreementByInstituteId(@PathVariable String institute_id){
	    List<ObjectNode> Details = oJTTraineeService.getAgreementByInstituteId(institute_id);
	    return ResponseEntity.ok(Details);
	}
	
	@PostMapping("/submit-trainee")
	public ResponseEntity<?> submitOJTTrainee(@RequestBody OJTTraineeDto request) {
		return (oJTTraineeService.submitOJTTrainee(request));
	}
	
	@GetMapping("/get-trainee/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getTraineeByInstituteId(@PathVariable String institute_id){
	    List<ObjectNode> Details = oJTTraineeService.getTraineeByInstituteId(institute_id);
	    return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-trainee-ojt-report")
	public ResponseEntity<List<ObjectNode>> getTraineeOJTReport(){
	    List<ObjectNode> Details = oJTTraineeService.getTraineeOJTReport();
	    return ResponseEntity.ok(Details);
	}

}
