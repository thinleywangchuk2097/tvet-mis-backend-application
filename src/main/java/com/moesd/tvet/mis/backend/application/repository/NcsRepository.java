package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.NcsApp;

import jakarta.persistence.Tuple;

public interface NcsRepository extends JpaRepository<NcsApp, Long>{

	@Query(value = "SELECT * FROM tbl_tot_app", nativeQuery = true)
	List<Tuple> getCourseDetailsAnnouncementByUserId();

}
