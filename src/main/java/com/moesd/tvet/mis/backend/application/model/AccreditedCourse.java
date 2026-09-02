package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import java.util.List;
import java.time.LocalDateTime;
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

	private Integer instituteId;
	
	//for time being
    //private Integer courseId;

	private Integer curriculumId;
	
	private String feesPerTrainee;
	
	private String enrolmentCapacity;

	private Date registration_date;

	private String validity_date;

	private Integer statusId;
	
	private Integer serviceId;
	
	private String LeadTrainerCidNo;
	
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
	@OneToMany(mappedBy = "accreditedCourse", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AccreditedCourseQualityStandardResponse> qualityStandardResponses = new ArrayList<>();

}
