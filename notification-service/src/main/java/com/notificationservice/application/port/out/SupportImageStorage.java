package com.notificationservice.application.port.out;
public interface SupportImageStorage { StoredImage upload(byte[] bytes,String filename,String contentType,long size); record StoredImage(String url,String publicId){} }
