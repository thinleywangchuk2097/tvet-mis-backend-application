package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
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
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_non_accredited_course_dtls")
public class InstituteNonAccreditedCourse {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false, unique = true)
	private String applicationNo;
	
	@Column(name = "institute_id")
	private String instituteId;
	
	private String courseTitle;

	private String theoryHour;

	private String practicalHour;

	private String ojtHour;

	private String feesPerTrainee;
	
	private String enrolmentCapacity;
	
	private String certificateLevelId;
	
	private String curriculumTypeId;
	
	private Integer statusId;
	
	private Date registrationDate;
	
	private Date validityDate;
	
	private String createdBy;

	private LocalDateTime createdAt;

	private Integer updatedBy;

	private LocalDateTime updatedAt;
}
