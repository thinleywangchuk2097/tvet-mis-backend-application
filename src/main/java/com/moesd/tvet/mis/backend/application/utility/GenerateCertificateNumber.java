package com.moesd.tvet.mis.backend.application.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.moesd.tvet.mis.backend.application.model.ServiceMaster;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateCertificateNumber {

    private static final String PREFIX = "BQPCA";

    private final ServiceMasterRepository serviceMasterRepository;

    public String generateCertificateNumber(Integer serviceId) {
        ServiceMaster serviceMaster = serviceMasterRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service master not found"));

        // Get current year (4 digits: YYYY)
        LocalDate currentDate = LocalDate.now();
        String year = currentDate.format(DateTimeFormatter.ofPattern("yyyy"));

        // Get and increment the sequence to build a unique ID
        Integer uniqueId = serviceMaster.getLicenseLastSequence();
        serviceMaster.setLicenseLastSequence(uniqueId + 1);
        serviceMasterRepository.save(serviceMaster);

        // Format: BQPCA/current year/uniqueId
        return String.format("%s/%s/%d", PREFIX, year, uniqueId);
    }
}