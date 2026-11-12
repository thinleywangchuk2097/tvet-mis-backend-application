package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDate;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Downloadable documents shown on the public site: Publications and Forms.
 * Managed (create / edit / delete) by the TVET QC role (22).
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_publication_forms")
public class PublicationForm {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** PUBLICATION or FORM */
	@Column(name = "category", nullable = false, length = 20)
	private String category;

	@Column(name = "title", nullable = false, length = 500)
	private String title;

	@Column(name = "published_date")
	private LocalDate publishedDate;

	@Column(name = "remarks", columnDefinition = "TEXT")
	private String remarks;

	@Column(name = "file_name", length = 500)
	private String fileName;

	@Column(name = "content_type", length = 200)
	private String contentType;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "upload_url", length = 1000)
	private String uploadUrl;

	@Column(name = "created_by", length = 100)
	private String createdBy;

	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "updated_by", length = 100)
	private String updatedBy;

	@Column(name = "updated_at")
	private Date updatedAt;
}
