package com.moesd.tvet.mis.backend.application.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AddTrainerDto;
import com.moesd.tvet.mis.backend.application.service.AddTrainerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/trainer")
public class AddTrainerController {

	private final AddTrainerService addTrainerService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitTrainer(@RequestBody AddTrainerDto request) {
		return (addTrainerService.submitTrainer(request));
	}

	@GetMapping("/get-all-trainer/{institute_id}")
	public ResponseEntity<?> getAllTrainer(@PathVariable("institute_id") Integer instituteId) {
		List<ObjectNode> trainer = addTrainerService.getAllTrainer(instituteId);
		return ResponseEntity.ok(trainer);
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateTrainer(@RequestBody AddTrainerDto request) {
		return addTrainerService.updateTrainer(request);
	}

	// Add / edit form: can this CID / work permit no be added for the institute?
	@GetMapping("/check-duplicate")
	public ResponseEntity<Map<String, Object>> checkTrainerConflict(
			@RequestParam(required = false) String citizenId, @RequestParam(required = false) String workPermitNo,
			@RequestParam Integer instituteId, @RequestParam(required = false) Long excludeId) {
		String message = addTrainerService.checkTrainerConflict(citizenId, workPermitNo, instituteId, excludeId);
		Map<String, Object> body = new HashMap<>();
		body.put("exists", message != null);
		body.put("message", message);
		return ResponseEntity.ok(body);
	}

	// Accredited programme form: lead trainer must be one of the institute's own active trainers
	@GetMapping("/institute-trainer")
	public ResponseEntity<Map<String, Object>> findInstituteTrainer(@RequestParam String citizenId,
			@RequestParam Integer instituteId) {
		return ResponseEntity.ok(addTrainerService.findInstituteTrainer(citizenId, instituteId));
	}

	@PostMapping("/delete/{trainerId}")
	public ResponseEntity<?> softDeleteTrainer(@PathVariable Long trainerId) {
		return addTrainerService.softDeleteTrainer(trainerId);
	}

}
