package com.hau.ai.application.port.out;import java.util.*;public interface AiResultRepository{String save(UUID jobId,String json);Optional<String> findByJobId(UUID jobId);}
