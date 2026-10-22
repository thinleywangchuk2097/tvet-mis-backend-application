package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.NonAccreditedCoursedto;

public interface NonAccreditedCourseService {
	
	ResponseEntity<?> submitNonAccreditedCourse(NonAccreditedCoursedto request);
	
	List<ObjectNode> getNonAccreditedCourseByApplicationNo(String applicationNo);
	
	ResponseEntity<?> verifyNonAccreditedCourse(NonAccreditedCoursedto request);
	
	List<ObjectNode> getNonAccreditedCourseDetailsByUserId(String userId);
	
	List<ObjectNode> getNonAccreditedApprovedCourseByUserId(String userId);
	
	List<ObjectNode> curriculumAlreadyExist(Long curriculumId, String registrationNo);
}
