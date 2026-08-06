package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "tbl_course_enrollment_trainee_app")
public class CourseEnrollmentTraineeApp {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String applicationNo;

	private String dosCertificateNo;

	private String certificateNo;

	private Integer resultStatusId;

	private String applicantName;

	private String cidNo;

	private String referenceNo;

	private Date dob;

	private String emailId;

	private String mobileNo;

	private Integer qualificationId;

	private Integer genderId;

	private Integer traineeTypeId;

	private String internalAssessment;

	private String theoryAssessment;

	private String practicalAssessment;

	private String vivaAssessment;

	private Integer statusId;

	private Integer reAssessmentNo;
	
	private String remarks;
	
	private Integer employmentStatusId;

	private Integer academicQualificationId;

	private String guardianName;

	private String guardianMobileNo;

	private Integer guardianMaritalStatusId;

	private Integer guardianOccupationId;

	private Integer presentDzongkhagId;

	private Integer presentGewogId;

	private Integer disabilityTypeId;

	private Integer guardianQualificationId;
	
	private String examYear;
	
	private String schoolName;
	
	private String stream;
	
	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_enrol_app_no", // FK column in trainee table
			referencedColumnName = "application_no", // PK column in course table
			nullable = false)

	private CourseEnrollmentApp course;

	// One course → many trainees applied
	@OneToMany(mappedBy = "courseTrainee", cascade = CascadeType.ALL)
	private List<CourseEnrollmentTraineeAppSubjectMarks> traineeMarks;

}
