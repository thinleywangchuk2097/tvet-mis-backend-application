package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.Privilegedto;
import com.moesd.tvet.mis.backend.application.service.PrivilegeService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/privilege")
public class PrivilegeController {
	
	private final PrivilegeService privilegeService;
	
	@GetMapping("/menu-lists/{roleId}")
	public ResponseEntity<List<Privilegedto>> getPrivileges(@PathVariable String roleId){
		List<Privilegedto> privileges = privilegeService.getPrivileges(roleId);
		return ResponseEntity.ok(privileges); // Automatically converted to JSON
	}
	
	@GetMapping("/parent-privileges-lists")
	public ResponseEntity<List<Privilegedto>> getParentPrivileges(){
		List<Privilegedto> privileges = privilegeService.getParentPrivileges();
		return ResponseEntity.ok(privileges); // Automatically converted to JSON
	}
	
	@GetMapping("/child-privileges-lists/{parentId}")
	public ResponseEntity<List<Privilegedto>> getChildPrivileges(@PathVariable String parentId){
		List<Privilegedto> privileges = privilegeService.getChildPrivileges(parentId);
		return ResponseEntity.ok(privileges); // Automatically converted to JSON
	}
}
