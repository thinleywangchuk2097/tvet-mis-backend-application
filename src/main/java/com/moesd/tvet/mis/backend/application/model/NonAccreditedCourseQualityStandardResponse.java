package com.moesd.tvet.mis.backend.application.model;

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

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_non_accredited_course_quality_standard_Response")
public class NonAccreditedCourseQualityStandardResponse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "standard_id", nullable = false)
	private Long standardId;

	@Column(name = "response_id", nullable = false)
	private String responseId;
	
	@Column(name = "remarks", columnDefinition = "TEXT")
	private String remarks;
	
	// Many-to-One relationship with NonAccreditedCourse
	@ManyToOne
	@JoinColumn(name = "application_no", // FK column in response table
			referencedColumnName = "application_no" // column in InstituteNonAccreditedCourse application_no
	)
	private NonAccreditedCourse nonAccreditedCourse;
}
