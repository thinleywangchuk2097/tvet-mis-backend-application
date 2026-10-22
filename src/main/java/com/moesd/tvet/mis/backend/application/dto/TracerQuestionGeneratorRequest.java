package com.moesd.tvet.mis.backend.application.dto;

import lombok.Data;
import java.util.List;

@Data
public class TracerQuestionGeneratorRequest {
	private String applicationNo;
    private String tracerTitle;
    private String parentTracerTypeId;
    private String subTracerTypeId;
    private List<TracerQuestionDTO> questions;
}