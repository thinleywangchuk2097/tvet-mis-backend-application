package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Ncsdto {
	private String applicationNo;
	private Integer occupationId;
	private Integer certificationId;
	private Integer sectorId;
	private Integer serviceId;
	private String programmeTitle;
	private Date validityDate;
	private Integer createdBy;
	private Integer updatedBy;
	private List<NcsUnitDto> units;
	private Documentdto[] documents;
}