package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteProposaldto;

public interface InstituteProposalService {

	ResponseEntity<?> submitInstituteProposal(InstituteProposaldto request);

	List<ObjectNode> getInstituteDetails(String applicationNo);
	
	ResponseEntity<?> verifyInstituteProposal(InstituteProposaldto request);
	
	List<ObjectNode> getApplicantDetails(String applicationNo);

	/** Message when a (not rejected) proposal already uses this institute name; null when the name is free */
	String checkDuplicateName(String proposedInstituteName);
}
