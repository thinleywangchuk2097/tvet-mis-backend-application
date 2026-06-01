package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SurveyResponseRequestDTO.ResponseItem;
import com.moesd.tvet.mis.backend.application.dto.TracerQuestionGeneratorRequest;
import com.moesd.tvet.mis.backend.application.dto.TracerSendRequestDTO;
import com.moesd.tvet.mis.backend.application.model.TracerQuestionGenerator;
import com.moesd.tvet.mis.backend.application.model.TracerSurveyResponseDetails;
import com.moesd.tvet.mis.backend.application.model.TracerSurveySendDetails;

public interface TracerQuestionGeneratorService {

	List<ObjectNode> getTracerQuestionDropdownType();

	List<ObjectNode> getParentTracerTypes();

	List<TracerQuestionGenerator> saveTracerQuestions(TracerQuestionGeneratorRequest request);

	List<ObjectNode> getTracerDetailsByApplicationNo(String application_no);

	List<ObjectNode> getTracerAllApplications();

	ResponseEntity<?> sendTraineeTracerSurvey(TracerSendRequestDTO request);

	ResponseEntity<?> sendEmployerTracerSurvey(TracerSendRequestDTO request);

	TracerSurveySendDetails getSurveyByUniqueId(String uniqueId);

	 List<TracerSurveyResponseDetails> saveSurveyResponses(String applicationNo, 
	            List<ResponseItem> responses);

}
