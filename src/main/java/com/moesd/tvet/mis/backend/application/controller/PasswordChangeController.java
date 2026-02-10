package com.moesd.tvet.mis.backend.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.ChangePasswordRequest;
import com.moesd.tvet.mis.backend.application.dto.ChangePasswordResponse;
import com.moesd.tvet.mis.backend.application.serviceImpl.UserServiceImpl;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/password/auth-password")
public class PasswordChangeController {
	
	private final UserServiceImpl userServiceImpl;
	
	@PostMapping("/changePassword")
	 public ResponseEntity<ChangePasswordResponse>changeUserPassword(@AuthenticationPrincipal UserDetails userDetails,
			  @RequestBody ChangePasswordRequest request) {
	        System.out.println(request);
	        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
	            return ResponseEntity.badRequest()
	                    .body(new ChangePasswordResponse(false, "The new password and confirmation password do not match. Please ensure both entries are identical and try again."));
	        }
	        ChangePasswordResponse response = userServiceImpl.changeUserPassword(
	                userDetails.getUsername(), 
	                request.getCurrentPassword(), 
	                request.getNewPassword());
	        if (response.isSuccess()) {
	            return ResponseEntity.ok(response);
	        } else {
	            return ResponseEntity.badRequest().body(response);
	        }
	    }
}
