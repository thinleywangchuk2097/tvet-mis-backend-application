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
import com.moesd.tvet.mis.backend.application.dto.InstituteNonAccreditedCoursedto;
import com.moesd.tvet.mis.backend.application.service.InstituteNonAccreditedCourseService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/non-accredited-course")
public class InstituteNonAccreditedCourseController {
	
	private final InstituteNonAccreditedCourseService instituteNonAccreditedCourseService;
	
	@PostMapping("/submit")
	public ResponseEntity<?> submitNonAccreditedCourse(@RequestBody InstituteNonAccreditedCoursedto request) {
		return(instituteNonAccreditedCourseService.submitNonAccreditedCourse(request));
	}
	
	@GetMapping("/get-course-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getNonAccreditedCourseByApplicationNo(@PathVariable String application_no){
	    List<ObjectNode> data = instituteNonAccreditedCourseService.getNonAccreditedCourseByApplicationNo(application_no);
	    return ResponseEntity.ok(data);
	}
	
	@PostMapping("/verify-non-accredited-course")
	public ResponseEntity<?> verifyNonAccreditedCourse(@RequestBody InstituteNonAccreditedCoursedto request) {
		return(instituteNonAccreditedCourseService.verifyNonAccreditedCourse(request));
	}
	
	@GetMapping("/get-application-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getNonAccreditedCourseDetailsByUserId(@PathVariable String user_id){
	    List<ObjectNode> Details = instituteNonAccreditedCourseService.getNonAccreditedCourseDetailsByUserId(user_id);
	    return ResponseEntity.ok(Details);
	}
}
