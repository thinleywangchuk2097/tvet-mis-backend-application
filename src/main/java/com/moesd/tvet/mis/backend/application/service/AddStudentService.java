package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StudentAddDto;


public interface AddStudentService {
	
	ResponseEntity<?> submitStudent(StudentAddDto request);

	List<ObjectNode> getAllActiveStudents(Integer institute_id);

	ResponseEntity<?> updateStudent(StudentAddDto request);

	ResponseEntity<?> softDeleteStudent(Long studentId);
}
