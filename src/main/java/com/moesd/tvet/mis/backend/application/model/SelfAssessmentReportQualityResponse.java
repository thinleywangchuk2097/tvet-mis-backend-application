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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Points per sub-indicator of the Self Assessment Report.
 * selfPoint   : points claimed by the institute (TVET provider)
 * earnedPoint : points confirmed by the verifier/accreditor (editable in verification)
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_self_assessment_report_quality_response")
public class SelfAssessmentReportQualityResponse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "standard_id", nullable = false)
	private Long standardId;

	@Column(name = "self_point")
	private Integer selfPoint;

	@Column(name = "earned_point")
	private Integer earnedPoint;

	@Column(name = "justification", columnDefinition = "TEXT")
	private String justification;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	@JoinColumn(name = "application_no", referencedColumnName = "application_no")
	private SelfAssessmentReport selfAssessmentReport;
}
