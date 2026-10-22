package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
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
import tools.jackson.core.exc.StreamWriteException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import com.moesd.tvet.mis.backend.application.configuration.JwtUtilService;
import com.moesd.tvet.mis.backend.application.configuration.LoginRateLimiter;
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
import com.moesd.tvet.mis.backend.application.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final JwtUtilService jwtUtilService;
	private final TokenRepository tokenRepository;
	private final AuthenticationManager authenticationManager;
	private final LoginRateLimiter loginRateLimiter;
	private static final int MAX_FAILED_ATTEMPTS = 5;
	private static final Duration LOCK_DURATION = Duration.ofMinutes(10);
	
	@Override
	@Transactional
	public ResponseEntity<?> register(UserRegisterRequest request) {
		try {
			// Check if user ID already exists
			if (userRepository.findByUserId(request.getUserId()).isPresent()) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status", HttpStatus.CONFLICT.value(),
						"error", "Conflict", "message", "User ID " + request.getUserId() + " already exists"));
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
			user.setUserRoles(userRoles);
			user = userRepository.save(user);

			// Generate JWT Tokens
			var jwtToken = jwtUtilService.generateToken(user);
			var refreshToken = jwtUtilService.generateRefreshToken(user);
			saveUserToken(user, jwtToken);

			return ResponseEntity.ok(Map.of("access_token", jwtToken, "refresh_token", refreshToken, "current_role",
					user.getCurrentRole(), "userId", user.getUserId(), "id", user.getId(), "locationId",
					user.getLocationId()));

		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(
					Map.of("status", e.getStatusCode().value(), "error", e.getReason(), "message", e.getReason()));
		} catch (Exception e) {
			log.error("Registration failed for userId: {}", request.getUserId(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.value(), "error", "Internal Server Error",
							"message", "An unexpected error occurred. Please try again later."));
		}
	}

	@Override
	@Transactional
	public ResponseEntity<?> authenticate(AuthenticationRequest request, HttpServletRequest httpRequest) {

		String ip = getClientIp(httpRequest);
		String username = request.getUsername();

		// 1. Per-IP rate limit
		if (!loginRateLimiter.resolveIpBucket(ip).tryConsume(1)) {
			log.warn("Rate limit exceeded for IP: {}", ip);
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "60").body(Map.of("status",
					429, "error", "Too Many Requests", "message", "Too many login attempts. Please try again later."));
		}

		// 2. Per-account rate limit
		if (!loginRateLimiter.resolveAccountBucket(username).tryConsume(1)) {
			log.warn("Rate limit exceeded for username: {}", username);
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "60")
					.body(Map.of("status", 429, "error", "Too Many Requests", "message",
							"Too many login attempts for this account. Please try again later."));
		}

		// 3. Account lockout check
		var userOpt = userRepository.findByUsername(username, 1);
		if (userOpt.isPresent()) {
			User u = userOpt.get();
			if (u.getLockUntil() != null && u.getLockUntil().isAfter(LocalDateTime.now())) {
				log.warn("Account locked: {}", username);
				return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of("status", 423, "error", "Locked", "message",
						"Account is temporarily locked. Try again later."));
			}
		}

		// 4. Authenticate
		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));

			var user = userRepository.findByUsername(username, 1)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

			// Reset failure counters on success
			user.setFailedLoginAttempts(0);
			user.setLockUntil(null);
			userRepository.save(user);

			var jwtToken = jwtUtilService.generateToken(user);
			var refreshToken = jwtUtilService.generateRefreshToken(user);

			revokeAllUserTokens(user);
			saveUserToken(user, jwtToken);

			return ResponseEntity.ok(Map.of("access_token", jwtToken, "refresh_token", refreshToken, "current_role",
					user.getCurrentRole(), "userId", user.getUserId(), "id", user.getId(), "locationId",
					user.getLocationId()));

		} catch (BadCredentialsException e) {
			// Increment failure counter; lock if threshold reached
			userOpt.ifPresent(u -> {
				int attempts = (u.getFailedLoginAttempts() == null ? 0 : u.getFailedLoginAttempts()) + 1;
				u.setFailedLoginAttempts(attempts);
				if (attempts >= MAX_FAILED_ATTEMPTS) {
					u.setLockUntil(LocalDateTime.now().plus(LOCK_DURATION));
					log.warn("Account {} locked after {} failed attempts", u.getUserId(), attempts);
				}
				userRepository.save(u);
			});

			log.warn("Authentication failed for username: {}", username);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("status", 401, "error", "Unauthorized",
					"message", "Invalid credentials. Please check your userId and password."));

		} catch (Exception e) {
			log.error("Authentication error for username: {}", username, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", 500, "error",
					"Internal Server Error", "message", "An unexpected error occurred during authentication."));
		}
	}

	private String getClientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
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
			try {
				var user = this.userRepository.findByUsername(userName, 1).orElseThrow();
				if (jwtUtilService.isTokenValid(refreshToken, user)) {
					var accessToken = jwtUtilService.generateToken(user);
					revokeAllUserTokens(user);
					saveUserToken(user, accessToken);
					var authResponse = AuthenticationResponse.builder().accessToken(accessToken)
							.refreshToken(refreshToken).build();
					new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
				}
			} catch (StreamWriteException e) {
				log.error("Stream write error while refreshing token for user: {}", userName, e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (DatabindException e) {
				log.error("Data binding error while refreshing token for user: {}", userName, e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (IOException e) {
				log.error("IO error while refreshing token for user: {}", userName, e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (Exception e) {
				log.error("Unexpected error while refreshing token for user: {}", userName, e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	@Override
	public ResponseEntity<?> authByBhutanNDI(AuthenticationRequest request) {
		try {
			var user = userRepository.findByUsername(request.getUsername(), 1)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

			var jwtToken = jwtUtilService.generateToken(user);
			var refreshToken = jwtUtilService.generateRefreshToken(user);

			revokeAllUserTokens(user);
			saveUserToken(user, jwtToken);

			return ResponseEntity.ok(Map.of("access_token", jwtToken, "refresh_token", refreshToken, "current_role",
					user.getCurrentRole(), "userId", user.getUserId(), "id", user.getId(), "locationId",
					user.getLocationId()));
		} catch (Exception e) {
			log.error("Bhutan NDI authentication failed for username: {}", request.getUsername(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.value(), "error", "Internal Server Error",
							"message", "An unexpected error occurred. Please try again later."));
		}
	}
}