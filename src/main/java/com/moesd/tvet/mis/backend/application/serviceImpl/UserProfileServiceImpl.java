package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.dto.SwitchRoleRequest;
import com.moesd.tvet.mis.backend.application.dto.UserProfileRequest;
import com.moesd.tvet.mis.backend.application.dto.UserProfileResponse;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.repository.RoleRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.service.UserProfileService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService{

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final ObjectToJson objectTojson;

    @Value("${profile.image.upload-dir}")
    private String uploadDir;
    
    @Value("${profile.image.base-url}")
    private String imageBaseUrl;

    @Override
    public ResponseEntity<?> updateUserProfile(UserProfileRequest request) {
        try {
            User user = userRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Update basic fields
            // 2. Handle name splitting if userName is provided
            if (request.getUserName() != null) {
                String[] nameParts = request.getUserName().split(" ");
                
                // Set firstName (first part)
                user.setFirstName(nameParts.length > 0 ? nameParts[0] : "");
                
                // Set middleName (second part if exists)
                user.setMiddleName(nameParts.length > 1 ? nameParts[1] : null);
                
                // Set lastName (remaining parts joined by space)
                if (nameParts.length > 2) {
                    StringBuilder lastName = new StringBuilder(nameParts[2]);
                    for (int i = 3; i < nameParts.length; i++) {
                        lastName.append(" ").append(nameParts[i]);
                    }
                    user.setLastName(lastName.toString());
                } else {
                    user.setLastName(null);
                }
            }
            if (request.getMobileNo() != null) {
                user.setMobileNo(request.getMobileNo());
            }
            if (request.getEmailId() != null) {
                user.setEmailId(request.getEmailId());
            }
            user.setUpdatedAt(new Date());

            // Handle base64 image
            if (request.getProfileImageBase64() != null && !request.getProfileImageBase64().isEmpty()) {
                String imagePath = saveBase64Image(request.getProfileImageBase64(), request.getUserId());
                user.setProfilePath(imagePath);
            }

            userRepository.save(user);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error updating profile: " + e.getMessage());
        }
    }

    private String saveBase64Image(String base64Data, String userId) throws IOException {
        // Separate metadata from actual base64 data
        String[] parts = base64Data.split(",");
        String base64Image = parts.length > 1 ? parts[1] : parts[0];
        // Get file extension from metadata
        String extension = determineExtension(parts[0]);
        // Decode base64 string to bytes
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        // Generate unique filename
        String filename = "user_" + userId + "_" + System.currentTimeMillis() + "." + extension;
        Path filePath = uploadPath.resolve(filename);
        
        // Save file
        Files.write(filePath, imageBytes);
        
        return filePath.toString();
    }

    private String determineExtension(String dataUri) {
        // Format: "data:image/<type>;base64"
        if (dataUri.contains("image/png")) {
            return "png";
        } else if (dataUri.contains("image/jpeg")) {
            return "jpg";
        } else if (dataUri.contains("image/gif")) {
            return "gif";
        } else if (dataUri.contains("image/webp")) {
            return "webp";
        }
        return "jpg"; // default extension
    }

	@Override
	public UserProfileResponse getUserProfile(String userId) {
		User user = userRepository.findByUserId(userId)
	            .orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		
		
	    return UserProfileResponse.builder()
	            .userId(user.getUserId())
	            .firstName(user.getFirstName())
	            .middleName(user.getMiddleName())
	            .lastName(user.getLastName())
	            .mobileNo(user.getMobileNo())
	            .emailId(user.getEmailId())
	            .profileImageUrl(formatImageUrl(user.getProfilePath())) // Or construct full URL
	            .build();
	}
	
	private String formatImageUrl(String filePath) {
	    if (filePath == null) return null;
	    
	    // Convert Windows paths to URL format
	    String normalizedPath = filePath.replace("\\", "/");
	    
	    // Remove any "./" or relative path segments
	    normalizedPath = normalizedPath.replaceAll("^\\./", "");
	    
	    // Construct full URL
	    return imageBaseUrl + normalizedPath;
	}
	
	@Override
    public ResponseEntity<byte[]> getProfileImage(String userId) {
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            
            if (user.getProfilePath() == null) {
                return ResponseEntity.notFound().build();
            }
            
            Path path = Paths.get(user.getProfilePath());
            byte[] imageBytes = Files.readAllBytes(path);
            
            String contentType = Files.probeContentType(path);
            MediaType mediaType = MediaType.parseMediaType(contentType);
            
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<?> getUserAssociatedRoles(String userId) {
        try {
            //List<Role> roles = roleRepository.getUserAssociatedRoles(userId);
            List<Tuple> roles = roleRepository.getUserAssociatedRoles(userId);
         
            if (roles == null || roles.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No roles found for user with ID: " + userId);
            }
            List<ObjectNode> json = objectTojson._toJson(roles);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error retrieving roles for user: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> switchRole(SwitchRoleRequest request) {
        try {
            // 1. Find the user
            Optional<User> user = userRepository.findByUserId(request.getUserId());
            
            // 2. If user doesn't exist, return 404
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // 3. Update the current role
            User existingUser = user.get();
            existingUser.setCurrentRole(Integer.parseInt(request.getSwitchedRoleId())); 
            userRepository.save(existingUser);
            
            // 4. Return success
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public ResponseEntity<?> getUserNameCurrentRoleName(String userId) {
        List<Tuple> result = userRepository.getUserNameCurrentRoleName(userId);
        if (result == null || result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Tuple tuple = result.get(0);
        Map<String, String> response = new HashMap<>();
        response.put("user_id", tuple.get("user_id", String.class));
        response.put("username", tuple.get("username", String.class));
        response.put("current_role_name", tuple.get("current_role_name", String.class));
        
        return ResponseEntity.ok(response);
    }

}
