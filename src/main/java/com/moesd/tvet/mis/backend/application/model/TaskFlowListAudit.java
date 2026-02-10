package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_task_dtls_audit")
public class TaskFlowListAudit {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(name = "application_no")
	private String applicationNo;

	@Column(name = "assigned_user_id")
	private String assignedUserId;

	@Column(name = "task_status_id")
	private Integer taskStatusId;

	@Column(name = "action_date")
	private Date actionDate;

	@Column(name = "audit_action_date")
	private Date auditActionDate;

	@Column(name = "workflow_id")
	private Long workflowId;

	@Column(name = "assigned_role_id", columnDefinition = "TEXT", length = 255)
	private String assignedRoleId;

	@Column(name = "save_remarks", columnDefinition = "TEXT")
	private String saveRemarks;

	@Column(name = "location_id")
	private Integer locationId;
	
	@ManyToOne
	@JoinColumn(name = "task_id")
	private TaskFlowList taskFlow;
}
