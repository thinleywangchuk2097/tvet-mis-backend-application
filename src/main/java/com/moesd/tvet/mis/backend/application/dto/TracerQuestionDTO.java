package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.Data;

@Data
public class TracerQuestionDTO {
	private String clientId; // "q-1"
	private Integer questionOrder;
	private String questionText;
	private Integer questionTypeId;
	private Integer required;
	private List<String> options;
	private List<String> multipleTextFields;
	private Integer ratingScale;

	// NEW
	private String showCondition;
	private String dependsOnClientId; // "q-1"
	private String showWhenOption;
	private Integer maxSelections;

	private List<TracerSubQuestionDTO> subQuestions;
}