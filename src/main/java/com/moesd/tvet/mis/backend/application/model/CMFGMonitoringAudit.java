package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;

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
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_cmfg_monitoring_dtl_audit")
public class CMFGMonitoringAudit {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "application_no")
	private String applicationNo;
	
	@Column(name = "standard_id", nullable = false)
	private Long standardId;

	@Column(name = "response_id", nullable = false)
	private String responseId;
	
	@Column(name = "remarks", columnDefinition = "TEXT")
	private String remarks;
	
	private String instituteId;
	
	private String createdBy;

	private LocalDateTime createdAt;

	private Integer updatedBy;

	private LocalDateTime updatedAt;
	
	@ManyToOne
	@JoinColumn(name = "parent_id")
	private CMFGMonitoring cmfgMonitoring;

}
