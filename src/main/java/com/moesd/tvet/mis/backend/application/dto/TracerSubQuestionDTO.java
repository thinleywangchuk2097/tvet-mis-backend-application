package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.Data;

@Data
public class TracerSubQuestionDTO {
	private String clientId;
	private Integer subQuestionOrder;
	private String questionText;
	private Integer questionTypeId;
	private Integer required;
	private List<String> options;
	private List<String> multipleTextFields;
	private Integer ratingScale;

	// NEW
	private String showCondition;
	private String dependsOnClientId;
	private String showWhenOption;
	private Integer maxSelections;
}