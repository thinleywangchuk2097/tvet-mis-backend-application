package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.Dzongkhag;

public interface DzongkhagRepository extends JpaRepository<Dzongkhag, Integer>{
	 // Custom query to get all Dzongkhags ordered by name
    @Query("SELECT d FROM Dzongkhag d ORDER BY d.dzonkhagName")
    List<Dzongkhag> findAllOrderByName();
}
