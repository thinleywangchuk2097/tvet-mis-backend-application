package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOTCertificateRequestDto {
	private String applicationNo;
	private List<Integer> trainerIds;
	private Integer generatedBy;
}
