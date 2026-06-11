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
import com.moesd.tvet.mis.backend.application.dto.SectorRequestDTO;
import com.moesd.tvet.mis.backend.application.service.SectorOccupationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/sector-occupation")
public class SectorOccupationController {

	private final SectorOccupationService sectorOccupationService;

	@PostMapping("/create")
	public ResponseEntity<?> createSectorWithOccupations(@RequestBody SectorRequestDTO request) {
		return (sectorOccupationService.createSectorWithOccupations(request));
	}

	@GetMapping("/get-sector-occupation-details")
	public ResponseEntity<List<ObjectNode>> getSectorOccupationLists() {
		List<ObjectNode> data = sectorOccupationService.getSectorOccupationLists();
		return ResponseEntity.ok(data);
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateSectorWithOccupations(@RequestBody SectorRequestDTO request) {
		return sectorOccupationService.updateSectorWithOccupations(request);
	}

	@PostMapping("/delete/{sectorId}")
	public ResponseEntity<?> deleteSectorWithOccupations(@PathVariable Integer sectorId) {
		return sectorOccupationService.deleteSectorWithOccupations(sectorId);
	}

}