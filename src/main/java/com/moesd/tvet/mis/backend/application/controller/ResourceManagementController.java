package com.moesd.tvet.mis.backend.application.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StaffManagementDto;
import com.moesd.tvet.mis.backend.application.service.ResourceManagementService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resource-management")
public class ResourceManagementController {

	private final ResourceManagementService resourceManagementService;

	@PostMapping("/submit-staff")
	public ResponseEntity<?> submitStaff(@RequestBody StaffManagementDto request) {
		return (resourceManagementService.submitStaff(request));
	}

	@GetMapping("/get-staff/{instituteId}")
	public ResponseEntity<List<ObjectNode>> getInstituteStaff(@PathVariable String instituteId) {
		List<ObjectNode> instituteStaffDetails = resourceManagementService.getInstituteStaff(instituteId);
		return ResponseEntity.ok(instituteStaffDetails);
	}

	// Staff form: is this CID / reference no already staff of the institute?
	@GetMapping("/check-staff-duplicate")
	public ResponseEntity<Map<String, Object>> checkStaffDuplicate(@RequestParam Integer instituteId,
			@RequestParam(required = false) String citizenId, @RequestParam(required = false) String referenceNo,
			@RequestParam(required = false) Long excludeId) {
		String message = resourceManagementService.checkStaffDuplicate(instituteId, citizenId, referenceNo, excludeId);
		Map<String, Object> body = new HashMap<>();
		body.put("exists", message != null);
		body.put("message", message);
		return ResponseEntity.ok(body);
	}

	@PostMapping("/edit-staff")
	public ResponseEntity<?> editStaff(@RequestBody StaffManagementDto request) {
		return (resourceManagementService.editStaff(request));
	}

	@DeleteMapping("/staff/{id}")
	public ResponseEntity<?> deleteStaff(@PathVariable Long id) {
		return resourceManagementService.deleteStaff(id);
	}
}
