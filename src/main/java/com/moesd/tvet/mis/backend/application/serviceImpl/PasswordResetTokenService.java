package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
	
	private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    
    @Value("${app.password-reset.token-expiration}")
    private long expiration;
    
    // Convert hex string to SecretKey
    private static final String SECRET_HEX = "6D5A7134743777217A25432A462D4A614E645267556B586E3272357538702F31";
    private final SecretKey jwtSecretKey = Keys.hmacShaKeyFor(
        SECRET_HEX.getBytes(StandardCharsets.UTF_8)
    );
    
    public boolean  sendPasswordResetToken(String email) {
    	System.out.println("email" + email);
        User user = userRepository.findByEmailId(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("below the user");
        
        String token = generateToken(user);
       
        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        System.out.println("resetLink"+ " " + resetLink);
        SimpleMailMessage message = new SimpleMailMessage();
      
        message.setTo(user.getEmailId());
        message.setSubject("Password Reset Request");
        message.setText("""
            Click the link to reset your password: %s
            This link will expire in %d minutes.
            
            If you didn't request this, please ignore this email.
            """.formatted(resetLink, TimeUnit.MILLISECONDS.toMinutes(expiration)));
       
        mailSender.send(message);
        System.out.println("inside the message");
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
