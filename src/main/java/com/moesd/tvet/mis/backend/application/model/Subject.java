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

	/** Add Subject application (service 56) */
	private String applicationNo;

	/** workflow status: 55 submitted, 56 verified, 58 rejected, 57 approved (null = before the workflow) */
	private Integer applicationStatusId;

	private String subjectName;

	/** Academic Qualification (dropdown child of parent 18) */
	private Integer classLevelId;

	private Integer totalDurationHours;

	private Integer instituteId;

	private Integer statusId;

	private String description;

	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;

}
