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
@Table(name = "tbl_course_enrollment_app")
public class CourseEnrollmentApp {

	@Id
	@Column(name = "application_no", nullable = false, unique = true)
	private String applicationNo;

	private String instituteId;

	private String courseId;

	private String feesPerTrainee;

	private Integer serviceId;

	private Integer statusId;

	private String enrollmentCapacity;

	private Date applicationStartDate;

	private Date applicationEndDate;

	private Date courseStartDate;

	private Date courseEndDate;

	private Date caStartDate;

	private Date caEndDate;

	private Integer certificationLevelId;

	private Integer fundingSourceId;

	private Integer trainingLocationId;

	private String courseDescription;

	@Column(columnDefinition = "TEXT")
	private String remarks;

	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

	//One course → many trainees
	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
	private List<CourseEnrollmentTraineeApp> trainees;

}
