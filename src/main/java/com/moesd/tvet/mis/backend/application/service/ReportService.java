package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;

public interface ReportService {
	
	 List<Map<String, Object>> getcourseData();
	 
	 List<Map<String, Object>> courseServiceType();
	 
	 List<Map<String, Object>> getAllInstitutes();
	 
	 List<Map<String, Object>> getInstitutesProposalType();
	 
	 List<Map<String, Object>> getInstituteRegistrationType();
	 
	 List<Map<String, Object>> getInstitutesProposalDetails();
	 
	 List<Map<String, Object>> getInstitutesRegistrationDetails();
	 
	 List<Map<String, Object>> getInstitutesTraineesDetails();
	 
	 

}
