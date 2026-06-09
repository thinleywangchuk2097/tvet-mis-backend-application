package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.OccupationRequestDTO;
import com.moesd.tvet.mis.backend.application.dto.SectorRequestDTO;
import com.moesd.tvet.mis.backend.application.model.Occupation;
import com.moesd.tvet.mis.backend.application.model.Sector;
import com.moesd.tvet.mis.backend.application.repository.SectorOccupationRepository;
import com.moesd.tvet.mis.backend.application.service.SectorOccupationService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SectorOccupationServiceImpl implements SectorOccupationService{
	
	private final SectorOccupationRepository sectorOccupationRepository;

	private final ObjectToJson objectTojson;
	
	@Override
	@Transactional
	public ResponseEntity<?> createSectorWithOccupations(SectorRequestDTO request) {
		 try {
	            // Build main entity (Sector)
	            Sector sector = Sector.builder()
	                    .sectorName(request.getSectorName())
	                    .isActive(request.getIsActive())
	                    .build();
	            // Build Occupation entities
	            if (request.getChild() != null && !request.getChild().isEmpty()) {
	                List<Occupation> occupations = request.getChild()
	                        .stream()
	                        .map(occupationDto -> Occupation.builder()
	                                .occupationName(occupationDto.getOccupationName())
	                                .iscoCode(occupationDto.getIscoCode())
	                                .isActive(occupationDto.getIsActive())
	                                .sector(sector)  // Set the relationship
	                                .build())
	                        .collect(Collectors.toList());
	                sector.setChild(occupations);
	            }
	            // Save (cascade handles children)
	            Sector saved = sectorOccupationRepository.save(sector);
	           
	         // Response
				return ResponseEntity.status(201).body(java.util.Map.of("id", saved.getId(),
						"status", 201, "message", "Sector and occupations created successfully"));
	            
	        } catch (Exception e) {
				e.printStackTrace();
				return ResponseEntity.status(500)
						.body(java.util.Map.of("message", "Failed to Sector and occupations created", "error", e.getMessage()));
			}
	}

	@Override
	public List<ObjectNode> getSectorOccupationLists() {
		List<Tuple> resultList = sectorOccupationRepository
				.getSectorOccupationLists();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}
	
	@Override
	@Transactional
	public ResponseEntity<?> updateSectorWithOccupations(SectorRequestDTO request) {
	    // Find existing sector
	    Sector existingSector = sectorOccupationRepository.findById(request.getId())
	            .orElseThrow(() -> new RuntimeException("Sector not found with id: " + request.getId()));
	    
	    // Update sector basic info
	    existingSector.setSectorName(request.getSectorName());
	    existingSector.setIsActive(request.getIsActive());
	    
	    // Get requested IDs from front end
	    List<Integer> requestedIds = new ArrayList<>();
	    if (request.getChild() != null) {
	        for (OccupationRequestDTO dto : request.getChild()) {
	            if (dto.getId() != null) {
	                requestedIds.add(dto.getId());
	            }
	        }
	    }
	    
	    // Initialize child list if null (safety check)
	    if (existingSector.getChild() == null) {
	        existingSector.setChild(new ArrayList<>());
	    }
	    
	    // DELETE: Remove occupations not in request
	    // With orphanRemoval=true, this will delete them from database
	    existingSector.getChild().removeIf(occ -> 
	            occ.getId() != null && !requestedIds.contains(occ.getId()));
	    
	    // UPDATE or CREATE
	    if (request.getChild() != null) {
	        for (OccupationRequestDTO dto : request.getChild()) {
	            if (dto.getId() != null) {
	                // Try to find existing occupation in current list
	                Occupation existingOcc = existingSector.getChild().stream()
	                        .filter(occ -> occ.getId() != null && occ.getId().equals(dto.getId()))
	                        .findFirst()
	                        .orElse(null);
	                
	                if (existingOcc != null) {
	                    // Update existing occupation
	                    existingOcc.setOccupationName(dto.getOccupationName());
	                    existingOcc.setIscoCode(dto.getIscoCode());
	                    existingOcc.setIsActive(dto.getIsActive());
	                } else {
	                    // ID provided but not found - create new (ignore the ID)
	                    Occupation newOcc = Occupation.builder()
	                            .occupationName(dto.getOccupationName())
	                            .iscoCode(dto.getIscoCode())
	                            .isActive(dto.getIsActive())
	                            .sector(existingSector)
	                            .build();
	                    existingSector.getChild().add(newOcc);
	                }
	            } else {
	                // Create new occupation (no ID)
	                Occupation newOcc = Occupation.builder()
	                        .occupationName(dto.getOccupationName())
	                        .iscoCode(dto.getIscoCode())
	                        .isActive(dto.getIsActive())
	                        .sector(existingSector)
	                        .build();
	                existingSector.getChild().add(newOcc);
	            }
	        }
	    }
	    
	    // Save sector - this will cascade and delete orphaned occupations
	    sectorOccupationRepository.save(existingSector);
	    
	    return ResponseEntity.ok(Map.of(
	            "status", 200,
	            "message", "Sector and occupations updated successfully"));
	}
	
	//Soft Delete
	@Override
	@Transactional
	public ResponseEntity<?> deleteSectorWithOccupations(Integer sectorId) {
	    try {
	        Sector sector = sectorOccupationRepository.findById(sectorId)
	                .orElseThrow(() -> new RuntimeException("Sector not found with id: " + sectorId));
	        
	        sector.setIsActive('N');
	        
	        if (sector.getChild() != null) {
	            sector.getChild().forEach(occupation -> occupation.setIsActive('N'));
	        }
	        
	        // Save sector - should cascade to children with MERGE
	        sectorOccupationRepository.save(sector);
	        
	        return ResponseEntity.ok(Map.of(
	                "status", 200,
	                "message", "Sector and its occupations deactivated successfully"));
	                
	    } catch (RuntimeException e) {
	        log.error("Error deleting sector: {}", e.getMessage());
	        return ResponseEntity.status(404)
	                .body(Map.of("message", e.getMessage()));
	    } catch (Exception e) {
	        log.error("Failed to delete sector", e);
	        return ResponseEntity.status(500)
	                .body(Map.of("message", "Failed to delete sector and its occupations"));
	    }
	}

}
