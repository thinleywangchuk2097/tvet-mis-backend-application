package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.Ncsdto;
import com.moesd.tvet.mis.backend.application.dto.NcsUnitDto;
import com.moesd.tvet.mis.backend.application.model.NcsApp;
import com.moesd.tvet.mis.backend.application.model.NcsUnit;
import com.moesd.tvet.mis.backend.application.repository.NcsRepository;
import com.moesd.tvet.mis.backend.application.repository.NcsUnitRepository;
import com.moesd.tvet.mis.backend.application.service.NcsService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.GenerateApplicationNumber;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NcsServiceImpl implements NcsService {

	private final NcsRepository ncsRepository;
	private final GenerateApplicationNumber generateApplicationNumber;
	private final ObjectToJson objectTojson;
	private final DocumentFileUploadService documentFileUploadService;
	private final NcsUnitRepository ncsUnitRepository;
	private static final String MESSAGE_KEY = "message";
	
	@Override
	@Transactional
	public ResponseEntity<?> submitNcs(Ncsdto request) {
		try {
			// Generate application number
			String applicationNo = generateApplicationNumber.generateApplicationNumber(request.getServiceId());

			NcsApp ncsApp = NcsApp.builder().occupationId(request.getOccupationId()).serviceId(request.getServiceId())
					.applicationNo(applicationNo).sectorId(request.getSectorId())
					.certificationId(request.getCertificationId()).programmeTitle(request.getProgrammeTitle())
					.validityDate(request.getValidityDate()).createdBy(request.getCreatedBy()).build();

			NcsApp savedNcs = ncsRepository.save(ncsApp);

			if (request.getUnits() != null && !request.getUnits().isEmpty()) {
				List<NcsUnit> units = new ArrayList<>();
				for (NcsUnitDto unitDto : request.getUnits()) {
					NcsUnit unit = NcsUnit.builder().unitCode(unitDto.getUnitCode()).unitTitle(unitDto.getUnitTitle())
							.ncsApp(savedNcs).build();
					units.add(unit);
				}
				ncsUnitRepository.saveAll(units);
			}
			// Documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), applicationNo, "NCS",
						request.getServiceId(), null, null);
			}

			return ResponseEntity.status(201).body(Map.of("status", 201, MESSAGE_KEY, "NCS created successfully"

			));

		} catch (Exception e) {
			log.error("Error submitting NCS: ", e);
			return ResponseEntity.status(500).body(Map.of(MESSAGE_KEY, "Failed to submit NCS", "error", e.getMessage()));
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> updateNcs(Integer editingId, Ncsdto request) {
		try {
			NcsApp existingNcs = ncsRepository.findById(editingId)
					.orElseThrow(() -> new RuntimeException("NCS not found with id: " + editingId));
			// Update NCS fields
			existingNcs.setProgrammeTitle(request.getProgrammeTitle());
			existingNcs.setValidityDate(request.getValidityDate());
			existingNcs.setUpdatedBy(request.getUpdatedBy());

			existingNcs.getUnits().clear();

			if (request.getUnits() != null && !request.getUnits().isEmpty()) {
				for (NcsUnitDto unitDto : request.getUnits()) {
					NcsUnit unit = NcsUnit.builder().unitCode(unitDto.getUnitCode()).unitTitle(unitDto.getUnitTitle())
							.ncsApp(existingNcs).build();
					existingNcs.getUnits().add(unit);
				}
			}
			ncsRepository.save(existingNcs);
			// Documents
			if (request.getDocuments() != null && request.getDocuments().length > 0) {
				documentFileUploadService.saveDocument(request.getDocuments(), request.getApplicationNo(), "NCS",
						request.getServiceId(), null, null);
			}

			return ResponseEntity.ok(Map.of("status", 200, MESSAGE_KEY, "NCS updated successfully"));
		} catch (Exception e) {
			log.error("Error updating NCS: ", e);
			return ResponseEntity.status(500).body(Map.of(MESSAGE_KEY, "Failed to update NCS", "error", e.getMessage()));
		}
	}

	@Override
	public List<ObjectNode> getNcsDetails() {
		List<Tuple> resultList = ncsRepository.getNcsDetails();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;

	}

	@Override
	public List<ObjectNode> getAlreadyNcsDetailsExist(Integer sector_id, Integer occupation_id, Integer certification_id) {
		List<Tuple> resultList = ncsRepository.getAlreadyNcsDetailsExist(sector_id,occupation_id,certification_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;

	}

	@Override
	public List<ObjectNode> getProgrammeTitleById(Integer programmeId) {
		List<Tuple> resultList = ncsRepository.getProgrammeTitleById(programmeId);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}
}