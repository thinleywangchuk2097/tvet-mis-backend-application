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
	public ResponseEntity<List<ObjectNode>> getTracerQuestionDropdownType() {
		List<ObjectNode> data = tracerQuestionGeneratorService.getTracerQuestionDropdownType();
		return ResponseEntity.ok(data);
	}
	
	@GetMapping("/get-parent-tracer-types")
	public ResponseEntity<List<ObjectNode>> getParentTracerTypes() {
		List<ObjectNode> data = tracerQuestionGeneratorService.getParentTracerTypes();
		return ResponseEntity.ok(data);
	}

	@PostMapping("/save-tracer-questions")
	public ResponseEntity<ObjectNode> saveTracerQuestions(@RequestBody TracerQuestionGeneratorRequest request) {
	    try {
	        tracerQuestionGeneratorService.saveTracerQuestions(request);
	        // Create response with only applicationNo and message
	        ObjectNode response = JsonNodeFactory.instance.objectNode();
	        //response.put("applicationNo", ((TracerQuestionGeneratorRequest) savedData).getApplicationNo());
	        response.put("message", "Tracer questions saved successfully");
	        response.put("success", true);
	        
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        e.printStackTrace();
	        
	        // Create error response
	        ObjectNode errorResponse = JsonNodeFactory.instance.objectNode();
	        errorResponse.put("success", false);
	        errorResponse.put("message", "Failed to save tracer questions: " + e.getMessage());
	        
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	    }
	}
	
	@GetMapping("/get-tracer/{application_no}")
	public ResponseEntity<List<ObjectNode>> getTracerDetailsByApplicationNo(@PathVariable String application_no){
	    List<ObjectNode> tracerDetails = tracerQuestionGeneratorService.getTracerDetailsByApplicationNo(application_no);
	    return ResponseEntity.ok(tracerDetails);
	}
	
	@GetMapping("/get-all-tracers")
	public ResponseEntity<List<ObjectNode>> getTracerAllApplications(){
	    List<ObjectNode> tracerDetails = tracerQuestionGeneratorService.getTracerAllApplications();
	    return ResponseEntity.ok(tracerDetails);
	}
	
	@PostMapping("/send-trainee-survey")
	public ResponseEntity<?> sendTraineeTracerSurvey(@RequestBody TracerSendRequestDTO request) {
	    return tracerQuestionGeneratorService.sendTraineeTracerSurvey(request);
	}

	@PostMapping("/send-employer-survey")
	public ResponseEntity<?> sendEmployerTracerSurvey(@RequestBody TracerSendRequestDTO request) {
	    return tracerQuestionGeneratorService.sendEmployerTracerSurvey(request);
	}

}
