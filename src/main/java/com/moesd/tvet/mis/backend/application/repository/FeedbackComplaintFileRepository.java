package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.FeedbackComplaintFile;

public interface FeedbackComplaintFileRepository extends JpaRepository<FeedbackComplaintFile, Long> {

	List<FeedbackComplaintFile> findByComplaintIdOrderByIdAsc(Long complaintId);

	Optional<FeedbackComplaintFile> findByIdAndComplaintId(Long id, Long complaintId);
}
