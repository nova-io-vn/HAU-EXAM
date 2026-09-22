package com.notificationservice.infrastructure.websocket;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConfiguration.class);
    private final String[] origins;
    private final JwtDecoder jwtDecoder;

    public WebSocketConfiguration(@Value("${notification.websocket.allowed-origins}") String origins, JwtDecoder jwtDecoder) {
        this.origins = origins.split(",");
        this.jwtDecoder = jwtDecoder;
    }

    @Override public void registerStompEndpoints(StompEndpointRegistry registry) { registry.addEndpoint("/ws").setAllowedOriginPatterns(origins); }
    @Override public void configureMessageBroker(MessageBrokerRegistry registry) { registry.enableSimpleBroker("/queue"); registry.setUserDestinationPrefix("/user"); }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) return message;
                if (StompCommand.CONNECT.equals(accessor.getCommand())) authenticate(accessor);
                if ((StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) && accessor.getUser() == null)
                    throw new BadCredentialsException("Authenticated STOMP session is required");
                return message;
            }
        });
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) throw new BadCredentialsException("Missing STOMP bearer token");
        var jwt = jwtDecoder.decode(authorization.substring(7));
        String role = jwt.getClaimAsString("role");
        var authorities = role == null ? List.<SimpleGrantedAuthority>of() : List.of(new SimpleGrantedAuthority("ROLE_" + role));
        accessor.setUser(new JwtAuthenticationToken(jwt, authorities));
        log.info("WebSocket STOMP session authenticated; userId={} role={}", jwt.getSubject(), role);
    }
}
