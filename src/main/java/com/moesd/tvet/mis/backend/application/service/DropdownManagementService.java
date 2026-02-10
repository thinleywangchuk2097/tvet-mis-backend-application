package com.moesd.tvet.mis.backend.application.service;

import org.springframework.http.ResponseEntity;
import com.moesd.tvet.mis.backend.application.dto.DropdownManagementdto;

public interface DropdownManagementService {
	
	ResponseEntity<?> createDropdown(DropdownManagementdto rquest);
	
	ResponseEntity<?> updateDropdown(DropdownManagementdto rquest);
	
	ResponseEntity<?> deleteDropdown(Integer parentId);
	
	ResponseEntity<?> getAllDropdownLists();
}
