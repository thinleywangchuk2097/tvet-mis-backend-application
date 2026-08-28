package com.moesd.tvet.mis.backend.application.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_programme_trainee_enrollment_subject_marks")
public class CourseEnrollmentTraineeAppSubjectMarks {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String subject;
	private String markScore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainee_id", // FK column table
			referencedColumnName = "id", // PK column 
			nullable = false)
	private CourseEnrollmentTraineeApp courseTrainee;
	
	
	
	
	

}
