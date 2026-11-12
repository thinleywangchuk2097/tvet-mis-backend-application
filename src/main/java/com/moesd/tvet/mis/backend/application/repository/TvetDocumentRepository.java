package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.TvetDocument;

public interface TvetDocumentRepository extends JpaRepository<TvetDocument, Long> {

	List<TvetDocument> findByCategoryOrderByPublishedDateDescIdDesc(String category);

	List<TvetDocument> findAllByOrderByPublishedDateDescIdDesc();
}
