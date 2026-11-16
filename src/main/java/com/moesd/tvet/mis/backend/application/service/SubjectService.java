package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SubjectDto;
import com.moesd.tvet.mis.backend.application.dto.SubjectVerifyDto;

public interface SubjectService {

	ResponseEntity<?> submitSubject(SubjectDto request);

	List<ObjectNode> getAllActiveSubjects(String instituteId);

	ResponseEntity<?> updateSubject(SubjectDto request);

	ResponseEntity<?> softDeleteSubject(Integer subjectId);

	List<ObjectNode> getSubjectByApplicationNo(String applicationNo);

	ResponseEntity<?> verifySubject(SubjectVerifyDto request);
}
