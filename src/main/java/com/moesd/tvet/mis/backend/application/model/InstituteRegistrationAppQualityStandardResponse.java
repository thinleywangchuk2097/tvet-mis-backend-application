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
@Table(name = "tbl_institute_registration_quality_response_app")
public class InstituteRegistrationAppQualityStandardResponse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "standard_id", nullable = false)
	private Long standardId;

	@Column(name = "response_id", nullable = false)
	private String responseId;
	
	@Column(name = "remarks", columnDefinition = "TEXT")
	private String remarks;
	
	// Many-to-One relationship with InstituteRegistration
	@ManyToOne
	@JoinColumn(name = "application_no", // FK column in response table
			referencedColumnName = "application_no" // column in InstituteRegistration application_no
	)
	private InstituteRegistrationApp instituteRegistration;
}
