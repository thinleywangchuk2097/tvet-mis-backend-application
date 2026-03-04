package com.moesd.tvet.mis.backend.application.service;

import org.springframework.http.ResponseEntity;
import com.moesd.tvet.mis.backend.application.dto.InstituteProposaldto;

public interface InstituteProposalService {

	ResponseEntity<?> submitInstituteProposal(InstituteProposaldto request);
}
