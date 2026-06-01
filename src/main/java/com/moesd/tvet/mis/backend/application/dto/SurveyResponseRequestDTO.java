package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseRequestDTO {
    private String uniqueId;
    private String applicationNo;
    private Long surveyId;
    private List<ResponseItem> responses;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseItem {
        private Long questionId;
        private Object response;
        private Boolean isSubQuestion;
    }
}
