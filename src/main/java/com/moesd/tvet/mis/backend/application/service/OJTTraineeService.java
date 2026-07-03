package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.OJTAgrementDto;
import com.moesd.tvet.mis.backend.application.dto.OJTCompanyDto;
import com.moesd.tvet.mis.backend.application.dto.OJTTraineeDto;

public interface OJTTraineeService {

	ResponseEntity<?> submitOJTCompany(OJTCompanyDto request);
	
	List<ObjectNode> getCompanyByInstituteId(String institute_id);
	
	ResponseEntity<?> submitOJTAgrement(OJTAgrementDto request);
	
	List<ObjectNode> getAgreementByInstituteId(String institute_id);

	ResponseEntity<?> submitOJTTrainee(OJTTraineeDto request);
	
	List<ObjectNode> getTraineeByInstituteId(String institute_id);
	
}
