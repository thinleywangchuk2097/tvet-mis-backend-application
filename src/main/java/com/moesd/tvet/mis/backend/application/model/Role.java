package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_role")
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true)
	private String roleName;

	private String description;

	private Integer unitId;

	private Integer createdBy;

	private LocalDateTime createdAt;

	private Integer updatedBy;
	
	private String statusId;

	private LocalDateTime  updatedAt;

	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserRole> userRoles;

	@OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RolePrivilege> privileges;
	
	 // Add this method to properly handle privilege assignment
    public void addPrivilege(Privilege privilege) {
        RolePrivilege rolePrivilege = RolePrivilege.builder()
            .role(this)
            .privilege(privilege)
            .build();
        privileges.add(rolePrivilege);
    }
}
