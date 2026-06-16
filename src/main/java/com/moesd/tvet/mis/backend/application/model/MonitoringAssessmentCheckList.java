package com.moesd.tvet.mis.backend.application.model;

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
@Table(name = "tbl_monitoring_assessment_checklist_dtls")
public class MonitoringAssessmentCheckList {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long standardId;
	
	private String responseId;
	
	private String remarks;
	
	@ManyToOne
	@JoinColumn(
	    name = "assessmentId",          
	    referencedColumnName = "id" 
	)
	
	private MonitoringAssessment monitoringAssessment;

	
}
