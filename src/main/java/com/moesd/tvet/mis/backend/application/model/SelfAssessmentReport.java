package com.moesd.tvet.mis.backend.application.model;

import java.util.ArrayList;
import java.util.Date;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Self Assessment Report (TVET Provider Accreditation) - service id 19.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_self_assessment_report_dtls")
public class SelfAssessmentReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "application_no", nullable = false, unique = true)
	private String applicationNo;

	private Integer instituteId;

	private Integer serviceId;

	private Integer statusId;

	private Double totalScore;

	private String grade;

	private Date submittedDate;

	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "selfAssessmentReport", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SelfAssessmentReportQualityResponse> qualityStandardResponses = new ArrayList<>();

	@Builder.Default
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "selfAssessmentReport", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SelfAssessmentReportMember> members = new ArrayList<>();
}
