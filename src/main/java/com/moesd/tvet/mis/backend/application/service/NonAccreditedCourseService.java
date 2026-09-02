package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.NonAccreditedCoursedto;

public interface NonAccreditedCourseService {
	
	ResponseEntity<?> submitNonAccreditedCourse(NonAccreditedCoursedto request);
	
	List<ObjectNode> getNonAccreditedCourseByApplicationNo(String application_no);
	
	ResponseEntity<?> verifyNonAccreditedCourse(NonAccreditedCoursedto request);
	
	List<ObjectNode> getNonAccreditedCourseDetailsByUserId(String user_id);
	
	List<ObjectNode> getNonAccreditedApprovedCourseByUserId(String user_id);
	
	List<ObjectNode> curriculumAlreadyExist(Long curriculumId, String registration_no);
}
