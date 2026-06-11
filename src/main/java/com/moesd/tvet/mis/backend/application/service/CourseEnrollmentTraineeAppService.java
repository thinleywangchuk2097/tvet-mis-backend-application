package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CourseEnrollmentTraineeAppdto;
import com.moesd.tvet.mis.backend.application.dto.SelectedTraineedto;

public interface CourseEnrollmentTraineeAppService {
	
	ResponseEntity<?> submitTrainee(CourseEnrollmentTraineeAppdto request);
	
	List<ObjectNode> getCourseAppliedTraineesByApplicationNo(String application_no);
	
	List<ObjectNode> getCourseAppliedTraineesReAssessmentByApplicationNo(String application_no);
	
	ResponseEntity<?> selectedTrainee(SelectedTraineedto request);
	
	ResponseEntity<?> submitReassessmentTrainees(SelectedTraineedto request);
	
	ResponseEntity<?> updateTraineeApplication(SelectedTraineedto request);

	List<ObjectNode> getFailedTraineeDetails(String user_id, String course_id);
	
	ResponseEntity<?> selectUnselectTrainee(SelectedTraineedto request);
	
}
