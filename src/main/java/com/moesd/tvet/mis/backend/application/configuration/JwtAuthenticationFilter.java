package com.moesd.tvet.mis.backend.application.configuration;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.moesd.tvet.mis.backend.application.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	private final JwtUtilService jwtUtilService;
	private final UserDetailsService userDetailsService;
	private final TokenRepository tokenRepository;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (request.getServletPath().contains("/api/v1/auth")|| request.getServletPath().contains("/api/v1/common")) {
		      filterChain.doFilter(request, response);
		      return;
		    }
		    final String authHeader = request.getHeader("Authorization");
		    final String jwt;
		    final String username;
		    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
		      filterChain.doFilter(request, response);
		      return;
		    }
		    jwt = authHeader.substring(7);
		    if("undefined".equals(jwt)) { 
		    	return;
		    }
		    username = jwtUtilService.extractUsername(jwt);
		    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
		      UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
		      var isTokenValid = tokenRepository.findByToken(jwt).map(t -> !t.getExpired() && !t.getRevoked()).orElse(false);
		      if (jwtUtilService.isTokenValid(jwt, userDetails) && isTokenValid) {
		        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
		            userDetails,
		            null,
		            userDetails.getAuthorities()
		        );
		        authToken.setDetails(
		            new WebAuthenticationDetailsSource().buildDetails(request)
		        );
		        SecurityContextHolder.getContext().setAuthentication(authToken);
		      }
		    }
		    filterChain.doFilter(request, response);
		
	}

}
