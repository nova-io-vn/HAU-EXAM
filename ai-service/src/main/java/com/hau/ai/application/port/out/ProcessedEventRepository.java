package com.hau.ai.application.port.out;import java.util.UUID;public interface ProcessedEventRepository{boolean exists(UUID id);void record(UUID id,String type);}
