package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementFirmDto;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementSessionDto;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementTraineeDto;
import com.moesd.tvet.mis.backend.application.exception.RecordNotFoundException;
import com.moesd.tvet.mis.backend.application.model.OnCampusJobPlacementFirm;
import com.moesd.tvet.mis.backend.application.model.OnCampusJobPlacementSession;
import com.moesd.tvet.mis.backend.application.model.OnCampusJobPlacementTrainee;
import com.moesd.tvet.mis.backend.application.repository.OnCampusJobPlacementFirmRepository;
import com.moesd.tvet.mis.backend.application.repository.OnCampusJobPlacementSessionRepository;
import com.moesd.tvet.mis.backend.application.repository.OnCampusJobPlacementTraineeRepository;
import com.moesd.tvet.mis.backend.application.service.OnCampusJobPlacementService;
import com.moesd.tvet.mis.backend.application.utility.DocumentFileUploadService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class OnCampusJobPlacementServiceImpl implements OnCampusJobPlacementService {
	
	private final OnCampusJobPlacementSessionRepository sessionRepository;
	private final OnCampusJobPlacementFirmRepository firmRepository;
	private final OnCampusJobPlacementTraineeRepository traineeRepository;
	private final ObjectToJson objectTojson;
	private final DocumentFileUploadService documentFileUploadService;
	
	
	@Override
	public ResponseEntity<?> submitPlacementSession(OnCampusJobPlacementSessionDto request) {
		if (sessionRepository.existsBySessionName(request.getSessionName())) {
		    return ResponseEntity.status(HttpStatus.CONFLICT)
		            .body(Map.of(
		                    "status", "CONFLICT",
		                    "message", "Session name '" + request.getSessionName() + "' already exists."
		            ));
		}
		
		OnCampusJobPlacementSession session = OnCampusJobPlacementSession.builder()
				.sessionName(request.getSessionName())
				.sessionDate(request.getSessionDate())
				.description(request.getDescription())
				.instituteId(request.getInstituteId())
				.sessionTime(request.getSessionTime())
				.createdBy(request.getCreatedBy())
				.venue(request.getVenue())
				.build();
				
		OnCampusJobPlacementSession saveSession = sessionRepository.save(session);
	 // Documents
	 if (request.getDocuments() != null && request.getDocuments().length > 0) {
	 				documentFileUploadService.saveDocument(request.getDocuments(), null, "on campus job placement",
	 						null, null, null);
	 }
	    return ResponseEntity.ok(saveSession);
	}
	
	@Override
	public List<ObjectNode> getPlacementSessionByInstituteId(String institute_id) {
		List<Tuple> result = sessionRepository.getPlacementSessionByInstituteId(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(result);
		return DtlsJson;
	}
	
	@Override
	public ResponseEntity<?> submitFirm(OnCampusJobPlacementFirmDto request) {
		
		OnCampusJobPlacementSession placementSession = sessionRepository.findById(request.getSessionId())
		        .orElseThrow(() -> new RecordNotFoundException(
		                "placement session not found with id: " + request.getSessionId()));
		
		OnCampusJobPlacementFirm firm = OnCampusJobPlacementFirm.builder()
				.firmName(request.getFirmName())
				.registrationNo(request.getRegistrationNo())
				.address(request.getAddress())
				.contactEmail(request.getContactEmail())
				.dzongkhagId(request.getDzongkhagId())
				.contactPerson(request.getContactPerson())
				.contactEmail(request.getContactEmail())
				.description(request.getDescription())
				.instituteId(request.getInstituteId())
				.createdBy(request.getCreatedBy())
				.session(placementSession)
				.build();
		OnCampusJobPlacementFirm saveFirm = firmRepository.save(firm);
				
		 return ResponseEntity.ok(saveFirm);
	}
	
	@Override
	public List<ObjectNode> getFirmByInstituteId(String institute_id) {
		List<Tuple> result = firmRepository.getFirmByInstituteId(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(result);
		return DtlsJson;
	}
	
	@Override
	public ResponseEntity<?> submitPlacementTrainee(OnCampusJobPlacementTraineeDto request) {
		
		OnCampusJobPlacementFirm placementFirm = firmRepository.findById(request.getFirmId())
		        .orElseThrow(() -> new RecordNotFoundException(
		                "placement session not found with id: " + request.getFirmId()));
		
		OnCampusJobPlacementTrainee trainee = OnCampusJobPlacementTrainee.builder()
				.courseId(request.getCourseId())
				.employmentStatusId(request.getEmploymentStatusId())
				.instituteId(request.getInstituteId())
				.salary(request.getSalary())
				.traineeCid(request.getTraineeCid())
				.traineeName(request.getTraineeName())
				.position(request.getPosition())
				.remarks(request.getRemarks())
				.placementDate(request.getPlacementDate())
				.firm(placementFirm)
				.build();
		OnCampusJobPlacementTrainee saveTrainee = traineeRepository.save(trainee);
		
		return ResponseEntity.ok(saveTrainee);
	}
	
	@Override
	public List<ObjectNode> getTraineeByInstituteId(String institute_id) {
		List<Tuple> result = traineeRepository.getTraineeByInstituteId(institute_id);
		List<ObjectNode> DtlsJson = objectTojson._toJson(result);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getTraineeOnPlacementReport() {
		List<Tuple> result = traineeRepository.getTraineeOnPlacementReport();
		List<ObjectNode> DtlsJson = objectTojson._toJson(result);
		return DtlsJson;
	}
	

}
