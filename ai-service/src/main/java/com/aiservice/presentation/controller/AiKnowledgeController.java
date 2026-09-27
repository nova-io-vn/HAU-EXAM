package com.aiservice.presentation.controller;

import com.aiservice.application.service.AiKnowledgeService;
import com.aiservice.presentation.response.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/ai-policy/documents")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class AiKnowledgeController {
 private final AiKnowledgeService service; public AiKnowledgeController(AiKnowledgeService service){this.service=service;}
 public record View(UUID id,String title,String originalFileName,String contentType,long fileSize,String status,boolean enabled,long chunkCount,UUID createdBy,java.time.Instant createdAt,java.time.Instant updatedAt,String processingError){}
 public record Content(String title,String originalFileName,String extractedText,long chunkCount){}
 public record Chunk(UUID id,int chunkIndex,String text,String metadataJson){}
 private View view(com.aiservice.infrastructure.persistence.entity.AiKnowledgeDocumentEntity d){return new View(d.id,d.title,d.originalFileName,d.contentType,d.fileSize,d.status,d.enabled,service.chunkCount(d.id),d.createdBy,d.createdAt,d.updatedAt,d.processingError);}
 @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public ApiResponse<View> upload(@AuthenticationPrincipal Jwt j,@RequestParam @NotBlank String title,@RequestParam(required=false) String description,@RequestPart("file") MultipartFile file)throws IOException{return ApiResponse.ok(view(service.upload(UUID.fromString(j.getSubject()),title,description,file.getOriginalFilename(),file.getContentType(),file.getSize(),file.getInputStream())));}
 @GetMapping public ApiResponse<List<View>> list(){return ApiResponse.ok(service.list().stream().map(this::view).toList());}
 @GetMapping("/{id}/content") public ApiResponse<Content> content(@PathVariable UUID id){var d=service.document(id);return ApiResponse.ok(new Content(d.title,d.originalFileName,d.extractedText,service.chunkCount(id)));}
 @GetMapping("/{id}/chunks") public ApiResponse<List<Chunk>> chunks(@PathVariable UUID id){return ApiResponse.ok(service.chunks(id).stream().map(c->new Chunk(c.id,c.chunkIndex,c.text,c.metadataJson)).toList());}
 @GetMapping("/debug/retrieval") public ApiResponse<List<AiKnowledgeService.DebugSource>> debug(@RequestParam String query,@RequestParam(defaultValue="8") int topK){return ApiResponse.ok(service.debugRetrieve(query,topK));}
 @PatchMapping("/{id}") public ApiResponse<View> toggle(@PathVariable UUID id,@RequestParam boolean enabled){return ApiResponse.ok(view(service.toggle(id,enabled)));}
 @PostMapping("/{id}/reprocess") public ApiResponse<Void> reprocess(@PathVariable UUID id){service.reprocess(id);return ApiResponse.ok(null);}
 @DeleteMapping("/{id}") public ApiResponse<Void> delete(@PathVariable UUID id){service.delete(id);return ApiResponse.ok(null);}
}
