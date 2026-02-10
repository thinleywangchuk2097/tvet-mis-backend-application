package com.moesd.tvet.mis.backend.application.service;

import org.springframework.http.ResponseEntity;
import com.moesd.tvet.mis.backend.application.dto.AuthenticationRequest;
import com.moesd.tvet.mis.backend.application.dto.UserRegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
	
	ResponseEntity<?> register(UserRegisterRequest request);

	ResponseEntity<?> authenticate(AuthenticationRequest request);

	void refreshToken(HttpServletRequest request, HttpServletResponse response);
}
