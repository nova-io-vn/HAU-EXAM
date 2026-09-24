package com.notificationservice.application.port.out;
public interface SupportImageStorage { StoredImage upload(byte[] bytes,String filename,String contentType,long size); long maxSize(); record StoredImage(String url,String publicId,String fileName,String contentType,long fileSize){} }
