package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
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
@Table(name = "tbl_accredited_course_dtls")
public class AccreditedCourse {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "application_no", nullable = false, unique = true)
	private String applicationNo;

	private String instituteId;

	private String courseId;

	private String courseFee;

	private String is_active;

	private String sectorId;

	private Date registration_date;

	private String validity_date;

	private Integer statusId;
	
	private Integer serviceId;
	
	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
	
	private String curriculumId;
	
	// Relationships - Using mappedBy to indicate the child owns the relationship
	@Builder.Default
	@OneToMany(mappedBy = "accreditedCourse", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AccreditedCourseQualityStandardResponse> qualityStandardResponses = new ArrayList<>();

}
