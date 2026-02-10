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
public class DropdownManagementdto {
	private Integer id;
	private String dropdownName;
    private String description;
    private Integer createdBy;
    private List<DropdownChilddto> dropdownChild;
}
