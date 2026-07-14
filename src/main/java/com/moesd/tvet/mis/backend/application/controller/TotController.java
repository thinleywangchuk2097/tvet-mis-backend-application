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
import com.moesd.tvet.mis.backend.application.dto.TOTProgramAnnouncementDto;
import com.moesd.tvet.mis.backend.application.dto.TOTProgramTrainerAppliedDto;
import com.moesd.tvet.mis.backend.application.dto.TotProgramDto;
import com.moesd.tvet.mis.backend.application.service.TotService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/tot")
public class TotController {
	
	private final TotService totService;
	
	@PostMapping("/submit-tot-program")
	public ResponseEntity<?> submitTOTProgram(@RequestBody TotProgramDto request) {
		return (totService.submitTOTProgram(request));
	}
	
	@GetMapping("/get-tot-programs")
	public ResponseEntity<List<ObjectNode>> getToTPrograms() {
		List<ObjectNode> Details = totService.getToTPrograms();
		return ResponseEntity.ok(Details);
	}
	
	@PostMapping("/delete-tot-program/{id}")
	public ResponseEntity<?> deleteToTPrograms(@PathVariable Long id) {
		return totService.deleteToTPrograms(id);
	}
	
	
	@PostMapping("/submit-tot-announcement")
	public ResponseEntity<?> submitTOTProgramAnnouncement(@RequestBody TOTProgramAnnouncementDto request) {
		return (totService.submitTOTProgramAnnouncement(request));
	}
	
	@GetMapping("/get-tot-announcements")
	public ResponseEntity<List<ObjectNode>> getToTProgramsAnnouncement() {
		List<ObjectNode> Details = totService.getToTProgramsAnnouncement();
		return ResponseEntity.ok(Details);
	}
	
	@PostMapping("/delete-tot-announcement/{id}")
	public ResponseEntity<?> deleteToTProgramsAnnouncement(@PathVariable Long id) {
		return totService.deleteToTProgramsAnnouncement(id);
	}
	
	@PostMapping("/apply-trainer-to-tot-program")
	public ResponseEntity<?> applyTrainerToTOTProgram( @RequestBody List<TOTProgramTrainerAppliedDto> request) {
		return (totService.applyTrainerToTOTProgram(request));
	}

}
