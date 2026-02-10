package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.configuration.JwtUtilService;
import com.moesd.tvet.mis.backend.application.dto.ApiResponse;
import com.moesd.tvet.mis.backend.application.dto.RolePrivilegeRequest;
import com.moesd.tvet.mis.backend.application.dto.RoleWithPrivilegesResponse;
import com.moesd.tvet.mis.backend.application.dto.UserRegisterRequest;
import com.moesd.tvet.mis.backend.application.model.Privilege;
import com.moesd.tvet.mis.backend.application.model.Role;
import com.moesd.tvet.mis.backend.application.model.RolePrivilege;
import com.moesd.tvet.mis.backend.application.model.Token;
import com.moesd.tvet.mis.backend.application.model.User;
import com.moesd.tvet.mis.backend.application.model.UserRole;
import com.moesd.tvet.mis.backend.application.repository.PrivilegeRepository;
import com.moesd.tvet.mis.backend.application.repository.RolePrivilegeRepository;
import com.moesd.tvet.mis.backend.application.repository.RoleRepository;
import com.moesd.tvet.mis.backend.application.repository.TokenRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRepository;
import com.moesd.tvet.mis.backend.application.repository.UserRoleRepository;
import com.moesd.tvet.mis.backend.application.service.UserRoleManagementService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRoleManagementServiceImpl implements UserRoleManagementService{
	
	private final RolePrivilegeRepository rolePrivilegeRepository;
    private final RoleRepository roleRepository;
    private final ObjectToJson objectTojson;
    private final PrivilegeRepository privilegeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;
    private final JwtUtilService jwtUtilService;
    private final TokenRepository tokenRepository;
    
    @Override
    @Transactional
    public ResponseEntity<?> createRole(RolePrivilegeRequest request) {
        // Validate request
        if (request == null || request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role name cannot be empty");
        }

        // Check if role already exists
        if (roleRepository.existsByRoleName(request.getRoleName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Role with name " + request.getRoleName() + " already exists");
        }

        try {
            // Save Role
            Role role = new Role();
            role.setRoleName(request.getRoleName());
            role.setDescription(request.getDescription());
            role.setCreatedAt(LocalDateTime.now());
            role.setStatusId("1");	        
            //role.setCreatedBy(currentUsername);
            Role savedRole = roleRepository.save(role);

            // Assign privileges if any
            if (request.getAssignedPrivilegeId() != null && !request.getAssignedPrivilegeId().isEmpty()) {
                Set<RolePrivilege> rolePrivileges = request.getAssignedPrivilegeId().stream()
                    .map(privilegeId -> {
                        Privilege privilege = new Privilege();
                        privilege.setId(privilegeId);
                        return RolePrivilege.builder()
                                .role(savedRole)
                                .privilege(privilege)
                                .build();
                    })
                    .collect(Collectors.toSet());
          
                rolePrivilegeRepository.saveAll(rolePrivileges);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedRole);
        } catch (Exception e) {
            // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating role: " + e.getMessage());
        }
    }

	@Override
	public List<ObjectNode> getAllPrivilegeRole() {
		List<Tuple> result = rolePrivilegeRepository.getAllPrivilegeRole();
		List<ObjectNode> json = objectTojson._toJson(result);
		return json;
	}

	@Override
	@Transactional
	public ResponseEntity<ApiResponse<RoleWithPrivilegesResponse>> editPrivilegesRole(RolePrivilegeRequest request) {
	    try {
	        // 1. Validate request
	        if (request.getId() == null) {
	            return ResponseEntity.badRequest()
	                .body(new ApiResponse<>("Role ID is required", null));
	        }
	        
	        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
	            return ResponseEntity.badRequest()
	                .body(new ApiResponse<>("Role name is required", null));
	        }

	        // 2. Find existing role (with privileges loaded)
	        Role existingRole = roleRepository.findByIdWithPrivileges(request.getId())
	            .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + request.getId()));

	        // 3. Check for duplicate role name
	        if (!existingRole.getRoleName().equals(request.getRoleName()) && 
	            roleRepository.existsByRoleName(request.getRoleName())) {
	            return ResponseEntity.status(HttpStatus.CONFLICT)
	                .body(new ApiResponse<>("Role name already exists: " + request.getRoleName(), null));
	        }

	        // 4. Update role
	        existingRole.setRoleName(request.getRoleName());
	        existingRole.setDescription(request.getDescription());
	        existingRole.setUpdatedAt(LocalDateTime.now());
	        //existingRole.setUpdatedBy(currentUsername);
	        
	        // 5. Handle privilege updates
	        if (request.getAssignedPrivilegeId() != null) {
	            updateRolePrivileges(existingRole, request.getAssignedPrivilegeId());
	        }

	        // 6. Refresh from database to verify updates
	        Role updatedRole = roleRepository.findByIdWithPrivileges(existingRole.getId()).get();
	        
	        // 7. Build response from fresh database state
	        RoleWithPrivilegesResponse response = buildRoleResponse(updatedRole);

	        return ResponseEntity.ok(
	            new ApiResponse<>("Role updated successfully", response));
	            
	    } catch (EntityNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	            .body(new ApiResponse<>(e.getMessage(), null));
	    } catch (Exception e) {
	        return ResponseEntity.internalServerError()
	            .body(new ApiResponse<>("Failed to update role: " + e.getMessage(), null));
	    }
	}

	private void updateRolePrivileges(Role role, Set<Long> privilegeIds) {
	    // Clear existing privileges
	    role.getPrivileges().clear();
	    
	    // Add new privileges
	    privilegeIds.forEach(privilegeId -> {
	        Privilege privilege = privilegeRepository.findById(privilegeId)
	            .orElseThrow(() -> new EntityNotFoundException("Privilege not found"));
	        role.addPrivilege(privilege);
	    });
	    
	    roleRepository.save(role); // Cascade will handle privilege updates
	}

	private RoleWithPrivilegesResponse buildRoleResponse(Role role) {
	    return RoleWithPrivilegesResponse.builder()
	        .id(role.getId())
	        .roleName(role.getRoleName())
	        .description(role.getDescription())
	        .assignedPrivilegeId(role.getPrivileges().stream()
	            .map(p -> p.getPrivilege().getId())
	            .collect(Collectors.toList()))
	        .updatedAt(role.getUpdatedAt())  // Add this
	       // .updatedBy(role.getUpdatedBy()) 
	        .build();
	}

	@Override
	@Transactional
	public ResponseEntity<ApiResponse<String>> deletePrivilegesRole(RolePrivilegeRequest request) {
	    try {
	        // 1. Validate request
	        if (request.getId() == null) {
	            return ResponseEntity.badRequest()
	                .body(new ApiResponse<>("Role ID is required", null));
	        }

	        // 2. Check if role exists
	        Role role = roleRepository.findById(request.getId())
	            .orElseThrow(() -> new EntityNotFoundException(
	                "Role not found with id: " + request.getId()));

	        // 3. Delete all associated privileges first
	        rolePrivilegeRepository.deleteByRoleId(role.getId());

	        // 4. Delete the role
	        roleRepository.delete(role);

	        return ResponseEntity.ok(
	            new ApiResponse<>("Role and associated privileges deleted successfully", null));
	            
	    } catch (EntityNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	            .body(new ApiResponse<>(e.getMessage(), null));
	    } catch (Exception e) {
	        return ResponseEntity.internalServerError()
	            .body(new ApiResponse<>("Failed to delete role: " + e.getMessage(), null));
	    }
	}

	@Override
	public List<ObjectNode> getRoles() {
		List<Tuple> result = roleRepository.getRoles();
		List<ObjectNode> json = objectTojson._toJson(result);
		return json;
	}

	@Override
	public ResponseEntity<?> createUser(UserRegisterRequest request) {
		try {
			// Check if user ID already exists
			if (userRepository.findByUserId(request.getUserId()).isPresent()) {
				return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
						"status", HttpStatus.CONFLICT.value(),
						"error", "Conflict", 
						"message", "User ID " + request.getUserId() + " already exists"));
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
			user = userRepository.save(user);
			
			userRoleRepository.saveAll(userRoles);
			user.setUserRoles(userRoles);
			var savedUser = userRepository.save(user);
			// Generate JWT Tokens
			var jwtToken = jwtUtilService.generateToken(savedUser);
			var refreshToken = jwtUtilService.generateRefreshToken(savedUser);
			saveUserToken(savedUser, jwtToken);
			
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(Map.of(
							"status", HttpStatus.CREATED.value(), 
							"message", "User registration successful",
							"user_id", savedUser.getUserId(),
							"access_token", jwtToken,
							"refresh_token", refreshToken));

		} catch (ResponseStatusException e) {
			return ResponseEntity.status(e.getStatusCode()).body(
					Map.of(
							"status", e.getStatusCode().value(),
							"error", e.getReason(),
							"message", e.getReason()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of(
							"status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
							"error", "Internal Server Error",
							"message", "An unexpected error occurred. Please try again later."));
		}
	}
	
	private void saveUserToken(User user, String jwtToken) {
		var token = Token.builder().user(user).token(jwtToken).tokenType("BEARER").expired(false).revoked(false)
				.build();
		tokenRepository.save(token);
	}

	@Override
	public ResponseEntity<?> editUser(UserRegisterRequest request) {
	    try {
	        // 1. Find the user being edited (using request.userId)
	        User currentUser = userRepository.findByUserId(request.getUserId())
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
	                "User not found with ID: " + request.getUserId()));

	        // 2. Check if another user (EXCLUDING current user) has the same userId
	        Optional<User> conflictingUser = userRepository.findByUserId(request.getUserId())
	            .filter(user -> !user.getId().equals(currentUser.getId())); // Exclude current user

	        if (conflictingUser.isPresent()) {
	            throw new ResponseStatusException(HttpStatus.CONFLICT, 
	                "User ID '" + request.getUserId() + "' is already taken by another user.");
	        }

	        // 3. Proceed with the update (your existing logic)
	        currentUser.setFirstName(request.getFirstName());
	        currentUser.setMiddleName(request.getMiddleName());
	        currentUser.setLastName(request.getLastName());
	        currentUser.setGenderId(request.getGenderId());
	        currentUser.setMobileNo(request.getMobileNo());
	        currentUser.setEmailId(request.getEmailId());
	        currentUser.setCurrentRole(request.getCurrentRole());
	        currentUser.setLocationId(request.getLocationId());
	        currentUser.setUpdatedAt(new Date());
	        currentUser.setUpdatedBy(request.getCreatedBy());

	        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
	            currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
	        }

	        // Update roles (your existing logic)
	        List<UserRole> existingRoles = userRoleRepository.findByUserId(currentUser.getId());
	        userRoleRepository.deleteAll(existingRoles);

	        List<UserRole> newUserRoles = new ArrayList<>();
	        for (Integer roleId : request.getRole()) {
	            Role role = roleRepository.findById(roleId)
	                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
	                    "Role with ID " + roleId + " does not exist"));
	            
	            UserRole userRole = new UserRole();
	            userRole.setUser(currentUser);
	            userRole.setRole(role);
	            newUserRoles.add(userRole);
	        }
	        userRoleRepository.saveAll(newUserRoles);

	        User updatedUser = userRepository.save(currentUser);

	        return ResponseEntity.ok(Map.of(
	            "status", HttpStatus.OK.value(),
	            "message", "User updated successfully",
	            "user_id", updatedUser.getUserId()));

	    } catch (ResponseStatusException e) {
	        return ResponseEntity.status(e.getStatusCode()).body(
	            Map.of(
	                "status", e.getStatusCode().value(),
	                "error", e.getReason(),
	                "message", e.getReason()));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Map.of(
	                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
	                "error", "Internal Server Error",
	                "message", "An unexpected error occurred. Please try again later."));
	    }
	}

	@Override
	@Transactional
	public ResponseEntity<?> deleteUser(UserRegisterRequest request) {
	    try {
	        // 1. Find the user to delete
	        User userToDelete = userRepository.findByUserId(request.getUserId())
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
	                "User not found with ID: " + request.getUserId()));

	        // 2. First delete all dependent records (tokens and user roles)
	        
	        // Delete all tokens associated with the user (using user's primary ID)
	        tokenRepository.deleteByUser_Id(userToDelete.getId());
	        
	        // Delete all roles associated with the user
	        List<UserRole> userRoles = userRoleRepository.findByUserId(userToDelete.getId());
	        if (!userRoles.isEmpty()) {
	            userRoleRepository.deleteAll(userRoles);
	        }

	        // 3. Now delete the user
	        userRepository.delete(userToDelete);

	        // 4. Return success response
	        return ResponseEntity.ok(Map.of(
	            "status", HttpStatus.OK.value(),
	            "message", "User deleted successfully",
	            "deleted_user_id", request.getUserId()));

	    } catch (ResponseStatusException e) {
	        return ResponseEntity.status(e.getStatusCode()).body(
	            Map.of(
	                "status", e.getStatusCode().value(),
	                "error", e.getReason(),
	                "message", e.getReason()));
	    } catch (DataIntegrityViolationException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(
	            Map.of(
	                "status", HttpStatus.CONFLICT.value(),
	                "error", "Data Integrity Violation",
	                "message", "Cannot delete user due to existing references"));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Map.of(
	                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
	                "error", "Internal Server Error",
	                "message", "Failed to delete user. Please try again later."));
	    }
	}

	@Override
	public List<ObjectNode> getAllUsers() {
		List<Tuple> result = userRepository.getAllUsers();
		List<ObjectNode> json = objectTojson._toJson(result);
		return json;
	}

}
