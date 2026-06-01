package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;

import lombok.Data;

@Data
public class TracerSendRequestDTO {
	private Integer parentTracerTypeId;
    private Integer subTracerTypeId;
    private String applicationNo;
    private List<TraineeDTO> selectedTrainees;
    private List<EmployerDTO> selectedEmployers;
}
