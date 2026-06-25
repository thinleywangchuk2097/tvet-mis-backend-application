package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Ncsdto;
import com.moesd.tvet.mis.backend.application.model.NcsApp;
import com.moesd.tvet.mis.backend.application.repository.NcsRepository;
import com.moesd.tvet.mis.backend.application.service.NcsService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NcsServiceImpl implements NcsService{
	
	private final NcsRepository ncsRepository;
	private final ObjectToJson objectTojson;
	
	public ResponseEntity<?> submitNcs(Ncsdto request) {
		try {
			// Build entity
			NcsApp dtls = NcsApp.builder()
					.occupationId(request.getOccupationId())
					.certificationId(request.getCertificationId())
					.courseTitle(request.getCourseTitle())
					.validityDate(request.getValidityDate())
					.ncsCode(request.getNcsCode())
					.publicationType(request.getPublicationType()).build();
			// Save entity
			ncsRepository.save(dtls);
			// Response
			return ResponseEntity.status(201).body(Map.of("status", 201, "message",
					"Created submitted successfully"));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500)
					.body(Map.of("message", "Failed to submit course announcement", "error", e.getMessage()));
		}
	}
	
	@Override
	public List<ObjectNode> getCourseDetailsAnnouncementByUserId() {
		List<Tuple> resultList = ncsRepository.getCourseDetailsAnnouncementByUserId();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}
}
