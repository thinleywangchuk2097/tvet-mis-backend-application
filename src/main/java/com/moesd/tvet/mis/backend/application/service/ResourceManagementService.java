package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import tools.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.StaffManagementDto;

public interface ResourceManagementService {
	
	ResponseEntity<?> submitStaff(StaffManagementDto request);

	/** Message when this CID / reference no is already staff of the institute, otherwise null. */
	String checkStaffDuplicate(Integer instituteId, String citizenId, String referenceNo, Long excludeId);
	
	List<ObjectNode> getInstituteStaff(String instituteId);
	
	ResponseEntity<?> editStaff(StaffManagementDto request);
	
	 ResponseEntity<?> deleteStaff(Long id);
}
