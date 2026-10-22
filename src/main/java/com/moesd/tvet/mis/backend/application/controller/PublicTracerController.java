package com.moesd.tvet.mis.backend.application.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.configuration.TracerSubmitRateLimiter;
import com.moesd.tvet.mis.backend.application.dto.SurveyResponseRequestDTO;
import com.moesd.tvet.mis.backend.application.model.TracerSurveySendDetails;
import com.moesd.tvet.mis.backend.application.service.TracerQuestionGeneratorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.node.ObjectNode;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/tracer")
public class PublicTracerController {

    private final TracerQuestionGeneratorService tracerQuestionGeneratorService;
    private final TracerSubmitRateLimiter tracerSubmitRateLimiter;

    @GetMapping("/survey/{uniqueId}")
    public ResponseEntity<?> getSurveyByUniqueId(@PathVariable String uniqueId) {
        try {
            TracerSurveySendDetails survey =
                    tracerQuestionGeneratorService.getSurveyByUniqueId(uniqueId);
            if (survey == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Survey not found",
                                "timestamp", LocalDateTime.now()));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", survey,
                    "timestamp", LocalDateTime.now()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch survey");
        }
    }

    @GetMapping("/get-tracer/{application_no}")
    public ResponseEntity<?> getTracerDetailsByApplicationNo(
            @PathVariable("application_no") String applicationNo) {
        try {
            List<ObjectNode> tracerDetails = tracerQuestionGeneratorService
                    .getTracerDetailsByApplicationNo(applicationNo);
            return ResponseEntity.ok(Map.of("success", true, "data", tracerDetails));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch tracer details");
        }
    }

    @GetMapping("/get-tracer-question-dropdown")
    public ResponseEntity<?> getTracerQuestionDropdownType() {
        try {
            List<ObjectNode> data =
                    tracerQuestionGeneratorService.getTracerQuestionDropdownType();
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch dropdown data");
        }
    }

    @PostMapping("/submit-survey-response")
    public ResponseEntity<?> submitSurveyResponse(
            @RequestBody SurveyResponseRequestDTO request,
            HttpServletRequest http) {
        try {
            // 0. Per-IP rate limit
            String ip = getClientIp(http);
            if (!tracerSubmitRateLimiter.resolve(ip).tryConsume(1)) {
                log.warn("Rate limit exceeded for tracer submit from IP: {}", ip);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .header("Retry-After", "60")
                        .body(Map.of(
                                "message", "Too many submissions. Please try again later.",
                                "timestamp", LocalDateTime.now()));
            }

            // 1. Survey must exist
            TracerSurveySendDetails survey = tracerQuestionGeneratorService
                    .getSurveyByUniqueId(request.getUniqueId());
            if (survey == null) {
                log.warn("Survey not found: uniqueId={}", request.getUniqueId());
                return notFound();
            }

            // 2. applicationNo must match the SEND transaction
            if (!survey.getApplicationNo().equals(request.getApplicationNo())) {
                log.warn("AppNo mismatch: survey.sendAppNo={} reqAppNo={}",
                        survey.getApplicationNo(), request.getApplicationNo());
                return notFound();
            }

            // 3. Resolve the TRACER application number (questions live here)
            String tracerApplicationNo = survey.getQuestionApplicationNo();
            if (tracerApplicationNo == null || tracerApplicationNo.isBlank()) {
                tracerApplicationNo = survey.getApplicationNo();
            }

            log.info("Submit: uniqueId={} sendAppNo={} tracerAppNo={}",
                    request.getUniqueId(),
                    survey.getApplicationNo(),
                    tracerApplicationNo);

            Set<Long> validQuestionIds = tracerQuestionGeneratorService
                    .getQuestionIdsByApplicationNo(tracerApplicationNo);

            log.info("Valid question count for {}: {}",
                    tracerApplicationNo, validQuestionIds.size());

            if (validQuestionIds.isEmpty()) {
                log.warn("No questions for tracer application {}",
                        tracerApplicationNo);
                return notFound();
            }

            for (SurveyResponseRequestDTO.ResponseItem r : request.getResponses()) {
                if (r.getQuestionId() == null
                        || !validQuestionIds.contains(r.getQuestionId())) {
                    log.warn("Invalid questionId={} for tracerAppNo={}",
                            r.getQuestionId(), tracerApplicationNo);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of(
                                    "message", "Invalid question: " + r.getQuestionId(),
                                    "timestamp", LocalDateTime.now()));
                }
            }

            // 4. Persist — service re-derives the correct application internally
            tracerQuestionGeneratorService.saveSurveyResponses(
                    request.getUniqueId(), request.getResponses());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Survey responses submitted successfully",
                    "timestamp", LocalDateTime.now()));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message", "Already submitted",
                            "timestamp", LocalDateTime.now()));
        } catch (Exception e) {
            log.error("Survey submission failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Failed to submit survey responses",
                            "timestamp", LocalDateTime.now()));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private ResponseEntity<?> notFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "message", "Survey not found",
                        "timestamp", LocalDateTime.now()));
    }
}