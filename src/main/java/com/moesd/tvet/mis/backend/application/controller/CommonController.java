package com.moesd.tvet.mis.backend.application.controller;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.moesd.tvet.mis.backend.application.model.Dzongkhag;
import com.moesd.tvet.mis.backend.application.service.CommonService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/common")
public class CommonController {
	
	private final CommonService commonService;

	@GetMapping("/get-dzongkhags")
	public ResponseEntity<List<Dzongkhag>> getAllDzongkhags() {
		List<Dzongkhag> dzongkhagLists = commonService.getAllDzongkhags();
		return ResponseEntity.ok(dzongkhagLists);
	}

	@GetMapping("/download-document")
	public void downloadFile(
            @RequestParam String upload_url,
            @RequestParam String fileName,
            HttpServletResponse response) throws IOException {

        // Decode URL-encoded path
        String decodedPath = URLDecoder.decode(upload_url, StandardCharsets.UTF_8);
        File file = new File(decodedPath);

        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
            return;
        }

        // Detect MIME type
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        // Set response headers
        response.setContentType(mimeType);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setContentLengthLong(file.length());

        // Stream file content
        Files.copy(file.toPath(), response.getOutputStream());
        response.flushBuffer();
    }
}
