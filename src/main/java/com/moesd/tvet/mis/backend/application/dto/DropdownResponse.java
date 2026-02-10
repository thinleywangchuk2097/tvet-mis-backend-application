package com.moesd.tvet.mis.backend.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DropdownResponse {
	private Integer id;
	private String dropdownName;
    private String description;
	private String statusId;
	private List<DropdownChildResponse> dropdownChild;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
