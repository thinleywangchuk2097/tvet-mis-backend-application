package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AssessorAccreditorQMSAuditordto;

public interface AssessorAccreditorQMSAuditorService {
	
	ResponseEntity<?> registerAssessorAccreditorQMSAuditor(AssessorAccreditorQMSAuditordto request);
	
	List<ObjectNode> getApplicationDetails(String application_no);
	
	ResponseEntity<?> verifyAssessorAccreditorQMSAuditor(AssessorAccreditorQMSAuditordto request);
	
	
}
