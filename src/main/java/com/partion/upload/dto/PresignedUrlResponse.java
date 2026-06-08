package com.partion.upload.dto;

import lombok.Getter;

@Getter
public class PresignedUrlResponse {

    private final String presignedUrl;
    private final String imageUrl;
    private final String objectKey;

    public PresignedUrlResponse(String presignedUrl, String imageUrl, String objectKey) {
        this.presignedUrl = presignedUrl;
        this.imageUrl = imageUrl;
        this.objectKey = objectKey;
    }
}