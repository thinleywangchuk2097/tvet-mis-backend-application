package com.moesd.tvet.mis.backend.application.model;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Student enrolled by an SES centre in one of its approved subjects. */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_student_enrollment_dtls")
public class StudentEnrollment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private Integer genderId;

	/** School Level: Academic Qualification (dropdown child of parent 18) */
	private Integer schoolLevelId;

	/** approved subject of tbl_subject_dtls */
	private Long subjectId;

	private Integer enrollmentYear;

	private Integer instituteId;

	/** 1 active, 0 deleted */
	private Integer statusId;

	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
}
