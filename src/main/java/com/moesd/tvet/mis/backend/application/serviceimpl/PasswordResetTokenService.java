package com.moesd.tvet.mis.backend.application.serviceimpl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.utility.NotificationService;
import com.moesd.tvet.mis.backend.application.utility.NotificationService.Recipient;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
	
	private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    @Value("${app.password-reset.token-expiration}")
    private long expiration;
    
    @Value("${app.secret-hex}")
    private String secretHex;

    /** Frontend base URL for the reset link (APP_FRONTEND_URL in the ConfigMap) */
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    private SecretKey jwtSecretKey;
    
    //FIXED: Initialize after @Value injection
    @PostConstruct
    public void init() {
        this.jwtSecretKey = Keys.hmacShaKeyFor(
            secretHex.getBytes(StandardCharsets.UTF_8)
        );
    }
    
    public boolean sendPasswordResetToken(String email) {
        User user = userRepository.findByEmailId(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("below the user");
        
        String token = generateToken(user);
       
        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;
        // reset link by SMS and email (queued; sent and retried by the dispatch jobs)
        notificationService.sendLink(new Recipient(user.getMobileNo(), user.getEmailId(), user.getFirstName()),
            "Password Reset Request", "use this link to reset your TVET-MIS password", resetLink,
            "This link will expire in %d minutes. If you didn't request this, please ignore it."
                .formatted(TimeUnit.MILLISECONDS.toMinutes(expiration)),
            "PASSWORD_RESET", user.getUserId());
        return true;
    }
    
    private String generateToken(User user) {
        return Jwts.builder()
            .subject(user.getEmailId())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(jwtSecretKey, Jwts.SIG.HS256)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public String getEmailFromToken(String token) throws InvalidTokenException {
        try {
            return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("Password reset token expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid password reset token", ex);
        }
    }
    
    public static class InvalidTokenException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}