package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StudentEnrollmentDto;

public interface StudentEnrollmentService {

	ResponseEntity<?> submitEnrollment(StudentEnrollmentDto request);

	List<ObjectNode> getAllActiveEnrollments(Integer instituteId);

	ResponseEntity<?> updateEnrollment(StudentEnrollmentDto request);

	ResponseEntity<?> softDeleteEnrollment(Long id);
}
