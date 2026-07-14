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
            + "a.publication_id as publicationId, "
            + "a.course_title as courseTitle, "
            + "DATE_FORMAT(a.validity_date, '%Y-%m-%d') as validityDate, "
            + "a.publication_type as publicationType, "
            + "a.occupation_id as occupationId, "
            + "a.certification_id as certificationId, "
            + "a1.occupation_name as occupationName, "
            + "a2.sector_name as sectorName, "
            + "a3.name as certificationName, "
            + "COALESCE("
            + "  (SELECT JSON_ARRAYAGG("
            + "    JSON_OBJECT('unitCode', u.unit_code, 'unitTitle', u.unit_title, 'unitId', u.unit_id)"
            + "   ) "
            + "   FROM tbl_ncs_units u "
            + "   WHERE u.publication_id = a.publication_id AND u.is_active = 1"
            + "  ), "
            + "  JSON_ARRAY()"
            + ") as units, "
            + "COALESCE("
            + "  (SELECT JSON_ARRAYAGG("
            + "    JSON_OBJECT("
            + "      'name', a4.document_name, "
            + "      'url', a4.upload_url, "
            + "      'contentType', a4.document_type, "
            + "      'documentId', a4.id"
            + "    )"
            + "   ) "
            + "   FROM tbl_document_master a4 "
            + "   WHERE a4.application_no = CAST(a.publication_id AS CHAR) COLLATE utf8mb4_general_ci "
            + "     AND a4.attachment_type = 'NCS_PUBLICATION'"
            + "  ), "
            + "  JSON_ARRAY()"
            + ") as documents "
            + "FROM tbl_ncs_curriculum_publication a "
            + "LEFT JOIN tbl_occupation_master a1 ON a.occupation_id = a1.id "
            + "LEFT JOIN tbl_sector_master a2 ON a1.sector_id = a2.id "
            + "LEFT JOIN tbl_dropdown_child_master a3 ON a.certification_id = a3.id AND a3.parent_id = 10 "
            + "GROUP BY a.publication_id, a.course_title, a.validity_date, a.publication_type, "
            + "a.occupation_id, a.certification_id, a1.occupation_name, a2.sector_name, a3.name "
            + "ORDER BY a.publication_id DESC", nativeQuery = true)
    List<Tuple> getCourseDetailsAnnouncementByUserId();
}