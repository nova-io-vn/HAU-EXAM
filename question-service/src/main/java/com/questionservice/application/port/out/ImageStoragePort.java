package com.questionservice.application.port.out;

import com.questionservice.application.model.ImageUploadCommand;
import com.questionservice.application.model.StoredImage;

public interface ImageStoragePort {
    StoredImage upload(ImageUploadCommand command, String folder);
    void delete(String publicId);
}
