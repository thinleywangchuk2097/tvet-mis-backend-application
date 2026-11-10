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

	Map<String, Object> getHomeStats();

	List<Map<String, Object>> getNcsPublications();

	List<Map<String, Object>> getCurriculumPublications();

	List<Map<String, Object>> getAssessmentResultsByCid(String cidNo);

	List<Map<String, Object>> getPublicTrainers();

	List<Map<String, Object>> getAccreditedCoursesWithInstitutes();
}
