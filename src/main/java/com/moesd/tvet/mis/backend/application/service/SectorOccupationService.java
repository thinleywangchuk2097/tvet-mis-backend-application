package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SectorRequestDTO;

public interface SectorOccupationService {

	ResponseEntity<?> createSectorWithOccupations(SectorRequestDTO sectorRequestDTO);

	List<ObjectNode> getSectorOccupationLists();

	ResponseEntity<?> updateSectorWithOccupations(SectorRequestDTO sectorRequestDTO);
	
	ResponseEntity<?> deleteSectorWithOccupations(Integer sectorId);
}
