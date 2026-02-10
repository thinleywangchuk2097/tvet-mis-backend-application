package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.RolePrivilegeRequest;
import com.moesd.tvet.mis.backend.application.dto.UserRegisterRequest;

public interface UserRoleManagementService {
	
	ResponseEntity<?> createRole(RolePrivilegeRequest rquest);
	
	List<ObjectNode> getAllPrivilegeRole();
	
	ResponseEntity<?> editPrivilegesRole(RolePrivilegeRequest rquest);
	
	ResponseEntity<?> deletePrivilegesRole(RolePrivilegeRequest rquest);
	
	List<ObjectNode> getRoles();
	
	ResponseEntity<?> createUser(UserRegisterRequest request);
	
	ResponseEntity<?> editUser(UserRegisterRequest request);
	
	ResponseEntity<?> deleteUser(UserRegisterRequest request);
	
	List<ObjectNode> getAllUsers();
}
