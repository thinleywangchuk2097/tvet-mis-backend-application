package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Create / update a public notice. showUntil is yyyy-MM-dd (shown to the end of that day). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicNoticedto {
	private String title;
	private String textMessage;
	private String showUntil;
}
