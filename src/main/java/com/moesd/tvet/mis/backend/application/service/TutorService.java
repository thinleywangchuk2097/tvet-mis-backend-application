package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TutorDto;
import com.moesd.tvet.mis.backend.application.dto.TutorVerifyDto;

public interface TutorService {
	
	ResponseEntity<?> submitTutor(TutorDto request);

	List<ObjectNode> getAllActiveTutors(String instituteId);

	ResponseEntity<?> updateTutor(TutorDto request);

	ResponseEntity<?> softDeleteTutor(Long tutorId);

	List<ObjectNode> getTutorByApplicationNo(String applicationNo);

	ResponseEntity<?> verifyTutor(TutorVerifyDto request);
	
}
