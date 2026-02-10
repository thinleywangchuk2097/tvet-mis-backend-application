package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Privilegedto {
	private Long id;
	private String disPlayOrder;
	private boolean  isDisplay;
	private Long parentId;
	private String privilegeName;
	private String routeName;
    private String icon;
}
