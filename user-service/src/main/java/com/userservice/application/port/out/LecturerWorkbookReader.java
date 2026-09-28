package com.userservice.application.port.out;

import java.io.InputStream;
import java.util.List;

public interface LecturerWorkbookReader {
    record Row(int rowNumber, String lecturerCode, String fullName, String phone, String academicRank,
               String academicDegree, String email, String facultyCode) { }
    List<Row> read(String fileName, String contentType, long size, InputStream input);
    byte[] template();
}
