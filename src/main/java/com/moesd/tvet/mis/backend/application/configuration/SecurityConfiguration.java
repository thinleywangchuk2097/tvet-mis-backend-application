package com.moesd.tvet.mis.backend.application.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final AuthenticationProvider authenticationProvider;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				// CSRF disabled — stateless JWT authentication
				.csrf(csrf -> csrf.disable())

				// Uses the CorsConfigurationSource bean from ApplicationConfiguration
				.cors(Customizer.withDefaults())

				.authorizeHttpRequests(auth -> auth
						// Only allow preflight (OPTIONS) on real API paths
						.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()

						// Spring Boot error forwarding endpoint
						.requestMatchers("/error").permitAll()

						// Public API endpoints
						.requestMatchers("/api/v1/auth/**",
								"/api/v1/public/**", 
								"/api/v1/common/**")
						.permitAll()

						// User management — restricted to listed authorities
						.requestMatchers("/api/v1/user/management/**")
						.hasAnyAuthority("1", "2", "5", "6", "7",
								"8", "9", "10", "11",
								"12", "13", "14", "15", "16",
								"17", "28", "30", "29", "21", "23")

						// Password endpoints — admin only
						.requestMatchers("/api/v1/user/password/**").hasAuthority("1")

						// Everything else requires authentication
						.anyRequest().authenticated())

				// Stateless session — no HTTP session, no cookies
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authenticationProvider(authenticationProvider)

				// JWT filter runs before username/password auth
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

				// Return 401 on authentication failure
				.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
				}))

				.build();
	}

}