package com.moesd.tvet.mis.backend.application.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.TOTAnnouncement;

public interface TOTAnnouncementRepository extends JpaRepository<TOTAnnouncement, Long> {
	
	Optional<TOTAnnouncement> findById(Long id);
}
