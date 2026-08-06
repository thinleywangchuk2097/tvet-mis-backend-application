package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StaffManagementDto;

public interface ResourceManagementService {
	
	ResponseEntity<?> submitStaff(StaffManagementDto request);
	
	List<ObjectNode> getInstituteStaff(String instituteId);
	
	ResponseEntity<?> editStaff(StaffManagementDto request);
	
	 ResponseEntity<?> deleteStaff(Long id);
}
