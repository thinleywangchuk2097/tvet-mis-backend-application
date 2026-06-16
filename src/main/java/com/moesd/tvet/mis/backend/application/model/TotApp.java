package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "tbl_tot_app")
public class TotApp {
	
	@Id
	@Column(name = "application_no", nullable = false, unique = true)
	private String applicationNo;

	private String courseId;

	private Integer statusId;

	//private String totalNoTrainees;

	private Date applicationStartDate;

	private Date applicationEndDate;

	private Date courseStartDate;

	private Date courseEndDate;

	private String courseDescription;

	@Column(columnDefinition = "TEXT")

	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

	//One course → many trainees
	//@OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
	//private List<CourseEnrollmentTraineeApp> trainees;

}
