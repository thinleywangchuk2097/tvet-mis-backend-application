package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.moesd.tvet.mis.backend.application.service.DatahubTokenService;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/public/gyalsung")
public class GyalsungApiController {

	@Value("${gyalsung-data-api}")
	private String gyalsungDataUrl;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DatahubTokenService datahubTokenService;

	@GetMapping("/gyalsungDetails/{citizenshipNo}")
    public ResponseEntity<?> getGyalsungDetails(@PathVariable String citizenshipNo) {

        try {
            // 1. Generate token (same DataHub token as the other DataHub APIs)
            String accessToken = datahubTokenService.generateNewToken();

            // 2. Prepare headers for GET request
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // 3. Correct API format (QUERY PARAM, not path variable)
            //    .../getGyalsungDetails?cid=XXXXXXXXXXX  (no trailing "/" before "?")
            String base = stripTrailingSlashes(gyalsungDataUrl.trim());
            String url = UriComponentsBuilder.fromUriString(base)
                    .queryParam("cid", citizenshipNo)
                    .toUriString();

            // 4. Call external API
            ResponseEntity<Object> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    Object.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (HttpStatusCodeException e) {
            log.error("Gyalsung lookup failed for {}: {} {}", citizenshipNo, e.getStatusCode(),
                    e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", "Failed to retrieve Gyalsung details"));
        } catch (Exception e) {
            log.error("Gyalsung lookup failed for {}", citizenshipNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to retrieve Gyalsung details"));
        }
    }

	// removes trailing "/" characters with a simple loop (no regex, so no backtracking)
	private static String stripTrailingSlashes(String url) {
		int end = url.length();
		while (end > 0 && url.charAt(end - 1) == '/') {
			end--;
		}
		return url.substring(0, end);
	}
}
