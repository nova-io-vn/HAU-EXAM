package com.aiservice.application.service;
import com.aiservice.application.port.out.*; import com.aiservice.domain.exception.*; import com.aiservice.domain.model.*;
import java.time.*; import java.util.*; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import tools.jackson.databind.ObjectMapper;
@Service public class ChatService {
 private final ChatRepository chats; private final DocumentRepository docs; private final StoragePort storage; private final TextExtractor extractor; private final AiProvider provider; private final ObjectMapper mapper; private final Clock clock;
 public ChatService(ChatRepository c,DocumentRepository d,StoragePort s,TextExtractor x,AiProvider p,ObjectMapper m,Clock k){chats=c;docs=d;storage=s;extractor=x;provider=p;mapper=m;clock=k;}
 @Transactional public ChatConversation create(UUID user){var now=Instant.now(clock);return chats.save(new ChatConversation(UUID.randomUUID(),user,"Cuộc trò chuyện mới",now,now));}
 @Transactional(readOnly=true) public List<ChatConversation> list(UUID user){return chats.findConversations(user);}
 @Transactional(readOnly=true) public ChatConversation own(UUID id,UUID user){var c=chats.findConversation(id).orElseThrow(()->new NotFoundException("Conversation not found"));if(!c.userId().equals(user))throw new NotFoundException("Conversation not found");return c;}
 @Transactional(readOnly=true) public List<ChatMessage> messages(UUID id,UUID user){own(id,user);return chats.findMessages(id);}
 @Transactional public void delete(UUID id,UUID user){own(id,user);chats.deleteConversation(id);}
 @Transactional public void attach(UUID id,UUID documentId,UUID user){own(id,user);var d=docs.findById(documentId).orElseThrow(()->new NotFoundException("Document not found"));if(!d.ownerId().equals(user))throw new NotFoundException("Document not found");if(!chats.findDocumentIds(id).contains(documentId))chats.attachDocument(id,documentId);}
 @Transactional public void detach(UUID id,UUID documentId,UUID user){own(id,user);chats.detachDocument(id,documentId);}
 @Transactional public List<UUID> documents(UUID id,UUID user){own(id,user);return chats.findDocumentIds(id);}
 @Transactional public ChatMessage send(UUID id,String content,UUID user){var c=own(id,user);var now=Instant.now(clock);chats.saveMessage(new ChatMessage(UUID.randomUUID(),id,ChatRole.USER,content,"COMPLETED",now));StringBuilder context=new StringBuilder();for(UUID docId:chats.findDocumentIds(id)){var d=docs.findById(docId).orElse(null);if(d!=null)context.append(extractor.extract(d.contentType(),storage.read(d.storageKey()))).append("\n");}String request;try{request=mapper.writeValueAsString(Map.of("conversationId",id,"message",content,"history",chats.findMessages(id)));}catch(Exception e){request=content;}String raw=provider.chat(context.toString(),request);String answer;try{var n=mapper.readTree(raw);answer=n.path("answer").asText(raw);}catch(Exception e){answer=raw;}var assistant=new ChatMessage(UUID.randomUUID(),id,ChatRole.ASSISTANT,answer,"COMPLETED",Instant.now(clock));chats.saveMessage(assistant);chats.save(new ChatConversation(c.id(),c.userId(),c.title().equals("Cuộc trò chuyện mới")?title(content):c.title(),c.createdAt(),Instant.now(clock)));return assistant;}
 private String title(String s){return s.length()>60?s.substring(0,57)+"...":s;}
}
