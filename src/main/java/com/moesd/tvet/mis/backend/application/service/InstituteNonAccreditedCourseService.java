package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteNonAccreditedCoursedto;

public interface InstituteNonAccreditedCourseService {
	
	ResponseEntity<?> submitNonAccreditedCourse(InstituteNonAccreditedCoursedto request);
	
	List<ObjectNode> getNonAccreditedCourseByApplicationNo(String application_no);
	
	ResponseEntity<?> verifyNonAccreditedCourse(InstituteNonAccreditedCoursedto request);
	
	List<ObjectNode> getNonAccreditedCourseDetailsByUserId(String user_id);
}
