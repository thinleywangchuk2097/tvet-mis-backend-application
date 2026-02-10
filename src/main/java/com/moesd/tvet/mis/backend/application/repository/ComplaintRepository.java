package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.Complaint;
import jakarta.persistence.Tuple;

public interface ComplaintRepository extends JpaRepository<Complaint, Long>{
	@Query(value =  
		    "SELECT "
		    + "  a.id, "
		    + "  a.applicant_name, "
		    + "  a.application_no, "
		    + "  a.complaint_type, "
		    + "  a.description, "
		    + "  a.emaild_id, "
		    + "  a.mobile_no, "
		    + "  a.priority_level, "
		    + "  GROUP_CONCAT(b.document_name) as document_names, "
		    + "  GROUP_CONCAT(b.upload_url) as upload_urls "
		    + "FROM "
		    + "  tbl_complaint a "
		    + "  LEFT JOIN tbl_document_master b "
		    + "    ON a.application_no = b.application_no "
		    + "WHERE a.application_no = ? "
		    + "GROUP BY a.id", nativeQuery = true)
		List<Tuple> getComplaintDetails(String application_no);
}
