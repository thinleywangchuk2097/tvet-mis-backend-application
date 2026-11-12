package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.PublicationForm;

public interface PublicationFormRepository extends JpaRepository<PublicationForm, Long> {

	List<PublicationForm> findByCategoryOrderByPublishedDateDescIdDesc(String category);

	List<PublicationForm> findAllByOrderByPublishedDateDescIdDesc();
}
