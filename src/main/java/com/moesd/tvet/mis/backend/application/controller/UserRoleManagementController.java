package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.RolePrivilegeRequest;
import com.moesd.tvet.mis.backend.application.dto.UserRegisterRequest;
import com.moesd.tvet.mis.backend.application.service.UserRoleManagementService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/")
public class UserRoleManagementController {
	
	private final UserRoleManagementService userRoleManagementService;
	
	@PostMapping("/create-role")
	public ResponseEntity<?>createRole(@RequestBody RolePrivilegeRequest request){
		return (userRoleManagementService.createRole(request));
	}
	
	@GetMapping("/get-role-privileges")
	public ResponseEntity<List<ObjectNode>> getAllPrivilegeRole() {
	    List<ObjectNode> privileges = userRoleManagementService.getAllPrivilegeRole();
	    return ResponseEntity.ok(privileges);
	}
	
	@PostMapping("/edit-role-privileges")
	public ResponseEntity<?>editPrivilegesRole(@RequestBody RolePrivilegeRequest request){
		return (userRoleManagementService.editPrivilegesRole(request));
	}
	
	@PostMapping("/delete-role-privileges")
	public ResponseEntity<?>deletePrivilegesRole(@RequestBody RolePrivilegeRequest request){
		return (userRoleManagementService.deletePrivilegesRole(request));
	}
	
	@GetMapping("/get-roles")
	public ResponseEntity<List<ObjectNode>> getRoles() {
	    List<ObjectNode> privileges = userRoleManagementService.getRoles();
	    return ResponseEntity.ok(privileges);
	}
	
	@PostMapping("/create-user")
	public ResponseEntity<?>createUser(@RequestBody UserRegisterRequest request){
		return (userRoleManagementService.createUser(request));
	}
	
	@PostMapping("/edit-user")
	public ResponseEntity<?>editUser(@RequestBody UserRegisterRequest request){
		return (userRoleManagementService.editUser(request));
	}
	
	@PostMapping("/delete-user")
	public ResponseEntity<?>deleteUser(@RequestBody UserRegisterRequest request){
		return (userRoleManagementService.deleteUser(request));
	}
	
	@GetMapping("/get-all-users")
	public ResponseEntity<List<ObjectNode>> getAllUsers() {
	    List<ObjectNode> privileges = userRoleManagementService.getAllUsers();
	    return ResponseEntity.ok(privileges);
	}
}
