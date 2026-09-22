package com.aiservice.presentation.controller;
import com.aiservice.application.service.ChatService; import com.aiservice.domain.model.*; import com.aiservice.presentation.response.ApiResponse; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.Instant; import java.util.*; import org.springframework.security.core.annotation.AuthenticationPrincipal; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/ai/chat") public class AiChatController {
 private final ChatService service; public AiChatController(ChatService s){service=s;} private UUID user(Jwt j){return UUID.fromString(j.getSubject());}
 public record ConversationView(UUID id,String title,Instant createdAt,Instant updatedAt){static ConversationView of(ChatConversation c){return new ConversationView(c.id(),c.title(),c.createdAt(),c.updatedAt());}}
 public record MessageView(UUID id,String role,String content,String status,Instant createdAt){static MessageView of(ChatMessage m){return new MessageView(m.id(),m.role().name(),m.content(),m.status(),m.createdAt());}}
 public record SendRequest(@NotBlank @Size(max=4000) String content){}
 public record AttachRequest(@NotNull UUID documentId){}
 @GetMapping("/conversations") public ApiResponse<List<ConversationView>> list(@AuthenticationPrincipal Jwt j){return ApiResponse.ok(service.list(user(j)).stream().map(ConversationView::of).toList());}
 @PostMapping("/conversations") public ApiResponse<ConversationView> create(@AuthenticationPrincipal Jwt j){return ApiResponse.ok(ConversationView.of(service.create(user(j))));}
 @GetMapping("/conversations/{id}/messages") public ApiResponse<List<MessageView>> messages(@PathVariable UUID id,@AuthenticationPrincipal Jwt j){return ApiResponse.ok(service.messages(id,user(j)).stream().map(MessageView::of).toList());}
 @PostMapping("/conversations/{id}/messages") public ApiResponse<MessageView> send(@PathVariable UUID id,@AuthenticationPrincipal Jwt j,@Valid @RequestBody SendRequest r){return ApiResponse.ok(MessageView.of(service.send(id,r.content().trim(),user(j))));}
 @DeleteMapping("/conversations/{id}") public ApiResponse<Void> delete(@PathVariable UUID id,@AuthenticationPrincipal Jwt j){service.delete(id,user(j));return ApiResponse.ok(null);}
 @PostMapping("/conversations/{id}/documents") public ApiResponse<List<UUID>> attach(@PathVariable UUID id,@AuthenticationPrincipal Jwt j,@Valid @RequestBody AttachRequest r){service.attach(id,r.documentId(),user(j));return ApiResponse.ok(service.documents(id,user(j)));}
 @DeleteMapping("/conversations/{id}/documents/{documentId}") public ApiResponse<List<UUID>> detach(@PathVariable UUID id,@PathVariable UUID documentId,@AuthenticationPrincipal Jwt j){service.detach(id,documentId,user(j));return ApiResponse.ok(service.documents(id,user(j)));}
 @GetMapping("/conversations/{id}/documents") public ApiResponse<List<UUID>> documents(@PathVariable UUID id,@AuthenticationPrincipal Jwt j){return ApiResponse.ok(service.documents(id,user(j)));}
}
