package com.questionservice.presentation.response;

import com.questionservice.application.model.StoredImage;

public record ImageUploadResponse(String url, String secureUrl, String publicId, String format, Integer width, Integer height, Long bytes) {
    public static ImageUploadResponse from(StoredImage image) { return new ImageUploadResponse(image.url(), image.secureUrl(), image.publicId(), image.format(), image.width(), image.height(), image.bytes()); }
}
