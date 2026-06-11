package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;

public interface PublicPageService {
	
	List<Map<String, Object>> trackApplicationStatus(@PathVariable String applicationNo);
	
	List<Map<String, Object>> getOngoingCourses();
	
	List<Map<String, Object>> getAllInstitutes();
	
	List<Map<String, Object>> getCourseBySector();
	
	List<Map<String, Object>> getCourseAnnounceNotifications();
}
