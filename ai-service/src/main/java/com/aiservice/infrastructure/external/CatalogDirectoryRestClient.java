package com.aiservice.infrastructure.external;

import com.aiservice.application.port.out.CatalogDirectoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import java.util.*;

@Component
public class CatalogDirectoryRestClient implements CatalogDirectoryPort {
    private final RestClient client; private final ObjectMapper mapper; private final String baseUrl; private final String token;
    public CatalogDirectoryRestClient(RestClient.Builder builder, ObjectMapper mapper,
            @Value("${question.service.url:http://question-service:8083}") String baseUrl,
            @Value("${ai.internal.service-token:${INTERNAL_SERVICE_TOKEN:dev-internal-token}}") String token) {
        this.client=builder.build(); this.mapper=mapper; this.baseUrl=baseUrl; this.token=token;
    }
    @Override public List<CatalogContext> resolve(List<Lookup> lookups) {
        if (lookups == null || lookups.isEmpty()) return List.of();
        try {
            String body=client.post().uri(baseUrl+"/api/v1/internal/catalog-contexts")
                    .header("X-Internal-Service-Token",token).body(lookups).retrieve().body(String.class);
            var root=mapper.readTree(body);
            var data=root.has("data")?root.get("data"):root;
            List<CatalogContext> result=new ArrayList<>();
            data.forEach(n->result.add(new CatalogContext(uuid(n,"subjectId"),text(n,"subjectCode"),text(n,"subjectName"),text(n,"managingFacultyId"),
                    uuid(n,"chapterId"),text(n,"chapterCode"),text(n,"chapterName"),uuid(n,"topicId"),text(n,"topicCode"),text(n,"topicName"))));
            return result;
        } catch(Exception ignored) { return Collections.nCopies(lookups.size(), null); }
    }
    private UUID uuid(tools.jackson.databind.JsonNode n,String field){String v=n.path(field).asString(null);return v==null||v.isBlank()?null:UUID.fromString(v);}
    private String text(tools.jackson.databind.JsonNode n,String field){String v=n.path(field).asString(null);return v==null||v.isBlank()?null:v;}
}
