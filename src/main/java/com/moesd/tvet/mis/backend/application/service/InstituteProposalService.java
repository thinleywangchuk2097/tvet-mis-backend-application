package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteProposaldto;

public interface InstituteProposalService {

	ResponseEntity<?> submitInstituteProposal(InstituteProposaldto request);

	List<ObjectNode> getInstituteDetails(String application_no);

	ResponseEntity<?> verifyInstituteProposal(InstituteProposaldto request);
}
