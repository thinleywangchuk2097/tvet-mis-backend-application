package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.InstituteRegistrationdto;

import jakarta.persistence.Tuple;

public interface InstituteRegistrationService {
	
	ResponseEntity<?> registerInstitute(InstituteRegistrationdto request);
	
	List<Tuple> applicationExistOrNot(String application_no,String service_id);
	
	List<ObjectNode> getInstituteRegistrationDetails(String application_no);
	
	List<ObjectNode> getInstituteDetails(String registration_no);
	
	ResponseEntity<?> verifyInstituteRegistration(InstituteRegistrationdto request);

}
