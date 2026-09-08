package com.userservice.application.model;

public record StoredImage(String url, String secureUrl, String publicId, String format, Integer width, Integer height, Long bytes) { }
