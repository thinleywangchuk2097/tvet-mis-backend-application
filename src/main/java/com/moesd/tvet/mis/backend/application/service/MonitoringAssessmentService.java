package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.MonitoringAssessmentDto;

public interface MonitoringAssessmentService {
	
	List<ObjectNode> getInstituteTypeDropdown();
	
	List<ObjectNode> getInstituteDropdown(String service_id);
	
	ResponseEntity<?> submitMonitoringAssessment(MonitoringAssessmentDto request);
	
	List<ObjectNode> getMonitoringAssessment(String user_id);
	
	ResponseEntity<?> verifyMonitoringAssessment(MonitoringAssessmentDto request);
	
	List<ObjectNode> getMonitoringAssessmentByApplicationNo(String applicationNo);
	
}
