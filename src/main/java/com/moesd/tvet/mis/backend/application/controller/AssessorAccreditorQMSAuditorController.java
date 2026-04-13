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
import com.moesd.tvet.mis.backend.application.dto.AssessorAccreditorQMSAuditordto;
import com.moesd.tvet.mis.backend.application.service.AssessorAccreditorQMSAuditorService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/register")
public class AssessorAccreditorQMSAuditorController {
	
	private final AssessorAccreditorQMSAuditorService assessorAccreditorQMSAuditorService;
	
	@PostMapping("/submit")
	public ResponseEntity<?> registerAssessorAccreditorQMSAuditor(@RequestBody AssessorAccreditorQMSAuditordto request) {
		return (assessorAccreditorQMSAuditorService.registerAssessorAccreditorQMSAuditor(request));
	}
	
	@GetMapping("/get-application-details/{application_no}")
	public ResponseEntity<List<ObjectNode>> getApplicationDetails(@PathVariable String application_no){
	    List<ObjectNode> instituteDetails = assessorAccreditorQMSAuditorService.getApplicationDetails(application_no);
	    return ResponseEntity.ok(instituteDetails);
	}
	
	@PostMapping("/verify-assessor-accreditor-qmsauditor")
	public ResponseEntity<?> verifyAssessorAccreditorQMSAuditor(@RequestBody AssessorAccreditorQMSAuditordto request) {
		return(assessorAccreditorQMSAuditorService.verifyAssessorAccreditorQMSAuditor(request));
	}
}
