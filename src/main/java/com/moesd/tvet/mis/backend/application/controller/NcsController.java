package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Ncsdto;
import com.moesd.tvet.mis.backend.application.service.NcsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/ncs")
public class NcsController {

	private final NcsService ncsService;

	@PostMapping("/ncs-create")
	public ResponseEntity<?> submitNcs(@RequestBody Ncsdto request) {
		return ncsService.submitNcs(request);
	}

	@PutMapping("/ncs-update/{editingId}")
	public ResponseEntity<?> updateNcs(@PathVariable Integer editingId, @RequestBody Ncsdto request) {
		return ncsService.updateNcs(editingId, request);
	}

	@GetMapping("/get-ncs-details")
	public ResponseEntity<List<ObjectNode>> getNcsDetails() {
		List<ObjectNode> details = ncsService.getNcsDetails();
		return ResponseEntity.ok(details);
	}

	@GetMapping("/get-ncs-already-exist/{sector_id}/{occupation_id}/{certification_id}")
	public ResponseEntity<List<ObjectNode>> getAlreadyNcsDetailsExist(@PathVariable Integer sector_id,
			@PathVariable Integer occupation_id, @PathVariable Integer certification_id) {
		List<ObjectNode> Details = ncsService.getAlreadyNcsDetailsExist(sector_id, occupation_id, certification_id);
		return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-programme-title/{programmeId}")
	public ResponseEntity<List<ObjectNode>> getProgrammeTitleById(@PathVariable Integer programmeId) {
		List<ObjectNode> Details = ncsService.getProgrammeTitleById(programmeId);
		return ResponseEntity.ok(Details);
	}
	
}