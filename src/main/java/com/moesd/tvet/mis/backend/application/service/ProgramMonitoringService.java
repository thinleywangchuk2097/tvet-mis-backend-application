package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.ProgramMonitoringDto;

public interface ProgramMonitoringService {
	
	List<ObjectNode> getCourseService();
	
	ResponseEntity<?> submitProgramMonitoring(ProgramMonitoringDto request);
	
	List<ObjectNode> getProgramMonitoring(String user_id);
	
	ResponseEntity<?> verifyProgramMonitoring(ProgramMonitoringDto request);
	
	List<ObjectNode> getProgramMonitoringByApplicationNo(String applicationNo);
	
	List<ObjectNode> getCourseByInstituteId(Integer institute_id, Integer course_type_id);
	
}
