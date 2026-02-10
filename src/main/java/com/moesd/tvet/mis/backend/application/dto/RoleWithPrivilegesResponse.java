package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleWithPrivilegesResponse {
	private Integer id;
	private String roleName;
	private String description;
	private List<Long> assignedPrivilegeId;
	private LocalDateTime updatedAt;  // Add this
	private String updatedBy;   
}
