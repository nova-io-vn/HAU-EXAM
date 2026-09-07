package com.questionservice.application.model;

public record ImageUploadCommand(byte[] bytes, String originalFilename, String contentType, long size) { }
