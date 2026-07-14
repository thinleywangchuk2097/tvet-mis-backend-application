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
	public ResponseEntity<List<ObjectNode>> getPlacementSessionByInstituteId(@PathVariable String institute_id) {
		List<ObjectNode> Details = onCampusJobPlacementService.getPlacementSessionByInstituteId(institute_id);
		return ResponseEntity.ok(Details);
	}

	@PostMapping("/submit-firm")
	public ResponseEntity<?> submitFirm(@RequestBody OnCampusJobPlacementFirmDto request) {
		return (onCampusJobPlacementService.submitFirm(request));
	}

	@GetMapping("/get-firm/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getFirmByInstituteId(@PathVariable String institute_id) {
		List<ObjectNode> Details = onCampusJobPlacementService.getFirmByInstituteId(institute_id);
		return ResponseEntity.ok(Details);
	}

	@PostMapping("/submit-trainee")
	public ResponseEntity<?> submitPlacementTrainee(@RequestBody OnCampusJobPlacementTraineeDto request) {
		return (onCampusJobPlacementService.submitPlacementTrainee(request));
	}

	@GetMapping("/get-trainee/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getTraineeByInstituteId(@PathVariable String institute_id) {
		List<ObjectNode> Details = onCampusJobPlacementService.getTraineeByInstituteId(institute_id);
		return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-trainee-report")
	public ResponseEntity<List<ObjectNode>> getTraineeOnPlacementReport() {
		List<ObjectNode> Details = onCampusJobPlacementService.getTraineeOnPlacementReport();
		return ResponseEntity.ok(Details);
	}
}
