package io.will.webfluxdemo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// @Order(-2) sets higher priority than default error handler DefaultErrorWebExceptionHandler, which is registered at @Order(-1).
@Component
@Order(-2)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        // Log the error with detailed information
        logError(exchange, ex);
        
        // Set response status
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        // Don't set content type here as it may conflict with Spring's handling
        
        // Create error response
        Map<String, Object> errorResponse = createErrorResponse(exchange, ex);
        String errorJson = convertToJson(errorResponse);
        
        // Write error response
        DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    private void logError(ServerWebExchange exchange, Throwable ex) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        String remoteAddress = exchange.getRequest().getRemoteAddress() != null ? 
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "unknown";
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        
        logger.error("=== GLOBAL ERROR HANDLER TRIGGERED ===");
        logger.error("Path: {}", path);
        logger.error("Method: {}", method);
        logger.error("Remote Address: {}", remoteAddress);
        logger.error("User-Agent: {}", userAgent);
        logger.error("Error Type: {}", ex.getClass().getSimpleName());
        logger.error("Error Message: {}", ex.getMessage());
        logger.error("Stack Trace:", ex);
        logger.error("=== END ERROR LOG ===");
    }
    
    private Map<String, Object> createErrorResponse(ServerWebExchange exchange, Throwable ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("method", exchange.getRequest().getMethod().name());
        errorResponse.put("status", 500);
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
        errorResponse.put("exception", ex.getClass().getSimpleName());
        
        // Add request ID if available
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-ID");
        if (requestId != null) {
            errorResponse.put("requestId", requestId);
        }
        
        return errorResponse;
    }
    
    private String convertToJson(Map<String, Object> errorResponse) {
        // Simple JSON conversion - in production, use Jackson ObjectMapper
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : errorResponse.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
}
