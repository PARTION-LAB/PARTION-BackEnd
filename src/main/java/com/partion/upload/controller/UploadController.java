package com.partion.upload.controller;

import com.partion.upload.dto.PresignedUrlRequest;
import com.partion.upload.dto.PresignedUrlResponse;
import com.partion.upload.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/products/presigned-url")
    public ResponseEntity<PresignedUrlResponse> createProductImagePresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        PresignedUrlResponse response =
                uploadService.createProductImagePresignedUrl(request);

        return ResponseEntity.ok(response);
    }
}