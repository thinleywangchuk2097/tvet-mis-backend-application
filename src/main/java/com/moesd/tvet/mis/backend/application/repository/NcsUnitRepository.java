package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.moesd.tvet.mis.backend.application.model.NcsUnit;

public interface NcsUnitRepository extends JpaRepository<NcsUnit, Integer> {
    
    List<NcsUnit> findByNcsApp_PublicationId(Integer publicationId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM NcsUnit u WHERE u.ncsApp.publicationId = :publicationId")
    void deleteByNcsAppPublicationId(@Param("publicationId") Integer publicationId);
}