package com.moesd.tvet.mis.backend.application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(name="tbl_role_service")
public class RoleService {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(name="current_role_id",columnDefinition = "TEXT")
	private String roleId;
	
	@Column(name="service_id")
	private Integer serviceId;
	
	@Column(name="next_role_id",columnDefinition = "TEXT")
	private String nextRoleId;
	
	@Column(name="next_status_id")
	private Integer nextStatusId;
}
