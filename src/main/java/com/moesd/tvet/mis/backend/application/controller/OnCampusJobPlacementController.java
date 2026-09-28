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
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementFirmDto;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementSessionDto;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementTraineeDto;
import com.moesd.tvet.mis.backend.application.service.OnCampusJobPlacementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/campus-placement")
public class OnCampusJobPlacementController {

	private final OnCampusJobPlacementService onCampusJobPlacementService;

	@PostMapping("/submit-session")
	public ResponseEntity<?> submitPlacementSession(@RequestBody OnCampusJobPlacementSessionDto request) {
		return (onCampusJobPlacementService.submitPlacementSession(request));
	}

	@GetMapping("/get-session/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getPlacementSessionByInstituteId(
			@PathVariable("institute_id") String instituteId) {
		List<ObjectNode> getPlacementSessionDetails = onCampusJobPlacementService.getPlacementSessionByInstituteId(instituteId);
		return ResponseEntity.ok(getPlacementSessionDetails);
	}

	@PostMapping("/submit-firm")
	public ResponseEntity<?> submitFirm(@RequestBody OnCampusJobPlacementFirmDto request) {
		return (onCampusJobPlacementService.submitFirm(request));
	}

	@GetMapping("/get-firm/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getFirmByInstituteId(@PathVariable("institute_id") String instituteId) {
		List<ObjectNode> getFirmByInstituteDetails = onCampusJobPlacementService.getFirmByInstituteId(instituteId);
		return ResponseEntity.ok(getFirmByInstituteDetails);
	}

	@PostMapping("/submit-trainee")
	public ResponseEntity<?> submitPlacementTrainee(@RequestBody OnCampusJobPlacementTraineeDto request) {
		return (onCampusJobPlacementService.submitPlacementTrainee(request));
	}

	@GetMapping("/get-trainee/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getTraineeByInstituteId(@PathVariable("institute_id") String instituteId) {
		List<ObjectNode> getTraineeDetails = onCampusJobPlacementService.getTraineeByInstituteId(instituteId);
		return ResponseEntity.ok(getTraineeDetails);
	}

	@GetMapping("/get-trainee-report")
	public ResponseEntity<List<ObjectNode>> getTraineeOnPlacementReport() {
		List<ObjectNode> getTraineeOnPlacementDetails = onCampusJobPlacementService.getTraineeOnPlacementReport();
		return ResponseEntity.ok(getTraineeOnPlacementDetails);
	}
}
