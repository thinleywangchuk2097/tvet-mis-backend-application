package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.FeedbackComplaint;

public interface FeedbackComplaintRepository extends JpaRepository<FeedbackComplaint, Long> {

	List<FeedbackComplaint> findByTypeOrderByCreatedAtDesc(String type);
}
