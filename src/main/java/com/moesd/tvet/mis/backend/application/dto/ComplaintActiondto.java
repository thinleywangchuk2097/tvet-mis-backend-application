package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** TVET QC update of a complaint: new status and the action taken. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintActiondto {
	/** SUBMITTED, UNDER_REVIEW, RESOLVED or REJECTED */
	private String status;
	private String actionTaken;
}
