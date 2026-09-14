package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.TuitionAnnouncement;

import jakarta.persistence.Tuple;

public interface TuitionAnnouncementRepository extends JpaRepository<TuitionAnnouncement, Long>{
	
	@NativeQuery("SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_tuition_announcement_dtls a "
			+ "WHERE a.institute_id = ?")
	List<Tuple> getAllTuitionAnnouncements(Integer institute_id);

}
