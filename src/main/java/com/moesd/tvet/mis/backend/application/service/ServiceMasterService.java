package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.ServiceMasterRequestDTO;

public interface ServiceMasterService {

	ResponseEntity<?> submitServiceMaster(ServiceMasterRequestDTO request);

	List<ObjectNode> getAllServiceMaster();

	ResponseEntity<?> updateServiceMaster(ServiceMasterRequestDTO request);

	ResponseEntity<?> softDeleteServiceMaster(Integer serviceId);
}
