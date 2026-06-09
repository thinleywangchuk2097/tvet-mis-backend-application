package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface MonitoringAssessmentService {
	
	List<ObjectNode> getInstituteTypeDropdown();
	
	List<ObjectNode> getInstituteDropdown(String service_id);
	
}
