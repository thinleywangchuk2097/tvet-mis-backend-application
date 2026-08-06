package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StaffManagementDto;
import com.moesd.tvet.mis.backend.application.service.ResourceManagementService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/resource-management")
public class ResourceManagementController {

	private final ResourceManagementService resourceManagementService;

	@PostMapping("/submit-staff")
	public ResponseEntity<?> submitStaff(@RequestBody StaffManagementDto request) {
		return (resourceManagementService.submitStaff(request));
	}

	@GetMapping("/get-staff/{instituteId}")
	public ResponseEntity<List<ObjectNode>> getInstituteStaff(@PathVariable String instituteId) {
		List<ObjectNode> Details = resourceManagementService.getInstituteStaff(instituteId);
		return ResponseEntity.ok(Details);
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
