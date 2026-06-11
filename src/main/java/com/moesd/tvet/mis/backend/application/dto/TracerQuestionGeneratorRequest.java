package com.moesd.tvet.mis.backend.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class TracerQuestionGeneratorRequest {
	private String tracerTitle;
	private String parentTracerTypeId;
	private String subTracerTypeId;
	private String applicationNo;
	private List<TracerQuestionDTO> questions;
}
