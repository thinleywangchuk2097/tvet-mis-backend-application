package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.AssessorAccreditorQMSAuditordto;

public interface AssessorAccreditorQMSAuditorService {
	
	ResponseEntity<?> registerAssessorAccreditorQMSAuditor(AssessorAccreditorQMSAuditordto request);
	
	List<ObjectNode> getApplicationDetails(String applicationNo);
	
	ResponseEntity<?> verifyAssessorAccreditorQMSAuditor(AssessorAccreditorQMSAuditordto request);
	
	List<ObjectNode> getApplicationByCitizenIdOrReferenceNo(String citizenId, String referenceNo, String serviceId);

	List<ObjectNode> getRegisteredByServiceId(Integer serviceId);

	/**
	 * Message when this CID / reference no already has a (not rejected) application for the service; null if none.
	 * renewal = true: only an application still in process blocks (an approved registration may renew).
	 */
	String checkDuplicateApplication(Integer serviceId, String citizenId, String referenceNo, boolean renewal);
	
}
