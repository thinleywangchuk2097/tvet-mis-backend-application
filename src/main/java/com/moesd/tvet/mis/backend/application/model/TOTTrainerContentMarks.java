package com.moesd.tvet.mis.backend.application.model;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Marks obtained by a trainer for one course content of a TOT module. */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_tot_trainer_content_marks_dtls",
	uniqueConstraints = @UniqueConstraint(columnNames = { "application_no", "trainer_id", "course_content_id" }))
public class TOTTrainerContentMarks {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "application_no")
	private String applicationNo;
	private Long appliedId;
	@Column(name = "trainer_id")
	private Integer trainerId;
	private Long moduleId;
	@Column(name = "course_content_id")
	private Long courseContentId;
	@Column(precision = 10, scale = 2)
	private BigDecimal fullMark;
	@Column(precision = 10, scale = 2)
	private BigDecimal marksObtained;
	private Integer createdBy;
	private Date createdAt;
	private Integer updatedBy;
	private Date updatedAt;
}
