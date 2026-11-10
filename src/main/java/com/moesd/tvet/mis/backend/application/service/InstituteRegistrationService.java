package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteChangeRequestDto;
import com.moesd.tvet.mis.backend.application.dto.InstituteChangeVerifyDto;
import com.moesd.tvet.mis.backend.application.dto.InstituteRegistrationdto;

import jakarta.persistence.Tuple;

public interface InstituteRegistrationService {

	ResponseEntity<?> registerInstitute(InstituteRegistrationdto request);

	List<Tuple> applicationExistOrNot(String applicationNo, String serviceId);

	List<ObjectNode> getInstituteRegistrationDetails(String applicationNo);

	List<ObjectNode> getInstituteDetails(String registrationNo);

	List<ObjectNode> getRegisteredInstitutesWithCourses();

	ResponseEntity<?> verifyInstituteRegistration(InstituteRegistrationdto request);

	List<ObjectNode> getInstituteRenewalDetails(String registrationNo);

	List<ObjectNode> getInstituteChangeDetails(String registrationNo);

	ResponseEntity<?> instituteChange(InstituteChangeRequestDto request);

	List<ObjectNode> getInstituteChangeByApplicationNo(String applicationNo);

	ResponseEntity<?> verifyInstituteChange(InstituteChangeVerifyDto request);

	/** Renewal preview: latest SAR grade, renewal years and resulting validity date. */
	Map<String, Object> getRenewalValidity(String registrationNo, Integer serviceId);

}
