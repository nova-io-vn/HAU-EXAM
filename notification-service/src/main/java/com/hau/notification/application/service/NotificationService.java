package com.hau.notification.application.service;
import com.hau.notification.application.port.in.NotificationUseCase;import com.hau.notification.domain.exception.*;import com.hau.notification.domain.model.Notification;import com.hau.notification.domain.repository.NotificationRepository;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import java.time.*;import java.util.*;
@Service public class NotificationService implements NotificationUseCase{
 private final NotificationRepository repository;private final Clock clock;public NotificationService(NotificationRepository r,Clock c){repository=r;clock=c;}
 @Transactional(readOnly=true)public List<Notification> list(UUID u,int p,int s){if(p<0||s<1||s>100)throw new IllegalArgumentException("Invalid page");return repository.findByUser(u,p,s);}
 @Transactional(readOnly=true)public long unreadCount(UUID u){return repository.countUnread(u);}
 @Transactional public Notification markRead(UUID u,UUID id){Notification n=repository.findById(id).orElseThrow(()->new NotificationNotFoundException(id));if(!n.getUserId().equals(u))throw new ForbiddenNotificationAccessException();return repository.save(n.markRead(Instant.now(clock)));}
 @Transactional public int markAllRead(UUID u){return repository.markAllRead(u,Instant.now(clock));}
}
