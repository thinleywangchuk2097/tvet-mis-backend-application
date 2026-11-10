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
 * ACCREDITATORS section of the Self Assessment Report.
 * memberType = ACCREDITOR  -> registered accreditor (name + CID auto filled)
 * memberType = COORDINATOR -> programme co-ordinator (name + designation typed)
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_self_assessment_report_member")
public class SelfAssessmentReportMember {

	public static final String TYPE_ACCREDITOR = "ACCREDITOR";
	public static final String TYPE_COORDINATOR = "COORDINATOR";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "member_type", nullable = false, length = 30)
	private String memberType;

	@Column(name = "user_id")
	private String userId;

	private String name;

	private String designation;

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@ManyToOne
	@JoinColumn(name = "application_no", referencedColumnName = "application_no")
	private SelfAssessmentReport selfAssessmentReport;
}
