package com.aiservice.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String HEADER = "X-Correlation-Id";
    private static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String correlationId = safe(request.getHeader(HEADER));
        long started = System.nanoTime();
        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);
        try {
            chain.doFilter(request, response);
            log.debug("Request completed method={} path={} status={} durationMs={}", request.getMethod(), request.getRequestURI(), response.getStatus(), elapsed(started));
        } catch (IOException | ServletException | RuntimeException exception) {
            log.warn("Request failed method={} path={} durationMs={} errorType={}", request.getMethod(), request.getRequestURI(), elapsed(started), exception.getClass().getSimpleName());
            throw exception;
        } finally { MDC.remove(MDC_KEY); }
    }

    private String safe(String value) { try { return value != null && value.length() <= 36 ? UUID.fromString(value).toString() : UUID.randomUUID().toString(); } catch (IllegalArgumentException ignored) { return UUID.randomUUID().toString(); } }
    private long elapsed(long started) { return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started); }
}
