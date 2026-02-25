package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moesd.tvet.mis.backend.application.configuration.JwtUtilService;
import com.moesd.tvet.mis.backend.application.dto.AuthenticationRequest;
import com.moesd.tvet.mis.backend.application.dto.AuthenticationResponse;
import com.moesd.tvet.mis.backend.application.dto.UserRegisterRequest;
import com.moesd.tvet.mis.backend.application.model.Role;
import com.moesd.tvet.mis.backend.application.model.Token;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.model.UserRole;
import com.moesd.tvet.mis.backend.application.repository.RoleRepository;
import com.moesd.tvet.mis.backend.application.repository.TokenRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRoleRepository;
import com.moesd.tvet.mis.backend.application.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService{

	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final JwtUtilService jwtUtilService;
	private final TokenRepository tokenRepository;
	private final AuthenticationManager authenticationManager;
	private final UserRoleRepository userRoleRepository;

	@Override
	@Transactional
	public ResponseEntity<?> register(UserRegisterRequest request) {
		try {
			// Check if user ID already exists
			if (userRepository.findByUserId(request.getUserId()).isPresent()) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
						"status", HttpStatus.CONFLICT.value(),
						"error", "Conflict", 
						"message", "User ID " + request.getUserId() + " already exists"));
			}
			// Create new User
			User user = new User();
			
			// Assign Roles
			List<UserRole> userRoles = new ArrayList<>();
			for (Integer roleId : request.getRole()) {
				Role role = roleRepository.findById(roleId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Role with ID " + roleId + " does not exist"));
				    UserRole userRole = new UserRole();
					userRole.setUser(user);
					userRole.setRole(role);
					userRoles.add(userRole);
			}
			user.setUserId(request.getUserId());
			user.setPassword(passwordEncoder.encode(request.getPassword()));
			user.setFirstName(request.getFirstName());
			user.setMiddleName(request.getMiddleName());
			user.setLastName(request.getLastName());
			user.setGenderId(request.getGenderId());
			user.setMobileNo(request.getMobileNo());
			user.setEmailId(request.getEmailId());
			user.setCurrentRole(request.getCurrentRole());
			user.setStatusId("1");
			user.setLocationId(request.getLocationId());
			user.setCreatedAt(new Date());
			user.setCreatedBy(request.getCreatedBy());
			user = userRepository.save(user);
			
			userRoleRepository.saveAll(userRoles);
			user.setUserRoles(userRoles);
			var savedUser = userRepository.save(user);
			
			//Generate JWT Tokens
			var jwtToken = jwtUtilService.generateToken(savedUser);
			var refreshToken = jwtUtilService.generateRefreshToken(savedUser);
			saveUserToken(savedUser, jwtToken);
			
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of(
							"status", HttpStatus.CREATED.value(), 
							"message", "User registration successful",
							"user_id", savedUser.getUserId(),
							"access_token", jwtToken,
							"refresh_token", refreshToken));

		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(
					Map.of(
							"status", e.getStatusCode().value(),
							"error", e.getReason(),
							"message", e.getReason()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
							"status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
							"error", "Internal Server Error",
							"message", "An unexpected error occurred. Please try again later."));
		}
	}

	@Override
	public ResponseEntity<?> authenticate(AuthenticationRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			var user = userRepository.findByUsername(request.getUsername(), 1)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

			var jwtToken = jwtUtilService.generateToken(user);
			var refreshToken = jwtUtilService.generateRefreshToken(user);

			revokeAllUserTokens(user);
			saveUserToken(user, jwtToken);

			return ResponseEntity.ok(Map.of("access_token", jwtToken, "refresh_token", refreshToken,"current_role", user.getCurrentRole(),"userId", user.getUserId(),"locationId", user.getLocationId()));

		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of(
							"status", HttpStatus.UNAUTHORIZED.value(), 
							"error", "Unauthorized",
							"message","Invalid credentials. Please check your userId and password."));
							
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
							"status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
							"error", "Internal Server Error",
							"message", "An unexpected error occurred during authentication. Please try again later."));
		}
	}

	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder().user(user).token(jwtToken).tokenType("BEARER").expired(false).revoked(false)
				.build();
		tokenRepository.save(token);
	}

	private void revokeAllUserTokens(User user) {
		var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getUsername());
		if (validUserTokens.isEmpty())
			return;
		validUserTokens.forEach(token -> {
			token.setExpired(true);
			token.setRevoked(true);
		});
		tokenRepository.saveAll(validUserTokens);
	}

	public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		final String refreshToken;
		final String userName;
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}
		refreshToken = authHeader.substring(7);
		userName = jwtUtilService.extractUsername(refreshToken);
		if (userName != null) {
			var user = this.userRepository.findByUsername(userName, 1).orElseThrow();
			if (jwtUtilService.isTokenValid(refreshToken, user)) {
				var accessToken = jwtUtilService.generateToken(user);
				revokeAllUserTokens(user);
				saveUserToken(user, accessToken);
				var authResponse = AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken)
						.build();
				try {
					new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
				} catch (StreamWriteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DatabindException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
