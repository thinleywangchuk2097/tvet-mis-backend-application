package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.TotApp;

import jakarta.persistence.Tuple;

public interface TotRepository extends JpaRepository<TotApp, Long>{

	@Query(value = "SELECT * FROM tbl_tot_app", nativeQuery = true)
	List<Tuple> getCourseDetailsAnnouncementByUserId();

}
