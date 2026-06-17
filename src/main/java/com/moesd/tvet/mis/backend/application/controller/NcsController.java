package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Ncsdto;
import com.moesd.tvet.mis.backend.application.service.NcsService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/ncs")
public class NcsController {
	
	private final NcsService ncsService;
	@PostMapping("/ncs-create")
	
	public ResponseEntity<?> submitNcs(@RequestBody Ncsdto request) {
		return (ncsService.submitNcs(request));
	}
	
	@GetMapping("/get-application-details")
	public ResponseEntity<List<ObjectNode>> getCourseDetailsAnnouncementByUserId() {
		List<ObjectNode> Details = ncsService.getCourseDetailsAnnouncementByUserId();
		return ResponseEntity.ok(Details);
	}

}
