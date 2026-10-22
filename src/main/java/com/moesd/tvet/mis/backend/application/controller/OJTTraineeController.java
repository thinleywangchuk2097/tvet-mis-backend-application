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
	public ResponseEntity<List<ObjectNode>> getCompanyByInstituteId(@PathVariable("institute_id") String instituteId) {
		List<ObjectNode> getDetails = oJTTraineeService.getCompanyByInstituteId(instituteId);
		return ResponseEntity.ok(getDetails);
	}

	@PostMapping("/submit-agreement")
	public ResponseEntity<?> submitOJTAgrement(@RequestBody OJTAgrementDto request) {
		return (oJTTraineeService.submitOJTAgrement(request));
	}

	@GetMapping("/get-agreement/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getAgreementByInstituteId(
			@PathVariable("institute_id") String instituteId) {
		List<ObjectNode> getDetailLists = oJTTraineeService.getAgreementByInstituteId(instituteId);
		return ResponseEntity.ok(getDetailLists);
	}

	@PostMapping("/submit-trainee")
	public ResponseEntity<?> submitOJTTrainee(@RequestBody OJTTraineeDto request) {
		return (oJTTraineeService.submitOJTTrainee(request));
	}

	@GetMapping("/get-trainee/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getTraineeByInstituteId(@PathVariable("institute_id") String instituteId) {
		List<ObjectNode> details = oJTTraineeService.getTraineeByInstituteId(instituteId);
		return ResponseEntity.ok(details);
	}

	@GetMapping("/get-trainee-ojt-report")
	public ResponseEntity<List<ObjectNode>> getTraineeOJTReport() {
		List<ObjectNode> getTraineeDetails = oJTTraineeService.getTraineeOJTReport();
		return ResponseEntity.ok(getTraineeDetails);
	}

}
