package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.moesd.tvet.mis.backend.application.model.NcsApp;

import jakarta.persistence.Tuple;

@Repository
public interface NcsRepository extends JpaRepository<NcsApp, Integer> {
    
    @Query(value = "SELECT "
    		+ "  a.*, "
    		+ "  (SELECT "
    		+ "    JSON_ARRAYAGG( "
    		+ "      JSON_OBJECT( "
    		+ "        'id', "
    		+ "        b.unit_id, "
    		+ "        'unitCode', "
    		+ "        b.unit_code, "
    		+ "        'unitTitle', "
    		+ "        b.unit_title "
    		+ "      ) "
    		+ "    ) "
    		+ "  FROM "
    		+ "    tbl_ncs_units b "
    		+ "  WHERE b.ncs_id = a.id) AS units, "
    		+ "  (SELECT "
    		+ "    IFNULL ( "
    		+ "      JSON_ARRAYAGG( "
    		+ "        JSON_OBJECT( "
    		+ "          'id', "
    		+ "          d.id, "
    		+ "          'documentName', "
    		+ "          d.document_name, "
    		+ "          'url', "
    		+ "          d.upload_url "
    		+ "        ) "
    		+ "      ), "
    		+ "      JSON_ARRAY () "
    		+ "    ) "
    		+ "  FROM "
    		+ "    tbl_document_master d "
    		+ "  WHERE d.application_no = a.application_no) AS documents "
    		+ "FROM "
    		+ "  tbl_ncs_app_dtls a", nativeQuery = true)
    List<Tuple> getNcsDetails();
    
    
    @Query(value = "SELECT "
    		+ "  a.id, "
    		+ "  a.sector_id, "
    		+ "  a.occupation_id, "
    		+ "  a.certification_id, "
    		+ "  a.programme_title "
    		+ "FROM "
    		+ "  tbl_ncs_app_dtls a "
    		+ "WHERE a.sector_id = ? "
    		+ "  AND a.occupation_id = ? "
    		+ "  AND a.certification_id = ?", nativeQuery = true)
    List<Tuple> getAlreadyNcsDetailsExist(Integer sector_id, Integer occupation_id, Integer certification_id);
    
    
    @Query(value = "SELECT "
    		+ "  a.id, "
    		+ "  a.programme_title "
    		+ "FROM "
    		+ "  tbl_ncs_app_dtls a "
    		+ "WHERE a.id = ?", nativeQuery = true)
    List<Tuple> getProgrammeTitleById(Integer programmeId);
}