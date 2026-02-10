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
@Table(name="tbl_workflow_dtls_audit")
public class WorkFlowListAudit {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Column(name="application_no")
	private String applicationNo;
	
	@Column(name="application_name")
	private String applicationName;
	
	@Column(name="status_id")
	private Integer statusId;
	
	@Column(name="service_id")
	private Integer serviceId;
	
	@Column(name="actor_id")
	private Integer actorId;
	
	@Column(name="role_id")
	private long roleId;
	
	@Column(name="action_date")
    private Date actionDate;
	
	@Column(name="created_at")
    private Date createdAt;
    
    @Column(name="wf_remarks", columnDefinition = "TEXT")
    private String wfRemarks;
    
    @ManyToOne
    @JoinColumn(name = "workflow_id")
    private WorkFlowList workFlow;
}
