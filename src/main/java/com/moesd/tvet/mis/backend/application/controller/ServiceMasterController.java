package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.ServiceMasterRequestDTO;
import com.moesd.tvet.mis.backend.application.service.ServiceMasterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/service-master")
public class ServiceMasterController {

	private final ServiceMasterService serviceMasterService;

	@PostMapping("/submit")
	public ResponseEntity<?> submitServiceMaster(@RequestBody ServiceMasterRequestDTO request) {
		return (serviceMasterService.submitServiceMaster(request));
	}

	@GetMapping("/get-service-masters")
	public ResponseEntity<?> getAllServiceMaster() {
		List<ObjectNode> allServiceMaster = serviceMasterService.getAllServiceMaster();
		return ResponseEntity.ok(allServiceMaster);
	}

	@PostMapping("/update")
	public ResponseEntity<?> updateServiceMaster(@RequestBody ServiceMasterRequestDTO request) {
		return serviceMasterService.updateServiceMaster(request);
	}

	@PostMapping("/delete/{serviceId}")
	public ResponseEntity<?> softDeleteServiceMaster(@PathVariable Integer serviceId) {
		return serviceMasterService.softDeleteServiceMaster(serviceId);
	}
}
