package com.moesd.tvet.mis.backend.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.SwitchRoleRequest;
import com.moesd.tvet.mis.backend.application.dto.UserProfileRequest;
import com.moesd.tvet.mis.backend.application.dto.UserProfileResponse;
import com.moesd.tvet.mis.backend.application.service.UserProfileService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/user-profile")
public class UserProfileController {
	
	private final UserProfileService userProfileService;

	@PostMapping("/update-user-profile")
	public ResponseEntity<?> updateUserProfile(@RequestBody UserProfileRequest request) {
		return (userProfileService.updateUserProfile(request));
	}
	
	@GetMapping("/get-user-profile/{userId}")
	public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String userId) {
	    UserProfileResponse response = userProfileService.getUserProfile(userId);
	    return ResponseEntity.ok(response);
	}
	
	@GetMapping("/image/{userId}")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String userId) {
        return userProfileService.getProfileImage(userId);
    }
    
    @GetMapping("/get-user-associated-roles/{userId}")
    public ResponseEntity<?> getUserAssociatedRoles(@PathVariable String userId) {
        return userProfileService.getUserAssociatedRoles(userId);
    }
    
    @PostMapping("/update-switch-role")
	public ResponseEntity<?> switchRole(@RequestBody SwitchRoleRequest request) {
		return (userProfileService.switchRole(request));
	}
    
    @GetMapping("/get-username-current-role/{userId}")
    public ResponseEntity<?> getUserNameCurrentRoleName(@PathVariable String userId) {
        return userProfileService.getUserNameCurrentRoleName(userId);
    }

}
