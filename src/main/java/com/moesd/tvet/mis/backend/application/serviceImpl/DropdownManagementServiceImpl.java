package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.dto.DropdownChildResponse;
import com.moesd.tvet.mis.backend.application.dto.DropdownChilddto;
import com.moesd.tvet.mis.backend.application.dto.DropdownManagementdto;
import com.moesd.tvet.mis.backend.application.dto.DropdownResponse;
import com.moesd.tvet.mis.backend.application.model.DropdownChild;
import com.moesd.tvet.mis.backend.application.model.DropdownParent;
import com.moesd.tvet.mis.backend.application.repository.DropdownManagementRepository;
import com.moesd.tvet.mis.backend.application.service.DropdownManagementService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DropdownManagementServiceImpl implements DropdownManagementService{
	
	private final DropdownManagementRepository dropdownManagementRepository;

    @Override
    public ResponseEntity<?> createDropdown(DropdownManagementdto request) {
        try {
            // 1. Create parent drop down
            DropdownParent parent = DropdownParent.builder()
                .dropdownName(request.getDropdownName())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy()) // Assuming this comes from DTO or security context
                .statusId("1") // Default status
                .build();

            // 2. Process children if they exist
            if (request.getDropdownChild() != null && !request.getDropdownChild().isEmpty()) {
                List<DropdownChild> children = request.getDropdownChild().stream()
                    .map(childDto -> DropdownChild.builder()
                        .name(childDto.getDesignation())
                        .parent(parent) // Set the parent reference
                        .build())
                    .collect(Collectors.toList());
                
                parent.setDropdownChild(children);
            }

            // 3. Save the parent (cascade will save children automatically)
            DropdownParent savedParent = dropdownManagementRepository.save(parent);

            // 4. Return response
            return ResponseEntity.status(HttpStatus.CREATED).body(savedParent);

        } catch (Exception e) {
      
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        		    .body(Map.of(
        		        "message", "Failed to create dropdown",
        		        "error", e.getMessage(),
        		        "timestamp", LocalDateTime.now()
        		    ));
        }
    }
    
    @Transactional
    @Override
    public ResponseEntity<?> updateDropdown(DropdownManagementdto request) {
        try {
            // 1. Find existing parent or return 404
            DropdownParent existingParent = dropdownManagementRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Dropdown not found"));

            // 2. Update parent fields
            if (request.getDropdownName() != null) {
                existingParent.setDropdownName(request.getDropdownName());
            }
            if (request.getDescription() != null) {
                existingParent.setDescription(request.getDescription());
            }
            
            existingParent.setUpdatedAt(LocalDateTime.now());
           // existingParent.setUpdatedBy(request.getUpdatedBy());

            // 3. Process child updates if provided
            if (request.getDropdownChild() != null) {
                // Clear existing children (will be replaced)
                existingParent.getDropdownChild().clear();
                
                // Add all new children from DTO
                for (DropdownChilddto childDto : request.getDropdownChild()) {
                    DropdownChild child = new DropdownChild();
                    child.setName(childDto.getDesignation());
                    child.setParent(existingParent);
                    existingParent.getDropdownChild().add(child);
                }
            }

            // 4. Save the updated parent (cascade will handle children)
            DropdownParent updatedParent = dropdownManagementRepository.save(existingParent);

            // 5. Return success response
            return ResponseEntity.ok(updatedParent);

        } catch (RuntimeException e) {
            // Handle not found and other runtime exceptions
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
                ));
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to update dropdown",
                    "details", e.getMessage(),
                    "timestamp", LocalDateTime.now()
                ));
        }
    }
    
    @Transactional
    @Override
    public ResponseEntity<?> deleteDropdown(Integer parentId) {
        try {
            // 1. Delete children first using direct JPQL (bypasses persistence context issues)
            int childrenDeleted = dropdownManagementRepository.deleteChildrenByParentId(parentId);
            
            // 2. Delete parent using direct JPQL
            int parentDeleted = dropdownManagementRepository.deleteParentById(parentId);
            
            if (parentDeleted == 0) {
                throw new RuntimeException("Parent dropdown not found with ID: " + parentId);
            }
            
            // 3. Return response
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "parentDeleted", parentId,
                "childrenDeletedCount", childrenDeleted,
                "timestamp", LocalDateTime.now()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Deletion failed",
                    "details", e.getMessage()
                ));
        }
    }

    @Override
    public ResponseEntity<?> getAllDropdownLists() {
        try {
            // 1. Fetch all parent drop downs with children
            List<DropdownParent> parents = dropdownManagementRepository.findAllWithChildren();
            
            // 2. Convert to DTOs
            List<DropdownResponse> response = parents.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            // 3. Return successful response
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "error",
                    "message", "Failed to fetch dropdown lists",
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
                ));
        }
    }

    private DropdownResponse convertToDto(DropdownParent parent) {
        return DropdownResponse.builder()
            .id(parent.getId())
            .dropdownName(parent.getDropdownName())
            .description(parent.getDescription())
            .statusId(parent.getStatusId())
            .createdAt(parent.getCreatedAt())
            .updatedAt(parent.getUpdatedAt())
            .dropdownChild(convertChildrenToDto(parent.getDropdownChild()))
            .build();
    }

    private List<DropdownChildResponse> convertChildrenToDto(List<DropdownChild> children) {
        if (children == null || children.isEmpty()) {
            return Collections.emptyList();
        }
        
        return children.stream()
            .map(child -> DropdownChildResponse.builder()
                .id(child.getId())
                .designation(child.getName())
                .build())
            .collect(Collectors.toList());
    }

}
