package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.Occupation;

public interface OccupationRepository extends JpaRepository<Occupation, Integer> {

	List<Occupation> findAll();

	List<Occupation> findBySectorId(Integer sectorId);

}
