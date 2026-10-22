package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AddTrainerDto;


public interface AddTrainerService {
	
	ResponseEntity<?> submitTrainer(AddTrainerDto request);

	List<ObjectNode> getAllTrainer(Integer instituteId);

	ResponseEntity<?> updateTrainer(AddTrainerDto request);

	ResponseEntity<?> softDeleteTrainer(Long trainerId);
}
