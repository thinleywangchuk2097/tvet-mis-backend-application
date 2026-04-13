package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.Sector;

public interface SectorRepository extends JpaRepository<Sector, Integer> {
	List<Sector> findAll();
}
