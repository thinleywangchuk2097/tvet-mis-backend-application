package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.DocumentFile;
import jakarta.persistence.Tuple;

public interface DocumentRepository extends JpaRepository<DocumentFile, Long>{
	@Query(value = "SELECT a.id,a.application_no,a.document_name,a.upload_url FROM tbl_document_master a WHERE a.application_no=?1", nativeQuery = true)
	List<Tuple> getUploadDocuments(String applicvationNo);
}
