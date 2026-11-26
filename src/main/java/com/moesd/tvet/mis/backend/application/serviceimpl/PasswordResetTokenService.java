package com.moesd.tvet.mis.backend.application.serviceimpl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.utility.NotificationService;
import com.moesd.tvet.mis.backend.application.utility.NotificationService.Recipient;
import io.jsonwebtoken.Claims;
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
    
    private static final String PASSWORD_FINGERPRINT = "pwf";

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
            .claim(PASSWORD_FINGERPRINT, passwordFingerprint(user))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(jwtSecretKey, Jwts.SIG.HS256)
            .compact();
    }
    
    /**
     * Email of a valid, unused reset token. The token carries a fingerprint of the password hash it was
     * issued for, so it stops working as soon as the password changes (single use).
     */
    public String verifyAndGetEmail(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(jwtSecretKey).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException ex) {
            throw new InvalidTokenException("Password reset token expired", ex);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid password reset token", ex);
        }
        User user = userRepository.findByEmailId(claims.getSubject())
            .orElseThrow(() -> new InvalidTokenException("Invalid password reset token", null));
        String expected = passwordFingerprint(user);
        String actual = claims.get(PASSWORD_FINGERPRINT, String.class);
        if (actual == null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8))) {
            throw new InvalidTokenException("Password reset token already used", null);
        }
        return user.getEmailId();
    }

    /** Short SHA-256 fingerprint of the user's current password hash. */
    private static String passwordFingerprint(User user) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(String.valueOf(user.getPassword()).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest).substring(0, 22);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static class InvalidTokenException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}