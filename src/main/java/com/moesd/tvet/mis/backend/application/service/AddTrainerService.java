package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AddTrainerDto;


public interface AddTrainerService {
	
	ResponseEntity<?> submitTrainer(AddTrainerDto request);

	List<ObjectNode> getAllTrainer(Integer institute_id);

	ResponseEntity<?> updateTrainer(AddTrainerDto request);

	ResponseEntity<?> softDeleteTrainer(Long trainerId);
}
