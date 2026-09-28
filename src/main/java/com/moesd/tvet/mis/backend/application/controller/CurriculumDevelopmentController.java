package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.CurriculumDevelopmentdto;
import com.moesd.tvet.mis.backend.application.model.CurriculumDevelopment;
import com.moesd.tvet.mis.backend.application.service.CurriculumDevelopmentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/curriculum")
public class CurriculumDevelopmentController {

	private final CurriculumDevelopmentService curriculumDevelopmentService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitCurriculum(@RequestBody CurriculumDevelopmentdto request) {
		return (curriculumDevelopmentService.submitCurriculum(request));
	}

	@GetMapping("/get-curriculum-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getCurriculumDetails(@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> curriculumDetails = curriculumDevelopmentService.getCurriculumDetails(applicationNo);
		return ResponseEntity.ok(curriculumDetails);
	}

	@GetMapping("/get-curriculum-application-details/{user_id}")
	public ResponseEntity<List<ObjectNode>> getCurriculumDetailsByUserId(@PathVariable("user_id") String userId) {
		List<ObjectNode> getCurriculumByUserIdDetails = curriculumDevelopmentService.getCurriculumDetailsByUserId(userId);
		return ResponseEntity.ok(getCurriculumByUserIdDetails);
	}

	@GetMapping("/get-approved-curriculums/{user_id}/{curriculum_type}")
	public ResponseEntity<List<ObjectNode>> getApprovedCurriculumDataByUserId(@PathVariable("user_id") String userId,
			@PathVariable("curriculum_type") String curriculumType) {
		List<ObjectNode> getApprovedCurriculumDataDetails = curriculumDevelopmentService.getApprovedCurriculumDataByUserId(userId,
				curriculumType);
		return ResponseEntity.ok(getApprovedCurriculumDataDetails);
	}

	@GetMapping("/get-curriculums-by-id/{id}")
	public ResponseEntity<CurriculumDevelopment> getCurriculumById(@PathVariable Long id) {
		return ResponseEntity.ok(curriculumDevelopmentService.getCurriculumById(id));
	}

	@PostMapping("/verify-curriculum")
	public ResponseEntity<?> verifyCurriculumDevelopment(@RequestBody CurriculumDevelopmentdto request) {
		return (curriculumDevelopmentService.verifyCurriculumDevelopment(request));
	}
}
