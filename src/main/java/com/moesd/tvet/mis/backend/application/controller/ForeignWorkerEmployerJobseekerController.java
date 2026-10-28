package com.moesd.tvet.mis.backend.application.controller;

import java.text.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.moesd.tvet.mis.backend.application.service.DatahubTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/data")
public class ForeignWorkerEmployerJobseekerController {
	
	@Value("${mis-jobseek-api}")
	String jobseekApiUrl;
	
	@Value("${mis-employer-api}")
	String employerApiUrl;
	
	@Value("${mis-foreign-worker-api}")
	String foreignWorkerApiUrl;

	@Value("${datahub.immigrant-api}")
	String immigrantApiUrl;

	@Value("${datahub.zest-employee-api}")
	String zestEmployeeApiUrl;
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private DatahubTokenService datahubTokenService;

	@GetMapping("/job-seeker-details/{citizenshipNo}")
	public ResponseEntity<Object> getJobSeekerDetails(@PathVariable String citizenshipNo) throws ParseException {

		// Step 1: Generate token (no model, no DB)
		String accessToken = datahubTokenService.generateNewToken();

		// Step 2: Prepare headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> request = new HttpEntity<>(headers);

		// Step 3: Call citizen API
		ResponseEntity<Object> response = restTemplate.exchange(jobseekApiUrl + citizenshipNo, HttpMethod.GET, request,
				Object.class);

		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
	}
	
	@GetMapping("/foreign-worker-details/{workPermitNo}")
	public ResponseEntity<Object> getForeignWorkerDetails(@PathVariable String workPermitNo) throws ParseException {

		// Step 1: Generate token (no model, no DB)
		String accessToken = datahubTokenService.generateNewToken();

		// Step 2: Prepare headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> request = new HttpEntity<>(headers);

		// Step 3: Call citizen API
		ResponseEntity<Object> response = restTemplate.exchange(foreignWorkerApiUrl + workPermitNo, HttpMethod.GET, request,
				Object.class);

		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
	}
	
	@GetMapping("/employer-details/{applicationNo}")
	public ResponseEntity<Object> getEmployerDetails(@PathVariable String applicationNo) throws ParseException {

		// Step 1: Generate token (no model, no DB)
		String accessToken = datahubTokenService.generateNewToken();

		// Step 2: Prepare headers
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> request = new HttpEntity<>(headers);

		// Step 3: Call citizen API
		ResponseEntity<Object> response = restTemplate.exchange(employerApiUrl + applicationNo, HttpMethod.GET, request,
				Object.class);

		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
	}

	// MoHCA immigration: /immigrantdetail/{workerid}
	@GetMapping("/immigrant-details/{workerId}")
	public ResponseEntity<Object> getImmigrantDetails(@PathVariable String workerId) throws ParseException {

		String accessToken = datahubTokenService.generateNewToken();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<Object> response = restTemplate.exchange(immigrantApiUrl + workerId, HttpMethod.GET, request,
				Object.class);

		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
	}

	// RCSC ZESt: /employeeFullDetailsByCID/{cid}
	@GetMapping("/civil-servant-details/{cid}")
	public ResponseEntity<Object> getCivilServantDetails(@PathVariable String cid) throws ParseException {

		String accessToken = datahubTokenService.generateNewToken();

		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);

		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<Object> response = restTemplate.exchange(zestEmployeeApiUrl + cid, HttpMethod.GET, request,
				Object.class);

		return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
	}
}
