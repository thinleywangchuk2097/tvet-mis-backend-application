package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AddTrainerDto;


public interface AddTrainerService {
	
	ResponseEntity<?> submitTrainer(AddTrainerDto request);

	List<ObjectNode> getAllTrainer(Integer instituteId);

	ResponseEntity<?> updateTrainer(AddTrainerDto request);

	ResponseEntity<?> softDeleteTrainer(Long trainerId);

	/**
	 * Message when this CID / work permit no can't be added for the institute: already registered
	 * there, or still active at another institute. Null when it is free. excludeId = trainer being edited.
	 */
	String checkTrainerConflict(String citizenId, String workPermitNo, Integer instituteId, Long excludeId);

	/**
	 * Lead trainer lookup for programme accreditation: the institute's own active trainer with this CID.
	 * Body: found, message, data (name, genderId, qualificationId, workExperience).
	 */
	Map<String, Object> findInstituteTrainer(String citizenId, Integer instituteId);
}
