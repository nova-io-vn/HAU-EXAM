package com.aiservice.application.port.out;
import java.io.*;

public interface StoragePort {
    StoredFile store(String name, InputStream data);
    InputStream read(String key);
    record StoredFile(String key, String checksum) {
    }
}
