package com.moesd.tvet.mis.backend.application.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SurveyResponseRequestDTO;
import com.moesd.tvet.mis.backend.application.model.TracerSurveyResponseDetails;
import com.moesd.tvet.mis.backend.application.model.TracerSurveySendDetails;
import com.moesd.tvet.mis.backend.application.service.TracerQuestionGeneratorService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/tracer")
public class PublicTracerController {
	
    private final TracerQuestionGeneratorService tracerQuestionGeneratorService;
    
	@GetMapping("/survey/{uniqueId}")
	public ResponseEntity<?> getSurveyByUniqueId(@PathVariable String uniqueId) {
		try {
			TracerSurveySendDetails survey = tracerQuestionGeneratorService.getSurveyByUniqueId(uniqueId);
			if (survey == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body(Map.of("message", "Survey not found", "timestamp", LocalDateTime.now()));
			}

			return ResponseEntity.ok(Map.of("success", true, "data", survey, "timestamp", LocalDateTime.now()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to fetch survey");
		}
	}
	
	@GetMapping("/get-tracer/{application_no}")
	public ResponseEntity<?> getTracerDetailsByApplicationNo(@PathVariable String application_no){
		try {
			List<ObjectNode> tracerDetails = tracerQuestionGeneratorService.getTracerDetailsByApplicationNo(application_no);
			return ResponseEntity.ok(Map.of("success", true, "data", tracerDetails));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to fetch tracer details");
		}
	}
	
	@GetMapping("/get-tracer-question-dropdown")
	public ResponseEntity<?> getTracerQuestionDropdownType() {
		try {
			List<ObjectNode> data = tracerQuestionGeneratorService.getTracerQuestionDropdownType();
			return ResponseEntity.ok(Map.of("success", true, "data", data));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to fetch dropdown data");
		}
	}
	
	@PostMapping("/submit-survey-response")
	public ResponseEntity<?> submitSurveyResponse(@RequestBody SurveyResponseRequestDTO request) {
		try {
			List<TracerSurveyResponseDetails> savedResponses = tracerQuestionGeneratorService.saveSurveyResponses(
					request.getApplicationNo(), 
					request.getResponses());
			
			return ResponseEntity.ok(Map.of(
					"success", true, 
					"message", "Survey responses submitted successfully",
					"data", savedResponses,
					"timestamp", LocalDateTime.now()));
			
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to submit survey responses");
		}
	}
}