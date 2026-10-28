package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Create / update one quality standard checklist node (parent, sub parent or checklist item). */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QualityStandardCheckListDto {
	private Integer id;          // null = create
	private Integer serviceId;
	private Integer parentId;    // 0 / null for a parent
	private String itemType;     // PARENT | SUB_PARENT | CHECKLIST
	private String dropdownName; // title / checklist text
	private String description;
	private String displayOrder;
	private Integer isActive;    // 1 active, 0 inactive
	private Integer updatedBy;
}
