package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.OJTAgrementDto;
import com.moesd.tvet.mis.backend.application.dto.OJTCompanyDto;
import com.moesd.tvet.mis.backend.application.dto.OJTTraineeDto;

public interface OJTTraineeService {

	ResponseEntity<?> submitOJTCompany(OJTCompanyDto request);
	
	List<ObjectNode> getCompanyByInstituteId(String instituteId);
	
	ResponseEntity<?> submitOJTAgrement(OJTAgrementDto request);
	
	List<ObjectNode> getAgreementByInstituteId(String instituteId);

	ResponseEntity<?> submitOJTTrainee(OJTTraineeDto request);
	
	List<ObjectNode> getTraineeByInstituteId(String instituteId);
	
	List<ObjectNode> getTraineeOJTReport();
	
	
}
