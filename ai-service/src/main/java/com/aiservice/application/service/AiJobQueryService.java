package com.aiservice.application.service;

import com.aiservice.application.model.*;
import com.aiservice.application.model.AiJobViews.*;
import com.aiservice.application.port.out.*;
import com.aiservice.application.port.out.CatalogDirectoryPort.*;
import com.aiservice.domain.exception.*;
import com.aiservice.domain.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AiJobQueryService {
    private static final DateTimeFormatter CODE_DATE=DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC);
    private static final Set<String> SAFE_CONFIG=Set.of("count","difficulty","language","includeImages","additionalRequirements","analysisType");
    private final AiJobRepository jobs; private final AiResultRepository results; private final DocumentRepository documents;
    private final UserDirectoryPort users; private final CatalogDirectoryPort catalogs; private final AiRuntimeMetadataPort runtime; private final ObjectMapper mapper; private final Clock clock; private final Duration stuckThreshold;
    public AiJobQueryService(AiJobRepository jobs,AiResultRepository results,DocumentRepository documents,UserDirectoryPort users,
                             CatalogDirectoryPort catalogs,AiRuntimeMetadataPort runtime,ObjectMapper mapper){this(jobs,results,documents,users,catalogs,runtime,mapper,Clock.systemUTC(),Duration.ofMinutes(15));}
    @Autowired
    public AiJobQueryService(AiJobRepository jobs,AiResultRepository results,DocumentRepository documents,UserDirectoryPort users,
                             CatalogDirectoryPort catalogs,AiRuntimeMetadataPort runtime,ObjectMapper mapper,Clock clock,
                             @Value("${ai.jobs.stuck-threshold:PT15M}") Duration stuckThreshold){this.jobs=jobs;this.results=results;this.documents=documents;this.users=users;this.catalogs=catalogs;this.runtime=runtime;this.mapper=mapper;this.clock=clock;this.stuckThreshold=stuckThreshold;}

    public WorkspacePage<Summary> all(int page,int size){
        if(page<0||size<1||size>100)throw new IllegalArgumentException("Invalid pagination");
        var source=jobs.findAll(page,size);return mapPage(source);
    }

    public Detail detail(UUID id,UUID actor,String role,String faculty){
        AiJob job=jobs.findById(id).orElseThrow(()->new NotFoundException("AI job not found"));
        authorize(job,actor,role,faculty);
        String result=results.findByJobId(id).orElse(null);
        UserDirectoryPort.UserProfile creator=users.find(job.requestedBy()).orElse(null);
        CatalogContext context=job.subjectId()==null?null:first(catalogs.resolve(List.of(lookup(job))));
        Summary summary=summary(job,creator,context,count(result));
        DocumentContext document=job.documentId()==null?null:documents.findById(job.documentId())
                .map(d->new DocumentContext(d.id(),d.originalName(),d.contentType(),d.size(),"EXTRACTED")).orElse(null);
        JsonNode request=parse(job.requestJson());Map<String,Object> config=new LinkedHashMap<>();
        SAFE_CONFIG.forEach(key->{JsonNode value=request.path(key);if(!value.isMissingNode()&&!value.isNull())config.put(key,safeValue(value));});
        String description=redact(text(request,"description"));
        var provider=runtime.current();
        return new Detail(summary,job.documentId()!=null?"DOCUMENT":description!=null?"DESCRIPTION":"DIRECT",document,description,
                Collections.unmodifiableMap(config),provider.provider(),provider.model(),countField(result,"acceptedCount","savedCount"),countField(result,"rejectedCount","validationRejectedCount"),result,job.errorCode(),safeError(job.errorCode()));
    }

    private WorkspacePage<Summary> mapPage(WorkspacePage<AiJob> source){
        Set<UUID> userIds=source.items().stream().map(AiJob::requestedBy).collect(Collectors.toSet());
        Map<UUID,UserDirectoryPort.UserProfile> profiles=users.findAll(userIds);
        List<Lookup> lookups=source.items().stream().filter(j->j.subjectId()!=null).map(this::lookup).distinct().toList();
        List<CatalogContext> contexts=catalogs.resolve(lookups);Map<Lookup,CatalogContext> byLookup=new HashMap<>();
        for(int i=0;i<lookups.size()&&i<contexts.size();i++)if(contexts.get(i)!=null)byLookup.put(lookups.get(i),contexts.get(i));
        Set<UUID> jobIds=source.items().stream().map(AiJob::id).collect(Collectors.toSet());Map<UUID,String> resultJson=results.findByJobIds(jobIds);
        List<Summary> items=source.items().stream().map(job->summary(job,profiles.get(job.requestedBy()),byLookup.get(lookup(job)),count(resultJson.get(job.id())))).toList();
        return new WorkspacePage<>(items,source.page(),source.size(),source.totalElements(),source.totalPages());
    }
    private Summary summary(AiJob job,UserDirectoryPort.UserProfile creator,CatalogContext context,Integer count){Instant end=job.completedAt()==null?Instant.now(clock):job.completedAt();Long duration=job.startedAt()==null?null:Math.max(0,Duration.between(job.startedAt(),end).toSeconds());boolean stuck=job.status()==JobStatus.PROCESSING&&duration!=null&&duration>=stuckThreshold.toSeconds();return new Summary(job.id(),code(job),job.type(),job.status(),progress(job.status()),creator==null?null:new Creator(creator.userId(),creator.lecturerCode(),creator.fullName(),creator.facultyId(),creator.academicRank(),creator.academicDegree(),creator.avatarUrl()),context,count,job.createdAt(),job.startedAt(),job.completedAt(),job.updatedAt(),duration,stuck);}
    private Lookup lookup(AiJob j){return new Lookup(j.subjectId(),j.chapterId(),j.topicId());}
    private void authorize(AiJob job,UUID actor,String role,String faculty){
        if("SYSTEM_ADMIN".equals(role))return;
        if("SUBJECT_ADMIN".equals(role)&&faculty!=null&&faculty.equals(job.facultyId()))return;
        if("USER".equals(role)&&job.requestedBy().equals(actor))return;
        throw new ForbiddenException("AI_JOB_ACCESS_DENIED","You cannot inspect this AI job");
    }
    private String code(AiJob j){return "AI-"+CODE_DATE.format(j.createdAt())+"-"+j.id().toString().substring(0,6).toUpperCase(Locale.ROOT);}
    private int progress(JobStatus s){return s==JobStatus.COMPLETED||s==JobStatus.FAILED||s==JobStatus.CANCELLED||s==JobStatus.RETRIED?100:s==JobStatus.PROCESSING?55:0;}
    private Integer count(String json){if(json==null)return null;try{JsonNode n=mapper.readTree(json);JsonNode q=n.isArray()?n:n.path("questions");return q.isArray()?q.size():null;}catch(Exception ignored){return null;}}
    private Integer countField(String json,String... names){if(json==null)return null;try{JsonNode node=mapper.readTree(json);for(String name:names){JsonNode value=node.path(name);if(value.isNumber())return value.intValue();}return null;}catch(Exception ignored){return null;}}
    private Object safeValue(JsonNode value){return value.isTextual()?redact(value.asString()):mapper.convertValue(value,Object.class);}
    private JsonNode parse(String json){try{return json==null?mapper.createObjectNode():mapper.readTree(json);}catch(Exception ignored){return mapper.createObjectNode();}}
    private String text(JsonNode n,String field){String value=n.path(field).asString(null);return value==null||value.isBlank()?null:value;}
    private String safeError(String code){if(code==null)return null;return switch(code){case "AI_PROVIDER_TIMEOUT"->"Nhà cung cấp AI phản hồi quá thời gian.";case "AI_PROVIDER_UNAVAILABLE"->"Nhà cung cấp AI tạm thời không khả dụng.";case "INVALID_AI_OUTPUT"->"Kết quả AI không đạt định dạng yêu cầu.";default->"Tác vụ AI không thể hoàn tất. Vui lòng dùng mã lỗi để tra cứu.";};}
    private String redact(String value){if(value==null)return null;return value.replaceAll("(?i)(api[_ -]?key|token|password|secret|credential)\\s*[:=]\\s*[^\\s,;]+","$1=[REDACTED]");}
    private <T>T first(List<T> values){return values==null||values.isEmpty()?null:values.getFirst();}
}
