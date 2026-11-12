package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create / update request for a publication or form.
 * file is optional on update (keeps the existing file when null).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationFormdto {
	private String category;
	/** yyyy-MM-dd */
	private String publishedDate;
	private String title;
	private String remarks;
	private Documentdto file;
}
