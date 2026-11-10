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

	// Headline figures for the home page "TVET by the numbers" card
	@GetMapping("/home-stats")
	public Map<String, Object> getHomeStats() {
		return publicPageService.getHomeStats();
	}

	// Public NCS publication list
	@GetMapping("/ncs-publications")
	public List<Map<String, Object>> getNcsPublications() {
		return publicPageService.getNcsPublications();
	}

	// Public curriculum publication list (Endorsed only)
	@GetMapping("/curriculum-publications")
	public List<Map<String, Object>> getCurriculumPublications() {
		return publicPageService.getCurriculumPublications();
	}

	// Public assessment result lookup by CID / reference no
	@GetMapping("/assessment-results/{cid_no}")
	public List<Map<String, Object>> getAssessmentResultsByCid(@PathVariable("cid_no") String cidNo) {
		return publicPageService.getAssessmentResultsByCid(cidNo == null ? "" : cidNo.trim());
	}

	// Public trainer report (trainers added through AddTrainer)
	@GetMapping("/trainers")
	public List<Map<String, Object>> getPublicTrainers() {
		return publicPageService.getPublicTrainers();
	}

	// Public "Courses Accredited" report (approved accredited courses with institutes)
	@GetMapping("/accredited-courses")
	public List<Map<String, Object>> getAccreditedCoursesWithInstitutes() {
		return publicPageService.getAccreditedCoursesWithInstitutes();
	}
}
