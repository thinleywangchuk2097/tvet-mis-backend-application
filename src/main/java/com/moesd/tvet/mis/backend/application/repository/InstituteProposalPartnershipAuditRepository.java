package com.moesd.tvet.mis.backend.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.InstituteProposalPartnershipAudit;

public interface InstituteProposalPartnershipAuditRepository
		extends JpaRepository<InstituteProposalPartnershipAudit, Long> {
}
