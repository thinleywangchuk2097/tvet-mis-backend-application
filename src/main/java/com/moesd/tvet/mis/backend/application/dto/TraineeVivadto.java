package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TraineeVivadto {
	private Long traineeId;
	private Integer vivaAssessment;
	private Integer practicalAssessment;
	
	//new added
    private Integer internalAssessment;

    // approver's remarks for the trainee (saved like the theory/practical path)
    private String remarks;
}
