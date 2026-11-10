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

/** Snapshot of tbl_accredited_course_quality_standard_response taken before a renewal overwrites it. */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_accredited_course_quality_standard_response_audit")
public class AccreditedCourseQualityStandardResponseAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** id of the row in tbl_accredited_course_quality_standard_response */
	private Long qualityStandardResponseId;

	@Column(name = "standard_id")
	private Long standardId;

	@Column(name = "response_id")
	private String responseId;

	@Column(name = "remarks", columnDefinition = "TEXT")
	private String remarks;

	// Many-to-One relationship with AccreditedCourseAudit
	@ManyToOne
	@JoinColumn(name = "application_no", // FK column in response audit table
			referencedColumnName = "application_no" // column in AccreditedCourseAudit application_no
	)
	private AccreditedCourseAudit accreditedCourseAudit;
}
