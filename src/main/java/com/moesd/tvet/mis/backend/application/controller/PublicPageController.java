package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moesd.tvet.mis.backend.application.service.PublicPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/data")
public class PublicPageController {

	private final PublicPageService publicPageService;

	@GetMapping("/application-status/{applicationNo}")
	public List<Map<String, Object>> trackApplicationStatus(@PathVariable String applicationNo) {
		return publicPageService.trackApplicationStatus(applicationNo);
	}
	
	@GetMapping("/ongoing-courses")
	public List<Map<String, Object>> getOngoingCourses() {
		return publicPageService.getOngoingCourses();
	}
	
	@GetMapping("/get-all-institutes")
	public List<Map<String, Object>> getAllInstitutes() {
		return publicPageService.getAllInstitutes();
	}
	
	@GetMapping("/get-course-by-sector")
	public List<Map<String, Object>> getCourseBySector() {
		return publicPageService.getCourseBySector();
	}
	
	@GetMapping("/get-course-announce-notifications")
	public List<Map<String, Object>> getCourseAnnounceNotifications() {
		return publicPageService.getCourseAnnounceNotifications();
	}

}
