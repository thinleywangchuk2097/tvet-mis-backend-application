package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TutorDto;

public interface TutorService {
	
	ResponseEntity<?> submitTutor(TutorDto request);

	List<ObjectNode> getAllActiveTutors(Integer institute_id);

	ResponseEntity<?> updateTutor(TutorDto request);

	ResponseEntity<?> softDeleteTutor(Long tutorId);
	
	List<ObjectNode> getTutorBySubjectId(Integer institute_id,Integer subject_id);
}
