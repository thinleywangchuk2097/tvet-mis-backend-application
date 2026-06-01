package com.moesd.tvet.mis.backend.application.utility;

import java.security.SecureRandom;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateTracerUniqueId {

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();

    public String generateTracerUniqueId(String applicationNo) {
        StringBuilder formattedTracerUniqueId = new StringBuilder();
        // generate 16-character random string
        for (int i = 0; i < 16; i++) {
            int index = random.nextInt(CHARACTERS.length());
            formattedTracerUniqueId.append(CHARACTERS.charAt(index));
        }

        return applicationNo + formattedTracerUniqueId.toString();
    }
}