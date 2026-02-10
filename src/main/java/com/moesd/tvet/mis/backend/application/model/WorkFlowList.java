package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name="tbl_workflow_dtls")
public class WorkFlowList {
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
	
	@Column(name="action_date")
    private Date actionDate;
    
    @Column(name="wf_remarks", columnDefinition = "TEXT")
    private String wfRemarks;
    
    @Column(name="role_id")
    private Integer roleId;
    
    @Column(name = "update_by")
    private Integer updateBy;
    
    @Column(name = "update_on")
    private Date updateOn;
}
