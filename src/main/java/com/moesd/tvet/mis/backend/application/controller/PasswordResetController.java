package com.moesd.tvet.mis.backend.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.dto.ResetPasswordRequest;
import com.moesd.tvet.mis.backend.application.serviceImpl.PasswordResetTokenService;
import com.moesd.tvet.mis.backend.application.serviceImpl.UserServiceImpl;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/public-password")
public class PasswordResetController {
	
	private final PasswordResetTokenService tokenService;
    private final UserServiceImpl userServiceImpl;
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ResetPasswordRequest request) {
    	//System.out.println(request.getEmail());
        try {
        	boolean emailSent = tokenService.sendPasswordResetToken(request.getEmail());
        	if (emailSent) {
            return ResponseEntity.ok("Password reset link sent to your email");
        	}
        }catch (Exception e) {
            return ResponseEntity.badRequest().body("We couldn’t find an account associated with the email address you provided.");
        }
		return null;
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!tokenService.validateToken(request.getToken())) {
            return ResponseEntity.badRequest().body("Invalid or Expired token");
        }
        
        String email = tokenService.getEmailFromToken(request.getToken());
        userServiceImpl.updatePassword(email, request.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
    }
}
