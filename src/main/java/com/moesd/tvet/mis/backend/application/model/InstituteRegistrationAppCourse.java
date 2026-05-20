package com.moesd.tvet.mis.backend.application.model;

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
@Table(name = "tbl_institute_registration_course_app")
public class InstituteRegistrationAppCourse {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String sectorId;
	private String courseId;
	private String theoryHours;
	private String practicalHours;
	private String ojtHours;
	private String feesPerTrainee;
	private String enrollmentCapacity;
	private String courseLevelId;
	// Many-to-One relationship with InstituteRegistration
	@ManyToOne
	@JoinColumn(name = "application_no", // FK column in trainer table
			referencedColumnName = "application_no" // column in InstituteRegistration
	)
	private InstituteRegistrationApp instituteRegistration;
}
