package com.moesd.tvet.mis.backend.application.service;

import org.springframework.http.ResponseEntity;
import com.moesd.tvet.mis.backend.application.dto.SwitchRoleRequest;
import com.moesd.tvet.mis.backend.application.dto.UserProfileRequest;
import com.moesd.tvet.mis.backend.application.dto.UserProfileResponse;



public interface UserProfileService {
	
	ResponseEntity<?> updateUserProfile(UserProfileRequest request);
	 
	UserProfileResponse getUserProfile(String userId);
	 
	ResponseEntity<byte[]> getProfileImage(String userId);
	 
	ResponseEntity<?> getUserAssociatedRoles(String userId);
	 
	ResponseEntity<?> switchRole(SwitchRoleRequest request);
	 
	ResponseEntity<?> getUserNameCurrentRoleName(String userId);
}
