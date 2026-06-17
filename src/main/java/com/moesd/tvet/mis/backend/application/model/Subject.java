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

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_subject_dtls")
public class Subject {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String subjectCode;

	private String subjectName;

	private String creditHours;

	private String theoryHours;
	
	private Integer instituteId;

	private String practicalHours;

	private Integer statusId;

	private String description;

	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

}
