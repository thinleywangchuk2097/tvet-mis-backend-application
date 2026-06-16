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
import com.moesd.tvet.mis.backend.application.dto.TuitionAnnouncementDto;
import com.moesd.tvet.mis.backend.application.service.TuitionAnnouncementService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/tuition-announcement")
public class TuitionAnnouncementController {
	
    private final TuitionAnnouncementService tuitionAnnouncementService;
    
	@PostMapping("/submit")
	public ResponseEntity<?> submitTuitionAnnouncement(@RequestBody TuitionAnnouncementDto request) {
		return (tuitionAnnouncementService.submitTuitionAnnouncement(request));
	}

	@GetMapping("/get-all-tuition-announcements/{institute_id}")
	public ResponseEntity<?> getAllTuitionAnnouncement(@PathVariable Integer institute_id) {
		List<ObjectNode> activeSubjects = tuitionAnnouncementService.getAllTuitionAnnouncement(institute_id);
		return ResponseEntity.ok(activeSubjects);
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateTuitionAnnouncement(@RequestBody TuitionAnnouncementDto request) {
		return tuitionAnnouncementService.updateTuitionAnnouncement(request);
	}

	@PostMapping("/delete/{tuitionId}")
	public ResponseEntity<?> softDeleteTuitionAnnouncement(@PathVariable Long tuitionId) {
		return tuitionAnnouncementService.softDeleteTuitionAnnouncement(tuitionId);
	}
}
