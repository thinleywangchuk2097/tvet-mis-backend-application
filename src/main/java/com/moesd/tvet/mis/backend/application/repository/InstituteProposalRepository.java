package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.InstituteProposal;

import jakarta.persistence.Tuple;

public interface InstituteProposalRepository extends JpaRepository<InstituteProposal, Integer>{
	@Query(value =  
			"SELECT "
					+ "  p.id, "
					+ "  p.service_id, "
					+ "  t.task_status_id, "
					+ "  p.application_no, "
					+ "  p.ownership_type_id, "
					+ "  p.registration_no, "
					+ "  p.company_name, "
					+ "  p.proposed_institute_name, "
					+ "  p.dzongkhag_id, "
					+ "  p.exact_location, "
					+ "  p.telephone_no, "
					+ "  p.mobile_no, "
					+ "  p.email_id, "
					+ "  p.sector_id, "
					+ "  p.course_id, "
					+ "  p.activity_level_id, "
					+ "  p.other_ownership_type_id, "
					+ "  p.other_name, "
					+ "  p.other_address, "
					+ "  p.promoter_citizen_id, "
					+ "  p.promoter_name, "
					+ "  p.status_id, "
					+ "  p.created_by, "
					+ "  p.created_at, "
					+ "  p.updated_by, "
					+ "  p.updated_at, "
					+ "  (SELECT "
					+ "    JSON_ARRAYAGG( "
					+ "      JSON_OBJECT( "
					+ "        'id', "
					+ "        pp.id, "
					+ "        'typeOfOwnerId', "
					+ "        pp.type_of_owner_id, "
					+ "        'partnerCidNo', "
					+ "        pp.partner_cid_no, "
					+ "        'partnerName', "
					+ "        pp.partner_name, "
					+ "        'partnerCompanyRegistrationNo', "
					+ "        pp.partner_company_registration_no, "
					+ "        'partnerCompanyName', "
					+ "        pp.partner_company_name "
					+ "      ) "
					+ "    ) "
					+ "  FROM "
					+ "    tbl_institute_proposal_partnership pp "
					+ "  WHERE pp.application_no = p.application_no) AS partnerships, "
					+ "  (SELECT "
					+ "    JSON_ARRAYAGG( "
					+ "      JSON_OBJECT( "
					+ "        'id', "
					+ "        d.id, "
					+ "        'name', "
					+ "        d.document_name, "
					+ "        'url', "
					+ "        d.upload_url "
					+ "      ) "
					+ "    ) "
					+ "  FROM "
					+ "    tbl_document_master d "
					+ "  WHERE d.application_no = p.application_no) AS documents "
					+ "FROM "
					+ "  tbl_institute_proposal p "
					+ "  LEFT JOIN tbl_task_dtls t "
					+ "    ON p.application_no = t.application_no "
					+ "WHERE p.application_no = ?", nativeQuery = true)
		List<Tuple> getInstituteDetails(String application_no);
	
	  	Optional<InstituteProposal> findByApplicationNo(String applicationNo);

}
