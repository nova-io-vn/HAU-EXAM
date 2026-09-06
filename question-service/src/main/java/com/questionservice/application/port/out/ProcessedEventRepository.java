package com.questionservice.application.port.out;
import java.util.UUID;
public interface ProcessedEventRepository { boolean exists(UUID eventId); void record(UUID eventId, String eventType); }
