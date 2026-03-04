package com.moesd.tvet.mis.backend.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
		return(instituteProposalService.submitInstituteProposal(request));
	}
}
