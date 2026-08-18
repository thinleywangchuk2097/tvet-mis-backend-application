package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.TracerQuestionGeneratorRequest;
import com.moesd.tvet.mis.backend.application.dto.TracerSendRequestDTO;
import com.moesd.tvet.mis.backend.application.service.TracerQuestionGeneratorService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/tracer")
public class TracerQuestionGeneratorController {

	private final TracerQuestionGeneratorService tracerQuestionGeneratorService;

	@GetMapping("/get-tracer-question-dropdown")
	public ResponseEntity<?> getTracerQuestionDropdownType() {
		try {
			List<ObjectNode> data = tracerQuestionGeneratorService.getTracerQuestionDropdownType();
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to fetch tracer question dropdown");
		}
	}
	
	@GetMapping("/get-parent-tracer-types")
	public ResponseEntity<?> getParentTracerTypes() {
		try {
			List<ObjectNode> data = tracerQuestionGeneratorService.getParentTracerTypes();
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to fetch parent tracer types");
		}
	}

	@PostMapping("/save-tracer-questions")
	public ResponseEntity<?> saveTracerQuestions(@RequestBody TracerQuestionGeneratorRequest request) {
		try {
			tracerQuestionGeneratorService.saveTracerQuestions(request);
			
			ObjectNode response = JsonNodeFactory.instance.objectNode();
			response.put("message", "Tracer questions saved successfully");
			response.put("success", true);
			
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to save tracer questions");
		}
	}
	
	@GetMapping("/get-tracer/{application_no}")
	public ResponseEntity<?> getTracerDetailsByApplicationNo(@PathVariable String application_no) {
		try {
			List<ObjectNode> tracerDetails = tracerQuestionGeneratorService.getTracerDetailsByApplicationNo(application_no);
			return ResponseEntity.ok(tracerDetails);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to fetch tracer details");
		}
	}
	
	@GetMapping("/get-all-tracers")
	public ResponseEntity<?> getTracerAllApplications() {
		try {
			List<ObjectNode> tracerDetails = tracerQuestionGeneratorService.getTracerAllApplications();
			return ResponseEntity.ok(tracerDetails);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to fetch all tracers");
		}
	}
	
	@PostMapping("/send-trainee-survey")
	public ResponseEntity<?> sendTraineeTracerSurvey(@RequestBody TracerSendRequestDTO request) {
		try {
			return tracerQuestionGeneratorService.sendTraineeTracerSurvey(request);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to send trainee survey");
		}
	}

	@PostMapping("/send-employer-survey")
	public ResponseEntity<?> sendEmployerTracerSurvey(@RequestBody TracerSendRequestDTO request) {
		try {
			return tracerQuestionGeneratorService.sendEmployerTracerSurvey(request);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to send employer survey");
		}
	}
}