package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementFirmDto;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementSessionDto;
import com.moesd.tvet.mis.backend.application.dto.OnCampusJobPlacementTraineeDto;

public interface OnCampusJobPlacementService {
	
	ResponseEntity<?> submitPlacementSession(OnCampusJobPlacementSessionDto request);

	List<ObjectNode> getPlacementSessionByInstituteId(String instituteId);

	ResponseEntity<?> submitFirm(OnCampusJobPlacementFirmDto request);

	List<ObjectNode> getFirmByInstituteId(String instituteId);

	ResponseEntity<?> submitPlacementTrainee(OnCampusJobPlacementTraineeDto request);

	List<ObjectNode> getTraineeByInstituteId(String instituteId);
	
	List<ObjectNode> getTraineeOnPlacementReport();
	
}
