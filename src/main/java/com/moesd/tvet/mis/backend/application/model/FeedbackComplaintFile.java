package com.moesd.tvet.mis.backend.application.model;

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

/** Supporting documents attached to a complaint (tbl_feedback_complaint). */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_feedback_complaint_file")
public class FeedbackComplaintFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "complaint_id", nullable = false)
	private Long complaintId;

	@Column(name = "file_name", length = 500)
	private String fileName;

	@Column(name = "content_type", length = 200)
	private String contentType;

	@Column(name = "file_size")
	private Long fileSize;

	@Column(name = "upload_url", length = 1000)
	private String uploadUrl;

	@Column(name = "created_at")
	private Date createdAt;
}
