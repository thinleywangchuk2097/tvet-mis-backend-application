package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TuitionDetailsdto {
	private String classLevel;
	private String duration;
	private String fees;
	private String subjects;
	private String tutorCid;
	private String tutorName;
	private String tutorQualification;
	

}
