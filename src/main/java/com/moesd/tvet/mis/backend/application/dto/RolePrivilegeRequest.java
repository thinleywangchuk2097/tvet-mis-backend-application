package com.moesd.tvet.mis.backend.application.dto;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePrivilegeRequest {
	private Integer id;
    private String roleName;
    private String description;
    private Set<Long> privileges;
    private Set<Long> assignedPrivilegeId;
}
