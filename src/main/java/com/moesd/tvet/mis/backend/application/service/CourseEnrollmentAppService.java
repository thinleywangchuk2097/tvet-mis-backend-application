package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CourseEnrollmentAppdto;

public interface CourseEnrollmentAppService {
	
	ResponseEntity<?> submitCourseAnnouncement(CourseEnrollmentAppdto request);
	
	List<ObjectNode> getCourseDetailsAnnouncementByUserId(String user_id, String service_id);
	
	List<ObjectNode> getReAssessmentServiceName();
	
	
	
	
}
