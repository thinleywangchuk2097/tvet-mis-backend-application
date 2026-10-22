package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteChangeRequestDto;
import com.moesd.tvet.mis.backend.application.dto.InstituteRegistrationdto;

import jakarta.persistence.Tuple;

public interface InstituteRegistrationService {

	ResponseEntity<?> registerInstitute(InstituteRegistrationdto request);

	List<Tuple> applicationExistOrNot(String applicationNo, String serviceId);

	List<ObjectNode> getInstituteRegistrationDetails(String applicationNo);

	List<ObjectNode> getInstituteDetails(String registrationNo);

	ResponseEntity<?> verifyInstituteRegistration(InstituteRegistrationdto request);

	List<ObjectNode> getInstituteRenewalDetails(String registrationNo);

	List<ObjectNode> getInstituteChangeDetails(String registrationNo);

	ResponseEntity<?> instituteChange(InstituteChangeRequestDto request);

	List<ObjectNode> getInstituteChangeByApplicationNo(String applicationNo);

}
