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
import com.moesd.tvet.mis.backend.application.dto.AddTrainerDto;
import com.moesd.tvet.mis.backend.application.service.AddTrainerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/trainer")
public class AddTrainerController {
	
	private final AddTrainerService addTrainerService;
	
	@PostMapping("/submit")
	public ResponseEntity<?> submitTrainer(@RequestBody AddTrainerDto request) {
		return (addTrainerService.submitTrainer(request));
	}

	@GetMapping("/get-all-trainer/{institute_id}")
	public ResponseEntity<?> getAllTrainer(@PathVariable Integer institute_id) {
		List<ObjectNode> trainer = addTrainerService.getAllTrainer(institute_id);
		return ResponseEntity.ok(trainer);
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateTrainer(@RequestBody AddTrainerDto request) {
		return addTrainerService.updateTrainer(request);
	}

	@PostMapping("/delete/{trainerId}")
	public ResponseEntity<?> softDeleteTrainer(@PathVariable Long trainerId) {
		return addTrainerService.softDeleteTrainer(trainerId);
	}
	

}
