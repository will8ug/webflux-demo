package io.will.webfluxdemo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class CustomExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleRuntimeException(
            RuntimeException ex, ServerWebExchange exchange) {
        
        logException(ex, exchange, "RuntimeException");
        
        Map<String, Object> errorResponse = createErrorResponse(exchange, ex, 
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgumentException(
            IllegalArgumentException ex, ServerWebExchange exchange) {
        
        logException(ex, exchange, "IllegalArgumentException");
        
        Map<String, Object> errorResponse = createErrorResponse(exchange, ex, 
            HttpStatus.BAD_REQUEST, "Bad Request");
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        
        logException(ex, exchange, "GenericException");
        
        Map<String, Object> errorResponse = createErrorResponse(exchange, ex, 
            HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse));
    }

    private void logException(Throwable ex, ServerWebExchange exchange, String exceptionType) {
        String requestId = (String) exchange.getAttributes().get("requestId");
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        
        logger.error("=== {} HANDLED ===", exceptionType);
        logger.error("Request ID: {}", requestId);
        logger.error("Path: {} {}", method, path);
        logger.error("Error Type: {}", ex.getClass().getSimpleName());
        logger.error("Error Message: {}", ex.getMessage());
        logger.error("Stack Trace:", ex);
        logger.error("==================");
    }

    private Map<String, Object> createErrorResponse(ServerWebExchange exchange, Throwable ex, 
                                                   HttpStatus status, String error) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", exchange.getRequest().getPath().value());
        errorResponse.put("method", exchange.getRequest().getMethod().name());
        errorResponse.put("status", status.value());
        errorResponse.put("error", error);
        errorResponse.put("message", ex.getMessage() != null ? ex.getMessage() : "An error occurred");
        errorResponse.put("exception", ex.getClass().getSimpleName());
        
        String requestId = (String) exchange.getAttributes().get("requestId");
        if (requestId != null) {
            errorResponse.put("requestId", requestId);
        }
        
        return errorResponse;
    }
}
