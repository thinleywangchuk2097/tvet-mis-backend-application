package com.moesd.tvet.mis.backend.application.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteProposaldto;
import com.moesd.tvet.mis.backend.application.service.InstituteProposalService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/institute-proposal")
public class InstituteProposalController {

	private final InstituteProposalService instituteProposalService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitInstituteProposal(@RequestBody InstituteProposaldto request) {
		return (instituteProposalService.submitInstituteProposal(request));
	}

	@GetMapping("/get-institute-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteDetails(@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> instituteDetails = instituteProposalService.getInstituteDetails(applicationNo);
		return ResponseEntity.ok(instituteDetails);
	}

	@PostMapping("/verify-institute-proposal")
	public ResponseEntity<?> verifyInstituteProposal(@RequestBody InstituteProposaldto request) {
		return (instituteProposalService.verifyInstituteProposal(request));
	}

	// Proposal form: is the proposed institute name already used?
	@GetMapping("/check-name")
	public ResponseEntity<Map<String, Object>> checkDuplicateName(@RequestParam String name) {
		String message = instituteProposalService.checkDuplicateName(name);
		Map<String, Object> body = new HashMap<>();
		body.put("exists", message != null);
		body.put("message", message);
		return ResponseEntity.ok(body);
	}

	@PreAuthorize("hasAnyAuthority('7')")
	@GetMapping("/get-applicant-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getApplicantDetails(@PathVariable("application_no") String applicationNo) {
		List<ObjectNode> instituteDetails = instituteProposalService.getApplicantDetails(applicationNo);
		return ResponseEntity.ok(instituteDetails);
	}

}
