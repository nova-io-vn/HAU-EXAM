package com.userservice.application.port.out;

import com.userservice.application.model.ImageUploadCommand;
import com.userservice.application.model.StoredImage;

public interface ImageStoragePort {
    StoredImage upload(ImageUploadCommand command, String folder);
    void delete(String publicId);
}
