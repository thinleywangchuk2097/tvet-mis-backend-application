package com.moesd.tvet.mis.backend.application.configuration;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {

	@Value("${allowed.origin}")
	private String allowOrigin;

	private final UserRepository userRepo;

	@Bean
	UserDetailsService userDetailsService() {
		return username -> userRepo.findByUsername(username, 1)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		// Fail fast if the configured origins are missing, blank, or contain a wildcard
		if (allowOrigin == null || allowOrigin.isBlank() || allowOrigin.contains("*")) {
			throw new IllegalStateException(
					"allowed.origin must contain one or more explicit origins "
							+ "(comma-separated). Wildcards are forbidden.");
		}

		CorsConfiguration configuration = new CorsConfiguration();

		// Explicit allow-list loaded from configuration
		// e.g. allowed.origin=https://hub.neyduetewa.gov.bt,https://tvet-mis.gov.bt
		configuration.setAllowedOrigins(
				Arrays.stream(allowOrigin.split(","))
						.map(String::trim)
						.filter(s -> !s.isEmpty())
						.toList());

		// Only the HTTP methods the API actually exposes
		configuration.setAllowedMethods(
				List.of("GET", "POST", "OPTIONS", "DELETE", "PUT", "PATCH"));

		// Only request headers clients actually send
		// (Access-Control-Request-* are managed by the browser and must NOT be listed here)
		configuration.setAllowedHeaders(
				List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));

		// Headers the browser is allowed to read from the response
		configuration.setExposedHeaders(
				List.of("Authorization", "Content-Disposition"));

		// Cache the preflight response for 1 hour
		configuration.setMaxAge(3600L);

		// JWT-only authentication — no session cookies
		configuration.setAllowCredentials(false);

		// Apply CORS ONLY to actual API endpoints (not "/", "/robots.txt", "/error", etc.)
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

}