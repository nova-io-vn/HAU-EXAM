package com.notificationservice.presentation.response;
public record EmailSettingsResponse(String host,int port,String username,String from,boolean auth,boolean startTls,boolean configured) {}
