package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AccreditedCoursedto;



public interface AccreditedCourseService {
	
	ResponseEntity<?> registerAccreditedCourse(AccreditedCoursedto request);
	
	List<ObjectNode> getAccreditedCourseByApplicationNo(String application_no);
	
	List<ObjectNode> getAccreditedCourseDetailsByUserId(String user_id);
	
	List<ObjectNode> getAccreditedCourseByInstituteId(String institute_id);
	
	List<ObjectNode> getAccreditedApprovedCourseByUserId(String user_id);
	
	ResponseEntity<?> verifyAccreditedCourse(AccreditedCoursedto request);
	
	List<ObjectNode> curriculumExist(Long curriculumId, String registration_no);
}
