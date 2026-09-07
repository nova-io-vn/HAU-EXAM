package com.questionservice.application.service;

import com.questionservice.application.exception.ImageUploadException;
import com.questionservice.application.model.ImageUploadCommand;
import com.questionservice.application.model.StoredImage;
import com.questionservice.application.port.out.ImageStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ImageStorageService {
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final ImageStoragePort storage;
    private final long maxSize;

    public ImageStorageService(ImageStoragePort storage, @Value("${cloudinary.image-max-size-bytes:5242880}") long maxSize) { this.storage = storage; this.maxSize = maxSize; }
    public StoredImage upload(ImageUploadCommand command, String folder) {
        if (command == null || command.bytes() == null || command.bytes().length == 0) throw new ImageUploadException("Image file is empty");
        if (command.size() > maxSize) throw new ImageUploadException("Image file is too large");
        if (command.contentType() == null || !ALLOWED_TYPES.contains(command.contentType().toLowerCase())) throw new ImageUploadException("Unsupported image type");
        return storage.upload(command, folder);
    }
}
