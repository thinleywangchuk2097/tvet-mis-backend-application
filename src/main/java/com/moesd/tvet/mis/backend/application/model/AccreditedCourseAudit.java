package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Snapshot of tbl_accredited_course_dtls taken before a renewal overwrites it. */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
// application_no is referenced by the quality standard response audit (FK needs an index)
@Table(name = "tbl_accredited_course_dtls_audit", indexes = @Index(name = "idx_ac_audit_application_no", columnList = "application_no"))
public class AccreditedCourseAudit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** id of the row in tbl_accredited_course_dtls */
	private Long accreditedCourseId;

	@Column(name = "application_no")
	private String applicationNo;

	private Integer instituteId;

	private Integer curriculumId;

	private Long ncsId;

	private String feesPerTrainee;

	private String enrolmentCapacity;

	private Date registration_date;

	private String validity_date;

	private Integer statusId;

	private Integer serviceId;

	private String leadTrainerCidNo;

	private Integer genderId;

	private String leadTrainerName;

	private String professionalExperience;

	private Integer qualificationId;

	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

	private LocalDateTime renewalDate;

	// Relationships - Using mappedBy to indicate the child owns the relationship
	@Builder.Default
	@OneToMany(mappedBy = "accreditedCourseAudit", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AccreditedCourseQualityStandardResponseAudit> qualityStandardResponses = new ArrayList<>();
}
