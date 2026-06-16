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
import com.moesd.tvet.mis.backend.application.dto.CourseEnrollmentAppdto;
import com.moesd.tvet.mis.backend.application.dto.Totdto;
import com.moesd.tvet.mis.backend.application.service.TotService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/tot")
public class TotController {
	
	private final TotService totService;
	@PostMapping("/tot-create")
	
	public ResponseEntity<?> submitCourseAnnouncement(@RequestBody Totdto request) {
		return (totService.submitCourseAnnouncement(request));
	}
	
	@GetMapping("/get-application-details")
	public ResponseEntity<List<ObjectNode>> getCourseDetailsAnnouncementByUserId() {
		List<ObjectNode> Details = totService.getCourseDetailsAnnouncementByUserId();
		return ResponseEntity.ok(Details);
	}

}
