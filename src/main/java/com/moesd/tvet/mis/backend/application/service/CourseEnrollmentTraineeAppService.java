package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CourseEnrollmentTraineeAppdto;
import com.moesd.tvet.mis.backend.application.dto.SelectedTraineedto;

public interface CourseEnrollmentTraineeAppService {
	
	ResponseEntity<?> submitTrainee(CourseEnrollmentTraineeAppdto request);
	
	List<ObjectNode> getCourseAppliedTraineesByApplicationNo(String applicationNo);
	
	List<ObjectNode> getCourseAppliedTraineesReAssessmentByApplicationNo(String applicationNo);
	
	ResponseEntity<?> submitSelectedTrainee(SelectedTraineedto request);
	
	ResponseEntity<?> submitReassessmentTrainees(SelectedTraineedto request);
	
	ResponseEntity<?> updateTraineeApplication(SelectedTraineedto request);

	List<ObjectNode> getFailedTraineeDetails(String userId, String courseId, Integer certificationLevelId);
	
	ResponseEntity<?> selectUnselectTrainee(SelectedTraineedto request);
	
	List<ObjectNode> fetchAssignedAssessors(String applicationNo);
	
	ResponseEntity<?> removeTraineeFromSelectedProgramme(SelectedTraineedto request);
	
	
	
	
}
