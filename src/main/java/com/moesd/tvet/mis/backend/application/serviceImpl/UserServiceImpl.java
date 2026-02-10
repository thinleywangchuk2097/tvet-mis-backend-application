package com.moesd.tvet.mis.backend.application.serviceImpl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.dto.ChangePasswordResponse;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService{

	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		 return userRepository.findByUsername(username, 1)
	                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}
	
	
	public ChangePasswordResponse changeUserPassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username, 1)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return new ChangePasswordResponse(false, "Current password is incorrect");
        }
        
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return new ChangePasswordResponse(false, "New password cannot be the same as current password");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return new ChangePasswordResponse(true, "Password changed successfully");
    }
	
	public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmailId(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
