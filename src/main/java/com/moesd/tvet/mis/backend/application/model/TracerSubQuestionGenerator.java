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

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_tracer_sub_question_dtls")
public class TracerSubQuestionGenerator {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Many-to-One relationship with AccreditedCourse
	@ManyToOne
	@JoinColumn(name = "application_no", // FK column in response table
			referencedColumnName = "application_no" // column in AccreditedCourse application_no
	)
	private TracerQuestionGenerator tracerQuestionGenerator;
}
