package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;

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
	
	@Column(name="assigned_role_id")
	private Integer assignedRoleId;
	
	@Column(name="next_role_id")
	private Integer nextRoleId;
	
	@Column(name="service_id")
	private Integer serviceId;
	
	@Column(name="assigned_user_id")
	private String assignedUserId;
	
	@Column(name="next_user_id")
	private String nextUserId;
	
	@Column(name="status_id")
	private Integer statusId;
	
	private Integer createdBy;
	
	private LocalDateTime createdAt;
}
