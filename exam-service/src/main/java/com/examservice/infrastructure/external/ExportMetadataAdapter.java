package com.examservice.infrastructure.external;

import com.examservice.application.port.out.ExportMetadataPort;
import com.examservice.domain.exception.DomainException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.*;

@Component
public class ExportMetadataAdapter implements ExportMetadataPort {
    private final RestClient client;private final ObjectMapper mapper;private final String users;private final String questions;
    public ExportMetadataAdapter(@LoadBalanced RestClient.Builder builder,ObjectMapper mapper,
            @Value("${exam.user-service-url:http://localhost:8082}")String users,
            @Value("${exam.question-service-url}")String questions){this.client=builder.build();this.mapper=mapper;this.users=users;this.questions=questions;}
    public Exporter exporter(String token){try{JsonNode n=data(client.get().uri(users+"/api/v1/users/me").headers(h->h.setBearerAuth(token)).retrieve().body(String.class));return new Exporter(UUID.fromString(n.path("id").asText()),n.path("fullName").asText(),n.path("lecturerCode").asText());}catch(Exception e){throw new DomainException("Unable to resolve authenticated exporter profile");}}
    public String facultyName(String code){try{JsonNode n=data(client.get().uri(users+"/api/v1/public/faculties?size=100").retrieve().body(String.class));JsonNode values=n.path("content");if(!values.isArray())values=n.path("items");for(JsonNode f:values)if(code.equalsIgnoreCase(f.path("code").asText()))return f.path("name").asText(code);return code;}catch(Exception e){throw new DomainException("Unable to resolve faculty metadata");}}
    public Subject subject(UUID id,String token){try{JsonNode n=data(client.get().uri(questions+"/api/v1/subjects").headers(h->h.setBearerAuth(token)).retrieve().body(String.class));if(n.isArray())for(JsonNode s:n)if(id.toString().equals(s.path("id").asText()))return new Subject(s.path("code").asText("SUBJECT"),s.path("name").asText(id.toString()));return new Subject("SUBJECT",id.toString());}catch(Exception e){throw new DomainException("Unable to resolve subject metadata");}}
    private JsonNode data(String raw)throws Exception{JsonNode root=mapper.readTree(raw);JsonNode data=root.path("data");if(data.isMissingNode()||data.isNull())throw new DomainException("Invalid metadata response");return data;}
}
