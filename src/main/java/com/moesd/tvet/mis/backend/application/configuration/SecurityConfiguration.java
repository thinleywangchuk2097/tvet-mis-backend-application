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
				//CSRF disabled — stateless JWT authentication
				.csrf(csrf -> csrf.disable())
				//Uses the CorsConfigurationSource bean from ApplicationConfiguration
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth
						// Only allow preflight (OPTIONS) on real API paths
						.requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
						// Spring Boot error forwarding endpoint
						.requestMatchers("/error").permitAll()
						// Public API endpoints
						.requestMatchers("/api/v1/auth/**", "/api/v1/public/**", "/api/v1/common/**").permitAll()
						// User management — admin only
						.requestMatchers("/api/v1/user/management/**").hasAnyAuthority("1", "7", "22")
						// Dropdown values — admin only
						.requestMatchers("/api/v1/dropdown-management/**").hasAnyAuthority("1", "22", "23")
						// Feature modules (moved out of /api/v1/user/management) — roles per module
						.requestMatchers("/api/v1/accredited-course/**").hasAnyAuthority("11", "12", "7", "23", "22")
						.requestMatchers("/api/v1/trainer/**").hasAnyAuthority("11", "7", "23", "22")
						.requestMatchers("/api/v1/cmfg-monitoring/**").hasAnyAuthority("11", "7", "23", "22")
						.requestMatchers("/api/v1/certificate/**").hasAnyAuthority("11", "7", "9", "22", "12")
						.requestMatchers("/api/v1/course-announcement/**").hasAnyAuthority("11", "7", "22", "12")
						.requestMatchers("/api/v1/curriculum/**").hasAnyAuthority("11", "21", "14", "15", "23")
						.requestMatchers("/api/v1/dashboard/**")
						.hasAnyAuthority("1", "7", "9", "10", "11", "12", "13", "14", "15", "16", "17",
								"28", "30", "29", "21", "23", "33")
						.requestMatchers("/api/v1/monitoring/**").hasAnyAuthority("11","12", "28", "7", "23", "22")
						.requestMatchers("/api/v1/ncs/**").hasAnyAuthority("1","11", "33", "12")
						.requestMatchers("/api/v1/non-accredited-course/**").hasAnyAuthority("11", "7", "23", "22")
						.requestMatchers("/api/v1/notification/**")
						.hasAnyAuthority("1", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
								"28", "30", "29", "21", "23", "33")
						.requestMatchers("/api/v1/ojt/**").hasAnyAuthority("11", "16", "21", "15")
						.requestMatchers("/api/v1/campus-placement/**").hasAnyAuthority("11", "16", "21", "15")
						.requestMatchers("/api/v1/program-monitoring/**").hasAnyAuthority("11", "12", "28", "7", "23", "22")
						.requestMatchers("/api/v1/quality-standard-check-list/**")
						.hasAnyAuthority("1", "22")
						.requestMatchers("/api/v1/report/**")
						.hasAnyAuthority("7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "28",
								"30", "29", "21", "23", "33")
						.requestMatchers("/api/v1/resource-management/**").hasAnyAuthority("11")
						.requestMatchers("/api/v1/sector-occupation/**").hasAnyAuthority("1","33","22")
						.requestMatchers("/api/v1/self-assessment-report/**").hasAnyAuthority("11", "7", "23", "22")
						.requestMatchers("/api/v1/service-master/**").hasAnyAuthority("1")
						.requestMatchers("/api/v1/student-enrollment/**").hasAnyAuthority("28", "7", "22")
						.requestMatchers("/api/v1/subject/**").hasAnyAuthority("28", "7", "22")
						.requestMatchers("/api/v1/tot/**").hasAnyAuthority("11", "16", "21", "15")
						.requestMatchers("/api/v1/tracer/**").hasAnyAuthority("11", "15", "21")
						.requestMatchers("/api/v1/tutor/**").hasAnyAuthority("28","7","22")
						.requestMatchers("/api/v1/user-profile/**")
						.hasAnyAuthority("1", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
								"28", "30", "29", "21", "23", "33")
						.requestMatchers("/api/v1/private/birms/payment/**")
						.hasAnyAuthority("7", "9", "22", "23", "11", "12", "28", "33")
						// Password endpoints — admin only
						.requestMatchers("/api/v1/user/password/**")
						.hasAnyAuthority("1", "7", "9", "10", "11", "12", "13", "14", "15", "16", "17", "28",
								"30", "29", "21", "23", "33")
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