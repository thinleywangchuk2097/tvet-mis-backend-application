package com.moesd.tvet.mis.backend.application.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.DropdownManagementdto;
import com.moesd.tvet.mis.backend.application.service.DropdownManagementService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/dropdown-management")
public class DropdownManagementController {
	
	private final DropdownManagementService dropdownManagementService;
	
	@PostMapping("/create-dropdown")
	public ResponseEntity<?>createRole(@RequestBody DropdownManagementdto request){
		return (dropdownManagementService.createDropdown(request));
	}
	
	@PostMapping("/update-dropdown")
	public ResponseEntity<?>updateDropdown(@RequestBody DropdownManagementdto request){
		return (dropdownManagementService.updateDropdown(request));
	}
	
	@PostMapping("/delete-dropdown")
	public ResponseEntity<?>deleteDropdown(@RequestBody Map<String, Integer> request){
		return (dropdownManagementService.deleteDropdown(request.get("parentId")));
	}
	
	@GetMapping("/get-dropdown-lists")
	public ResponseEntity<?> getAllDropdownLists() {
	    ResponseEntity<?> privileges = dropdownManagementService.getAllDropdownLists();
	    return ResponseEntity.ok(privileges);
	}
}
