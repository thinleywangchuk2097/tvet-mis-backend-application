package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AccreditedCoursedto;
import com.moesd.tvet.mis.backend.application.service.AccreditedCourseService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/accredited-course")
public class AccreditedCourseController {
	
	private final AccreditedCourseService instituteAccreditedCourseService;
	
	@PostMapping("/submit")
	public ResponseEntity<?> registerAccreditedCourse(@RequestBody AccreditedCoursedto request) {
		return (instituteAccreditedCourseService.registerAccreditedCourse(request));
	}
	
	@GetMapping("/get-course-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getAccreditedCourseByApplicationNo(@PathVariable String application_no){
	    List<ObjectNode> data = instituteAccreditedCourseService.getAccreditedCourseByApplicationNo(application_no);
	    return ResponseEntity.ok(data);
	}
	
	@GetMapping("/get-application-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getAccreditedCourseDetailsByUserId(@PathVariable String user_id){
	    List<ObjectNode> Details = instituteAccreditedCourseService.getAccreditedCourseDetailsByUserId(user_id);
	    return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-accredited-course/{institute_id}")
	public ResponseEntity<List<ObjectNode>> getAccreditedCourseByInstituteId(@PathVariable String institute_id){
	    List<ObjectNode> Details = instituteAccreditedCourseService.getAccreditedCourseByInstituteId(institute_id);
	    return ResponseEntity.ok(Details);
	}
	
	@GetMapping("/get-accredited-approved-course-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getAccreditedApprovedCourseByUserId(@PathVariable String user_id){
	    List<ObjectNode> Details = instituteAccreditedCourseService.getAccreditedApprovedCourseByUserId(user_id);
	    return ResponseEntity.ok(Details);
	}
	
	@PostMapping("/verify-accredited-course")
	public ResponseEntity<?> verifyAccreditedCourse(@RequestBody AccreditedCoursedto request) {
		return(instituteAccreditedCourseService.verifyAccreditedCourse(request));
	}
	
	@GetMapping("/get-curriculum-exist/{curriculumId}/{registration_no}")
	public ResponseEntity<List<ObjectNode>> curriculumExist(@PathVariable Long curriculumId, @PathVariable String registration_no){
	    List<ObjectNode> Details = instituteAccreditedCourseService.curriculumExist(curriculumId, registration_no);
	    return ResponseEntity.ok(Details);
	}
}
