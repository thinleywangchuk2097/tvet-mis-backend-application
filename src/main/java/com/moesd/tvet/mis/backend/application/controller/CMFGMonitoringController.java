package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.CMFGMonitoringDto;
import com.moesd.tvet.mis.backend.application.dto.CMFGVerifyDto;
import com.moesd.tvet.mis.backend.application.service.CMFGMonitoringService;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.node.ObjectNode;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/cmfg-monitoring")
public class CMFGMonitoringController {

	private final CMFGMonitoringService cmfgMonitoringService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitMonitoring(@RequestBody CMFGMonitoringDto request) {
		return cmfgMonitoringService.submitMonitoring(request);
	}

	/** Monitoring applications of the logged-in institute (userId = institute registration no). */
	@GetMapping("/get-by-user/{userId}")
	public ResponseEntity<List<ObjectNode>> getMonitoringByUserId(@PathVariable String userId) {
		return ResponseEntity.ok(cmfgMonitoringService.getMonitoringByUserId(userId));
	}

	@GetMapping("/get-by-application-no/{applicationNo}")
	public ResponseEntity<?> getMonitoringByApplicationNo(@PathVariable String applicationNo) {
		return cmfgMonitoringService.getMonitoringByApplicationNo(applicationNo);
	}

	/** Reviewer action from the task list (verify). */
	@PostMapping("/verify")
	public ResponseEntity<?> verifyMonitoring(@RequestBody CMFGVerifyDto request) {
		return cmfgMonitoringService.verifyMonitoring(request);
	}

}
