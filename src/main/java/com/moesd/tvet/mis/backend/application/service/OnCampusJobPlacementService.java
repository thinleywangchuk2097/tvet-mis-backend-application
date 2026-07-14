package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementFirmDto;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementSessionDto;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementTraineeDto;

public interface OnCampusJobPlacementService {
	
	ResponseEntity<?> submitPlacementSession(OnCampusJobPlacementSessionDto request);

	List<ObjectNode> getPlacementSessionByInstituteId(String institute_id);

	ResponseEntity<?> submitFirm(OnCampusJobPlacementFirmDto request);

	List<ObjectNode> getFirmByInstituteId(String institute_id);

	ResponseEntity<?> submitPlacementTrainee(OnCampusJobPlacementTraineeDto request);

	List<ObjectNode> getTraineeByInstituteId(String institute_id);
	
	List<ObjectNode> getTraineeOnPlacementReport();
	
}
