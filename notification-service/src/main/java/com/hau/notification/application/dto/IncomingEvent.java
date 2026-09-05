package com.hau.notification.application.dto;import java.util.*;public record IncomingEvent(UUID eventId,String eventType,UUID correlationId,Map<String,Object> payload){}
