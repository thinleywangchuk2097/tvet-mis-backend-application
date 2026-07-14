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
		System.out.println("InstituteProposaldto" + request);
		return(instituteProposalService.submitInstituteProposal(request));
	}
	
	@GetMapping("/get-institute-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getInstituteDetails(@PathVariable String application_no){
	    List<ObjectNode> instituteDetails = instituteProposalService.getInstituteDetails(application_no);
	    return ResponseEntity.ok(instituteDetails);
	}
	
	@PostMapping("/verify-institute-proposal")
	public ResponseEntity<?> verifyInstituteProposal(@RequestBody InstituteProposaldto request) {
		return(instituteProposalService.verifyInstituteProposal(request));
	}
	
	
}
