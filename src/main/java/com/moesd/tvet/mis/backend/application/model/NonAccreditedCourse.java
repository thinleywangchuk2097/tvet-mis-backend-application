package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "tbl_non_accredited_course_dtls")
public class NonAccreditedCourse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "application_no", nullable = false, unique = true)
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

	private String curriculumId;

	private Integer statusId;

	private Date registrationDate;

	private Date validityDate;
	
	private Integer serviceId;
	
	private String createdBy;

	private LocalDateTime createdAt;

	private Integer updatedBy;

	private LocalDateTime updatedAt;

	// Relationships - Using mappedBy to indicate the child owns the relationship
	@Builder.Default
	@OneToMany(mappedBy = "nonAccreditedCourse", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<NonAccreditedCourseQualityStandardResponse> qualityStandardResponses = new ArrayList<>();

}
