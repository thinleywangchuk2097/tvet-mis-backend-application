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
public class SectorRequestDTO {
	private Integer id;
	private String sectorName;
	private char isActive;
	private List<OccupationRequestDTO> child;
}
