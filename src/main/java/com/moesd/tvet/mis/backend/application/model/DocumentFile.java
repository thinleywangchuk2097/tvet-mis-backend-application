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

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "tbl_document_master")
public class DocumentFile {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "application_no")
	private String applicationNo;
	
	@Column(name = "document_type")
	private String documentType;
	
	@Column(name = "document_name")
	private String documentName;
	
	@Column(name = "service_id")
	private Integer serviceId;
	
	@Column(name = "upload_url")
	private String uploadUrl;
	
	@Column(name = "attachment_type")
	private String attachmentType;
	
	@Column(name = "created_at")
	private Date createdAt;
	
	@Column(name = "created_by")
	private Integer createdBy;
}
