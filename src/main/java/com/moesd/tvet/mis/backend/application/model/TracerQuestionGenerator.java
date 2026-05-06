package com.moesd.tvet.mis.backend.application.model;

import java.util.ArrayList;
import java.util.List;
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
@Table(name = "tbl_tracer_question_dtls")
public class TracerQuestionGenerator {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "application_no", nullable = false, unique = true)
	private String applicationNo;
	
	private Integer questionTypeId;
	
	@Builder.Default
	@OneToMany(mappedBy = "tracerQuestionGenerator", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<TracerSubQuestionGenerator> tracerSubQuestionGenerator = new ArrayList<>();
}
