package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.ProgramMonitoringDto;

public interface ProgramMonitoringService {
	
	List<ObjectNode> getCourseService();
	
	ResponseEntity<?> submitProgramMonitoring(ProgramMonitoringDto request);
	
	List<ObjectNode> getProgramMonitoring(String userId);
	
	ResponseEntity<?> verifyProgramMonitoring(ProgramMonitoringDto request);
	
	List<ObjectNode> getProgramMonitoringByApplicationNo(String applicationNo);
	
	List<ObjectNode> getCourseByInstituteId(Integer instituteId, Integer courseTypeId);
	
}
