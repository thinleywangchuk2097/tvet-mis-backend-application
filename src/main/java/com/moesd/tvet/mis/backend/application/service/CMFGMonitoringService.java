package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.moesd.tvet.mis.backend.application.dto.CMFGMonitoringDto;
import com.moesd.tvet.mis.backend.application.dto.CMFGVerifyDto;
import tools.jackson.databind.node.ObjectNode;

public interface CMFGMonitoringService {

	ResponseEntity<?> submitMonitoring(CMFGMonitoringDto request);

	List<ObjectNode> getMonitoringByUserId(String userId);

	ResponseEntity<?> getMonitoringByApplicationNo(String applicationNo);

	ResponseEntity<?> verifyMonitoring(CMFGVerifyDto request);
}
