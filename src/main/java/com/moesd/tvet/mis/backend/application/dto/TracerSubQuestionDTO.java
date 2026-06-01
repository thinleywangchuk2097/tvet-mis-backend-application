package com.moesd.tvet.mis.backend.application.dto;
import java.util.List;
import lombok.Data;

@Data
public class TracerSubQuestionDTO {
	private Integer subQuestionOrder;
	private String questionText;
	private Integer questionTypeId;
	private Integer required;
	private List<String> options;
	private List<String> multipleTextFields;
	private Integer ratingScale;
}
