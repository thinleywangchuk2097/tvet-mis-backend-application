package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SubjectDto;


public interface SubjectService {
	
	ResponseEntity<?> submitSubject(SubjectDto request);
	
	List<ObjectNode> getAllActiveSubjects(Integer institute_id);

	ResponseEntity<?> updateSubject(SubjectDto request);

	ResponseEntity<?> softDeleteSubject(Integer subjectId);
}
