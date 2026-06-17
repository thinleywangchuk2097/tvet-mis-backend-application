package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ncsdto {
	private String ncsCode;
	private Integer occupationId;
	private Integer certificationId;
	private String courseTitle;
	private Date validityDate;
	private String publicationType;
	private Integer createdBy;
	private Integer updatedBy;
}
