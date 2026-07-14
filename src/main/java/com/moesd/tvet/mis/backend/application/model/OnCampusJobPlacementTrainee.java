package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDate;
import java.util.Date;

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
@Table(name = "tbl_campus_job_placement_trainee_dtls")
public class OnCampusJobPlacementTrainee {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String traineeCid;
	private String traineeName;
	private Integer courseId;
	private String position;
	private Integer employmentStatusId;
	private String salary;
	private String remarks;
	private Integer instituteId;
	private LocalDate placementDate;
	private LocalDate startDate;
	private Integer createdBy;
	private Date createdAt;
	private Integer updatedBy;
	private Date updatedAt;
	
	@ManyToOne
	@JoinColumn(name = "firm_id")
	private OnCampusJobPlacementFirm firm;
	
	
}
