package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class DropdownManagementServiceImpl implements DropdownManagementService {

	private final DropdownManagementRepository dropdownManagementRepository;

	@Override
	public ResponseEntity<?> createDropdown(DropdownManagementdto request) {
		try {
			// 1. Create parent drop down
			DropdownParent parent = DropdownParent.builder().dropdownName(request.getDropdownName())
					.description(request.getDescription()).createdAt(LocalDateTime.now())
					.createdBy(request.getCreatedBy()).statusId("1") // Default status
					.build();

			// 2. Process children if they exist
			if (request.getDropdownChild() != null && !request.getDropdownChild().isEmpty()) {
				List<DropdownChild> children = request.getDropdownChild().stream()
						.map(childDto -> DropdownChild.builder().name(childDto.getDesignation()).parent(parent) // Set
																												// the
																												// parent
																												// reference
								.build())
						.collect(Collectors.toList());

				parent.setDropdownChild(children);
			}

			// 3. Save the parent (cascade will save children automatically)
			DropdownParent savedParent = dropdownManagementRepository.save(parent);

			// 4. Return response
			return ResponseEntity.status(HttpStatus.CREATED).body(savedParent);

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message",
					"Failed to create dropdown", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	@Transactional
	@Override
	public ResponseEntity<?> updateDropdown(DropdownManagementdto request) {
	    DropdownParent parent = dropdownManagementRepository.findById(request.getId())
	            .orElseThrow(() -> new RuntimeException("Dropdown not found"));

	    // Update parent fields
	    parent.setDropdownName(request.getDropdownName());
	    parent.setDescription(request.getDescription());
	    parent.setUpdatedAt(LocalDateTime.now());

	    List<DropdownChild> existingChildren = parent.getDropdownChild();
	    Map<Integer, DropdownChild> existingMap = existingChildren.stream()
	            .collect(Collectors.toMap(DropdownChild::getId, c -> c));

	    List<DropdownChild> updatedChildren = new ArrayList<>();

	    for (DropdownChilddto dto : request.getDropdownChild()) {
	        if (dto.getId() != null && existingMap.containsKey(dto.getId())) {
	            // 1️Update existing child
	            DropdownChild child = existingMap.get(dto.getId());
	            child.setName(dto.getDesignation());
	            updatedChildren.add(child);
	        } else {
	            // 2️ New child
	            DropdownChild newChild = new DropdownChild();
	            newChild.setName(dto.getDesignation());
	            newChild.setParent(parent);
	            updatedChildren.add(newChild);
	        }
	    }

	    // 3️ If a child exists in DB but is not sent in the update request → delete it
	    existingChildren.removeIf(c -> updatedChildren.stream()
	            .noneMatch(u -> c.getId() != null && c.getId().equals(u.getId())));

	    // Add new children (with null IDs)
	    existingChildren.addAll(updatedChildren.stream()
	            .filter(c -> c.getId() == null)
	            .toList());

	    // Save parent with updated children
	    DropdownParent saved = dropdownManagementRepository.save(parent);

	    return ResponseEntity.ok(saved);
	}

	@Transactional
	@Override
	public ResponseEntity<?> deleteDropdown(Integer parentId) {
		try {
			// 1. Delete children first using direct JPQL (bypasses persistence context
			// issues)
			int childrenDeleted = dropdownManagementRepository.deleteChildrenByParentId(parentId);

			// 2. Delete parent using direct JPQL
			int parentDeleted = dropdownManagementRepository.deleteParentById(parentId);

			if (parentDeleted == 0) {
				throw new RuntimeException("Parent dropdown not found with ID: " + parentId);
			}

			// 3. Return response
			return ResponseEntity.ok(Map.of("status", "success", "parentDeleted", parentId, "childrenDeletedCount",
					childrenDeleted, "timestamp", LocalDateTime.now()));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("status", "error", "message", "Deletion failed", "details", e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<?> getAllDropdownLists() {
		try {
			// 1. Fetch all parent drop downs with children
			List<DropdownParent> parents = dropdownManagementRepository.findAllWithChildren();

			// 2. Convert to DTOs
			List<DropdownResponse> response = parents.stream().map(this::convertToDto).collect(Collectors.toList());

			// 3. Return successful response
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "error", "message",
					"Failed to fetch dropdown lists", "error", e.getMessage(), "timestamp", LocalDateTime.now()));
		}
	}

	private DropdownResponse convertToDto(DropdownParent parent) {
		return DropdownResponse.builder().id(parent.getId()).dropdownName(parent.getDropdownName())
				.description(parent.getDescription()).statusId(parent.getStatusId()).createdAt(parent.getCreatedAt())
				.updatedAt(parent.getUpdatedAt()).dropdownChild(convertChildrenToDto(parent.getDropdownChild()))
				.build();
	}

	private List<DropdownChildResponse> convertChildrenToDto(List<DropdownChild> children) {
		if (children == null || children.isEmpty()) {
			return Collections.emptyList();
		}

		return children.stream()
				.map(child -> DropdownChildResponse.builder().id(child.getId()).designation(child.getName()).build())
				.collect(Collectors.toList());
	}

	// @Override
	// public List<DropdownChild> getByParentId(String parentId) {

	// List<DropdownChild> children =
	// dropdownManagementRepository.findChildByParentId(parentId);
	// return children;
	// }

}
