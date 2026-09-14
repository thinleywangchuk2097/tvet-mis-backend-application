package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AssessorAccreditorQMSAuditordto;

public interface AssessorAccreditorQMSAuditorService {
	
	ResponseEntity<?> registerAssessorAccreditorQMSAuditor(AssessorAccreditorQMSAuditordto request);
	
	List<ObjectNode> getApplicationDetails(String application_no);
	
	ResponseEntity<?> verifyAssessorAccreditorQMSAuditor(AssessorAccreditorQMSAuditordto request);
	
	List<ObjectNode> getApplicationByCitizenIdOrReferenceNo(String citizenId, String referenceNo, String serviceId);
	
}
