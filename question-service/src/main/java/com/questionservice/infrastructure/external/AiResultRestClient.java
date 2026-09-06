package com.questionservice.infrastructure.external;
import com.questionservice.application.port.out.AiResultClient;
import com.questionservice.domain.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.*;
import java.util.*;
@Component public class AiResultRestClient implements AiResultClient {
 private final RestClient client; private final ObjectMapper mapper; private final String baseUrl,internalToken;
 public AiResultRestClient(RestClient.Builder b,ObjectMapper m,@Value("${ai.service.url}")String u,@Value("${ai.internal.result-token}")String t){if(t==null||t.isBlank())throw new IllegalStateException("INTERNAL_SERVICE_TOKEN is required for Question to retrieve AI results");client=b.build();mapper=m;baseUrl=u;internalToken=t;}
 public GeneratedResult fetch(UUID jobId,String resultReference){if(resultReference==null||resultReference.isBlank())throw new IllegalArgumentException("AI result reference is required");String body=client.get().uri(baseUrl+"/api/v1/internal/ai/jobs/{id}/result",jobId).header("X-Internal-Service-Token",internalToken).retrieve().body(String.class);try{JsonNode root=mapper.readTree(body),data=root==null?null:root.get("data");if(data==null||!data.isArray()||data.isEmpty())throw new IllegalArgumentException("AI result must contain questions");List<GeneratedQuestion> out=new ArrayList<>();int i=0;for(JsonNode n:data)out.add(parse(n,jobId+":"+i++));return new GeneratedResult(List.copyOf(out));}catch(IllegalArgumentException e){throw e;}catch(Exception e){throw new IllegalArgumentException("Invalid AI result payload",e);}}
 private GeneratedQuestion parse(JsonNode n,String source){String content=text(n,"question"),correct=text(n,"correctAnswer");Difficulty d=Difficulty.valueOf(text(n,"difficulty"));JsonNode a=n.get("options");if(a==null||!a.isArray()||a.size()<2)throw new IllegalArgumentException("AI options are invalid");List<GeneratedOption> os=new ArrayList<>();int i=0;for(JsonNode o:a){String label=text(o,"label");os.add(new GeneratedOption(label,text(o,"content"),null,null,label.equals(correct),i++));}UUID topic=n.hasNonNull("topicId")?UUID.fromString(n.get("topicId").asText()):null;return new GeneratedQuestion(source,content,null,null,QuestionType.SINGLE_CHOICE,d,topic,List.copyOf(os));}
 private String text(JsonNode n,String f){JsonNode v=n==null?null:n.get(f);if(v==null||!v.isTextual()||v.asText().isBlank())throw new IllegalArgumentException("AI result field is required: "+f);return v.asText();}
}
